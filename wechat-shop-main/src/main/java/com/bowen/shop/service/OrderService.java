package com.bowen.shop.service;

import com.bowen.shop.api.entity.DataStatus;
import com.bowen.shop.api.entity.GoodsIdAndNumber;
import com.bowen.shop.api.entity.HttpException;
import com.bowen.shop.api.entity.ResponseWithPages;
import com.bowen.shop.api.entity.RpcOrder;
import com.bowen.shop.api.generate.Order;
import com.bowen.shop.api.rpc.OrderRpcService;
import com.bowen.shop.dao.GoodsStockMapper;
import com.bowen.shop.entity.GoodsWithNumber;
import com.bowen.shop.entity.OrderResponse;
import com.bowen.shop.generate.Goods;
import com.bowen.shop.generate.GoodsExample;
import com.bowen.shop.generate.GoodsMapper;
import com.bowen.shop.generate.Shop;
import com.bowen.shop.generate.ShopMapper;
import com.bowen.shop.generate.UserMapper;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.apache.dubbo.config.annotation.DubboReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
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

    @Autowired
    @SuppressFBWarnings(value = {"EI_EXPOSE_REP", "EI_EXPOSE_REP2"}, justification = "I prefer to suppress these FindBugs warnings")
    public OrderService(GoodsMapper goodsMapper,
                        ShopMapper shopMapper,
                        UserMapper userMapper,
                        GoodsStockMapper goodsStockMapper) {
        this.goodsMapper = goodsMapper;
        this.shopMapper = shopMapper;
        this.userMapper = userMapper;
        this.goodsStockMapper = goodsStockMapper;
    }

    /**
     * 扣减库存
     *
     * @param goodsIdAndNumberList 商品ID和数量列表
     */
    @Transactional
    public void deductStock(List<GoodsIdAndNumber> goodsIdAndNumberList) {
        for (GoodsIdAndNumber goodsIdAndNumber : goodsIdAndNumberList) {
            if (goodsStockMapper.deductStock(goodsIdAndNumber) <= 0) {
                LOGGER.error("扣减库存失败，商品ID：" + goodsIdAndNumber.getId() + "，数量：" + goodsIdAndNumber.getNumber());
                throw HttpException.gone("扣减库存失败！");
            }
        }
    }

    private Map<Long, Goods> getIdToGoodsMap(List<Long> goodsIdList) {
        GoodsExample goodsListExample = new GoodsExample();
        goodsListExample.createCriteria().andIdIn(goodsIdList);
        List<Goods> goodsList = goodsMapper.selectByExample(goodsListExample);
        return goodsList.stream().collect(Collectors.toMap(Goods::getId, goods -> goods));
    }

    public OrderResponse placeOrder(List<GoodsIdAndNumber> goodsIdAndNumberList, long userId) {
        Map<Long, Goods> idToGoodsMap = getIdToGoodsMap(goodsIdAndNumberList.stream().distinct().map(GoodsIdAndNumber::getId).collect(toList()));
        Order order = createOrderViaRpc(goodsIdAndNumberList, userId, idToGoodsMap);
        return generateOrderResponse(goodsIdAndNumberList, idToGoodsMap, order);
    }

    private OrderResponse generateOrderResponse(List<GoodsIdAndNumber> goodsIdAndNumberList, Map<Long, Goods> idToGoodsMap, Order order) {
        OrderResponse orderResponse = new OrderResponse(order);

        orderResponse.setShop(shopMapper.selectByPrimaryKey(new ArrayList<>(idToGoodsMap.values()).get(0).getShopId()));
        orderResponse.setGoods(goodsIdAndNumberList.stream()
                .map(goodsIdAndNumber -> getGoodsWithNumber(idToGoodsMap, goodsIdAndNumber))
                .collect(toList()));
        return orderResponse;
    }

    private Order createOrderViaRpc(List<GoodsIdAndNumber> goodsIdAndNumberList, long userId, Map<Long, Goods> idToGoodsMap) {
        Order order = new Order();
        Goods goods = goodsMapper.selectByPrimaryKey(goodsIdAndNumberList.get(0).getId());
        order.setShopId(goods.getShopId());
        order.setAddress(userMapper.selectByPrimaryKey(userId).getAddress());
        order.setTotalPrice(goodsIdAndNumberList.stream()
                .map(goodsIdAndNumber -> calculateItemTotalPrice(idToGoodsMap, goodsIdAndNumber))
                .reduce(0L, Math::addExact));
        order.setUserId(userId);
        return orderRpcService.createOrder(goodsIdAndNumberList, order);
    }

    private GoodsWithNumber getGoodsWithNumber(Map<Long, Goods> idToGoodsMap, GoodsIdAndNumber goodsIdAndNumber) {
        GoodsWithNumber goodsWithNumber = new GoodsWithNumber(idToGoodsMap.get(goodsIdAndNumber.getId()));
        goodsWithNumber.setNumber(goodsIdAndNumber.getNumber());
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

    public OrderResponse deleteOrder(long orderId, long userId) {
        RpcOrder rpcOrder = orderRpcService.deleteOrder(orderId, userId);
        return convertRpcOrderToOrderResponse(rpcOrder);
    }

    private OrderResponse convertRpcOrderToOrderResponse(RpcOrder rpcOrder) {
        Map<Long, Goods> idToGoodsMap = getIdToGoodsMap(rpcOrder.getGoodsIdAndNumberList().stream().map(GoodsIdAndNumber::getId).collect(toList()));
        return generateOrderResponse(rpcOrder.getGoodsIdAndNumberList(), idToGoodsMap, rpcOrder);
    }

    public ResponseWithPages<List<OrderResponse>> getOrderListWithPageByUserId(int pageNum, int pageSize, DataStatus status, Long userId) {
        ResponseWithPages<List<RpcOrder>> orderListResponseWithPages = orderRpcService.getOrderListWithPageByUserId(pageNum, pageSize, status, userId);
        if (orderListResponseWithPages.getData().isEmpty()) {
            return ResponseWithPages.response(pageNum, pageSize, orderListResponseWithPages.getTotalPage(), Collections.emptyList());
        }

        List<Long> goodsIdList = orderListResponseWithPages
                .getData()
                .stream()
                .map(RpcOrder::getGoodsIdAndNumberList)
                .flatMap(Collection::stream)
                .map(GoodsIdAndNumber::getId)
                .distinct()
                .collect(toList());

        Map<Long, Goods> idToGoodsMap = getIdToGoodsMap(goodsIdList);

        List<OrderResponse> orderResponseList = orderListResponseWithPages
                .getData()
                .stream()
                .map(rpcOrder -> generateOrderResponse(rpcOrder.getGoodsIdAndNumberList(), idToGoodsMap, rpcOrder))
                .collect(toList());

        return ResponseWithPages.response(pageNum, pageSize, orderListResponseWithPages.getTotalPage(), orderResponseList);
    }

    public OrderResponse updateExpressInformation(Order order, long userId) {
        checkOrderIsValid(order, userId);

        Order pendingUpdateOrder = new Order();
        pendingUpdateOrder.setId(order.getId());
        pendingUpdateOrder.setExpressId(order.getExpressId());
        pendingUpdateOrder.setExpressCompany(order.getExpressCompany());
        return convertRpcOrderToOrderResponse(orderRpcService.updateOrder(pendingUpdateOrder));
    }

    private void checkOrderIsValid(Order order, long userId) {
        Order orderById = orderRpcService.getOrderById(order.getId());
        if (orderById == null || DataStatus.DELETED.getStatus().equals(orderById.getStatus())) {
            throw HttpException.notFound("订单未找到，orderId：" + order.getId());
        }
        Shop shop = shopMapper.selectByPrimaryKey(orderById.getShopId());
        if (shop.getOwnerUserId() != userId) {
            throw HttpException.forbidden("无权访问！");
        }
    }

    public OrderResponse updateOrderStatus(Order order, long userId) {
        checkOrderIsValid(order, userId);

        Order pendingUpdateOrder = new Order();
        pendingUpdateOrder.setStatus(order.getStatus());
        return convertRpcOrderToOrderResponse(orderRpcService.updateOrder(pendingUpdateOrder));
    }
}
