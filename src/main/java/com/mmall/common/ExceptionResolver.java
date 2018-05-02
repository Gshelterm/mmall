package com.mmall.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.json.MappingJacksonJsonView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Slf4j
@Component  // 需要将bean注入到容器当中
public class ExceptionResolver implements HandlerExceptionResolver {

    @Override
    public ModelAndView resolveException(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        log.error("{} Exception", request.getRequestURI(), ex); // request.getRequestURI() 发生异常 ex
        // 需要返回json, 将modelAndView转化为json view， 不需要返回页面
        ModelAndView modelAndView = new ModelAndView(new MappingJacksonJsonView());
        modelAndView.addObject("status", ResponseCode.ERROR.getCode());
        modelAndView.addObject("msg", "接口异常，详情查看日志");
        modelAndView.addObject("data", ex.toString());
        return modelAndView;
    }
}
