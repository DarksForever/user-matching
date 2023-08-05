package com.example.usercenter.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.usercenter.bean.Team;
import com.example.usercenter.bean.User;
import com.example.usercenter.bean.Vo.TeamUserVO;
import com.example.usercenter.bean.Vo.TeamVO;
import com.example.usercenter.bean.requestVo.*;
import com.example.usercenter.common.BaseResponse;
import com.example.usercenter.common.ErrorCode;
import com.example.usercenter.common.ResultUtil;
import com.example.usercenter.exception.BusinessException;
import com.example.usercenter.service.TeamService;
import com.example.usercenter.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @Author 写你的名字
 * @Date 2023/5/7 16:51
 * @Version 1.0 （版本号）
 */
@RestController
@RequestMapping("/team")
@Slf4j
public class TeamController {
    @Resource
    private TeamService teamService;

    @Resource
    private UserService userService;

    /*
     * @description: 创建队伍
     * @author: liaocy
     * @date: 2023/5/7 16:54
     * @param: []
     * @return: long 队伍id
     **/
    @PostMapping("/add")
    public BaseResponse<Long> addTeam(@RequestBody TeamAddRequest teamAddRequest, HttpServletRequest request) {
        if (teamAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        Team team = new Team();
        BeanUtils.copyProperties(teamAddRequest,team);
        Long teamId = teamService.addTeam(team, loginUser);
        return ResultUtil.success(teamId);
    }

    /*
     * @description: 根据队伍id查询队伍
     * @author: liaocy
     * @date: 2023/5/7 20:16
     * @param: [username, request]
     * @return: java.util.List<com.example.usercenter.bean.User>
     **/
    @PostMapping("/get")
    public BaseResponse<Team> getTeamById(@RequestBody long id) {
        if(id<=0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Team team = teamService.getById(id);
        if(team==null){
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        return ResultUtil.success(team);
    }

    /*
     * @description: 删除队伍
     * @author: liaocy
     * @date: 2023/5/7 20:17
     * @param: [id]
     * @return: java.lang.Boolean
     **/
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteTeam(@RequestBody Long id,HttpServletRequest request) {
        if(id<=0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        boolean result = teamService.deleteTeam(id,loginUser);
        if(!result){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"删除失败");
        }
        return ResultUtil.success(true);
    }

    /*
    * @description: 更新队伍信息
    * @author: liaocy
    * @date: 2023/5/22 16:26
    * @param: [user 前端提交表单的信息]
    * @return: com.example.usercenter.common.BaseResponse<java.lang.Boolean>
    **/
    @PostMapping("/update")
    public BaseResponse<Boolean> updateTeam(@RequestBody TeamUpdateRequest teamUpdateRequest
    ,HttpServletRequest request){
        if(teamUpdateRequest==null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        boolean result = teamService.updateTeam(teamUpdateRequest,loginUser);
        if(!result){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"修改失败");
        }
        return ResultUtil.success(true);
    }

    /*
    * @description: 列表查询队伍
    * @author: liaocy
    * @date: 2023/5/31 23:06
    * @param: [teamQuery]
    * @return: com.example.usercenter.common.BaseResponse<java.util.List<com.example.usercenter.bean.Team>>
    **/
    @PostMapping("/list")
    public BaseResponse<List<TeamUserVO>> listTeams(TeamQuery teamQuery, HttpServletRequest request){
        if(teamQuery==null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean isAdmin = userService.isAdmin(request);
        User loginUser = userService.getLoginUser(request);
        List<TeamUserVO> teamList = teamService.listTeams(teamQuery,isAdmin,loginUser);
        return ResultUtil.success(teamList);
    }

    @PostMapping("/list/page")
    public BaseResponse<Page<Team>> listTeamsByPage(TeamQuery teamQuery){
        if(teamQuery==null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Team team=new Team();
        BeanUtils.copyProperties(teamQuery,team);
        Page<Team> page=new Page<>(teamQuery.getPageNum(),teamQuery.getPageSize());
        QueryWrapper<Team> queryWrapper=new QueryWrapper<>(team);
        Page<Team> teamPage = teamService.page(page, queryWrapper);
        return ResultUtil.success(teamPage);
    }

    /**
     * @description: 加入队伍
     * @author: liaocy
     * @date: 2023/6/5 17:00
     * @param: [joinTeamRequest, request]
     * @return: com.example.usercenter.common.BaseResponse<java.lang.Boolean>
     **/
    @PostMapping("/join")
    public BaseResponse<Boolean> joinTeam(JoinTeamRequest joinTeamRequest,HttpServletRequest request){
        if(joinTeamRequest==null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        boolean result = teamService.joinTeam(joinTeamRequest, loginUser);
        return ResultUtil.success(result);
    }

    /**
     * @description: 退出队伍
     * @author: liaocy
     * @date: 2023/6/5 22:52
     * @param: [quitTeamRequest, request]
     * @return: com.example.usercenter.common.BaseResponse<java.lang.Boolean>
     **/
    @PostMapping("/quit")
    public BaseResponse<Boolean> quitTeam(QuitTeamRequest quitTeamRequest,HttpServletRequest request){
        if(quitTeamRequest==null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        boolean result = teamService.quitTeam(quitTeamRequest, loginUser);
        return ResultUtil.success(result);
    }

    /**
     * @description: 获取当前用户加入的队伍
     * @author: liaocy
     * @date: 2023/6/6 10:04
     * @param: [request]
     * @return: com.example.usercenter.common.BaseResponse<java.util.List<com.example.usercenter.bean.Vo.TeamVO>>
     **/
    @GetMapping("/getCurrentJoinTeams")
    public BaseResponse<List<TeamVO>> getCurrentJoinTeams(HttpServletRequest request){
        User loginUser = userService.getLoginUser(request);
        List<TeamVO> currentLeadTeams = teamService.getCurrentJoinTeams(loginUser);
        return ResultUtil.success(currentLeadTeams);
    }

    /**
     * @description: 获取当前用户领导的队伍
     * @author: liaocy
     * @date: 2023/6/6 10:04
     * @param: [request]
     * @return: com.example.usercenter.common.BaseResponse<java.util.List<com.example.usercenter.bean.Vo.TeamVO>>
     **/
    @GetMapping("/getCurrentLeadTeams")
    public BaseResponse<List<TeamVO>> getCurrentLeadTeams(HttpServletRequest request){
        User loginUser = userService.getLoginUser(request);
        List<TeamVO> currentLeadTeams = teamService.getCurrentLeadTeams(loginUser);
        return ResultUtil.success(currentLeadTeams);
    }
}
