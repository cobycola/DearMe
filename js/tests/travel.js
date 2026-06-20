import { WeightedScoring } from '../core/scorer.js';
import { calculateMatchPercent } from '../core/utils.js';

export const meta = {
  id: 'travel',
  title: '你的旅行人格',
  description: '9 道题，发现你属于哪种旅行者。'
};

export const questions = [
  { id: 'q1', text: '出发前，你通常会？',
    options: [
      { id: 'a', text: 'Excel 行程表精确到每小时', weights: { 攻略王: 5, 酒店控: 3 } },
      { id: 'b', text: '只订第一晚住宿，剩下随缘', weights: { 背包客: 5, 冒险家: 3 } },
      { id: 'c', text: '收藏了一堆咖啡馆和书店地址', weights: { 文艺青年: 5, 躺平派: 3 } },
      { id: 'd', text: '先查当地有什么必吃的美食', weights: { 美食猎人: 5, 攻略王: 3 } }
    ]},
  { id: 'q2', text: '你理想中的住宿是？',
    options: [
      { id: 'a', text: '五星酒店，必须有好床和浴缸', weights: { 酒店控: 5, 躺平派: 3 } },
      { id: 'b', text: '青旅多人间，便宜又热闹', weights: { 背包客: 5, 冒险家: 3 } },
      { id: 'c', text: '有特色的民宿，要有故事', weights: { 文艺青年: 5, 美食猎人: 3 } },
      { id: 'd', text: '海边小屋或山林木屋', weights: { 躺平派: 5, 摄影师: 3 } }
    ]},
  { id: 'q3', text: '旅行中你最在意的开销是？',
    options: [
      { id: 'a', text: '吃——来都来了，必须吃遍', weights: { 美食猎人: 5, 攻略王: 3 } },
      { id: 'b', text: '住——住得舒服才有精力玩', weights: { 酒店控: 5, 躺平派: 3 } },
      { id: 'c', text: '体验——跳伞、潜水、热气球', weights: { 冒险家: 5, 背包客: 3 } },
      { id: 'd', text: '购物和交通，不太计较', weights: { 背包客: 5, 文艺青年: 3 } }
    ]},
  { id: 'q4', text: '面对一个完全陌生的地方，你的第一反应是？',
    options: [
      { id: 'a', text: '兴奋！打开地图就开始规划路线', weights: { 攻略王: 5, 背包客: 3 } },
      { id: 'b', text: '找当地人聊天，问他们去哪吃去哪玩', weights: { 美食猎人: 5, 冒险家: 3 } },
      { id: 'c', text: '不急着走，先感受空气和光线', weights: { 文艺青年: 5, 摄影师: 3 } },
      { id: 'd', text: '找个风景好的地方坐下，什么都不想', weights: { 躺平派: 5, 文艺青年: 3 } }
    ]},
  { id: 'q5', text: '你的行李箱里一定会有？',
    options: [
      { id: 'a', text: '相机或拍立得，记录每一个瞬间', weights: { 摄影师: 5, 文艺青年: 3 } },
      { id: 'b', text: '一双能走一天路的鞋', weights: { 背包客: 5, 攻略王: 3 } },
      { id: 'c', text: '便携咖啡或小零食', weights: { 美食猎人: 5, 酒店控: 3 } },
      { id: 'd', text: 'kindle / 笔记本 / 耳机', weights: { 躺平派: 5, 文艺青年: 3 } }
    ]},
  { id: 'q6', text: '发现计划之外的意外时，你会？',
    options: [
      { id: 'a', text: '调整计划，但大方向不能乱', weights: { 攻略王: 5, 酒店控: 3 } },
      { id: 'b', text: '将错就错，说不定更好玩', weights: { 冒险家: 5, 背包客: 3 } },
      { id: 'c', text: '先吃顿好的，然后重新打算', weights: { 美食猎人: 5, 躺平派: 3 } },
      { id: 'd', text: '随缘，累了就休息', weights: { 躺平派: 5, 背包客: 3 } }
    ]},
  { id: 'q7', text: '你觉得旅行最珍贵的收获是？',
    options: [
      { id: 'a', text: '拍到了绝美的照片', weights: { 摄影师: 5, 文艺青年: 3 } },
      { id: 'b', text: '体验了从未做过的事', weights: { 冒险家: 5, 背包客: 3 } },
      { id: 'c', text: '吃到让人铭记一生的味道', weights: { 美食猎人: 5, 攻略王: 3 } },
      { id: 'd', text: '彻底放空，重新认识自己', weights: { 躺平派: 5, 文艺青年: 3 } }
    ]},
  { id: 'q8', text: '你更愿意和谁一起旅行？',
    options: [
      { id: 'a', text: '知己好友，一路有说有笑', weights: { 攻略王: 5, 美食猎人: 3 } },
      { id: 'b', text: '一个人，自由自在不用商量', weights: { 背包客: 5, 摄影师: 3 } },
      { id: 'c', text: '志同道合的小团体', weights: { 冒险家: 5, 背包客: 3 } },
      { id: 'd', text: '和伴侣或家人，享受彼此陪伴', weights: { 躺平派: 5, 酒店控: 3 } }
    ]},
  { id: 'q9', text: '你会怎么整理旅行的照片和记忆？',
    options: [
      { id: 'a', text: '修图发朋友圈/小红书，认真营业', weights: { 摄影师: 5, 攻略王: 3 } },
      { id: 'b', text: '随手拍，存着但不一定整理', weights: { 背包客: 5, 冒险家: 3 } },
      { id: 'c', text: '写旅行日记或做手账', weights: { 文艺青年: 5, 美食猎人: 3 } },
      { id: 'd', text: '记在心里就行，不太拍照', weights: { 躺平派: 5, 文艺青年: 3 } }
    ]}
];

