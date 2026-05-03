package com.example.digitaltwin.service;

import com.example.digitaltwin.config.AiPredictionConfig;
import com.example.digitaltwin.dto.TimeValuePoint;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.volcengine.ark.runtime.model.completion.chat.ChatCompletionRequest;
import com.volcengine.ark.runtime.model.completion.chat.ChatCompletionResult;
import com.volcengine.ark.runtime.model.completion.chat.ChatMessage;
import com.volcengine.ark.runtime.model.completion.chat.ChatMessageRole;
import com.volcengine.ark.runtime.service.ArkService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * AI预测服务（火山引擎 Doubao 模型）
 * 使用火山引擎官方SDK进行调用
 */
@Service
public class AiPredictService {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AiPredictService.class);

    private final AiPredictionConfig config;
    private final ObjectMapper objectMapper;
    private final ArkService arkService;

    public AiPredictService(AiPredictionConfig config, ObjectMapper objectMapper) {
        this.config = config;
        this.objectMapper = objectMapper;

        // 初始化火山引擎 Ark 服务 - 只使用 apiKey，让 SDK 自动处理端点
        this.arkService = ArkService.builder()
            .apiKey(config.getApiKey())
            .build();

        log.info("[AI预测服务] 初始化完成，provider={}, model={}", config.getProvider(), config.getModel());
    }

    /**
     * 调用AI接口进行预测
     *
     * @param systemPrompt 系统提示词（角色定义）
     * @param userPrompt 用户提示词（具体任务）
     * @return 预测的时间序列数据点列表
     */
    public List<TimeValuePoint> predict(String systemPrompt, String userPrompt) {
        try {
            log.info("[AI预测] 开始调用火山引擎 Doubao 模型");
            log.info("[AI预测] ========== 系统提示词 ==========");
            log.info("{}", systemPrompt);
            log.info("[AI预测] ========== 用户提示词 ==========");
            log.info("{}", userPrompt);
            log.info("[AI预测] =====================================");

            // 构建消息列表
            ChatMessage systemMessage = ChatMessage.builder()
                .role(ChatMessageRole.SYSTEM)
                .content(systemPrompt)
                .build();

            ChatMessage userMessage = ChatMessage.builder()
                .role(ChatMessageRole.USER)
                .content(userPrompt)
                .build();

            // 构建请求
            ChatCompletionRequest request = ChatCompletionRequest.builder()
                .model(config.getModel())
                .messages(Arrays.asList(systemMessage, userMessage))
                .build();

            // 调用API
            ChatCompletionResult response = arkService.createChatCompletion(request);

            if (response == null || response.getChoices() == null || response.getChoices().isEmpty()) {
                log.warn("[AI预测] 响应为空");
                return null;
            }

            // 解析响应
            return parseResponse(response);

        } catch (Exception e) {
            log.error("[AI预测] 调用异常", e);
            return null;
        }
    }

    /**
     * 解析火山引擎响应，提取时间序列数据
     */
    private List<TimeValuePoint> parseResponse(ChatCompletionResult response) {
        try {
            // 获取AI返回的文本内容
            String content = response.getChoices().get(0).getMessage().stringContent();
            log.info("[AI预测] AI返回内容: {}", content.substring(0, Math.min(200, content.length())));

            // 提取JSON数组（可能包含在markdown代码块中）
            String jsonArray = extractJsonArray(content);

            // 解析为TimeValuePoint列表
            JsonNode dataPoints = objectMapper.readTree(jsonArray);
            List<TimeValuePoint> result = new ArrayList<>();

            for (JsonNode point : dataPoints) {
                double time = point.path("time").asDouble();
                double value = point.path("value").asDouble();

                TimeValuePoint tvp = new TimeValuePoint();
                tvp.setTime(time);
                tvp.setValue(value);
                result.add(tvp);
            }

            log.info("[AI预测] 成功解析{}个数据点", result.size());
            return result;

        } catch (Exception e) {
            log.error("[AI预测] 解析响应失败", e);
            return null;
        }
    }

    /**
     * 从AI响应中提取JSON数组
     * 处理可能的markdown代码块包裹
     */
    private String extractJsonArray(String content) {
        String cleaned = content.trim();

        // 移除markdown代码块标记
        if (cleaned.startsWith("```json")) {
            cleaned = cleaned.substring(7);
        } else if (cleaned.startsWith("```")) {
            cleaned = cleaned.substring(3);
        }

        if (cleaned.endsWith("```")) {
            cleaned = cleaned.substring(0, cleaned.length() - 3);
        }

        return cleaned.trim();
    }
}
