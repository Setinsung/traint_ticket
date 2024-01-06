package com.hdu.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class TrainNumberLeftDto {

    private int id;

    private String number;

    private long leftCount;
}
