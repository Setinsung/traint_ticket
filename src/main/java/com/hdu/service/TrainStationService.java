package com.hdu.service;

import com.hdu.dao.TrainCityMapper;
import com.hdu.dao.TrainStationMapper;
import com.hdu.exception.BusinessException;
import com.hdu.model.TrainCity;
import com.hdu.model.TrainStation;
import com.hdu.reqparm.TrainStationParam;
import com.hdu.utils.BeanValidator;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class TrainStationService {
    @Resource
    private TrainStationMapper trainStationMapper;
    @Resource
    private TrainCityMapper trainCityMapper;

    public List<TrainStation> getAll() {
        return trainStationMapper.getAll();
    }

    public void save(TrainStationParam param) {
        BeanValidator.check(param);
        TrainCity trainCity = trainCityMapper.selectByPrimaryKey(param.getCityId());
        if (trainCity == null) {
            throw new BusinessException("城市站点不存在");
        }
        if (checkExist(param.getName(), param.getId(), param.getCityId())) {
            throw new BusinessException("该城市下存在相同站点");
        }
        TrainStation trainStation = TrainStation.builder().name(param.getName()).cityId(param.getCityId()).build();
        trainStationMapper.insertSelective(trainStation);
    }

    public void update(TrainStationParam param) {
        BeanValidator.check(param);
        TrainCity trainCity = trainCityMapper.selectByPrimaryKey(param.getCityId());
        if (trainCity == null) {
            throw new BusinessException("城市站点不存在");
        }
        if (checkExist(param.getName(), param.getId(), param.getCityId())) {
            throw new BusinessException("该城市下存在相同站点");
        }
        TrainStation before = trainStationMapper.selectByPrimaryKey(param.getId());
        if (before == null) {
            throw new BusinessException("更新站点不存在");
        }
        TrainStation trainStation = TrainStation.builder().name(param.getName()).id(param.getId()).cityId(param.getCityId()).build();
        trainStationMapper.updateByPrimaryKeySelective(trainStation);
    }

    private boolean checkExist(String name, Integer stationId, Integer cityId) {
        return trainStationMapper.countByIdAndNameAndCityId(name, stationId, cityId) > 0;
    }

    public Integer getCityIdByStationId(int stationId){
        TrainStation trainStation = trainStationMapper.selectByPrimaryKey(stationId);
        if (trainStation == null){
            throw new BusinessException("站点不存在");
        }
        return trainStation.getCityId();
    }
}
