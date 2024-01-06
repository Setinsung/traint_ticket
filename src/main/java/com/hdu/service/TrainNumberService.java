package com.hdu.service;

import com.alibaba.otter.canal.protocol.CanalEntry;
import com.google.common.base.Splitter;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.hdu.common.TrainEsConstant;
import com.hdu.common.TrainType;
import com.hdu.dao.TrainNumberDetailMapper;
import com.hdu.dao.TrainNumberMapper;
import com.hdu.es.EsClient;
import com.hdu.exception.BusinessException;
import com.hdu.model.TrainNumber;
import com.hdu.model.TrainNumberDetail;
import com.hdu.reqparm.TrainNumberParam;
import com.hdu.utils.BeanValidator;
import com.hdu.utils.JsonMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.get.MultiGetItemResponse;
import org.elasticsearch.action.get.MultiGetRequest;
import org.elasticsearch.action.get.MultiGetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.update.UpdateRequest;
import org.springframework.stereotype.Service;
import com.google.common.cache.Cache;
import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class TrainNumberService {
    @Resource
    private TrainNumberMapper trainNumberMapper;
    @Resource
    private TrainNumberDetailMapper trainNumberDetailMapper;
    @Resource
    private TrainCacheService trainCacheService;

    @Resource
    private EsClient esClient;


    public List<TrainNumber> getAll() {
        return trainNumberMapper.getAll();
    }


    public void save(TrainNumberParam param) {
        BeanValidator.check(param);
        TrainNumber origin = trainNumberMapper.findByName(param.getName());
        if (origin != null) {
            throw new BusinessException("车系已经存在");
        }
        TrainNumber trainNumber = TrainNumber.builder()
                .name(param.getName())
                .trainType(param.getTrainType())
                .type(param.getType().shortValue())
                .seatNum(TrainType.valueOf(param.getTrainType()).getCount())
                .build();
        trainNumberMapper.insertSelective(trainNumber);
    }

    public void update(TrainNumberParam param) {
        BeanValidator.check(param);
        TrainNumber origin = trainNumberMapper.findByName(param.getName());
        if (origin != null && origin.getId().intValue() != param.getId().intValue()) {
            throw new BusinessException("车系已经存在");
        }

        // 可以考虑根据seat判断是否分配过，不推荐修改
        TrainNumber trainNumber = TrainNumber.builder()
                .id(param.getId())
                .name(param.getName())
                .trainType(param.getTrainType())
                .type(param.getType().shortValue())
                .seatNum(TrainType.valueOf(param.getTrainType()).getCount())
                .build();
        trainNumberMapper.updateByPrimaryKeySelective(trainNumber);
    }

    public void handle(List<CanalEntry.Column> columns, CanalEntry.EventType eventType) throws Exception {
        if (eventType != CanalEntry.EventType.UPDATE) {
            log.info("not update, no need care");
            return;
        }
        int trainNumberId = 0;
        for (CanalEntry.Column column :
                columns) {
            if (column.getName().equals("id")) {
                trainNumberId = Integer.parseInt(column.getValue());
                break;
            }
        }
        TrainNumber trainNumber = trainNumberMapper.selectByPrimaryKey(trainNumberId);
        if (trainNumber == null) {
            log.error("not found trainNumber, trainNumberId:{}", trainNumberId);
            return;
        }
        List<TrainNumberDetail> detailList = trainNumberDetailMapper.getByTrainNumberId(trainNumberId);
        if (CollectionUtils.isEmpty(detailList)) {
            log.warn("no detail, no need care, trainNumber:{}", trainNumber.getName());
            return;
        }
        trainCacheService.set("TN_" + trainNumber.getName(), JsonMapper.obj2String(detailList));
        log.info("trainNumber:{} detailList update", trainNumber.getName());

        saveEs(detailList, trainNumber);
        log.info("trainNumber:{} detailList update es", trainNumber.getName());
    }

    private void saveEs(List<TrainNumberDetail> detailList, TrainNumber trainNumber) throws Exception {
        /**
         * A->B fromStationId->toStataionId
         *
         * trainNumber: A->B->C   D386:北京->锦州->大连    D386:北京->鞍山->大连
         * 北京-大连？
         * D386:北京-锦州、锦州-大连、北京-大连
         * D386:北京-鞍山、鞍山-大连、北京-大连
         *
         * fromStationId->toStataionId:trainNumberId1,trainNumberId2,...
         */
        List<String> list = Lists.newArrayList();
        if (detailList.size() == 1) {
            int fromStationId = trainNumber.getFromStationId();
            int toStationId = trainNumber.getToStationId();
            list.add(fromStationId + "_" + toStationId);
        } else { //多段，保证detailList有序
            for (int i = 0; i < detailList.size(); i++) {
                int tempFromStationId = detailList.get(i).getFromStationId();
                for (int j = i; j < detailList.size(); j++) {
                    int tempToStationId = detailList.get(j).getToStationId();
                    list.add(tempFromStationId + "_" + tempToStationId);
                }
            }
        }

        // 组装批量的请求，获取es已经存储的数据
        MultiGetRequest multiGetRequest = new MultiGetRequest();
        for (String item : list) {
            multiGetRequest.add(new MultiGetRequest.Item(TrainEsConstant.INDEX, TrainEsConstant.TYPE, item));
        }
        MultiGetResponse multiGetItemResponses = esClient.multiGet(multiGetRequest);

        BulkRequest bulkRequest = new BulkRequest();

        // 处理每一项
        for (MultiGetItemResponse itemResponse : multiGetItemResponses.getResponses()) {
            if (itemResponse.isFailed()) {
                log.error("multiGet item failed, itemResponse:{}", itemResponse);
                continue;
            }
            GetResponse getResponse = itemResponse.getResponse();
            if (getResponse == null) {
                log.error("mutiGet item getResponse is null, itemResponse:{}", itemResponse);
                continue;
            }

            // 存储更新的数据
            Map<String, Object> dataMap = Maps.newHashMap();
            Map<String, Object> map = getResponse.getSourceAsMap();
            if (!getResponse.isExists() || map == null) {
                // add index
                dataMap.put(TrainEsConstant.COLUMN_TRAIN_NUMBER, trainNumber.getName());
                IndexRequest indexRequest = new IndexRequest(TrainEsConstant.INDEX, TrainEsConstant.TYPE, getResponse.getId()).source(dataMap);
                bulkRequest.add(indexRequest);
                continue;
            }

            String origin = (String) map.get(TrainEsConstant.COLUMN_TRAIN_NUMBER);
            Set<String> set = Sets.newHashSet(Splitter.on(",").trimResults().omitEmptyStrings().split(origin));
            if (!set.contains(trainNumber.getName())) {
                // update index
                dataMap.put(TrainEsConstant.COLUMN_TRAIN_NUMBER, origin + "," + trainNumber.getName());
                UpdateRequest updateRequest = new UpdateRequest(TrainEsConstant.INDEX, TrainEsConstant.TYPE, getResponse.getId()).doc(dataMap);
                bulkRequest.add(updateRequest);
            }
        }

        // 批量更新es数据
        BulkResponse bulkItemResponses = esClient.bulk(bulkRequest);
        log.info("es buld, response:{}", JsonMapper.obj2String(bulkItemResponses));
        if (bulkItemResponses.hasFailures()) {
            throw new RuntimeException("es bulk failure");
        }
    }

    private static Cache<String, TrainNumber> trainNumberCache = CacheBuilder
            .newBuilder()
            .expireAfterWrite(5, TimeUnit.MINUTES)
            .build();

    public TrainNumber findByNameFromCache(String name) {
        TrainNumber trainNumber = trainNumberCache.getIfPresent(name);
        if (trainNumber != null) {
            return trainNumber;
        }
        trainNumber = trainNumberMapper.findByName(name);
        if (trainNumber != null) {
            trainNumberCache.put(name, trainNumber);
        }
        return trainNumber;
    }

}















