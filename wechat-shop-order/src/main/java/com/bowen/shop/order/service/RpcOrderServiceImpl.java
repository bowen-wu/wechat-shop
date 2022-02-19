package com.bowen.shop.order.service;

import com.bowen.shop.api.entity.DataStatus;
import com.bowen.shop.api.entity.GoodsIdAndNumber;
import com.bowen.shop.api.entity.HttpException;
import com.bowen.shop.api.entity.ResponseWithPages;
import com.bowen.shop.api.entity.RpcOrder;
import com.bowen.shop.api.generate.Order;
import com.bowen.shop.api.generate.OrderExample;
import com.bowen.shop.api.generate.OrderGoods;
import com.bowen.shop.api.generate.OrderGoodsExample;
import com.bowen.shop.api.generate.OrderGoodsMapper;
import com.bowen.shop.api.generate.OrderMapper;
import com.bowen.shop.api.rpc.OrderRpcService;
import com.bowen.shop.order.dao.CustomOrderGoodsMapper;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.apache.dubbo.common.utils.StringUtils;
import org.apache.dubbo.config.annotation.DubboService;
import org.apache.ibatis.session.RowBounds;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BooleanSupplier;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;

@DubboService(version = "${shop.orderService.version}")
public class RpcOrderServiceImpl implements OrderRpcService {
    private final OrderMapper orderMapper;
    private final OrderGoodsMapper orderGoodsMapper;
    private final CustomOrderGoodsMapper customOrderGoodsMapper;

    @Autowired
    @SuppressFBWarnings(value = {"EI_EXPOSE_REP", "EI_EXPOSE_REP2"}, justification = "I prefer to suppress these FindBugs warnings")
    public RpcOrderServiceImpl(OrderMapper orderMapper,
                               OrderGoodsMapper orderGoodsMapper,
                               CustomOrderGoodsMapper customOrderGoodsMapper) {
        this.orderMapper = orderMapper;
        this.orderGoodsMapper = orderGoodsMapper;
        this.customOrderGoodsMapper = customOrderGoodsMapper;
    }

    @Override
    public Order createOrder(List<GoodsIdAndNumber> goodsIdAndNumberList, Order order) {
        try {
            insertOrder(order);
        } catch (IllegalArgumentException e) {
            throw HttpException.badRequest(e.getMessage());
        }
        List<OrderGoods> orderGoodsList = goodsIdAndNumberList.stream()
                .map(goodsIdAndNumber -> convertToOrderGoods(goodsIdAndNumber.getId(), goodsIdAndNumber.getNumber(), order.getId()))
                .collect(toList());
        customOrderGoodsMapper.batchInsert(orderGoodsList);
        return order;
    }

    @Override
    public RpcOrder updateOrder(Order order) {
        Order orderInDB = orderMapper.selectByPrimaryKey(order.getId());
        order.setUpdatedAt(new Date());
        customOrderGoodsMapper.updateOrder(order);
        if (!Objects.equals(order.getStatus(), orderInDB.getStatus())) {
            orderInDB.setStatus(order.getStatus());
        }

        if (!Objects.equals(order.getExpressCompany(), orderInDB.getExpressCompany())) {
            orderInDB.setExpressCompany(order.getExpressCompany());
        }
        if (!Objects.equals(order.getExpressId(), orderInDB.getExpressId())) {
            orderInDB.setExpressId(order.getExpressId());
        }
        return convertOrderToRpcOrder(orderInDB);
    }

    @Override
    public RpcOrder deleteOrder(long orderId, long userId) {
        Order order = orderMapper.selectByPrimaryKey(orderId);
        if (order == null) {
            throw HttpException.notFound("订单ID非法，订单ID：" + orderId);
        }
        if (order.getUserId() != userId) {
            throw HttpException.forbidden("无权访问！");
        }
        OrderGoodsExample orderGoodsExample = new OrderGoodsExample();
        orderGoodsExample.createCriteria().andOrderIdEqualTo(orderId);

        order.setStatus(DataStatus.DELETED.getStatus());
        order.setUpdatedAt(new Date());
        orderMapper.updateByPrimaryKey(order);
        orderGoodsMapper.deleteByExample(orderGoodsExample);
        return convertOrderToRpcOrder(order);
    }

