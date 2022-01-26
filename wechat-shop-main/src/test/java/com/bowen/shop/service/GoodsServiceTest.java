package com.bowen.shop.service;

import com.bowen.shop.entity.DataStatus;
import com.bowen.shop.entity.HttpException;
import com.bowen.shop.entity.ResponseWithPages;
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

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// TODO: template mode
@ExtendWith(MockitoExtension.class)
class GoodsServiceTest {
    @Mock
    GoodsMapper mockGoodsMapper;
    @Mock
    ShopMapper mockShopMapper;
    @InjectMocks
    GoodsService mockGoodsService;

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
        when(mockShopMapper.selectByPrimaryKey(1L)).thenReturn(null);

        HttpException httpException = assertThrows(HttpException.class, () -> {
            mockGoodsService.createGoods(testGoods);
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
        when(mockShopMapper.selectByPrimaryKey(1L)).thenReturn(testShop);

        HttpException httpException = assertThrows(HttpException.class, () -> {
            mockGoodsService.createGoods(testGoods);
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
        when(mockShopMapper.selectByPrimaryKey(1L)).thenReturn(testShop);

        Goods goods = mockGoodsService.createGoods(testGoods);
        assertEquals(DataStatus.OK.getStatus(), goods.getStatus());
    }

    @Test
    public void returnNotFoundWhenDeleteGoods() {
        long testGoodsId = 1L;
        when(mockGoodsMapper.selectByPrimaryKey(testGoodsId)).thenReturn(null);

        HttpException httpException = assertThrows(HttpException.class, () -> {
            mockGoodsService.deleteGoods(testGoodsId);
        });

        assertEquals(HttpStatus.NOT_FOUND.value(), httpException.getStatusCode());
        assertEquals("商品不存在！", httpException.getMessage());

        Goods testGoods = new Goods();
        testGoods.setStatus(DataStatus.DELETED.getStatus());
        when(mockGoodsMapper.selectByPrimaryKey(testGoodsId)).thenReturn(testGoods);

        httpException = assertThrows(HttpException.class, () -> {
            mockGoodsService.deleteGoods(testGoodsId);
        });

        assertEquals(HttpStatus.NOT_FOUND.value(), httpException.getStatusCode());
        assertEquals("商品不存在！", httpException.getMessage());
    }

    @Test
    public void returnForbiddenWhenDeleteGoods() {
        long testShopId = 2L;
        long testGoodsId = 1L;
        Goods testGoods = new Goods();
        testGoods.setShopId(testShopId);
        Shop testShop = new Shop();
        testShop.setOwnerUserId(2L);
        when(mockGoodsMapper.selectByPrimaryKey(testGoodsId)).thenReturn(testGoods);
        when(mockShopMapper.selectByPrimaryKey(testShopId)).thenReturn(testShop);

        HttpException httpException = assertThrows(HttpException.class, () -> {
            mockGoodsService.deleteGoods(testGoodsId);
        });

        assertEquals(HttpStatus.FORBIDDEN.value(), httpException.getStatusCode());
        assertEquals("不能删除非自己店铺的商品！", httpException.getMessage());
    }

    @Test
    public void deleteGoodsSuccess() {
        long testShopId = 2L;
        long testGoodsId = 1L;
        Goods testGoods = new Goods();
        testGoods.setShopId(testShopId);
        Shop testShop = new Shop();
        testShop.setOwnerUserId(3L);
        when(mockGoodsMapper.selectByPrimaryKey(testGoodsId)).thenReturn(testGoods);
        when(mockShopMapper.selectByPrimaryKey(testShopId)).thenReturn(testShop);

        Goods goods = mockGoodsService.deleteGoods(testGoodsId);
        verify(mockGoodsMapper).updateByPrimaryKey(testGoods);
        assertEquals(DataStatus.DELETED.getStatus(), goods.getStatus());
    }

    @Test
    public void returnNotFoundWhenUpdateGoods() {
        Goods testGoods = new Goods();
        testGoods.setId(1L);
        when(mockGoodsMapper.selectByPrimaryKey(any())).thenReturn(null);

        HttpException httpException = assertThrows(HttpException.class, () -> {
            mockGoodsService.updateGoods(testGoods);
        });

        assertEquals(HttpStatus.NOT_FOUND.value(), httpException.getStatusCode());
        assertEquals("商品不存在！", httpException.getMessage());

        testGoods.setStatus(DataStatus.DELETED.getStatus());
        when(mockGoodsMapper.selectByPrimaryKey(any())).thenReturn(testGoods);

        httpException = assertThrows(HttpException.class, () -> {
            mockGoodsService.updateGoods(testGoods);
        });

        assertEquals(HttpStatus.NOT_FOUND.value(), httpException.getStatusCode());
        assertEquals("商品不存在！", httpException.getMessage());
    }

    @Test
    public void returnForbiddenWhenUpdateGoods() {
        long testShopId = 2L;
        long testGoodsId = 1L;

        Goods testGoods = new Goods();
        testGoods.setId(testGoodsId);
        testGoods.setShopId(testShopId);

        // 当前登录 userId 3
        Shop testShop = new Shop();
        testShop.setOwnerUserId(2L);

        when(mockGoodsMapper.selectByPrimaryKey(testGoodsId)).thenReturn(testGoods);
        when(mockShopMapper.selectByPrimaryKey(testShopId)).thenReturn(testShop);

        HttpException httpException = assertThrows(HttpException.class, () -> {
            mockGoodsService.updateGoods(testGoods);
        });

        assertEquals(HttpStatus.FORBIDDEN.value(), httpException.getStatusCode());
        assertEquals("不能更新非自己店铺的商品！", httpException.getMessage());
    }

    @Test
    public void updateGoodsSuccess() {
        long testShopId = 2L;
        long testGoodsId = 1L;

        Goods testGoods = new Goods();
        testGoods.setId(testGoodsId);
        testGoods.setShopId(testShopId);

        Shop testShop = new Shop();
        testShop.setOwnerUserId(3L);

        when(mockGoodsMapper.selectByPrimaryKey(testGoodsId)).thenReturn(testGoods);
        when(mockShopMapper.selectByPrimaryKey(testShopId)).thenReturn(testShop);

        Goods goods = mockGoodsService.updateGoods(testGoods);

        assertEquals(testGoods.getId(), goods.getId());
        assertEquals(testGoods.getShopId(), goods.getShopId());
    }

    @Test
    public void returnNotFoundWhenGetGoodsById() {
        long testGoodsId = 1L;
        when(mockGoodsMapper.selectByPrimaryKey(testGoodsId)).thenReturn(null);

        HttpException httpException = assertThrows(HttpException.class, () -> {
            mockGoodsService.getGoodsById(testGoodsId);
        });

        assertEquals(HttpStatus.NOT_FOUND.value(), httpException.getStatusCode());
        assertEquals("商品不存在！", httpException.getMessage());

        Goods testGoods = new Goods();
        testGoods.setStatus(DataStatus.DELETED.getStatus());
        when(mockGoodsMapper.selectByPrimaryKey(testGoodsId)).thenReturn(testGoods);

        httpException = assertThrows(HttpException.class, () -> {
            mockGoodsService.getGoodsById(testGoodsId);
        });

        assertEquals(HttpStatus.NOT_FOUND.value(), httpException.getStatusCode());
        assertEquals("商品不存在！", httpException.getMessage());
    }

    @Test
    public void getGoodsByIdSuccess() {
        long testGoodsId = 1L;
        Goods testGoods = new Goods();
        testGoods.setId(testGoodsId);
        when(mockGoodsMapper.selectByPrimaryKey(testGoodsId)).thenReturn(testGoods);

        Goods goods = mockGoodsService.getGoodsById(testGoodsId);

        assertEquals(testGoodsId, goods.getId());
    }

    @Test
    public void getGoodsListSuccess() {
        long testGoodsId = 1L;
        Goods testGoods = new Goods();
        testGoods.setId(testGoodsId);
        List<Goods> testGoodsList = new ArrayList<>();
        testGoodsList.add(testGoods);
        when(mockGoodsMapper.countByExample(any())).thenReturn(47L);
        when(mockGoodsMapper.selectByExampleWithRowbounds(any(), any())).thenReturn(testGoodsList);

        ResponseWithPages<List<Goods>> goodsWithPage = mockGoodsService.getGoodsWithPage(1, 20, null);

        assertEquals(3, goodsWithPage.getTotalPage());
        assertEquals(1, goodsWithPage.getPageNum());
        assertEquals(20, goodsWithPage.getPageSize());
        assertEquals(1, goodsWithPage.getData().size());
        assertEquals(testGoodsId, goodsWithPage.getData().get(0).getId());

        when(mockGoodsMapper.countByExample(any())).thenReturn(80L);
        when(mockGoodsMapper.selectByExampleWithRowbounds(any(), any())).thenReturn(testGoodsList);

        goodsWithPage = mockGoodsService.getGoodsWithPage(1, 20, 1L);

        assertEquals(4, goodsWithPage.getTotalPage());
    }
}
