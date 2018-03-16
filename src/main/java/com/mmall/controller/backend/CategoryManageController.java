package com.mmall.controller.backend;

import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.User;
import com.mmall.service.ICategoryService;
import com.mmall.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;

@Controller
@RequestMapping("/manage/category")
public class CategoryManageController {

    @Autowired
    private IUserService userService;

    @Autowired
    private ICategoryService categoryService;

    @RequestMapping(value = "add_category.do")
    @ResponseBody
    public ServerResponse addCategory(HttpSession session, String categoryName,
                                      @RequestParam(value = "parentId", defaultValue = "0") int parentId) {
        User user = (User)session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "用户未登录，请登录");
        }

        // 效验一下是否是管理员
        if (userService.checkAdminRole(user).isSuccess()) {
            // 是管理员
            // 增加处理分类的逻辑
            return categoryService.addCategory(categoryName, parentId);
        }else  {
            return ServerResponse.createByErrorMessage("无权限操作，需要管理员权限");
        }

    }

    @RequestMapping(value = "set_category_name.do")
    @ResponseBody
    public ServerResponse setCategoryName(HttpSession session, String categoryName, int categoryId) {
        User user = (User)session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "用户未登录，请登录");
        }

        // 效验一下是否是管理员
        if (userService.checkAdminRole(user).isSuccess()) {
            // 是管理员
            // 更新CategoryName的逻辑
            return categoryService.updateCategoryName(categoryName, categoryId);
        }else  {
            return ServerResponse.createByErrorMessage("无权限操作，需要管理员权限");
        }
    }

    @RequestMapping(value = "get_category.do")
    @ResponseBody
    public ServerResponse getChildrenParallelCategory(HttpSession session,
                                                      @RequestParam(value= "categoryId", defaultValue = "0") int categoryId) {
        User user = (User)session.getAttribute(Const.CURRENT_USER);     // 效验用户是否登录的Key是Current User
        if (user == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "用户未登录，请登录");
        }

        // 效验一下是否是管理员
        if (userService.checkAdminRole(user).isSuccess()) {
            // 是管理员
            // 查询子节点的category信息, 并且不递归，保持平级
            return categoryService.getChildrenParallelCategory(categoryId);
        }else  {
            return ServerResponse.createByErrorMessage("无权限操作，需要管理员权限");
        }
    }

    @RequestMapping(value = "get_deep_category.do")
    @ResponseBody
    public ServerResponse getDeepChildrenCategory(HttpSession session,
                                                  @RequestParam(value= "categoryId", defaultValue = "0") int categoryId) {
        User user = (User)session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "用户未登录，请登录");
        }

        // 效验一下是否是管理员
        if (userService.checkAdminRole(user).isSuccess()) {
            // 是管理员
            // 查询当前结点的id和递归子节点的结点
            return categoryService.selectCategoryAndChildrenById(categoryId);
        }else  {
            return ServerResponse.createByErrorMessage("无权限操作，需要管理员权限");
        }
    }



}
