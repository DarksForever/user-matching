package com.example.usercenter.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.usercenter.bean.UserTeam;
import com.example.usercenter.service.UserTeamService;
import com.example.usercenter.mapper.UserTeamMapper;
import org.springframework.stereotype.Service;

/**
* @author ASUS
* @description 针对表【user_team(用户队伍关系)】的数据库操作Service实现
* @createDate 2023-05-31 15:58:34
*/
@Service
public class UserTeamServiceImpl extends ServiceImpl<UserTeamMapper, UserTeam>
    implements UserTeamService{

}




