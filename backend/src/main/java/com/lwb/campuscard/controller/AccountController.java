package com.lwb.campuscard.controller;

import com.lwb.campuscard.common.Result;
import com.lwb.campuscard.model.dto.AccountCreateCommand;
import com.lwb.campuscard.model.entity.Account;
import com.lwb.campuscard.service.AccountService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 账户与卡片
 */
@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping
    public Result<List<Map<String, Object>>> list() {
        return Result.ok(accountService.listAll());
    }

    @PostMapping
    public Result<Account> open(@RequestBody AccountCreateCommand cmd) {
        return Result.ok(accountService.open(cmd));
    }

    @GetMapping("/{cardNo}")
    public Result<Map<String, Object>> detail(@PathVariable String cardNo) {
        return Result.ok(accountService.detail(cardNo));
    }

    /** status: 1 正常 2 挂失 3 注销 */
    @PostMapping("/{cardNo}/status")
    public Result<Void> changeStatus(@PathVariable String cardNo, @RequestParam int status) {
        accountService.changeStatus(cardNo, status);
        return Result.ok();
    }

    @PostMapping("/{cardNo}/password")
    public Result<Void> changePassword(@PathVariable String cardNo,
                                       @RequestParam String oldPwd,
                                       @RequestParam String newPwd) {
        accountService.changePassword(cardNo, oldPwd, newPwd);
        return Result.ok();
    }
}
