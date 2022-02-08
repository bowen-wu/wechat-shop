package com.bowen.shop.dao;

import com.bowen.shop.api.entity.GoodsIdAndNumber;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface GoodsStockMapper {
    int deductStock(GoodsIdAndNumber goodsIdAndNumber);
}

