package com.mmall.controller.portal;

import com.github.pagehelper.PageInfo;
import com.mmall.common.ServiceResponse;
import com.mmall.service.IProducetService;
import com.mmall.vo.ProductDetailVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/product/")
public class productController {
    @Autowired
    private IProducetService iProducetService;

    /**
     * 获取产品详情;
     * @param productId
     * @return
     */
    @RequestMapping("detail.do")
    @ResponseBody
    public ServiceResponse<ProductDetailVo> detail (Integer productId){
        return iProducetService.getProductDetail(productId);
    }

    /**
     * 动态排序 + 分页功能
     * @param keyword
     * @param categoryId
     * @param PageNum
     * @param pageSize
     * @param orderBy
     * @return
     */
    @RequestMapping("list.do")
    @ResponseBody
    public ServiceResponse<PageInfo> list(@RequestParam(value = "keyword",required = false) String keyword,
                                          @RequestParam(value = "categoryId",required = false) Integer categoryId,
                                          @RequestParam(value = "PageNum",defaultValue = "1") int PageNum ,
                                          @RequestParam(value = "pageSize",defaultValue = "10") int pageSize,
                                          @RequestParam(value = "orderBy",defaultValue = "") String orderBy){
        return iProducetService.getProductByKeywordCategory(keyword,categoryId,PageNum,pageSize,orderBy);
    }
}
