package com.hdu.dto;

import com.hdu.model.TrainStation;
import lombok.*;

@ToString
public class TrainStationDto extends TrainStation {

    @Getter
    @Setter
    private String cityName;
}
