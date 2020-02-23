package com.mmall.controller.common.interceptor;

import com.mmall.common.Const;
import com.mmall.common.ServiceResponse;
import com.mmall.pojo.User;
import com.mmall.util.CookieUtils;
import com.mmall.util.RedisUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;

public class AuthorityInterceptor implements HandlerInterceptor {

    @Autowired
    private RedisUtil redisUtil;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        HandlerMethod handlerMethod = (HandlerMethod) handler;
        // 解释handleMethod
        String methodName = handlerMethod.getMethod().getName();
        String className = handlerMethod.getBean().getClass().getSimpleName();

//        我们可以这这里更细化地判定拦截器的放行规则 章节 10-7 具体查看;

//        解析参数
        StringBuffer requestParamBuffer = new StringBuffer();
        Map paramMap = request.getParameterMap();
        Iterator it = paramMap.entrySet().iterator();
        while(it.hasNext()){
            Map.Entry entry = (Map.Entry) it.next();
            String mapKey = (String) entry.getKey();

            String mapValue = StringUtils.EMPTY;

            // request 这个参数的map 里面的value返回的是一个String[];

            Object obj = entry.getValue();
            if(obj instanceof String[]){
                String[] strs = (String[]) obj;
                mapValue = Arrays.toString(strs);
            }
            requestParamBuffer.append(mapKey).append("=").append(mapValue);

        }
        // 利用cookies获取token
        String access_token = CookieUtils.getCookieValue(request,"access_token");
        // 从redis中获取登录信息
        User user = (User)redisUtil.get(access_token);
        // 如果 user null是空 或者非管理员
        if(user == null || (user.getRole().intValue() != Const.Role.Role_ADMIN)){
            // 返回false 即不会调用controller的方法
//            return false;
            response.reset(); // 这里要添加reset, 否则报异常,getWriter() has already been called for this response.
            // 这里已经脱离了spring MVC的流程
            response.setCharacterEncoding("UTF-8");
            response.setContentType("application/json;charset=UTF-8"); // 这里要设置返回值类型
            PrintWriter out = response.getWriter();

            // 这里需要转一次json;
            if(user == null){
                out.print(ServiceResponse.createByErrorMessage("拦截器拦截,用户未登录"));
            }else{
                out.print(ServiceResponse.createByErrorMessage("拦截器拦截,非管理员权限"));
            }
            out.flush();
            out.close(); // 这里要关闭;
            return false;
        }
        // 正常方法 进入controller
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        System.out.println();
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {

    }
}
