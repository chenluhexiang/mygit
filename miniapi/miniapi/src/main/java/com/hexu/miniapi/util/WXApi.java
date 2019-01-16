package com.hexu.miniapi.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;


/**
 * <p>Copyright: All Rights Reserved</p>
 * <p>Company: 指点无限(北京)科技有限公司   http://www.zhidianwuxian.cn</p>
 * <p>Description:  </p>
 * <p>Author:hexu/方和煦, 18-6-15</p>
 */
public class WXApi {

    private static final String BASE_URL = "https://api.weixin.qq.com/sns/";

    private static Logger logger = LoggerFactory.getLogger(WXApi.class);

    private static final String appid = "wxca8726cf8cb40eec";
    private static final String secret = "e6a2c1e8692e6c6cfa74931b44cb60e6";

    public static WXUserInfo getUserInfoForH5(String code) {
        logger.info("开始访问微信接口");
        WXUserInfo user = new WXUserInfo();
        String responseText = null;
        String url = BASE_URL + "jscode2session?appid=" + appid + "&secret=" + secret + "&js_code=" + code + "&grant_type=authorization_code";
        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpGet httppost = new HttpGet(url);
        CloseableHttpResponse response = null;
        try {
            logger.info(url);
            response = httpclient.execute(httppost);
            logger.info(response.toString());
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                responseText = EntityUtils.toString(entity, "UTF-8");
                logger.info(responseText);
                JSONObject object = JSON.parseObject(responseText);
                //openid  session_key
                user.setSessionKey(object.get("session_key") == null ? null : object.get("session_key").toString());
                user.setAccessToken(object.get("access_token") == null ? null : object.get("access_token").toString());
                user.setOpenId(object.get("openid") == null ? null : object.get("openid").toString());
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                httpclient.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return user;
    }

}
