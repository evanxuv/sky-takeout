package com.sky.mapper;

import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @author evan
 * @version 1.0
 */
@Mapper
public interface SetmealDishMapper {

    /**
     * 统计
     * @return
     */
    Integer countByDishId(List<Long> dishIds);

}
