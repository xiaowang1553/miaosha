package com.wang.miaosha.controller;

import com.wang.miaosha.Service.GoodsService;
import com.wang.miaosha.Service.MiaoshaService;
import com.wang.miaosha.Service.MiaoshaUserService;
import com.wang.miaosha.Service.OrderService;
import com.wang.miaosha.domain.MiaoshaOrder;
import com.wang.miaosha.domain.MiaoshaUser;
import com.wang.miaosha.domain.OrderInfo;
import com.wang.miaosha.redis.GoodsKey;
import com.wang.miaosha.redis.RedisService;
import com.wang.miaosha.result.CodeMsg;
import com.wang.miaosha.result.Result;
import com.wang.miaosha.vo.GoodsDetailVo;
import com.wang.miaosha.vo.GoodsVo;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.thymeleaf.spring4.context.SpringWebContext;
import org.thymeleaf.spring4.view.ThymeleafViewResolver;


import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.List;

@Controller
@RequestMapping("/goods")
public class GoodsController {

    @Autowired
    MiaoshaUserService userService;

    @Autowired
    GoodsService goodsService;

    @Autowired
    OrderService orederService;

    @Autowired
    MiaoshaService miaoshaService;

    @Autowired
    RedisService redisService;

    @Autowired
    ThymeleafViewResolver thymeleafViewResolver;

    @Autowired
    ApplicationContext applicationContext;

    /**
     *为了适应手机端和电脑端不同形式的获取cookie上的token，采用了两种参数接收token，
     * 第一种是直接通过cookie获取token，手机端是通过参数的形式获取token
     */
    @RequestMapping(value = "to_list",produces = "text/html")
    @ResponseBody
    public String list(HttpServletRequest request,HttpServletResponse response,Model model, MiaoshaUser user){
//                          @CookieValue(value = MiaoshaUserService.COOKI_NAME_TOKEN,required = false)String cookieToken,
//                          @RequestParam(value=MiaoshaUserService.COOKI_NAME_TOKEN,required = false)String paramToken
//        if(StringUtils.isEmpty(cookieToken)&&StringUtils.isEmpty(paramToken)){
//            return "login";
//        }
//        String token=StringUtils.isEmpty(paramToken)?cookieToken:paramToken;
//        MiaoshaUser user=userService.getByToken(response,token);
        //取缓存
        String html=redisService.get(GoodsKey.getGoodsList,"",String.class);
        if(!StringUtils.isEmpty(html)){
            return html;
        }
        model.addAttribute("user",user);
        //查询商品列表
        List<GoodsVo> goodsList= goodsService.listGoodsVo();
        model.addAttribute("goodsList",goodsList);
//        return "goods_list";

        SpringWebContext ctx=new SpringWebContext(request,response,request.getServletContext(),
                request.getLocale(),model.asMap(),applicationContext);
        //手动渲染
        html=thymeleafViewResolver.getTemplateEngine().process("goods_list",ctx);
        if(!StringUtils.isEmpty(html)){
            redisService.set(GoodsKey.getGoodsList,"",html);
        }
        return html;
    }

    /**
     *返回详情页的第一版，使用redis缓存页面进行优化
     */
    @RequestMapping("/to_detail1/{goodsId}")
    @ResponseBody
    public String detail1(HttpServletRequest request,HttpServletResponse response,Model model, MiaoshaUser user, @PathVariable("goodsId")long goodsId){
        model.addAttribute("user",user);
        //取缓存
        String html=redisService.get(GoodsKey.getGoodsDetail,""+goodsId,String.class);
        if(!StringUtils.isEmpty(html)){
            return html;
        }
        //手动渲染
        GoodsVo goodsVo= goodsService.getGoodsVoByGoodsId(goodsId);
        long startAt=goodsVo.getStartDate().getTime();
        long endAt= goodsVo.getEndDate().getTime();
        long now=System.currentTimeMillis();
        int miaoshaStatus=0;
        int remainSeconds=0;
        if(now<startAt){ //秒杀还没开始，倒计时
            miaoshaStatus=0;
            remainSeconds=(int)((startAt-now)/1000);
        }else if(now>endAt){ //秒杀进行中
            miaoshaStatus=2;
            remainSeconds=-1;
        }else{  //秒杀已经结束
            miaoshaStatus=1;
            remainSeconds=0;
        }
        model.addAttribute("goods",goodsVo);
        model.addAttribute("miaoshaStatus",miaoshaStatus);
        model.addAttribute("remainSeconds",remainSeconds);
//        return "goods_detail";
        SpringWebContext ctx=new SpringWebContext(request,response,request.getServletContext(),
                request.getLocale(),model.asMap(),applicationContext);
        html=thymeleafViewResolver.getTemplateEngine().process("goods_detail",ctx);
        if(!StringUtils.isEmpty(html)){
            redisService.set(GoodsKey.getGoodsDetail,""+goodsId,html);
        }
        return html;
    }

    @RequestMapping("/detail/{goodsId}")
    @ResponseBody
    public Result<GoodsDetailVo> detail(HttpServletRequest request,HttpServletResponse response,Model model, MiaoshaUser user, @PathVariable("goodsId")long goodsId){
        GoodsVo goodsVo= goodsService.getGoodsVoByGoodsId(goodsId);
        long startAt=goodsVo.getStartDate().getTime();
        long endAt= goodsVo.getEndDate().getTime();
        long now=System.currentTimeMillis();
        int miaoshaStatus=0;
        int remainSeconds=0;
        if(now<startAt){ //秒杀还没开始，倒计时
            miaoshaStatus=0;
            remainSeconds=(int)((startAt-now)/1000);
        }else if(now>endAt){ //秒杀进行中
            miaoshaStatus=2;
            remainSeconds=-1;
        }else{  //秒杀已经结束
            miaoshaStatus=1;
            remainSeconds=0;
        }
        GoodsDetailVo vo=new GoodsDetailVo();
        vo.setGoods(goodsVo);
        vo.setMiaoshaStatus(miaoshaStatus);
        vo.setRemainSeconds(remainSeconds);
        vo.setUser(user);
        return Result.success(vo);
    }


}
