package com.mmall.controller.portal;

import com.mmall.common.ServiceResponse;
import com.mmall.service.ICategoryService;
import com.mmall.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;

@Controller
@RequestMapping("/homePageView/")
public class homePageViewController {
    @Autowired
    private ICategoryService iCategoryService;
    /**
     * 根据categoryId 获取当前categoryId  子节点 所有平级category 信息 不递归;
     * @param session
     * @param categoryId
     * @return
     */
    @RequestMapping("get_category.do")
    @ResponseBody
    public ServiceResponse getChildrenParalleCategory(HttpSession session, @RequestParam(value ="categoryId",defaultValue = "0") int categoryId){
        // 查询子节点的category信息, 并且不递归 保持平级
//        return iCategoryService.getChildrenParalleCategory(categoryId);
        return null;
    }
}
