package com.frank.dto;

/**
 * Created by frank on 17/5/31.
 */
public class BaseJsonResult {
    private String uptoken;

    public BaseJsonResult(String uptoken) {
        this.uptoken = uptoken;
    }

    public String getUptoken() {
        return uptoken;
    }

    public void setUptoken(String uptoken) {
        this.uptoken = uptoken;
    }

    @Override
    public String toString() {
        return "BaseJsonResult{" +
                "uptoken='" + uptoken + '\'' +
                '}';
    }
}
