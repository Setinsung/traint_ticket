package com.hdu.controller;

import com.hdu.common.JsonData;
import com.hdu.dto.TrainStationDto;
import com.hdu.model.TrainCity;
import com.hdu.model.TrainStation;
import com.hdu.reqparm.TrainStationParam;
import com.hdu.service.TrainCityService;
import com.hdu.service.TrainStationService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/admin/train/station")
public class TrainStationController {

    @Resource
    private TrainStationService trainStationService;

    @Resource
    private TrainCityService trainCityService;

    @GetMapping("/list")
    public JsonData list() {
        List<TrainStation> trainStationList = trainStationService.getAll();
        List<TrainCity> cityList = trainCityService.getAll();
        Map<Integer, String> cityMap = cityList.stream().collect(Collectors.toMap(TrainCity::getId, TrainCity::getName));
        List<TrainStationDto> dtoList = trainStationList.stream().map(trainStation -> {
            TrainStationDto dto = new TrainStationDto();
            dto.setCityName(cityMap.get(trainStation.getCityId()));
            dto.setCityId(trainStation.getCityId());
            dto.setId(trainStation.getId());
            dto.setName(trainStation.getName());
            return dto;
        }).collect(Collectors.toList());
        return JsonData.success(dtoList);
    }

    @PostMapping
    public JsonData save(TrainStationParam param) {
        trainStationService.save(param);
        return JsonData.success();
    }

    @PutMapping
    public JsonData update(TrainStationParam param) {
        trainStationService.update(param);
        return JsonData.success();
    }
}































