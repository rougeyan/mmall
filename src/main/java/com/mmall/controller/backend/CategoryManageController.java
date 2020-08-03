package com.mmall.controller.backend;

import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServiceResponse;
import com.mmall.pojo.Category;
import com.mmall.pojo.User;
import com.mmall.service.ICategoryService;
import com.mmall.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
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
    @RequestMapping(value = "add_category.do", method = RequestMethod.POST)
    @ResponseBody
    public ServiceResponse addCategory(HttpSession session,
                                       Category category){
                String categoryName = category.getName();
                Integer parentId = category.getParentId();
        return iCategoryService.addCategory(categoryName,parentId);
    }

    /**
     * 更新品类名称
     * @param session
     * @param categoryId
     * @param categoryName
     * @return
     */
    @RequestMapping(value = "update_category.do", method = RequestMethod.POST)
    @ResponseBody
    public ServiceResponse updateCategory(HttpSession session,
                                           Category category){
        String categoryname = category.getName();
        return iCategoryService.updateCategory(category);
    }

    /**
     * 根据categoryId 获取当前categoryId  子节点 所有平级category 信息 不递归;
     * @param session
     * @param categoryId
     * @return
     */
    @RequestMapping("get_category.do")
    @ResponseBody
    public ServiceResponse getChildrenParalleCategory(@RequestParam(value ="categoryId",defaultValue = "0") int categoryId,
                                                      @RequestParam(value ="pageNum",defaultValue = "1") int pageNum,
                                                      @RequestParam(value ="pageSize",defaultValue = "10") int pageSize){
        // 查询子节点的category信息, 并且不递归 保持平级
        return iCategoryService.getChildrenParalleCategory(categoryId,pageNum,pageSize);
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
