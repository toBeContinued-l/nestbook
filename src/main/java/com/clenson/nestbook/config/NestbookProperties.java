package com.clenson.nestbook.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "nestbook")
public class NestbookProperties {

    private int trialDays = 30;
    private int defaultDailyAiLimit = 10;

    public int getTrialDays() {
        return trialDays;
    }

    public void setTrialDays(int trialDays) {
        this.trialDays = trialDays;
    }

    public int getDefaultDailyAiLimit() {
        return defaultDailyAiLimit;
    }

    public void setDefaultDailyAiLimit(int defaultDailyAiLimit) {
        this.defaultDailyAiLimit = defaultDailyAiLimit;
    }
}
