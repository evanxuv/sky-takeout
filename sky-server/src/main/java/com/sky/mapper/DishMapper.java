package com.sky.mapper;

import com.sky.annotation.AutoFill;
import com.sky.entity.Dish;
import com.sky.enumeration.OperationType;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface DishMapper {

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
}
