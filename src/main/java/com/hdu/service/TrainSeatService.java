package com.hdu.service;

import com.alibaba.otter.canal.protocol.CanalEntry;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Table;
import com.hdu.common.*;
import com.hdu.dao.TrainNumberDetailMapper;
import com.hdu.dao.TrainNumberMapper;
import com.hdu.dto.TrainNumberLeftDto;
import com.hdu.es.EsClient;
import com.hdu.exception.BusinessException;
import com.hdu.model.TrainNumber;
import com.hdu.model.TrainNumberDetail;
import com.hdu.model.TrainSeat;
import com.hdu.reqparm.FrontSearchParam;
import com.hdu.reqparm.GenerateTicketParam;
import com.hdu.reqparm.PublishTicketParam;
import com.hdu.reqparm.TrainSeatSearchParam;
import com.hdu.seatDao.TrainSeatMapper;
import com.hdu.utils.BeanValidator;
import com.hdu.utils.JsonMapper;
import com.hdu.utils.StringUtil;
import javafx.util.Pair;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.codehaus.jackson.type.TypeReference;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@Slf4j
public class TrainSeatService {
    @Resource
    private TrainNumberMapper trainNumberMapper;
    @Resource
    private TrainNumberDetailMapper trainNumberDetailMapper;
    @Resource
    private TrainSeatMapper trainSeatMapper;
    @Resource
    private TransactionService transactionService;

    @Resource
    private TrainNumberService trainNumberService;

    @Resource
    private TrainCacheService trainCacheService;

    @Resource
    private EsClient esClient;

    public List<TrainSeat> searchList(TrainSeatSearchParam param, PageQuery pageQuery) {
        BeanValidator.check(param);
        BeanValidator.check(pageQuery);
        TrainNumber trainNumber = trainNumberMapper.findByName(param.getTrainNumber());
        if (trainNumber == null) {
            throw new BusinessException("查询的车次不存在");
        }
        return trainSeatMapper.searchList(trainNumber.getId(), param.getTicket(), param.getStatus(), param.getCarriageNum(),
                param.getRowNum(), param.getSeatNum(), pageQuery.getOffset(), pageQuery.getPageSize());
    }

    public int countList(TrainSeatSearchParam param) {
        BeanValidator.check(param);
        TrainNumber trainNumber = trainNumberMapper.findByName(param.getTrainNumber());
        if (trainNumber == null) {
            throw new BusinessException("查询的车次不存在");
        }
        return trainSeatMapper.countList(trainNumber.getId(), param.getTicket(), param.getStatus(), param.getCarriageNum(),
                param.getRowNum(), param.getSeatNum());
    }

    public void generate(GenerateTicketParam param) {
        BeanValidator.check(param);
        // 车次
        TrainNumber trainNumber = trainNumberMapper.selectByPrimaryKey(param.getTrainNumberId());
        if (trainNumber == null) {
            throw new BusinessException("该车次不存在");
        }
        // 车次详情
        List<TrainNumberDetail> detailList = trainNumberDetailMapper.getByTrainNumberId(param.getTrainNumberId());
        if (CollectionUtils.isEmpty(detailList)) {
            throw new BusinessException("该车次暂无详情，请添加详情");
        }
//        Collections.sort(detailList, Comparator.comparingInt(TrainNumberDetail::getStationIndex));
        // 座位类型
        TrainType trainType = TrainType.valueOf(trainNumber.getTrainType());
        // 座位配置
        Table<Integer, Integer, Pair<Integer, Integer>> seatTable = TrainTypeSeatConstant.getTable(trainType);
        // 时间
        ZoneId zoneId = ZoneId.systemDefault();
        LocalDateTime fromLocalDateTime = LocalDateTime.parse(param.getFromTime(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));

        List<TrainSeat> list = Lists.newArrayList();
        String ticket = fromLocalDateTime.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        // 遍历车次的每一段
        for (TrainNumberDetail detail : detailList) {
            // 每一段的发车时间
            Date fromDate = Date.from(fromLocalDateTime.atZone(zoneId).toInstant());
            // 每一段的到站时间
            Date toDate = Date.from(fromLocalDateTime.plusMinutes(detail.getRelativeMinute()).atZone(zoneId).toInstant());

            Map<Integer, Integer> seatMoneyMap = splitSeatMoney(detail.getMoney());
            // 遍历每一节车厢，每一排
            for (Table.Cell<Integer, Integer, Pair<Integer, Integer>> cell : seatTable.cellSet()) {
                // 当前车厢，获取座位数及座位等级
                Integer carriage = cell.getRowKey();
                // 当前车厢的排，锁定座位数
                Integer row = cell.getColumnKey();
                // 座位的等级
                TrainSeatLevel seatLevel = TrainTypeSeatConstant.getSeatLevel(trainType, carriage);
                // 座位的价钱
                Integer money = seatMoneyMap.get(seatLevel.getLevel());
                // 获取指定车厢指定排的座位号的范围
                Pair<Integer, Integer> rowSeatRange = seatTable.get(carriage, row);

                //遍历每一排的每个座位
                for (int index = rowSeatRange.getKey(); index <= rowSeatRange.getValue(); index++) {
                    // 生成座位
                    TrainSeat trainSeat = TrainSeat.builder()
                            .carriageNumber(carriage)
                            .rowNumber(row)
                            .seatNumber(index)
                            .money(money)
                            .ticket(ticket)
                            .seatLevel(seatLevel.getLevel())
                            .trainStart(fromDate)
                            .trainEnd(toDate)
                            .trainNumberId(trainNumber.getId())
                            .showNumber(carriage + "车" + row + "排" + index)
                            .status(0)
                            .fromStationId(detail.getFromStationId())
                            .toStationId(detail.getToStationId())
                            .build();
                    list.add(trainSeat);
                }
            }
            fromLocalDateTime = fromLocalDateTime.plusMinutes(detail.getRelativeMinute() + detail.getWaitMinute());
        }
        transactionService.batchInsertSeat(list);
    }

