package me.ridog.lecloud4j;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.google.common.base.Strings;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author: Tate
 * @date: 2016/6/17 10:43
 */
public class LeHttpClient {

    private static final String host = "http://api.open.letvcloud.com/live/execute";
    private static final Integer userid = 826763;
    private static final String secret = "df116ce79e6509b44f267d75361305d1";
    private static final String ver = "3.1";

    private HttpClient client = HttpClients.createDefault();

    private static final Logger _log = LoggerFactory.getLogger(LeHttpClient.class);

    public RemoteResultWrapper executePost(final String method,
                                           Map<String, String> headerMap, Map<String, Object> paramsMap) {
        return executePost(client, method, headerMap, paramsMap, null);
    }

    public <T> RemoteResultWrapper<T> executePost(final String method,
                                                  Map<String, String> headerMap, Map<String, Object> paramsMap,
                                                  TypeReference<T> typeReference) {
        return executePost(client, method, headerMap, paramsMap, typeReference);
    }

    public <T> RemoteResultWrapper<T> executePost(HttpClient client,
                                                  final String method, Map<String, String> headerMap,
                                                  Map<String, Object> paramsMap, TypeReference<T> typeReference) {

        if (Strings.isNullOrEmpty(method)) {
            throw new IllegalArgumentException("调用的 method 为空!");
        }

        _log.info("Begin invoke method:{},args:{}", method,
                JSON.toJSONString(paramsMap));

        DefaultHeaderMap defaultHeaderMap = new DefaultHeaderMap(method, ver, userid);

        RemoteResultWrapper<T> wrapper = new RemoteResultWrapper<T>();
        RemoteResultVo remoteResult = new RemoteResultVo();
        wrapper.setRemoteResult(remoteResult);
        HttpPost post = new HttpPost(host);
        setDefaultHeadForMethod(post, defaultHeaderMap);
        if (headerMap != null && !headerMap.isEmpty()) {
            headerMap.forEach((k, v) -> post.addHeader(k, v + ""));
        }

        // 设置默认头
        post.addHeader("Content-Type", "application/x-www-form-urlencoded;charset=utf-8");

        // 设置参数
        ByteArrayEntity byteArrayEntity = new ByteArrayEntity(JSON.toJSONString(paramsMap).getBytes());
        post.setEntity(byteArrayEntity);

      /*  List<BasicNameValuePair> data = new ArrayList<BasicNameValuePair>();
        paramsMap.forEach((k,v)-> data.add(new BasicNameValuePair(k, v+"")));
        try {
            post.setEntity(new UrlEncodedFormEntity(data, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }*/

        try {
            long start = System.currentTimeMillis();
            HttpResponse response = client.execute(post);
            remoteResult.setRs(response.getStatusLine().getStatusCode());
            HttpEntity entity = response.getEntity();
            String content = EntityUtils.toString(entity, "UTF-8");
            if (!Strings.isNullOrEmpty(content)) {
                remoteResult.setJsonObj(content);
                if (typeReference != null) {
                    try {
                        wrapper.setObj(JSON.parseObject(content, typeReference));
                    } catch (Exception e) {
                        _log.error("{} 转化为{}不正确!", content, typeReference.getType().toString());
                        wrapper.setObj(null);
                    }
                }
            }
            long cost = System.currentTimeMillis() - start;
            _log.info("Post cost={}ms, rsCode={}, method={}, params={}, rs:{}", cost, wrapper.getRemoteResult().getRs(),
                    method, JSON.toJSONString(paramsMap), wrapper.getRemoteResult().getJsonObj());
            return wrapper;
        } catch (HttpHostConnectException e) {
            throw new RuntimeException("主机连接超时!", e);
        } catch (SocketTimeoutException e) {
            throw new RuntimeException("接口超时!", e);
        } catch (IOException e) {
            _log.error("error", e);
            throw new RuntimeException(e);
        } finally {
            post.releaseConnection();
        }
    }


    private static void setDefaultHeadForMethod(HttpPost post, DefaultHeaderMap defaultHeaderMap) {

        String sign = generateSign(defaultHeaderMap, secret);
        defaultHeaderMap.put("sign", sign);
        defaultHeaderMap.forEach((k, v) -> post.addHeader(k, v + ""));

    }

    private static String generateSign(Map<String, Object> params, String secret) {

        StringBuilder result = new StringBuilder();
        List<String> keys = new ArrayList<String>(params.keySet());
        Collections.sort(keys);

        keys.forEach((item) -> result.append(item).append(params.get(item)));

        result.append(secret);
        _log.debug("sign:{}", result.toString());
        return MD5.encrypt(result.toString());

    }
}

