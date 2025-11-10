package com.simudap.Config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.simudap.service.KisWebSocketService;
import com.simudap.service.WebSocketSessionManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.BinaryWebSocketHandler;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class WebSocketMessageHandler extends BinaryWebSocketHandler {

    private final WebSocketSessionManager sessionManager;
    private final KisWebSocketService kisWebSocketService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        // KIS WebSocket 세션 설정
        kisWebSocketService.setKisSession(session);
        log.info("KIS WebSocket 연결됨: {}", session.getId());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        String[] recvvalue = message.getPayload().split("\\^");
        if (recvvalue.length > 1) {
            String stockCode = recvvalue[0];

            // 로그 출력 (디버깅용)
            log.info("유가증권 단축 종목코드: {}", stockCode);
            log.info("영업시간: {}, 시간구분 코드: {}", recvvalue[1], recvvalue[2]);

            // 데이터를 JSON 형태로 변환
            Map<String, Object> data = buildStockData(recvvalue);

            try {
                String jsonData = objectMapper.writeValueAsString(data);
                // 해당 종목을 구독 중인 클라이언트들에게 브로드캐스트
                sessionManager.broadcastToStock(stockCode, jsonData);
                log.debug("종목 데이터 전송 완료: {}", stockCode);
            } catch (Exception e) {
                log.error("데이터 전송 중 오류 발생 - 종목: {}", stockCode, e);
            }
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        log.warn("KIS WebSocket 연결 해제됨 - 상태: {}", status);
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        log.error("KIS WebSocket 전송 오류", exception);
    }

    /**
     * 수신한 데이터를 구조화된 JSON 객체로 변환
     */
    private Map<String, Object> buildStockData(String[] recvvalue) {
        Map<String, Object> data = new HashMap<>();

        data.put("stockCode", recvvalue[0]);
        data.put("businessTime", recvvalue[1]);
        data.put("timeCode", recvvalue[2]);

        // 매도호가 (10단계)
        Map<String, Object> askPrices = new HashMap<>();
        for (int i = 1; i <= 10; i++) {
            Map<String, String> priceInfo = new HashMap<>();
            priceInfo.put("price", recvvalue[12 - i + 1]);
            priceInfo.put("volume", recvvalue[32 - i + 1]);
            askPrices.put("level" + i, priceInfo);
        }
        data.put("askPrices", askPrices);

        // 매수호가 (10단계)
        Map<String, Object> bidPrices = new HashMap<>();
        for (int i = 1; i <= 10; i++) {
            Map<String, String> priceInfo = new HashMap<>();
            priceInfo.put("price", recvvalue[12 + i]);
            priceInfo.put("volume", recvvalue[32 + i]);
            bidPrices.put("level" + i, priceInfo);
        }
        data.put("bidPrices", bidPrices);

        // 총 호가 정보
        data.put("totalAskVolume", recvvalue[43]);
        data.put("totalAskVolumeChange", recvvalue[54]);
        data.put("totalBidVolume", recvvalue[44]);
        data.put("totalBidVolumeChange", recvvalue[55]);

        // 시간외 호가 정보
        data.put("afterHoursTotalAskVolume", recvvalue[45]);
        data.put("afterHoursTotalBidVolume", recvvalue[46]);
        data.put("afterHoursTotalAskVolumeChange", recvvalue[56]);
        data.put("afterHoursTotalBidVolumeChange", recvvalue[57]);

        // 예상 체결 정보
        data.put("expectedPrice", recvvalue[47]);
        data.put("expectedVolume", recvvalue[48]);
        data.put("expectedTotalVolume", recvvalue[49]);
        data.put("expectedPriceChange", recvvalue[50]);
        data.put("expectedPriceSign", recvvalue[51]);
        data.put("expectedPriceChangeRate", recvvalue[52]);

        // 기타
        data.put("accumulatedVolume", recvvalue[53]);
        data.put("tradeTypeCode", recvvalue[58]);

        return data;
    }
}
