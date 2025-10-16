package com.github.ltprc.gamepal.model;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

@Data
public class QwenResponse {

    @JSONField(name = "request_id")
    private String requestId;

    private Output output;

    private Usage usage;


    @Data
    public static class Output {
        private String text;

        @JSONField(name = "finish_reason")
        private String finishReason;

        private Message message;
    }

    @Data
    public static class Message {
        private String role;

        private String content;
    }

    @Data
    public static class Usage {
        @JSONField(name = "input_tokens")
        private Integer inputTokens;

        @JSONField(name = "output_tokens")
        private Integer outputTokens;

        @JSONField(name = "total_tokens")
        private Integer totalTokens;
    }
}
