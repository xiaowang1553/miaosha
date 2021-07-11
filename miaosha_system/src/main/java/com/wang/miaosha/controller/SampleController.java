package com.wang.miaosha.controller;

import com.wang.miaosha.rabbitmq.MQReceiver;
import com.wang.miaosha.rabbitmq.MQSender;
import com.wang.miaosha.result.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/demo")
public class SampleController {
    @Autowired
    MQSender sender;

    @Autowired
    MQReceiver receiver;

//    @RequestMapping("/mq")
//    @ResponseBody
//    public Result<String> mq(){
//        sender.send("hello rabbitmq");
//        return Result.success("hello,world");
//    }

//
//    @Autowired
//    UserService userService;
//
//    @Autowired
//    RedisService redisService;
//
//    @RequestMapping("/thymeleaf")
//    public String thymeleaf(Model model){
//        model.addAttribute("name","wang");
//        return "hello";
//    }
//
//    @RequestMapping("/db/get")
//    @ResponseBody
//    public Result<MiaoshaUser> doGet(){
//        MiaoshaUser user=userService.getById(1);
//        if(user==null){
//            return Result.error(CodeMsg.SERVER_ERROR);
//        }
//        return Result.success(user);
//    }
//
//    @RequestMapping("/db/tx")
//    @ResponseBody
//    public Result<Boolean> dbTx(){
//        userService.tx();
//        return Result.success(true);
//    }
//
//    @RequestMapping("/redis/get")
//    @ResponseBody
//    public Result<MiaoshaUser> redisGet(){
//        MiaoshaUser user = redisService.get(UserKey.getById, "" + 1, MiaoshaUser.class);
//        return Result.success(user);
//    }
//
//    @RequestMapping("/redis/set")
//    @ResponseBody
//    public Result<Boolean> redisSet(){
//        MiaoshaUser user=new MiaoshaUser();
//        user.setId(1);
//        user.setName("lucy");
//        Boolean ref=redisService.set(UserKey.getById,""+1,user);
//        return Result.success(true);
//    }
}
