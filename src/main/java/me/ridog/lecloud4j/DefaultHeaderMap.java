package me.ridog.lecloud4j;

import java.util.HashMap;

/**
 * @author: Tate
 * @date: 2016/6/17 10:17
 */
public class DefaultHeaderMap extends HashMap<String, Object> {

    public DefaultHeaderMap(String method, String ver, Integer userid) {
        this.put("method", method);
        this.put("ver", ver);
        this.put("userid", userid);
        this.put("timestamp", String.valueOf(System.currentTimeMillis()));
    }

}
