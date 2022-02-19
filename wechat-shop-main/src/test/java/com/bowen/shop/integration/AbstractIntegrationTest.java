package com.bowen.shop.integration;

import com.bowen.shop.entity.LoginResponse;
import com.bowen.shop.entity.TelAndCode;
import com.bowen.shop.generate.User;
import com.bowen.shop.service.TelVerificationServiceTest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.Method;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.support.ClassicRequestBuilder;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.configuration.ClassicConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;

import java.net.URI;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public abstract class AbstractIntegrationTest {
    @Value("${spring.datasource.url}")
    private String databaseUrl;
    @Value("${spring.datasource.username}")
    private String databaseUsername;
    @Value("${spring.datasource.password}")
    private String databasePassword;

    @Autowired
    Environment environment;

    @BeforeEach
    public void setup() {
        ClassicConfiguration configuration = new ClassicConfiguration();
        configuration.setDataSource(databaseUrl, databaseUsername, databasePassword);
        Flyway flyway = new Flyway(configuration);
        flyway.clean();
        flyway.migrate();
    }

    public final ObjectMapper objectMapper = new ObjectMapper();

    private String getUrl(String apiName) {
        return "http://localhost:" + environment.getProperty("local.server.port") + apiName;
    }

    public ClassicHttpRequest createRequestBuilder(Method method, String apiName, Object body) throws Exception {
        ClassicRequestBuilder accept = ClassicRequestBuilder.create(String.valueOf(method))
                .setUri(new URI(getUrl(apiName)))
                .addHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .addHeader("accept", MediaType.APPLICATION_JSON_VALUE);
        if (body != null) {
            accept.setEntity(objectMapper.writeValueAsString(body));
        }
        return accept.build();
    }

    public User login(CloseableHttpClient httpclient) throws Exception {
        // 1. send sms code
        ClassicHttpRequest sendSmsCode = createRequestBuilder(Method.POST, "/api/v1/code", TelVerificationServiceTest.VALID_PARAMETER);
        httpclient.execute(sendSmsCode);

        // 2. login => cookie
        ClassicHttpRequest login = createRequestBuilder(Method.POST,
                "/api/v1/login",
                new TelAndCode(TelVerificationServiceTest.VALID_PARAMETER.getTel(), "000000"));
        httpclient.execute(login);

        // 3. 获取用户信息
        ClassicHttpRequest loginInfo = createRequestBuilder(Method.GET, "/api/v1/status", null);
        try (CloseableHttpResponse response = httpclient.execute(loginInfo)) {
            LoginResponse loginResponse = objectMapper.readValue(EntityUtils.toString(response.getEntity()), LoginResponse.class);
            return loginResponse.getUser();
        }
    }

    public void assertHttpException(CloseableHttpClient httpclient, Method method, String api, Object data, int HttpStatusCode, String errorMessage) throws Exception {
        try (CloseableHttpResponse response = httpclient.execute(createRequestBuilder(method, api, data))) {
            assertEquals(HttpStatusCode, response.getCode());
            assertTrue(EntityUtils.toString(response.getEntity()).contains(errorMessage));
        }
    }
}
