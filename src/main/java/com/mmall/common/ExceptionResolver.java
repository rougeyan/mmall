package com.mmall.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.json.MappingJackson2JsonView;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

// 处理全局异常
@Component
public class ExceptionResolver  implements HandlerExceptionResolver {

    private static final Logger logger = LoggerFactory.getLogger(ExceptionResolver.class);

    @Override
    public ModelAndView resolveException(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, Exception e) {
        // 记录错误日志；
        logger.error("{}Exception",httpServletRequest.getRequestURI(),e);
        // 通过modelView
        ModelAndView modelAndView = new ModelAndView(new MappingJackson2JsonView());
        modelAndView.addObject("status",ResponseCode.ERROR.getCode());
        modelAndView.addObject("msg","接口异常,请联系管理员");
        modelAndView.addObject("data",e.toString());
        return modelAndView;
    }
}
