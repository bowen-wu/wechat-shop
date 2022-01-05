package com.bowen.shop.controller;

import com.bowen.shop.entity.HttpException;
import com.bowen.shop.entity.Response;
import com.bowen.shop.generate.Goods;
import com.bowen.shop.service.GoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.util.Date;

@RestController
@RequestMapping("/api/v1")
public class GoodsController {
    private final GoodsService goodsService;

    @Autowired
    public GoodsController(GoodsService goodsService) {
        this.goodsService = goodsService;
    }

    /**
     * @api {post} /goods 创建商品
     * @apiName CreateGoods
     * @apiGroup Goods
     *
     * @apiParamExample {json} Request-Example:
     *          {
     *              "name": "肥皂",  // required
     *              "description": "纯天然无污染肥皂",
     *              "details": "这是一块好肥皂",
     *              "imgUrl": "https://img.url",
     *              "price": 500, // required
     *              "stock": 10,
     *              "shopId": 12345 // required
     *          }
     *
     * @apiSuccess {Goods} data 创建的商品
     * @apiSuccessExample Success-Response:
     *      HTTP/1.1 201 Created
     *      {
     *          "data": {
     *              "id": 12345,
     *              "name": "肥皂",
     *              "description": "纯天然无污染肥皂",
     *              "details": "这是一块好肥皂",
     *              "imgUrl": "https://img.url",
     *              "price": 500,
     *              "stock": 10,
     *              "shopId": 12345,
     *              "createdAt": "2020-03-22T13:22:03Z",
     *              "updatedAt": "2020-03-22T13:22:03Z"
     *          }
     *      }
     * @apiError 400 Bad Request 若用户请求包含错误
     * @apiError 401 Unauthorized 若用户未登录
     * @apiError 403 Forbidden 若用户尝试创建非自己管理店铺的商品
     * @apiError 404 Not Found 若店铺不存在
     *
     * @apiErrorExample Error-Response:
     *      HTTP/1.1 400 Bad Request
     *      {
     *          "message": "Bad Request"
     *      }
     */
    /**
     * 创建商品
     *
     * @param goods    创建的商品
     * @param response response
     * @return 新创建的商品
     */
    @PostMapping("/goods")
    public Response<Goods> createGoods(@RequestBody Goods goods, HttpServletResponse response) {
        if (goods.getName() == null || goods.getPrice() == null || goods.getShopId() == null) {
            response.setStatus(HttpStatus.BAD_REQUEST.value());
            return null;
        }
        clean(goods);
        response.setStatus(HttpStatus.CREATED.value());
        try {
            return Response.of(goodsService.createGoods(goods));
        } catch (HttpException exception) {
            response.setStatus(exception.getStatusCode());
            return null;
        }
    }

    public Goods clean(Goods goods) {
        goods.setId(null);
        goods.setUpdatedAt(new Date());
        goods.setCreatedAt(new Date());
        if (goods.getStock() == null) {
            goods.setStock(0);
        }
        return goods;
    }

    /**
     * @api {delete} /goods/:id 删除商品
     * @apiName deleteGoods
     * @apiGroup Goods
     *
     * @apiParam {Number} id 商品id
     * @apiSuccess {Goods} data 被删除的商品
     * @apiSuccessExample Success-Response:
     *      HTTP/1.1 204 No Content
     *      {
     *          "data": {
     *              "id": 12345,
     *              "name": "肥皂",
     *              "description": "纯天然无污染肥皂",
     *              "details": "这是一块好肥皂",
     *              "imgUrl": "https://img.url",
     *              "price": 500,
     *              "stock": 10,
     *              "shopId": 12345,
     *              "createdAt": "2020-03-22T13:22:03Z",
     *              "updatedAt": "2020-03-22T13:22:03Z"
     *          }
     *      }
     * @apiError 400 Bad Request 若用户请求包含错误
     * @apiError 401 Unauthorized 若用户未登录
     * @apiError 403 Forbidden 若用户尝试创建非自己管理店铺的商品
     * @apiError 404 Not Found 若商品未找到
     *
     * @apiErrorExample Error-Response:
     *      HTTP/1.1 400 Bad Request
     *      {
     *          "message": "Bad Request"
     *      }
     */
    /**
     * 删除商品
     *
     * @param goodsId 待删除商品 ID
     */
    @DeleteMapping("/goods/{goodsId}")
    public void deleteGoods(@PathVariable("goodsId") String goodsId) {
    }

