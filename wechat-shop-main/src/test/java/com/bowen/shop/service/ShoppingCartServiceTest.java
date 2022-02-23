package com.bowen.shop.service;

import com.bowen.shop.api.entity.DataStatus;
import com.bowen.shop.api.entity.GoodsIdAndNumber;
import com.bowen.shop.api.entity.ResponseWithPages;
import com.bowen.shop.dao.CustomShoppingCartMapper;
import com.bowen.shop.entity.GoodsWithNumber;
import com.bowen.shop.entity.ShoppingCartData;
import com.bowen.shop.generate.Goods;
import com.bowen.shop.generate.GoodsMapper;
import com.bowen.shop.generate.Shop;
import com.bowen.shop.generate.ShopMapper;
import com.bowen.shop.generate.ShoppingCart;
import com.bowen.shop.generate.ShoppingCartMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ShoppingCartServiceTest {
    @Mock
    GoodsMapper goodsMapper;
    @Mock
    ShopMapper shopMapper;
    @Mock
    ShoppingCartMapper shoppingCartMapper;
    @Mock
    CustomShoppingCartMapper customShoppingCartMapper;

    @InjectMocks
    ShoppingCartService shoppingCartService;

    @Test
    public void returnNotFoundWhenDeleteGoodsInShoppingCart() {
        when(shoppingCartMapper.selectByExample(any())).thenReturn(Collections.emptyList());

        TestHelper.assertHttpException(() -> shoppingCartService.deleteGoodsInShoppingCart(1, 1L), HttpStatus.NOT_FOUND, "商品未找到！goodsId：1");
    }

    @Test
    public void testDeleteGoodsInShoppingCartSuccess() {
        long testShopId = 2L;
        long testGoodsId = 1L;
        Shop testShop = new Shop();
        testShop.setId(testShopId);
        Goods testGoods = new Goods();
        testGoods.setId(testGoodsId);
        testGoods.setShopId(testShopId);
        List<ShoppingCart> testShoppingCartList = new ArrayList<>();
        ShoppingCart testShoppingCart = new ShoppingCart();
        testShoppingCart.setShopId(testShopId);
        testShoppingCart.setNumber(10);
        testShoppingCart.setGoodsId(testGoodsId);
        testShoppingCartList.add(testShoppingCart);

        when(shoppingCartMapper.selectByExample(any())).thenReturn(testShoppingCartList);
        when(shopMapper.selectByPrimaryKey(testShopId)).thenReturn(testShop);
        when(goodsMapper.selectByExample(any())).thenReturn(Collections.singletonList(testGoods));

        ShoppingCartData shoppingCartData = shoppingCartService.deleteGoodsInShoppingCart(1, 1L);
        verify(customShoppingCartMapper).batchDelete(testShoppingCartList);
        assertEquals(testShop, shoppingCartData.getShop());
        assertEquals(1, shoppingCartData.getGoods().size());
        assertEquals(testShopId, shoppingCartData.getGoods().get(0).getShopId());
        assertEquals(10, shoppingCartData.getGoods().get(0).getNumber());
    }

    @Test
    public void returnNotFoundWhenAddGoodsListToShoppingCart() {
        long testShopId = 2L;
        List<GoodsIdAndNumber> testGoodsIdAndNumberList = new ArrayList<>();
        GoodsIdAndNumber goodsIdAndNumber1 = new GoodsIdAndNumber(2, 1L);
        GoodsIdAndNumber goodsIdAndNumber2 = new GoodsIdAndNumber(3, 2L);
        testGoodsIdAndNumberList.add(goodsIdAndNumber1);
        testGoodsIdAndNumberList.add(goodsIdAndNumber2);

        List<ShoppingCart> goodsListOfShoppingCartOfAlreadyInDatabase = new ArrayList<>();
        ShoppingCart testShoppingCart = new ShoppingCart();
        testShoppingCart.setGoodsId(1L);
        testShoppingCart.setNumber(1);
        testShoppingCart.setShopId(testShopId);
        goodsListOfShoppingCartOfAlreadyInDatabase.add(testShoppingCart);

        when(shoppingCartMapper.selectByExample(any())).thenReturn(goodsListOfShoppingCartOfAlreadyInDatabase);
        when(shopMapper.selectByPrimaryKey(testShopId)).thenReturn(null);

        TestHelper.assertHttpException(
                () -> shoppingCartService.addGoodsListToShoppingCart(testGoodsIdAndNumberList, 1L),
                HttpStatus.NOT_FOUND,
                "店铺未找到！shopId：2");

        Shop testShop = new Shop();
        testShop.setId(testShopId);
        testShop.setStatus(DataStatus.DELETED.getStatus());
        when(shopMapper.selectByPrimaryKey(testShopId)).thenReturn(testShop);

        TestHelper.assertHttpException(
                () -> shoppingCartService.addGoodsListToShoppingCart(testGoodsIdAndNumberList, 1L),
                HttpStatus.NOT_FOUND,
                "店铺未找到！shopId：2");

        testShop.setStatus(DataStatus.OK.getStatus());
        when(shopMapper.selectByPrimaryKey(testShopId)).thenReturn(testShop);
        when(goodsMapper.selectByPrimaryKey(2L)).thenReturn(null);

        TestHelper.assertHttpException(
                () -> shoppingCartService.addGoodsListToShoppingCart(testGoodsIdAndNumberList, 1L),
                HttpStatus.NOT_FOUND,
                "商品未找到！goodsId：2");

        Goods testGoods = new Goods();
        testGoods.setId(2L);
        testGoods.setStatus(DataStatus.DELETED.getStatus());
        when(goodsMapper.selectByPrimaryKey(2L)).thenReturn(testGoods);
        TestHelper.assertHttpException(
                () -> shoppingCartService.addGoodsListToShoppingCart(testGoodsIdAndNumberList, 1L),
                HttpStatus.NOT_FOUND,
                "商品未找到！goodsId：2");
    }

    @Test
    public void testAddGoodsListToShoppingCartSuccess() {
        long testShopId = 2L;

        List<ShoppingCart> goodsListOfShoppingCartOfAlreadyInDatabase = new ArrayList<>();
        ShoppingCart testShoppingCart = new ShoppingCart();
        testShoppingCart.setGoodsId(1L);
        testShoppingCart.setNumber(1);
        testShoppingCart.setShopId(testShopId);
        goodsListOfShoppingCartOfAlreadyInDatabase.add(testShoppingCart);
        when(shoppingCartMapper.selectByExample(any())).thenReturn(goodsListOfShoppingCartOfAlreadyInDatabase);

        Shop testShop = new Shop();
        testShop.setId(testShopId);
        testShop.setStatus(DataStatus.OK.getStatus());
        when(shopMapper.selectByPrimaryKey(testShopId)).thenReturn(testShop);

        Goods testGoods2 = new Goods();
        testGoods2.setId(2L);
        testGoods2.setStatus(DataStatus.OK.getStatus());
        when(goodsMapper.selectByPrimaryKey(2L)).thenReturn(testGoods2);
        Goods testGoods1 = new Goods();
        testGoods1.setId(1L);
        testGoods1.setStatus(DataStatus.OK.getStatus());
        when(goodsMapper.selectByExample(any())).thenReturn(Arrays.asList(testGoods1, testGoods2));

        List<GoodsIdAndNumber> testGoodsIdAndNumberList = new ArrayList<>();
        GoodsIdAndNumber goodsIdAndNumber1 = new GoodsIdAndNumber(2, 1L);
        GoodsIdAndNumber goodsIdAndNumber2 = new GoodsIdAndNumber(3, 2L);
        testGoodsIdAndNumberList.add(goodsIdAndNumber1);
        testGoodsIdAndNumberList.add(goodsIdAndNumber2);

        ShoppingCartData shoppingCartData = shoppingCartService.addGoodsListToShoppingCart(testGoodsIdAndNumberList, 1L);

        // TODO: verify customShoppingCartMapper.batchInsert has been called
        verify(customShoppingCartMapper).batchUpdate(goodsListOfShoppingCartOfAlreadyInDatabase);
        assertEquals(testShopId, shoppingCartData.getShop().getId());
        assertEquals(1, shoppingCartData.getGoods().size());
        assertEquals(Collections.singletonList(1L), shoppingCartData.getGoods().stream().map(GoodsWithNumber::getId).collect(Collectors.toList()));
        assertEquals(Collections.singletonList(3), shoppingCartData.getGoods().stream().map(GoodsWithNumber::getNumber).collect(Collectors.toList()));
    }

    @Test
    public void testGetGoodsWithPageFromShoppingCart() {
        long testShopId = 2L;
        Shop testShop = new Shop();
        testShop.setId(testShopId);
        List<ShoppingCart> testShoppingCartList = new ArrayList<>();
        ShoppingCart testShoppingCart = new ShoppingCart();
        testShoppingCart.setShopId(testShopId);
        testShoppingCart.setNumber(10);
        testShoppingCart.setGoodsId(3L);
        testShoppingCartList.add(testShoppingCart);
        List<Long> testShopIdList = Collections.singletonList(testShopId);
        Goods testGoods = new Goods();
        testGoods.setId(3L);
        testGoods.setShopId(testShopId);
        testGoods.setStatus(DataStatus.OK.getStatus());

        when(shopMapper.selectByPrimaryKey(any())).thenReturn(testShop);
        when(goodsMapper.selectByExample(any())).thenReturn(Collections.singletonList(testGoods));
        when(shoppingCartMapper.selectByExample(any())).thenReturn(testShoppingCartList);
        when(customShoppingCartMapper.getTotal(1L)).thenReturn(8);
        when(customShoppingCartMapper.getShopListFromShoppingCartWithPage(1L, 0, 3)).thenReturn(testShopIdList);

        ResponseWithPages<List<ShoppingCartData>> goodsWithPageFromShoppingCart = shoppingCartService
                .getGoodsWithPageFromShoppingCart(1, 3, 1L);

        assertEquals(1, goodsWithPageFromShoppingCart.getPageNum());
        assertEquals(3, goodsWithPageFromShoppingCart.getPageSize());
        assertEquals(3, goodsWithPageFromShoppingCart.getTotalPage());
        assertEquals(testShopId, goodsWithPageFromShoppingCart.getData().get(0).getShop().getId());
        assertEquals(testShopId, goodsWithPageFromShoppingCart.getData().get(0).getGoods().get(0).getShopId());
        assertEquals(10, goodsWithPageFromShoppingCart.getData().get(0).getGoods().get(0).getNumber());
    }
}
