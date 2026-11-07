package com.lwb.campuscard.pattern.chain;

import com.lwb.campuscard.model.entity.Account;
import com.lwb.campuscard.model.entity.Card;
import com.lwb.campuscard.model.entity.Terminal;

import java.math.BigDecimal;

/**
 * 扣款链上下文：一个请求从头传到尾，链上各处理器只读自己关心的字段
 */
public class PayContext {

    private String requestNo;
    private String cardNo;
    private Card card;
    private Account account;
    private Terminal terminal;
    /** 优惠计算后的实付金额 */
    private BigDecimal payAmount;
    private String payPwd;

    public String getRequestNo() { return requestNo; }

    public void setRequestNo(String requestNo) { this.requestNo = requestNo; }

    public String getCardNo() { return cardNo; }

    public void setCardNo(String cardNo) { this.cardNo = cardNo; }

    public Card getCard() { return card; }

    public void setCard(Card card) { this.card = card; }

    public Account getAccount() { return account; }

    public void setAccount(Account account) { this.account = account; }

    public Terminal getTerminal() { return terminal; }

    public void setTerminal(Terminal terminal) { this.terminal = terminal; }

    public BigDecimal getPayAmount() { return payAmount; }

    public void setPayAmount(BigDecimal payAmount) { this.payAmount = payAmount; }

    public String getPayPwd() { return payPwd; }

    public void setPayPwd(String payPwd) { this.payPwd = payPwd; }
}
