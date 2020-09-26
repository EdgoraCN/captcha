package com.edgora.ms.captcha.utils;

import com.wf.captcha.SpecCaptcha;

public class CustomCaptcha extends SpecCaptcha {
    public CustomCaptcha(int width, int height, int digits) {
        super(width,height,digits);
	}

	public void setText(String text) {
        super.chars = text;
    }
}
