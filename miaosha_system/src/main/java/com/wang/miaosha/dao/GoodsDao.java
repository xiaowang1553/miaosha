package com.wang.miaosha.dao;

import com.wang.miaosha.domain.Goods;
import com.wang.miaosha.domain.MiaoshaGoods;
import com.wang.miaosha.vo.GoodsVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@Mapper
public interface GoodsDao {
    @Select("select g.*,mg.stock_count,mg.start_date,mg.end_date,mg.miaosha_price from miaosha_goods mg left join goods g on mg.goods_id=g.id")
    public List<GoodsVo> listGoodsVo();
    @Select("select g.*,mg.stock_count,mg.start_date,mg.end_date,mg.miaosha_price from miaosha_goods mg left join goods g on mg.goods_id=g.id where g.id=#{goodsId}")
    public GoodsVo getGoodsVoByGoodsId(long goodsId);

    @Update("update miaosha_goods set stock_count=stock_count-1 where id=#{goodsId} and stock_count>0")
    public int reduceStock(MiaoshaGoods g);
}
