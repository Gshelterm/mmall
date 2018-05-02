package com.mmall.controller.backend;

import com.github.pagehelper.PageInfo;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.User;
import com.mmall.service.IOrderService;
import com.mmall.service.IUserService;
import com.mmall.util.CookieUtil;
import com.mmall.util.JsonUtil;
import com.mmall.util.RedisShardedPoolUtil;
import com.mmall.vo.OrderVo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/manage/order")
public class OrderManageController {

    @Autowired
    private IUserService userService;

    @Autowired
    private IOrderService orderService;

    @RequestMapping("list.do")
    @ResponseBody
    public ServerResponse<PageInfo> orderList(HttpServletRequest httpServletRequest, @RequestParam(value = "pageNum", defaultValue = "1")int pageNum,
                                              @RequestParam(value = "pageSize", defaultValue = "10")int pageSize) {
//        String loginToken = CookieUtil.readLoginToken(httpServletRequest);
//        if (StringUtils.isBlank(loginToken)) {
//            return ServerResponse.createByErrorMessage("用户未登录， 无法获取当前用户的信息");
//        }
//        String userJsonStr = RedisShardedPoolUtil.get(loginToken);
//        User user = JsonUtil.stringToObject(userJsonStr, User.class);
//        if(user == null){
//            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),
//                    "用户未登录,请登录管理员");
//        }
//        if(userService.checkAdminRole(user).isSuccess()){
//            return orderService.manageList(pageNum, pageSize);
//        }else{
//            return ServerResponse.createByErrorMessage("无权限操作");
//        }
        // 添加拦截器以后
        return orderService.manageList(pageNum, pageSize);
    }

    @RequestMapping("detail.do")
    @ResponseBody
    public ServerResponse<OrderVo> detail(HttpServletRequest httpServletRequest, Long orderNo) {
//        String loginToken = CookieUtil.readLoginToken(httpServletRequest);
//        if (StringUtils.isBlank(loginToken)) {
//            return ServerResponse.createByErrorMessage("用户未登录， 无法获取当前用户的信息");
//        }
//        String userJsonStr = RedisShardedPoolUtil.get(loginToken);
//        User user = JsonUtil.stringToObject(userJsonStr, User.class);
//
//        if(user == null){
//            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),
//                    "用户未登录,请登录管理员");
//        }
//        if(userService.checkAdminRole(user).isSuccess()){
//            return orderService.manageDetail(orderNo);
//        }else{
//            return ServerResponse.createByErrorMessage("无权限操作");
//        }

        return orderService.manageDetail(orderNo);
    }

    @RequestMapping("search.do")
    @ResponseBody
    public ServerResponse<OrderVo> orderSearch(HttpServletRequest httpServletRequest, Long orderNo,
                                               @RequestParam(value = "pageNum",defaultValue = "1") int pageNum,
                                               @RequestParam(value = "pageSize",defaultValue = "10")int pageSize) {
//        String loginToken = CookieUtil.readLoginToken(httpServletRequest);
//        if (StringUtils.isBlank(loginToken)) {
//            return ServerResponse.createByErrorMessage("用户未登录， 无法获取当前用户的信息");
//        }
//        String userJsonStr = RedisShardedPoolUtil.get(loginToken);
//        User user = JsonUtil.stringToObject(userJsonStr, User.class);
//
//        if(user == null){
//            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),
//                    "用户未登录,请登录管理员");
//        }
//        if(userService.checkAdminRole(user).isSuccess()){
//            return orderService.manageSearch(orderNo,pageNum,pageSize);
//        }else{
//            return ServerResponse.createByErrorMessage("无权限操作");
//        }

        return orderService.manageSearch(orderNo,pageNum,pageSize);
    }

    @RequestMapping("send_goods.do")
    @ResponseBody
    public ServerResponse<String> orderSendGoods(HttpServletRequest httpServletRequest, Long orderNo){

//        String loginToken = CookieUtil.readLoginToken(httpServletRequest);
//        if (StringUtils.isBlank(loginToken)) {
//            return ServerResponse.createByErrorMessage("用户未登录， 无法获取当前用户的信息");
//        }
//        String userJsonStr = RedisShardedPoolUtil.get(loginToken);
//        User user = JsonUtil.stringToObject(userJsonStr, User.class);
//        if(user == null){
//            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"用户未登录,请登录管理员");
//
//        }
//        if(userService.checkAdminRole(user).isSuccess()){
//            return orderService.manageSendGoods(orderNo);
//        }else{
//            return ServerResponse.createByErrorMessage("无权限操作");
//        }

        return orderService.manageSendGoods(orderNo);
    }
}
