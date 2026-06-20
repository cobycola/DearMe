/**
 * 将原始得分转换为匹配度百分比。
 * 使用平方根变换拉高中低分区，使结果更符合直觉（典型区间 55%-85%）。
 *
 * @param {number} score - 原始得分
 * @param {number} maxScore - 理论满分（题目数 × 5）
 * @returns {number} 0-99 的整数百分比
 */
export function calculateMatchPercent(score, maxScore) {
  if (maxScore <= 0 || score <= 0) return 0;
  return Math.min(99, Math.round(Math.sqrt(score / maxScore) * 100));
}
