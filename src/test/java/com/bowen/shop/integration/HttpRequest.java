package com.bowen.shop.integration;

import com.bowen.shop.entity.TelAndCode;
import com.bowen.shop.service.TelVerificationServiceTest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.Method;
import org.apache.hc.core5.http.io.support.ClassicRequestBuilder;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;

import java.net.URI;

public class HttpRequest {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private String getUrl(Environment environment, String apiName) {
        return "http://localhost:" + environment.getProperty("local.server.port") + apiName;
    }

    public static ClassicHttpRequest createRequestBuilder(Environment environment, Method method, String apiName, Object body) throws Exception {
        ClassicRequestBuilder accept = ClassicRequestBuilder.create(String.valueOf(method))
                .setUri(new URI(new HttpRequest().getUrl(environment, apiName)))
                .addHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .addHeader("accept", MediaType.APPLICATION_JSON_VALUE);
        if (body != null) {
            accept.setEntity(objectMapper.writeValueAsString(body));
        }
        return accept.build();
    }

    public static void login(CloseableHttpClient httpclient, Environment environment) throws Exception {
        // 1. send sms code
        ClassicHttpRequest sendSmsCode = HttpRequest.createRequestBuilder(environment, Method.POST, "/api/v1/code", TelVerificationServiceTest.VALID_PARAMETER);
        httpclient.execute(sendSmsCode);

        // 2. login => cookie
        ClassicHttpRequest login = HttpRequest.createRequestBuilder(environment,
                Method.POST,
                "/api/v1/login",
                new TelAndCode(TelVerificationServiceTest.VALID_PARAMETER.getTel(), "000000"));
        httpclient.execute(login);
    }
}
