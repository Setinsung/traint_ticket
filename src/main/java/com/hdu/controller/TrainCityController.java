package com.hdu.controller;

import com.hdu.common.JsonData;
import com.hdu.service.TrainCityService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

}
























