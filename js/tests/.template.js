// 新建测试模板 — 复制此文件，改 meta / questions / profiles / MAX_SCORE / resultMapping
//
// MAX_SCORE = 题目数 × 5（每题每选项最高 5 分）
// resultMapping 用 score / MAX_SCORE * 100 计算匹配度

import { WeightedScoring } from '../core/scorer.js';
import { calculateMatchPercent } from '../core/utils.js';

export const meta = {
  id: 'template',
  title: '测试标题',
  description: '测试描述文案'
};

export const questions = [
  // { id: 'q1', text: '题目',
  //   options: [
  //     { id: 'a', text: '选项A', weights: { 结果A: 5, 结果B: 3 } },
  //     { id: 'b', text: '选项B', weights: { 结果C: 5, 结果D: 3 } }
  //   ]}
];

export const scoring = new WeightedScoring();

const profiles = {
  // '结果A': { subtitle: '副标题', summary: '总结文案', traits: ['特质1', '特质2'], vibes: '氛围词' }
};

const FALLBACK_PROFILE = { subtitle: '独一无二的你', summary: '你的选择不属于任何一个标签。', traits: ['独特'], vibes: '' };

const MAX_SCORE = 0; // 题目数 × 5

export function resultMapping(scores) {
  const sorted = Object.entries(scores).sort((a, b) => b[1] - a[1]);
  const [top, ...rest] = sorted;
  const topKey = top ? top[0] : '未知';
  return {
    primary: { key: topKey, name: topKey, score: top[1],
      matchPercent: calculateMatchPercent(top[1], MAX_SCORE),
      ...(profiles[topKey] || FALLBACK_PROFILE) },
    runners: rest.slice(0, 3).map(([key, score]) => ({ key, name: key, score,
      matchPercent: calculateMatchPercent(score, MAX_SCORE),
      ...(profiles[key] || { subtitle: '', traits: [], vibes: '' }) })),
    timestamp: new Date().toISOString()
  };
}
