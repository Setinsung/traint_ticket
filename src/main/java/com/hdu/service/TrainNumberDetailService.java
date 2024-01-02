package com.hdu.service;

import com.hdu.dao.TrainNumberDetailMapper;
import com.hdu.model.TrainNumberDetail;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class TrainNumberDetailService {
    @Resource
    private TrainNumberDetailMapper trainNumberDetailMapper;

    public List<TrainNumberDetail> getAll(){
        return trainNumberDetailMapper.getAll();
    }
}
