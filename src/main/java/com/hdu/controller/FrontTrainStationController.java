package com.hdu.controller;

import com.hdu.common.JsonData;
import com.hdu.dto.TrainNumberLeftDto;
import com.hdu.reqparm.FrontSearchParam;
import com.hdu.service.TrainSeatService;
import com.hdu.service.TrainStationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/api/station")
@Slf4j
public class FrontTrainStationController {

    @Resource
    private TrainStationService trainStationService;

    @Resource
    private TrainSeatService trainSeatService;

    @GetMapping("/list")
    public JsonData apiList() {
        return JsonData.success(trainStationService.getAll());
    }

    @PostMapping("/search")
    public JsonData search(FrontSearchParam param) {
        try {
            List<TrainNumberLeftDto> dtoList = trainSeatService.FrontSearch(param);
            return JsonData.success(dtoList);
        } catch (Exception e) {
            log.error("searchLeftCount exception, param:{}", param, e);
            return JsonData.fail("查询异常，请稍后重试");
        }
    }
}






















