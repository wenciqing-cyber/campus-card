package com.lwb.campuscard.pattern.chain;

import com.lwb.campuscard.common.BizException;
import com.lwb.campuscard.common.ErrorCode;
import org.springframework.stereotype.Component;

/**
 * 第二环：交易密码
 */
@Component
public class PasswordHandler extends PayHandler {

    @Override
    protected void check(PayContext ctx) {
        String input = ctx.getPayPwd();
        if (input == null || input.isBlank()) {
            throw new BizException(ErrorCode.PASSWORD_ERROR, "请输入交易密码");
        }
        if (!input.equals(ctx.getCard().getPayPwd())) {
            throw new BizException(ErrorCode.PASSWORD_ERROR, "交易密码错误");
        }
    }
}
