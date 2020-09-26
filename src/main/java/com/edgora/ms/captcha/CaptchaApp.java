package com.edgora.ms.captcha;

import com.blade.Blade;
public class CaptchaApp {
    public static void main(String[] args) {
        Blade.of().get("/", ctx -> ctx.text("Hello Blade")).start();
    }
}
