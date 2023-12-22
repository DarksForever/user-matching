package com.example.usercenter.service.impl;

import cn.hutool.core.lang.Pair;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.usercenter.bean.Vo.UserVO;
import com.example.usercenter.common.AlgorithmUtils;
import com.example.usercenter.common.ErrorCode;
import com.example.usercenter.exception.BusinessException;
import com.example.usercenter.mapper.UserMapper;
import com.example.usercenter.service.UserService;
import com.example.usercenter.bean.User;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.example.usercenter.constant.UserConstant.ADMIN_ROLE;
import static com.example.usercenter.constant.UserConstant.USER_LOGIN_STATE;

/**
 * @author ASUS
 * @description 针对表【user】的数据库操作Service实现
 * @createDate 2023-05-05 20:54:47
 */
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
        implements UserService {

    @Resource
    private UserMapper userMapper;

    private final String SALT = "yupi";


    @Override
    public long userRegister(String userAccount, String password, String checkedPassword) {
        //校验账户、密码、确认密码非空
        if (StringUtils.isAnyBlank(userAccount, password, checkedPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"账号或密码为空!");
        }
        //账户不能重复
        Long count = userMapper.selectCount(new QueryWrapper<User>().
                eq("user_account", userAccount));
        if (count > 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"账号重复!");
        }
        //账户长度不小于4位,密码不小于8位
        if (userAccount.length() < 4 || password.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"账号或密码长度错误!");
        }
        //账户不包含特殊字符
        String validPattern = "[`~!@#$%^&*()+=|{}':;',\\\\[\\\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";
        Matcher matcher = Pattern.compile(validPattern).matcher(userAccount);
        if (matcher.find()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"账号包含特殊字符!");
        }

        //密码和确认密码相同
        if (!password.equals(checkedPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"密码和确认密码不相同!");
        }

        //对密码加密
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + password).getBytes());
        //插入数据
        User user = new User();
        user.setUserAccount(userAccount);
        user.setUserPassword(encryptPassword);
