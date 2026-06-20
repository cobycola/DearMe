export class WeightedScoring {
  calculate(answers, questions) {
    const scores = {};
    for (const q of questions) {
      const oid = answers.get(q.id);
      if (!oid) continue;           // 未作答跳过
      const opt = q.options.find(o => o.id === oid);
      if (!opt || !opt.weights) continue;
      for (const [key, w] of Object.entries(opt.weights)) {
        if (typeof w !== 'number') continue;
        scores[key] = (scores[key] || 0) + w;
      }
    }
    return scores;
  }
}

// 未来扩展
export class TypeMatchScoring {
  calculate(answers, questions) { return {}; }
}
