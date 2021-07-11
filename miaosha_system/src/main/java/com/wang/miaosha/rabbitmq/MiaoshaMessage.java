package com.wang.miaosha.rabbitmq;

import com.wang.miaosha.domain.MiaoshaUser;

public class MiaoshaMessage {
    private long goodsId;
    private MiaoshaUser user;

    public long getGoodsId() {
        return goodsId;
    }

    public void setGoodsId(long goodsId) {
        this.goodsId = goodsId;
    }

    public MiaoshaUser getUser() {
        return user;
    }

    public void setUser(MiaoshaUser user) {
        this.user = user;
    }
}
