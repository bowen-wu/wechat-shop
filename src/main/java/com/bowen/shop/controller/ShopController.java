package com.bowen.shop.controller;

import com.bowen.shop.generate.Shop;
import com.bowen.shop.service.ShopService;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.springframework.beans.factory.annotation.Autowired;
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

@RestController
@RequestMapping("/api/v1")
public class ShopController {
    private ShopService shopService;

    @Autowired
    @SuppressFBWarnings(value = {"EI_EXPOSE_REP", "EI_EXPOSE_REP2"}, justification = "I prefer to suppress these FindBugs warnings")
    public ShopController(ShopService shopService) {
        this.shopService = shopService;
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
     * @api {post} /shop 创建店铺
     * @apiName CreateShop
     * @apiGroup Shop
     *
     * @apiParamExample {json} Request-Example:
     *      {
     *          "name": "My shop",
     *          "description": "Description of my shop",
     *          "imgUrl": "http://img.url"
     *          "status": "ok",
     *      }
     *
     * @apiSuccess (Success 201) {Shop} data 创建的店铺
     * @apiSuccessExample Success-Response:
     *      HTTP/1.1 201 Created
     *      {
     *          "id": 123,
     *          "name": "My shop",
     *          "description": "Description of my shop",
     *          "imgUrl": "http://img.url",
     *          "ownerUserId": 1,
     *          "status": "ok",
     *          "createdAt": "2020-03-22T13:22:03Z",
     *          "updatedAt": "2020-03-22T13:22:03Z"
     *      }
     *
     * @apiUse ErrorResponse
     */
    /**
     * 创建店铺
     *
     * @param shop     店铺信息
     * @param response response
     */
    @PostMapping("/shop")
    public void createShop(@RequestBody Shop shop, HttpServletResponse response) {
    }

    /**
     * @api {delete} /shop/:id 删除店铺
     * @apiName deleteShop
     * @apiGroup Shop
     *
     * @apiParam {Number} id 店铺id
     * @apiSuccess (Success 204) {Shop} data 被删除的店铺
     * @apiSuccessExample Success-Response:
     *      HTTP/1.1 204 No Content
     *      {
     *          "data": {
     *              "id": 123,
     *              "name": "My shop",
     *              "description": "Description of my shop",
     *              "imgUrl": "http://img.url",
     *              "ownerUserId": 1,
     *              "status": "ok",
     *              "createdAt": "2020-03-22T13:22:03Z",
     *              "updatedAt": "2020-03-22T13:22:03Z"
     *          }
     *      }
     *
     * @apiError 403 Forbidden 若用户尝试删除非自己管理的店铺
     * @apiError 404 Not Found 若店铺未找到
     * @apiUse ErrorResponse
     *
     */
    /**
     * 删除店铺
     *
     * @param shopId   店铺ID
     * @param response response
     */
    @DeleteMapping("/shop/{shopId}")
    public void deleteShop(@PathVariable("shopId") Long shopId, HttpServletResponse response) {
    }

    /**
     * @api {patch} /shop 更新店铺
     * @apiName updateShop
     * @apiGroup Shop
     *
     * @apiParamExample {json} Request-Example:
     *          {
     *              "id": 123,
     *              "name": "Update name",
     *              "description": "Update description",
     *              "imgUrl": "http://img.url",
     *              "status": "ok",
     *          }
     *
     * @apiSuccess {Shop} data 更新的店铺
     * @apiSuccessExample Success-Response:
     *      HTTP/1.1 200 OK
     *      {
     *          "data": {
     *              "id": 123,
     *              "name": "Update name",
     *              "description": "Update description",
     *              "imgUrl": "http://img.url",
     *              "status": "ok",
     *              "ownerUserId": 1,
     *              "createdAt": "2020-03-22T13:22:03Z",
     *              "updatedAt": "2020-03-22T13:22:03Z"
     *          }
     *      }
     *
     * @apiError 403 Forbidden 若用户尝试更新非自己管理的店铺
     * @apiError 404 Not Found 若店铺未找到
     * @apiUse ErrorResponse
     */
    /**
     * 更新店铺
     *
     * @param shop     店铺信息
     * @param response response
     */
    @PatchMapping("/shop}")
    public void updateShop(@RequestBody Shop shop, HttpServletResponse response) {
    }

    /**
     * @api {get} /shop/:id 根据店铺 id 获取店铺信息
     * @apiName getShopById
     * @apiGroup Shop
     *
     * @apiParam {Number} id 店铺id
     * @apiSuccess {Goods} data 店铺信息
     * @apiSuccessExample Success-Response:
     *      HTTP/1.1 200 OK
     *      {
     *          "data": {
     *              "id": 123,
     *              "name": "My shop",
     *              "description": "Description of my shop",
     *              "imgUrl": "http://img.url",
     *              "ownerUserId": 1,
     *              "status": "ok",
     *              "createdAt": "2020-03-22T13:22:03Z",
     *              "updatedAt": "2020-03-22T13:22:03Z"
     *          }
     *      }
     *
     * @apiError 404 Not Found 若店铺未找到
     * @apiUse ErrorResponse
     */
    /**
     * 获取指定id的店铺
     *
     * @param shopId   店铺id
     * @param response response
     */
    @GetMapping("/shop/{shopId}")
    public void getShopById(@PathVariable("shopId") Long shopId, HttpServletResponse response) {
    }

    /**
     * @api {get} /shop 获取当前用户名下的所有店铺
     * @apiName getShopList
     * @apiGroup Shop
     *
     * @apiParam {Number} pageNum 页数，从1开始
     * @apiParam {Number} pageSize 每页显示数量
     *
     * @apiSuccess {Number} pageNum 页数，从1开始
     * @apiSuccess {Number} pageSize 每页显示数量
     * @apiSuccess {Number} totalPage 共有多少页
     * @apiSuccess {Shop[]} data 店铺列表
     * @apiSuccessExample Success-Response:
     *      HTTP/1.1 200 OK
     *      {
     *          "pageNum": 1,
     *          "pageSize": 10,
     *          "totalPage": 1,
     *          "data": [
     *              {
     *                  "id": 123,
     *                  "name": "My shop",
     *                  "description": "Description of my shop",
     *                  "imgUrl": "http://img.url",
     *                  "ownerUserId": 1,
     *                  "status": "ok",
     *                  "createdAt": "2020-03-22T13:22:03Z",
     *                  "updatedAt": "2020-03-22T13:22:03Z"
     *              },
     *              {
     *                  ...
     *              }
     *          ]
     *      }
     *
     * @apiUse ErrorResponse
     *
     */
    /**
     * 获取当前用户名下的所有店铺
     *
     * @param pageNum  页码，从1开始
     * @param pageSize 每页显示数量
     * @param response response
     */
    @GetMapping("/shop")
    public void getShopList(@RequestParam("pageNum") int pageNum,
                            @RequestParam(value = "pageSize", required = false) Integer pageSize,
                            HttpServletResponse response) {

    }
}
