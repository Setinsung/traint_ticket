package com.hdu.common;

import lombok.Getter;

@Getter
public enum TrainSeatLevel {
    TOP_GRADE(0,"特等座"),
    GRADE_1(1,"一等座，一排4座"),
    GRADE_2(2,"二等座，一排5座");

    int level;
    String desc;

    TrainSeatLevel(int level,String desc){
        this.desc=desc;
        this.level=level;
    }
}
