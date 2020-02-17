package com.mmall.controller.portal;

import com.mmall.util.ImageUtil;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@Controller
@RequestMapping("/code/")
public class CodeController {
    @RequestMapping(value = "get_verify_image_code.do", method = RequestMethod.GET)
    @ResponseBody
    public String getVerifyImageCode(HttpServletRequest request, HttpServletResponse response) throws Exception{
        response.setContentType("image/jpeg");
        //禁止图像缓存
        response.setHeader("Pragma","no-cache");
        response.setHeader("Cache-Control", "no-cache");
        response.setDateHeader("Expires", 0);
        HttpSession session = request.getSession();
        // 这里拿到缓存
        ImageUtil imageUtil = new ImageUtil(120, 40, 5,30);
        session.setAttribute("code", imageUtil.getCode());
        System.out.println(imageUtil.getCode());
        imageUtil.write(response.getOutputStream());
        return null;
    }
}
