package com.zxc.publics.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
@ApiModel(value = "返回结果")
public class Result<T> {

    @ApiModelProperty("返回码")
    private int code;

    @ApiModelProperty("返回描述")
    private String msg;

    @ApiModelProperty("返回对象")
    private T data;

}