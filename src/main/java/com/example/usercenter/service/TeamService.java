package com.example.usercenter.service;

import com.example.usercenter.bean.Team;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.usercenter.bean.Team;
import com.example.usercenter.bean.User;
import com.example.usercenter.bean.Vo.TeamUserVO;
import com.example.usercenter.bean.Vo.TeamVO;
import com.example.usercenter.bean.Vo.UserVO;
import com.example.usercenter.bean.requestVo.JoinTeamRequest;
import com.example.usercenter.bean.requestVo.QuitTeamRequest;
import com.example.usercenter.bean.requestVo.TeamQuery;
import com.example.usercenter.bean.requestVo.TeamUpdateRequest;

import java.util.List;

/**
* @author ASUS
* @description 针对表【team(队伍)】的数据库操作Service
* @createDate 2023-05-31 15:46:41
*/
public interface TeamService extends IService<Team> {
    /**
     * @description: 添加队伍
     * @author: liaocy
     * @date: 2023/6/3 16:00
     * @param: [team, loginUser]
     * @return: java.lang.Long
     **/
    public Long addTeam(Team team, User loginUser);

    /**
     * @description: 列表展示有查看权限的队伍
     * @author: liaocy
     * @date: 2023/6/5 17:35
     * @param: [teamQuery, isAdmin, loginUser]
     * @return: java.util.List<com.example.usercenter.bean.Vo.TeamUserVO>
     **/
    public List<TeamUserVO> listTeams(TeamQuery teamQuery, boolean isAdmin,User loginUser);

    /**
     * @description: 修改队伍信息
     * @author: liaocy
     * @date: 2023/6/4 21:05
     * @param: [teamUpdateRequest, loginUser]
     * @return: java.lang.Long
     **/
    public boolean updateTeam(TeamUpdateRequest teamUpdateRequest,User loginUser);

    /**
     * @description: 加入队伍
     * @author: liaocy
     * @date: 2023/6/4 22:35
     * @param: [joinTeamRequest, loginUser]
     * @return: boolean
     **/
    boolean joinTeam(JoinTeamRequest joinTeamRequest, User loginUser);


    /**
     * @description: 退出队伍
     * @author: liaocy
     * @date: 2023/6/5 21:43
     * @param: [quitTeamRequest, loginUser]
     * @return: boolean
     **/
    boolean quitTeam(QuitTeamRequest quitTeamRequest, User loginUser);

    /**
     * @description: 解散队伍
     * @author: liaocy
     * @date: 2023/6/5 22:42
     * @param: [teamId, loginUser]
     * @return: boolean
     **/
    boolean deleteTeam(long teamId, User loginUser);

    /**
     * @description: 获取当前用户加入的队伍
     * @author: liaocy
     * @date: 2023/6/6 10:09
     * @param: [loginUser]
     * @return: java.util.List<com.example.usercenter.bean.Vo.TeamVO>
     **/
    List<TeamVO> getCurrentJoinTeams(User loginUser);

    /**
     * @description: 获取当前用户领导的队伍
     * @author: liaocy
     * @date: 2023/6/6 10:02
     * @param: [loginUser]
     * @return: java.util.List<com.example.usercenter.bean.Vo.TeamVO>
     **/
    List<TeamVO> getCurrentLeadTeams(User loginUser);

}
