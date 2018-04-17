package com.mmall.controller.portal;

import com.mmall.common.Const;
import com.mmall.common.RedisPool;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.User;
import com.mmall.service.IUserService;
import com.mmall.util.CookieUtil;
import com.mmall.util.JsonUtil;
import com.mmall.util.RedisPoolUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * User controller
 */
@Controller
@RequestMapping("/user/")
public class UserController {

    @Autowired
    private IUserService userService;

    /**
     * 用户登录
     */
    @RequestMapping(value = "login.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<User> login(String username, String password, HttpSession session,
                                      HttpServletResponse httpServletResponse) {
        ServerResponse<User> serverResponse = userService.login(username, password);
        if (serverResponse.isSuccess()) {

            CookieUtil.writeLoginToken(httpServletResponse, session.getId());   // 写向浏览器的cookie

            // session.getId() :CAA02D1EF6F64A0B2B409D7904E6A621 ==》 登录后浏览器请求JessionId/cookie：CAA02D1EF6F64A0B2B409D7904E6A621
            // Redis缓存登录用户信息
            RedisPoolUtil.setex(session.getId(), JsonUtil.objectToString(serverResponse.getData()),
                    Const.RedisCacheExTime.REDIS_SESSION_EXTIME);

            // session.setAttribute(Const.CURRENT_USER, serverResponse.getData());
        }
        return serverResponse;
    }

    @RequestMapping(value = "logout.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> logout(HttpServletRequest httpServletRequest,
                                         HttpServletResponse httpServletResponse) {
        String loginToken = CookieUtil.readLoginToken(httpServletRequest);
        CookieUtil.deleteLoginToken(httpServletRequest, httpServletResponse);   // 删除浏览器端的cookie
        RedisPoolUtil.del(loginToken);  // 删除redis里的token

        // session.removeAttribute(Const.CURRENT_USER); v1.0
        return ServerResponse.createBySuccess();
    }

    @RequestMapping(value = "register", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> register(User user) {
        return userService.register(user);
    }

    // 再做一步效验
    @RequestMapping(value = "check_valid.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> checkValid(String str, String type) {
        return userService.checkValid(str, type);
    }

    @RequestMapping(value = "get_user_info.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<User> getUserInfo(HttpServletRequest httpServletRequest) {
        // User user = (User) session.getAttribute(Const.CURRENT_USER); v1.0 从session获取用户
        String loginToken = CookieUtil.readLoginToken(httpServletRequest);
        if (StringUtils.isBlank(loginToken)) {
            return ServerResponse.createByErrorMessage("用户未登录， 无法获取当前用户的信息");
        }
        String userJsonStr = RedisPoolUtil.get(loginToken);
        User user = JsonUtil.stringToObject(userJsonStr, User.class);

        if (user != null) {
            return ServerResponse.createBySuccess(user);
        }

        return ServerResponse.createByErrorMessage("用户未登录，无法获取当前用户的信息");
    }

    @RequestMapping(value = "forget_get_question.do", method = RequestMethod.POST)
    @ResponseBody
    // 忘记密码
    public ServerResponse<String> forgetGetQuestion(String username) {
        return userService.selectQuestion(username);
    }

    @RequestMapping(value = "forget_check_answer.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> forgetCheckAnswer(String username, String question, String answer) { // username拼错会接收不到参数，成为null
        return userService.checkAnswer(username, question, answer);
    }

    @RequestMapping(value = "forget_reset_password.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> forgetResetPassword(String username, String passwordNew, String forgetToken) {
        return userService.forgetResetPassword(username, passwordNew, forgetToken);
    }

    // 登录状态更改密码
    @RequestMapping(value = "reset_password.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> resetPassword(HttpServletRequest httpServletRequest, String passwordOld, String passwordNew) {
        String loginToken = CookieUtil.readLoginToken(httpServletRequest);
        if (StringUtils.isBlank(loginToken)) {
            return ServerResponse.createByErrorMessage("用户未登录， 无法获取当前用户的信息");
        }
        String userJsonStr = RedisPoolUtil.get(loginToken);
        User user = JsonUtil.stringToObject(userJsonStr, User.class);
        if (user == null) {
            return ServerResponse.createByErrorMessage("用户未登录");
        }

        return userService.resetPassword(passwordOld, passwordNew, user);
    }

    @RequestMapping(value = "update_information.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<User> updateInformation(HttpServletRequest httpServletRequest, User user) {
        String loginToken = CookieUtil.readLoginToken(httpServletRequest);
        if (StringUtils.isBlank(loginToken)) {
            return ServerResponse.createByErrorMessage("用户未登录， 无法获取当前用户的信息");
        }
        String userJsonStr = RedisPoolUtil.get(loginToken);
        User currentUser = JsonUtil.stringToObject(userJsonStr, User.class);

        if (currentUser == null) {
            return ServerResponse.createByErrorMessage("用户未登录");
        }
        user.setId(currentUser.getId());    // 前端传过来的user对象包含更新信息，可能没有userId； 防止越权，设置原有的user id
        user.setUsername(currentUser.getUsername());                 // 都不能被更新
        ServerResponse<User> response = userService.updateInformation(user);
        if (response.isSuccess()) {
            response.getData().setUsername(currentUser.getUsername());      //  有必要吗
            RedisPoolUtil.setex(loginToken, JsonUtil.objectToString(response.getData()),
                    Const.RedisCacheExTime.REDIS_SESSION_EXTIME);
        }

        return response;
    }

    @RequestMapping(value = "get_information.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<User> getInformation(HttpServletRequest httpServletRequest) {
        String loginToken = CookieUtil.readLoginToken(httpServletRequest);
        if (StringUtils.isBlank(loginToken)) {
            return ServerResponse.createByErrorMessage("用户未登录， 无法获取当前用户的信息");
        }
        String userJsonStr = RedisPoolUtil.get(loginToken);
        User currentUser = JsonUtil.stringToObject(userJsonStr, User.class);

        if (currentUser == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"未登录,需要强制登录status=10");
        }
        return userService.getInformation(currentUser.getId());
    }

}
