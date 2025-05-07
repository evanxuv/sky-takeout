package com.sky.mapper;

import com.sky.entity.User;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;

/**
 * @author evan
 * @version 1.0
 */
@Mapper
public interface UserMapper {
    @Select("select * from user where openid = #{openid}")
    User selectByOpenid(String openid);
    // 新增的用户没有id，要给新增用户添加id
    @Options(useGeneratedKeys = true,keyProperty = "id")
    @Insert("insert into user(openid,name,create_time) values(#{openid},#{name},#{createTime})")
    void insert(User user);
}