    // 这种写法是错误的，请一定重视
    @Transactional(rollbackFor = Exception.class)
    public void batchInsertSeat(List<TrainSeat> list) {
        List<List<TrainSeat>> trainTicketPartitionList = Lists.partition(list, 1000);
        trainTicketPartitionList.parallelStream().forEach(partitionList -> {
            // 批量插入
            trainSeatMapper.batchInsert(partitionList);
        });
    }

    private Map<Integer, Integer> splitSeatMoney(String money) {
        // money: 0:100,1:90,2:80
        try {
            List<String> list = Splitter.on(",").trimResults().omitEmptyStrings().splitToList(money);
            Map<Integer, Integer> map = Maps.newHashMap();
            list.stream().forEach(str -> {
                String[] split = str.split(":");
                map.put(Integer.parseInt(split[0]), Integer.parseInt(split[1]));
            });
            return map;
        } catch (Exception e) {
            throw new BusinessException("价钱解析出错，请检查车站详情配置");
        }
    }


    @Transactional(rollbackFor = Exception.class)
    public void publish(PublishTicketParam param) {
        BeanValidator.check(param);
        TrainNumber trainNumber = trainNumberMapper.findByName(param.getTrainNumber());
        if (trainNumber == null) {
            throw new BusinessException("车次不存在");
        }
        List<Long> trainSeatIdList = StringUtil.splitToListLong(param.getTrainSeatIds());
        List<List<Long>> idPartitionList = Lists.partition(trainSeatIdList, 1000);
        for (List<Long> partitionList : idPartitionList) {
            int count = trainSeatMapper.batchPublish(trainNumber.getId(), partitionList);
            if (count != partitionList.size()) {
                throw new BusinessException("部分座位不满足条件，请重新查询【初始】状态的座位进行放票");
            }
        }
    }

    public void handle(List<CanalEntry.Column> columns, CanalEntry.EventType eventType) {
        if (eventType != CanalEntry.EventType.UPDATE) {
            log.info("not update, no need care");
            return;
        }
        TrainSeat trainSeat = new TrainSeat();
        boolean isStatusUpdated = false;
        for (CanalEntry.Column column : columns) {
            if (column.getName().equals("status")) {
                trainSeat.setStatus(Integer.parseInt(column.getValue()));
                if (column.getUpdated()) {
                    isStatusUpdated = true;
                } else {
                    break;
                }
            } else if (column.getName().equals("id")) {
                trainSeat.setId(Long.parseLong(column.getValue()));
            } else if (column.getName().equals("carriage_number")) {
                trainSeat.setCarriageNumber(Integer.parseInt(column.getValue()));
            } else if (column.getName().equals("row_number")) {
                trainSeat.setRowNumber(Integer.parseInt(column.getValue()));
            } else if (column.getName().equals("seat_number")) {
                trainSeat.setSeatNumber(Integer.parseInt(column.getValue()));
            } else if (column.getName().equals("train_number_id")) {
                trainSeat.setTrainNumberId(Integer.parseInt(column.getValue()));

            } else if (column.getName().equals("ticket")) {
                trainSeat.setTicket(column.getValue());
            } else if (column.getName().equals("from_station_id")) {
                trainSeat.setFromStationId(Integer.parseInt(column.getValue()));
            } else if (column.getName().equals("to_station_id")) {
                trainSeat.setToStationId(Integer.parseInt(column.getValue()));
            }
        }
        if (!isStatusUpdated) {
            log.info("status not update,no need care");
            return;
        }
        log.info("train seat update, trainSeat:{}", trainSeat);

        /**
         * 1、指定座位是否被占
         * key：车次_日期，D386_20201211
         * value：0，空闲，1，占座
         *
         * 2、每个座位详情剩余座位
         * key：车次_日期_Count,D386_20201211_Count
         * value：实际座位数
         */
        TrainNumber trainNumber = trainNumberMapper.selectByPrimaryKey(trainSeat.getTrainNumberId());
        if (trainSeat.getStatus() == 1) { // 放票
            trainCacheService.hset(trainNumber.getName() + "_" + trainSeat.getTicket(),
                    trainSeat.getCarriageNumber() + "_" + trainSeat.getRowNumber() + "_" + trainSeat.getSeatNumber() + "_" +
                            trainSeat.getFromStationId() + "_" + trainSeat.getToStationId(), "0");
            trainCacheService.hincrBy(trainNumber.getName() + "_" + trainSeat.getTicket() + "_Count",
                    trainSeat.getFromStationId() + "_" + trainSeat.getToStationId(),
                    1L);
            log.info("seat+1,trainNumber:{},trainSeat:{}", trainNumber.getName(), trainSeat);
        } else if (trainSeat.getStatus() == 2) { // 占票
            trainCacheService.hset(trainNumber.getName() + "_" + trainSeat.getTicket(),
                    trainSeat.getCarriageNumber() + "_" + trainSeat.getRowNumber() + "_" + trainSeat.getSeatNumber() + "_" +
                            trainSeat.getFromStationId() + "_" + trainSeat.getToStationId(), "1");
            trainCacheService.hincrBy(trainNumber.getName() + "_" + trainSeat.getTicket() + "_Count",
                    trainSeat.getFromStationId() + "_" + trainSeat.getToStationId(),
                    -1L);
            log.info("seat-1,trainNumber:{},trainSeat:{}", trainNumber.getName(), trainSeat);
        } else {
            log.info("status update not 1 or 2, no need care");
        }
    }

