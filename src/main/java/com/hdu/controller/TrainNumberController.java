package com.hdu.controller;

import com.hdu.common.JsonData;
import com.hdu.dto.TrainNumberDto;
import com.hdu.model.TrainNumber;
import com.hdu.model.TrainStation;
import com.hdu.reqparm.TrainNumberParam;
import com.hdu.service.TrainNumberService;
import com.hdu.service.TrainStationService;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/admin/train/number")
public class TrainNumberController {
    @Resource
    private TrainNumberService trainNumberService;
    @Resource
    private TrainStationService trainStationService;

    @GetMapping("/list")
    public JsonData list() {
        List<TrainNumber> trainNumberList = trainNumberService.getAll();
        List<TrainStation> trainStationList = trainStationService.getAll();
        Map<Integer, String> stationMap = trainStationList.stream().collect(Collectors.toMap(TrainStation::getId, TrainStation::getName));
        List<TrainNumberDto> dtoList = trainNumberList.stream().map(trainNumber -> {
            TrainNumberDto trainNumberDto = new TrainNumberDto();
            trainNumberDto.setId(trainNumber.getId());
            trainNumberDto.setName(trainNumber.getName());
            trainNumberDto.setFromStation(stationMap.get(trainNumber.getFromStationId()));
            trainNumberDto.setToStation(stationMap.get(trainNumber.getToStationId()));
            trainNumberDto.setFromStationId(trainNumber.getFromStationId());
            trainNumberDto.setToStationId(trainNumber.getToStationId());
            trainNumberDto.setFromCityId(trainNumber.getFromCityId());
            trainNumberDto.setToCityId(trainNumber.getToCityId());
            trainNumberDto.setTrainType(trainNumber.getTrainType());
            trainNumberDto.setType(trainNumber.getType());
            trainNumberDto.setSeatNum(trainNumber.getSeatNum());
            return trainNumberDto;
        }).collect(Collectors.toList());
        return JsonData.success(dtoList);
    }


    @PostMapping
    public JsonData save(TrainNumberParam param){
        trainNumberService.save(param);
        return JsonData.success();
    }

    @PutMapping
    public JsonData update(TrainNumberParam param){
        trainNumberService.update(param);
        return JsonData.success();
    }

}





















