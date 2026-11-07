package com.lwb.campuscard.common;

/**
 * 错误码集中定义，前端按 code 做不同提示
 */
public interface ErrorCode {

    int BIZ_ERROR = 1000;

    int ACCOUNT_NOT_FOUND = 1001;
    int CARD_LOST = 1002;
    int CARD_CANCELLED = 1003;
    int BALANCE_NOT_ENOUGH = 1004;
    int PASSWORD_ERROR = 1005;
    int SINGLE_LIMIT_EXCEED = 1006;
    int DAILY_LIMIT_EXCEED = 1007;
    int DUPLICATE_REQUEST = 1008;
    int AMOUNT_INVALID = 1009;
    int TERMINAL_OFFLINE = 1010;
    int TRADE_NOT_FOUND = 1011;
    int TRADE_ALREADY_REVERSED = 1012;
}
