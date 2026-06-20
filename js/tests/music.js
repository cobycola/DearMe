import { WeightedScoring } from '../core/scorer.js';

export const meta = {
  id: 'music',
  title: '你的音乐灵魂',
  description: '8 道题，发现你灵魂深处的旋律。'
};

export const questions = [
  { id: 'q1', text: '你通常在什么时候最想听音乐？',
    options: [
      { id: 'a', text: '通勤路上，需要给一天充电', weights: { 流行: 5, 摇滚: 3 } },
      { id: 'b', text: '深夜一个人，需要和自己对话', weights: { 民谣: 5, 古典: 3 } },
      { id: 'c', text: '工作或学习时，需要专心', weights: { 古典: 5, 电子: 3 } },
      { id: 'd', text: '运动或派对时，需要燃起来', weights: { 嘻哈: 5, 电子: 3 } }
    ]},
  { id: 'q2', text: '一首歌最打动你的是什么？',
    options: [
      { id: 'a', text: '旋律——好听的 hook 一遍就上头', weights: { 流行: 5, 电子: 3 } },
      { id: 'b', text: '歌词——像在写我的故事', weights: { 民谣: 5, 摇滚: 3 } },
      { id: 'c', text: '氛围——把我带到另一个世界', weights: { 电子: 5, 爵士: 3 } },
      { id: 'd', text: '节奏——身体不由自主想动', weights: { 嘻哈: 5, 摇滚: 3 } }
    ]},
  { id: 'q3', text: '如果要学一门乐器，你会选？',
    options: [
      { id: 'a', text: '吉他——抱着就能唱一整晚', weights: { 民谣: 5, 摇滚: 3 } },
      { id: 'b', text: '钢琴——黑白键里有全世界', weights: { 古典: 5, 爵士: 3 } },
      { id: 'c', text: '鼓——敲出来的力量最直接', weights: { 摇滚: 5, 嘻哈: 3 } },
      { id: 'd', text: '合成器或编曲软件', weights: { 电子: 5, 流行: 3 } }
    ]},
  { id: 'q4', text: '去 livehouse 或音乐节，你更想看？',
    options: [
      { id: 'a', text: '能全场大合唱的流行歌手', weights: { 流行: 5, 民谣: 3 } },
      { id: 'b', text: '吉他失真、全场 pogo 的乐队', weights: { 摇滚: 5, 嘻哈: 3 } },
      { id: 'c', text: '一直蹦到天亮都不停的 DJ', weights: { 电子: 5, 流行: 3 } },
      { id: 'd', text: '闭眼沉浸的爵士四重奏', weights: { 爵士: 5, 古典: 3 } }
    ]},
  { id: 'q5', text: '你心情不好的时候，会听什么？',
    options: [
      { id: 'a', text: '伤感情歌，让自己哭出来', weights: { 流行: 5, 民谣: 3 } },
      { id: 'b', text: '把音量开到最大的摇滚', weights: { 摇滚: 5, 嘻哈: 3 } },
      { id: 'c', text: 'loop 一首安静的钢琴曲', weights: { 古典: 5, 爵士: 3 } },
      { id: 'd', text: '一个 beat 循环，放空自己', weights: { 嘻哈: 5, 电子: 3 } }
    ]},
  { id: 'q6', text: '你发现一首好歌时的第一反应？',
    options: [
      { id: 'a', text: '分享到朋友圈或群里，让大家一起听', weights: { 流行: 5, 嘻哈: 3 } },
      { id: 'b', text: '默默收藏，单曲循环到腻', weights: { 民谣: 5, 古典: 3 } },
      { id: 'c', text: '放进对应心情的 playlist 里', weights: { 爵士: 5, 电子: 3 } },
      { id: 'd', text: '研究它的编曲和制作，扒细节', weights: { 电子: 5, 摇滚: 3 } }
    ]},
  { id: 'q7', text: '一句话描述音乐对你的意义？',
    options: [
      { id: 'a', text: '生活需要 BGM 才完整', weights: { 流行: 5, 电子: 3 } },
      { id: 'b', text: '一个人最好的朋友是耳机', weights: { 民谣: 5, 嘻哈: 3 } },
      { id: 'c', text: '没有音乐的生活简直不能想象', weights: { 摇滚: 5, 流行: 3 } },
      { id: 'd', text: '音乐是让我安静下来的唯一方式', weights: { 古典: 5, 爵士: 3 } }
    ]},
  { id: 'q8', text: '你觉得音乐和人生的关系是？',
    options: [
      { id: 'a', text: '人生像一首流行歌——有高潮有低谷，但会一直唱下去', weights: { 流行: 5, 民谣: 3 } },
      { id: 'b', text: '人生像一首叙事民谣——慢慢来，故事都在细节里', weights: { 民谣: 5, 古典: 3 } },
      { id: 'c', text: '人生像即兴演奏——没有固定谱子，随时都可以变', weights: { 爵士: 5, 嘻哈: 3 } },
      { id: 'd', text: '人生像电子混音——自己组合、混搭、创造', weights: { 电子: 5, 摇滚: 3 } }
    ]}
];

