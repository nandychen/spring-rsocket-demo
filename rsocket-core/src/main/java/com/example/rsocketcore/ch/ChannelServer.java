package com.example.rsocketcore.ch;

import io.rsocket.Payload;
import io.rsocket.SocketAcceptor;
import io.rsocket.core.RSocketServer;
import io.rsocket.transport.netty.server.TcpServerTransport;
import io.rsocket.util.DefaultPayload;
import java.util.concurrent.TimeUnit;
import reactor.core.publisher.Flux;

public class ChannelServer {

    public static void main(String[] args) throws InterruptedException {
        SocketAcceptor serverAcceptor =
                SocketAcceptor.forRequestChannel(
                        payloads -> Flux.from(payloads)
                                .map(Payload::getDataUtf8)
                                .map(s -> "服务端收到消息为: " + s)
                                .map(DefaultPayload::create));

        RSocketServer.create(serverAcceptor)
                .bind(TcpServerTransport.create("localhost", 7080))
                .subscribe();
        TimeUnit.MINUTES.sleep(10);
    }

}
