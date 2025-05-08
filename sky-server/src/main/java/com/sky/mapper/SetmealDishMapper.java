package com.sky.mapper;

import com.sky.entity.SetmealDish;
import org.apache.ibatis.annotations.Delete;
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

    /**
     * 新增套餐菜品
     * @param setmealDisheList
     */
    void insertBatch(List<SetmealDish> setmealDisheList);
    /**
     * 根据套餐id删除套餐和菜品的关联关系
     * @param setmealId
     */
    @Delete("delete from setmeal_dish where setmeal_id = #{setmealId}")
    void deleteBySetmealId(Long setmealId);
}
