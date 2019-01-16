package com.hexu.miniapi.controller;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hexu.miniapi.mapper.InviteDayMapper;
import com.hexu.miniapi.mapper.InviteMapper;
import com.hexu.miniapi.mapper.UserLogMapper;
import com.hexu.miniapi.mapper.WxUserDAO;
import com.hexu.miniapi.model.Invite;
import com.hexu.miniapi.model.InviteDay;
import com.hexu.miniapi.model.UserLog;
import com.hexu.miniapi.model.WxUser;
import com.hexu.miniapi.task.Task;
import com.hexu.miniapi.util.RestResponse;
import com.hexu.miniapi.util.WXApi;
import com.hexu.miniapi.util.WXUserInfo;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>Copyright: All Rights Reserved</p>
 * <p>Company: 指点无限(北京)科技有限公司   http://www.zhidianwuxian.cn</p>
 * <p>Description:  </p>
 * <p>Author:hexu/方和煦, 18-6-15</p>
 */
@RestController
@RequestMapping(value = "/")
public class MainController {

    private static Logger logger = LoggerFactory.getLogger(MainController.class);


//    @Resource
//    private UserVoteDAO userVoteDAO;

    @Resource
    private WxUserDAO wxUserDAO;

    @Resource
    private InviteMapper inviteMapper;

    @Resource
    private InviteDayMapper inviteDayMapper;

    @Resource
    private UserLogMapper userLogMapper;

    @Autowired
    private Task task;

//    @RequestMapping(value = "test", method = RequestMethod.POST)
//    public RestResponse test(HttpServletRequest request) {
//
//        return new RestResponse(0, "正常");
//    }

    @ApiOperation(value = "getUserLog", notes = "getUserLog")
    @ApiImplicitParam(name = "openid", value = "openid", required = true, dataType = "String", paramType = "query")
    @RequestMapping(value = "getUserLog", method = RequestMethod.POST)
    public RestResponse getUserLog(HttpServletRequest request) {
        String openid = request.getParameter("openid");
        List<UserLog> list = userLogMapper.selectList(new QueryWrapper<UserLog>().lambda().eq(UserLog::getOpenId, openid));


        return new RestResponse(0, "正常", list);
    }

    @ApiOperation(value = "邀请用户", notes = "邀请用户")
    @ApiImplicitParams({@ApiImplicitParam(name = "fromopenid", value = "邀请方openid", required = true, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "toopenid", value = "呗邀请方openid", required = true, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "type", value = "type", required = true, dataType = "int", paramType = "query")})
    @RequestMapping(value = "invite", method = RequestMethod.POST)
    public RestResponse invite(HttpServletRequest request) {
        String fromopenid = request.getParameter("fromopenid");
        String toopenid = request.getParameter("toopenid");
        Integer type = Integer.valueOf(request.getParameter("type"));
        if (type.equals(1)) {
            //邀请
            List<Invite> list = inviteMapper.selectList(new QueryWrapper<Invite>().lambda().eq(Invite::getFromOpenId, fromopenid));
            if (list != null && list.size() >= 20) {
                return new RestResponse(-1, "已经邀请20人");
            }
            for (Invite invite : list) {
                if (invite.getToOpenId().equals(toopenid)) {
                    return new RestResponse(-1, "重复邀请");
                }
            }
            Invite invite = new Invite();
            invite.setFromOpenId(fromopenid);
            invite.setToOpenId(toopenid);
            invite.setStatus(0);
            invite.setCreateTime(new Date());
            inviteMapper.insert(invite);
        } else {
            //每日
            List<InviteDay> list = inviteDayMapper.selectList(new QueryWrapper<InviteDay>().lambda().eq(InviteDay::getFromOpenId, fromopenid));
            if (list != null && list.size() >= 3) {
                return new RestResponse(-1, "已经邀请3人");
            }
            for (InviteDay invite : list) {
                if (invite.getToOpenId().equals(toopenid)) {
                    return new RestResponse(-1, "重复邀请");
                }
            }
            InviteDay id = new InviteDay();
            id.setFromOpenId(fromopenid);
            id.setToOpenId(toopenid);
            id.setCreateTime(new Date());
            inviteDayMapper.insert(id);
        }
        return new RestResponse(0, "正常");
    }

    @ApiOperation(value = "领取奖励", notes = "领取奖励")
    @ApiImplicitParams({@ApiImplicitParam(name = "fromopenid", value = "邀请方openid", required = true, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "toopenid", value = "呗邀请方openid", required = true, dataType = "String", paramType = "query")})
    @RequestMapping(value = "receiveShare", method = RequestMethod.POST)
    public RestResponse receiveShare(HttpServletRequest request) {
        String fromopenid = request.getParameter("fromopenid");
        String toopenid = request.getParameter("toopenid");
        Invite invite = inviteMapper.selectOne(new QueryWrapper<Invite>().lambda().eq(Invite::getFromOpenId, fromopenid).eq(Invite::getToOpenId, toopenid).last("limit 1"));
        if (invite == null) {
            return new RestResponse(-1, "错误的信息");
        }
        if (invite.getStatus().equals(1)) {
            return new RestResponse(-1, "已经领取");
        }
        invite.setStatus(1);
        inviteMapper.updateById(invite);
        return new RestResponse(0, "正常");
    }

