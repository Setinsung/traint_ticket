package com.hdu.controller;

import com.hdu.common.JsonData;
import com.hdu.model.TrainSeat;
import com.hdu.model.TrainStation;
import com.hdu.reqparm.GenerateTicketParam;
import com.hdu.service.TrainSeatService;
import com.hdu.service.TrainStationService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

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


    @RequestMapping("/generate.json")
    public JsonData generate(GenerateTicketParam param) {
        trainSeatService.generate(param);
        return JsonData.success();
    }

}
