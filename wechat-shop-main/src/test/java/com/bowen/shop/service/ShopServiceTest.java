package com.bowen.shop.service;

import com.bowen.shop.entity.DataStatus;
import com.bowen.shop.entity.ResponseWithPages;
import com.bowen.shop.generate.Shop;
import com.bowen.shop.generate.ShopMapper;
import com.bowen.shop.generate.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ShopServiceTest {
    @Mock
    ShopMapper mockShopMapper;

    @InjectMocks
    ShopService mockShopService;

    private static final User currentUser = new User();

    @BeforeEach
    void beforeEach() {
        currentUser.setId(1L);
        UserContext.setCurrentUser(currentUser);
    }

    @AfterEach
    void afterEach() {
        UserContext.setCurrentUser(null);
    }

    @Test
    public void testCreateShop() {
        Shop testShop = new Shop();
        Shop createdShop = mockShopService.createShop(testShop);

        verify(mockShopMapper).insert(testShop);
        assertEquals(createdShop, testShop);
    }

    private void assertNotFound(Executable executable) {
        TestHelper.assertHttpException(executable, HttpStatus.NOT_FOUND, "店铺不存在！");
    }

    @Test
    public void returnNotFoundWhenDeleteShop() {
        long deleteShopId = 1L;
        when(mockShopMapper.selectByPrimaryKey(deleteShopId)).thenReturn(null);
        assertNotFound(() -> mockShopService.deleteShop(deleteShopId));

        Shop queryShop = new Shop();
        queryShop.setId(deleteShopId);
        queryShop.setStatus(DataStatus.DELETED.getStatus());

        when(mockShopMapper.selectByPrimaryKey(deleteShopId)).thenReturn(queryShop);
        assertNotFound(() -> mockShopService.deleteShop(deleteShopId));
    }

    @Test
    public void returnForbiddenWhenDeleteShop() {
        long deleteShopId = 1L;

        Shop queryShop = new Shop();
        queryShop.setId(deleteShopId);
        queryShop.setStatus(DataStatus.OK.getStatus());
        queryShop.setOwnerUserId(2L);

        when(mockShopMapper.selectByPrimaryKey(deleteShopId)).thenReturn(queryShop);
        TestHelper.assertHttpException(() -> mockShopService.deleteShop(deleteShopId), HttpStatus.FORBIDDEN, "不能删除非自己管理的店铺！");
    }

    @Test
    public void testDeleteShopSuccess() {
        long deleteShopId = 1L;

        Shop queryShop = new Shop();
        queryShop.setId(deleteShopId);
        queryShop.setStatus(DataStatus.OK.getStatus());
        queryShop.setOwnerUserId(1L);
        when(mockShopMapper.selectByPrimaryKey(deleteShopId)).thenReturn(queryShop);

        Shop deletedShop = mockShopService.deleteShop(deleteShopId);
        verify(mockShopMapper).updateByPrimaryKey(deletedShop);
        assertEquals(deleteShopId, deletedShop.getId());
        assertEquals(DataStatus.DELETED.getStatus(), deletedShop.getStatus());
    }

    @Test
    public void returnNotFoundWhenUpdateShop() {
        long updateShopId = 1L;
        Shop pendingUpdateShop = new Shop();
        pendingUpdateShop.setId(updateShopId);


        when(mockShopMapper.selectByPrimaryKey(updateShopId)).thenReturn(null);
        assertNotFound(() -> mockShopService.updateShop(pendingUpdateShop));

        Shop queryShop = new Shop();
        queryShop.setId(updateShopId);
        queryShop.setStatus(DataStatus.DELETED.getStatus());

        when(mockShopMapper.selectByPrimaryKey(updateShopId)).thenReturn(queryShop);
        assertNotFound(() -> mockShopService.updateShop(pendingUpdateShop));
    }

    @Test
    public void returnForbiddenWhenUpdateShop() {
        long updateShopId = 1L;
        Shop pendingUpdateShop = new Shop();
        pendingUpdateShop.setId(updateShopId);

        Shop queryShop = new Shop();
        queryShop.setId(updateShopId);
        queryShop.setOwnerUserId(2L);

        when(mockShopMapper.selectByPrimaryKey(updateShopId)).thenReturn(queryShop);
        TestHelper.assertHttpException(() -> mockShopService.updateShop(pendingUpdateShop), HttpStatus.FORBIDDEN, "不能更新非自己管理的店铺！");
    }

    @Test
    public void testUpdateSuccess() {
        long updateShopId = 1L;
        Shop pendingUpdateShop = new Shop();
        pendingUpdateShop.setId(updateShopId);
        pendingUpdateShop.setStatus(DataStatus.DELETED.getStatus());

        Shop queryShop = new Shop();
        queryShop.setId(updateShopId);
        queryShop.setStatus(DataStatus.OK.getStatus());
        queryShop.setOwnerUserId(1L);

        when(mockShopMapper.selectByPrimaryKey(updateShopId)).thenReturn(queryShop);

        Shop updatedShop = mockShopService.updateShop(pendingUpdateShop);
        verify(mockShopMapper).updateByPrimaryKey(queryShop);
        assertEquals(updateShopId, updatedShop.getId());
        assertEquals(DataStatus.DELETED.getStatus(), updatedShop.getStatus());
    }

    @Test
    public void returnNotFoundWhenGetShopById() {
        long testShopId = 1L;
        when(mockShopMapper.selectByPrimaryKey(testShopId)).thenReturn(null);
        assertNotFound(() -> mockShopService.getShopById(testShopId));


        Shop queryShop = new Shop();
        queryShop.setId(testShopId);
        queryShop.setStatus(DataStatus.DELETED.getStatus());

        when(mockShopMapper.selectByPrimaryKey(testShopId)).thenReturn(queryShop);
        assertNotFound(() -> mockShopService.getShopById(testShopId));
    }

    @Test
    public void testGetShopByIdSuccess() {
        long testShopId = 1L;

        Shop queryShop = new Shop();
        queryShop.setId(testShopId);
        queryShop.setStatus(DataStatus.OK.getStatus());

        when(mockShopMapper.selectByPrimaryKey(testShopId)).thenReturn(queryShop);
        Shop shopById = mockShopService.getShopById(testShopId);
        assertEquals(queryShop, shopById);
    }

    @Test
    public void testGetShopListWithPageSuccess() {
        int pageNum = 2;
        int pageSize = 10;

        when(mockShopMapper.countByExample(any())).thenReturn(22L);
        List<Shop> queryShopList = new ArrayList<>();
        queryShopList.add(new Shop());

        when(mockShopMapper.selectByExampleWithRowbounds(any(), any())).thenReturn(queryShopList);

        ResponseWithPages<List<Shop>> shopListWithPage = mockShopService.getShopListWithPage(pageNum, pageSize);
        assertEquals(pageNum, shopListWithPage.getPageNum());
        assertEquals(pageSize, shopListWithPage.getPageSize());
        assertEquals(3, shopListWithPage.getTotalPage());
        assertEquals(queryShopList, shopListWithPage.getData());
    }
}
