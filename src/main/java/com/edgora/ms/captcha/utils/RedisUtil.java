package com.edgora.ms.captcha.utils;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;

public class RedisUtil {
    public static final Map<String,StatefulRedisConnection> CONNS = new HashMap<>();
    public static final Map<String,RedisClient> CLIENTS = new HashMap<>();

    private static StatefulRedisConnection conn = null;
    private static final Logger log = LoggerFactory.getLogger(RedisUtil.class);

    public static StatefulRedisConnection getConnection(){
        if(conn==null){
            RedisClient client =  RedisClient.create(RedisURI.create(Config.getValue("redis.url", "redis://127.0.0.1:6379")));
            conn = client.connect();
        }
        return conn;
    }
}
