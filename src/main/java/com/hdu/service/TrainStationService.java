package com.hdu.service;

import com.hdu.dao.TrainStationMapper;
import com.hdu.model.TrainStation;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class TrainStationService {
    @Resource
    private TrainStationMapper trainStationMapper;

    public List<TrainStation> getAll(){
        return trainStationMapper.getAll();
    }
}
