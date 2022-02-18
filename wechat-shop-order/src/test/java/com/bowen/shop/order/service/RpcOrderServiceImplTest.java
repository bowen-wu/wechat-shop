package com.bowen.shop.order.service;

import com.bowen.shop.api.generate.OrderGoodsMapper;
import com.bowen.shop.api.generate.OrderMapper;
import com.bowen.shop.order.dao.CustomOrderGoodsMapper;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.configuration.ClassicConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import java.io.IOException;
import java.io.InputStream;

// TODO: integrationTest Or unitTest
// 不启动 Spring，通过读取 Mybatis 配置，创建 SqlSession 实例，之后创建 rpcOrderService
class RpcOrderServiceImplTest {
    String databaseUrl = "jdbc:mysql://localhost:3307/order";
    String databaseUsername = "root";
    String databasePassword = "my-secret-pw";

    RpcOrderServiceImpl rpcOrderService;

    SqlSession sqlSession;

    @BeforeEach
    public void setUpDatabase() throws IOException {
        ClassicConfiguration configuration = new ClassicConfiguration();
        configuration.setDataSource(databaseUrl, databaseUsername, databasePassword);
        Flyway flyway = new Flyway(configuration);
        flyway.clean();
        flyway.migrate();

        // 读取 Mybatis 配置，创建 SqlSession 实例
        String resource = "db/config.xml";
        InputStream inputStream = Resources.getResourceAsStream(resource);
        SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
        sqlSession = sqlSessionFactory.openSession(true);

        rpcOrderService = new RpcOrderServiceImpl(
                sqlSession.getMapper(OrderMapper.class),
                sqlSession.getMapper(OrderGoodsMapper.class),
                sqlSession.getMapper(CustomOrderGoodsMapper.class)
        );
    }

    @AfterEach
    public void cleanUp() {
        sqlSession.close();
    }
}