    @ApiOperation(value = "查看奖励领取状态0为未领取,1为领取", notes = "查看奖励领取状态0为未领取,1为领取")
    @ApiImplicitParam(name = "openid", value = "openid", required = true, dataType = "String", paramType = "query")
    @RequestMapping(value = "getShareList", method = RequestMethod.POST)
    public RestResponse getShareList(HttpServletRequest request) {
        String fromopenid = request.getParameter("openid");
        List<Invite> list = inviteMapper.selectList(new QueryWrapper<Invite>().lambda().eq(Invite::getFromOpenId, fromopenid));
        Map<String, Object> map = new HashMap<>();
        if (list != null) {
            map.put("count", list.size());
            map.put("list", list);
        } else {
            map.put("count", 0);
        }
        return new RestResponse(0, "正常", map);
    }


    @ApiOperation(value = "查看每日邀请情况", notes = "查看每日邀请情况")
    @ApiImplicitParam(name = "openid", value = "openid", required = true, dataType = "String", paramType = "query")
    @RequestMapping(value = "getDayShareList", method = RequestMethod.POST)
    public RestResponse getDayShareList(HttpServletRequest request) {
        String fromopenid = request.getParameter("openid");
        List<InviteDay> list = inviteDayMapper.selectList(new QueryWrapper<InviteDay>().lambda().eq(InviteDay::getFromOpenId, fromopenid));
        Map<String, Object> map = new HashMap<>();
        if (list != null) {
            map.put("count", list.size());
            map.put("list", list);
        } else {
            map.put("count", 0);
        }
        return new RestResponse(0, "正常", map);
    }


    @ApiOperation(value = "用code换取openid", notes = "用code换取openid")
    @ApiImplicitParam(name = "code", value = "微信code", required = true, dataType = "String", paramType = "query")
    @RequestMapping(value = "getopenid", method = RequestMethod.POST)
    public RestResponse<WXUserInfo> getopenid(HttpServletRequest request) {
        String code = request.getParameter("code");
        WXUserInfo wxUserInfo = WXApi.getUserInfoForH5(code);
        WxUser oldUser = wxUserDAO.selectById(wxUserInfo.getOpenId());
        if (oldUser != null) {
            wxUserInfo.setHeadImgUrl(oldUser.getImageUrl());
            wxUserInfo.setNickName(oldUser.getNikeName());
        }
        return new RestResponse(0, "正常", wxUserInfo);
    }


//    @ApiOperation(value = "输入密码清除数据", notes = "输入密码清除数据")
//    @ApiImplicitParam(name = "password", value = "输入密码清除数据", required = true, dataType = "String", paramType = "query")
//    @RequestMapping(value = "clear", method = RequestMethod.POST)
//    public RestResponse clear(HttpServletRequest request) {
//        String password = request.getParameter("password");
//        if (password.equals("clear123")) {
//            userVoteDAO.deleteAll();
//            return new RestResponse(0, "正常");
//        } else {
//            return new RestResponse(-1, "密码错误");
//        }
//    }

    @ApiOperation(value = "输入用户信息", notes = "输入用户信息")
    @ApiImplicitParams({@ApiImplicitParam(name = "openid", value = "openid", required = true, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "nickname", value = "昵称", required = true, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "imageurl", value = "头像", required = true, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "sex", value = "性别1男2女", required = true, dataType = "int", paramType = "query"),
            @ApiImplicitParam(name = "address", value = "地址", required = true, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "otherdata", value = "其他信息", required = true, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "money", value = "money", required = true, dataType = "int", paramType = "query"),
            @ApiImplicitParam(name = "diamond", value = "diamond", required = true, dataType = "int", paramType = "query"),
            @ApiImplicitParam(name = "level", value = "level", required = true, dataType = "int", paramType = "query"),
            @ApiImplicitParam(name = "otherdata2", value = "其他信息2", required = true, dataType = "String", paramType = "query")})
    @RequestMapping(value = "putwxuser", method = RequestMethod.POST)
    public RestResponse putWxUser(HttpServletRequest request) {
        String openId = request.getParameter("openid");
        String nickName = request.getParameter("nickname");
        String imageUrl = request.getParameter("imageurl");
        Integer sex = request.getParameter("sex") == null ? null : Integer.valueOf(request.getParameter("sex"));
        String address = request.getParameter("address");
        String otherData = request.getParameter("otherdata");
        String otherData2 = request.getParameter("otherdata2");
        Long money = request.getParameter("money") == null ? null : Long.valueOf(request.getParameter("money"));
        Long diamond = request.getParameter("diamond") == null ? null : Long.valueOf(request.getParameter("diamond"));
        Integer level = request.getParameter("level") == null ? null : Integer.valueOf(request.getParameter("level"));
        WxUser user = new WxUser();
        user.setOpenId(openId);
        user.setAddress(address);
        user.setImageUrl(imageUrl);
        user.setNikeName(nickName);
        user.setSex(sex);
        user.setOtherData(otherData);
        user.setOtherData2(otherData2);
        user.setMoney(money);
        user.setDiamond(diamond);
        user.setLevel(level);
        // FIXME: 2018/12/5 优化sql
        WxUser oldUser = wxUserDAO.selectById(openId);

        if (oldUser == null) {
            wxUserDAO.insert(user);
        } else {
            wxUserDAO.updateById(user);
            task.addDelete3Day(user);
        }

        return new RestResponse(0, "正常");
    }


