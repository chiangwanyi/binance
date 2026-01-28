package com.example.binance.api;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.springframework.stereotype.Component;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URI;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class BinanceFStream {
    private static final String WS_URL =
            "wss://fstream.binance.com/ws/btcusdt@kline_5m";

    private WebSocketClient client;

    private final ScheduledExecutorService scheduler =
            Executors.newSingleThreadScheduledExecutor();

    @PostConstruct
    public void start() {
        connect();
    }

    private void connect() {
        try {
            client = new WebSocketClient(new URI(WS_URL)) {

                @Override
                public void onOpen(ServerHandshake handshake) {
                    log.info("Binance WS connected");
                }

                @Override
                public void onMessage(String message) {
                    System.out.println("KLINE: " + message);
                }

                @Override
                public void onClose(int code, String reason, boolean remote) {
                    log.warn("Binance WS closed, reason: {}", reason);
                    scheduleReconnect();
                }

                @Override
                public void onError(Exception ex) {
                    log.error("Binance WS error: {}", ex.getMessage());
                    scheduleReconnect();
                }
            };

            // ⭐ 必须 SOCKS5
            Proxy proxy = new Proxy(
                    Proxy.Type.SOCKS,
                    new InetSocketAddress("localhost", 7890)
            );
            client.setProxy(proxy);
            client.connect();

        } catch (Exception e) {
            scheduleReconnect();
        }
    }

    private void scheduleReconnect() {
        scheduler.schedule(() -> {
            try {
                if (client != null) {
                    client.close();
                }
            } catch (Exception ignored) {}

            connect();
        }, 5, TimeUnit.SECONDS);
    }
}
