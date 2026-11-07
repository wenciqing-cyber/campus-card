package com.lwb.campuscard.service;

import com.lwb.campuscard.common.BizException;
import com.lwb.campuscard.common.ErrorCode;
import com.lwb.campuscard.dao.AccountMapper;
import com.lwb.campuscard.dao.CardMapper;
import com.lwb.campuscard.dao.TradeMapper;
import com.lwb.campuscard.model.dto.AccountCreateCommand;
import com.lwb.campuscard.model.entity.Account;
import com.lwb.campuscard.model.entity.Card;
import com.lwb.campuscard.model.enums.CardStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 账户与卡片管理：开户、挂失、解挂、销户
 * 账户状态和卡片状态始终一起变，所以放在同一个事务里
 */
@Service
public class AccountService {

    private final AccountMapper accountMapper;
    private final CardMapper cardMapper;
    private final TradeMapper tradeMapper;

    public AccountService(AccountMapper accountMapper, CardMapper cardMapper, TradeMapper tradeMapper) {
        this.accountMapper = accountMapper;
        this.cardMapper = cardMapper;
        this.tradeMapper = tradeMapper;
    }

    @Transactional(rollbackFor = Exception.class)
    public Account open(AccountCreateCommand cmd) {
        if (cmd.getStudentNo() == null || cmd.getStudentNo().trim().isEmpty()) {
            throw new BizException("学号不能为空");
        }
        if (accountMapper.findByStudentNo(cmd.getStudentNo()) != null) {
            throw new BizException("学号 " + cmd.getStudentNo() + " 已开过户");
        }

        Account account = new Account();
        account.setStudentNo(cmd.getStudentNo().trim());
        account.setStudentName(cmd.getStudentName());
        account.setDept(cmd.getDept());
        account.setBalance(BigDecimal.ZERO);
        account.setPoints(0);
        account.setStatus(CardStatus.NORMAL.getCode());
        accountMapper.insert(account);

        // 卡号用账户自增 id 拼，保证唯一且看起来像校园卡
        Card card = new Card();
        card.setCardNo("6221" + String.format("%08d", account.getId()));
        card.setAccountId(account.getId());
        card.setCardStatus(CardStatus.NORMAL.getCode());
        card.setPayPwd(cmd.getPayPwd() == null ? "123456" : cmd.getPayPwd());
        cardMapper.insert(card);

        return account;
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> listAll() {
        return cardMapper.listWithAccount();
    }

    @Transactional(readOnly = true)
    public Map<String, Object> detail(String cardNo) {
        Card card = cardMapper.findByCardNo(cardNo);
        if (card == null) {
            throw new BizException(ErrorCode.ACCOUNT_NOT_FOUND, "卡号不存在");
        }
        Account account = accountMapper.findById(card.getAccountId());
        Map<String, Object> map = new HashMap<>();
        map.put("cardNo", card.getCardNo());
        map.put("cardStatus", card.getCardStatus());
        map.put("cardStatusLabel", CardStatus.of(card.getCardStatus()).getLabel());
        map.put("issuedAt", card.getIssuedAt());
        map.put("studentNo", account.getStudentNo());
        map.put("studentName", account.getStudentName());
        map.put("dept", account.getDept());
        map.put("balance", account.getBalance());
        map.put("points", account.getPoints());
        map.put("status", account.getStatus());
        map.put("todayConsume", tradeMapper.sumConsumeToday(account.getId()));
        return map;
    }

    /**
     * 1 正常 2 挂失 3 销户
     */
    @Transactional(rollbackFor = Exception.class)
    public void changeStatus(String cardNo, int status) {
        Card card = cardMapper.findByCardNo(cardNo);
        if (card == null) {
            throw new BizException(ErrorCode.ACCOUNT_NOT_FOUND, "卡号不存在");
        }
        Account account = accountMapper.findById(card.getAccountId());
        if (status == CardStatus.CANCELLED.getCode()
                && account.getBalance().compareTo(BigDecimal.ZERO) > 0) {
            throw new BizException("卡内还有 " + account.getBalance().toPlainString() + " 元，请先退款再销户");
        }
        if (status == CardStatus.NORMAL.getCode()
                && card.getCardStatus() != null
                && card.getCardStatus() == CardStatus.CANCELLED.getCode()) {
            throw new BizException("已注销的卡不能恢复");
        }
        cardMapper.updateStatus(cardNo, status);
        accountMapper.updateStatus(card.getAccountId(), status);
    }

    @Transactional(rollbackFor = Exception.class)
    public void changePassword(String cardNo, String oldPwd, String newPwd) {
        Card card = cardMapper.findByCardNo(cardNo);
        if (card == null) {
            throw new BizException(ErrorCode.ACCOUNT_NOT_FOUND, "卡号不存在");
        }
        if (!card.getPayPwd().equals(oldPwd)) {
            throw new BizException(ErrorCode.PASSWORD_ERROR, "原密码不正确");
        }
        if (newPwd == null || newPwd.length() != 6) {
            throw new BizException(ErrorCode.PASSWORD_ERROR, "新密码必须是 6 位");
        }
        cardMapper.updatePwd(cardNo, newPwd);
    }
}
