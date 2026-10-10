package com.gexianmi.simulator;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * 环境数据模拟器，模拟传感器定时上报环境数据。
 * <p>
 * 每 5 秒向接口 POST 一条随机环境数据，手动运行 main 方法启动，Ctrl+C 停止。
 * <p>
 * 用法：在 IDEA 中右键 → Run 'DataSimulator.main()'
 *
 * @author GXM-Team
 * @since 2026-07-11
 */
public class DataSimulator {

    /** 环境数据上报接口地址 */
    private static final String URL = "http://localhost:8000/api/environment/data";
    /** 模拟设备 ID 列表 */
    private static final String[] DEVICE_IDS = {"pond-01", "pond-02"};
    private static final Random RANDOM = new Random();
    private static final HttpClient CLIENT = HttpClient.newHttpClient();
    /** 时间格式化器 */
    private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 模拟器入口，循环向各设备发送随机环境数据。
     *
     * @param args 命令行参数（未使用）
     * @throws InterruptedException 线程被中断时抛出
     */
    public static void main(String[] args) throws InterruptedException {
        System.out.println("====================================");
        System.out.println("  葛仙米环境数据模拟器（Java）");
        System.out.println("  每 5 秒发送一条，Ctrl+C 停止");
        System.out.println("====================================\n");

        while (true) {
            for (String deviceId : DEVICE_IDS) {
                String json = buildJson(deviceId);
                sendPost(json);
                Thread.sleep(TimeUnit.SECONDS.toMillis(5));
            }
        }
    }

    /**
     * 构建指定设备的一条随机环境数据 JSON。
     *
     * @param deviceId 设备编号
     * @return JSON 格式的环境数据
     */
    private static String buildJson(String deviceId) {
        // 模拟合理范围内的随机环境参数
        double waterTemp = round(20.0 + RANDOM.nextDouble() * 5.0);
        double ph = round(6.5 + RANDOM.nextDouble() * 1.0);
        double doxy = round(5.0 + RANDOM.nextDouble() * 3.0);
        double light = round(800 + RANDOM.nextDouble() * 700);
        double cond = round(300 + RANDOM.nextDouble() * 100);
        String timestamp = LocalDateTime.now().format(FMT);

        return String.format(
                "{\"deviceId\":\"%s\",\"timestamp\":\"%s\"," +
                "\"waterTemp\":%.1f,\"ph\":%.1f,\"dissolvedOxygen\":%.1f," +
                "\"lightIntensity\":%.0f,\"conductivity\":%.0f}",
                deviceId, timestamp, waterTemp, ph, doxy, light, cond
        );
    }

    /**
     * 发送 POST 请求上报环境数据。
     *
     * @param json 请求体 JSON 字符串
     */
    private static void sendPost(String json) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(URL))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();
            HttpResponse<String> response = CLIENT.send(request,
                    HttpResponse.BodyHandlers.ofString());
            System.out.println("[" + LocalDateTime.now().format(FMT) + "] 发送 → " + json);
            System.out.println("[" + LocalDateTime.now().format(FMT) + "] 响应 ← " + response.body());
            System.out.println("---");
        } catch (Exception e) {
            System.err.println("[ERROR] 发送失败: " + e.getMessage());
        }
    }

    /**
     * 四舍五入保留一位小数。
     *
     * @param value 原始值
     * @return 保留一位小数后的值
     */
    private static double round(double value) {
        return BigDecimal.valueOf(value)
                .setScale(1, RoundingMode.HALF_UP)
                .doubleValue();
    }
}
