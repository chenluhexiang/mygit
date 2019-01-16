package com.hexu.miniapi.vo;

import com.hexu.miniapi.model.UserVote;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>Copyright: All Rights Reserved</p>
 * <p>Company: 指点无限(北京)科技有限公司   http://www.zhidianwuxian.cn</p>
 * <p>Description:  </p>
 * <p>Author:hexu/方和煦, 18-6-15</p>
 */
public class UserVo {

    List<UserVote> voteList = new ArrayList<>();


    public List<UserVote> getVoteList() {
        return voteList;
    }

    public void setVoteList(List<UserVote> voteList) {
        this.voteList = voteList;
    }

}