    /**
     * @api {patch} /goods/:id 更新商品
     * @apiName updateGoods
     * @apiGroup Goods
     *
     * @apiParamExample {json} Request-Example:
     *          {
     *              "name": "肥皂",
     *              "description": "纯天然无污染肥皂",
     *              "details": "这是一块好肥皂",
     *              "imgUrl": "https://img.url",
     *              "price": 500,
     *              "stock": 10,
     *              "shopId": 12345
     *          }
     *
     * @apiSuccess {Goods} data 更新的商品
     * @apiSuccessExample Success-Response:
     *      HTTP/1.1 201 Created
     *      {
     *          "data": {
     *              "id": 12345,
     *              "name": "肥皂",
     *              "description": "纯天然无污染肥皂",
     *              "details": "这是一块好肥皂",
     *              "imgUrl": "https://img.url",
     *              "price": 500,
     *              "stock": 10,
     *              "shopId": 12345,
     *              "createdAt": "2020-03-22T13:22:03Z",
     *              "updatedAt": "2020-03-22T13:22:03Z"
     *          }
     *      }
     * @apiError 400 Bad Request 若用户请求包含错误
     * @apiError 401 Unauthorized 若用户未登录
     * @apiError 403 Forbidden 若用户尝试创建非自己管理店铺的商品
     *
     * @apiErrorExample Error-Response:
     *      HTTP/1.1 400 Bad Request
     *      {
     *          "message": "Bad Request"
     *      }
     */
    /**
     * 更新商品
     *
     * @param goodsId 待更新的商品ID
     */
    @PatchMapping("/goods/{goodsId}")
    public void updateGoods(@PathVariable("goodsId") String goodsId) {
    }

    /**
     * @api {get} /goods/:id 根据商品 id 获取商品信息
     * @apiName getGoodsByGoodsId
     * @apiGroup Goods
     *
     * @apiParam {Number} id 商品id
     * @apiSuccess {Goods} data 商品信息
     * @apiSuccessExample Success-Response:
     *      HTTP/1.1 200 OK
     *      {
     *          "data": {
     *              "id": 12345,
     *              "name": "肥皂",
     *              "description": "纯天然无污染肥皂",
     *              "details": "这是一块好肥皂",
     *              "imgUrl": "https://img.url",
     *              "price": 500,
     *              "stock": 10,
     *              "shopId": 12345,
     *              "createdAt": "2020-03-22T13:22:03Z",
     *              "updatedAt": "2020-03-22T13:22:03Z"
     *          }
     *      }
     * @apiError 400 Bad Request 若用户请求包含错误
     * @apiError 401 Unauthorized 若用户未登录
     * @apiError 404 Not Found 若商品未找到
     *
     * @apiErrorExample Error-Response:
     *      HTTP/1.1 400 Bad Request
     *      {
     *          "message": "Bad Request"
     *      }
     */
    /**
     * 根据商品 id 获取商品信息
     *
     * @param goodsId 商品 id
     */
    @GetMapping("/goods/{goodsId}")
    public void getGoodsByGoodsId(@PathVariable("goodsId") String goodsId) {
    }

    /**
     * @api {get} /goods 分页获取商品列表
     * @apiName getGoodsList
     * @apiGroup Goods
     *
     * @apiParam {Number} pageNum 页数，从1开始
     * @apiParam {Number} pageSize 每页显示数量
     * @apiParam {Number} [shopId] 店铺id，若传递，则显示该店铺的商品
     *
     * @apiSuccess {Number} pageNum 页数，从1开始
     * @apiSuccess {Number} pageSize 每页显示数量
     * @apiSuccess {Number} totalPage 共有多少页
     * @apiSuccess {Goods} data 商品列表
     * @apiSuccessExample Success-Response:
     *      HTTP/1.1 200 OK
     *      {
     *          "pageNum": 1,
     *          "pageSize": 10,
     *          "totalPage": 1,
     *          "data": [
     *              {
     *                  "id": 12345,
     *                  "name": "肥皂",
     *                  "description": "纯天然无污染肥皂",
     *                  "details": "这是一块好肥皂",
     *                  "imgUrl": "https://img.url",
     *                  "price": 500,
     *                  "stock": 10,
     *                  "shopId": 12345,
     *                  "createdAt": "2020-03-22T13:22:03Z",
     *                  "updatedAt": "2020-03-22T13:22:03Z"
     *              },
     *              {
     *                  ...
     *              }
     *          ]
     *      }
     * @apiError 400 Bad Request 若用户请求包含错误
     * @apiError 401 Unauthorized 若用户未登录
     *
     * @apiErrorExample Error-Response:
     *      HTTP/1.1 400 Bad Request
     *      {
     *          "message": "Bad Request"
     *      }
     *
     */
    /**
     * 分页获取商品列表
     */
    @GetMapping("/goods")
    public void getGoodsList() {
    }
}
