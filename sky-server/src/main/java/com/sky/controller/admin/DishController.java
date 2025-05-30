package com.sky.controller.admin;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author evan
 * @version 1.0
 */
@Slf4j
@Api(tags = "菜品相关接口")
@RequestMapping("/admin/dish")
@RestController
public class DishController {
    @Autowired
    private DishService dishService;

    /**
     *修改菜品状态
     * @param status
     * @param id
     * @return
     */
    @PostMapping("/status/{status}")
    @ApiOperation("修改菜品状态")
    public Result startOtStop(@PathVariable("status") Integer status, Long id) {
        log.info("修改菜品状态 id={}",id);
        dishService.updateStatus(status,id);
        return Result.success();
    }
    /**
     * 添加菜品
     * @return
     */
    @PostMapping
    @ApiOperation("添加菜品")
    public Result<String> addDish(@RequestBody DishDTO dishDTO) {
        log.info("添加菜品:{}", dishDTO);
        dishService.addDish(dishDTO);
        return Result.success();
    }

    /**
     * 菜品分页管理
     * @param dishPageQueryDTO
     * @return
     */
    @ApiOperation("菜品分页管理")
    @GetMapping("/page")
    public Result<PageResult> pageDish(DishPageQueryDTO dishPageQueryDTO) {
        log.info("菜品分页管理{}", dishPageQueryDTO);
        PageResult pageResult = dishService.pageDish(dishPageQueryDTO);
        return Result.success(pageResult);
    }

    /**
     * 删除菜品
     * @param ids
     * @return
     */
    @DeleteMapping
    @ApiOperation("删除菜品")
    public Result deleteDish(@RequestParam List<Long> ids){// 菜品的主键 id 通常是 Long 类型,List 是为了支持批量操作。
        log.info("删除菜品:{}",ids);
        dishService.deleteBatch(ids);
        return Result.success();
    }

    /***
     *回显菜品
     * @param id
     * @return
     */
    @ApiOperation("回显菜品和口味")
    @GetMapping("/{id}")
    public Result<DishVO> selectDishById(@PathVariable Long id){
        log.info("回显菜品：{}",id);
        DishVO dishVO = dishService.getById(id);
        return Result.success(dishVO);
    }

    /**
     * 修改菜品
     * @param dishDTO
     * @return
     */
    @ApiOperation("修改菜品")
    @PutMapping
    public Result updateDish(@RequestBody DishDTO dishDTO){
        log.info("修改菜品:{}",dishDTO);
        dishService.updateDish(dishDTO);
        return Result.success();
    }
}
