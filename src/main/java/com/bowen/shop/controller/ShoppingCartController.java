package com.bowen.shop.controller;

import com.bowen.shop.entity.AddToShoppingCartGoods;
import com.bowen.shop.entity.HttpException;
import com.bowen.shop.entity.Pages;
import com.bowen.shop.entity.Response;
import com.bowen.shop.entity.ShoppingCartResponse;
import com.bowen.shop.service.ShoppingCartService;
import com.bowen.shop.service.UserContext;
import org.apache.coyote.http11.AbstractHttp11JsseProtocol;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
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
public class ShoppingCartController {

    private ShoppingCartService shoppingCartService;

    @Autowired
    public ShoppingCartController(ShoppingCartService shoppingCartService) {
        this.shoppingCartService = shoppingCartService;
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
     * @api {get} /shoppingCart 分页获取当前用户名下的所有购物车物品
     * @apiName getGoodsWithPageFromShoppingCart
     * @apiGroup ShoppingCart
     *
     * @apiParam {Number} pageNum 页数，从1开始
     * @apiParam {Number} pageSize 每页显示数量
     *
     * @apiSuccess {Number} pageNum 页数，从1开始
     * @apiSuccess {Number} pageSize 每页显示数量
     * @apiSuccess {Number} totalPage 共有多少页
     * @apiSuccess {ShoppingCart[]} data 店铺列表
     *
     * @apiSuccessExample Success-Response:
     *      HTTP/1.1 200 OK
     *      {
     *          "pageNum": 1,
     *          "pageSize": 10,
     *          "totalPage": 2,
     *          "data": [
     *              {
     *                  "shop": {
     *                      "id": 123,
     *                      "name": "My shop",
     *                      "description": "Description of my shop",
     *                      "imgUrl": "http://img.url",
     *                      "ownerUserId": 1,
     *                      "status": "ok",
     *                      "createdAt": "2020-03-22T13:22:03Z",
     *                      "updatedAt": "2020-03-22T13:22:03Z"
     *                  },
     *                  "goods": [
     *                      {
     *                          "number": 1,
     *                          "id": 12345,
     *                          "name": "肥皂",
     *                          "description": "纯天然无污染肥皂",
     *                          "details": "这是一块好肥皂",
     *                          "imgUrl": "https://img.url",
     *                          "price": 500,
     *                          "stock": 10,
     *                          "shopId": 123,
     *                          "createdAt": "2020-03-22T13:22:03Z",
     *                          "updatedAt": "2020-03-22T13:22:03Z"
     *                      },
     *                      {
     *                          ...
     *                      }
     *                  ]
     *              },
     *              {
     *                  ...
     *              }
     *          ]
     *      }
     *
     * @apiUse ErrorResponse
     */
    /**
     * 分页获取当前用户名下的所有购物车物品
     *
     * @param pageNum  当前页码
     * @param pageSize 一页展示多少条数据
     * @param response response
     * @return 购物车物品
     */
    @GetMapping("/shoppingCart")
    public Response<List<ShoppingCartResponse>> getGoodsWithPageFromShoppingCart(@RequestParam("pageNum") int pageNum,
                                                                                 @RequestParam("pageSize") Integer pageSize,
                                                                                 HttpServletResponse response) {
        try {
            return Response.success(shoppingCartService.getGoodsWithPageFromShoppingCart(
                    pageNum,
                    pageSize == null ? Pages.DEFAULT_PAGE_SIZE : pageSize,
                    UserContext.getCurrentUser().getId()));
        } catch (HttpException e) {
            response.setStatus(e.getStatusCode());
            return Response.fail(e.getMessage());
        }
    }

    /**
     * @api {post} /shoppingCart 加商品到购物车中
     * @apiName addGoodsListToShoppingCart
     * @apiGroup ShoppingCart
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
     * @apiSuccess {ShoppingCart} data 更新后的该店铺物品列表
     *
     * @apiSuccessExample Success-Response:
     *      HTTP/1.1 200 OK
     *      {
     *          "data": {
     *              "shop": {
     *                  "id": 123,
     *                  "name": "My shop",
     *                  "description": "Description of my shop",
     *                  "imgUrl": "http://img.url",
     *                  "ownerUserId": 1,
     *                  "status": "ok",
     *                  "createdAt": "2020-03-22T13:22:03Z",
     *                  "updatedAt": "2020-03-22T13:22:03Z"
     *              },
     *              "goods": [
     *                  {
     *                      "number": 1,
     *                      "id": 12345,
     *                      "name": "肥皂",
     *                      "description": "纯天然无污染肥皂",
     *                      "details": "这是一块好肥皂",
     *                      "imgUrl": "https://img.url",
     *                      "price": 500,
     *                      "stock": 10,
     *                      "shopId": 123,
     *                      "createdAt": "2020-03-22T13:22:03Z",
     *                      "updatedAt": "2020-03-22T13:22:03Z"
     *                  },
     *                  {
     *                      ...
     *                  }
     *              ]
     *          }
     *      }
     *
     * @apiError 404 Not Found 若店铺未找到
     * @apiUse ErrorResponse
     */
    /**
     * 加商品到购物车中
     *
     * @param addToShoppingCartGoodsList 加入到购物车的商品列表
     * @param response                   response
     * @return 该店铺在购物车中的所有商品
     */
    @PostMapping("/shoppingCart")
    public Response<ShoppingCartResponse> addGoodsListToShoppingCart(@RequestBody List<AddToShoppingCartGoods> addToShoppingCartGoodsList,
                                                                     HttpServletResponse response) {
        if (addToShoppingCartGoodsList.stream().anyMatch(addToShoppingCartGoods -> addToShoppingCartGoods.getNumber() <= 0)) {
            response.setStatus(HttpStatus.BAD_REQUEST.value());
            return Response.fail("请求参数错误！");
        }
        try {
            return Response.success(shoppingCartService.addGoodsListToShoppingCart(addToShoppingCartGoodsList, UserContext.getCurrentUser().getId()));
        } catch (HttpException e) {
            response.setStatus(e.getStatusCode());
            return Response.fail(e.getMessage());
        }
    }

    /**
     * @api {delete} /shoppingCart/:goodsId 删除当前用户购物车中指定的商品
     * @apiName deleteGoodsInShoppingCart
     * @apiGroup ShoppingCart
     *
     * @apiParam {Number} goodsId 要删除的商品ID
     *
     * @apiSuccess {ShoppingCart} data 更新后的该店铺物品列表
     *
     * @apiSuccessExample Success-Response:
     *      HTTP/1.1 200 OK
     *      {
     *          "data": {
     *              "shop": {
     *                  "id": 123,
     *                  "name": "My shop",
     *                  "description": "Description of my shop",
     *                  "imgUrl": "http://img.url",
     *                  "ownerUserId": 1,
     *                  "status": "ok",
     *                  "createdAt": "2020-03-22T13:22:03Z",
     *                  "updatedAt": "2020-03-22T13:22:03Z"
     *              },
     *              "goods": [
     *                  {
     *                      "number": 1,
     *                      "id": 12345,
     *                      "name": "肥皂",
     *                      "description": "纯天然无污染肥皂",
     *                      "details": "这是一块好肥皂",
     *                      "imgUrl": "https://img.url",
     *                      "price": 500,
     *                      "stock": 10,
     *                      "shopId": 123,
     *                      "createdAt": "2020-03-22T13:22:03Z",
     *                      "updatedAt": "2020-03-22T13:22:03Z"
     *                  },
     *                  {
     *                      ...
     *                  }
     *              ]
     *              }
     *      }
     *
     * @apiError 404 Not Found 若商品没有在购物车中
     * @apiUse ErrorResponse
     */
    /**
     * 删除当前用户购物车中指定的商品
     *
     * @param goodsId  删除商品Id
     * @param response response
     * @return 购物车物品
     */
    @DeleteMapping("/shoppingCart/{goodsId}")
    public Response<ShoppingCartResponse> deleteGoodsInShoppingCart(@PathVariable("goodsId") long goodsId,
                                                                    HttpServletResponse response) {
        try {
            return Response.success(shoppingCartService.deleteGoodsInShoppingCart(goodsId, UserContext.getCurrentUser().getId()));
        } catch (HttpException e) {
            response.setStatus(e.getStatusCode());
            return Response.fail(e.getMessage());
        }
    }

}
