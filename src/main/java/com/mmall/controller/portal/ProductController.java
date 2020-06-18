package com.mmall.controller.portal;

import com.github.pagehelper.PageInfo;
import com.mmall.common.ServiceResponse;
import com.mmall.service.IProducetService;
import com.mmall.vo.ProductDetailVo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/product/")
public class ProductController {
    @Autowired
    private IProducetService iProducetService;

    /**
     * 获取产品详情;
     * @param productId
     * @return
     */
    // @RequestMapping("detail.do") http://localhost:8081/product/detail.do?productId=28
    @RequestMapping(value = "/{productId}",method = RequestMethod.GET)
    // http://localhost:8081/product/28 相对简洁一些 配合注解 @PathVariable
    @ResponseBody
    public ServiceResponse<ProductDetailVo> detail (@PathVariable Integer productId){
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

    // restful;这里是所有参数都传
    @RequestMapping(value = "/{keyword}/{categoryId}/{pageNum}/{pageSize}/{orderBy}",method = RequestMethod.GET)
    @ResponseBody
    public ServiceResponse<PageInfo> listRESTful(@PathVariable(value = "keyword") String keyword,
                                          @PathVariable(value = "categoryId") Integer categoryId,
                                          @PathVariable(value = "pageNum") Integer pageNum ,
                                          @PathVariable(value = "pageSize") Integer pageSize,
                                          @PathVariable(value = "orderBy") String orderBy){
        if(pageNum == null){
            pageNum = 1;
        }
        if(pageSize== null){
            pageSize = 10;
        }
        if(StringUtils.isBlank(orderBy)){
            orderBy = "price_asc";
        }
        return iProducetService.getProductByKeywordCategory(keyword,categoryId,pageNum,pageSize,orderBy);
    }
    // 没传品类id
    @RequestMapping(value = "/{keyword}/{pageNum}/{pageSize}/{orderBy}",method = RequestMethod.GET)
    @ResponseBody
    public ServiceResponse<PageInfo> listRESTfulBadcase(@PathVariable(value = "keyword") String keyword,
                                                 @PathVariable(value = "pageNum") Integer pageNum ,
                                                 @PathVariable(value = "pageSize") Integer pageSize,
                                                 @PathVariable(value = "orderBy") String orderBy){
        if(pageNum == null){
            pageNum = 1;
        }
        if(pageSize== null){
            pageSize = 10;
        }
        if(StringUtils.isBlank(orderBy)){
            orderBy = "price_asc";
        }
        return iProducetService.getProductByKeywordCategory(keyword,null,pageNum,pageSize,orderBy);
    }

    // 没传关键字
    @RequestMapping(value = "/{categoryId}/{pageNum}/{pageSize}/{orderBy}",method = RequestMethod.GET)
    @ResponseBody
    public ServiceResponse<PageInfo> listRESTfulBadcase(@PathVariable(value = "categoryId") Integer categoryId,
                                                 @PathVariable(value = "pageNum") Integer pageNum ,
                                                 @PathVariable(value = "pageSize") Integer pageSize,
                                                 @PathVariable(value = "orderBy") String orderBy){
        if(pageNum == null){
            pageNum = 1;
        }
        if(pageSize== null){
            pageSize = 10;
        }
        if(StringUtils.isBlank(orderBy)){
            orderBy = "price_asc";
        }
        return iProducetService.getProductByKeywordCategory(null,categoryId,pageNum,pageSize,orderBy);
    }
    // 只传品类id(或产品id);
    @RequestMapping(value = "/category/{keyword}/{categoryId}/{pageNum}/{pageSize}/{orderBy}",method = RequestMethod.GET)
    @ResponseBody
    public ServiceResponse<PageInfo> listRESTful(@PathVariable(value = "categoryId") Integer categoryId,
                                                 @PathVariable(value = "pageNum") Integer pageNum ,
                                                 @PathVariable(value = "pageSize") Integer pageSize,
                                                 @PathVariable(value = "orderBy") String orderBy){
        if(pageNum == null){
            pageNum = 1;
        }
        if(pageSize== null){
            pageSize = 10;
        }
        if(StringUtils.isBlank(orderBy)){
            orderBy = "price_asc";
        }
        return iProducetService.getProductByKeywordCategory(null,categoryId,pageNum,pageSize,orderBy);
    }

    // 只传关键字;
    @RequestMapping(value = "/keyword/{keyword}/{categoryId}/{pageNum}/{pageSize}/{orderBy}",method = RequestMethod.GET)
    @ResponseBody
    public ServiceResponse<PageInfo> listRESTful(@PathVariable(value = "keyword") String keyword,
                                                 @PathVariable(value = "pageNum") Integer pageNum ,
                                                 @PathVariable(value = "pageSize") Integer pageSize,
                                                 @PathVariable(value = "orderBy") String orderBy){
        if(pageNum == null){
            pageNum = 1;
        }
        if(pageSize== null){
            pageSize = 10;
        }
        if(StringUtils.isBlank(orderBy)){
            orderBy = "price_asc";
        }
        return iProducetService.getProductByKeywordCategory(keyword,null,pageNum,pageSize,orderBy);
    }
}
