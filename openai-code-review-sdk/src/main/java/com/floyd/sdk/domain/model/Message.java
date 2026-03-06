package com.floyd.sdk.domain.model;

import java.util.HashMap;

public class Message {

    String touser = "os9OmvvuCI4rn691AQMT7wNVP6IA";

    String template_id = "fIF8KEE0MOrlv6CuAaVtzKMn4T_OTr3TJeOu8lWXGhc";

    // 日志 url, 用于跳转
    String url;

    HashMap<String, HashMap<String, String>> data = new HashMap<>();

    public void put(String key, String value) {
        data.put(key, new HashMap<String, String>() {
            {
                put("value", value);
            }
        });
    }

    public String getTouser() {
        return touser;
    }

    public void setTouser(String touser) {
        this.touser = touser;
    }

    public String getTemplate_id() {
        return template_id;
    }

    public void setTemplate_id(String template_id) {
        this.template_id = template_id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public HashMap<String, HashMap<String, String>> getData() {
        return data;
    }

    public void setData(HashMap<String, HashMap<String, String>> data) {
        this.data = data;
    }

}
