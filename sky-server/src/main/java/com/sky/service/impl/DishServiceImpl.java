package com.sky.service.impl;

import com.sky.dto.DishDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author evan
 * @version 1.0
 */
@Service
@Slf4j
public class DishServiceImpl implements DishService {

    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private DishFlavorMapper dishFlavorMapper;
    /**
     * 添加菜品
     * @param dishDTO
     */
    @Transactional// 声明开启事务(涉及到多张表的增删改操作，需要开启事务)
    public void addDish(DishDTO dishDTO) {
        // 1.构造菜品基本信息，将其存入dish表中
        Dish dish = new Dish();
        // 拷贝dishDTO中的属性值到dish表中,这里前端传入的参数之后，dish表还剩下的属性在公共字段加入了
        BeanUtils.copyProperties(dishDTO, dish);
        // 调用mapper，保存方法
        dishMapper.insert(dish);
        log.info("dish表id：{}", dish.getId());

        // dish_flavor表中的dish_id没有dish_flavor声明，需要关联dish表的id
        Long id = dish.getId();
        // 2. 构造菜品口味列表数据，将其存入dish_flavor表中
        List<DishFlavor> dishFlavorList = dishDTO.getFlavors();
        //2.1 关联菜品id
        dishFlavorList.forEach(dishFlavor -> {
            dishFlavor.setDishId(id);
        });
        //2.2 调用mapper,批量插入口味列表数据
        dishFlavorMapper.insertBatch(dishFlavorList);

    }
}
