package com.wang.miaosha.access;

import com.alibaba.fastjson.JSON;
import com.wang.miaosha.Service.MiaoshaUserService;
import com.wang.miaosha.domain.MiaoshaUser;
import com.wang.miaosha.redis.AccessKey;
import com.wang.miaosha.redis.RedisService;
import com.wang.miaosha.result.CodeMsg;
import com.wang.miaosha.result.Result;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.util.WebUtils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;

@Service
public class AccessInterceptor implements HandlerInterceptor {
    @Autowired
    MiaoshaUserService userService;

    @Autowired
    RedisService redisService;
    @Override
    public boolean preHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object handler) throws Exception {
        if(handler instanceof HandlerMethod){
            MiaoshaUser user=getUser(httpServletRequest,httpServletResponse);
            UserContext.setUser(user);
            HandlerMethod hm=(HandlerMethod)handler;
            AccessLimit accessLimit = hm.getMethodAnnotation(AccessLimit.class);
            if(accessLimit==null){
                return true;
            }
            int seconds = accessLimit.seconds();
            int maxCount = accessLimit.maxCount();
            boolean needLogin=accessLimit.needLogin();
            String key=httpServletRequest.getRequestURI();
            if(needLogin){
                if(user==null){
                    render(httpServletResponse,CodeMsg.SESSION_ERROR);
                    return false;
                }
                key+="_"+user.getId();
            }else{
                // do nothing
            }
            AccessKey ak=AccessKey.withExpire(seconds);
            Integer count=redisService.get(ak,key,Integer.class);
            if(count==null){
                redisService.set(ak,key,""+1);
            }
            else if(count<maxCount){
                redisService.incr(ak,key);
            }
            else {
                render(httpServletResponse,CodeMsg.ACCESS_LIMIT_REACHED);
                return false;
            }
        }
        return true;
    }

    private void render(HttpServletResponse response, CodeMsg cm) {
        try {
            response.setContentType("application/json;charset=UTF-8");
            OutputStream out=response.getOutputStream();
            String msg= JSON.toJSONString(Result.error(cm));
            out.write(msg.getBytes("UTF-8"));
            out.flush();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void postHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, ModelAndView modelAndView) throws Exception {

    }

    @Override
    public void afterCompletion(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, Exception e) throws Exception {
        Logger logger=LoggerFactory.getLogger(AccessInterceptor.class);
        logger.info(String.valueOf(httpServletRequest.getServerPort()));
    }

    private MiaoshaUser getUser(HttpServletRequest request, HttpServletResponse response) {
        String paramToken = request.getParameter(MiaoshaUserService.COOKI_NAME_TOKEN);
        String cookieToken = getCookieValue(request, MiaoshaUserService.COOKI_NAME_TOKEN);
        if(StringUtils.isEmpty(cookieToken) && StringUtils.isEmpty(paramToken)) {
            return null;
        }
        String token = StringUtils.isEmpty(paramToken)?cookieToken:paramToken;
        return userService.getByToken(response, token);
    }

    private String getCookieValue(HttpServletRequest request, String cookiName) {
        Cookie[]  cookies = request.getCookies();
        if(cookies == null || cookies.length <= 0){
            return null;
        }
        for(Cookie cookie : cookies) {
            if(cookie.getName().equals(cookiName)) {
                return cookie.getValue();
            }
        }
        return null;
    }
}
