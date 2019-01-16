package com.hexu.miniapi.task;

import com.hexu.miniapi.mapper.InviteDayMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class ScheduledTasks {

    private static Logger logger = LoggerFactory.getLogger(ScheduledTasks.class);

    @Resource
    private InviteDayMapper inviteDayMapper;

    @Scheduled(cron = "0 0 6 * * ?")
    public void deleteDayInvite() {
        inviteDayMapper.delete(null);
        logger.info("=======================deleteDayInvite========================");
    }

}
