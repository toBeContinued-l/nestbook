package com.clenson.nestbook.service;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

@Component
class RuleBillParser {

    private static final Pattern EXPLICIT_BILL_PATTERN = Pattern.compile(
            "^(收入|支出)\\s+([0-9]+(?:\\.[0-9]{1,2})?)\\s+(.+)$"
    );
    private static final Pattern INCOME_SUFFIX_PATTERN = Pattern.compile(
            "^(.+?)收入\\s*([0-9]+(?:\\.[0-9]{1,2})?)$"
    );
    private static final Pattern DEFAULT_EXPENSE_PATTERN = Pattern.compile(
            "^(.+?)([0-9]+(?:\\.[0-9]{1,2})?)$"
    );

    Optional<ParsedBill> parse(String text) {
        Matcher explicitMatcher = EXPLICIT_BILL_PATTERN.matcher(text);
        if (explicitMatcher.matches()) {
            return parsedBill("收入".equals(explicitMatcher.group(1)) ? 1 : 2,
                    explicitMatcher.group(2), explicitMatcher.group(3));
        }

        Matcher incomeMatcher = INCOME_SUFFIX_PATTERN.matcher(text);
        if (incomeMatcher.matches()) {
            return parsedBill(1, incomeMatcher.group(2), incomeMatcher.group(1));
        }

        Matcher expenseMatcher = DEFAULT_EXPENSE_PATTERN.matcher(text);
        if (expenseMatcher.matches()) {
            return parsedBill(2, expenseMatcher.group(2), expenseMatcher.group(1));
        }
        return Optional.empty();
    }

    private Optional<ParsedBill> parsedBill(int billType, String amountText, String categoryText) {
        BigDecimal amount = new BigDecimal(amountText);
        if (amount.signum() <= 0) {
            return Optional.empty();
        }

        String category = categoryText.trim();
        if (category.isBlank() || category.length() > 64) {
            return Optional.empty();
        }
        return Optional.of(new ParsedBill(billType, amount, category));
    }

    record ParsedBill(int billType, BigDecimal amount, String category) {
    }
}
