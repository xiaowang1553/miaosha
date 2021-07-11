package com.wang.miaosha.Service;

import com.wang.miaosha.dao.GoodsDao;
import com.wang.miaosha.domain.Goods;
import com.wang.miaosha.domain.MiaoshaGoods;
import com.wang.miaosha.vo.GoodsVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GoodsService {
    @Autowired
    GoodsDao goodsDao;
    public List<GoodsVo> listGoodsVo(){
         return goodsDao.listGoodsVo();
    }
    //为点击获取详情页提供服务
    public GoodsVo getGoodsVoByGoodsId(long goodsId){
        return goodsDao.getGoodsVoByGoodsId(goodsId);
    }

    public boolean reduceStock(GoodsVo goods) {
        MiaoshaGoods g=new MiaoshaGoods();
        g.setGoodsId(goods.getId());
        int ret=goodsDao.reduceStock(g);
        return ret>0;
    }
}
