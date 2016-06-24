package me.ridog.lecloud4j;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.SocketTimeoutException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author: Tate
 * @date: 2016/6/17 10:43
 */
public class LeHttpClient {

    private final String host = "http://api.open.letvcloud.com/live/execute";
    private final Integer userid = ;
    private final String secret = "";
    private final String ver = "3.1";

    private static final Logger _log = LoggerFactory.getLogger(LeHttpClient.class);

    public RemoteResultWrapper executePost(final String method,
                                           Map<String, String> headerMap, Map<String, Object> paramsMap) {
        return executePost(method, headerMap, paramsMap, null);
    }

    public RemoteResultWrapper executeGet(final String method,
                                          Map<String, String> headerMap, Map<String, Object> paramsMap) {
        return executeGet(method, headerMap, paramsMap, null);
    }


    public <T> RemoteResultWrapper<T> executePost(final String method, Map<String, String> headerMap,
                                                  Map<String, Object> paramsMap, TypeReference<T> typeReference) {

        if (Strings.isNullOrEmpty(method)) {
            throw new IllegalArgumentException("调用的 method 为空!");
        }

        _log.info("Begin invoke method:{},args:{}", method,
                JSON.toJSONString(paramsMap));

        DefaultHeaderMap defaultHeaderMap = new DefaultHeaderMap(method, ver, userid);

        String sign = generateSign(defaultHeaderMap, secret);
        defaultHeaderMap.put("sign", sign);
        paramsMap.putAll(defaultHeaderMap);

        RemoteResultWrapper<T> wrapper = new RemoteResultWrapper<T>();
        RemoteResultVo remoteResult = new RemoteResultVo();
        wrapper.setRemoteResult(remoteResult);
        HttpPost post = new HttpPost(host);

        // 设置默认头
        post.addHeader("Content-Type", "application/x-www-form-urlencoded;charset=utf-8");



        // 设置 header
        if (null != headerMap) {
            headerMap.forEach((k, v) -> post.addHeader(k, v.toString()));
        }

        // 设置参数

        List<BasicNameValuePair> data = Lists.newArrayList();
        paramsMap.forEach((k, v) -> data.add(new BasicNameValuePair(k, v.toString())));

        try {
            post.setEntity(new UrlEncodedFormEntity(data, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        /*ByteArrayEntity byteArrayEntity = new ByteArrayEntity(JSON.toJSONString(paramsMap).getBytes());
        List<BasicNameValuePair> data = new ArrayList<BasicNameValuePair>();
        paramsMap.forEach((k,v)-> data.add(new BasicNameValuePair(k, v+"")));
        try {
            post.setEntity(new UrlEncodedFormEntity(data, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }*/

        try {
            long start = System.currentTimeMillis();
            HttpClient client = HttpClients.createDefault();
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


    public <T> RemoteResultWrapper<T> executeGet(final String method, Map<String, String> headerMap,
                                                 Map<String, Object> paramsMap, TypeReference<T> typeReference) {
        if (Strings.isNullOrEmpty(method)) {
            throw new IllegalArgumentException("调用的 method 为空!");
        }

        if (null==paramsMap) {
            paramsMap = Maps.newHashMap();
        }

        _log.info("Begin invoke method:{},args:{}", method,
                JSON.toJSONString(paramsMap));

        DefaultHeaderMap defaultHeaderMap = new DefaultHeaderMap(method, ver, userid);
        String sign = generateSign(defaultHeaderMap, secret);
        defaultHeaderMap.put("sign", sign);

        paramsMap.putAll(defaultHeaderMap);



        RemoteResultWrapper<T> wrapper = new RemoteResultWrapper<T>();
        RemoteResultVo remoteResult = new RemoteResultVo();
        wrapper.setRemoteResult(remoteResult);


        // 设置参数
        StringBuffer uri = new StringBuffer(host);
        uri.append("?");
        paramsMap.forEach((k, v) -> {
            uri.append(k);
            uri.append("=");
            try {
                uri.append(URLEncoder.encode(v.toString(), "UTF-8"));
            } catch (Exception e) {
                e.printStackTrace();
            }
            uri.append("&");
        });

        HttpGet get = new HttpGet(uri.toString());
        // 设置 header
        if (null != headerMap) {
            headerMap.forEach((k, v) -> get.addHeader(k, v.toString()));
        }

        try {
            long start = System.currentTimeMillis();
            HttpClient client = HttpClients.createDefault();
            HttpResponse response = client.execute(get);
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
            _log.info("Get cost={}ms, rsCode={}, method={}, params={}, rs:{}", cost, wrapper.getRemoteResult().getRs(),
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
            get.releaseConnection();
        }
    }

    private String generateSign(Map<String, Object> params, String secret) {

        StringBuilder result = new StringBuilder();
        List<String> keys = new ArrayList<String>(params.keySet());
        Collections.sort(keys);

        keys.forEach((item) -> {
            if (!"sign".equalsIgnoreCase(item)) {
                result.append(item).append(params.get(item));
            }
        });

        result.append(secret);
        _log.debug("sign:{}", result.toString());
        return MD5.encrypt(result.toString());

    }
}

