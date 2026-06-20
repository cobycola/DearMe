import { WeightedScoring } from '../core/scorer.js';

export const meta = {
  id: 'color',
  title: '你的隐藏性格色彩',
  description: '7 道题，揭开你性格底色的秘密。'
};

export const questions = [
  { id: 'q1', text: '和朋友聚会时，你通常是？',
    options: [
      { id: 'a', text: '话题中心，带动气氛的那一个', weights: { 橙: 5, 红: 3 } },
      { id: 'b', text: '安静倾听，偶尔插几句', weights: { 蓝: 5, 绿: 3 } },
      { id: 'c', text: '帮忙张罗，确保每个人都被照顾到', weights: { 黄: 5, 绿: 3 } },
      { id: 'd', text: '观察每个人的反应，心里默默分析', weights: { 紫: 5, 蓝: 3 } }
    ]},
  { id: 'q2', text: '做重大决定时，你更依赖什么？',
    options: [
      { id: 'a', text: '直觉和当下的感觉', weights: { 红: 5, 橙: 3 } },
      { id: 'b', text: '数据和逻辑分析', weights: { 蓝: 5, 紫: 3 } },
      { id: 'c', text: '对周围人影响的考量', weights: { 绿: 5, 黄: 3 } },
      { id: 'd', text: '内心价值观的指引', weights: { 紫: 5, 蓝: 3 } }
    ]},
  { id: 'q3', text: '被批评时，你的第一反应？',
    options: [
      { id: 'a', text: '立刻反驳，捍卫自己', weights: { 红: 5, 橙: 3 } },
      { id: 'b', text: '沉默消化，之后再想', weights: { 蓝: 5, 紫: 3 } },
      { id: 'c', text: '先理解对方为什么这么说', weights: { 绿: 5, 黄: 3 } },
      { id: 'd', text: '有点受伤，但还是谢谢对方', weights: { 黄: 5, 绿: 3 } }
    ]},
  { id: 'q4', text: '你理想中的周末夜晚？',
    options: [
      { id: 'a', text: '去 livehouse 或派对，释放能量', weights: { 红: 5, 橙: 3 } },
      { id: 'b', text: '在家看电影、看书、写日记', weights: { 蓝: 5, 紫: 3 } },
      { id: 'c', text: '约几个好友吃顿饭聊聊天', weights: { 橙: 5, 黄: 3 } },
      { id: 'd', text: '散步或做瑜伽，和自己相处', weights: { 绿: 5, 蓝: 3 } }
    ]},
  { id: 'q5', text: '别人对你的第一印象通常是？',
    options: [
      { id: 'a', text: '有气场，不太好接近', weights: { 红: 5, 紫: 3 } },
      { id: 'b', text: '温和友善，相处舒服', weights: { 绿: 5, 黄: 3 } },
      { id: 'c', text: '开朗有趣，笑点低', weights: { 橙: 5, 黄: 3 } },
      { id: 'd', text: '沉稳有深度，话不多', weights: { 蓝: 5, 紫: 3 } }
    ]},
  { id: 'q6', text: '你最能从什么事情中获得力量？',
    options: [
      { id: 'a', text: '被认可、被看见、被表扬', weights: { 红: 5, 橙: 3 } },
      { id: 'b', text: '独处时和内心的对话', weights: { 蓝: 5, 紫: 3 } },
      { id: 'c', text: '帮助别人、被别人需要', weights: { 黄: 5, 绿: 3 } },
      { id: 'd', text: '走进大自然、感受日落和风', weights: { 绿: 5, 蓝: 3 } }
    ]},
  { id: 'q7', text: '你最难忍受什么样的人？',
    options: [
      { id: 'a', text: '拖拖拉拉、犹豫不决的人', weights: { 红: 5, 橙: 3 } },
      { id: 'b', text: '情绪化、不讲逻辑的人', weights: { 蓝: 5, 紫: 3 } },
      { id: 'c', text: '冷漠自私、不关心别人感受的人', weights: { 黄: 5, 绿: 3 } },
      { id: 'd', text: '浮夸做作、不真实的人', weights: { 紫: 5, 绿: 3 } }
    ]}
];

