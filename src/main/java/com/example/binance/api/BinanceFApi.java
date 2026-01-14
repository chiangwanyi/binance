package com.example.binance.api;

import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import com.example.binance.config.BinanceIntervalEnum;
import com.example.binance.entity.KlineEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Service
@Slf4j
public class BinanceFApi {

    private static final String BINANCE_KLINE_API =
            "https://fapi.binance.com/fapi/v1/klines";

    /**
     * 本地代理（Clash / v2ray / etc）
     */
    private static final String PROXY_HOST = "127.0.0.1";
    private static final int PROXY_PORT = 7890;

    public List<KlineEntity> getKlineData(
            String symbol,
            BinanceIntervalEnum interval,
            long startTime,
            long endTime,
            Integer limit
    ) {

        HashMap<String, Object> paramMap = new HashMap<>();
        paramMap.put("symbol", symbol);
        paramMap.put("interval", interval.getInterval());
        paramMap.put("startTime", startTime);
        paramMap.put("endTime", endTime);
        paramMap.put("limit", limit);

        // 构建代理
        Proxy proxy = new Proxy(
                Proxy.Type.HTTP,
                new InetSocketAddress(PROXY_HOST, PROXY_PORT)
        );

        String response = HttpRequest.get(BINANCE_KLINE_API)
                .setProxy(proxy)
                .form(paramMap)
                .timeout(15_000)   // 15 秒超时
                .execute()
                .body();

        JSONArray arr = JSONUtil.parseArray(response);
        List<KlineEntity> result = new ArrayList<>(arr.size());

        for (int i = 0; i < arr.size(); i++) {
            JSONArray k = arr.getJSONArray(i);
            KlineEntity entity = new KlineEntity(
                    k.getLong(0),
                    k.getStr(1),
                    k.getStr(2),
                    k.getStr(3),
                    k.getStr(4),
                    k.getStr(5),
                    k.getLong(6),
                    k.getStr(7),
                    k.getInt(8),
                    k.getStr(9),
                    k.getStr(10)
            );
            result.add(entity);
        }

        return result;
    }
}
