package com.example.rsocketcore.ch;

import io.rsocket.Payload;
import io.rsocket.RSocket;
import io.rsocket.core.RSocketConnector;
import io.rsocket.transport.netty.client.TcpClientTransport;
import io.rsocket.util.DefaultPayload;
import java.time.Duration;
import reactor.core.publisher.Flux;

public class ChannelClient {

    public static void main(String[] args) {
        RSocket socket =
                RSocketConnector.connectWith(TcpClientTransport.create("localhost", 7080)).block();
        assert socket != null;
        socket.requestChannel(
                Flux.interval(Duration.ofMillis(1000)).map(i -> DefaultPayload.create("我是客户端啊")))
                .map(Payload::getDataUtf8)
                .doOnNext(System.out::println)
                .take(10)
                .doFinally(signalType -> socket.dispose())
                .then()
                .block();
    }

}
