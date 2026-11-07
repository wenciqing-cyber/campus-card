package com.lwb.campuscard.pattern.adapter;

import com.lwb.campuscard.common.BizException;
import com.lwb.campuscard.model.entity.Terminal;
import com.lwb.campuscard.model.enums.TerminalType;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * 适配器注册表：按终端类型找对应适配器
 */
@Component
public class TerminalAdapterRegistry {

    private final Map<TerminalType, TerminalAdapter> map = new EnumMap<>(TerminalType.class);

    public TerminalAdapterRegistry(List<TerminalAdapter> adapters) {
        for (TerminalAdapter a : adapters) {
            map.put(a.support(), a);
        }
    }

    public PayRequest adapt(RawTerminalMessage raw, Terminal terminal) {
        TerminalType type = TerminalType.of(terminal.getTerminalType());
        TerminalAdapter adapter = map.get(type);
        if (adapter == null) {
            throw new BizException("没有匹配的终端适配器: " + terminal.getTerminalType());
        }
        return adapter.adapt(raw, terminal);
    }
}
