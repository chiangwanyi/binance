package com.example.binance.api;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.example.binance.entity.KlineEntity;
import com.example.binance.mapper.BTCUSDT5mPMapper;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URI;
import java.util.Arrays;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
@Slf4j
public class BinanceFStream {
    @Autowired
    private BTCUSDT5mPMapper btcusdt5mPMapper;

    private static final String WS_URL =
            "wss://fstream.binance.com/ws/btcusdt@kline_5m";

    private WebSocketClient client;

    private final ScheduledExecutorService scheduler =
            Executors.newSingleThreadScheduledExecutor();

    private final AtomicBoolean reconnecting = new AtomicBoolean(false);

    @PostConstruct
    public void start() {
//        connect();
    }

    private void connect() {
        try {
            client = new WebSocketClient(new URI(WS_URL)) {

                @Override
                public void onOpen(ServerHandshake handshake) {
                    log.info("Binance WS connected");
                    reconnecting.set(false);
                }

                @Override
                public void onMessage(String message) {
                    JSONObject k = (JSONObject) JSONUtil.parse(message).getByPath("k");
                    KlineEntity kline = new KlineEntity(
                            k.getLong("t"),
                            k.getStr("o"),
                            k.getStr("h"),
                            k.getStr("l"),
                            k.getStr("c"),
                            k.getStr("v"),
                            k.getLong("T"),
                            k.getStr("q"),
                            k.getInt("n"),
                            k.getStr("V"),
                            k.getStr("Q")
                    );
                    btcusdt5mPMapper.batchUpsert(Arrays.asList(kline));
                }

                @Override
                public void onClose(int code, String reason, boolean remote) {
                    log.warn("Binance WS closed, reason: {}", reason);
                    scheduleReconnect();
                }

                @Override
                public void onError(Exception ex) {
                    log.error("Binance WS error: {}", ex.getMessage());
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
        // 已经有重连计划了，直接忽略
        if (!reconnecting.compareAndSet(false, true)) {
            return;
        }

        scheduler.schedule(() -> {
            try {
                if (client != null && !client.isClosed()) {
                    client.close();
                }
            } catch (Exception ignored) {}

            try {
                connect();
            } finally {
                reconnecting.set(false);
            }
        }, 5, TimeUnit.SECONDS);
    }
}
