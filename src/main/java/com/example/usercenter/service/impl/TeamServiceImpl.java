package com.example.usercenter.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.usercenter.bean.Team;
import com.example.usercenter.bean.User;
import com.example.usercenter.bean.UserTeam;
import com.example.usercenter.bean.Vo.TeamUserVO;
import com.example.usercenter.bean.Vo.TeamVO;
import com.example.usercenter.bean.Vo.UserVO;
import com.example.usercenter.bean.enums.TeamStatusEnum;
import com.example.usercenter.bean.requestVo.JoinTeamRequest;
import com.example.usercenter.bean.requestVo.QuitTeamRequest;
import com.example.usercenter.bean.requestVo.TeamQuery;
import com.example.usercenter.bean.requestVo.TeamUpdateRequest;
import com.example.usercenter.common.ErrorCode;
import com.example.usercenter.exception.BusinessException;
import com.example.usercenter.service.TeamService;
import com.example.usercenter.mapper.TeamMapper;
import com.example.usercenter.service.UserService;
import com.example.usercenter.service.UserTeamService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
* @author ASUS
* @description 针对表【team(队伍)】的数据库操作Service实现
* @createDate 2023-05-31 15:46:41
*/
@Service
public class TeamServiceImpl extends ServiceImpl<TeamMapper, Team>
    implements TeamService {

    @Resource
    private UserTeamService userTeamService;

    @Resource
    private UserService userService;


    /*
    * @description: 添加队伍
    * @author: liaocy
    * @date: 2023/6/1 15:33
    * @param: [team, loginUser]
    * @return: java.lang.Long
    **/
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long addTeam(Team team, User loginUser){
        //1. 请求参数是否为空？
        if(team==null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //2. 是否登录，未登录不允许创建
        if(loginUser==null){
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        //3. 校验信息
        Long userId = loginUser.getId();
        //   1. 队伍人数 > 1 且 <= 20
        Integer maxNum = team.getMaxNum();
        if(maxNum==null||maxNum<=1||maxNum>20){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"队伍人数不满足要求");
        }
        //   2. 队伍标题 <= 20
        String name = team.getName();
        if(StringUtils.isBlank(name) ||name.length()>20){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"队伍名称不满足要求");
        }
        //   3. 描述 <= 512
        String description = team.getDescription();
        if(StringUtils.isNotBlank(description)&&description.length()>512){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"队伍描述过长");
        }
        //   4. status 是否公开（int）不传默认为 0（公开）
        Integer status = Optional.ofNullable(team.getStatus()).orElse(0);
        TeamStatusEnum teamStatusEnum = TeamStatusEnum.getEnumByValue(status);
        if(teamStatusEnum==null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"队伍状态不满足要求");
        }
        //   5. 如果 status 是加密状态，一定要有密码，且密码 <= 32
        String password = team.getPassword();
        if(TeamStatusEnum.SECRET.equals(teamStatusEnum)){
            if(StringUtils.isBlank(password)||password.length()>32){
                throw new BusinessException(ErrorCode.PARAMS_ERROR,"队伍密码不满足要求");
            }
        }
        //   6. 超时时间需要 > 当前时间
        Date expireTime = team.getExpireTime();
        if(new Date().after(expireTime)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"队伍超时时间早于当前时间");
        }
        //   7. 校验用户最多创建 5 个队伍
        // todo 有bug。并发创建队伍 可能同时创建100个队伍
        QueryWrapper<Team> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id",userId);
        long hasTeamNum = this.count(queryWrapper);
        if(hasTeamNum>=5){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"当前已创建队伍多于5");
        }
        //4. 插入队伍信息到队伍表
        //team.setId(null);
        team.setUserId(userId);
        boolean result = this.save(team);
        if(!result){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"队伍插入失败");
        }
        Long teamId = team.getId();
        //5. 插入用户 => 队伍关系到关系表
        UserTeam userTeam = new UserTeam();
        userTeam.setUserId(userId);
        userTeam.setTeamId(teamId);
        userTeam.setJoinTime(new Date());
        result = userTeamService.save(userTeam);
        if(!result){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"用户-队伍关系插入失败");
        }
        return teamId;
    }

    @Override
    public List<TeamUserVO> listTeams(TeamQuery teamQuery, boolean isAdmin,User loginUser) {
        //1. 从请求参数中取出队伍名称等查询条件，如果存在则作为查询条件
        QueryWrapper<Team> queryWrapper=new QueryWrapper<>();
        if(teamQuery!=null){
            Long id = teamQuery.getId();
            if(id!=null&&id>0){
                queryWrapper.eq("id",id);
            }
            String name = teamQuery.getName();
            if(StringUtils.isNotBlank(name)){
                queryWrapper.like("name",name);
            }
            String description = teamQuery.getDescription();
            if(StringUtils.isNotBlank(description)){
                queryWrapper.like("description",description);
            }
            Integer maxNum = teamQuery.getMaxNum();
            if(maxNum!=null&&maxNum>0){
                queryWrapper.eq("max_num",maxNum);
            }
            Long userId = teamQuery.getUserId();
            if(userId!=null&&userId>0){
                queryWrapper.eq("user_id",userId);
            }
            //3.通过某个关键词同时对队伍名称和队伍描述查询
            String searchText = teamQuery.getSearchText();
            if(StringUtils.isNotBlank(searchText)){
                queryWrapper.and(qw->qw.like("name",searchText).or()
                        .like("description",searchText));
            }
            //4. 只有管理员才能查看加密还有非公开的房间
            Integer status = teamQuery.getStatus();
            TeamStatusEnum teamStatusEnum = TeamStatusEnum.getEnumByValue(status);
            if(teamStatusEnum==null){
                teamStatusEnum=TeamStatusEnum.PUBLIC;
            }
            if(!isAdmin&&!teamStatusEnum.equals(TeamStatusEnum.PUBLIC)){
                throw new BusinessException(ErrorCode.NO_AUTH);
            }
            queryWrapper.eq("status",teamStatusEnum.getValue());
        }
        //2. 不展示已过期的队伍（根据过期时间筛选）
        //即查expireTime>now()||expireTime==null
        queryWrapper.and(qw->qw.gt("expire_time",new Date())
                .or().isNull("expire_time"));
        List<Team> teamList = this.list(queryWrapper);
        if(CollectionUtils.isEmpty(teamList)){
            return new ArrayList<>();
        }

        //5. 关联查询已加入队伍的用户信息
        //Team->TeamUserVO 需要补充UserVo、hasJoinNum、hasJoin
        List<TeamUserVO> teamUserVOList=new ArrayList<>();
        for(Team team:teamList){
            //根据队伍里的userID查询创建人信息
            Long userId = team.getUserId();
            User user = userService.getById(userId);
            TeamUserVO teamUserVO = new TeamUserVO();
            BeanUtils.copyProperties(team,teamUserVO);
            if(user!=null) {
                UserVO userVO = new UserVO();
                BeanUtils.copyProperties(user, userVO);
                teamUserVO.setCreateUser(userVO);
            }
            //查询已加入该队伍的人数
            QueryWrapper<UserTeam> teamQueryWrapper=new QueryWrapper<>();
            teamQueryWrapper.eq("team_id",team.getId());
            Long hasJoinTeamNum = userTeamService.count(teamQueryWrapper);
            teamUserVO.setHasJoinNum(hasJoinTeamNum);
            //查询是否加入当前队伍
            teamQueryWrapper=new QueryWrapper<>();
            teamQueryWrapper.eq("team_id",team.getId()).eq("user_id",loginUser.getId());
            long count = userTeamService.count(teamQueryWrapper);
            if(count!=0){
                teamUserVO.setHasJoin(true);
            }
            teamUserVOList.add(teamUserVO);
        }
        return teamUserVOList;
    }

    @Override
    public boolean updateTeam(TeamUpdateRequest teamUpdateRequest, User loginUser) {
        //1. 判断请求参数是否为空
        if(teamUpdateRequest==null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"请求参数不能为空");
        }
        //2. 查询队伍是否存在
        Long teamId = teamUpdateRequest.getId();
        if(teamId==null||teamId<0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Team team = this.getById(teamId);
        if(team==null){
            throw new BusinessException(ErrorCode.NULL_ERROR,"队伍不存在");
        }
        //3. 只有管理员或者队伍的创建者可以修改
        if(!userService.isAdmin(loginUser)&&!team.getUserId().equals(loginUser.getId())){
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        //4. 如果用户传入的新值和老值一致，就不用 update 了（可自行实现，降低数据库使用次数）
        //5. 如果队伍状态改为加密，必须要有密码
        Integer status = teamUpdateRequest.getStatus();
        TeamStatusEnum teamStatusEnum = TeamStatusEnum.getEnumByValue(status);
        if(TeamStatusEnum.SECRET.equals(teamStatusEnum)){
            if(StringUtils.isBlank(teamUpdateRequest.getPassword())){
                throw new BusinessException(ErrorCode.PARAMS_ERROR,"加密房间需要设置密码");
            }
        }
        //6. 更新成功
        BeanUtils.copyProperties(teamUpdateRequest,team);
        return this.updateById(team);
    }

    @Override
    public boolean joinTeam(JoinTeamRequest joinTeamRequest, User loginUser) {
        //1、校验参数
        if(joinTeamRequest==null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //3、队伍存在、未过期
        Long teamId = joinTeamRequest.getTeamId();
        if(teamId==null||teamId<0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Team team = this.getById(teamId);
        if(team==null){
            throw new BusinessException(ErrorCode.NULL_ERROR,"队伍不存在");
        }
        Date expireTime = team.getExpireTime();
        if(expireTime!=null&&expireTime.before(new Date())){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"队伍已过期");
        }
        //4、若队伍加密（status=2），需要输入正确的密码
        Integer status = team.getStatus();
        TeamStatusEnum teamStatusEnum = TeamStatusEnum.getEnumByValue(status);
        if(TeamStatusEnum.SECRET.equals(teamStatusEnum)){
            String password = joinTeamRequest.getPassword();
            if(StringUtils.isBlank(password)||!password.equals(team.getPassword())){
                throw new BusinessException(ErrorCode.PARAMS_ERROR,"密码错误");
            }
        }
        //5、若队伍私密，不允许加入该队伍
        if(TeamStatusEnum.PRIVATE.equals(teamStatusEnum)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"禁止加入私有队伍");
        }
        //2、用户最多加入5个队伍,涉及数据库查询，放在参数校验后
        Long userId = loginUser.getId();
        QueryWrapper<UserTeam> queryWrapper=new QueryWrapper<>();
        queryWrapper.eq("user_id",userId);
        long hasJoinNUm = userTeamService.count(queryWrapper);
        if(hasJoinNUm>=5){
            throw new BusinessException(ErrorCode.NO_AUTH,"用户最多加入5个队伍");
        }
        //6、不能加入自己创建的队伍，不能重复加入已进入的队伍
        Long createUserId = team.getUserId();
        if(createUserId.equals(loginUser.getId())){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"禁止加入自己创建的队伍");
        }
        List<UserTeam> joinTeamList = userTeamService.list(queryWrapper);
        if(!CollectionUtils.isEmpty(joinTeamList)) {
            for (UserTeam joinTeam : joinTeamList) {
                if(joinTeam.getTeamId().equals(joinTeamRequest.getTeamId())){
                    throw new BusinessException(ErrorCode.PARAMS_ERROR,"已加入该队伍");
                }
            }
        }
        //不能加入已达人数上限的队伍
        queryWrapper=new QueryWrapper<>();
        queryWrapper.eq("team_id",teamId);
        long teamHasJoinNum = userTeamService.count(queryWrapper);
        if(teamHasJoinNum>=team.getMaxNum()){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"该队伍已满员");
        }
        //7、更新UserTeam表信息
        UserTeam userTeam = new UserTeam();
        userTeam.setUserId(loginUser.getId());
        userTeam.setTeamId(joinTeamRequest.getTeamId());
        userTeam.setJoinTime(new Date());
        return userTeamService.save(userTeam);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean quitTeam(QuitTeamRequest quitTeamRequest, User loginUser) {
        //1、校验参数
        if(quitTeamRequest==null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //2、队伍是否存在
        Long teamId = quitTeamRequest.getTeamId();
        if(teamId==null||teamId<=0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"队伍号不符合要求");
        }
        Team team = this.getById(teamId);
        if(team==null){
            throw new BusinessException(ErrorCode.NULL_ERROR,"队伍不存在");
        }
        //3、是否加入队伍
        Long userId = loginUser.getId();
        QueryWrapper<UserTeam> queryWrapper=new QueryWrapper<>();
        queryWrapper.eq("user_id",userId).eq("team_id",teamId);
        long count = userTeamService.count(queryWrapper);
        if(count==0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"您未加入该队伍");
        }
        //4、如果队伍
        //   仅剩一人    队伍解散
        long hasJoinTeamNum = this.countUserTeamByTeamId(teamId);
        if(hasJoinTeamNum==1){
            this.removeById(teamId);
        }else{
            //还有多人    队长退出，将队长移交給最早加入队伍的人
            if(team.getUserId().equals(userId)){
                //查最早加入队伍的两个人，队伍由队长创建且退出移交给最早，因此队长一定是最早加入队伍的
                queryWrapper=new QueryWrapper<>();
                queryWrapper.eq("team_id",teamId).orderByAsc("join_time").last("limit 2");
                List<UserTeam> earlyJoinUsers=userTeamService.list(queryWrapper);
                //如果不足两人
                if(CollectionUtils.isEmpty(earlyJoinUsers)||earlyJoinUsers.size()<2){
                    throw new BusinessException(ErrorCode.SYSTEM_ERROR);
                }
                UserTeam nextUserTeam = earlyJoinUsers.get(1);
                Long nextTeamLeaderId = nextUserTeam.getUserId();
                //更新当前队伍队长
                team.setUserId(nextTeamLeaderId);
                boolean updateResult = this.updateById(team);
                if(!updateResult){
                    throw new BusinessException(ErrorCode.SYSTEM_ERROR,"修改队伍队长失败");
                }
            }
            // 非队长，自己退出,移除userTeam关系
        }
        queryWrapper=new QueryWrapper<>();
        queryWrapper.eq("team_id",teamId).eq("user_id",userId);
        return userTeamService.remove(queryWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteTeam(long teamId, User loginUser){
        //1、校验参数
        if(teamId<=0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //2、队伍存在
        Team team = this.getById(teamId);
        if(team==null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"队伍不存在");
        }
        //3、只有队长可以解散队伍
        Long loginUserId = loginUser.getId();
        if(!loginUserId.equals(team.getUserId())){
            throw new BusinessException(ErrorCode.NO_AUTH,"您不是队伍队长");
        }
        //4、删除所有该队伍关联信息
        QueryWrapper<UserTeam> userTeamQueryWrapper=new QueryWrapper<>();
        userTeamQueryWrapper.eq("team_id",teamId);
        boolean removeResult = userTeamService.remove(userTeamQueryWrapper);
        if(!removeResult){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"删除user_team表信息失败");
        }
        //5、删除队伍
        return this.removeById(teamId);
    }

    @Override
    public List<TeamVO> getCurrentJoinTeams(User loginUser){
        List<TeamVO> teamVOList=new ArrayList<>();
        Long loginUserId = loginUser.getId();
        QueryWrapper<UserTeam> userTeamQueryWrapper=new QueryWrapper<>();
        userTeamQueryWrapper.eq("user_id",loginUserId);
        List<UserTeam> userTeamList = userTeamService.list(userTeamQueryWrapper);
        if(CollectionUtils.isEmpty(userTeamList)){
            throw new BusinessException(ErrorCode.NULL_ERROR,"没有加入队伍");
        }
        for(UserTeam userTeam:userTeamList){
            Long teamId = userTeam.getTeamId();
            if(teamId==null||teamId<=0){
                throw new BusinessException(ErrorCode.SYSTEM_ERROR,"查询出的队伍id异常");
            }
            Team team = this.getById(teamId);
            //不展示过期队伍，可注释掉
            if(team.getExpireTime().before(new Date())){
                continue;
            }
            TeamVO teamVO = new TeamVO();
            BeanUtils.copyProperties(team,teamVO);
            teamVOList.add(teamVO);
        }
        return teamVOList;
    }

    @Override
    public List<TeamVO> getCurrentLeadTeams(User loginUser){
        List<TeamVO> teamVOList=new ArrayList<>();
        Long loginUserId = loginUser.getId();
        QueryWrapper<Team> teamQueryWrapper=new QueryWrapper<>();
        //不展示过期队伍，可删掉第二行条件展示过期队伍
        teamQueryWrapper.eq("user_id",loginUserId)
                .and(qw->qw.gt("expire_time",new Date()).or().isNull("expire_time"));
        List<Team> teamList = this.list(teamQueryWrapper);
        if(CollectionUtils.isEmpty(teamList)){
            throw new BusinessException(ErrorCode.NULL_ERROR,"没有作为队长的队伍");
        }
        for(Team team:teamList){
            TeamVO teamVO = new TeamVO();
            BeanUtils.copyProperties(team,teamVO);
            teamVOList.add(teamVO);
        }
        return teamVOList;
    }

    public long countUserTeamByTeamId(Long teamId){
        QueryWrapper<UserTeam> queryWrapper=new QueryWrapper<>();
        queryWrapper.eq("team_id",teamId);
        return userTeamService.count(queryWrapper);
    }
}




