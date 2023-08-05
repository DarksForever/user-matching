package com.example.usercenter.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.usercenter.bean.User;
import com.example.usercenter.bean.Vo.UserVO;
import org.springframework.http.HttpRequest;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
* @author ASUS
* @description 针对表【user】的数据库操作Service
* @createDate 2023-05-05 20:54:47
*/
public interface UserService extends IService<User> {
    /*
    * @description: 注册
    * @author: liaocy
    * @date: 2023/5/5 21:30
    * @param: [userAccount 用户账号, password 用户密码, checkedPassword 确认密码]
    * @return: long 新用户id
    **/
    long userRegister(String userAccount,String password,String checkedPassword);

    /*
    * @description: 用户登录
    * @author: liaocy
    * @date: 2023/5/7 16:33
    * @param: [userAccount 用户账号, password 用户密码, httpRequest 请求域]
    * @return: com.example.usercenter.bean.User 脱敏后的用户信息
    **/
    User userLogin(String userAccount, String password, HttpServletRequest httpRequest);

    /*
    * @description: 脱敏
    * @author: liaocy
    * @date: 2023/5/7 20:07
    * @param: [user]
    * @return: com.example.usercenter.bean.User
    **/
    User doSafe(User user);

    /*
    * @description: 用户注销
    * @author: liaocy
    * @date: 2023/5/7 22:20
    * @param: [request]
    * @return: int
    **/
    int userLogout(HttpServletRequest request);

    /*
    * @description: 根据用户标签查询用户
    * @author: liaocy
    * @date: 2023/5/13 23:35
    * @param: [tagNameList]
    * @return: java.util.List<com.example.usercenter.bean.User>
    **/
    List<User> serachUserByTags(List<String> tagNameList);

    /*
    * @description: 根据前端提交的表单更改用户信息
    * @author: liaocy
    * @date: 2023/5/22 16:27
    * @param: [user, loginUser]
    * @return: int
    **/
    int updateUser(User user, User loginUser);

    /**
     * @description: 获取最匹配的num个用户
     * @author: liaocy
     * @date: 2023/6/6 23:14
     * @param: [num, loginUser]
     * @return: java.util.List<com.example.usercenter.bean.Vo.UserVO>
     **/
    List<User> getMatchedUser(int num, User loginUser);

    /*
    * @description: 判断用户是否为管理员
    * @author: liaocy
    * @date: 2023/5/22 16:31
    * @param: [user]
    * @return: boolean
    **/
    boolean isAdmin(User user);

    /*
     * @description: 鉴权，是否是管理员
     * @author: liaocy
     * @date: 2023/5/7 20:26
     * @param: [request]
     * @return: boolean
     **/
    boolean isAdmin(HttpServletRequest request);

    public User getLoginUser(HttpServletRequest request);
}
