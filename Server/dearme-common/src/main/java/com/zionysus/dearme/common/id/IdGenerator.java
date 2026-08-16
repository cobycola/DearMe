package com.zionysus.dearme.common.id;

import java.util.UUID;

/**
 * ID 生成抽象。
 *
 * 抽出来便于：
 *   - 测试时替换为固定 ID（断言确定性）
 *   - 未来换 Snowflake / DB 序列等方案
 *
 * 默认实现用 JDK UUID（去掉横线、22 字符以内的紧凑型足够 MVP 用）。
 */
public interface IdGenerator {

    String newId();

    /** 默认实现：UUID 不带横线，32 字符。 */
    IdGenerator DEFAULT = () -> UUID.randomUUID().toString().replace("-", "");
}