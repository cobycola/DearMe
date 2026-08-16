package com.zionysus.dearme.ohs.local.node;

/**
 * 流程编排节点契约（对应 pay-settle-front ohs-local 的 NodeService）。
 *
 * 每个用例拆成一个 Node，AppService 按顺序串链各 Node，Node 之间互不依赖，
 * 只通过流程上下文（T）传递状态。
 */
public interface NodeService<T> {

    void execute(T context);
}
