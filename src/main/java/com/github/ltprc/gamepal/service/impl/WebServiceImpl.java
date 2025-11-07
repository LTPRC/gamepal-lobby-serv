package com.github.ltprc.gamepal.service.impl;

import com.alibaba.fastjson.JSON;
import com.github.ltprc.gamepal.model.QwenResponse;
import com.github.ltprc.gamepal.service.WebService;
import com.github.ltprc.gamepal.util.ErrorUtil;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

@Service
public class WebServiceImpl implements WebService {

    private static final Log logger = LogFactory.getLog(WebServiceImpl.class);
    private static final String API_URL = "https://dashscope.aliyuncs.com/api/v1/services/aigc/text-generation/generation";
    private static final String API_KEY = "Bearer sk-cf4a21c79ccf42149e67ee67cf49d7e0";
    private final OkHttpClient client = new OkHttpClient();

    @Override
    public QwenResponse callQwenApi(String model, String prompt) {
        String jsonBody = "{\n" +
                "  \"model\": \"" + model + "\",\n" +
                "  \"input\": {\n" +
                "    \"messages\": [\n" +
                "      { \"role\": \"user\", \"content\": \"" + prompt + "\" }\n" +
                "    ]\n" +
                "  },\n" +
                "  \"parameters\": {\n" +
                "    \"temperature\": 0.8,\n" +
                "    \"top_p\": 0.9,\n" +
                "    \"max_tokens\": 512\n" +
                "  }\n" +
                "}";

        RequestBody body = RequestBody.create(jsonBody, MediaType.get("application/json"));
        Request request = new Request.Builder()
                .url(API_URL)
                .post(body)
                .addHeader("Authorization", API_KEY)
                .addHeader("Content-Type", "application/json")
                .build();

        try (okhttp3.Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                logger.error(ErrorUtil.ERROR_1044);
                return null;
            }

            assert response.body() != null;
            String responseBody = response.body().string();
            return JSON.parseObject(responseBody, QwenResponse.class);

        } catch (IOException e) {
            logger.error(ErrorUtil.ERROR_1044 + " message: " + e.getMessage());
            return null;
        }
    }

    @Override
    public CompletableFuture<QwenResponse> callQwenApiAsync(String model, String prompt) {
        return CompletableFuture.supplyAsync(() -> callQwenApi(model, prompt));
    }
}
