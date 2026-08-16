package com.zionysus.dearme.ohs.local.node;

/**
 * Node 模板方法基类（对应 pay-settle-front ohs-local 的 AbstractNodeService）。
 *
 * execute 固定三段式：before（守卫/数据准备，false 则跳过本节点）→ deal（业务）→ after（收尾/持久化）。
 * 本项目不引入 AOP 监控切面（无 UMP/pfinder 设施），仅保留编排骨架。
 */
public abstract class AbstractNodeService<T> implements NodeService<T> {

    @Override
    public void execute(T context) {
        if (before(context)) {
            deal(context);
            after(context);
        }
    }

    /** 节点前置守卫：返回 false 时跳过 deal/after。守卫失败应在实现内把失败写入 context。 */
    protected abstract boolean before(T context);

    /** 节点核心业务。 */
    protected abstract void deal(T context);

    /** 节点收尾（状态推进/持久化）。 */
    protected abstract void after(T context);
}
