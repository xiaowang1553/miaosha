package com.wang.miaosha.controller;

import com.wang.miaosha.result.CodeMsg;
import com.wang.miaosha.result.Result;
import com.wang.miaosha.Service.MiaoshaUserService;
import com.wang.miaosha.util.ValidatorUtil;
import com.wang.miaosha.vo.LoginVo;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

@Controller
@RequestMapping("/login")
public class LoginController {
    @Autowired
    MiaoshaUserService miaoshaUserService;

    private static Logger logger= LoggerFactory.getLogger(LoginController.class);

    @RequestMapping("/to_login")
    public String toLogin(){
        return "login";
    }

    @RequestMapping("/do_login")
    @ResponseBody
    public Result<String>  doLogin(HttpServletResponse response, @Valid LoginVo loginVo){
        //参数校验
//        String passInput=loginVo.getPassword();
////        String mobile=loginVo.getMobile();
////        if(StringUtils.isEmpty(passInput)){
////            return Result.error(CodeMsg.PASSWORD_EMPTY);
////        }
////        if(StringUtils.isEmpty(mobile)){
////            return Result.error(CodeMsg.MOBILE_EMPTY);
////        }
////        if(!ValidatorUtil.isMobile(mobile)){
////            return Result.error(CodeMsg.MOBILE_ERROR);
////        }
        //登录
        String token=miaoshaUserService.login(response,loginVo);
        return Result.success(token);
    }
}
