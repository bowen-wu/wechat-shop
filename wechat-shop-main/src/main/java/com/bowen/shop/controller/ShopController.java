package com.bowen.shop.controller;

import com.bowen.shop.api.entity.DataStatus;
import com.bowen.shop.api.entity.Pages;
import com.bowen.shop.entity.Response;
import com.bowen.shop.api.entity.ResponseWithPages;
import com.bowen.shop.generate.Shop;
import com.bowen.shop.service.ShopService;
import com.bowen.shop.service.UserContext;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.apache.commons.lang.StringUtils;
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
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class ShopController {
    private final ShopService shopService;

    @Autowired
    @SuppressFBWarnings(value = {"EI_EXPOSE_REP", "EI_EXPOSE_REP2"}, justification = "I prefer to suppress these FindBugs warnings")
    public ShopController(ShopService shopService) {
        this.shopService = shopService;
    }

    public void clean(Shop shop) {
        shop.setId(null);

        if (StringUtils.isBlank(shop.getStatus())) {
            shop.setStatus(DataStatus.OK.getStatus());
        }
        shop.setOwnerUserId(UserContext.getCurrentUser().getId());
        shop.setCreatedAt(new Date());
        shop.setUpdatedAt(new Date());
    }

    public boolean checkShopAttributeInvalid(Shop shop) {
        return StringUtils.isBlank(shop.getName()) || StringUtils.isBlank(shop.getDescription()) || StringUtils.isBlank(shop.getImgUrl());
    }

    /**
     * @apiDefine ErrorResponse
     *
     * @apiError 400 Bad Request ???????????????????????????
     * @apiError 401 Unauthorized ??????????????????
     * @apiErrorExample Error-Response:
     *      HTTP/1.1 401 Unauthorized
     *      {
     *          "message": "Unauthorized"
     *      }
     */

    /**
     * @api {post} /shop ????????????
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
     * @apiSuccess (Success 201) {Shop} data ???????????????
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
     * ????????????
     *
     * @param shop     ????????????
     * @param response response
     * @return ????????????
     */
    @PostMapping("/shop")
    public Response<Shop> createShop(@RequestBody Shop shop, HttpServletResponse response) {
        if (checkShopAttributeInvalid(shop)) {
            response.setStatus(HttpStatus.BAD_REQUEST.value());
            return Response.fail("??????????????????");
        }
        clean(shop);
        response.setStatus(HttpStatus.CREATED.value());
        return Response.success(shopService.createShop(shop));
    }

    /**
     * @api {delete} /shop/:id ????????????
     * @apiName deleteShop
     * @apiGroup Shop
     *
     * @apiParam {Number} id ??????id
     * @apiSuccess (Success 204) {Shop} data ??????????????????
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
     * @apiError 403 Forbidden ?????????????????????????????????????????????
     * @apiError 404 Not Found ??????????????????
     * @apiUse ErrorResponse
     *
     */
    /**
     * ????????????
     *
     * @param shopId   ??????ID
     * @param response response
     * @return ???????????????
     */
    @DeleteMapping("/shop/{shopId}")
    public Response<Shop> deleteShop(@PathVariable("shopId") Long shopId, HttpServletResponse response) {
        response.setStatus(HttpStatus.NO_CONTENT.value());
        return Response.success(shopService.deleteShop(shopId));
    }

    /**
     * @api {patch} /shop ????????????
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
     * @apiSuccess {Shop} data ???????????????
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
     * @apiError 403 Forbidden ?????????????????????????????????????????????
     * @apiError 404 Not Found ??????????????????
     * @apiUse ErrorResponse
     */
    /**
     * ????????????
     *
     * @param shop     ????????????
     * @param response response
     * @return ??????????????????
     */
    @PatchMapping("/shop")
    public Response<Shop> updateShop(@RequestBody Shop shop, HttpServletResponse response) {
        if (shop.getId() == null || checkShopAttributeInvalid(shop)) {
            response.setStatus(HttpStatus.BAD_REQUEST.value());
            return Response.fail("??????????????????");
        }
        return Response.success(shopService.updateShop(shop));
    }

    /**
     * @api {get} /shop/:id ???????????? id ??????????????????
     * @apiName getShopById
     * @apiGroup Shop
     *
     * @apiParam {Number} id ??????id
     * @apiSuccess {Goods} data ????????????
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
     * @apiError 404 Not Found ??????????????????
     * @apiUse ErrorResponse
     */
    /**
     * ????????????id?????????
     *
     * @param shopId   ??????id
     * @return ??????
     */
    @GetMapping("/shop/{shopId}")
    public Response<Shop> getShopById(@PathVariable("shopId") Long shopId) {
        return Response.success(shopService.getShopById(shopId));
    }

    /**
     * @api {get} /shop ???????????????????????????????????????
     * @apiName getShopListWithPage
     * @apiGroup Shop
     *
     * @apiParam {Number} pageNum ????????????1??????
     * @apiParam {Number} pageSize ??????????????????
     *
     * @apiSuccess {Number} pageNum ????????????1??????
     * @apiSuccess {Number} pageSize ??????????????????
     * @apiSuccess {Number} totalPage ???????????????
     * @apiSuccess {Shop[]} data ????????????
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
     * ???????????????????????????????????????
     *
     * @param pageNum  ????????????1??????
     * @param pageSize ??????????????????
     * @return ????????????
     */
    @GetMapping("/shop")
    public ResponseWithPages<List<Shop>> getShopListWithPage(@RequestParam("pageNum") int pageNum,
                                                             @RequestParam(value = "pageSize", required = false) Integer pageSize
    ) {
        int isolatePageSize = pageSize == null ? Pages.DEFAULT_PAGE_SIZE : pageSize;
        return shopService.getShopListWithPage(pageNum, isolatePageSize);
    }
}
