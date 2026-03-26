package com.studyproxy.entity.yinghua;

import com.studyproxy.util.InMemoryCookieJar;
import lombok.Data;

@Data
public class YinghuaUserCache {

    private String preUrl;

    private String account;

    private String password;

    private Boolean ipProxySw;

    private String proxyIp;

    private String verCode;

    private String cookie;

    private InMemoryCookieJar cookieJar;

    private String token;

    private String sign;

    public YinghuaUserCache() {
        this.cookieJar = new InMemoryCookieJar();
    }
}
