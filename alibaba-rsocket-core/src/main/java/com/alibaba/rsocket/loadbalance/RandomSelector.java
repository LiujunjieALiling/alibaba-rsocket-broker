package com.alibaba.rsocket.loadbalance;

import com.alibaba.rsocket.observability.RsocketErrorCode;
import org.jetbrains.annotations.Nullable;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

/**
 * random selector
 *
 * @author leijuan
 */
public class RandomSelector<T> implements Supplier<Mono<T>> {
    // round robin index
    private final AtomicInteger position = new AtomicInteger(0);
    private List<T> elements;
    private int size;
    private String name;

    public RandomSelector(String name, List<T> elements) {
        this.elements = elements;
        this.size = elements.size();
        this.name = name;
    }

    @Nullable
    public T next() {
        if (size > 1) {
            int pos = Math.abs(this.position.incrementAndGet());
            T t = elements.get(pos % size);
            if (t == null) {
                t = elements.get(0);
            }
            return t;
        } else if (size == 1) {
            return elements.get(0);
        } else {
            return null;
        }
    }

    @Override
    public Mono<T> get() {
        T next = next();
        if (next == null) {
            return Mono.error(new NoAvailableConnectionException(RsocketErrorCode.message("RST-200404", this.name)));
        }
        return Mono.just(next);
    }
}
