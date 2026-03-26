package com.studyproxy.util;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

@Slf4j
public class HtmlUtil {

    public static Document parseHtml(String html) {
        return Jsoup.parse(html);
    }

    public static String extractText(String html, String cssQuery) {
        Document doc = parseHtml(html);
        Elements elements = doc.select(cssQuery);
        return elements.text();
    }

    public static String extractAttr(String html, String cssQuery, String attr) {
        Document doc = parseHtml(html);
        Elements elements = doc.select(cssQuery);
        return elements.attr(attr);
    }

    public static Elements selectElements(String html, String cssQuery) {
        Document doc = parseHtml(html);
        return doc.select(cssQuery);
    }

    public static Element selectFirst(String html, String cssQuery) {
        Document doc = parseHtml(html);
        return doc.selectFirst(cssQuery);
    }

    public static String cleanHtml(String html) {
        Document doc = parseHtml(html);
        return doc.text();
    }

    public static boolean containsText(String html, String text) {
        return html.contains(text);
    }

    public static boolean containsElement(String html, String cssQuery) {
        Document doc = parseHtml(html);
        return !doc.select(cssQuery).isEmpty();
    }
}