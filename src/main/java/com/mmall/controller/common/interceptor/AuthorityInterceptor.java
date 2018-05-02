package com.mmall.controller.common.interceptor;


import com.google.common.collect.Maps;
import com.mmall.common.Const;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.User;
import com.mmall.util.CookieUtil;
import com.mmall.util.JsonUtil;
import com.mmall.util.RedisShardedPoolUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;

@Slf4j
public class AuthorityInterceptor implements HandlerInterceptor {

    /**
     * 在请求处理之前执行，会在Controller 方法调用之前被调用;  该方法主要是用于准备资源数据的
     * @Param handler: handlerMethod类型对象，指定具体类下的方法
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        log.info("preHandle");

        HandlerMethod handlerMethod = (HandlerMethod) handler;

        String methodName = handlerMethod.getMethod().getName();
        String className = handlerMethod.getBean().getClass().getSimpleName();
        // 解析参数, key - value
        StringBuffer requestParamBuffer = new StringBuffer();
        Map paramMap = request.getParameterMap();
        Iterator iterator = paramMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry entry = (Map.Entry)iterator.next();
            String mapKey = (String)entry.getKey();

            String mapValue = StringUtils.EMPTY;

            // request这个参数的map，entry.getValue()返回String[]
            Object object = entry.getValue();
            if (object instanceof String[]) {
                String[] strs = (String[]) object;
                mapValue = Arrays.toString(strs);
            }
            requestParamBuffer.append(mapKey).append("=").append(mapValue);
        }

        // 除了使用exclude-mapping外， 在preHandle中手动处理
        if (StringUtils.equals(className, "UserManageController") && StringUtils.equals(methodName, "login")) {
            log.info("拦截无权限请求， className:{}, methodName:{}", className, methodName);
            return true; // 返回到登录方法中进行登录, 防止循环登录
        }

        log.info("拦截无权限请求， className:{}, methodName:{}， param:{}", className, methodName, requestParamBuffer);
        User user = null;
        String loginToken = CookieUtil.readLoginToken(request);
        if (StringUtils.isNotEmpty(loginToken)) {
            String userJsonStr = RedisShardedPoolUtil.get(loginToken);
            user = JsonUtil.stringToObject(userJsonStr, User.class);
        }

        // 重写response 使返回所需的json
        if (user == null || user.getRole().intValue() != Const.Role.ROLE_ADMIN) {
            // return false
            response.reset();   // 重置response, 否则出现异常
            response.setCharacterEncoding("UTF-8");
            response.setContentType("application/json;charset=UTF-8");

            PrintWriter out = response.getWriter();
            Map resultMap = Maps.newHashMap();
            if (user == null) {
                // 富文本上传权限验证处理, 需要返回指定格式，故单独处理
                if (StringUtils.equals(className, "ProductManageController") && StringUtils.equals(methodName, "richtextImgUpload")) {
                    log.info("拦截无权限请求， className:{}, methodName:{}", className, methodName);

                    resultMap.put("success", false);
                    resultMap.put("msg", "请登录管理员");
                    out.print(JsonUtil.objectToString(resultMap));
                }
                else
                    out.print(JsonUtil.objectToString(ServerResponse.createByErrorMessage("拦截器拦截, 用户未登录")));
            }
            else {
                // 登录的不是管理员
                if (StringUtils.equals(className, "ProductManageController") && StringUtils.equals(methodName, "richtextImgUpload")) {
                    log.info("拦截无权限请求， className:{}, methodName:{}", className, methodName);

                    resultMap.put("success",false);
                    resultMap.put("msg","无权限操作");
                    out.print(JsonUtil.objectToString(resultMap));
                }
                else out.print(JsonUtil.objectToString(ServerResponse.createByErrorMessage("拦截器拦截, 用户无权限操作")));
            }
            out.flush();
            out.close();

            return false;   // 不在进入Controller
        }


        return true;
    }

    /**
     * 该方法将在Controller执行之后，返回视图之前执行，ModelMap表示请求Controller处理之后返回的Model对象，所以可以在
     * 这个方法中修改ModelMap的属性，从而达到改变返回的模型的效果。
     */
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        log.info("postHandler");
    }

    /**
     * 该方法将在整个请求完成之后，也就是说在视图渲染之后进行调用，主要用于进行一些资源的释放
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        log.info("afterCompletion");
    }
}
