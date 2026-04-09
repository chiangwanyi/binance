package com.example.binance;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling  // 开启定时任务
@MapperScan("com.example.binance.mapper")  // 扫描Mapper接口
public class BinanceApplication {

	public static void main(String[] args) {
		SpringApplication.run(BinanceApplication.class, args);
	}

}
