package com.example.binance.controller;

import com.example.binance.service.DahuaDownloadService;
import com.netsdk.lib.NetSDKLib;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

@RestController
@RequestMapping("/camera")
public class DahuaController {

    @Resource
    DahuaDownloadService service;

    // 时间格式化工具，严格匹配 YYYY-MM-DD HH:mm:ss
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @GetMapping("/download")
    public void download(@RequestParam("start") String startTimeStr,
                           @RequestParam("duration") Integer duration,
                           HttpServletResponse response) throws Exception {
        // startTimeStr 格式为 YYYY-MM-DD HH:mm:ss
        // duration 秒

        // 1. 解析字符串时间为 Date 对象
        Date startDate = DATE_FORMAT.parse(startTimeStr);

        // 2. 将 Date 转换为 Calendar，方便获取年、月、日、时、分、秒
        Calendar startCal = Calendar.getInstance();
        startCal.setTime(startDate);

        // 3. 赋值开始时间 NET_TIME
        NetSDKLib.NET_TIME start = new NetSDKLib.NET_TIME();
        start.dwYear = startCal.get(Calendar.YEAR);
        start.dwMonth = startCal.get(Calendar.MONTH) + 1; // 月份从0开始，需要+1
        start.dwDay = startCal.get(Calendar.DAY_OF_MONTH);
        start.dwHour = startCal.get(Calendar.HOUR_OF_DAY);
        start.dwMinute = startCal.get(Calendar.MINUTE);
        start.dwSecond = startCal.get(Calendar.SECOND);

        // 4. 计算结束时间 = 开始时间 + duration 秒
        Calendar endCal = (Calendar) startCal.clone();
        endCal.add(Calendar.SECOND, duration); // 增加指定秒数

        // 5. 赋值结束时间 NET_TIME
        NetSDKLib.NET_TIME end = new NetSDKLib.NET_TIME();
        end.dwYear = endCal.get(Calendar.YEAR);
        end.dwMonth = endCal.get(Calendar.MONTH) + 1;
        end.dwDay = endCal.get(Calendar.DAY_OF_MONTH);
        end.dwHour = endCal.get(Calendar.HOUR_OF_DAY);
        end.dwMinute = endCal.get(Calendar.MINUTE);
        end.dwSecond = endCal.get(Calendar.SECOND);

        // 类似 D:\Data\Java\binance\20260311_210000__20260311_210030.mp4
        String videoFilePath = service.download(
                "192.168.1.108",
                37777,
                "admin",
                "drx123456",
                start,
                end
        );

        // 4. 读取文件并返回给前端
        File videoFile = new File(videoFilePath);
        if (!videoFile.exists()) {
            response.getWriter().write("视频文件不存在");
            return;
        }

        // 设置响应头：支持在线播放 + 下载
        String fileName = URLEncoder.encode(videoFile.getName(), "UTF-8");
        response.setContentType("video/mp4");
        response.setHeader("Content-Disposition", "attachment;filename=\"" + fileName + "\"");
        response.setHeader("Accept-Ranges", "bytes");

        // 写出文件流
        try (InputStream in = new FileInputStream(videoFile);
             OutputStream out = response.getOutputStream()) {

            byte[] buffer = new byte[4096];
            int len;
            while ((len = in.read(buffer)) != -1) {
                out.write(buffer, 0, len);
            }
            out.flush();
        }
    }
}