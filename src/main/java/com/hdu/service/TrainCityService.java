package com.hdu.service;

import com.hdu.dao.TrainCityMapper;
import com.hdu.model.TrainCity;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class TrainCityService {
    @Resource
    private TrainCityMapper trainCityMapper;
    public List<TrainCity> getAll(){
        return trainCityMapper.getAll();
    }
}

























