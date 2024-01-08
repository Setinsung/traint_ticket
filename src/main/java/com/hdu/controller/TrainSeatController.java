package com.hdu.controller;

import com.hdu.common.JsonData;
import com.hdu.common.PageQuery;
import com.hdu.common.PageResult;
import com.hdu.dto.TrainSeatDto;
import com.hdu.model.TrainSeat;
import com.hdu.model.TrainStation;
import com.hdu.reqparm.GenerateTicketParam;
import com.hdu.reqparm.PublishTicketParam;
import com.hdu.reqparm.TrainSeatSearchParam;
import com.hdu.service.TrainSeatService;
import com.hdu.service.TrainStationService;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/admin/train/seat")
public class TrainSeatController {
    @Resource
    private TrainSeatService trainSeatService;
    @Resource
    private TrainStationService trainStationService;

    @RequestMapping("/search")
    public JsonData search(TrainSeatSearchParam param, PageQuery pageQuery) {
        int total = trainSeatService.countList(param);
        if (total == 0) {
            return JsonData.success(PageResult.<TrainSeat>builder().total(0).build());
        }
        List<TrainSeat> seatList = trainSeatService.searchList(param, pageQuery);
        if (CollectionUtils.isEmpty(seatList)) {
            return JsonData.success(PageResult.<TrainSeatDto>builder().total(total).build());
        }
        List<TrainStation> trainStationList = trainStationService.getAll();
        Map<Integer, String> stationMap = trainStationList.stream().collect(Collectors.toMap(TrainStation::getId, TrainStation::getName));
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        ZoneId zoneId = ZoneId.systemDefault();
        List<TrainSeatDto> dtoList = seatList.stream().map(trainSeat -> {
            TrainSeatDto dto = new TrainSeatDto();
            dto.setId(trainSeat.getId());
            dto.setFromStationId(trainSeat.getFromStationId());
            dto.setFromStation(stationMap.get(trainSeat.getFromStationId()));
            dto.setToStationId(trainSeat.getToStationId());
            dto.setToStation(stationMap.get(trainSeat.getToStationId()));
            dto.setTrainNumberId(trainSeat.getTrainNumberId());
            dto.setTrainNumber(param.getTrainNumber());
            dto.setShowStart(LocalDateTime.ofInstant(trainSeat.getTrainStart().toInstant(), zoneId).format(formatter));
            dto.setShowEnd(LocalDateTime.ofInstant(trainSeat.getTrainEnd().toInstant(), zoneId).format(formatter));
            dto.setSeatLevel(trainSeat.getSeatLevel());
            dto.setStatus(trainSeat.getStatus());
            dto.setCarriageNumber(trainSeat.getCarriageNumber());
            dto.setRowNumber(trainSeat.getRowNumber());
            dto.setSeatNumber(trainSeat.getSeatNumber());
            dto.setMoney(trainSeat.getMoney());
            return dto;
        }).collect(Collectors.toList());
        return JsonData.success(PageResult.<TrainSeatDto>builder().data(dtoList).total(total).build());
    }

    @PostMapping("/search2")
    public JsonData search2(TrainSeatSearchParam param, PageQuery pageQuery) {
        int total = trainSeatService.countList(param);
        if (total == 0) {
            return JsonData.success(PageResult.<TrainSeat>builder().total(0).build());
        }
        List<TrainSeat> seatList = trainSeatService.searchList(param, pageQuery);
        if (CollectionUtils.isEmpty(seatList)) {
            return JsonData.success(PageResult.<TrainSeatDto>builder().total(total).build());
        }
        List<TrainStation> trainStationList = trainStationService.getAll();
        Map<Integer, String> stationMap = trainStationList.stream().collect(Collectors.toMap(TrainStation::getId, TrainStation::getName));
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        ZoneId zoneId = ZoneId.systemDefault();
        List<TrainSeatDto> dtoList = seatList.stream().map(trainSeat -> {
            TrainSeatDto dto = new TrainSeatDto();
            dto.setId(trainSeat.getId());
            dto.setFromStationId(trainSeat.getFromStationId());
            dto.setFromStation(stationMap.get(trainSeat.getFromStationId()));
            dto.setToStationId(trainSeat.getToStationId());
            dto.setToStation(stationMap.get(trainSeat.getToStationId()));
            dto.setTrainNumberId(trainSeat.getTrainNumberId());
            dto.setTrainNumber(param.getTrainNumber());
            dto.setShowStart(LocalDateTime.ofInstant(trainSeat.getTrainStart().toInstant(), zoneId).format(formatter));
            dto.setShowEnd(LocalDateTime.ofInstant(trainSeat.getTrainEnd().toInstant(), zoneId).format(formatter));
            dto.setSeatLevel(trainSeat.getSeatLevel());
            dto.setStatus(trainSeat.getStatus());
            dto.setCarriageNumber(trainSeat.getCarriageNumber());
            dto.setRowNumber(trainSeat.getRowNumber());
            dto.setSeatNumber(trainSeat.getSeatNumber());
            dto.setMoney(trainSeat.getMoney());
            return dto;
        }).collect(Collectors.toList());
        return JsonData.success(PageResult.<TrainSeatDto>builder().data(dtoList).total(total).build());
    }

    @PostMapping("/generate")
    public JsonData generate(GenerateTicketParam param) {
        trainSeatService.generate(param);
        return JsonData.success();
    }

    @PostMapping("/publish")
    public JsonData publish(PublishTicketParam param){
        trainSeatService.publish(param);
        return JsonData.success();
    }

}
