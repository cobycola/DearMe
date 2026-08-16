package com.zionysus.dearme.acl.ports;

import com.zionysus.dearme.domain.inference.CharacterProfile;

import java.util.List;

/**
 * 候选人物来源端口（南向 outbound port）。
 *
 * 扩展点（agent 知识库定制方向）：
 *   - 当前 JsonResourceCharacterAdapter 静态加载
 *   - 未来 RAG 实现可动态从主题向量库检索
 *   - 未来 DB 实现可由运营后台维护
 */
public interface CharacterSourcePort {

    List<String> supportedTopics();

    List<CharacterProfile> characters(String topicId);
}