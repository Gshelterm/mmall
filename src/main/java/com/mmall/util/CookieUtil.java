package com.mmall.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Slf4j
public class CookieUtil {
    public  final static String COOKIE_DOMAIN = "gimooc.com";  // 写在一级域名下 // todo 修改
    public  final static String COOKIE_NAME = "mmall_login_token";

    public static void writeLoginToken(HttpServletResponse  response, String token) {
        Cookie cookie = new Cookie(COOKIE_NAME, token);
        cookie.setDomain(COOKIE_DOMAIN);
        cookie.setPath("/");    // 设置在根目录，所有页面可获取
        cookie.setMaxAge(60 * 60 * 24 * 180);     // 单位秒； -1：永久； 不设置，cookie不会写入硬盘，而写入内存，只在当前页面有效
        log.info("write cookieName:{}, cookieValue:{}", cookie.getName(), cookie.getValue());
        response.addCookie(cookie);
    }

    public static String readLoginToken(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                log.info("read CookieName:{},CookieValue:{}", cookie.getName(), cookie.getValue());
                if (StringUtils.equals(cookie.getName(), COOKIE_NAME)) { // 空判断
                    log.info("return cookieName:{}, cookieValue:{}", cookie.getName(), cookie.getValue());
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    // 注销登陆
    public static void deleteLoginToken(HttpServletRequest request, HttpServletResponse response) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                cookie.setDomain(COOKIE_DOMAIN);
                cookie.setPath("/");
                cookie.setMaxAge(0); // 设置为0， 代表删除cookie
                log.info("delete CookieName:{},CookieValue:{}", cookie.getName(), cookie.getValue());
                response.addCookie(cookie);
                return;
            }
        }
    }
}
