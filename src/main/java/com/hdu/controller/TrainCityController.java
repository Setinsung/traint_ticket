package com.hdu.controller;

import com.hdu.common.JsonData;
import com.hdu.reqparm.TrainCityParam;
import com.hdu.service.TrainCityService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@RequestMapping("/admin/train/city")
public class TrainCityController {
    @Resource
    private TrainCityService trainCityService;

    @GetMapping("/list")
    public JsonData list() {
        return JsonData.success(trainCityService.getAll());
    }

    @PostMapping
    public JsonData save(TrainCityParam param) {
        trainCityService.save(param);
        return JsonData.success();
    }

    @PutMapping
    public JsonData update(TrainCityParam param){
        trainCityService.update(param);
        return JsonData.success();
    }

}
























