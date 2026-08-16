package com.zionysus.dearme.south.adapter.listener;

import java.io.IOException;
import java.io.StringReader;
import java.util.Properties;

/**
 * 南向网关配置监听器基类。
 *
 * 子类实现「从哪里订阅 + 怎么拉初始配置」（init）与「推下来怎么应用」（applyConfig）。
 * 生命周期钩子：init 在容器就绪后启动订阅，shutdown 在销毁时释放连接（默认 no-op）。
 *
 * parseProperties 是推下文本 → Properties 的公共解析，各配置源共用。
 */
public abstract class ConfigListener {

    /** 启动订阅。返回 void 不抛异常，本地无配置源时仅 log，走 application defaults。 */
    public abstract void init();

    /** 应用推下来的配置文本。任何失败都应被内部吞掉，不打断调用方。 */
    public abstract void applyConfig(String configInfo);

    /** 释放连接资源。默认 no-op，需要关闭资源的子类覆写。 */
    public void shutdown() {
    }

    protected static Properties parseProperties(String configInfo) throws IOException {
        Properties properties = new Properties();
        properties.load(new StringReader(configInfo));
        return properties;
    }
}