export const scoring = new WeightedScoring();

const travelProfiles = {
  '背包客': { subtitle: '在路上，一切从简', summary: '你看重自由胜过享受，看重经历胜过打卡。一个背包、一张地图、一双走不坏的鞋——你的旅行没有太多预设，最好的风景总是在路上。你不怕陌生，甚至享受不确定带来的惊喜。', traits: ['自由', '独立', '随遇而安'], vibes: '青旅 · 地图 · 登山包 · 火车 · 日出' },
  '酒店控': { subtitle: '旅行是另一种生活方式的体验', summary: '你觉得旅行首先是善待自己。住得舒服才有精力探索——一张好床、一杯手冲、一个可以看到风景的阳台，这些不只是享受，是旅行仪式感的一部分。你愿意为品质买单，因为你值得。', traits: ['精致', '有品味', '懂享受'], vibes: '套房 · 下午茶 · 无边泳池 · 高脚杯 · 落日' },
  '文艺青年': { subtitle: '在世界的褶皱里寻找灵感', summary: '你旅行的目的地往往是内心情感的映射——一座有故事的老城、一家巷子深处的书店、一场不期而遇的黄昏。你不追求打卡，而是追求被某个瞬间击中灵魂的感觉。你的旅行是散文，不是攻略。', traits: ['敏感', '浪漫', '有审美'], vibes: '旧书店 · 咖啡渍 · 手账 · 胶片 · 雨季' },
  '冒险家': { subtitle: '永远在挑战舒适区的边界', summary: '安逸让你焦虑，刺激让你活着。跳伞、潜水、穿越无人区——你的旅行词典里没有"算了吧"三个字。你相信人在极限状态下才能看到最真实的自己。故事是冒险家的真正战利品。', traits: ['勇敢', '热血', '不设限'], vibes: '跳伞 · 攀岩 · 夜潜 · 沙漠 · 极限' },
  '美食猎人': { subtitle: '带着胃走遍世界', summary: '你的旅行是味觉驱动型——从米其林三星到街边苍蝇馆子，没有你不敢试的味道。你相信食物是一座城市最诚实的名片，吃到好的东西，就感觉和这个地方产生了真正的连结。', traits: ['好奇', '直接', '有烟火气'], vibes: '夜市 · 排档 · 早茶 · 香料 · 菜市场' },
  '躺平派': { subtitle: '换个地方晒太阳也是旅行', summary: '你的旅行不需要理由，也不需要日程。找一个舒服的地方，晒晒太阳、泡泡温泉、看看书——不做任何"有意义"的事本身就是最大的意义。你不赶路，你让路来经过你。', traits: ['松弛', '自洽', '不焦虑'], vibes: '吊床 · 海浪声 · 午觉 · 温泉 · 什么都不做' },
  '摄影师': { subtitle: '用镜头和光交流', summary: '你的眼睛自带取景框——对光影、色彩和构图的敏感让你总能发现别人忽略的美。旅行对你来说是一场持续的光影收集。你不只是为了发照片，你是在用镜头和世界对话。', traits: ['敏锐', '专注', '有视角'], vibes: '取景框 · 黄金时刻 · 暗房 · 长曝光 · 构图' },
  '攻略王': { subtitle: '信息差为零，体验感拉满', summary: '你不会让任何一次旅行"浪费"在一团混乱中。你的行程表是一份艺术品——高效、丰富、有备无患。同行的人常常惊叹于你总能找到最划算的机票和最地道的馆子。你不是焦虑，你是在为快乐铺路。', traits: ['规划力', '细致', '靠谱'], vibes: 'Excel · 比价 · 预约制 · 行程表 · 防雷' }
};

const FALLBACK_PROFILE = { subtitle: '独一无二的旅行者', summary: '你的旅行风格不属于任何一个标签——这本身就很酷。', traits: ['独特', '自由'], vibes: '你的路，自己定义' };

const MAX_SCORE = 45;

export function resultMapping(scores) {
  const sorted = Object.entries(scores).sort((a, b) => b[1] - a[1]);
  const [top, ...rest] = sorted;
  const topKey = top ? top[0] : '未知';
  return {
    primary: { key: topKey, name: topKey, score: top[1],
      matchPercent: calculateMatchPercent(top[1], MAX_SCORE),
      ...(travelProfiles[topKey] || FALLBACK_PROFILE) },
    runners: rest.slice(0, 3).map(([key, score]) => ({ key, name: key, score,
      matchPercent: calculateMatchPercent(score, MAX_SCORE),
      ...(travelProfiles[key] || { subtitle: '', traits: [], vibes: '' }) })),
    timestamp: new Date().toISOString()
  };
}
