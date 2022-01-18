package com.bowen.shop.service;

import com.bowen.shop.dao.CustomShoppingCartMapper;
import com.bowen.shop.entity.AddToShoppingCartGoods;
import com.bowen.shop.entity.DataStatus;
import com.bowen.shop.entity.GoodsWithNumber;
import com.bowen.shop.entity.HttpException;
import com.bowen.shop.entity.ShoppingCartResponse;
import com.bowen.shop.generate.Goods;
import com.bowen.shop.generate.GoodsMapper;
import com.bowen.shop.generate.Shop;
import com.bowen.shop.generate.ShopMapper;
import com.bowen.shop.generate.ShoppingCart;
import com.bowen.shop.generate.ShoppingCartExample;
import com.bowen.shop.generate.ShoppingCartMapper;
import org.apache.ibatis.session.RowBounds;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class ShoppingCartService {
    private final GoodsMapper goodsMapper;
    private final ShopMapper shopMapper;
    private final ShoppingCartMapper shoppingCartMapper;
    private final CustomShoppingCartMapper customShoppingCartMapper;

    @Autowired
    public ShoppingCartService(GoodsMapper goodsMapper,
                               ShopMapper shopMapper,
                               ShoppingCartMapper shoppingCartMapper,
                               CustomShoppingCartMapper customShoppingCartMapper) {
        this.goodsMapper = goodsMapper;
        this.shopMapper = shopMapper;
        this.shoppingCartMapper = shoppingCartMapper;
        this.customShoppingCartMapper = customShoppingCartMapper;
    }

    private AddToShoppingCartGoods mixinGoodsNumberFromPendingAndDatabase(AddToShoppingCartGoods pendingShoppingCartGoods,
                                                                          List<ShoppingCart> shoppingCartListFromDatabase) {
        ShoppingCart shoppingCart = shoppingCartListFromDatabase.stream()
                .filter(isolateShoppingCart -> isolateShoppingCart.getGoodsId().equals(pendingShoppingCartGoods.getId()))
                .collect(Collectors.toList())
                .get(0);
        if (shoppingCart != null) {
            pendingShoppingCartGoods.setNumber(shoppingCart.getNumber());
        }
        return pendingShoppingCartGoods;
    }

    private AddToShoppingCartGoods includeGoodsInList(long goodsId, List<AddToShoppingCartGoods> addToShoppingCartGoodsList) {
        List<AddToShoppingCartGoods> goodsSingleList = addToShoppingCartGoodsList.stream()
                .filter(item -> item.getId() == goodsId)
                .collect(Collectors.toList());
        if (goodsSingleList.size() <= 0) {
            return null;
        }
        return goodsSingleList.get(0);
    }

    private boolean isGoodsIdInList(long goodsId, List<AddToShoppingCartGoods> addToShoppingCartGoodsList) {
        return addToShoppingCartGoodsList.stream().anyMatch(goods -> goods.getId() == goodsId);
    }


    public ShoppingCartResponse addGoodsListToShoppingCart(List<AddToShoppingCartGoods> addToShoppingCartGoodsList, Long userId) {
        // 如果该商品在购物车中则更新，如果不在则新增
        List<Long> goodsIdList = addToShoppingCartGoodsList.stream().distinct().map(AddToShoppingCartGoods::getId).collect(Collectors.toList());

        // insert
        List<GoodsWithNumber> goodsWithNumberList = addToShoppingCartGoodsList.stream()
                .filter(pendingGoods -> isGoodsIdInList(pendingGoods.getId(), addToShoppingCartGoodsList))
                .map(pendingGoods -> extracted(pendingGoods.getId(), pendingGoods.getNumber()))
                .collect(Collectors.toList());

        Shop queryShopFromDatabase = shopMapper.selectByPrimaryKey(goodsWithNumberList.get(0).getShopId());
        if (queryShopFromDatabase == null || DataStatus.DELETED.getStatus().equals(queryShopFromDatabase.getStatus())) {
            throw HttpException.notFound("店铺未找到！shopId" + goodsWithNumberList.get(0).getShopId());
        }

        List<ShoppingCart> shoppingCartList = goodsWithNumberList.stream()
                .map(goodsWithNumber -> convertToShoppingCartFromGoodsWithNumber(goodsWithNumber, queryShopFromDatabase.getId(), userId))
                .collect(Collectors.toList());

        // update
        ShoppingCartExample goodsIdListExample = new ShoppingCartExample();
        goodsIdListExample.createCriteria().andGoodsIdIn(goodsIdList);
        List<ShoppingCart> existGoodsInDatabase = shoppingCartMapper.selectByExample(goodsIdListExample);
        for (ShoppingCart existGoods : existGoodsInDatabase) {
            existGoods.setNumber(Objects.requireNonNull(includeGoodsInList(existGoods.getGoodsId(), addToShoppingCartGoodsList)).getNumber());
        }

        customShoppingCartMapper.batchUpdate(existGoodsInDatabase);
        customShoppingCartMapper.batchInsert(shoppingCartList);

        return ShoppingCartResponse.of(queryShopFromDatabase, goodsWithNumberList);
    }

    private ShoppingCart convertToShoppingCartFromGoodsWithNumber(GoodsWithNumber goodsWithNumber, Long shopId, Long userId) {
        ShoppingCart shoppingCart = new ShoppingCart();
        shoppingCart.setNumber(goodsWithNumber.getNumber());
        shoppingCart.setStatus(DataStatus.OK.getStatus());
        shoppingCart.setShopId(shopId);
        shoppingCart.setCreatedAt(new Date());
        shoppingCart.setUpdatedAt(new Date());
        shoppingCart.setUserId(userId);
        shoppingCart.setGoodsId(goodsWithNumber.getId());
        return shoppingCart;
    }

    private GoodsWithNumber generateGoodsWithNumber(Goods goods, int number) {
        GoodsWithNumber goodsWithNumber = new GoodsWithNumber();
        goodsWithNumber.setNumber(number);
        goodsWithNumber.setCreatedAt(goods.getCreatedAt());
        goodsWithNumber.setDescription(goods.getDescription());
        goodsWithNumber.setDetails(goods.getDetails());
        goodsWithNumber.setId(goods.getId());
        goodsWithNumber.setImageUrl(goods.getImageUrl());
        goodsWithNumber.setName(goods.getName());
        goodsWithNumber.setPrice(goods.getPrice());
        goodsWithNumber.setStock(goods.getStock());
        goodsWithNumber.setStatus(goods.getStatus());
        goodsWithNumber.setShopId(goods.getShopId());
        goodsWithNumber.setUpdatedAt(goods.getUpdatedAt());
        return goodsWithNumber;
    }

    public ShoppingCartResponse deleteGoodsInShoppingCart(long goodsId, Long userId) {
        ShoppingCartExample example = new ShoppingCartExample();
        example.createCriteria().andGoodsIdEqualTo(goodsId).andUserIdEqualTo(userId).andStatusEqualTo(DataStatus.OK.getStatus());
        List<ShoppingCart> shoppingCartList = shoppingCartMapper.selectByExample(example);
        if (shoppingCartList.size() == 0) {
            throw HttpException.notFound("商品未找到！goodsId：" + goodsId);
        }
        customShoppingCartMapper.batchDelete(shoppingCartList);

        Long shopId = shoppingCartList.get(0).getShopId();
        ShoppingCartExample responseExample = new ShoppingCartExample();
        responseExample.createCriteria().andShopIdEqualTo(shopId).andUserIdEqualTo(userId).andStatusEqualTo(DataStatus.OK.getStatus());
        List<ShoppingCart> shoppingCarts = shoppingCartMapper.selectByExample(example);

        Shop shop = shopMapper.selectByPrimaryKey(shopId);
        List<GoodsWithNumber> goodsWithNumberList = shoppingCarts.stream()
                .map(shoppingCart -> extracted(shoppingCart.getGoodsId(), shoppingCart.getNumber()))
                .collect(Collectors.toList());

        return ShoppingCartResponse.of(shop, goodsWithNumberList);
    }

    private GoodsWithNumber extracted(Long goodsId, Integer number) {
        Goods queryGoodsFromDatabase = goodsMapper.selectByPrimaryKey(goodsId);
        if (queryGoodsFromDatabase == null || DataStatus.DELETED.getStatus().equals(queryGoodsFromDatabase.getStatus())) {
            throw HttpException.notFound("商品未找到！goodsId" + goodsId);
        }
        return generateGoodsWithNumber(queryGoodsFromDatabase, number);
    }

    public List<ShoppingCartResponse> getGoodsWithPageFromShoppingCart(int pageNum, int pageSize, Long userId) {
        ShoppingCartExample example = new ShoppingCartExample();
        example.createCriteria().andUserIdEqualTo(userId).andStatusEqualTo(DataStatus.OK.getStatus());
        List<ShoppingCart> shoppingCartList = shoppingCartMapper.selectByExampleWithRowbounds(example, new RowBounds((pageNum - 1) * pageSize, pageSize));
        Map<Long, List<GoodsWithNumber>> shopWithGoodsListMap = getShopIdMapGoodsWithNumberList(shoppingCartList);
        return shopWithGoodsListMap.values().stream()
                .map(this::convertGoodsWithNumberListToShoppingCartResponse)
                .collect(Collectors.toList());
    }

    private Map<Long, List<GoodsWithNumber>> getShopIdMapGoodsWithNumberList(List<ShoppingCart> shoppingCartList) {
        // TODO: stream
        Map<Long, List<GoodsWithNumber>> shopWithGoodsListMap = new ConcurrentHashMap<>();
        for (ShoppingCart shoppingCart : shoppingCartList) {
            List<GoodsWithNumber> goodsWithNumberList = shopWithGoodsListMap.get(shoppingCart.getShopId());
            if (goodsWithNumberList == null) {
                shopWithGoodsListMap.put(shoppingCart.getShopId(), Collections.singletonList(extracted(shoppingCart.getGoodsId(), shoppingCart.getNumber())));
            } else {
                goodsWithNumberList.add(extracted(shoppingCart.getGoodsId(), shoppingCart.getNumber()));
                shopWithGoodsListMap.put(shoppingCart.getShopId(), goodsWithNumberList);
            }
        }
        return shopWithGoodsListMap;
    }

    private ShoppingCartResponse convertGoodsWithNumberListToShoppingCartResponse(List<GoodsWithNumber> goodsWithNumberList) {
        Shop shop = shopMapper.selectByPrimaryKey(goodsWithNumberList.get(0).getShopId());
        return ShoppingCartResponse.of(shop, goodsWithNumberList);
    }
}
