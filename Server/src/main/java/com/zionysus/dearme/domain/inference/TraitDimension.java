package com.zionysus.dearme.domain.inference;

/**
 * 特征维度。连续值尺度，两极命名。
 * 候选人物的维度值落在 [-1.0, +1.0] 上某点。
 *
 * 维度命名约定：A_B 表示 B 端为 +1.0，A 端为 -1.0。
 */
public enum TraitDimension {
    INTROVERT_EXTROVERT,   // 内向 ~ 外向
    IDEALIST_PRAGMATIST,   // 理想 ~ 现实
    LOGIC_EMOTION,         // 理性 ~ 感性
    TEAM_INDEPENDENT,      // 集体 ~ 独立
    RISK_CAUTION,          // 冒险 ~ 谨慎
    OPTIMIST_PESSIMIST,    // 乐观 ~ 悲观
    DUTY_FREEDOM,          // 责任 ~ 自由
    WARMTH_RESERVE         // 温暖 ~ 克制
}