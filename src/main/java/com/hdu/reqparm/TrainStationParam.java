package com.hdu.reqparm;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@ToString
@Getter
@Setter
public class TrainStationParam {

    private Integer id;

    @NotBlank(message = "站点名称不能为空")
    @Length(min = 2, max = 20, message = "站点名称长度在2~20之间")
    private String name;

    @NotNull(message = "城市不可为空")
    @Min(value = 1, message = "城市不合法")
    private Integer cityId;
}