export const scoring = new WeightedScoring();

const musicProfiles = {
  '流行': { subtitle: '人人都能跟唱的灵魂共鸣', summary: '你认同自己所在的世代，也享受大众文化的温度。流行音乐不是没有个性——而是你愿意在共同的旋律里找到连接。你的歌单别人打开一看，每一首都耳熟能详——这不是没品味，是你在用声音搭建社交的桥梁。', traits: ['亲和', '开放', '有共鸣'], vibes: '大合唱 · 榜单 · 耳机 · KTV · 副歌' },
  '民谣': { subtitle: '歌词里的叙事诗人', summary: '你对文字和故事有天然的敏感。一首民谣就像一个朋友坐在你对面，用三分钟讲完一个关于爱、离别、或者成长的故事。你不赶时髦，你听的东西有质感——它不一定要多热闹，但要真实。', traits: ['细腻', '真诚', '有故事'], vibes: '木吉他 · 公路 · 酒 · 写信 · 黄昏' },
  '古典': { subtitle: '永恒的秩序与情绪', summary: '你喜欢有结构的美。古典音乐不是老派，而是一种经过时间检验的深度——巴赫的精密、肖邦的诗意、德彪西的光影。你能在旋律的起伏中感受情绪的精准表达。外面的世界很嘈杂，一段布鲁克纳能让你重获内心的秩序。', traits: ['专注', '深沉', '有教养'], vibes: '交响 · 黑胶 · 琴键 · 大调 · 安魂曲' },
  '摇滚': { subtitle: '不妥协的生命力', summary: '你心里有一团火。摇滚不是叛逆，而是对真实的不妥协——失真吉他的轰鸣像你体内积压的力量被释放。你不喜欢被定义，也不擅长讨好。听摇滚的人往往有一副不太会哭的硬壳，但一首慢板 solo 就能让你破防。', traits: ['热血', '真实', '不屈服'], vibes: '失真 · 鼓点 · 皮衣 · 音乐节 · 嘶吼' },
  '嘻哈': { subtitle: '节奏里的态度宣言', summary: '你有强烈的自我表达欲——不爽就说，好就夸，不想拐弯抹角。HIP-HOP 不只是音乐，是你体内的一种态度：自信、直接、不装。你喜欢文字游戏和节奏变化，一首歌的 flow 让你听一百遍都不腻。你真的在听每一个字。', traits: ['自信', '直接', '有态度'], vibes: 'beat · flow · 街头 · freestyle · real talk' },
  '电子': { subtitle: '声波里的建筑师', summary: '你对声音的颗粒度有超越常人的感受力——一个 pad 铺底、一个 arp 上行、一个 drop 释放，能量在你体内精准流转。你喜欢新鲜感，喜欢科技和艺术的交界。一首电子乐像一座你反复走进去的建筑，每一次听都发现不同的层次。', traits: ['前卫', '好奇', '有层次'], vibes: '合成器 · 灯光 · 舞池 · loop · 凌晨' },
  '爵士': { subtitle: '自由而优雅的即兴者', summary: '你喜欢意外。爵士的魅力在于——你永远不知道下一个和弦会去哪，但回头一想又觉得理所当然。你不喜欢被安排，享受在框架里即兴发挥的优雅。爵士听众往往有一种从容：不必急着表态，等对的时机再出手。', traits: ['从容', '有深度', '懂得分寸'], vibes: '萨克斯 · 即兴 · 威士忌 · 烟雾 · 午夜' }
};

const FALLBACK_PROFILE = { subtitle: '独一无二的听众', summary: '你的音乐品味跨越一切标签。任何一种风格都只是你的一部分。', traits: ['多元', '自由', '不设限'], vibes: '你的播放列表，独一无二' };

const MAX_SCORE = 40;

export function resultMapping(scores) {
  const sorted = Object.entries(scores).sort((a, b) => b[1] - a[1]);
  const [top, ...rest] = sorted;
  const topKey = top ? top[0] : '未知';
  return {
    primary: { key: topKey, name: topKey, score: top[1],
      matchPercent: Math.min(99, Math.round((top[1] / MAX_SCORE) * 100)),
      ...(musicProfiles[topKey] || FALLBACK_PROFILE) },
    runners: rest.slice(0, 3).map(([key, score]) => ({ key, name: key, score,
      matchPercent: Math.min(99, Math.round((score / MAX_SCORE) * 100)),
      ...(musicProfiles[key] || { subtitle: '', traits: [], vibes: '' }) })),
    timestamp: new Date().toISOString()
  };
}
