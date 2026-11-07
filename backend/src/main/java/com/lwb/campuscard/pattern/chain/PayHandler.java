package com.lwb.campuscard.pattern.chain;

/**
 * 责任链基类
 * 每个处理器只负责一项校验，校验不过直接抛 BizException，链就断了
 */
public abstract class PayHandler {

    private PayHandler next;

    /** 串链，返回下一个以便链式书写 */
    public PayHandler next(PayHandler next) {
        this.next = next;
        return next;
    }

    public void doHandle(PayContext ctx) {
        check(ctx);
        if (next != null) {
            next.doHandle(ctx);
        }
    }

    protected abstract void check(PayContext ctx);
}
