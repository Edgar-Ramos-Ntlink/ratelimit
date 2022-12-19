package com.eddy.tool;

import reactor.core.publisher.Mono;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class SystemTimeSupplier {
    public SystemTimeSupplier() {
    }

    public CompletionStage<Long> getAsync() {
        return CompletableFuture.completedFuture(this.get());
    }

    public Mono<Long> getReactive() {
        return Mono.just(this.get());
    }

    public long get() {
        return System.currentTimeMillis() / 1000L;
    }
}
