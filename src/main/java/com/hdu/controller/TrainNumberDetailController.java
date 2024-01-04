package com.hdu.controller;

import com.hdu.common.JsonData;
import com.hdu.dto.TrainNumberDetailDto;
import com.hdu.model.TrainNumber;
import com.hdu.model.TrainNumberDetail;
import com.hdu.model.TrainStation;
import com.hdu.reqparm.TrainNumberDetailParam;
import com.hdu.service.TrainNumberDetailService;
import com.hdu.service.TrainNumberService;
import com.hdu.service.TrainStationService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/admin/train/numberDetail")
public class TrainNumberDetailController {
    @Resource
    private TrainNumberDetailService trainNumberDetailService;
    @Resource
    private TrainStationService trainStationService;
    @Resource
    private TrainNumberService trainNumberService;

    @GetMapping("list")
    public JsonData list(){
        List<TrainNumberDetail> trainNumberDetailList = trainNumberDetailService.getAll();
        List<TrainStation> trainStationList = trainStationService.getAll();
        Map<Integer,String> stationMap = trainStationList.stream().collect(Collectors.toMap(TrainStation::getId,TrainStation::getName));
        List<TrainNumber> trainNumberList = trainNumberService.getAll();
        Map<Integer,String> numberMap = trainNumberList.stream().collect(Collectors.toMap(TrainNumber::getId,TrainNumber::getName));
        List<TrainNumberDetailDto> dtoList = trainNumberDetailList.stream().map(detail -> {
            TrainNumberDetailDto dto = new TrainNumberDetailDto();
            dto.setId(detail.getId());
            dto.setFromStation(stationMap.get(detail.getFromStationId()));
            dto.setToStation(stationMap.get(detail.getToStationId()));
            dto.setFromCityId(detail.getFromCityId());
            dto.setToCityId(detail.getToCityId());
            dto.setFromStationId(detail.getFromStationId());
            dto.setToStationId(detail.getToStationId());
            dto.setTrainNumberId(detail.getTrainNumberId());
            dto.setTrainNumber(numberMap.get(detail.getTrainNumberId()));
            dto.setStationIndex(detail.getStationIndex());
            dto.setRelativeMinute(detail.getRelativeMinute());
            dto.setWaitMinute(detail.getWaitMinute());
            dto.setMoney(detail.getMoney());
            return dto;
        }).collect(Collectors.toList());
        return JsonData.success(dtoList);
    }

    @PostMapping
    public JsonData save(TrainNumberDetailParam param){
        trainNumberDetailService.save(param);
        return JsonData.success();
    }

    @DeleteMapping
    public JsonData delete(@RequestParam("id") Integer id){
        trainNumberDetailService.delete(id);
        return JsonData.success();
    }
}
