package com.example.digitaltwin.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * AI预测配置
 * 从 application.yml 读取 ai.prediction.* 配置项
 */
@Configuration
@ConfigurationProperties(prefix = "ai.prediction")
public class AiPredictionConfig {
    /** 是否启用AI预测 */
    private boolean enabled = false;

    /** AI提供商：deepseek | openai | doubao */
    private String provider = "deepseek";

    /** API密钥 */
    private String apiKey;

    /** API基础URL */
    private String baseUrl = "https://api.deepseek.com/v1";

    /** 模型名称 */
    private String model = "deepseek-chat";

    /** 超时时间（毫秒） */
    private int timeout = 30000;

    /** 最大重试次数 */
    private int maxRetries = 2;

    /** 上下文历史数据条数 */
    private int contextSize = 15;

    // Getters and Setters
    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public int getMaxRetries() {
        return maxRetries;
    }

    public void setMaxRetries(int maxRetries) {
        this.maxRetries = maxRetries;
    }

    public int getContextSize() {
        return contextSize;
    }

    public void setContextSize(int contextSize) {
        this.contextSize = contextSize;
    }
}
