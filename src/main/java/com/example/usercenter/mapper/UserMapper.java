package com.example.usercenter.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.usercenter.bean.User;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

/**
* @author ASUS
* @description 针对表【user】的数据库操作Mapper
* @createDate 2023-05-05 20:54:47
* @Entity generator.domain.User
*/
@Repository
public interface UserMapper extends BaseMapper<User> {

}




