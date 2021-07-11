package com.wang.miaosha.redis;

public class MiaoshaUserKey extends BasePrefix{
    //设置默认token的有效时间是2天
    private static final int TOKEN_EXPIRE=3600*24*2;

    private MiaoshaUserKey(int expireSecond,String prefix){
        super(expireSecond,prefix);
    }

    public static MiaoshaUserKey token=new MiaoshaUserKey(TOKEN_EXPIRE,"tk");

    public static MiaoshaUserKey getById=new MiaoshaUserKey(0,"id");
}
