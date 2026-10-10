package com.gexianmi.service;

import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 告警实时推送服务（SSE）。
 * <p>
 * 前端通过 EventSource 订阅，有新告警时后端主动推送，无需轮询。
 * </p>
 *
 * @author GXM-Team
 * @since 2026-10-10
 */
@Service
public class AlertSseService {

    /** 当前所有在线订阅者（线程安全的列表） */
    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    /** 订阅告警推送，返回一个长连接的 SseEmitter */
    public SseEmitter subscribe() {
        SseEmitter emitter = new SseEmitter(0L);
        emitters.add(emitter);
        emitter.onCompletion(() -> emitters.remove(emitter));
        emitter.onTimeout(() -> emitters.remove(emitter));
        emitter.onError(e -> emitters.remove(emitter));
        return emitter;
    }

    /** 向所有订阅者广播指定事件 */
    public void broadcast(String event, Object data) {
        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event().name(event).data(data));
            } catch (Exception e) {
                emitters.remove(emitter);
            }
        }
    }
}
