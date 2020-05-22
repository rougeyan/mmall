package com.mmall.controller.backend;

import com.github.pagehelper.PageInfo;
import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServiceResponse;
import com.mmall.pojo.User;
import com.mmall.service.IOrderService;
import com.mmall.service.IUserService;
import com.mmall.vo.OrderVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;

@Controller
@RequestMapping("/manage/order/")
public class OrderManageController {

    @Autowired
    private IUserService iUserService;
    @Autowired
    private IOrderService iOrderService;

    @RequestMapping("list.do")
    @ResponseBody
    public ServiceResponse<PageInfo> orderList(HttpSession session,
                                               @RequestParam(value ="pageNum",defaultValue = "1") int pageNum,
                                               @RequestParam(value ="pageSize",defaultValue = "10") int pageSize){
////        User user = (User)session.getAttribute(Const.CURRENT_USER);
////        if(user == null){
////            return  ServiceResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"用户未登录,请登录");
////        }
////        // 校验一下是否是管理员
////        if(iUserService.checkAdminRole(user).isSuccess()){
////            // 填充业务员逻辑;
////
////        }else{
////            return ServiceResponse.createByErrorMessage("无权限操作,需要管理员权限");
////        }
        return iOrderService.manageList(pageNum,pageSize);
    }

    // backend

    @RequestMapping("detail.do")
    @ResponseBody
    public ServiceResponse<OrderVo> orderDetail(HttpSession session, Long orderNo){
        return  iOrderService.manageDetail(orderNo);
    }
    //  一期是暂时为了精确匹配
    // 针对二期作多条件耦合的模糊匹配查询(分页)
    @RequestMapping("search.do")
    @ResponseBody
    public ServiceResponse<PageInfo> manageSearch(HttpSession session,
                                                 Long orderNo,
                                                 @RequestParam(value ="pageNum",defaultValue = "1") int pageNum,
                                                 @RequestParam(value ="pageSize",defaultValue = "10") int pageSize){
        return iOrderService.manageSearch(orderNo,pageNum,pageSize);
    }
    
    @RequestMapping("send_goods.do")
    @ResponseBody
    public ServiceResponse<String> orderSendGoods(HttpSession session, Long orderNo){
        return  iOrderService.manageSendGoods(orderNo);
    }


}
