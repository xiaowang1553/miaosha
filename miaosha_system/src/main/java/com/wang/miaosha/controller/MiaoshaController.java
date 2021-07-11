package com.wang.miaosha.controller;

import com.oracle.webservices.internal.api.message.PropertySet;
import com.wang.miaosha.Service.GoodsService;
import com.wang.miaosha.Service.MiaoshaService;
import com.wang.miaosha.Service.OrderService;
import com.wang.miaosha.access.AccessLimit;
import com.wang.miaosha.domain.MiaoshaOrder;
import com.wang.miaosha.domain.MiaoshaUser;
import com.wang.miaosha.domain.OrderInfo;
import com.wang.miaosha.rabbitmq.MQSender;
import com.wang.miaosha.rabbitmq.MiaoshaMessage;
import com.wang.miaosha.redis.AccessKey;
import com.wang.miaosha.redis.GoodsKey;
import com.wang.miaosha.redis.MiaoshaKey;
import com.wang.miaosha.redis.RedisService;
import com.wang.miaosha.result.CodeMsg;
import com.wang.miaosha.result.Result;
import com.wang.miaosha.util.MD5Utils;
import com.wang.miaosha.util.UUIDUtil;
import com.wang.miaosha.vo.GoodsVo;
import org.apache.http.HttpResponse;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.websocket.server.PathParam;
import java.awt.image.BufferedImage;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;

@Controller
@RequestMapping("/miaosha")
public class MiaoshaController implements InitializingBean {

    @Autowired
    GoodsService goodsService;

    @Autowired
    OrderService orderService;

    @Autowired
    MiaoshaService miaoshaService;

    @Autowired
    RedisService redisService;

    @Autowired
    MQSender sender;

    private HashMap<Long, Boolean> localOverMap = new HashMap<>();

    @Override
    public void afterPropertiesSet() throws Exception {
        List<GoodsVo> goodsList = goodsService.listGoodsVo();
        if (goodsList == null) {
            return;
        }
        for (GoodsVo goods : goodsList) {
            redisService.set(GoodsKey.getMiaoshaGoodsStock, "" + goods.getId(), goods.getStockCount());
            localOverMap.put(goods.getId(), false);
        }
    }


