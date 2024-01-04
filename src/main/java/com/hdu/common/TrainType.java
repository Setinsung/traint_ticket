package com.hdu.common;

import lombok.Getter;
import lombok.Setter;

@Getter
public enum TrainType {
    CRH2(1220),
    CRH5(1244);
    int count;

    TrainType(int count){
        this.count = count;
    }
}
