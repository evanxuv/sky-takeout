package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.annotation.AutoFill;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.enumeration.OperationType;
import com.sky.vo.DishVO;
import io.swagger.annotations.ApiOperation;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface DishMapper {
    /**
     * 查询套餐
     * @param dish
     * @return
     */
    @Select("select * from dish where category_id = #{categoryId} and status = #{status}")
    List<Dish> list(Dish dish);

    /**
     * 根据分类id查询菜品数量
     * @param categoryId
     * @return
     */
    @Select("select count(id) from dish where category_id = #{categoryId}")
    Integer countByCategoryId(Long categoryId);

    /**
     * 新增菜品
     * @param dish
     */
    // 加上 @Options(useGeneratedKeys = true, keyProperty = "id") ，
    // 可以让 MyBatis 自动获取并回填数据库生成的主键，方便后续业务处理。
    @Options(useGeneratedKeys = true,keyProperty = "id") // 获取主键值，并赋值给dish的id属性
    @AutoFill(OperationType.INSERT)
    @Insert("insert into dish values(null,#{name},#{categoryId},#{price},#{image}," +
            "#{description},#{status},#{createTime},#{updateTime},#{createUser},#{updateUser})")
    // 如果sql写在xml上的话，useGeneratedKeys = true,keyProperty = "id"需要写在xml中，
    // 比如：<insert id="insert" useGeneratedKeys="true" keyProperty="id">
    void insert(Dish dish);

    /**
     * 菜品分页管理
     * @param dishPageQueryDTO
     * @return
     */
    Page<DishVO> pageQuery(DishPageQueryDTO dishPageQueryDTO);

    /**
     * 根据id查询菜品
     * @param id
     * @return
     */
    Dish selectById(Long id);

    /**
     * 批量删除菜品
     * @param ids
     */
    void deleteBatch(List<Long> ids);

    /**
     * 根据套餐id查询菜品
     * @param setmealId
     * @return
     */
    @Select("select a.* from dish a left join setmeal_dish b on a.id = b.dish_id where b.setmeal_id = #{setmealId}")
    List<Dish> getBySetmealId(Long setmealId);
    /**
     * 修改菜品
     * @param dish
     */
    @AutoFill(OperationType.UPDATE)
    void update(Dish dish);
}
