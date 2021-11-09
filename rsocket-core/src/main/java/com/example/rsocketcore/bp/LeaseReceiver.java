package com.example.rsocketcore.bp;

import io.rsocket.lease.Lease;
import java.util.function.Consumer;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.ReplayProcessor;

@Slf4j
public class LeaseReceiver implements Consumer<Flux<Lease>> {

    final String tag;
    // 缓存最后一个租约，每当新的订阅者订阅的时候就回放这个租约
    final ReplayProcessor<Lease> lastLeaseReplay = ReplayProcessor.cacheLast();

    public LeaseReceiver(String tag) {
        this.tag = tag;
    }

    @Override
    public void accept(Flux<Lease> receivedLeases) {
        receivedLeases.subscribe(
                l -> {
                    log.info("{} received leases - ttl: {}, requests: {}", tag, l.getTimeToLiveMillis(),
                            l.getAllowedRequests());
                    lastLeaseReplay.onNext(l);
                });
    }

    /**
     * 通知下游新的有效租约的到来
     */
    public Mono<Lease> notifyWhenNewLease() {
        return lastLeaseReplay.filter(l -> l.isValid()).next();
    }


}