    /**
     * qps:235
     * 线程数：5000*10
     */

//    @RequestMapping("/do_miaosha")
//    public String doMiaosha(Model model, MiaoshaUser user, @RequestParam("goodsId")long goodsId){
//        //判断用户是否登录
//        if(user==null){
//            return "login";
//        }
//        //判断库存
//        GoodsVo goods=goodsService.getGoodsVoByGoodsId(goodsId);
//        int stock=goods.getStockCount();
//        if(stock<=0){
//            model.addAttribute("errmsg", CodeMsg.MIAO_SHA_OVER);
//            return "miaosha_fail";
//        }
//        //判断是否是重复秒杀
//        MiaoshaOrder order=orderService.getMiaoshaOrderByUserIdGoodsId(user.getId(),goodsId);
//        if(order!=null){
//            model.addAttribute("errmsg",CodeMsg.REPEATE_MIAOSHA);
//            return "miaosha_fail";
//        }
//        //执行秒杀操作-减库存 下订单 写入秒杀订单
//        OrderInfo orderInfo=miaoshaService.miaosha(user,goods);
//        model.addAttribute("orderInfo",orderInfo);
//        model.addAttribute("goods",goods);
//        return "order_detail";
//    }
    @RequestMapping(value = "/{path}/do_miaosha", method = RequestMethod.POST)
    @ResponseBody
    public Result<Integer> miaosha(Model model, MiaoshaUser user, @RequestParam("goodsId") long goodsId, @PathVariable("path") String path) {
        //判断用户是否登录
        if (user == null) {
            return Result.error(CodeMsg.SESSION_ERROR);
        }
        model.addAttribute("user", user);
        //验证path
        boolean check=miaoshaService.checkPath(user,goodsId,path);
        if(!check){
            return Result.error(CodeMsg.REQUEST_ILLEGAL);
        }
        //内存标记，减少redis访问
        if (localOverMap.get(goodsId)) {
            return Result.error(CodeMsg.MIAO_SHA_OVER);
        }
        //预减库存
        long stock = redisService.decr(GoodsKey.getMiaoshaGoodsStock, "" + goodsId);
        if (stock < 0) {
            localOverMap.put(goodsId, true);
            return Result.error(CodeMsg.MIAO_SHA_OVER);
        }
        //判断是否重复秒杀
        MiaoshaOrder order = orderService.getMiaoshaOrderByUserIdGoodsId(user.getId(), goodsId);
        if (order != null) {
            return Result.error(CodeMsg.REPEATE_MIAOSHA);
        }
        //入队
        MiaoshaMessage mm = new MiaoshaMessage();
        mm.setUser(user);
        mm.setGoodsId(goodsId);
        sender.sendMiaoshaMessage(mm);
        return Result.success(0); //排队中
//        //判断库存
//        GoodsVo goods=goodsService.getGoodsVoByGoodsId(goodsId);
//        int stock=goods.getStockCount();
//        if(stock<=0){
//            return Result.error(CodeMsg.MIAO_SHA_OVER);
//        }
//        //判断是否是重复秒杀
//        MiaoshaOrder order=orderService.getMiaoshaOrderByUserIdGoodsId(user.getId(),goodsId);
//        if(order!=null){
//            return Result.error(CodeMsg.REPEATE_MIAOSHA);
//        }
//        //执行秒杀操作-减库存 下订单 写入秒杀订单
//        OrderInfo orderInfo=miaoshaService.miaosha(user,goods);
//        return Result.success(orderInfo);
    }

    /**
     * @return orderId:成功
     * -1:秒杀失败
     * 0:排队中
     */
    @RequestMapping(value = "/result", method = RequestMethod.GET)
    @ResponseBody
    public Result<Long> miaoshaResult(Model model, MiaoshaUser user, @RequestParam("goodsId") long goodsId) {
        if (user == null) {
            return Result.error(CodeMsg.SESSION_ERROR);
        }
        model.addAttribute("user", user);
        long result = miaoshaService.getMiaoshaResult(user.getId(), goodsId);
        return Result.success(result);
    }

    @AccessLimit(seconds = 5,maxCount = 5,needLogin = true)
    @RequestMapping(value = "/path", method = RequestMethod.GET)
    @ResponseBody
    public Result<String> getMiaoshaPath(HttpServletRequest request,Model model, MiaoshaUser user, @RequestParam("goodsId") long goodsId, @RequestParam(value = "verifyCode",defaultValue = "0")int verifyCode) {
        model.addAttribute("user", user);
        boolean check = miaoshaService.checkVerifyCode(user, goodsId, verifyCode);
        if(!check) {
            return Result.error(CodeMsg.REQUEST_ILLEGAL);
        }
        String path=miaoshaService.createMiaoshaPath(user,goodsId);
        return Result.success(path);
    }

    @RequestMapping(value = "/verifyCode", method=RequestMethod.GET)
    @ResponseBody
    public Result<String> getMiaoshaVerifyCode(HttpServletResponse response, Model model, MiaoshaUser user, @RequestParam("goodsId") long goodsId) {
        if (user == null) {
            return Result.error(CodeMsg.SESSION_ERROR);
        }
        model.addAttribute("user", user);
        try {
            BufferedImage image  = miaoshaService.createVerifyCode(user, goodsId);
            OutputStream out = response.getOutputStream();
            ImageIO.write(image, "JPEG", out);
            out.flush();
            out.close();
            return null;
        }catch(Exception e) {
            e.printStackTrace();
            return Result.error(CodeMsg.MIAOSHA_FAIL);
        }
    }
}
