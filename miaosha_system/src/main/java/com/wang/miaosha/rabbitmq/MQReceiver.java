package com.wang.miaosha.rabbitmq;

import com.wang.miaosha.Service.GoodsService;
import com.wang.miaosha.Service.MiaoshaService;
import com.wang.miaosha.Service.OrderService;
import com.wang.miaosha.domain.MiaoshaOrder;
import com.wang.miaosha.domain.MiaoshaUser;
import com.wang.miaosha.domain.OrderInfo;
import com.wang.miaosha.redis.RedisService;
import com.wang.miaosha.result.CodeMsg;
import com.wang.miaosha.vo.GoodsVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MQReceiver {
    private static Logger log= LoggerFactory.getLogger(MQReceiver.class);

    @Autowired
    GoodsService goodsService;

    @Autowired
    OrderService orderService;

    @Autowired
    MiaoshaService miaoshaService;

    @Autowired
    RedisService redisService;

    @RabbitListener(queues = MQConfig.MIAOSHA_QUEUE)
    public void receive(String message){
        log.info("receive message:"+message);
        MiaoshaMessage mm= RedisService.stringToBean(message,MiaoshaMessage.class);
        MiaoshaUser user=mm.getUser();
        long goodsId=mm.getGoodsId();
        //判断库存
        GoodsVo goods=goodsService.getGoodsVoByGoodsId(goodsId);
        int stock=goods.getStockCount();
        if(stock<=0){
            return;
        }
        //判断是否是重复秒杀
        MiaoshaOrder order=orderService.getMiaoshaOrderByUserIdGoodsId(user.getId(),goodsId);
        if(order!=null){
            return;
        }
        //执行秒杀操作-减库存 下订单 写入秒杀订单
        miaoshaService.miaosha(user,goods);
    }
}
