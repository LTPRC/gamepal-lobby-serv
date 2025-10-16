package com.github.ltprc.gamepal.service;

import com.github.ltprc.gamepal.model.QwenResponse;

import java.util.concurrent.CompletableFuture;

public interface WebService {
    QwenResponse callQwenApi(String model, String prompt);
    CompletableFuture<QwenResponse> callQwenApiAsync(String model, String prompt);
}
