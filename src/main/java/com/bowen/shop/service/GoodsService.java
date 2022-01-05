package com.bowen.shop.service;

import com.bowen.shop.entity.DataStatus;
import com.bowen.shop.entity.HttpException;
import com.bowen.shop.generate.Goods;
import com.bowen.shop.generate.GoodsMapper;
import com.bowen.shop.generate.Shop;
import com.bowen.shop.generate.ShopMapper;
import com.bowen.shop.generate.User;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class GoodsService {
    private final GoodsMapper goodsMapper;
    private final ShopMapper shopMapper;

    @Autowired
    @SuppressFBWarnings(value = {"EI_EXPOSE_REP", "EI_EXPOSE_REP2"}, justification = "I prefer to suppress these FindBugs warnings")
    public GoodsService(GoodsMapper goodsMapper, ShopMapper shopMapper) {
        this.goodsMapper = goodsMapper;
        this.shopMapper = shopMapper;
    }

    public Goods createGoods(Goods goods) {
        User user = UserContext.getCurrentUser();
        Shop shop = shopMapper.selectByPrimaryKey(goods.getShopId());
        if (shop == null) {
            throw HttpException.notFound("店铺不存在！");
        }
        if (!shop.getOwnerUserId().equals(user.getId())) {
            throw HttpException.forbidden("不能创建非自己店铺的商品！");
        }
        goods.setStatus(DataStatus.OK.getStatus());
        long goodsId = goodsMapper.insert(goods);
        goods.setId(goodsId);
        return goods;
    }
}
