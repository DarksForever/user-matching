package com.example.usercenter.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.usercenter.bean.User;
import com.example.usercenter.bean.Vo.UserVO;
import com.example.usercenter.bean.requestVo.UserLoginRequest;
import com.example.usercenter.bean.requestVo.UserRegisterRequest;
import com.example.usercenter.common.BaseResponse;
import com.example.usercenter.common.ErrorCode;
import com.example.usercenter.common.ResultUtil;
import com.example.usercenter.exception.BusinessException;
import com.example.usercenter.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.example.usercenter.constant.UserConstant.ADMIN_ROLE;
import static com.example.usercenter.constant.UserConstant.USER_LOGIN_STATE;

/**
 * @Author 写你的名字
 * @Date 2023/5/7 16:51
 * @Version 1.0 （版本号）
 */
@RestController
@RequestMapping("/user")
@Slf4j
@CrossOrigin(origins = {"http://localhost:5173/"})
public class UserController {

    @Resource
    private UserService userService;

    @Resource
    private RedisTemplate redisTemplate;

    /*
     * @description: 注册
     * @author: liaocy
     * @date: 2023/5/7 16:54
     * @param: []
     * @return: long 用户id
     **/
    @PostMapping("/regist")
    public BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest userRegisterRequest) {
        if (userRegisterRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String userAccount = userRegisterRequest.getUserAccount();
        String password = userRegisterRequest.getUserPassword();
        String checkedPassword = userRegisterRequest.getCheckedPassword();
        if (StringUtils.isAnyBlank(userAccount, password, checkedPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        long result = userService.userRegister(userAccount, password, checkedPassword);
        return ResultUtil.success(result);
    }

    /*
     * @description: 登录
     * @author: liaocy
     * @date: 2023/5/7 17:12
     * @param: [userLoginRequest（以json格式接收）, request]
     * @return: com.example.usercenter.bean.User 登录用户
     **/
    @PostMapping("/login")
    public BaseResponse<User> userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request) {
        if (userLoginRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String userAccount = userLoginRequest.getUserAccount();
        String password = userLoginRequest.getUserPassword();
        if (StringUtils.isAnyBlank(userAccount, password)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.userLogin(userAccount, password, request);
        return ResultUtil.success(user);
    }

    /*
     * @description: 获取当前登录的用户信息
     * @author: liaocy
     * @date: 2023/5/9 14:38
     * @param: [request]
     * @return: com.example.usercenter.bean.User
     **/
    @GetMapping("/current")
    public BaseResponse<User> getCurrentUser(HttpServletRequest request) {
        User user = (User) request.getSession().getAttribute(USER_LOGIN_STATE);
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        Long id = user.getId();
        User currentUser = userService.getById(id);
        //TODO 校验当前用户是否合法
        User safeUser = userService.doSafe(currentUser);
        return ResultUtil.success(safeUser);
    }

    /*
     * @description: 注销用户
     * @author: liaocy
     * @date: 2023/5/7 22:21
     * @param: [userLoginRequest, request]
     * @return: com.example.usercenter.bean.User
     **/
    @PostMapping("/logout")
    public BaseResponse<Integer> userLogout(HttpServletRequest request) {
        if (request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        int result = userService.userLogout(request);
        return ResultUtil.success(result);
    }

    /*
     * @description: 根据账号查询用户
     * @author: liaocy
     * @date: 2023/5/7 20:16
     * @param: [username, request]
     * @return: java.util.List<com.example.usercenter.bean.User>
     **/
    @GetMapping("/search")
    public BaseResponse<List<User>> searchUsers(String username, HttpServletRequest request) {
        //鉴权，仅管理员可以查询用户
        if (!userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        //校验,查询条件不为空时加上输入的限定条件
        if (StringUtils.isNotBlank(username)) {
            wrapper.like("username", username);
        }
        List<User> userList = userService.list(wrapper);
        List<User> users = userList.stream().map(user -> {
            return userService.doSafe(user);
        }).collect(Collectors.toList());
        return ResultUtil.success(users);
    }

    /**
     * 根据标签搜索用户
     *
     * @param tagNameList
     * @return
     */
    @GetMapping("/search/tags")
    public BaseResponse<List<User>> searchUsersByTags(@RequestParam(required = false) List<String> tagNameList) {
        if (CollectionUtils.isEmpty(tagNameList)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        List<User> userList = userService.serachUserByTags(tagNameList);
        return ResultUtil.success(userList);
    }

    /*
     * @description: 删除用户
     * @author: liaocy
     * @date: 2023/5/7 20:17
     * @param: [id]
     * @return: java.lang.Boolean
     **/
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteUser(Long id, HttpServletRequest request) {
        //鉴权，仅管理员可以删除用户
        if (!userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        boolean result = userService.removeById(id);
        return ResultUtil.success(result);
    }

    /*
     * @description: 更新用户信息
     * @author: liaocy
     * @date: 2023/5/22 16:26
     * @param: [user 前端提交表单的信息]
     * @return: com.example.usercenter.common.BaseResponse<java.lang.Integer>
     **/
    @PostMapping("/update")
    public BaseResponse<Integer> updateUser(@RequestBody User user, HttpServletRequest request) {
        if (user == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = (User) request.getSession().getAttribute(USER_LOGIN_STATE);
        int result = userService.updateUser(user, loginUser);
        return ResultUtil.success(result);
    }

    //TODO 推荐匹配用户
    @GetMapping("/recommend")
    public BaseResponse<Page<User>> recommendUsers(long pageSize, long pageNum, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        String redisKey = String.format("yupao:user:recommend:%s", loginUser.getId());
        ValueOperations valueOperations = redisTemplate.opsForValue();
        //如果有缓存，直接读缓存
        Page<User> userPage = (Page<User>) valueOperations.get(redisKey);
        if (userPage != null) {
            return ResultUtil.success(userPage);
        }
        //无缓存，先从数据库读数据
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        userPage = userService.page(new Page<>(pageNum, pageSize), wrapper);
        //写缓存,即使缓存有异常，也不应该影响到业务的正常执行
        try {
            //所有写入Redis中的数据一定要设置过期时间
            valueOperations.set(redisKey, userPage, 1000000, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            //打印日志告知缓存异常即可，不需要抛出异常
            log.error("redis set key error", e);
        }
        return ResultUtil.success(userPage);
    }

    /**
     * @description: 获取当前用户的匹配用户
     * @author: liaocy
     * @date: 2023/6/6 23:10
     * @param: [num 展示最多num个匹配用户, request]
     * @return: com.example.usercenter.common.BaseResponse<java.util.List < com.example.usercenter.bean.User>>
     **/
    @GetMapping("/match")
    public BaseResponse<List<User>> getMatchedUser(int num, HttpServletRequest request) {
        //num限制上限防止被恶意查询所有用户
        if (num <= 0 || num > 20) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        List<User> userList = userService.getMatchedUser(num, loginUser);
        return ResultUtil.success(userList);
    }
}
