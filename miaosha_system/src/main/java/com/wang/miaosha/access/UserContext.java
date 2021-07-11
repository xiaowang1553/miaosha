package com.wang.miaosha.access;

import com.wang.miaosha.domain.MiaoshaUser;

public class UserContext {
    private static ThreadLocal<MiaoshaUser> userHolder=new ThreadLocal<>();
    public static void setUser(MiaoshaUser user){
        userHolder.set(user);
    }

    public static MiaoshaUser getUser(){
        return userHolder.get();
    }

}
