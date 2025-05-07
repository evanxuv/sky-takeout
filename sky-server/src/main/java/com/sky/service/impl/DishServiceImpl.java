package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
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
    @Autowired
    private SetmealDishMapper setmealDishMapper;
    @Autowired
    private SetmealMapper setmealMapper;
    /**
     * 条件查询菜品和口味
     * @param dish
     * @return
     */
    public List<DishVO> listWithFlavor(Dish dish) {
        List<Dish> dishList = dishMapper.list(dish);

        List<DishVO> dishVOList = new ArrayList<>();

        for (Dish d : dishList) {
            DishVO dishVO = new DishVO();
            BeanUtils.copyProperties(d,dishVO);

            //根据菜品id查询对应的口味
            List<DishFlavor> flavors = dishFlavorMapper.selectByDishId(d.getId());

            dishVO.setFlavors(flavors);
            dishVOList.add(dishVO);
        }

        return dishVOList;
    }

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

    /**
     * 菜品分页管理
     * @param dishPageQueryDTO
     * @return
     */
    @Override
    public PageResult pageDish(DishPageQueryDTO dishPageQueryDTO) {
        // 1. 启动分页，指定当前页码和每页大小
        PageHelper.startPage(dishPageQueryDTO.getPage(),dishPageQueryDTO.getPageSize());
        // 2. 调用 dishMapper 的 pageQuery 方法，执行分页查询，返回 Page<DishVO> 对象
        Page<DishVO> page = dishMapper.pageQuery(dishPageQueryDTO);
        // 3. 封装分页结果，返回总条数和当前页数据列表
        return new PageResult(page.getTotal(), page.getResult());
    }

    /**
     * 批量删除菜品
     * @param ids
     */
    @Transactional
    public void deleteBatch(List<Long> ids) {
        //1. 判断当前要删除的菜品状态是否为起售中
        ids.forEach(id ->{
            Dish dish = dishMapper.selectById(id);
            if (dish.getStatus() == StatusConstant.ENABLE) {
                throw new DeletionNotAllowedException(MessageConstant.DISH_ON_SALE);
            }
        });
        //2. 判断当前要删除的菜品是否被套餐关联了
        Integer count = setmealDishMapper.countByDishId(ids);
        if (count > 0) {
            throw new DeletionNotAllowedException(MessageConstant.DISH_BE_RELATED_BY_SETMEAL);
        }
        //3. 删除dish菜品表中的数据
        dishMapper.deleteBatch(ids);
        //4. 删除dish_flavor口味表中的数据
        dishFlavorMapper.deleteBarch(ids);

    }

    /**
     * 回显菜品
     * @param id
     * @return
     */
    @Override
    public DishVO getById(Long id) {
        DishVO dishVO = new DishVO();
        //1. 查询dish表的数据
        Dish dish = dishMapper.selectById(id);
        BeanUtils.copyProperties(dish, dishVO);
        //2. 查询dish_flavor 表的数据
        List<DishFlavor> flavors = dishFlavorMapper.selectByDishId(id);
        dishVO.setFlavors(flavors);
        //构造DishVO对象返回
        return dishVO;
    }

    /**
     * 修改菜品
     * @param dishDTO
     */
    @Transactional
    public void updateDish(DishDTO dishDTO) {
        //1. 修改dish表数据
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);
        dishMapper.update(dish);
        //2. 修改dish_flavor表数据
        // 口味可能增加、删除、修改，如果一个一个操作很麻烦，可以先删除在添加
        dishFlavorMapper.deleteByDishId(dishDTO.getId());
        List<DishFlavor> flavors = dishDTO.getFlavors();
        if (flavors != null && flavors.size() > 0) {
            // 需要关联菜品id
            flavors.forEach(flavor -> {
                flavor.setDishId(dish.getId());
            });
            dishFlavorMapper.insertBatch(flavors);
        }



    }
}
