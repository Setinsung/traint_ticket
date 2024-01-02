package com.hdu.orderDao;

import com.hdu.model.TrainOrder;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface TrainOrderMapper {
    int deleteByPrimaryKey(Long id);

    int insert(TrainOrder record);

    int insertSelective(TrainOrder record);

    TrainOrder selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(TrainOrder record);

    int updateByPrimaryKey(TrainOrder record);

    TrainOrder findByOrderId(@Param("orderId") String orderId);

    List<TrainOrder> getByUserId(@Param("userId") long userId);
}