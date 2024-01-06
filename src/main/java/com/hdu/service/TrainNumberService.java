package com.hdu.service;

import com.alibaba.otter.canal.protocol.CanalEntry;
import com.hdu.common.TrainType;
import com.hdu.dao.TrainNumberDetailMapper;
import com.hdu.dao.TrainNumberMapper;
import com.hdu.exception.BusinessException;
import com.hdu.model.TrainNumber;
import com.hdu.model.TrainNumberDetail;
import com.hdu.reqparm.TrainNumberParam;
import com.hdu.utils.BeanValidator;
import com.hdu.utils.JsonMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
@Slf4j
public class TrainNumberService {
    @Resource
    private TrainNumberMapper trainNumberMapper;
    @Resource
    private TrainNumberDetailMapper trainNumberDetailMapper;
    @Resource
    private TrainCacheService trainCacheService;

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

    public void handle(List<CanalEntry.Column> columns, CanalEntry.EventType eventType) {
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

//        saveEs(detailList,trainNumber);
//        log.info("trainNumber:{} detailList update es", trainNumber.getName());
    }
}















