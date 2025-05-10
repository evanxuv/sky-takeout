package com.sky.service.impl;

import com.sky.context.BaseContext;
import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.entity.ShoppingCart;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.mapper.ShoppingCartMapper;
import com.sky.service.ShoppingCartService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author evan
 * @version 1.0
 */
@Service
public class ShoppingCartServiceImpl implements ShoppingCartService {
    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private DishFlavorMapper dishFlavorMapper;
    @Autowired
    private ShoppingCartMapper shoppingCartMapper;
    @Autowired
    private SetmealMapper setmealMapper;
    /**
     * 添加购物车
     * @param shoppingCartDTO
     */
    @Override
    public void addCart(ShoppingCartDTO shoppingCartDTO) {
        //因为 shoppingCart表中很多字段在shoppingCartDTO对象中没有，需要补充字段
        // 创建shoppingCart对象
        ShoppingCart shoppingCart = new ShoppingCart();
        // 拷贝属性值
        BeanUtils.copyProperties(shoppingCartDTO, shoppingCart);
        //1. 判断该商品是否已经存在购物车--条件： dishId+dishFlavor+userId
        // 只查当前用户自己的购物车
        shoppingCart.setUserId(BaseContext.getCurrentId());
        ShoppingCart cart =  shoppingCartMapper.selectBy(shoppingCart);
        if (cart == null) {// 代表购物车没有该商品数据
            // 2. 补充缺失字段
            // 判断是新增套餐还是新增菜品
            if (shoppingCartDTO.getDishId()!=null) {// 代表是新增菜品
                // 根据菜品id查询菜品表，获取菜品信息
                Dish dish = dishMapper.selectById(shoppingCartDTO.getDishId());
                shoppingCart.setName(dish.getName());
                shoppingCart.setAmount(dish.getPrice());
                shoppingCart.setImage(dish.getImage());

            }else {// 代表是新增套餐
                Setmeal setmeal = setmealMapper.getById(shoppingCartDTO.getSetmealId());
                shoppingCart.setName(setmeal.getName());
                shoppingCart.setAmount(setmeal.getPrice());
                shoppingCart.setImage(setmeal.getImage());

            }
            shoppingCart.setCreateTime(LocalDateTime.now());
            shoppingCart.setNumber(1); // 数量---到底是1还是加1，得判断该商品是否存在购物车

            //3.将商品数据存入到shopping_cart表中
            shoppingCartMapper.insert(shoppingCart);
        }else{// 代表购物车有该商品数据
            //4.将原来的购物车商品数量+1，调用mapper更新方法
            cart.setNumber(cart.getNumber()+1);
            shoppingCartMapper.update(cart);
        }

        // 最终目的：将用户添加的商品，存入到购物车表shoppingCart表中

    }

    /**
     * 查看购物车
     * @return
     */
    @Override
    public List<ShoppingCart> list() {
        // 注意 只能查看自己名下的购物车
        return shoppingCartMapper.list(BaseContext.getCurrentId());
    }

    /**
     * 清空购物车
     */
    @Override
    public void clean() {
        // 注意 只能清空自己名下的购物车
        shoppingCartMapper.delete(BaseContext.getCurrentId());
    }
}