    @Override
    public Order getOrderById(long orderId) {
        return customOrderGoodsMapper.selectByPrimaryKey(orderId);
    }

    @Override
    public ResponseWithPages<List<RpcOrder>> getOrderListWithPageByUserId(int pageNum, int pageSize, DataStatus status, Long userId) {
        OrderExample orderExample = new OrderExample();
        if (status == null) {
            orderExample.createCriteria().andUserIdEqualTo(userId).andStatusNotEqualTo(DataStatus.DELETED.getStatus());
        } else {
            orderExample.createCriteria().andUserIdEqualTo(userId).andStatusEqualTo(status.getStatus());
        }

        List<Order> orderList = orderMapper.selectByExampleWithRowbounds(orderExample, new RowBounds((pageNum - 1) * pageSize, pageSize));
        long total = orderMapper.countByExample(orderExample);
        int totalPage = (int) (total % pageSize == 0 ? total / pageSize : total / pageSize + 1);

        OrderGoodsExample orderGoodsExample = new OrderGoodsExample();
        orderGoodsExample.createCriteria().andOrderIdIn(orderList.stream().map(Order::getId).collect(toList()));
        List<OrderGoods> orderGoodsList = orderGoodsMapper.selectByExample(orderGoodsExample);

        Map<Long, List<GoodsIdAndNumber>> orderIdToGoodsIdAndNumberListMap = orderGoodsList
                .stream()
                .collect(groupingBy(OrderGoods::getOrderId, mapping(orderGoods -> GoodsIdAndNumber.of(orderGoods.getNumber().intValue(), orderGoods.getGoodsId()), toList())));

        List<RpcOrder> rpcOrderList = orderList
                .stream()
                .map(order -> RpcOrder.of(order, orderIdToGoodsIdAndNumberListMap.getOrDefault(order.getId(), Collections.emptyList())))
                .collect(toList());
        return ResponseWithPages.response(pageNum, pageSize, totalPage, rpcOrderList);
    }

    private RpcOrder convertOrderToRpcOrder(Order order) {
        OrderGoodsExample orderGoodsExample = new OrderGoodsExample();
        orderGoodsExample.createCriteria().andOrderIdEqualTo(order.getId());
        List<OrderGoods> orderGoodsList = orderGoodsMapper.selectByExample(orderGoodsExample);
        List<GoodsIdAndNumber> goodsIdAndNumberList = orderGoodsList
                .stream()
                .map(orderGoods -> GoodsIdAndNumber.of(orderGoods.getNumber().intValue(), orderGoods.getGoodsId()))
                .collect(toList());

        return RpcOrder.of(order, goodsIdAndNumberList);
    }

    private OrderGoods convertToOrderGoods(long goodsId, int number, long orderId) {
        OrderGoods orderGoods = new OrderGoods();
        orderGoods.setGoodsId(goodsId);
        orderGoods.setNumber((long) number);
        orderGoods.setOrderId(orderId);
        return orderGoods;
    }

    private void insertOrder(Order order) {
        order.setStatus(DataStatus.PENDING.getStatus());

        verify(() -> StringUtils.isBlank(order.getAddress()), "address 不能为空!");
        verify(() -> order.getTotalPrice() == null || order.getTotalPrice().compareTo(0L) < 0, "totalPrice非法!");
        verify(() -> order.getUserId() == null, "userId 不能为空!");

        order.setCreatedAt(new Date());
        order.setUpdatedAt(new Date());
        order.setExpressId(null);
        order.setExpressCompany(null);
        orderMapper.insert(order);
    }

    private void verify(BooleanSupplier supplier, String message) {
        if (supplier.getAsBoolean()) {
            throw new IllegalArgumentException(message);
        }
    }
}
