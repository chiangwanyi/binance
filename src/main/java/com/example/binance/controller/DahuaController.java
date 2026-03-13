package com.example.binance.controller;

import com.example.binance.service.DahuaDownloadService;
import com.netsdk.lib.NetSDKLib;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/camera")
public class DahuaController {

    @Resource
    DahuaDownloadService service;

    @GetMapping("/download")
    public String download(@RequestParam("start") String startTimeStr, @RequestParam("duration") Integer duration) throws Exception {
        // startTimeStr 格式为 YYYY-MM-DD HH:mm:ss
        // duration 秒

        NetSDKLib.NET_TIME start = new NetSDKLib.NET_TIME();
        start.dwYear = 2026;
        start.dwMonth = 3;
        start.dwDay = 11;
        start.dwHour = 21;
        start.dwMinute = 0;
        start.dwSecond = 0;

        NetSDKLib.NET_TIME end = new NetSDKLib.NET_TIME();
        end.dwYear = 2026;
        end.dwMonth = 3;
        end.dwDay = 11;
        end.dwHour = 21;
        end.dwMinute = 0;
        end.dwSecond = 30;

        // 类似 D:\Data\Java\binance\20260311_210000__20260311_210030.mp4
        String videoFilePath = service.download(
                "192.168.1.108",
                37777,
                "admin",
                "drx123456",
                start,
                end
        );
        return videoFilePath;
    }
}
