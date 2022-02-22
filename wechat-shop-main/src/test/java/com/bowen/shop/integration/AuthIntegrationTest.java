package com.bowen.shop.integration;

import com.bowen.shop.WechatShopApplication;
import com.bowen.shop.entity.LoginResponse;
import com.bowen.shop.entity.TelAndCode;
import com.bowen.shop.service.TelVerificationServiceTest;
import org.apache.hc.client5.http.cookie.BasicCookieStore;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.Method;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static java.net.HttpURLConnection.HTTP_BAD_REQUEST;
import static java.net.HttpURLConnection.HTTP_MOVED_TEMP;
import static java.net.HttpURLConnection.HTTP_OK;
import static java.net.HttpURLConnection.HTTP_UNAUTHORIZED;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = WechatShopApplication.class, webEnvironment = WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {"spring.config.location=classpath:test-application.yml"})
public class AuthIntegrationTest extends AbstractIntegrationTest {
    public void return401WhenAccessDenied() throws Exception {
        try (CloseableHttpClient httpclient = HttpClients.custom().build()) {
            final ClassicHttpRequest sendSmsCode = createRequestBuilder(Method.POST, "/api/v1/any", TelVerificationServiceTest.VALID_PARAMETER);
            try (CloseableHttpResponse response = httpclient.execute(sendSmsCode)) {
                assertEquals(HTTP_UNAUTHORIZED, response.getCode());
            }
        }
    }

    @Test
    public void returnHttpOKWhenParameterIsCorrect() throws Exception {
        try (CloseableHttpClient httpclient = HttpClients.custom().build()) {
            final ClassicHttpRequest sendSmsCode = createRequestBuilder(Method.POST, "/api/v1/code", TelVerificationServiceTest.VALID_PARAMETER);
            try (CloseableHttpResponse response = httpclient.execute(sendSmsCode)) {
                assertEquals(HTTP_OK, response.getCode());
            }
        }
    }

    @Test
    public void returnHttpBadRequestWhenParameterIsCorrect() throws Exception {
        try (CloseableHttpClient httpclient = HttpClients.custom().build()) {
            final ClassicHttpRequest sendSmsCode = createRequestBuilder(Method.POST, "/api/v1/code", TelVerificationServiceTest.INVALID_PARAMETER);
            try (CloseableHttpResponse response = httpclient.execute(sendSmsCode)) {
                assertEquals(HTTP_BAD_REQUEST, response.getCode());
            }
        }
    }

    @Test
    public void testLoginProcess() throws Exception {
        final BasicCookieStore cookieStore = new BasicCookieStore();
        try (CloseableHttpClient httpclient = HttpClients.custom()
                .setDefaultCookieStore(cookieStore)
                .build()) {
            // 0. check login user info = false
            testUserInfoWhenNotLogin(httpclient);

            // 1. send sms code
            ClassicHttpRequest sendSmsCode = createRequestBuilder(Method.POST, "/api/v1/code", TelVerificationServiceTest.VALID_PARAMETER);
            try (CloseableHttpResponse response = httpclient.execute(sendSmsCode)) {
                assertEquals(HTTP_OK, response.getCode());
            }

            // 2. login => cookie
            ClassicHttpRequest login = createRequestBuilder(
                    Method.POST,
                    "/api/v1/login",
                    new TelAndCode(TelVerificationServiceTest.VALID_PARAMETER.getTel(), "000000"));
            try (CloseableHttpResponse response = httpclient.execute(login)) {
                assertEquals(HTTP_OK, response.getCode());
            }

            // 3. check login user info = true
            testUserInfoWhenLogged(httpclient);

            // 4. logout
            ClassicHttpRequest logout = createRequestBuilder(Method.POST, "/api/v1/logout", null);
            try (CloseableHttpResponse response = httpclient.execute(logout)) {
                assertEquals(HTTP_OK, response.getCode());
            }

            // 5. check login user info = false
            testUserInfoWhenNotLogin(httpclient);
        }
    }

    public void testUserInfoWhenLogged(CloseableHttpClient httpclient) throws Exception {
        ClassicHttpRequest loginInfo = createRequestBuilder(Method.GET, "/api/v1/status", null);
        try (CloseableHttpResponse response = httpclient.execute(loginInfo)) {
            assertEquals(HTTP_OK, response.getCode());
            LoginResponse loginResponse = objectMapper.readValue(EntityUtils.toString(response.getEntity()), LoginResponse.class);
            assertTrue(loginResponse.isLogin());
            assertEquals(TelVerificationServiceTest.VALID_PARAMETER.getTel(), loginResponse.getUser().getTel());
        }
    }

    public void testUserInfoWhenNotLogin(CloseableHttpClient httpclient) throws Exception {
        ClassicHttpRequest loginInfo = createRequestBuilder(Method.GET, "/api/v1/status", null);
        try (CloseableHttpResponse response = httpclient.execute(loginInfo)) {
            assertEquals(HTTP_OK, response.getCode());
            LoginResponse loginResponse = objectMapper.readValue(EntityUtils.toString(response.getEntity()), LoginResponse.class);
            assertFalse(loginResponse.isLogin());
            assertNull(loginResponse.getUser());
        }
    }
}
