package com.bowen.shop.controller;

import com.bowen.shop.api.entity.DataStatus;
import com.bowen.shop.api.entity.GoodsIdAndNumber;
import com.bowen.shop.api.entity.HttpException;
import com.bowen.shop.api.entity.Pages;
import com.bowen.shop.api.entity.ResponseWithPages;
import com.bowen.shop.api.generate.Order;
import com.bowen.shop.entity.OrderResponse;
import com.bowen.shop.entity.Response;
import com.bowen.shop.service.OrderService;
import com.bowen.shop.service.UserContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class OrderController {
    private OrderService orderService;

    @Autowired
    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    /**
     * @apiDefine ErrorResponse
     *
     * @apiError 400 Bad Request 若用户请求包含错误
     * @apiError 401 Unauthorized 若用户未登录
     * @apiErrorExample Error-Response:
     *      HTTP/1.1 401 Unauthorized
     *      {
     *          "message": "Unauthorized"
     *      }
     */

    /**
     * @api {post} /order 下订单
     * @apiName placeOrder
     * @apiGroup Order
     *
     * @apiParamExample {json} Request-Example:
     *          {
     *              goods: [
     *                  {
     *                      "id": 123,
     *                      "number": 2,
     *                  },
     *                  {
     *                      ...
     *                  }
     *              ]
     *          }
     *
     * @apiSuccess {Order} data 创建的订单
     *
     * @apiSuccessExample Success-Response
     *      HTTP/1.1 201 Created
     *      {
     *        "data": {
     *            "id": 12345,
     *            "expressCompany": null,
     *            "expressId": null,
     *            "status": "pending",
     *            "address": "XXX",
     *            "shop": {
     *               "id": 12345,
     *               "name": "我的店铺",
     *               "description": "我的苹果专卖店",
     *               "imgUrl": "https://img.url",
     *               "ownerUserId": 12345,
     *               "createdAt": "2020-03-22T13:22:03Z",
     *               "updatedAt": "2020-03-22T13:22:03Z"
     *             },
     *             "goods": [
     *               {
     *                   "id": 12345,
     *                   "name": "肥皂",
     *                   "description": "纯天然无污染肥皂",
     *                   "details": "这是一块好肥皂",
     *                   "imgUrl": "https://img.url",
     *                   "address": "XXX",
     *                   "price": 500,
     *                   "number": 10,
     *                   "createdAt": "2020-03-22T13:22:03Z",
     *                   "updatedAt": "2020-03-22T13:22:03Z"
     *               },
     *               {
     *                     ...
     *               }
     *            ]
     *          }
     *      }
     *
     * @apiError 404 Not Found 若商品未找到
     * @apiUse ErrorResponse
     */
    /**
     * 下订单
     *
     * @param goodsIdAndNumberList 商品列表
     * @return 订单信息
     */
    @PostMapping("/order")
    public Response<OrderResponse> placeOrder(@RequestBody List<GoodsIdAndNumber> goodsIdAndNumberList) {
        orderService.deductStock(goodsIdAndNumberList);
        OrderResponse orderResponse = orderService.placeOrder(goodsIdAndNumberList, UserContext.getCurrentUser().getId());
        return Response.success(orderResponse);
    }

    /**
     * @api {delete} /order/:orderId 删除订单
     * @apiName deleteOrderById
     * @apiGroup Order
     *
     * @apiParam {Number} orderId 要删除的订单ID
     *
     * @apiSuccess {Order} data 删除的订单
     *
     * @apiSuccessExample Success-Response
     *      HTTP/1.1 204 No Content
     *      {
     *        "data": {
     *            "id": 12345,
     *            "expressCompany": null,
     *            "expressId": null,
     *            "status": "pending",
     *            "address": "XXX",
     *            "shop": {
     *               "id": 12345,
     *               "name": "我的店铺",
     *               "description": "我的苹果专卖店",
     *               "imgUrl": "https://img.url",
     *               "ownerUserId": 12345,
     *               "createdAt": "2020-03-22T13:22:03Z",
     *               "updatedAt": "2020-03-22T13:22:03Z"
     *             },
     *             "goods": [
     *               {
     *                   "id": 12345,
     *                   "name": "肥皂",
     *                   "description": "纯天然无污染肥皂",
     *                   "details": "这是一块好肥皂",
     *                   "imgUrl": "https://img.url",
     *                   "address": "XXX",
     *                   "price": 500,
     *                   "number": 10,
     *                   "createdAt": "2020-03-22T13:22:03Z",
     *                   "updatedAt": "2020-03-22T13:22:03Z"
     *               },
     *               {
     *                     ...
     *               }
     *            ]
     *          }
     *      }
     *
     * @apiError 403 Forbidden 若用户删除非自己的订单
     * @apiError 404 Not Found 若订单未找到
     * @apiUse ErrorResponse
     */
    /**
     * 删除订单
     *
     * @param orderId 订单ID
     */
    @DeleteMapping("/order/{orderId}")
    public Response<OrderResponse> deleteOrderById(@PathVariable("orderId") long orderId) {
        return Response.success(orderService.deleteOrder(orderId, UserContext.getCurrentUser().getId()));
    }

    /**
     * @api {patch} /order 更新订单(只能更新物流信息/签收状态)
     * @apiName updateOrderInfo
     * @apiGroup Order
     *
     * @apiParamExample {json} Request-Example:
     *      {
     *          "id": 12345,
     *          "expressCompany": "圆通",
     *          "expressId": "YTO1234",
     *      }
     *      {
     *          "id": 12345,
     *          "status": "RECEIVED"
     *      }
     *
     * @apiSuccess {Order} data 更新后的订单
     *
     * @apiSuccessExample Success-Response
     *      HTTP/1.1 200 OK
     *      {
     *        "data": {
     *            "id": 12345,
     *            "expressCompany": null,
     *            "expressId": null,
     *            "status": "pending",
     *            "address": "XXX",
     *            "shop": {
     *               "id": 12345,
     *               "name": "我的店铺",
     *               "description": "我的苹果专卖店",
     *               "imgUrl": "https://img.url",
     *               "ownerUserId": 12345,
     *               "createdAt": "2020-03-22T13:22:03Z",
     *               "updatedAt": "2020-03-22T13:22:03Z"
     *             },
     *             "goods": [
     *               {
     *                   "id": 12345,
     *                   "name": "肥皂",
     *                   "description": "纯天然无污染肥皂",
     *                   "details": "这是一块好肥皂",
     *                   "imgUrl": "https://img.url",
     *                   "address": "XXX",
     *                   "price": 500,
     *                   "number": 10,
     *                   "createdAt": "2020-03-22T13:22:03Z",
     *                   "updatedAt": "2020-03-22T13:22:03Z"
     *               },
     *               {
     *                     ...
     *               }
     *            ]
     *          }
     *      }
     *
     * @apiError 403 Forbidden 若用户删除非自己的订单
     * @apiError 404 Not Found 若订单未找到
     * @apiUse ErrorResponse
     */
    /**
     * 更新订单
     *
     * @param orderInfo 订单更新的信息
     * @param response  response
     */
    @PatchMapping("/order")
    public Response<OrderResponse> updateOrderInfo(@RequestBody Order order, HttpServletResponse response) {
        if (order.getId() == null) {
            response.setStatus(HttpStatus.BAD_REQUEST.value());
            return Response.fail("非法 orderId" + order.getId());
        }
        if (order.getExpressCompany() != null && order.getExpressId() != null) {
            return Response.success(orderService.updateExpressInformation(order, UserContext.getCurrentUser().getId()));
        } else {
            return Response.success(orderService.updateOrderStatus(order, UserContext.getCurrentUser().getId()));
        }
    }

    /**
     * @api {get} /order 获取当前用户名下的订单(分页)
     * @apiName getOrderListByUserId
     * @apiGroup Order
     *
     * @apiParam {Number} pageNum 页数，从1开始
     * @apiParam {Number} pageSize 每页显示数量
     * @apiParam {String=pending/paid/delivered/received} [status] 订单状态: PENDING 等付款 PAID 已付款 DELIVERED 物流中 RECEIVED 已收货
     *
     * @apiSuccess {Number} pageNum 页数，从1开始
     * @apiSuccess {Number} pageSize 每页显示数量
     * @apiSuccess {Number} totalPage 共有多少页
     * @apiSuccess {Order[]} data 订单列表
     *
     * @apiSuccessExample Success-Response
     *      HTTP/1.1 200 OK
     *      {
     *        "pageNum": 1,
     *        "pageSize": 10,
     *        "totalPage": 5,
     *        "data": [
     *           {
     *            "id": 12345,
     *            "expressCompany": null,
     *            "expressId": null,
     *            "status": "pending",
     *            "totalPrice": 10000,
     *            "address": "XXX",
     *            "shop": {
     *               "id": 12345,
     *               "name": "我的店铺",
     *               "description": "我的苹果专卖店",
     *               "imgUrl": "https://img.url",
     *               "ownerUserId": 12345,
     *               "createdAt": "2020-03-22T13:22:03Z",
     *               "updatedAt": "2020-03-22T13:22:03Z"
     *             },
     *             "goods": [
     *               {
     *                   "id": 12345,
     *                   "name": "肥皂",
     *                   "description": "纯天然无污染肥皂",
     *                   "details": "这是一块好肥皂",
     *                   "imgUrl": "https://img.url",
     *                   "address": "XXX",
     *                   "price": 500,
     *                   "number": 10,
     *                   "createdAt": "2020-03-22T13:22:03Z",
     *                   "updatedAt": "2020-03-22T13:22:03Z"
     *               },
     *               {
     *                     ...
     *               }
     *            ]
     *          },
     *          {
     *               ...
     *          }
     *        ]
     *      }
     *
     * @apiUse ErrorResponse
     */
    /**
     * 获取当前用户名下的订单(分页)
     *
     * @param pageNum  当前页码
     * @param pageSize 一页展示多少条数据
     * @param status   订单状态
     * @return response
     */
    @GetMapping("/order")
    public ResponseWithPages<List<OrderResponse>> getOrderListByUserId(@RequestParam("pageNum") int pageNum,
                                                                       @RequestParam("pageSize") Integer pageSize,
                                                                       @RequestParam(value = "status", required = false) String status) {
        if (status != null && DataStatus.fromStatus(status) == null) {
            throw HttpException.badRequest("非法 status：" + status);
        }
        int isolatePageSize = pageSize == null ? Pages.DEFAULT_PAGE_SIZE : pageSize;
        return orderService.getOrderListWithPageByUserId(pageNum, isolatePageSize, DataStatus.fromStatus(status), UserContext.getCurrentUser().getId());
    }

}
