package com.bowen.shop.service;

import com.bowen.shop.entity.DataStatus;
import com.bowen.shop.entity.HttpException;
import com.bowen.shop.entity.ResponseWithPages;
import com.bowen.shop.generate.Goods;
import com.bowen.shop.generate.GoodsMapper;
import com.bowen.shop.generate.Shop;
import com.bowen.shop.generate.ShopMapper;
import com.bowen.shop.generate.User;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.apache.ibatis.session.RowBounds;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

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
        goodsMapper.insert(goods);
        return goods;
    }

    public Goods deleteGoods(long goodsId) {
        Goods goods = goodsMapper.selectByPrimaryKey(goodsId);
        if (goods == null || DataStatus.DELETED.getStatus().equals(goods.getStatus())) {
            throw HttpException.notFound("商品不存在！");
        }
        Shop shop = shopMapper.selectByPrimaryKey(goods.getShopId());
        if (!shop.getOwnerUserId().equals(UserContext.getCurrentUser().getId())) {
            throw HttpException.forbidden("不能删除非自己店铺的商品！");
        }
        goods.setStatus(DataStatus.DELETED.getStatus());
        goods.setUpdatedAt(new Date());
        goodsMapper.updateByPrimaryKey(goods);
        return goods;
    }

    public Goods updateGoods(Goods goods) {
        Goods queryGoods = goodsMapper.selectByPrimaryKey(goods.getId());
        if (queryGoods == null || DataStatus.DELETED.getStatus().equals(queryGoods.getStatus())) {
            throw HttpException.notFound("商品不存在！");
        }
        Shop shop = shopMapper.selectByPrimaryKey(queryGoods.getShopId());
        if (!shop.getOwnerUserId().equals(UserContext.getCurrentUser().getId())) {
            throw HttpException.forbidden("不能更新非自己店铺的商品！");
        }

        queryGoods.setStock(goods.getStock());
        queryGoods.setStatus(goods.getStatus());
        queryGoods.setImageUrl(goods.getImageUrl());
        queryGoods.setPrice(goods.getPrice());
        queryGoods.setName(goods.getName());
        queryGoods.setDetails(goods.getDetails());
        queryGoods.setDescription(goods.getDescription());
        queryGoods.setUpdatedAt(new Date());
        goodsMapper.updateByPrimaryKey(queryGoods);
        return queryGoods;
    }

    public Goods getGoodsById(Long goodsId) {
        Goods goods = goodsMapper.selectByPrimaryKey(goodsId);
        if (goods == null || DataStatus.DELETED.getStatus().equals(goods.getStatus())) {
            throw HttpException.notFound("商品不存在！");
        }
        return goods;
    }

    public ResponseWithPages<List<Goods>> getGoodsWithPage(int pageNum, int pageSize, Long shopId) {
        GoodsExample goodsExample = new GoodsExample();
        if (shopId == null) {
            goodsExample.createCriteria().andStatusEqualTo(DataStatus.OK.getStatus());
        } else {
            goodsExample.createCriteria().andStatusEqualTo(DataStatus.OK.getStatus()).andShopIdEqualTo(shopId);
        }
        long total = goodsMapper.countByExample(goodsExample);
        int totalPage = (int) (total % pageSize == 0 ? total / pageSize : total / pageSize + 1);
        List<Goods> goodsList = goodsMapper.selectByExampleWithRowbounds(goodsExample,
                new RowBounds((pageNum - 1) * pageSize, pageSize));
        return ResponseWithPages.response(pageNum, pageSize, totalPage, goodsList);
    }
}
