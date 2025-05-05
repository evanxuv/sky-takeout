package com.sky.controller.admin;

import com.sky.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

/**
 * @author evan
 * @version 1.0
 */
@RestController("adminShopController") //给这个Controller起一个bean的名字
@RequestMapping("/admin/shop")
@Api(tags = "店铺相关接口")
@Slf4j
public class ShopController {
    // 设置店铺状态为静态常量，
    public static final String KEY = "SHOP_STATUS";
    // 因为店铺状态只有一个值，顾不创建数据库表，使用redis
    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 设置店铺的营业状态
     * @param status
     * @return
     */
    @ApiOperation("设置店铺的营业状态")
    @PutMapping("/{status}")
    public Result setStatus(@PathVariable Integer status){
        log.info("设置店铺的营业状态:{}",status == 1 ? "营业中":"打烊了");
        redisTemplate.opsForValue().set(KEY,status);
        return Result.success();
    }

    /**
     * 获取店铺的营业状态
     * @return
     */
    @ApiOperation("管理端获取店铺营业状态")
    @GetMapping("/status")
    public Result<Integer> getStatus(){
        Integer status = (Integer) redisTemplate.opsForValue().get(KEY);
        log.info("获取到店铺的营业状态:{}",status == 1 ? "营业中":"打烊了");
        return Result.success(status);
    }
}
