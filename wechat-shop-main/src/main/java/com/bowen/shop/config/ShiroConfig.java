package com.bowen.shop.config;

import com.bowen.shop.service.ShiroRealmService;
import com.bowen.shop.service.UserContext;
import com.bowen.shop.service.UserService;
import com.bowen.shop.service.VerificationCodeCheckService;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.apache.shiro.web.session.mgt.DefaultWebSessionManager;
import org.crazycake.shiro.RedisCacheManager;
import org.crazycake.shiro.RedisManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.util.Arrays;

@Configuration
public class ShiroConfig implements WebMvcConfigurer {
    private final UserService userService;

    @Value("${shop.redis.host}")
    private String redisHost;
    @Value("${shop.redis.port}")
    private String redisPort;

    @Autowired
    public ShiroConfig(UserService userService) {
        this.userService = userService;
    }

    // cors
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        //设置允许跨域的路径
        registry.addMapping("/**")
                //设置允许跨域请求的域名
                //当**Credentials为true时，**Origin不能为星号，需为具体的ip地址【如果接口不带cookie,ip无需设成具体ip】
                .allowedOrigins("http://localhost:3000")
                //是否允许证书 不再默认开启
                .allowCredentials(true)
                //设置允许的方法
                .allowedMethods("*")
                //跨域允许时间
                .maxAge(3600);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new HandlerInterceptor() {
            @Override
            public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
                Object tel = SecurityUtils.getSubject().getPrincipal();
                if (tel != null) {
                    userService.getUserInfoByTel((String) tel).ifPresent(UserContext::setCurrentUser);
                    return true;
                }
                if (Arrays.asList(
                        "/api/v1/code",
                        "/api/v1/login",
                        "/api/v1/status",
                        "/api/v1/testRpc"
                ).contains(request.getRequestURI())) {
                    return true;
                }
                response.setStatus(HttpStatus.UNAUTHORIZED.value());
                return false;
            }

            @Override
            public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
                // 非常非常重要，线程会被复用
                UserContext.clearCurrentUser();
            }
        });
    }

    @Bean
    public DataSourceTransactionManager transactionManager(DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

    @Bean
    public ShiroFilterFactoryBean shiroFilter(SecurityManager securityManager) {
        ShiroFilterFactoryBean shiroFilterFactoryBean = new ShiroFilterFactoryBean();
        shiroFilterFactoryBean.setSecurityManager(securityManager);
        return shiroFilterFactoryBean;
    }

    @Bean
    public RedisCacheManager redisCacheManager() {
        RedisCacheManager redisCacheManager = new RedisCacheManager();
        RedisManager redisManager = new RedisManager();
        redisManager.setHost(redisHost + ":" + redisPort);
        redisCacheManager.setRedisManager(redisManager);
        return redisCacheManager;
    }

    @Bean
    public SecurityManager securityManager(ShiroRealmService shiroRealmService, RedisCacheManager redisCacheManager) {
        DefaultWebSecurityManager securityManager = new DefaultWebSecurityManager();
        securityManager.setRealm(shiroRealmService);
        securityManager.setCacheManager(redisCacheManager);
        securityManager.setSessionManager(new DefaultWebSessionManager());
        SecurityUtils.setSecurityManager(securityManager);
        return securityManager;
    }

    @Bean
    public ShiroRealmService myShiroRealmService(VerificationCodeCheckService verificationCodeCheckService) {
        return new ShiroRealmService(verificationCodeCheckService);
    }

}