    public List<TrainNumberLeftDto> FrontSearch(FrontSearchParam param) throws Exception {
        BeanValidator.check(param);
        List<TrainNumberLeftDto> dtoList = Lists.newArrayList();
        // 从es里获取满足条件的车次
        GetRequest getRequest = new GetRequest(TrainEsConstant.INDEX, TrainEsConstant.TYPE, param.getFromStationId() + "_" + param.getToStationId());
        GetResponse getResponse = esClient.get(getRequest);
        if (getResponse == null) {
            throw new BusinessException("数据查询失败，请重试");
        }
        Map<String, Object> map = getResponse.getSourceAsMap();
        if (MapUtils.isEmpty(map)) {
            return dtoList;
        }

        String trainNumbers = (String) map.get(TrainEsConstant.COLUMN_TRAIN_NUMBER); // D9,D386
        // 拆分出所有车次
        List<String> numberList = Splitter.on(",").trimResults().omitEmptyStrings().splitToList(trainNumbers);

        numberList.parallelStream().forEach(number -> {
            TrainNumber trainNumber = trainNumberService.findByNameFromCache(number);
            if (trainNumber == null) {
                return;
            }
            String detailStr = trainCacheService.get("TN_" + number);
            List<TrainNumberDetail> detailList = JsonMapper.string2Obj(detailStr, new TypeReference<List<TrainNumberDetail>>() {
            });
            Map<Integer, TrainNumberDetail> detailMap = Maps.newHashMap();
            detailList.stream().forEach(detail -> detailMap.put(detail.getFromStationId(), detail));
            /**
             * detailList:{1,2},{2,3},{3,4}
             * 1->{1,2},2->{2,3} ... 5 -> {5,6}
             * param:2->5
             * target:{2,3},{3,4},{4,5}
             * detailMap 2->{2,3}->3->{3,4}->4->{4,5}
             *
             * {2,3}:5,{3,4}:3,{4,5}:10  -> left:3 获取所有段最少的座位
             */
            int curFromStationId = param.getFromStationId();
            int targetToStationId = param.getToStationId();
            long min = Long.MAX_VALUE;
            boolean isSuccess = false;
            String redisKey = number + "_" + param.getDate() + "_Count";

            while (true) {
                TrainNumberDetail detail = detailMap.get(curFromStationId);
                if (detail == null) {
                    log.error("detail is null, stationId:{}, number:{}", curFromStationId, number);
                    break;
                }

                // 从redis里取出本短详情剩余的座位，并更新整体最小的座位数
                min = Math.min(min, NumberUtils.toLong(trainCacheService.hget(redisKey, detail.getFromStationId() + "_" + detail.getToStationId()), 0L));

                if (detail.getToStationId() == targetToStationId) {
                    isSuccess = true;
                    break;
                }

                // 下次查询的起始站是本次详情的到达站
                curFromStationId = detail.getToStationId();
            }
            if (isSuccess) {
                dtoList.add(new TrainNumberLeftDto(trainNumber.getId(), number, min));
            }
        });
        return dtoList;
    }
}


















