package com.mmall.controller.common;

import com.mmall.common.Const;
import com.mmall.pojo.User;
import com.mmall.util.CookieUtil;
import com.mmall.util.JsonUtil;
import com.mmall.util.RedisShardedPoolUtil;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

public class SessionExpireFilter implements Filter {
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest)request;

        String loginToken = CookieUtil.readLoginToken(httpServletRequest);

        if (StringUtils.isNotBlank(loginToken)) {
            // 判断loginToken是否为空
            String userJsonStr = RedisShardedPoolUtil.get(loginToken);
            User user = JsonUtil.stringToObject(userJsonStr, User.class);
            if (user != null) {
                // user不为空，则重置session的时间，调用expire
                RedisShardedPoolUtil.expire(loginToken, Const.RedisCacheExTime.REDIS_SESSION_EXTIME);
            }
        }
        chain.doFilter(request, response);  // 传递下一个filter或者是最后一个
    }

    @Override
    public void destroy() {

    }
}
