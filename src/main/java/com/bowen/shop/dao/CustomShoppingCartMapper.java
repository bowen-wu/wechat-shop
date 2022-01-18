package com.bowen.shop.dao;

import com.bowen.shop.generate.ShoppingCart;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface CustomShoppingCartMapper {
    void batchInsert(List<ShoppingCart> shoppingCartList);

    void batchUpdate(List<ShoppingCart> shoppingCartList);

    void batchDelete(List<ShoppingCart> shoppingCartList);
}
