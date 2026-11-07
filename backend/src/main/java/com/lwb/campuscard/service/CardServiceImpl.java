package com.lwb.campuscard.service;

import com.lwb.campuscard.common.BizException;
import com.lwb.campuscard.common.ErrorCode;
import com.lwb.campuscard.dao.AccountMapper;
import com.lwb.campuscard.dao.CardMapper;
import com.lwb.campuscard.dao.TerminalMapper;
import com.lwb.campuscard.dao.TradeMapper;
import com.lwb.campuscard.model.dto.ConsumeCommand;
import com.lwb.campuscard.model.dto.PricingRequest;
import com.lwb.campuscard.model.dto.PricingResult;
import com.lwb.campuscard.model.dto.RechargeCommand;
import com.lwb.campuscard.model.dto.RefundCommand;
import com.lwb.campuscard.model.entity.Account;
import com.lwb.campuscard.model.entity.Card;
import com.lwb.campuscard.model.entity.Terminal;
import com.lwb.campuscard.model.entity.Trade;
import com.lwb.campuscard.model.enums.TradeStatus;
import com.lwb.campuscard.model.enums.TradeType;
import com.lwb.campuscard.pattern.adapter.PayRequest;
import com.lwb.campuscard.pattern.adapter.RawTerminalMessage;
import com.lwb.campuscard.pattern.adapter.TerminalAdapterRegistry;
import com.lwb.campuscard.pattern.builder.TransactionDirector;
import com.lwb.campuscard.pattern.builder.TransactionTicket;
import com.lwb.campuscard.pattern.chain.PayChainFactory;
import com.lwb.campuscard.pattern.chain.PayContext;
import com.lwb.campuscard.pattern.factory.AbstractTransaction;
import com.lwb.campuscard.pattern.factory.TransactionFactory;
import com.lwb.campuscard.pattern.observer.TradeEvent;
import com.lwb.campuscard.pattern.observer.TradeEventPublisher;
import com.lwb.campuscard.pattern.singleton.TradeNoGenerator;
import com.lwb.campuscard.pattern.support.Money;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 交易核心实现：把适配、定价、校验、扣款、建单、广播串成一条线
 * 每个环节的细节都在各自的模式类里，这里只负责顺序
 */
@Service
public class CardServiceImpl implements PayFacade {

    private final CardMapper cardMapper;
    private final AccountMapper accountMapper;
    private final TerminalMapper terminalMapper;
    private final TradeMapper tradeMapper;
    private final TerminalAdapterRegistry adapterRegistry;
    private final PricingService pricingService;
    private final PayChainFactory payChain;
    private final TransactionFactory transactionFactory;
    private final TransactionDirector director;
    private final TradeEventPublisher publisher;

