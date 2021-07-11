package com.wang.miaosha.controller;

import com.wang.miaosha.Service.GoodsService;
import com.wang.miaosha.Service.OrderService;
import com.wang.miaosha.domain.MiaoshaUser;
import com.wang.miaosha.domain.OrderInfo;
import com.wang.miaosha.result.CodeMsg;
import com.wang.miaosha.result.Result;
import com.wang.miaosha.vo.GoodsVo;
import com.wang.miaosha.vo.OrderDetailVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller()
@RequestMapping("/order")
/**
 * @NeedLogin 每次判断user是否为空
 */
public class OrderController {

    @Autowired
    OrderService orderService;

    @Autowired
    GoodsService goodsService;

    @RequestMapping("/detail")
    @ResponseBody
    public Result<OrderDetailVo> info(Model model, MiaoshaUser user,
                                      @RequestParam("orderId") long orderId) {
        if(user==null){
            return Result.error(CodeMsg.SESSION_ERROR);
        }
        OrderInfo order=orderService.getOrderById(orderId);
        if(order==null){
            return Result.error(CodeMsg.ORDER_NOT_EXIST);
        }
        GoodsVo goods=goodsService.getGoodsVoByGoodsId(order.getGoodsId());
        OrderDetailVo vo=new OrderDetailVo();
        vo.setOrderInfo(order);
        vo.setGoods(goods);
        return Result.success(vo);
    }
}
