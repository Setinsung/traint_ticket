package com.hdu.reqparm;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotBlank;
import org.springframework.stereotype.Service;

@Getter
@Setter
@ToString
public class TrainSeatSearchParam {
    @NotBlank(message = "车次不可以为空")
    @Length(min = 2,max = 20,message = "车次长度要在2~20字之间")
    private String trainNumber;

    @NotBlank(message = "出发日期不可以为空")
    @Length(min = 8,max  =8,message = "出发日期格式必须为yyyyMMdd")
    private String ticket;

    private Integer status;

    private Integer carriageNum;

    private Integer rowNum;

    private Integer seatNum;
}
