package com.studyproxy.util;

import lombok.extern.slf4j.Slf4j;
import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class InMemoryCookieJar implements CookieJar {

    private final Map<String, List<Cookie>> cookieStore = new HashMap<>();

    @Override
    public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
        for (Cookie cookie : cookies) {
            String domain = cookie.domain();
            if (domain == null || domain.isEmpty()) {
                domain = url.host();
            }
            
            domain = normalizeDomain(domain);
            
            List<Cookie> existingCookies = cookieStore.computeIfAbsent(domain, k -> new ArrayList<>());
            
            boolean found = false;
            for (int i = 0; i < existingCookies.size(); i++) {
                Cookie existingCookie = existingCookies.get(i);
                if (existingCookie.name().equals(cookie.name())) {
                    existingCookies.set(i, cookie);
                    found = true;
                    break;
                }
            }
            if (!found) {
                existingCookies.add(cookie);
            }
        }
    }

    @Override
    public List<Cookie> loadForRequest(HttpUrl url) {
        List<Cookie> cookies = new ArrayList<>();
        String host = url.host();
        
        for (Map.Entry<String, List<Cookie>> entry : cookieStore.entrySet()) {
            String cookieDomain = entry.getKey();
            if (matchesDomain(host, cookieDomain)) {
                for (Cookie cookie : entry.getValue()) {
                    if (cookie.expiresAt() >= System.currentTimeMillis()) {
                        cookies.add(cookie);
                    }
                }
            }
        }
        
        return cookies;
    }

    private String normalizeDomain(String domain) {
        if (domain.startsWith(".")) {
            return domain.substring(1);
        }
        return domain;
    }

    private boolean matchesDomain(String host, String cookieDomain) {
        String normalizedCookieDomain = normalizeDomain(cookieDomain);
        
        if (host.equals(normalizedCookieDomain)) {
            return true;
        }
        
        if (host.endsWith("." + normalizedCookieDomain)) {
            return true;
        }
        
        return false;
    }

    public String getCookieString(String host) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, List<Cookie>> entry : cookieStore.entrySet()) {
            if (matchesDomain(host, entry.getKey())) {
                for (Cookie cookie : entry.getValue()) {
                    sb.append(cookie.name()).append("=").append(cookie.value()).append("; ");
                }
            }
        }
        return sb.toString();
    }

    public Cookie getCookie(String host, String name) {
        for (Map.Entry<String, List<Cookie>> entry : cookieStore.entrySet()) {
            if (matchesDomain(host, entry.getKey())) {
                for (Cookie cookie : entry.getValue()) {
                    if (cookie.name().equals(name)) {
                        return cookie;
                    }
                }
            }
        }
        return null;
    }

    public void clearCookies(String host) {
        cookieStore.remove(host);
    }

    public void clearAllCookies() {
        cookieStore.clear();
    }

    public String getAllCookieString() {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, List<Cookie>> entry : cookieStore.entrySet()) {
            for (Cookie cookie : entry.getValue()) {
                if (cookie.expiresAt() >= System.currentTimeMillis()) {
                    sb.append(cookie.name()).append("=").append(cookie.value()).append("; ");
                }
            }
        }
        return sb.toString();
    }
}
