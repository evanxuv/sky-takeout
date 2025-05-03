package com.sky.service;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.result.PageResult;

import java.util.List;

/**
 * @author evan
 * @version 1.0
 */
public interface DishService {
    /**
     * 添加菜品
     */
    void addDish(DishDTO dishDTO);

    /**
     * 菜品分页管理
     * @return
     */
    PageResult pageDish(DishPageQueryDTO dishPageQueryDTO);

    /**
     * 删除菜品
     * @param ids
     */
    void deleteBatch(List<Long> ids);
}
