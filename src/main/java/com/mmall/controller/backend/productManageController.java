package com.mmall.controller.backend;

import com.google.common.collect.Maps;
import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServiceResponse;
import com.mmall.pojo.Product;
import com.mmall.pojo.User;
import com.mmall.service.IFileService;
import com.mmall.service.IProducetService;
import com.mmall.service.IUserService;
import com.mmall.util.PropertiesUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Map;

@Controller
@RequestMapping("/manage/product")
public class productManageController {
    @Autowired
    private IUserService iUserService;

    @Autowired
    private IProducetService iProducetService;

    @Autowired
    private IFileService iFileService;

    /**
     * 新增/更新商品
     * @param session
     * @param product
     * @return
     */
    @RequestMapping("save.do")
    @ResponseBody
    public ServiceResponse productSave(HttpSession session, Product product){
        User user = (User)session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            return  ServiceResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"用户未登录,请登录");
        }
        // 校验一下是否是管理员
        if(iUserService.checkAdminRole(user).isSuccess()){
            // 增加产品的业务逻辑
            return iProducetService.saveOrUpdateProduct(product);
        }else{
            return ServiceResponse.createByErrorMessage("无权限操作,需要管理员权限");
        }
    }

    /**
     * 设置产品上下架;
     * @param session
     * @param productId
     * @param status
     * @return
     */
    @RequestMapping("set_sale_status.do")
    @ResponseBody
    public ServiceResponse setSaleStatus(HttpSession session, Integer productId,Integer status){
        User user = (User)session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            return  ServiceResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"用户未登录,请登录");
        }
        // 校验一下是否是管理员
        if(iUserService.checkAdminRole(user).isSuccess()){

            return iProducetService.setSaleStatus(productId,status);
        }else{
            return ServiceResponse.createByErrorMessage("无权限操作,需要管理员权限");
        }
    }

    @RequestMapping("detail.do")
    @ResponseBody
    public ServiceResponse getDetail(HttpSession session, Integer productId){
        User user = (User)session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            return  ServiceResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"用户未登录,请登录");
        }
        // 校验一下是否是管理员
        if(iUserService.checkAdminRole(user).isSuccess()){
            // 填充业务;
            // properties读取
            // vo的建立;
            return iProducetService.manageProductDetail(productId);
        }else{
            return ServiceResponse.createByErrorMessage("无权限操作,需要管理员权限");
        }
    }

    /**
     * 后台查询列表接口 分页功能和动态排序;
     * @param session
     * @param pageNum
     * @param pageSize
     * @return
     */
    @RequestMapping("list.do")
    @ResponseBody
    public ServiceResponse getList(HttpSession session,@RequestParam(value = "pageNum",defaultValue = "1") int pageNum,@RequestParam(value = "pageSize",defaultValue = "10") int pageSize){
        User user = (User)session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            return  ServiceResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"用户未登录,请登录");
        }
        // 校验一下是否是管理员
        if(iUserService.checkAdminRole(user).isSuccess()){
            // 动态分页功能
            // mybatis-pageHelper;
            return iProducetService.getProductList(pageNum,pageSize);
        }else{
            return ServiceResponse.createByErrorMessage("无权限操作,需要管理员权限");
        }
    }

    /**
     * 后台产品搜索
     * @param session
     * @param productName
     * @param productId
     * @return
     */
    @RequestMapping("search.do")
    @ResponseBody
    public ServiceResponse productSearch(HttpSession session,String productName, Integer productId,@RequestParam(value = "pageNum",defaultValue = "1") int pageNum,@RequestParam(value = "pageSize",defaultValue = "10") int pageSize){
        User user = (User)session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            return  ServiceResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"用户未登录,请登录");
        }
        // 校验一下是否是管理员
        if(iUserService.checkAdminRole(user).isSuccess()){
            // 动态分页功能
            // mybatis-pageHelper;
            return iProducetService.searchProduct(productName,productId,pageNum,pageSize);
        }else{
            return ServiceResponse.createByErrorMessage("无权限操作,需要管理员权限");
        }
    }

    /**
     * 上传文件接口
     * @param file
     * @param request
     * @return
     */
    @RequestMapping("upload.do")
    @ResponseBody
    public ServiceResponse upload(HttpSession session, @RequestParam(value = "upload_file",required = false) MultipartFile file, HttpServletRequest request){
        User user = (User)session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            return  ServiceResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"用户未登录,请登录");
        }
        // 校验一下是否是管理员
        if(iUserService.checkAdminRole(user).isSuccess()){
            // 具体业务逻辑
            // 拿一个request, 根据servlet 的上下文 动态创建一个相对路径出来
            String path = request.getSession().getServletContext().getRealPath("upload");
            String targetName =  iFileService.upload(file,path);
            String url = PropertiesUtil.getProperty("ftp.server.http.prefix")+targetName;

            Map fileMap = Maps.newHashMap();
            fileMap.put("uri",targetName);
            fileMap.put("url",url);
            return ServiceResponse.createBySuccess(fileMap);
        }else{
            return ServiceResponse.createByErrorMessage("无权限操作,需要管理员权限");
        }

    }


    @RequestMapping("richtext_img_upload.do")
    @ResponseBody
    public Map richtextImgUpload(HttpSession session, @RequestParam(value = "upload_file",required = false) MultipartFile file, HttpServletRequest request, HttpServletResponse response){
        Map resultMap =Maps.newHashMap();
        User user = (User)session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            resultMap.put("success",false);
            resultMap.put("msg","请登录管理员'");
            return  resultMap;
        }
        // 校验一下是否是管理员
        if(iUserService.checkAdminRole(user).isSuccess()){
            // 具体业务逻辑

            // 富文本中对与返回值有自己的要求,我们使用的是simditor的要求进行返回;
//            {
//                "success": true/false,
//                "msg": "error message", # optional
//                "file_path": "[real file path]"
//            }
            String path = request.getSession().getServletContext().getRealPath("upload");
            String targetName =  iFileService.upload(file,path);
            if(StringUtils.isBlank(targetName)){
                resultMap.put("success",false);
                resultMap.put("msg","上传失败");
                return resultMap;
            }
            resultMap.put("success",true);
            resultMap.put("msg","上传成功");
            String url = PropertiesUtil.getProperty("ftp.server.http.prefix")+targetName;
            resultMap.put("file_path",url);
            response.addHeader("Access-Control-Allow-Header","X-File-Name");
            return resultMap;
        }else{
            resultMap.put("success",false);
            resultMap.put("msg","无权限操作'");
            return resultMap;
        }

    }
}
