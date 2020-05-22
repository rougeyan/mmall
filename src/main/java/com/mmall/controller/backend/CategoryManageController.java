package com.mmall.controller.backend;

import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServiceResponse;
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
    private IUserService iUserService;

    @Autowired
    private ICategoryService iCategoryService;
    /**
     *  增加分类功能
     * @param session
     * @param categoryName
     * @param parentId 默认根节点 0;
     * @return
     */
    @RequestMapping("add_category.do")
    @ResponseBody
    public ServiceResponse addCategory(HttpSession session,
                                       String categoryName,
                                       @RequestParam(value = "parentId",defaultValue = "0") int parentId){
//        User user = (User)session.getAttribute(Const.CURRENT_USER);
//        if(user == null){
//            return  ServiceResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"用户未登录,请登录");
//        }
//        // 校验一下是否是管理员
//        if(iUserService.checkAdminRole(user).isSuccess()){
//            // 是管理员
//            // 分家我们处理分类的逻辑
//        }else{
//            return ServiceResponse.createByErrorMessage("无权限操作,需要管理员权限");
//        }
        return iCategoryService.addCategory(categoryName,parentId);
    }

    /**
     * 更新品类名称
     * @param session
     * @param categoryId
     * @param categoryName
     * @return
     */
    @RequestMapping("set_category_name.do")
    @ResponseBody
    public ServiceResponse setCategoryName(HttpSession session,
                                           Integer categoryId,
                                           String categoryName){
        return iCategoryService.updateCategoryName(categoryId,categoryName);
    }

    /**
     * 根据categoryId 获取当前categoryId  子节点 所有平级category 信息 不递归;
     * @param session
     * @param categoryId
     * @return
     */
    @RequestMapping("get_category.do")
    @ResponseBody
    public ServiceResponse getChildrenParalleCategory(HttpSession session,@RequestParam(value ="categoryId",defaultValue = "0") int categoryId){
        // 查询子节点的category信息, 并且不递归 保持平级
        return iCategoryService.getChildrenParalleCategory(categoryId);
    }

    /**
     * 根据当前categoryId 并且递归查询它的子节点的categoryId (递归)
     * @param session
     * @param categoryId
     * @return
     */
    @RequestMapping("get_deep_category.do")
    @ResponseBody
    public ServiceResponse getCategoryAndDeepChildrenCategory(HttpSession session,@RequestParam(value ="categoryId",defaultValue = "0") int categoryId){
        // 查询当前节点的id和递归子节点的id;
        // 0 -> 10000 -> 100000
        // 传0 要 100000 100000
        // 传10000  获取的只有100000
        return iCategoryService.selectCategoryAndChildrenById(categoryId);
    }
}
