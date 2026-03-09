package com.floyd.sdk.infrastructure.weixin.dto;

import java.util.HashMap;

public class TemplateMessageDTO {

    String touser = "os9OmvvuCI4rn691AQMT7wNVP6IA";

    String template_id = "fIF8KEE0MOrlv6CuAaVtzKMn4T_OTr3TJeOu8lWXGhc";

    // 日志 url, 用于跳转
    String url;

    HashMap<String, HashMap<String, String>> data = new HashMap<>();

    public TemplateMessageDTO(String touser, String template_id) {
        this.touser = touser;
        this.template_id = template_id;
    }

    public void put(String key, String value) {
        data.put(key, new HashMap<String, String>() {
            {
                put("value", value);
            }
        });
    }

    public static void put (HashMap<String, HashMap<String, String>> data, TemplateKey key, String value) {
        data.put(key.getCode(), new HashMap<String, String>() {
            {
                put("value", value);
            }
        });
    }

    public enum TemplateKey {
        REPO_NAME("repo_name", "项目名称"),
        BRANCH_NAME("branch_name", "分支名称"),
        COMMIT_AUTHOR("commit_author", "提交作者"),
        COMMIT_MESSAGE("commit_message", "提交信息");

        private String code;

        private String desc;

        TemplateKey(String code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getDesc() {
            return desc;
        }

        public void setDesc(String desc) {
            this.desc = desc;
        }
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
