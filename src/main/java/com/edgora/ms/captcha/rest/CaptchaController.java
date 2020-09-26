package com.edgora.ms.captcha.rest;

import java.awt.FontFormatException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.blade.kit.UUID;
import com.blade.mvc.RouteContext;
import com.blade.mvc.annotation.GetRoute;
import com.blade.mvc.annotation.JSON;
import com.blade.mvc.annotation.Path;
import com.blade.mvc.annotation.PathParam;
import com.edgora.ms.captcha.utils.Config;
import com.edgora.ms.captcha.utils.CustomCaptcha;
import com.edgora.ms.captcha.utils.RedisUtil;
import com.wf.captcha.SpecCaptcha;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;

@Path
public class CaptchaController {
    private RedisUtil redisUtil;
    private static Logger LOG = LoggerFactory.getLogger(CaptchaController.class);
    private Long defaultTime = Config.getValue("captcha.lifetime", 120L);
    private int digits = Config.getValue("captcha.digits", 6);
    private int width = Config.getValue("captcha.width", 120);
    private int height = Config.getValue("captcha.height", 40);
    private int charType = Config.getValue("captcha.charType", 6);
    private int font = Config.getValue("captcha.font", 0);
    
    @GetRoute("/api/test")
    public void test(RouteContext ctx) throws IOException, FontFormatException {
        CustomCaptcha specCaptcha = new CustomCaptcha(width, height,digits);
        // 设置字体
        specCaptcha.setFont(font); // 有默认字体，可以不用设置
        specCaptcha.setCharType(charType);
        ctx.html("<img alt='" + specCaptcha.text() + "' src='" + specCaptcha.toBase64() + "' />");
    }

    @GetRoute("/api/create/:id/:value/:time")
    @JSON
    public Map<String, String> create(@PathParam String id,@PathParam String value, @PathParam Long time)  throws IOException, FontFormatException {
        // 三个参数分别为宽、高、位数
        SpecCaptcha specCaptcha = new SpecCaptcha(width, 48, digits);
        // 设置字体
        specCaptcha.setFont(font); // 有默认字体，可以不用设置
        // 设置类型，纯数字、纯字母、字母数字混合
        specCaptcha.setCharType(charType);
        String text = value;
        if(text==null){
            text = specCaptcha.text();
        }
        String key = id;
        StatefulRedisConnection conn = redisUtil.getConnection();
        RedisCommands<String, String> cmds = conn.sync();
        cmds.setex(key, time, text);
        LOG.info("create captcha {}={} with life time {}", key, text, time);
        Map<String, String> data = new HashMap<>();
        data.put("key", id);
        data.put("image", specCaptcha.toBase64());
        data.put("lifetime", time + "");
        data.put("value", text);
        return data;
    }

    @GetRoute("/api/create/:id/:value")
    @JSON
    public Map<String, String> create(@PathParam String id,@PathParam String value) throws IOException, FontFormatException {
        return create(id, value,defaultTime);
    }

    @GetRoute("/api/create/:id")
    @JSON
    public Map<String, String> create(@PathParam String id) throws IOException, FontFormatException {
        return create(id,null);
    }

    @GetRoute("/api/create")
    @JSON
    public Map<String, String> create() throws IOException, FontFormatException {
        return create(UUID.UU16(),null);
    }

    @GetRoute("/api/generate/:value")
    @JSON
    public Map<String, String> generate(@PathParam String value) throws IOException, FontFormatException {
        return create(UUID.UU16(),value);
    }
    @GetRoute("/api/generate/:value/:time")
    @JSON
    public Map<String, String> generate(@PathParam String value, @PathParam Long time) throws IOException, FontFormatException {
        return create(UUID.UU16(),value,time);
    }

    @GetRoute("/api/verify/:id/:value")
    @JSON
    public Map<String, Object> verify(@PathParam String id, @PathParam String value) {
        StatefulRedisConnection conn = redisUtil.getConnection();
        RedisCommands<String, String> cmds = conn.sync();
        boolean result = value.equalsIgnoreCase(cmds.get(id));
        if(result){
            cmds.del(id);
        }
        LOG.info("verify captcha {}={} with result {}", id, value, result);
        Map<String, Object> data = new HashMap<>();
        data.put("success", result);
        return data;
    }
}
