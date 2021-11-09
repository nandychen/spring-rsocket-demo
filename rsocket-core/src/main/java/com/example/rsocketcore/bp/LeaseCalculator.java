package com.example.rsocketcore.bp;

import io.rsocket.lease.Lease;
import io.rsocket.lease.LeaseStats;
import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.function.Function;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

@Slf4j
public class LeaseCalculator implements Function<Optional<LeaseStats>, Flux<Lease>> {

    final String tag;
    final BlockingQueue<?> queue;

    public LeaseCalculator(String tag, BlockingQueue<?> queue) {
        this.tag = tag;
        this.queue = queue;
    }

    @Override
    public Flux<Lease> apply(Optional<LeaseStats> leaseStats) {
        log.info("{} stats are {}", tag, leaseStats.isPresent() ? "present" : "absent");
        Duration ttlDuration = Duration.ofSeconds(10);
        // The interval function is used only for the demo purpose and should not be
        // considered as the way to issue leases.
        // For advanced RateLimiting with Leasing
        // consider adopting https://github.com/Netflix/concurrency-limits#server-limiter
        // 每2秒发送租约，租约内容为队列容量和10秒有效期
        return Flux.interval(Duration.ofSeconds(0), ttlDuration.dividedBy(2))
                .handle((__, sink) -> {
                    // put queue.remainingCapacity() + 1 here if you want to observe that app is
                    // terminated  because of the queue overflowing
                    int requests = queue.remainingCapacity();
                    // reissue new lease only if queue has remaining capacity to
                    // accept more requests
                    if (requests > 0) {
                        long ttl = ttlDuration.toMillis();
                        sink.next(Lease.create((int) ttl, requests));
                    }
                });
    }

}
