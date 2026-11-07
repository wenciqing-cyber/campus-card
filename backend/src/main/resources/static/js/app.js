const { createApp } = Vue;

const terminalSvgs = {
    POS: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="4" width="18" height="16" rx="2"/><path d="M7 9h10M7 13h6"/><circle cx="17" cy="16" r="1.2"/></svg>',
    WATER: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round"><path d="M12 3s5 6 5 10a5 5 0 0 1-10 0c0-4 5-10 5-10z"/><path d="M9.5 13.5a2.5 2.5 0 0 0 2.5 2.5"/></svg>',
    GATE: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round"><path d="M4 21V4a1 1 0 0 1 1-1h9a1 1 0 0 1 1 1v17"/><path d="M15 12h5"/><circle cx="11" cy="12" r="1"/><path d="M2 21h20"/></svg>'
};

createApp({
    data() {
        return {
            scrolled: false,
            refreshSec: 15,
            today: new Date().toLocaleDateString('zh-CN'),

            terminals: [],
            accounts: [],
            overview: {},
            trend: [],
            notifications: [],
            settlementRows: [],
            settleResult: null,
            settling: false,

            animated: ['0', '0', '0', '0'],

            terminalNo: 'POS-001',
            terminalType: 'POS',
            payForm: {
                cardNo: '622100000001',
                amount: 12.5,
                ruleCode: 'NORMAL',
                payPwd: '123456'
            },
            paying: false,
            lastResult: null,

            filter: { cardNo: '', tradeType: '', terminalNo: '' },
            trades: { rows: [], total: 0, page: 1, size: 8 },

            newAccount: { studentNo: '', studentName: '', dept: '', payPwd: '' },
            opening: false,

            recharge: { show: false, cardNo: '', name: '', amount: 100, loading: false },

            toast: { show: false, text: '', type: 'info' },
            timers: [],
            searchTimer: null,
            revealObserver: null
        };
    },

    computed: {
        metrics() {
            const o = this.overview || {};
            return [
                { label: '账户总数', value: Number(o.accountCount || 0), prefix: '', unit: '个', hint: '含挂失与已注销' },
                { label: '累计消费', value: Number(o.consumeTotal || 0), prefix: '¥', unit: '', hint: '全部终端合计' },
                { label: '成功交易', value: Number(o.tradeCount || 0), prefix: '', unit: '笔', hint: '消费 / 充值 / 退款' },
                { label: '实时对账', value: Number(o.liveReconcileCount || 0), prefix: '', unit: '笔', hint: '观察者内存计数' }
            ];
        },
        amountLabel() {
            if (this.terminalType === 'WATER') return '用水量（升）';
            if (this.terminalType === 'GATE') return '金额（留空按次计费）';
            return '消费金额（元）';
        },
        amountPlaceholder() {
            if (this.terminalType === 'WATER') return '20';
            if (this.terminalType === 'GATE') return '留空 = ' + (this.currentRate || '0.00') + ' 元';
            return '12.50';
        },
        currentRate() {
            const t = this.terminals.find(x => x.terminalNo === this.terminalNo);
            return t && t.rate ? Number(t.rate).toFixed(2) : null;
        },
        resultClass() {
            if (!this.lastResult) return '';
            return this.lastResult.ok ? 'ok' : 'fail';
        },
        maxTrend() {
            return Math.max(...this.trend.map(d => Number(d.total) || 0), 1);
        },
        maxType() {
            const list = this.overview.byType || [];
            return Math.max(...list.map(t => Number(t.total) || 0), 1);
        },
        maxSettle() {
            const list = this.overview.byTerminal || [];
            return Math.max(...list.map(t => Number(t.total) || 0), 1);
        }
    },

    mounted() {
        this.loadAll();
        window.addEventListener('scroll', this.onScroll, { passive: true });
        window.addEventListener('keydown', this.onKeydown);
        this.onScroll();
        this.revealObserver = new IntersectionObserver(entries => {
            entries.forEach(e => {
                if (e.isIntersecting) {
                    e.target.classList.add('in');
                    this.revealObserver.unobserve(e.target);
                }
            });
        }, { threshold: 0.12, rootMargin: '0px 0px -40px 0px' });
        this.observeReveals();

        // 定时刷新概览，让观察者计数动起来
        const t = setInterval(() => {
            this.loadOverview();
            this.loadNotifications();
        }, this.refreshSec * 1000);
        this.timers.push(t);
    },

    beforeUnmount() {
        this.timers.forEach(t => clearInterval(t));
        window.removeEventListener('scroll', this.onScroll);
        window.removeEventListener('keydown', this.onKeydown);
        clearTimeout(this.searchTimer);
        if (this.revealObserver) this.revealObserver.disconnect();
    },

    methods: {
        /* ---------- 数据加载 ---------- */
        async loadAll() {
            await Promise.all([
                this.loadTerminals(),
                this.loadAccounts(),
                this.loadOverview(),
                this.loadTrend(),
                this.loadNotifications()
            ]);
            this.loadTrades(1);
            this.$nextTick(() => this.observeReveals());
        },

        async loadTerminals() {
            try {
                this.terminals = await API.terminals();
                const cur = this.terminals.find(t => t.terminalNo === this.terminalNo);
                if (cur) this.terminalType = cur.terminalType;
                else if (this.terminals.length) this.pickTerminal(this.terminals[0]);
            } catch (e) { this.showToast(e.message, 'error'); }
        },

        async loadAccounts() {
            try {
                this.accounts = await API.accounts();
            } catch (e) { this.showToast(e.message, 'error'); }
        },

        async loadOverview() {
            try {
                this.overview = await API.overview();
                this.$nextTick(() => {
                    this.animateNumbers();
                    this.observeReveals();
                });
            } catch (e) { /* 轮询失败不打扰用户 */ }
        },

        async loadTrend() {
            try {
                this.trend = await API.trend(7);
            } catch (e) { /* ignore */ }
        },

        async loadNotifications() {
            try {
                this.notifications = await API.notifications();
            } catch (e) { /* ignore */ }
        },

        async loadTrades(page) {
            const p = Math.max(1, page || 1);
            try {
                const data = await API.trades({
                    cardNo: this.filter.cardNo,
                    tradeType: this.filter.tradeType,
                    terminalNo: this.filter.terminalNo,
                    page: p,
                    size: this.trades.size
                });
                this.trades = { rows: data.rows || [], total: data.total || 0, page: data.page || p, size: data.size || this.trades.size };
            } catch (e) {
                this.showToast(e.message, 'error');
            }
        },

        resetFilter() {
            this.filter = { cardNo: '', tradeType: '', terminalNo: '' };
            this.loadTrades(1);
        },

        /* 卡号输入停 300 毫秒再查，避免敲一个字符打一次接口 */
        onCardNoInput() {
            clearTimeout(this.searchTimer);
            this.searchTimer = setTimeout(() => this.loadTrades(1), 300);
        },

        /* 交易号太长，手动拖选很烦，点一下直接进剪贴板 */
        async copyText(text) {
            try {
                await navigator.clipboard.writeText(text);
                this.showToast('已复制 ' + text, 'success');
            } catch (e) {
                this.showToast('复制失败，请手动选中', 'error');
            }
        },

        /*
         * 全局快捷键
         * Esc 关充值浮层；/ 跳到流水卡号搜索框（光标不在输入框里时才生效）
         */
        onKeydown(e) {
            if (e.key === 'Escape' && this.recharge.show) {
                this.recharge.show = false;
                return;
            }
            if (e.key === '/' && !/^(INPUT|TEXTAREA|SELECT)$/.test(document.activeElement.tagName)) {
                const box = document.querySelector('.filter-bar input');
                if (box) {
                    e.preventDefault();
                    box.scrollIntoView({ behavior: 'smooth', block: 'center' });
                    box.focus({ preventScroll: true });
                }
            }
        },

        /* ---------- 交易 ---------- */
        pickTerminal(t) {
            if (t.status === 0) return;
            this.terminalNo = t.terminalNo;
            this.terminalType = t.terminalType;
        },

        async doConsume() {
            if (this.paying) return;
            const km = this.payForm;
            if (!km.cardNo) return this.showToast('请填写卡号', 'error');

            const payload = {};
            if (this.terminalType === 'WATER') {
                if (!km.amount || km.amount <= 0) return this.showToast('请填写用水量', 'error');
                payload.volume = km.amount;
            } else if (this.terminalType === 'GATE') {
                if (km.amount) payload.amount = km.amount;
            } else {
                if (!km.amount || km.amount <= 0) return this.showToast('请填写消费金额', 'error');
                payload.amount = km.amount;
            }

            this.paying = true;
            try {
                const trade = await API.consume({
                    terminalNo: this.terminalNo,
                    cardNo: km.cardNo,
                    payPwd: km.payPwd,
                    requestNo: 'WEB' + Date.now() + Math.floor(Math.random() * 1000),
                    ruleCode: km.ruleCode,
                    payload: payload,
                    remark: '前端模拟终端'
                });
                this.lastResult = {
                    ok: true,
                    title: '支付成功',
                    detail: '终端已放行，余额实时更新',
                    rows: [
                        { k: '交易号', v: trade.tradeNo },
                        { k: '原价', v: '¥' + this.money(trade.originAmount) },
                        { k: '优惠', v: Number(trade.discountAmount) > 0 ? '-¥' + this.money(trade.discountAmount) : '无' },
                        { k: '实付', v: '¥' + this.money(trade.payAmount) },
                        { k: '积分变动', v: (trade.pointsDelta || 0) > 0 ? '+' + trade.pointsDelta : (trade.pointsDelta === 0 ? '0' : trade.pointsDelta) }
                    ]
                };
                this.showToast('支付成功，已扣款 ¥' + this.money(trade.payAmount), 'success');
                this.afterTrade();
            } catch (e) {
                this.lastResult = { ok: false, title: '交易失败', detail: e.message, rows: [] };
                this.showToast(e.message, 'error');
            } finally {
                this.paying = false;
            }
        },

        async doRefund(tradeNo) {
            if (!window.confirm('确认对 ' + tradeNo + ' 发起全额退款冲正？')) return;
            try {
                const trade = await API.refund({
                    originTradeNo: tradeNo,
                    requestNo: 'RFD' + Date.now()
                });
                this.showToast('已退款 ¥' + this.money(trade.payAmount), 'success');
                this.lastResult = {
                    ok: true,
                    title: '退款成功',
                    detail: '原单已标记冲正，新增一张反向流水',
                    rows: [
                        { k: '退款单号', v: trade.tradeNo },
                        { k: '原单号', v: tradeNo },
                        { k: '金额', v: '¥' + this.money(trade.payAmount) }
                    ]
                };
                this.afterTrade();
            } catch (e) {
                this.showToast(e.message, 'error');
            }
        },

        afterTrade() {
            this.loadAccounts();
            this.loadOverview();
            this.loadTrades(this.trades.page);
            this.loadNotifications();
        },

        /* ---------- 账户 ---------- */
        async createAccount() {
            if (this.opening) return;
            if (!this.newAccount.studentNo) return this.showToast('请填写学号', 'error');
            if (!this.newAccount.studentName) return this.showToast('请填写姓名', 'error');
            this.opening = true;
            try {
                await API.openAccount({
                    studentNo: this.newAccount.studentNo,
                    studentName: this.newAccount.studentName,
                    dept: this.newAccount.dept,
                    payPwd: this.newAccount.payPwd || '123456'
                });
                this.showToast('开户成功，卡号已生成', 'success');
                this.newAccount = { studentNo: '', studentName: '', dept: '', payPwd: '' };
                this.loadAccounts();
                this.loadOverview();
            } catch (e) {
                this.showToast(e.message, 'error');
            } finally {
                this.opening = false;
            }
        },

        async changeStatus(cardNo, status) {
            const word = status === 2 ? '挂失' : '解挂';
            if (!window.confirm('确认将 ' + cardNo + ' ' + word + '？')) return;
            try {
                await API.changeStatus(cardNo, status);
                this.showToast(word + '成功', 'success');
                this.loadAccounts();
            } catch (e) {
                this.showToast(e.message, 'error');
            }
        },

        openRecharge(a) {
            this.recharge = { show: true, cardNo: a.cardNo, name: a.studentName, amount: 100, loading: false };
        },

        async doRecharge() {
            if (this.recharge.loading) return;
            const amt = Number(this.recharge.amount);
            if (!amt || amt <= 0) return this.showToast('请输入正确的充值金额', 'error');
            if (amt > 5000) return this.showToast('单次充值不超过 5000 元', 'error');
            this.recharge.loading = true;
            try {
                const t = await API.recharge({
                    cardNo: this.recharge.cardNo,
                    amount: amt,
                    requestNo: 'R' + Date.now() + Math.floor(Math.random() * 1000)
                });
                this.showToast('充值成功 ¥' + this.money(amt) + ' · ' + t.tradeNo, 'success');
                this.recharge.show = false;
                this.loadAccounts();
                this.loadOverview();
                this.loadTrades();
            } catch (e) {
                this.showToast(e.message, 'error');
            } finally {
                this.recharge.loading = false;
            }
        },

        async openAccountDetail(cardNo) {
            try {
                const d = await API.accountDetail(cardNo);
                this.lastResult = {
                    ok: true,
                    title: d.studentName + ' · ' + d.cardNo,
                    detail: d.cardStatusLabel + ' · ' + (d.dept || '未填写院系'),
                    rows: [
                        { k: '余额', v: '¥' + this.money(d.balance) },
                        { k: '积分', v: d.points },
                        { k: '今日消费', v: '¥' + this.money(d.todayConsume) },
                        { k: '开户时间', v: d.issuedAt || '—' }
                    ]
                };
                document.getElementById('pay').scrollIntoView({ behavior: 'smooth' });
            } catch (e) {
                this.showToast(e.message, 'error');
            }
        },

        /* ---------- 结算 ---------- */
        async runSettlement() {
            if (this.settling) return;
            this.settling = true;
            try {
                const rows = await API.runSettlement();
                this.settleResult = rows || [];
                this.showToast('日终结账完成，共 ' + this.settleResult.length + ' 个终端', 'success');
                this.loadOverview();
            } catch (e) {
                this.showToast(e.message, 'error');
            } finally {
                this.settling = false;
            }
        },

        /* ---------- 交互 ---------- */
        onScroll() {
            this.scrolled = window.scrollY > 20;
        },

        observeReveals() {
            const nodes = document.querySelectorAll('.reveal:not(.in)');
            nodes.forEach(n => this.revealObserver && this.revealObserver.observe(n));
        },

        animateNumbers() {
            this.metrics.forEach((m, i) => {
                const target = Number(m.value) || 0;
                const dec = m.prefix === '¥' ? 2 : 0;
                const from = Number(this.animated[i]) || 0;
                this.tween(from, target, 900, v => {
                    this.animated[i] = v.toFixed(dec);
                });
            });
        },

        tween(from, to, ms, cb) {
            const start = performance.now();
            const step = now => {
                const p = Math.min((now - start) / ms, 1);
                const eased = 1 - Math.pow(1 - p, 3);
                cb(from + (to - from) * eased);
                if (p < 1) requestAnimationFrame(step);
            };
            requestAnimationFrame(step);
        },

        showToast(text, type) {
            this.toast = { show: true, text: text, type: type || 'info' };
            clearTimeout(this.toastTimer);
            this.toastTimer = setTimeout(() => { this.toast.show = false; }, 3200);
        },

        /* ---------- 展示辅助 ---------- */
        terminalIcon(type) {
            return terminalSvgs[type] || terminalSvgs.POS;
        },

        terminalTypeLabel(type) {
            return { POS: '收银', GATE: '门禁', WATER: '水控' }[type] || type;
        },

        typeLabel(type) {
            return { CONSUME: '消费', RECHARGE: '充值', REFUND: '退款' }[type] || type;
        },

        statusLabel(s) {
            return { 1: '成功', 2: '失败', 3: '已冲正' }[s] || s;
        },

        accountStatusLabel(s) {
            return { 1: '正常', 2: '挂失', 3: '已销户' }[s] || s;
        },

        money(v) {
            return Number(v || 0).toFixed(2);
        },

        shortTime(iso) {
            if (!iso) return '';
            return String(iso).replace('T', ' ').slice(11, 19);
        },

        shortDay(day) {
            const s = String(day || '');
            return s.length >= 10 ? s.slice(5, 10).replace('-', '/') : s;
        },

        barHeight(total) {
            return Math.max(6, Math.round((Number(total) || 0) / this.maxTrend * 100));
        },

        typePercent(total) {
            return Math.max(4, Math.round((Number(total) || 0) / this.maxType * 100));
        },

        settlePercent(total) {
            return Math.max(4, Math.round((Number(total) || 0) / this.maxSettle * 100));
        }
    }
}).mount('#app');
