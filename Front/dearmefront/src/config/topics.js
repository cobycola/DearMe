// 硬编码主题元数据。后端 MVP 无 /topics 列表接口，前端暂培单主题。
// 题目内容不在此硬编码，由 first-question/answers 接口动态下发。
export const TOPICS = [
  {
    id: "anime-character",
    displayName: "看看我像哪个动漫人物",
    description: "通过逐题定制问卷，推理你最像的动漫角色",
    mood: "理性与浪漫之间，你落在哪一格？",
    priceYuan: 9.9,
  },
];

export function findTopic(id) {
  return TOPICS.find((t) => t.id === id);
}