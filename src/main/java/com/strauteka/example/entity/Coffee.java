package com.strauteka.example.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
@ApiModel
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Coffee {
    @ApiModelProperty(example = "1")
    Long id;
    @ApiModelProperty(example = "Arabic coffee")
    String name;
    @ApiModelProperty(example = "21.12.2021 12:21:21")
    @JsonFormat(pattern = "dd.MM.yyyy hh:mm:ss")
    Date creationTime;
    @ApiModelProperty(example = "Bitter")
    String flavor;
}
