package com.bowen.shop.integration;

import com.bowen.shop.WechatShopApplication;
import com.bowen.shop.service.TelVerificationServiceTest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.io.support.ClassicRequestBuilder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.net.URI;

import static java.net.HttpURLConnection.HTTP_BAD_REQUEST;
import static java.net.HttpURLConnection.HTTP_OK;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = WechatShopApplication.class, webEnvironment = WebEnvironment.RANDOM_PORT)
@TestPropertySource(locations = "classpath:application.yml")
public class CodeIntegrationTest {
    @Autowired
    Environment environment;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void returnHttpOKWhenParameterIsCorrect() throws Exception {
        try (CloseableHttpClient httpclient = HttpClients.custom().build()) {
            final ClassicHttpRequest login = ClassicRequestBuilder.post()
                    .setUri(new URI(getUrl("/api/v1/code")))
                    .addHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                    .addHeader("accept", MediaType.APPLICATION_JSON_VALUE)
                    .setEntity(objectMapper.writeValueAsString(TelVerificationServiceTest.VALID_PARAMETER))
                    .build();
            try (CloseableHttpResponse response = httpclient.execute(login)) {
                Assertions.assertEquals(HTTP_OK, response.getCode());
            }
        }
    }

    @Test
    public void returnHttpBadRequestWhenParameterIsCorrect() throws Exception {
        try (CloseableHttpClient httpclient = HttpClients.custom().build()) {
            final ClassicHttpRequest login = ClassicRequestBuilder.post()
                    .setUri(new URI(getUrl("/api/v1/code")))
                    .addHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                    .addHeader("accept", MediaType.APPLICATION_JSON_VALUE)
                    .setEntity(objectMapper.writeValueAsString(TelVerificationServiceTest.INVALID_PARAMETER))
                    .build();
            try (CloseableHttpResponse response = httpclient.execute(login)) {
                Assertions.assertEquals(HTTP_BAD_REQUEST, response.getCode());
            }
        }
    }

    private String getUrl(String apiName) {
        return "http://localhost:" + environment.getProperty("local.server.port") + apiName;
    }
}
