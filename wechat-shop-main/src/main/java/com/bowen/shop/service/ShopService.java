package com.bowen.shop.service;

import com.bowen.shop.api.entity.DataStatus;
import com.bowen.shop.entity.HttpException;
import com.bowen.shop.entity.ResponseWithPages;
import com.bowen.shop.generate.Shop;
import com.bowen.shop.generate.ShopExample;
import com.bowen.shop.generate.ShopMapper;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.apache.ibatis.session.RowBounds;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class ShopService {
    private final ShopMapper shopMapper;
    private static final Logger LOGGER = LoggerFactory.getLogger(ShopService.class);

    @Autowired
    @SuppressFBWarnings(value = {"EI_EXPOSE_REP", "EI_EXPOSE_REP2"}, justification = "I prefer to suppress these FindBugs warnings")
    public ShopService(ShopMapper shopMapper) {
        this.shopMapper = shopMapper;
    }

    public Shop createShop(Shop shop) {
        shopMapper.insert(shop);
        return shop;
    }

    private void checkShopIsNotFound(Shop shop) {
        if (shop == null || DataStatus.DELETED.getStatus().equals(shop.getStatus())) {
            LOGGER.warn("Not Found Shop");
            throw HttpException.notFound("店铺不存在！");
        }
    }

    private void checkShopIsForbidden(Shop shop, String errorMessage) {
        if (!shop.getOwnerUserId().equals(UserContext.getCurrentUser().getId())) {
            LOGGER.warn("Forbidden {}", shop.getId());
            throw HttpException.forbidden(errorMessage);
        }
    }

    public Shop deleteShop(Long shopId) {
        LOGGER.info("Delete shop, shopId is " + shopId.toString().replaceAll("[\r\n]", ""));
        Shop shop = shopMapper.selectByPrimaryKey(shopId);
        checkShopIsNotFound(shop);
        checkShopIsForbidden(shop, "不能删除非自己管理的店铺！");
        shop.setStatus(DataStatus.DELETED.getStatus());
        shop.setUpdatedAt(new Date());
        shopMapper.updateByPrimaryKey(shop);
        return shop;
    }

    public Shop updateShop(Shop shop) {
        Shop queryShop = shopMapper.selectByPrimaryKey(shop.getId());
        checkShopIsNotFound(queryShop);
        checkShopIsForbidden(queryShop, "不能更新非自己管理的店铺！");
        queryShop.setUpdatedAt(new Date());
        queryShop.setStatus(shop.getStatus());
        queryShop.setDescription(shop.getDescription());
        queryShop.setImgUrl(shop.getImgUrl());
        queryShop.setName(shop.getName());
        shopMapper.updateByPrimaryKey(queryShop);
        return queryShop;
    }

    public Shop getShopById(Long shopId) {
        Shop shop = shopMapper.selectByPrimaryKey(shopId);
        checkShopIsNotFound(shop);
        return shop;
    }

    public ResponseWithPages<List<Shop>> getShopListWithPage(int pageNum, int pageSize) {
        ShopExample example = new ShopExample();
        example.createCriteria().andOwnerUserIdEqualTo(UserContext.getCurrentUser().getId()).andStatusEqualTo(DataStatus.OK.getStatus());
        long total = shopMapper.countByExample(example);
        int totalPage = (int) (total % pageSize == 0 ? total / pageSize : total / pageSize + 1);
        List<Shop> shops = shopMapper.selectByExampleWithRowbounds(example, new RowBounds((pageNum - 1) * pageSize, pageSize));
        return ResponseWithPages.response(pageNum, pageSize, totalPage, shops);
    }
}
