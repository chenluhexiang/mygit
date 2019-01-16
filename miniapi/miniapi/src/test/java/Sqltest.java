
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.hexu.miniapi.Application;
import com.hexu.miniapi.mapper.InviteDayMapper;
import com.hexu.miniapi.mapper.WxUserDAO;
import com.hexu.miniapi.model.WxUser;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.Date;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = Application.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class Sqltest {

//    @Resource
//    private SignupMapper signupMapper;

    @Resource
    private InviteDayMapper inviteDayMapper;


    @Resource
    private WxUserDAO wxUserDAO;

    @Test
    public void deleteall() {
        inviteDayMapper.delete(null);
        System.out.println(1);
    }

    @Test
    public void update() {
        WxUser wx = new WxUser();
        wx.setOpenId("123");
        wx.setNikeName("t1t");
        wx.setLevel(2);
        wxUserDAO.update(wx, new UpdateWrapper<WxUser>().lambda().eq(WxUser::getOpenId, wx.getOpenId()));
        System.out.println(1);
    }


    @Test
    public void test() {
//        Signup s = signupMapper.selectById(1);
//        System.out.println(1);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date dd = new Date();
        dd.setTime(1534927740808L);
        System.out.println(sdf.format(dd));
    }


}
