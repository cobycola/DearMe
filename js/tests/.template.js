// 新建测试模板 — 复制此文件，改 meta / questions / scoring / resultMapping

import { WeightedScoring } from '../core/scorer.js';

export const meta = {
  id: 'template',
  title: '测试标题',
  description: '测试描述文案',
  totalQuestions: 0
};

export const questions = [
  // 格式:
  // { id: 'q1', text: '题目',
  //   options: [
  //     { id: 'a', text: '选项', weights: { 城市A: 3, 城市B: 1 } }
  //   ]}
];

export const scoring = new WeightedScoring();

export function resultMapping(scores) {
  const sorted = Object.entries(scores).sort((a, b) => b[1] - a[1]);
  const total = sorted.reduce((sum, [, s]) => sum + s, 0) || 1;
  const [top, ...rest] = sorted;
  return {
    primary: {
      key: top[0], name: top[0], score: top[1],
      matchPercent: Math.round((top[1] / total) * 100)
    },
    runners: rest.map(([key, score]) => ({
      key, name: key, score,
      matchPercent: Math.round((score / total) * 100)
    })),
    summary: '',
    timestamp: new Date().toISOString()
  };
}