    @ApiOperation(value = "获得用户信息", notes = "获得用户信息")
    @ApiImplicitParam(name = "openid", value = "openid", required = true, dataType = "String", paramType = "query")
    @RequestMapping(value = "getwxuser", method = RequestMethod.POST)
    public RestResponse<WxUser> getWxUser(HttpServletRequest request) {
        String openId = request.getParameter("openid");
        WxUser user = wxUserDAO.selectById(openId);
        return new RestResponse(0, "正常", user);
    }

    @ApiOperation(value = "输入密码清除用户数据", notes = "输入密码清除用户数据")
    @ApiImplicitParam(name = "password", value = "输入密码清除数据", required = true, dataType = "String", paramType = "query")
    @RequestMapping(value = "clearuser", method = RequestMethod.POST)
    public RestResponse clearuser(HttpServletRequest request) {
        String password = request.getParameter("password");
        if (password.equals("clear123")) {
            wxUserDAO.delete(null);
            userLogMapper.delete(null);
            return new RestResponse(0, "正常");
        } else {
            return new RestResponse(-1, "密码错误");
        }
    }

    @ApiOperation(value = "输入用户信息", notes = "输入用户信息")
    @ApiImplicitParams({@ApiImplicitParam(name = "openid", value = "openid", required = true, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "nickname", value = "昵称", required = false, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "imageurl", value = "头像", required = false, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "sex", value = "性别1男2女", required = false, dataType = "int", paramType = "query"),
            @ApiImplicitParam(name = "address", value = "地址", required = false, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "otherdata", value = "其他信息", required = false, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "money", value = "money", required = false, dataType = "int", paramType = "query"),
            @ApiImplicitParam(name = "diamond", value = "diamond", required = false, dataType = "int", paramType = "query"),
            @ApiImplicitParam(name = "level", value = "level", required = false, dataType = "int", paramType = "query"),
            @ApiImplicitParam(name = "otherdata2", value = "其他信息2", required = false, dataType = "String", paramType = "query")})
    @RequestMapping(value = "updatewxuser", method = RequestMethod.POST)
    public RestResponse updateWxUser(HttpServletRequest request) {
        String openId = request.getParameter("openid");
        String nickName = request.getParameter("nickname");
        String imageUrl = request.getParameter("imageurl");
        Integer sex = request.getParameter("sex") == null ? null : Integer.valueOf(request.getParameter("sex"));
        String address = request.getParameter("address");
        String otherData = request.getParameter("otherdata");
        String otherData2 = request.getParameter("otherdata2");
        Long money = request.getParameter("money") == null ? null : Long.valueOf(request.getParameter("money"));
        Long diamond = request.getParameter("diamond") == null ? null : Long.valueOf(request.getParameter("diamond"));
        Integer level = request.getParameter("level") == null ? null : Integer.valueOf(request.getParameter("level"));
        WxUser user = new WxUser();
        user.setOpenId(openId);
        user.setAddress(address);
        user.setImageUrl(imageUrl);
        user.setNikeName(nickName);
        user.setSex(sex);
        user.setOtherData(otherData);
        user.setOtherData2(otherData2);
        user.setMoney(money);
        user.setDiamond(diamond);
        user.setLevel(level);
        wxUserDAO.update(user, new UpdateWrapper<WxUser>().lambda().eq(WxUser::getOpenId, user.getOpenId()));
        task.addDelete3Day(user);
        return new RestResponse(0, "正常");
    }

    @ApiOperation(value = "用户列表", notes = "用户列表")
    @ApiImplicitParams({@ApiImplicitParam(name = "page", value = "页号", required = true, dataType = "int", paramType = "query"),
            @ApiImplicitParam(name = "size", value = "页面大小", required = true, dataType = "int", paramType = "query"),
            @ApiImplicitParam(name = "password", value = "密码", required = true, dataType = "int", paramType = "query")})
    @RequestMapping(value = "/getuserlist", method = RequestMethod.GET)
    public RestResponse getUserList(Integer page, Integer size, String password) {
        if (!password.equals("query123")) {
            return new RestResponse(-1, "密码错误");
        }
        Page<WxUser> pageInfo = new Page<>(page, size);
        IPage<WxUser> list = wxUserDAO.selectPage(pageInfo, null);
        return new RestResponse(0, "正常", list);
    }

}
