package com.example.binance.controller;

import com.example.binance.config.AjaxResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/c/binance")
public class BinanceController {
    @GetMapping("/ping")
    public AjaxResult ping() {
        return AjaxResult.success("pong");
    }
}
