package com.hdu.reqparm;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.ibatis.annotations.SelectProvider;
import org.hibernate.validator.constraints.NotBlank;

@Getter
@Setter
@ToString
public class PublishTicketParam {

    @NotBlank(message = "车次不可以为空")
    private String trainNumber;

    @NotBlank(message = "座位不可以为空")
    private String trainSeatIds;
}