export const scoring = new WeightedScoring();

const colorProfiles = {
  '红': { subtitle: '热烈而赤诚的行动派', summary: '你是那种想到什么就去做什么的人。勇气和行动力是你的底色——不喜欢犹豫，也不喜欢被人拖慢。你像一团火，靠近你的人都会感觉到热度和能量。偶尔会急躁，但你从来不缺重新站起来的力气。', traits: ['果敢', '热情', '有领导力'], vibes: '烈火 · 速度 · 竞技场 · 掌声 · 不服输' },
  '蓝': { subtitle: '冷静深邃的思考者', summary: '你的内心是一座安静但深邃的图书馆。你不是不想说，而是在别人还在喧哗时，你已经在思考问题的本质。蓝色性格的人对逻辑和秩序有天然的亲近——你的力量不是爆发力，是稳定和深度。', traits: ['理性', '专注', '可靠'], vibes: '书房 · 星空 · 逻辑 · 独处 · 深海' },
  '绿': { subtitle: '自然平和的治愈系', summary: '你是朋友圈里那个「让人感到安心」的存在。不喜欢冲突，也不太在意名利——你更希望世界是温柔而有序的。绿色性格的你像一棵树：安静地站着，却给很多人提供了荫蔽。', traits: ['平和', '包容', '治愈'], vibes: '森林 · 植物 · 清晨 · 内敛 · 宁静' },
  '黄': { subtitle: '阳光暖心的共情者', summary: '你最擅长的事是让别人感到被理解和被在乎。你的底色是温暖的——你会在意别人的感受，也会默默为身边的人付出。不需要站在舞台中央，但总有人需要在你的光里取暖。', traits: ['温暖', '共情', '善良'], vibes: '阳光 · 向日葵 · 微笑 · 拥抱 · 细节' },
  '橙': { subtitle: '鲜活灵动的快乐源泉', summary: '你是人群中那个自带感染力的人。乐观、幽默、总能找到让人发笑的角度——这不是肤浅，而是一种了不起的能力。橙色性格让周围的人感到轻松，你带来的快乐是真切的。', traits: ['乐观', '有趣', '有感染力'], vibes: '笑声 · 橘子汽水 · 派对 · 创意 · 自由' },
  '紫': { subtitle: '神秘而敏锐的洞察者', summary: '你拥有一种「看穿事物」的天赋。不是读心术，而是你对人性和情绪有一种天然的直觉。紫色性格的人往往是最有创造力的——你从独特的视角理解世界，这种独特就是你最迷人的部分。', traits: ['敏锐', '独特', '有灵性'], vibes: '黄昏 · 诗歌 · 梦境 · 直觉 · 例外' }
};

const FALLBACK_PROFILE = { subtitle: '独一无二的你', summary: '你的色彩不属于任何一个标签——这是最特别的地方。', traits: ['独特', '丰富', '自己定义'], vibes: '你的调色盘' };

const MAX_SCORE = 35; // 7 题 × 5

export function resultMapping(scores) {
  const sorted = Object.entries(scores).sort((a, b) => b[1] - a[1]);
  const [top, ...rest] = sorted;
  const topKey = top ? top[0] : '未知';
  return {
    primary: { key: topKey, name: topKey, score: top[1],
      matchPercent: Math.min(99, Math.round((top[1] / MAX_SCORE) * 100)),
      ...(colorProfiles[topKey] || FALLBACK_PROFILE) },
    runners: rest.slice(0, 3).map(([key, score]) => ({ key, name: key, score,
      matchPercent: Math.min(99, Math.round((score / MAX_SCORE) * 100)),
      ...(colorProfiles[key] || { subtitle: '', traits: [], vibes: '' }) })),
    timestamp: new Date().toISOString()
  };
}