//        int insert = userMapper.insert(user);
        boolean result = this.save(user);
        if (!result) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"数据插入失败!");
        } else {
            return user.getId();
        }
    }

    @Override
    public User userLogin(String userAccount, String password, HttpServletRequest httpRequest) {
        //校验账户、密码非空
        if (StringUtils.isAnyBlank(userAccount, password)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"账号或密码为空！");
        }
        //账户长度不小于4位,密码不小于8位
        if (userAccount.length() < 4 || password.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"账号或密码长度错误！");
        }
        //账户不包含特殊字符
        String validPattern = "[`~!@#$%^&*()+=|{}':;',\\\\[\\\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";
        Matcher matcher = Pattern.compile(validPattern).matcher(userAccount);
        if (matcher.find()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"账号包含特殊字符！");
        }

        //对密码加密
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + password).getBytes());
        //查询用户是否存在
        User user = userMapper.selectOne(new QueryWrapper<User>().
                eq("user_account", userAccount).
                eq("user_password", encryptPassword));
        if(user==null){
            //log.info("user login failed,userAccount cannot match userPassword");
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"user login failed,userAccount cannot match userPassword");
        }
        //脱敏
        User safeUser = doSafe(user);

        //保存登录态
        httpRequest.getSession().setAttribute(USER_LOGIN_STATE,safeUser);

        return safeUser;
    }


    /*
    * @description: 脱敏
    * @author: liaocy
    * @date: 2023/5/7 20:07
    * @param: [user]
    * @return: com.example.usercenter.bean.User
    **/
    @Override
    public User doSafe(User user){
        if(user==null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User safeUser = new User();
        safeUser.setId(user.getId());
        safeUser.setUsername(user.getUsername());
        safeUser.setUserAccount(user.getUserAccount());
        safeUser.setAvatarUrl(user.getAvatarUrl());
        safeUser.setGender(user.getGender());
        safeUser.setPhone(user.getPhone());
        safeUser.setEmail(user.getEmail());
        safeUser.setUserStatus(user.getUserStatus());
        safeUser.setTags(user.getTags());
        safeUser.setUserRole(user.getUserRole());
        safeUser.setCreateTime(user.getCreateTime());
        return safeUser;
    }

    @Override
    public int userLogout(HttpServletRequest request) {
        request.getSession().removeAttribute(USER_LOGIN_STATE);
        return 1;
    }

    @Override
    public List<User> serachUserByTags(List<String> tagNameList) {
        //return serachUserByTagsBySQL(tagNameList);
        return serachUserByTagsByMemory(tagNameList);
    }

    @Override
    public int updateUser(User user, User loginUser) {
        long userId = user.getId();
        //校验参数
        if(user==null||loginUser==null||userId<=0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //校验是否能更改
        if(!isAdmin(loginUser)&&userId!=loginUser.getId()){
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        //更改信息
        return userMapper.updateById(user);
    }

    //SQL查询（实现简单，可以通过拆分查询进一步优化）
    private List<User> serachUserByTagsBySQL(List<String> tagNameList){
        //如果标签为空，不应该查询出所有用户
        if(CollectionUtils.isEmpty(tagNameList)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        QueryWrapper<User> wrapper=new QueryWrapper<>();
        for (String tagName:tagNameList){
            wrapper.like("tags",tagName);
        }
        List<User> users = userMapper.selectList(wrapper);
        //脱敏
        return users.stream().map(user -> {
            return doSafe(user);
        }).collect(Collectors.toList());
    }

    /*
    * @description: 内存查询（灵活，可以通过并发进一步优化）
    * @author: liaocy
    * @date: 2023/5/18 16:10
    * @param: [tagNameList 标签名列表 json]
    * @return: java.util.List<com.example.usercenter.bean.User>
    **/
    private List<User> serachUserByTagsByMemory(List<String> tagNameList){
        //如果标签为空，不应该查询出所有用户
        if(CollectionUtils.isEmpty(tagNameList)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //查询所有用户
        List<User> userList = this.list();
        //对每个用户进行标签对比
        return userList.stream().filter(user -> {
             String tags=user.getTags();
             if(tags==null){
                 return false;
             }
             //使用GSON把json字符串转化为String
             Gson gson=new Gson();
             Set<String> tagNameSet = gson.fromJson(tags, new TypeToken<Set<String>>() {
             }.getType());
             //java中集合对象一定要判空
             tagNameSet=Optional.ofNullable(tagNameSet).orElse(new HashSet<>());
             for(String tagName:tagNameList){
                 if(!tagNameSet.contains(tagName)){
                     return false;
                 }
             }
             return true;
        }).map(user->doSafe(user)).collect(Collectors.toList());
    }

    /*
    * @description: 判断用户是否为管理员
    * @author: liaocy
    * @date: 2023/5/22 16:31
    * @param: [user]
    * @return: boolean
    **/
    @Override
    public boolean isAdmin(User user){
        return user != null && user.getUserRole() == ADMIN_ROLE;
    }

    /*
     * @description: 鉴权，是否是管理员
     * @author: liaocy
     * @date: 2023/5/7 20:26
     * @param: [request]
     * @return: boolean
     **/
    @Override
    public boolean isAdmin(HttpServletRequest request){
        User user = (User) request.getSession().getAttribute(USER_LOGIN_STATE);
        return user != null && user.getUserRole() == ADMIN_ROLE;
    }

    /*
    * @description: 获取当前登录用户信息
    * @author: liaocy
    * @date: 2023/5/30 11:03
    * @param: [request]
    * @return: com.example.usercenter.bean.User
    **/
    @Override
    public User getLoginUser(HttpServletRequest request){
        User loginUser= (User) request.getSession().getAttribute(USER_LOGIN_STATE);
        if(loginUser==null){
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }else{
            return loginUser;
        }
    }

    @Override
    public List<User> getMatchedUser(int num, User loginUser) {
        //获取有标签的所有用户
        QueryWrapper<User> userQueryWrapper=new QueryWrapper<>();
        //      由于用户表数据量大，此处只获取需要用到的数据可以节省性能
        userQueryWrapper.select("id","tags");
        userQueryWrapper.isNotNull("tags");
        List<User> userList = this.list(userQueryWrapper);
        //依次匹配，计算分数，保存至TreeMap中
        String tags=loginUser.getTags();
        Gson gson = new Gson();
        List<String> tagList = gson.fromJson(tags, new TypeToken<List<String>>() {
        }.getType());
        //      map:userList下标 -> 匹配分数
        List<Pair<User,Integer>> list=new ArrayList<>();
        //      计算与所有其他用户的相似度
        for(int i=0;i<userList.size();i++){
            User user = userList.get(i);
            String userTags = user.getTags();
            //      无标签或为用户自身
            if(StringUtils.isBlank(userTags)||user.getId().equals(loginUser.getId())){
                continue;
            }
            List<String> userTagList = gson.fromJson(userTags, new TypeToken<List<String>>() {
            }.getType());
            //      计算分数
            int distance = AlgorithmUtils.minDistance(tagList, userTagList);
            list.add(new Pair<>(user,distance));
        }
        //按编辑距离由低到高排序并取前num
        List<Pair<User, Integer>> topUserPairList = list.stream()
                .sorted((a, b) -> a.getValue() - b.getValue())
                .limit(num).collect(Collectors.toList());
        //取出userId，原本顺序的 userId 列表
        List<Long> topUserIdList = topUserPairList.stream()
                .map(pair -> pair.getKey().getId())
                .collect(Collectors.toList());
        //取出对应User进行脱敏
        userQueryWrapper=new QueryWrapper<>();
        userQueryWrapper.in("id",topUserIdList);
        Map<Long, List<User>> userIdUserListMap = this.list(userQueryWrapper)
                .stream()
                .map(user -> this.doSafe(user))
                .collect(Collectors.groupingBy(user -> user.getId()));
        //根据原本顺序的userId列表添加新的list
        List<User> userVOList=new ArrayList<>();
        for(Long id:topUserIdList){
            userVOList.add(userIdUserListMap.get(id).get(0));
        }
        return userVOList;
    }
}




