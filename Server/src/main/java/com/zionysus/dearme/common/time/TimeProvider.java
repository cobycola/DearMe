package com.zionysus.dearme.common.time;

import java.time.Clock;
import java.time.Instant;

/**
 * 时间提供抽象。
 *
 * 抽出来便于：
 *   - 测试用 {@link #fixed(Instant)} 固定时间，断言确定性
 *   - 未来多时区/业务时间源切换
 *
 * 默认实现用 {@link Clock#systemUTC()}。
 */
public interface TimeProvider {

    Instant now();

    /** 系统默认 UTC 时钟。 */
    TimeProvider DEFAULT = () -> Instant.now();

    /** 测试用固定时间。 */
    static TimeProvider fixed(Instant instant) {
        Clock clock = Clock.fixed(instant, java.time.ZoneOffset.UTC);
        return () -> Instant.now(clock);
    }
}