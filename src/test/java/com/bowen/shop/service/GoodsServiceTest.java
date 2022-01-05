package com.bowen.shop.service;

import com.bowen.shop.entity.DataStatus;
import com.bowen.shop.entity.HttpException;
import com.bowen.shop.generate.Goods;
import com.bowen.shop.generate.GoodsMapper;
import com.bowen.shop.generate.Shop;
import com.bowen.shop.generate.ShopMapper;
import com.bowen.shop.generate.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GoodsServiceTest {
    @Mock
    GoodsMapper goodsMapper;
    @Mock
    ShopMapper shopMapper;
    @InjectMocks
    GoodsService goodsService;

    private static final User currentUser = new User();

    @BeforeEach
    void beforeEach() {
        currentUser.setId(3L);
        UserContext.setCurrentUser(currentUser);
    }

    @AfterEach
    void afterEach() {
        UserContext.setCurrentUser(null);
    }

    @Test
    public void returnNotFoundWhenCreateGoods() {
        Goods testGoods = new Goods();
        testGoods.setShopId(1L);
        when(shopMapper.selectByPrimaryKey(1L)).thenReturn(null);

        HttpException httpException = assertThrows(HttpException.class, () -> {
            goodsService.createGoods(testGoods);
        });

        assertEquals(HttpStatus.NOT_FOUND.value(), httpException.getStatusCode());
        assertEquals("店铺不存在！", httpException.getMessage());
    }

    @Test
    public void returnForbiddenWhenCreateGoods() {
        Goods testGoods = new Goods();
        testGoods.setShopId(1L);
        Shop testShop = new Shop();
        testShop.setOwnerUserId(2L);
        when(shopMapper.selectByPrimaryKey(1L)).thenReturn(testShop);

        HttpException httpException = assertThrows(HttpException.class, () -> {
            goodsService.createGoods(testGoods);
        });

        assertEquals(HttpStatus.FORBIDDEN.value(), httpException.getStatusCode());
        assertEquals("不能创建非自己店铺的商品！", httpException.getMessage());
    }

    @Test
    public void testCreateGoodsSuccess() {
        Goods testGoods = new Goods();
        testGoods.setShopId(1L);
        Shop testShop = new Shop();
        testShop.setOwnerUserId(3L);
        when(shopMapper.selectByPrimaryKey(1L)).thenReturn(testShop);
        when(goodsMapper.insert(testGoods)).thenReturn(1);

        Goods goods = goodsService.createGoods(testGoods);
        assertEquals(DataStatus.OK.getStatus(), goods.getStatus());
        assertEquals(1, goods.getId());
    }
}
