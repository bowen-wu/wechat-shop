package com.bowen.shop.api.generate;

import com.bowen.shop.api.generate.OrderGoods;
import com.bowen.shop.api.generate.OrderGoodsExample;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.session.RowBounds;

@Mapper
public interface OrderGoodsMapper {
    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table ORDER_GOODS
     *
     * @mbg.generated Tue Feb 08 19:59:25 CST 2022
     */
    long countByExample(OrderGoodsExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table ORDER_GOODS
     *
     * @mbg.generated Tue Feb 08 19:59:25 CST 2022
     */
    int deleteByExample(OrderGoodsExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table ORDER_GOODS
     *
     * @mbg.generated Tue Feb 08 19:59:25 CST 2022
     */
    int deleteByPrimaryKey(Long id);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table ORDER_GOODS
     *
     * @mbg.generated Tue Feb 08 19:59:25 CST 2022
     */
    int insert(OrderGoods record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table ORDER_GOODS
     *
     * @mbg.generated Tue Feb 08 19:59:25 CST 2022
     */
    int insertSelective(OrderGoods record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table ORDER_GOODS
     *
     * @mbg.generated Tue Feb 08 19:59:25 CST 2022
     */
    List<OrderGoods> selectByExampleWithRowbounds(OrderGoodsExample example, RowBounds rowBounds);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table ORDER_GOODS
     *
     * @mbg.generated Tue Feb 08 19:59:25 CST 2022
     */
    List<OrderGoods> selectByExample(OrderGoodsExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table ORDER_GOODS
     *
     * @mbg.generated Tue Feb 08 19:59:25 CST 2022
     */
    OrderGoods selectByPrimaryKey(Long id);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table ORDER_GOODS
     *
     * @mbg.generated Tue Feb 08 19:59:25 CST 2022
     */
    int updateByExampleSelective(@Param("record") OrderGoods record, @Param("example") OrderGoodsExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table ORDER_GOODS
     *
     * @mbg.generated Tue Feb 08 19:59:25 CST 2022
     */
    int updateByExample(@Param("record") OrderGoods record, @Param("example") OrderGoodsExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table ORDER_GOODS
     *
     * @mbg.generated Tue Feb 08 19:59:25 CST 2022
     */
    int updateByPrimaryKeySelective(OrderGoods record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table ORDER_GOODS
     *
     * @mbg.generated Tue Feb 08 19:59:25 CST 2022
     */
    int updateByPrimaryKey(OrderGoods record);
}