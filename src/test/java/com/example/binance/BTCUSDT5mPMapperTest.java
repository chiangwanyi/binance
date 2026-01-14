package com.example.binance;

import com.example.binance.entity.KlineEntity;
import com.example.binance.mapper.BTCUSDT5mPMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@ActiveProfiles("test")
@Slf4j
class BTCUSDT5mPMapperTest {

    @Autowired
    private BTCUSDT5mPMapper BTCUSDT5mPMapper;

    @Test
    void selectAll_shouldWork() {
        List<KlineEntity> list = BTCUSDT5mPMapper.selectAll();
        log.info("list: {}", list);
    }

    @Test
    void batchInsert_shouldWork() {
        KlineEntity k1 = new KlineEntity(
                1L,
                "100",
                "110",
                "95",
                "105",
                "123.45",
                2L,
                "12345.67",
                100,
                "60.12",
                "6012.34"
        );

        int rows = BTCUSDT5mPMapper.batchUpsert(List.of(k1));
        assertEquals(1, rows);
    }

}
