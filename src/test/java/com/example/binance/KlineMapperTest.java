package com.example.binance;

import com.example.binance.entity.KlineEntity;
import com.example.binance.mapper.KlineMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

@SpringBootTest
@ActiveProfiles("test")
@Slf4j
class KlineMapperTest {

    @Autowired
    private KlineMapper klineMapper;

    @Test
    void selectAll_shouldWork() {
        List<KlineEntity> list = klineMapper.selectAll();
        log.info("list: {}", list);
    }
}
