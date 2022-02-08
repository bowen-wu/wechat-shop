package com.bowen.shop.service;

import com.bowen.shop.api.entity.GoodsIdAndNumber;
import com.bowen.shop.api.generate.Order;
import com.bowen.shop.api.rpc.OrderRpcService;
import com.bowen.shop.dao.GoodsStockMapper;
import com.bowen.shop.entity.GoodsWithNumber;
import com.bowen.shop.entity.HttpException;
import com.bowen.shop.entity.OrderResponse;
import com.bowen.shop.generate.Goods;
import com.bowen.shop.generate.GoodsExample;
import com.bowen.shop.generate.GoodsMapper;
import com.bowen.shop.generate.ShopMapper;
import com.bowen.shop.generate.UserMapper;
import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

@Service
public class OrderService {
    private static final Logger LOGGER = LoggerFactory.getLogger(OrderService.class);
    @DubboReference(version = "${shop.orderService.version}", url = "${shop.orderService.url}")
    private OrderRpcService orderRpcService;
    private final GoodsMapper goodsMapper;
    private final ShopMapper shopMapper;
    private final UserMapper userMapper;
    private final GoodsStockMapper goodsStockMapper;
    private final SqlSessionFactory sqlSessionFactory;

    @Autowired
    public OrderService(GoodsMapper goodsMapper,
                        ShopMapper shopMapper,
                        UserMapper userMapper,
                        GoodsStockMapper goodsStockMapper,
                        SqlSessionFactory sqlSessionFactory) {
        this.goodsMapper = goodsMapper;
        this.shopMapper = shopMapper;
        this.userMapper = userMapper;
        this.goodsStockMapper = goodsStockMapper;
        this.sqlSessionFactory = sqlSessionFactory;
    }

    /**
     * 扣减库存
     *
     * @param goodsIdAndNumberList 商品ID和数量列表
     * @return 若扣减成功返回 true，否则返回 false
     */
    private boolean deductStock(List<GoodsIdAndNumber> goodsIdAndNumberList) {
        try (SqlSession sqlSession = sqlSessionFactory.openSession(false)) {
            for (GoodsIdAndNumber goodsIdAndNumber : goodsIdAndNumberList) {
                if (goodsStockMapper.deductStock(goodsIdAndNumber) <= 0) {
                    LOGGER.error("扣减库存失败，商品ID：" + goodsIdAndNumber.getId() + "，数量：" + goodsIdAndNumber.getNumber());
                    sqlSession.rollback();
                    return false;
                }
            }
            sqlSession.commit();
            return true;
        }
    }

    private Map<Long, Goods> getIdToGoodsMap(List<Long> goodsIdList) {
        GoodsExample goodsListExample = new GoodsExample();
        goodsListExample.createCriteria().andIdIn(goodsIdList);
        List<Goods> goodsList = goodsMapper.selectByExample(goodsListExample);
        return goodsList.stream().collect(Collectors.toMap(Goods::getId, goods -> goods));
    }

    public OrderResponse placeOrder(List<GoodsIdAndNumber> goodsIdAndNumberList, long userId) {
        if (!deductStock(goodsIdAndNumberList)) {
            throw HttpException.gone("扣减库存失败!");
        }

        Map<Long, Goods> idToGoodsMap = getIdToGoodsMap(goodsIdAndNumberList.stream().distinct().map(GoodsIdAndNumber::getId).collect(toList()));
        Order order = createOrderViaRpc(goodsIdAndNumberList, userId, idToGoodsMap);
        return generateOrderResponse(goodsIdAndNumberList, idToGoodsMap, order);
    }

    private OrderResponse generateOrderResponse(List<GoodsIdAndNumber> goodsIdAndNumberList, Map<Long, Goods> idToGoodsMap, Order order) {
        OrderResponse orderResponse = new OrderResponse(order);

        orderResponse.setShop(shopMapper.selectByPrimaryKey(new ArrayList<>(idToGoodsMap.values()).get(0).getShopId()));
        orderResponse.setGoodsList(goodsIdAndNumberList.stream()
                .map(goodsIdAndNumber -> getGoodsWithNumber(idToGoodsMap, goodsIdAndNumber))
                .collect(toList()));
        return orderResponse;
    }

    private Order createOrderViaRpc(List<GoodsIdAndNumber> goodsIdAndNumberList, long userId, Map<Long, Goods> idToGoodsMap) {
        Order order = new Order();
        order.setAddress(userMapper.selectByPrimaryKey(userId).getAddress());
        order.setTotalPrice(goodsIdAndNumberList.stream()
                .map(goodsIdAndNumber -> calculateItemTotalPrice(idToGoodsMap, goodsIdAndNumber))
                .reduce(0, Math::addExact));
        order.setUserId(userId);
        return orderRpcService.createOrder(goodsIdAndNumberList, order);
    }

    private GoodsWithNumber getGoodsWithNumber(Map<Long, Goods> idToGoodsMap, GoodsIdAndNumber goodsIdAndNumber) {
        GoodsWithNumber goodsWithNumber = new GoodsWithNumber(idToGoodsMap.get(goodsIdAndNumber.getId()));
        goodsWithNumber.setNumber(goodsWithNumber.getNumber());
        return goodsWithNumber;
    }

    private long calculateItemTotalPrice(Map<Long, Goods> idToGoodsMap, GoodsIdAndNumber goodsIdAndNumber) {
        Goods goods = idToGoodsMap.get(goodsIdAndNumber.getId());
        if (goods == null) {
            throw HttpException.badRequest("goods id非法：" + goodsIdAndNumber.getId());
        }
        if (goodsIdAndNumber.getNumber() <= 0) {
            throw HttpException.badRequest("数量非法：" + goodsIdAndNumber.getNumber());
        }
        return goods.getPrice() * goodsIdAndNumber.getNumber();
    }

}
