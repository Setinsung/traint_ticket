package com.hdu.service;

import com.hdu.dao.TrainNumberMapper;
import com.hdu.model.TrainNumber;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class TrainNumberService {
    @Resource
    private TrainNumberMapper trainNumberMapper;

    public List<TrainNumber> getAll(){
        return trainNumberMapper.getAll();
    }
}
