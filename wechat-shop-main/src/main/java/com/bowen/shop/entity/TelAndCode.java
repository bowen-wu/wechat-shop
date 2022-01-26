package com.bowen.shop.entity;

public class TelAndCode extends Tel {
    private String code;

    public TelAndCode(String tel, String code) {
        super(tel);
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
