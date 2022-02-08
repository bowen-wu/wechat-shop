package com.bowen.shop.service;

import com.bowen.shop.dao.CustomShoppingCartMapper;
import com.bowen.shop.api.entity.GoodsIdAndNumber;
import com.bowen.shop.api.entity.DataStatus;
import com.bowen.shop.entity.GoodsWithNumber;
import com.bowen.shop.entity.HttpException;
import com.bowen.shop.entity.ResponseWithPages;
import com.bowen.shop.entity.ShoppingCartData;
import com.bowen.shop.generate.Goods;
import com.bowen.shop.generate.GoodsMapper;
import com.bowen.shop.generate.Shop;
import com.bowen.shop.generate.ShopMapper;
import com.bowen.shop.generate.ShoppingCart;
import com.bowen.shop.generate.ShoppingCartExample;
import com.bowen.shop.generate.ShoppingCartMapper;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
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
    @SuppressFBWarnings(value = {"EI_EXPOSE_REP", "EI_EXPOSE_REP2"}, justification = "I prefer to suppress these FindBugs warnings")
    public ShoppingCartService(GoodsMapper goodsMapper,
                               ShopMapper shopMapper,
                               ShoppingCartMapper shoppingCartMapper,
                               CustomShoppingCartMapper customShoppingCartMapper) {
        this.goodsMapper = goodsMapper;
        this.shopMapper = shopMapper;
        this.shoppingCartMapper = shoppingCartMapper;
        this.customShoppingCartMapper = customShoppingCartMapper;
    }

    private GoodsIdAndNumber includeGoodsInList(ShoppingCart shoppingCartInDatabase, List<GoodsIdAndNumber> goodsIdAndNumberList) {
        List<GoodsIdAndNumber> goodsSingleList = goodsIdAndNumberList.stream()
                .filter(item -> item.getId() == shoppingCartInDatabase.getGoodsId())
                .map(item -> {
                    if (item.getId() == shoppingCartInDatabase.getGoodsId()) {
                        item.setNumber(item.getNumber() + shoppingCartInDatabase.getNumber());
                    }
                    return item;
                })
                .collect(Collectors.toList());
        if (goodsSingleList.size() <= 0) {
            return null;
        }
        return goodsSingleList.get(0);
    }

    private boolean isGoodsIdNotInList(long goodsId, List<ShoppingCart> shoppingCartList) {
        return shoppingCartList.stream().anyMatch(goods -> goods.getGoodsId() != goodsId);
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

    private Map<Long, List<GoodsWithNumber>> getShopIdMapGoodsWithNumberList(List<ShoppingCart> shoppingCartList) {
        // TODO: stream
        Map<Long, List<GoodsWithNumber>> shopWithGoodsListMap = new ConcurrentHashMap<>();
        for (ShoppingCart shoppingCart : shoppingCartList) {
            List<GoodsWithNumber> goodsWithNumberList = shopWithGoodsListMap.get(shoppingCart.getShopId());
            if (goodsWithNumberList == null) {
                shopWithGoodsListMap.put(shoppingCart.getShopId(), Collections.singletonList(getGoodsWithNumberFromGoodsIdAndNumber(shoppingCart.getGoodsId(), shoppingCart.getNumber())));
            } else {
                goodsWithNumberList.add(getGoodsWithNumberFromGoodsIdAndNumber(shoppingCart.getGoodsId(), shoppingCart.getNumber()));
                shopWithGoodsListMap.put(shoppingCart.getShopId(), goodsWithNumberList);
            }
        }
        return shopWithGoodsListMap;
    }

    public GoodsWithNumber getGoodsWithNumberFromGoodsIdAndNumber(Long goodsId, Integer number) {
        Goods queryGoodsFromDatabase = goodsMapper.selectByPrimaryKey(goodsId);
        if (queryGoodsFromDatabase == null || DataStatus.DELETED.getStatus().equals(queryGoodsFromDatabase.getStatus())) {
            throw HttpException.notFound("商品未找到！goodsId：" + goodsId);
        }

        GoodsWithNumber goodsWithNumber = new GoodsWithNumber(queryGoodsFromDatabase);
        goodsWithNumber.setNumber(number);
        return goodsWithNumber;
    }


    private ShoppingCart convertToShoppingCartFromAddToShoppingCartGoodsAndShopIdAndUserId(GoodsIdAndNumber goodsIdAndNumber, long shopId, long userId) {
        GoodsWithNumber goodsWithNumber = getGoodsWithNumberFromGoodsIdAndNumber(goodsIdAndNumber.getId(), goodsIdAndNumber.getNumber());
        return convertToShoppingCartFromGoodsWithNumber(goodsWithNumber, shopId, userId);
    }

    private List<GoodsWithNumber> getGoodsWithNumberListByShopIdAndUserId(long shopId, long userId) {
        ShoppingCartExample example = new ShoppingCartExample();
        example.createCriteria().andShopIdEqualTo(shopId).andUserIdEqualTo(userId).andStatusEqualTo(DataStatus.OK.getStatus());
        return shoppingCartMapper.selectByExample(example).stream()
                .map(shoppingCart -> getGoodsWithNumberFromGoodsIdAndNumber(shoppingCart.getGoodsId(), shoppingCart.getNumber()))
                .collect(Collectors.toList());
    }

    private ShoppingCartData convertGoodsWithNumberListToShoppingCartResponse(List<GoodsWithNumber> goodsWithNumberList) {
        Shop shop = shopMapper.selectByPrimaryKey(goodsWithNumberList.get(0).getShopId());
        return ShoppingCartData.of(shop, goodsWithNumberList);
    }

    /*
     * 1. delete item by goodsId and userId
     * 2. get ShoppingCartResponse by shopId and userId
     */
    public ShoppingCartData deleteGoodsInShoppingCart(long goodsId, Long userId) {
        ShoppingCartExample example = new ShoppingCartExample();
        example.createCriteria().andGoodsIdEqualTo(goodsId).andUserIdEqualTo(userId).andStatusEqualTo(DataStatus.OK.getStatus());
        List<ShoppingCart> shoppingCartList = shoppingCartMapper.selectByExample(example);
        if (shoppingCartList.isEmpty()) {
            throw HttpException.notFound("商品未找到！goodsId：" + goodsId);
        }
        customShoppingCartMapper.batchDelete(shoppingCartList);

        Long shopId = shoppingCartList.get(0).getShopId();
        Shop shop = shopMapper.selectByPrimaryKey(shopId);

        return ShoppingCartData.of(shop, getGoodsWithNumberListByShopIdAndUserId(shopId, userId));
    }

    /*
     * 如果该商品在购物车中则更新，如果不在则新增
     * 此处认为所有商品来自一个店铺
     *
     * 1. get shoppingCartList by goodsIdList and userId
     * 2. get shop by shopId
     * 3. get pending update list
     * 4. get pending insert list
     * 5. get ShoppingCartResponse by shopId and userId
     */
    public ShoppingCartData addGoodsListToShoppingCart(List<GoodsIdAndNumber> goodsIdAndNumberList, Long userId) {
        // 获取数据库已有的购物车商品
        List<Long> goodsIdList = goodsIdAndNumberList.stream().distinct().map(GoodsIdAndNumber::getId).collect(Collectors.toList());
        ShoppingCartExample goodsIdListExample = new ShoppingCartExample();
        goodsIdListExample.createCriteria().andGoodsIdIn(goodsIdList).andUserIdEqualTo(userId);
        List<ShoppingCart> goodsListOfShoppingCartOfAlreadyInDatabase = shoppingCartMapper.selectByExample(goodsIdListExample);

        long shopId;
        if (goodsListOfShoppingCartOfAlreadyInDatabase.isEmpty()) {
            Goods goods = goodsMapper.selectByPrimaryKey(goodsIdList.get(0));
            if (goods == null || DataStatus.DELETED.getStatus().equals(goods.getStatus())) {
                throw HttpException.notFound("商品未找到！goodsId：" + goodsIdList.get(0));
            }
            shopId = goods.getShopId();

        } else {
            shopId = goodsListOfShoppingCartOfAlreadyInDatabase.get(0).getShopId();
        }

        Shop shop = shopMapper.selectByPrimaryKey(shopId);
        if (shop == null || DataStatus.DELETED.getStatus().equals(shop.getStatus())) {
            throw HttpException.notFound("店铺未找到！shopId：" + shopId);
        }

        // insert
        List<ShoppingCart> shoppingCartListOfPendingInsert = goodsIdAndNumberList.stream()
                .filter(goodsOfPendingInsertToDatabase -> isGoodsIdNotInList(goodsOfPendingInsertToDatabase.getId(), goodsListOfShoppingCartOfAlreadyInDatabase))
                .map(goodsOfPendingInsertToDatabase -> convertToShoppingCartFromAddToShoppingCartGoodsAndShopIdAndUserId(goodsOfPendingInsertToDatabase, shop.getId(), userId))
                .collect(Collectors.toList());

        // update
        for (ShoppingCart existGoods : goodsListOfShoppingCartOfAlreadyInDatabase) {
            existGoods.setNumber(Objects.requireNonNull(includeGoodsInList(existGoods, goodsIdAndNumberList)).getNumber());
        }

        if (!goodsListOfShoppingCartOfAlreadyInDatabase.isEmpty()) {
            customShoppingCartMapper.batchUpdate(goodsListOfShoppingCartOfAlreadyInDatabase);
        }
        if (!shoppingCartListOfPendingInsert.isEmpty()) {
            customShoppingCartMapper.batchInsert(shoppingCartListOfPendingInsert);
        }

        return ShoppingCartData.of(shop, getGoodsWithNumberListByShopIdAndUserId(shopId, userId));
    }


    /*
     * 按照店铺分页
     */
    public ResponseWithPages<List<ShoppingCartData>> getGoodsWithPageFromShoppingCart(int pageNum,
                                                                                      int pageSize, Long userId) {
        // 分页获取所有的 shopId
        List<Long> shopIdList = customShoppingCartMapper.getShopListFromShoppingCartWithPage(userId, (pageNum - 1) * pageSize, pageSize);

        // 获取所有的 shoppingCart
        ShoppingCartExample example = new ShoppingCartExample();
        example.createCriteria().andUserIdEqualTo(userId).andStatusEqualTo(DataStatus.OK.getStatus()).andShopIdIn(shopIdList);
        List<ShoppingCart> shoppingCartList = shoppingCartMapper.selectByExample(example);

        // shoppingCart (shopId, goodsId, number)  => List<ShoppingCartData>
        Map<Long, List<GoodsWithNumber>> shopWithGoodsListMap = getShopIdMapGoodsWithNumberList(shoppingCartList);
        List<ShoppingCartData> shoppingCartResponseList = shopWithGoodsListMap.values().stream()
                .map(this::convertGoodsWithNumberListToShoppingCartResponse)
                .collect(Collectors.toList());

        int total = customShoppingCartMapper.getTotal(userId);
        int totalPage = total % pageSize == 0 ? total / pageSize : total / pageSize + 1;
        return ResponseWithPages.response(pageNum, pageSize, totalPage, shoppingCartResponseList);
    }
}
