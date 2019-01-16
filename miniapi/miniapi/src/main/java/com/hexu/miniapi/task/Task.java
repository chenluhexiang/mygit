package com.hexu.miniapi.task;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.hexu.miniapi.mapper.UserLogMapper;
import com.hexu.miniapi.model.UserLog;
import com.hexu.miniapi.model.WxUser;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Component
public class Task {

    @Resource
    private UserLogMapper userLogMapper;

    @Async
    public void addDelete3Day(WxUser user) {
        List<UserLog> list = userLogMapper.selectList(new QueryWrapper<UserLog>().lambda().eq(UserLog::getOpenId, user.getOpenId()).orderByDesc(UserLog::getCreateTime));
        if (list != null && list.size() > 0) {
            Date d1 = new Date();
            d1 = list.get(0).getCreateTime();

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(d1);
            calendar.add(Calendar.DAY_OF_MONTH, -1);

            Date d2 = calendar.getTime();
            for (UserLog userLog : list) {
                if (userLog.getCreateTime().before(d2)) {
                    d2 = userLog.getCreateTime();
                    break;
                }
            }
            calendar.setTime(d2);
            calendar.add(Calendar.DAY_OF_MONTH, -1);

            Date d3 = calendar.getTime();
            for (UserLog userLog : list) {
                if (userLog.getCreateTime().before(d3)) {
                    d3 = userLog.getCreateTime();
                    break;
                }
            }

            List<Long> ids = new ArrayList<>();
            for (UserLog userLog : list) {
                if (userLog.getCreateTime().before(d3)) {
                    ids.add(userLog.getId());
                }
            }

            if(ids.size()>0){
                userLogMapper.deleteBatchIds(ids);
            }
        }
        UserLog userLog = new UserLog();
        userLog.setOpenId(user.getOpenId());
        userLog.setLevel(user.getLevel());
        userLog.setMoney(user.getMoney());
        userLog.setOtherData(user.getOtherData());
        userLog.setCreateTime(new Date());
        userLogMapper.insert(userLog);
    }


}
