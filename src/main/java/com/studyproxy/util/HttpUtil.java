package com.studyproxy.util;

import okhttp3.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.Map;

@Component
public class HttpUtil {

    @Resource
    private OkHttpClient okHttpClient;

    public String get(String url) {
        return get(url, null, null);
    }

    public String get(String url, Map<String, String> headers) {
        return get(url, headers, null);
    }

    public String get(String url, Map<String, String> headers, CookieJar cookieJar) {
        Request.Builder builder = new Request.Builder().url(url).get();

        if (headers != null) {
            headers.forEach(builder::addHeader);
        }

        OkHttpClient client = cookieJar != null ? okHttpClient.newBuilder().cookieJar(cookieJar).build() : okHttpClient;

        try (Response response = client.newCall(builder.build()).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("请求失败: " + response.code());
            }
            ResponseBody body = response.body();
            return body != null ? body.string() : "";
        } catch (IOException e) {
            throw new RuntimeException("GET请求失败: " + e.getMessage(), e);
        }
    }

    public String post(String url, String body) {
        return post(url, body, null, null);
    }

    public String post(String url, String body, Map<String, String> headers) {
        return post(url, body, headers, null);
    }

    public String post(String url, String body, Map<String, String> headers, CookieJar cookieJar) {
        RequestBody requestBody = RequestBody.create(body, MediaType.parse("application/json; charset=utf-8"));
        Request.Builder builder = new Request.Builder().url(url).post(requestBody);

        if (headers != null) {
            headers.forEach(builder::addHeader);
        }

        OkHttpClient client = cookieJar != null ? okHttpClient.newBuilder().cookieJar(cookieJar).build() : okHttpClient;

        try (Response response = client.newCall(builder.build()).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("请求失败: " + response.code());
            }
            ResponseBody responseBody = response.body();
            return responseBody != null ? responseBody.string() : "";
        } catch (IOException e) {
            throw new RuntimeException("POST请求失败", e);
        }
    }

    public String postForm(String url, Map<String, String> formData) {
        return postForm(url, formData, null, null);
    }

    public String postForm(String url, Map<String, String> formData, Map<String, String> headers) {
        return postForm(url, formData, headers, null);
    }

    public String postForm(String url, Map<String, String> formData, Map<String, String> headers, CookieJar cookieJar) {
        FormBody.Builder formBuilder = new FormBody.Builder();
        if (formData != null) {
            formData.forEach((key, value) -> {
                if (key != null && value != null) {
                    formBuilder.add(key, value);
                }
            });
        }

        Request.Builder builder = new Request.Builder().url(url).post(formBuilder.build());

        if (headers != null) {
            headers.forEach(builder::addHeader);
        }

        OkHttpClient client = cookieJar != null ? okHttpClient.newBuilder().cookieJar(cookieJar).build() : okHttpClient;

        try (Response response = client.newCall(builder.build()).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("请求失败: " + response.code());
            }
            ResponseBody responseBody = response.body();
            return responseBody != null ? responseBody.string() : "";
        } catch (IOException e) {
            throw new RuntimeException("POST表单请求失败", e);
        }
    }

    public String postMultipart(String url, Map<String, String> formData, Map<String, String> headers) {
        return postMultipart(url, formData, headers, null);
    }

    public String postMultipart(String url, Map<String, String> formData, Map<String, String> headers, CookieJar cookieJar) {
        MultipartBody.Builder multipartBuilder = new MultipartBody.Builder().setType(MultipartBody.FORM);

        if (formData != null) {
            formData.forEach((key, value) -> {
                if (key != null && value != null) {
                    multipartBuilder.addFormDataPart(key, value);
                }
            });
        }

        Request.Builder builder = new Request.Builder().url(url).post(multipartBuilder.build());

        if (headers != null) {
            headers.forEach(builder::addHeader);
        }

        OkHttpClient client = cookieJar != null ? okHttpClient.newBuilder().cookieJar(cookieJar).build() : okHttpClient;

        try (Response response = client.newCall(builder.build()).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("请求失败: " + response.code());
            }
            ResponseBody responseBody = response.body();
            return responseBody != null ? responseBody.string() : "";
        } catch (IOException e) {
            throw new RuntimeException("POST多部分请求失败", e);
        }
    }

    public Document parseHtml(String html) {
        return Jsoup.parse(html);
    }

    public Document getHtml(String url) {
        String html = get(url);
        return parseHtml(html);
    }

    public String extractText(String html, String cssQuery) {
        Document doc = parseHtml(html);
        Elements elements = doc.select(cssQuery);
        return elements.text();
    }

    public String extractAttr(String html, String cssQuery, String attr) {
        Document doc = parseHtml(html);
        Elements elements = doc.select(cssQuery);
        return elements.attr(attr);
    }

    public String postJson(String url, String json, Map<String, String> headers) {
        return post(url, json, headers);
    }

    public byte[] getBytes(String url, Map<String, String> headers) {
        return getBytes(url, headers, null);
    }

    public byte[] getBytes(String url, Map<String, String> headers, CookieJar cookieJar) {
        Request.Builder builder = new Request.Builder().url(url).get();

        if (headers != null) {
            headers.forEach(builder::addHeader);
        }

        OkHttpClient client = cookieJar != null ? okHttpClient.newBuilder().cookieJar(cookieJar).build() : okHttpClient;

        try (Response response = client.newCall(builder.build()).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("请求失败: " + response.code());
            }
            ResponseBody body = response.body();
            return body != null ? body.bytes() : new byte[0];
        } catch (IOException e) {
            throw new RuntimeException("GET请求失败", e);
        }
    }
}
