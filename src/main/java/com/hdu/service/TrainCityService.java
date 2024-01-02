package com.hdu.service;

import com.hdu.dao.TrainCityMapper;
import com.hdu.exception.BusinessException;
import com.hdu.model.TrainCity;
import com.hdu.reqparm.TrainCityParam;
import com.hdu.utils.BeanValidator;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class TrainCityService {
    @Resource
    private TrainCityMapper trainCityMapper;

    public List<TrainCity> getAll() {
        return trainCityMapper.getAll();
    }

    public void save(TrainCityParam param) {
        BeanValidator.check(param);
        if (checkExist(param.getName(), param.getId())) {
            throw new BusinessException("存在相同名称的城市");
        }
        TrainCity trainCity = TrainCity.builder().name(param.getName()).build();
        trainCityMapper.insertSelective(trainCity);
    }

    public void update(TrainCityParam param) {
        BeanValidator.check(param);
        if (checkExist(param.getName(), param.getId())) {
            throw new BusinessException("存在相同名称的城市");
        }
        TrainCity before = trainCityMapper.selectByPrimaryKey(param.getId());
        if (before == null) {
            throw new BusinessException("城市不存在");
        }
        TrainCity trainCity = TrainCity.builder().name(param.getName()).id(param.getId()).build();
        trainCityMapper.updateByPrimaryKeySelective(trainCity);
    }

    private boolean checkExist(String name, Integer cityId) {
        return trainCityMapper.countByNameAndId(name, cityId) > 0;
    }
}

























