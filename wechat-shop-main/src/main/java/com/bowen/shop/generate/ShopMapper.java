package com.bowen.shop.generate;

import com.bowen.shop.generate.Shop;
import com.bowen.shop.generate.ShopExample;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.session.RowBounds;

@Mapper
public interface ShopMapper {
    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table SHOP
     *
     * @mbg.generated Thu Jan 27 15:25:19 CST 2022
     */
    long countByExample(ShopExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table SHOP
     *
     * @mbg.generated Thu Jan 27 15:25:19 CST 2022
     */
    int deleteByExample(ShopExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table SHOP
     *
     * @mbg.generated Thu Jan 27 15:25:19 CST 2022
     */
    int deleteByPrimaryKey(Long id);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table SHOP
     *
     * @mbg.generated Thu Jan 27 15:25:19 CST 2022
     */
    int insert(Shop record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table SHOP
     *
     * @mbg.generated Thu Jan 27 15:25:19 CST 2022
     */
    int insertSelective(Shop record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table SHOP
     *
     * @mbg.generated Thu Jan 27 15:25:19 CST 2022
     */
    List<Shop> selectByExampleWithRowbounds(ShopExample example, RowBounds rowBounds);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table SHOP
     *
     * @mbg.generated Thu Jan 27 15:25:19 CST 2022
     */
    List<Shop> selectByExample(ShopExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table SHOP
     *
     * @mbg.generated Thu Jan 27 15:25:19 CST 2022
     */
    Shop selectByPrimaryKey(Long id);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table SHOP
     *
     * @mbg.generated Thu Jan 27 15:25:19 CST 2022
     */
    int updateByExampleSelective(@Param("record") Shop record, @Param("example") ShopExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table SHOP
     *
     * @mbg.generated Thu Jan 27 15:25:19 CST 2022
     */
    int updateByExample(@Param("record") Shop record, @Param("example") ShopExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table SHOP
     *
     * @mbg.generated Thu Jan 27 15:25:19 CST 2022
     */
    int updateByPrimaryKeySelective(Shop record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table SHOP
     *
     * @mbg.generated Thu Jan 27 15:25:19 CST 2022
     */
    int updateByPrimaryKey(Shop record);
}