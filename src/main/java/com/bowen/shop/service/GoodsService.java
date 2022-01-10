package com.bowen.shop.service;

import com.bowen.shop.entity.DataStatus;
import com.bowen.shop.entity.GoodsPages;
import com.bowen.shop.entity.HttpException;
import com.bowen.shop.entity.ResponseWithPages;
import com.bowen.shop.generate.Goods;
import com.bowen.shop.generate.GoodsExample;
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
        long goodsId = goodsMapper.insert(goods);
        goods.setId(goodsId);
        return goods;
    }

    public Goods deleteGoods(long goodsId) {
        GoodsExample example = new GoodsExample();
        example.createCriteria().andIdEqualTo(goodsId).andStatusEqualTo(DataStatus.OK.getStatus());
        List<Goods> goodsList = goodsMapper.selectByExample(example);
        if (goodsList.size() == 0) {
            throw HttpException.notFound("商品不存在！");
        }
        Goods goods = goodsList.get(0);
        Shop shop = shopMapper.selectByPrimaryKey(goods.getShopId());
        if (!shop.getOwnerUserId().equals(UserContext.getCurrentUser().getId())) {
            throw HttpException.forbidden("不能删除非自己店铺的商品！");
        }
        goods.setStatus(DataStatus.FAIL.getStatus());
        goods.setUpdatedAt(new Date());
        goodsMapper.updateByPrimaryKey(goods);
        return goods;
    }

    public Goods updateGoods(Goods goods) {
        GoodsExample example = new GoodsExample();
        example.createCriteria().andIdEqualTo(goods.getId()).andStatusEqualTo(DataStatus.OK.getStatus());
        List<Goods> goodsList = goodsMapper.selectByExample(example);
        if (goodsList.size() == 0) {
            throw HttpException.notFound("商品不存在！");
        }
        Goods queryGoods = goodsList.get(0);
        Shop shop = shopMapper.selectByPrimaryKey(queryGoods.getShopId());
        if (!shop.getOwnerUserId().equals(UserContext.getCurrentUser().getId())) {
            throw HttpException.forbidden("不能更新非自己店铺的商品！");
        }

        goods.setShopId(queryGoods.getShopId());
        goods.setUpdatedAt(new Date());
        goodsMapper.updateByPrimaryKey(goods);
        return goods;
    }

    public Goods getGoodsById(Long goodsId) {
        GoodsExample example = new GoodsExample();
        example.createCriteria().andIdEqualTo(goodsId).andStatusEqualTo(DataStatus.OK.getStatus());
        List<Goods> goodsList = goodsMapper.selectByExample(example);
        if (goodsList.size() == 0) {
            throw HttpException.notFound("商品不存在！");
        }
        return goodsList.get(0);
    }

    public ResponseWithPages<List<Goods>> getGoodsWithPage(GoodsPages goodsPages) {
        int pageSize = goodsPages.getPageSize();
        int pageNum = goodsPages.getPageNum();
        GoodsExample goodsExample = new GoodsExample();
        if (goodsPages.getShopId() == null) {
            goodsExample.createCriteria().andStatusEqualTo(DataStatus.OK.getStatus());
        } else {
            goodsExample.createCriteria().andStatusEqualTo(DataStatus.OK.getStatus()).andShopIdEqualTo(goodsPages.getShopId());
        }
        long total = goodsMapper.countByExample(goodsExample);
        int totalPage = (int) (total % pageSize == 0 ? total / pageSize : total / pageSize + 1);
        List<Goods> goodsList = goodsMapper.selectByExampleWithRowbounds(goodsExample,
                new RowBounds((pageNum - 1) * pageSize, pageSize));
        return new ResponseWithPages<>(pageNum, pageSize, totalPage, goodsList);
    }
}
