package com.example.binance;

import com.example.binance.entity.KlineEntity;
import com.example.binance.mapper.KlineMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

@SpringBootTest
@ActiveProfiles("test")
class KlineMapperTest {

    @Autowired
    private KlineMapper klineMapper;

    @Test
    void selectAll_shouldWork() {
        List<KlineEntity> list = klineMapper.selectAll();
        System.out.println(list);
    }
}
