package com.sky.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class EmployeePageQueryDTO implements Serializable {

    //员工姓名
    private String name;

    //页码
    // 默认类型会有默认值，int类型默认值是0
    // 在开发中，要使用包装类，包装类没有默认值，定义的时候需要默认值
    //private int page;
    private Integer page = 1;

    //每页显示记录数
    //private int pageSize;
    private int pageSize =10;

}
