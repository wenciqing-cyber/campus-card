package com.lwb.campuscard.pattern.adapter;

import com.lwb.campuscard.model.entity.Terminal;
import com.lwb.campuscard.model.enums.TerminalType;

/**
 * 适配器模式：把不同终端的报文翻译成统一的 PayRequest
 * 新增一种终端（比如打印店扫码），加一个实现类即可
 */
public interface TerminalAdapter {

    TerminalType support();

    PayRequest adapt(RawTerminalMessage raw, Terminal terminal);
}
