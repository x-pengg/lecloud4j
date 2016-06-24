package me.ridog.lecloud4j.test;

import com.google.common.collect.Maps;
import me.ridog.lecloud4j.LeHttpClient;
import org.junit.Test;

import java.util.HashMap;

/**
 * @author: Tate
 * @date: 2016/6/17 14:10
 */
public class CreateActivity {

    @Test
    public void testCreate() throws Exception {

        LeHttpClient leHttpClient = new LeHttpClient();
        HashMap<String, Object> params = Maps.newHashMap();
        params.put("activityName", "111测试活动111");
        params.put("startTime", "20160617104655");
        params.put("endTime", "20160623234655");
        params.put("liveNum", 1);
        params.put("codeRateTypes", "1080,99");
        params.put("needRecord", 0);
        params.put("needTimeShift", 0);
        params.put("needFullView", 0);
        params.put("activityCategory", "013");
        params.put("playMode", 0);

       leHttpClient.executePost("lecloud.cloudlive.activity.create", null, params);

    }

    @Test
    public void testName() throws Exception {
        HashMap<String, Object> params = Maps.newHashMap();
        params.put("activityId", "测试活动111");
        params.put("activityName", "测试活动111");
        params.put("activityStatus", "测试活动111");
        params.put("offSet", "测试活动111");
        params.put("fetchSize", "测试活动111");

        LeHttpClient leHttpClient = new LeHttpClient();
        leHttpClient.executeGet("lecloud.cloudlive.vrs.activity.vrsinfo.search", null,null);

    }
}
