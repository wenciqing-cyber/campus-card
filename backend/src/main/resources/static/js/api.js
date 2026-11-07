// 后端接口封装。同源直接用 /api，本地双击打开 html 时退回 8080
const BASE = location.protocol === 'file:' ? 'http://localhost:8080/api' : '/api';

async function http(path, options = {}) {
    const resp = await fetch(BASE + path, {
        headers: { 'Content-Type': 'application/json' },
        ...options
    });
    let json;
    try {
        json = await resp.json();
    } catch (e) {
        throw new Error('服务无响应，检查后端是否启动');
    }
    if (json.code !== 0) {
        throw new Error(json.message || '请求失败');
    }
    return json.data;
}

function qs(params) {
    return Object.keys(params)
        .filter(k => params[k] !== '' && params[k] !== null && params[k] !== undefined)
        .map(k => encodeURIComponent(k) + '=' + encodeURIComponent(params[k]))
        .join('&');
}

window.API = {
    // 账户
    accounts: () => http('/accounts'),
    openAccount: body => http('/accounts', { method: 'POST', body: JSON.stringify(body) }),
    accountDetail: cardNo => http('/accounts/' + cardNo),
    changeStatus: (cardNo, status) =>
        http('/accounts/' + cardNo + '/status?status=' + status, { method: 'POST' }),

    // 交易
    consume: body => http('/pay/consume', { method: 'POST', body: JSON.stringify(body) }),
    recharge: body => http('/pay/recharge', { method: 'POST', body: JSON.stringify(body) }),
    refund: body => http('/pay/refund', { method: 'POST', body: JSON.stringify(body) }),
    trades: params => http('/trades?' + qs(params)),

    // 查询统计
    overview: () => http('/stats/overview'),
    trend: days => http('/stats/trend?days=' + days),
    byType: () => http('/stats/type'),
    byTerminal: () => http('/stats/terminal'),
    notifications: () => http('/notifications'),
    reconciliation: () => http('/reconciliation'),
    logs: limit => http('/logs?limit=' + limit),

    // 基础资料
    terminals: () => http('/terminals'),
    rules: () => http('/rules'),
    config: () => http('/config'),
    runSettlement: date => http('/settlement/run' + (date ? '?date=' + date : ''), { method: 'POST' }),
    settlement: date => http('/settlement' + (date ? '?date=' + date : ''))
};
