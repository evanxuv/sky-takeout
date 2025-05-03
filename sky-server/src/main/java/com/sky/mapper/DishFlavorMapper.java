package com.sky.mapper;

import com.sky.entity.DishFlavor;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @author evan
 * @version 1.0
 */
// 在SkyApplication中声明注解@MapperScan("com.sky.mapper"),
// 这样mapper层的注解@Mapper就可以省略了
@Mapper
public interface DishFlavorMapper {

    /**
     * 批量插入口味列表
     * @param dishFlavorList
     */
    public void insertBatch(List<DishFlavor> dishFlavorList);

}