    public CardServiceImpl(CardMapper cardMapper,
                           AccountMapper accountMapper,
                           TerminalMapper terminalMapper,
                           TradeMapper tradeMapper,
                           TerminalAdapterRegistry adapterRegistry,
                           PricingService pricingService,
                           PayChainFactory payChain,
                           TransactionFactory transactionFactory,
                           TransactionDirector director,
                           TradeEventPublisher publisher) {
        this.cardMapper = cardMapper;
        this.accountMapper = accountMapper;
        this.terminalMapper = terminalMapper;
        this.tradeMapper = tradeMapper;
        this.adapterRegistry = adapterRegistry;
        this.pricingService = pricingService;
        this.payChain = payChain;
        this.transactionFactory = transactionFactory;
        this.director = director;
        this.publisher = publisher;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Trade consume(ConsumeCommand cmd) {
        // 幂等：同一个 requestNo 只处理一次
        Trade existed = tradeMapper.findByRequestNo(cmd.getRequestNo());
        if (existed != null) {
            throw new BizException(ErrorCode.DUPLICATE_REQUEST,
                    "该请求已处理，交易号 " + existed.getTradeNo());
        }

        Card card = cardMapper.findByCardNo(cmd.getCardNo());
        if (card == null) {
            throw new BizException(ErrorCode.ACCOUNT_NOT_FOUND, "卡号不存在");
        }
        Account account = accountMapper.findById(card.getAccountId());
        Terminal terminal = terminalMapper.findByNo(cmd.getTerminalNo());
        if (terminal == null) {
            throw new BizException(ErrorCode.TERMINAL_OFFLINE, "终端不存在");
        }
        if (terminal.getStatus() != null && terminal.getStatus() == 0) {
            throw new BizException(ErrorCode.TERMINAL_OFFLINE, "终端已停用");
        }

        // 适配器：不同终端报文 → 统一金额
        RawTerminalMessage raw = new RawTerminalMessage();
        raw.setTerminalNo(cmd.getTerminalNo());
        raw.setCardNo(cmd.getCardNo());
        raw.setPayPwd(cmd.getPayPwd());
        raw.setRequestNo(cmd.getRequestNo());
        raw.setPayload(cmd.getPayload());
        PayRequest payRequest = adapterRegistry.adapt(raw, terminal);

        // 定价：策略 + 装饰
        PricingRequest pricingRequest = new PricingRequest();
        pricingRequest.setCardNo(cmd.getCardNo());
        pricingRequest.setOriginAmount(payRequest.getAmount());
        pricingRequest.setPoints(account.getPoints());
        String ruleCodes = (cmd.getRuleCode() == null || cmd.getRuleCode().isBlank())
                ? "NORMAL" : cmd.getRuleCode();
        PricingResult pricing = pricingService.calculate(ruleCodes, pricingRequest);

        // 责任链：卡状态 → 密码 → 单笔限额 → 日累计 → 余额 → 扣款
        PayContext ctx = new PayContext();
        ctx.setRequestNo(cmd.getRequestNo());
        ctx.setCardNo(cmd.getCardNo());
        ctx.setCard(card);
        ctx.setAccount(account);
        ctx.setTerminal(terminal);
        ctx.setPayAmount(pricing.getPayAmount());
        ctx.setPayPwd(cmd.getPayPwd());
        payChain.execute(ctx);

        // 积分抵扣在同一事务里扣掉
        if (pricing.getPointsDelta() != null && pricing.getPointsDelta() < 0) {
            accountMapper.addPoints(account.getId(), pricing.getPointsDelta());
        }

        // 建造者组装交易单并落库
        String tradeNo = TradeNoGenerator.getInstance().next();
        TransactionTicket ticket = director.buildConsume(tradeNo, cmd.getRequestNo(), cmd.getCardNo(),
                account.getId(), terminal, pricing, cmd.getRemark());
        tradeMapper.insert(ticket.getTrade());

        // 观察者广播
        publisher.publish(new TradeEvent(ticket.getTrade(), account, terminal.getTerminalName()));
        return ticket.getTrade();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Trade recharge(RechargeCommand cmd) {
        if (cmd.getAmount() == null || cmd.getAmount().compareTo(java.math.BigDecimal.ZERO) <= 0) {
            throw new BizException(ErrorCode.AMOUNT_INVALID, "充值金额必须大于 0");
        }
        Trade existed = tradeMapper.findByRequestNo(cmd.getRequestNo());
        if (existed != null) {
            throw new BizException(ErrorCode.DUPLICATE_REQUEST, "该充值请求已处理");
        }
        Card card = cardMapper.findByCardNo(cmd.getCardNo());
        if (card == null) {
            throw new BizException(ErrorCode.ACCOUNT_NOT_FOUND, "卡号不存在");
        }
        if (card.getCardStatus() != null && card.getCardStatus() == 3) {
            throw new BizException(ErrorCode.CARD_CANCELLED, "该卡已注销，不能充值");
        }
        Account account = accountMapper.findById(card.getAccountId());

        // 工厂：拿到充值交易，由它决定怎么记账
        AbstractTransaction tx = transactionFactory.create(TradeType.RECHARGE);
        tx.apply(accountMapper, account.getId(), Money.of(cmd.getAmount()));

        String tradeNo = TradeNoGenerator.getInstance().next();
        TransactionTicket ticket = director.buildRecharge(tradeNo, cmd.getRequestNo(), cmd.getCardNo(),
                account.getId(), Money.of(cmd.getAmount()), cmd.getRemark());
        tradeMapper.insert(ticket.getTrade());

        publisher.publish(new TradeEvent(ticket.getTrade(), account, null));
        return ticket.getTrade();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Trade refund(RefundCommand cmd) {
        Trade existed = tradeMapper.findByRequestNo(cmd.getRequestNo());
        if (existed != null) {
            throw new BizException(ErrorCode.DUPLICATE_REQUEST, "该退款请求已处理");
        }
        Trade origin = tradeMapper.findByTradeNo(cmd.getOriginTradeNo());
        if (origin == null) {
            throw new BizException(ErrorCode.TRADE_NOT_FOUND, "原交易不存在");
        }
        if (!TradeType.CONSUME.name().equals(origin.getTradeType())) {
            throw new BizException("只有消费交易可以退款");
        }
        if (origin.getStatus() != null && origin.getStatus() == TradeStatus.REVERSED.getCode()) {
            throw new BizException(ErrorCode.TRADE_ALREADY_REVERSED, "该交易已冲正过");
        }

        Account account = accountMapper.findById(origin.getAccountId());

        AbstractTransaction tx = transactionFactory.create(TradeType.REFUND);
        tx.apply(accountMapper, account.getId(), origin.getPayAmount());

        // 原单标记为已冲正，流水本身金额不动
        tradeMapper.updateStatus(origin.getTradeNo(), TradeStatus.REVERSED.getCode());

        String tradeNo = TradeNoGenerator.getInstance().next();
        TransactionTicket ticket = director.buildRefund(tradeNo, cmd.getRequestNo(), origin.getCardNo(),
                account.getId(), origin.getPayAmount(), origin.getTradeNo(), cmd.getRemark());
        tradeMapper.insert(ticket.getTrade());

        publisher.publish(new TradeEvent(ticket.getTrade(), account, null));
        return ticket.getTrade();
    }
}
