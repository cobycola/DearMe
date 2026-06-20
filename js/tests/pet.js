import { WeightedScoring } from '../core/scorer.js';
import { calculateMatchPercent } from '../core/utils.js';

export const meta = {
  id: 'pet',
  title: '你最适合养什么宠物',
  description: '8 道题，发现最适合陪你生活的小生命。'
};

export const questions = [
  { id: 'q1', text: '你理想中的周末是？',
    options: [
      { id: 'a', text: '宅家追剧，窝在沙发里不动', weights: { 猫: 5, 仓鼠: 3 } },
      { id: 'b', text: '出门跑步爬山，一身大汗', weights: { 狗: 5, 猫: 3 } },
      { id: 'c', text: '约朋友喝咖啡聊天', weights: { 狗: 5, 鹦鹉: 3 } },
      { id: 'd', text: '安静做手工、看书、养花', weights: { 鱼: 5, 猫: 3 } }
    ]},
  { id: 'q2', text: '你对「麻烦」的容忍度？',
    options: [
      { id: 'a', text: '每天遛弯、洗澡、陪玩都可以', weights: { 狗: 5, 鹦鹉: 3 } },
      { id: 'b', text: '可以互动，但别太黏人', weights: { 猫: 5, 兔子: 3 } },
      { id: 'c', text: '喂食换水就够了，互不打扰', weights: { 鱼: 5, 仓鼠: 3 } },
      { id: 'd', text: '最好全自动，看看就行', weights: { 鱼: 5, 猫: 3 } }
    ]},
  { id: 'q3', text: '你对家里环境卫生有多在意？',
    options: [
      { id: 'a', text: '有点洁癖，不能忍受毛发', weights: { 鱼: 5, 龟: 3 } },
      { id: 'b', text: '正常打扫，不太纠结', weights: { 猫: 5, 兔子: 3 } },
      { id: 'c', text: '为了它我愿意多打扫几次', weights: { 狗: 5, 鹦鹉: 3 } },
      { id: 'd', text: '无所谓，一起乱着呗', weights: { 狗: 5, 仓鼠: 3 } }
    ]},
  { id: 'q4', text: '你希望宠物和你的互动方式？',
    options: [
      { id: 'a', text: '热烈回应！它冲过来迎接我才满足', weights: { 狗: 5, 鹦鹉: 3 } },
      { id: 'b', text: '偶尔蹭蹭，保持一点距离感', weights: { 猫: 5, 兔子: 3 } },
      { id: 'c', text: '安静陪伴，在同一空间就好', weights: { 龟: 5, 鱼: 3 } },
      { id: 'd', text: '远远看着它做自己的事就治愈', weights: { 鱼: 5, 仓鼠: 3 } }
    ]},
  { id: 'q5', text: '你的生活作息？',
    options: [
      { id: 'a', text: '早睡早起，生活规律', weights: { 狗: 5, 龟: 3 } },
      { id: 'b', text: '夜猫子，晚上才是我的时间', weights: { 猫: 5, 仓鼠: 3 } },
      { id: 'c', text: '时间自由，随时有空', weights: { 鹦鹉: 5, 狗: 3 } },
      { id: 'd', text: '经常出差或加班', weights: { 鱼: 5, 龟: 3 } }
    ]},
  { id: 'q6', text: '你想从宠物那里得到什么？',
    options: [
      { id: 'a', text: '无条件的陪伴和爱', weights: { 狗: 5, 猫: 3 } },
      { id: 'b', text: '有趣的对话和互动', weights: { 鹦鹉: 5, 狗: 3 } },
      { id: 'c', text: '一个静静的倾听者', weights: { 猫: 5, 鱼: 3 } },
      { id: 'd', text: '减压放松，看看就开心', weights: { 鱼: 5, 兔子: 3 } }
    ]},
  { id: 'q7', text: '你住的地方怎么样？',
    options: [
      { id: 'a', text: '有院子或大阳台', weights: { 狗: 5, 兔子: 3 } },
      { id: 'b', text: '公寓，空间不算大但够用', weights: { 猫: 5, 仓鼠: 3 } },
      { id: 'c', text: '小户型，需要不太占地方的', weights: { 鱼: 5, 龟: 3 } },
      { id: 'd', text: '合租或宿舍，空间很有限', weights: { 仓鼠: 5, 龟: 3 } }
    ]},
  { id: 'q8', text: '如果宠物闯祸了，你会？',
    options: [
      { id: 'a', text: '叹了口气，然后好好教育它', weights: { 狗: 5, 鹦鹉: 3 } },
      { id: 'b', text: '假装生气，然后偷偷原谅了', weights: { 猫: 5, 兔子: 3 } },
      { id: 'c', text: '它根本闯不了什么祸', weights: { 鱼: 5, 龟: 3 } },
      { id: 'd', text: '没关系，早就习惯了', weights: { 仓鼠: 5, 狗: 3 } }
    ]}
];

export const scoring = new WeightedScoring();

const petProfiles = {
  '狗': { subtitle: '陪你奔跑的忠诚伙伴', summary: '你热情而有耐心，愿意为所爱付出时间和精力。遛狗不只是责任，更是你一天中最放松的仪式。你适合一只永远对你摇尾巴的家伙——你们的快乐是相互的。', traits: ['热情', '有耐心', '忠诚'], vibes: '晨跑 · 飞盘 · 摇尾巴 · 公园 · 泥爪子' },
  '猫': { subtitle: '若即若离的灵魂室友', summary: '你懂得尊重边界——无论是对别人还是对自己。你不喜欢太黏的关系，但骨子里又渴望柔软的陪伴。一只猫刚好能满足你：它在的时候温暖，不在的时候自由。', traits: ['独立', '细腻', '有边界感'], vibes: '晒太阳 · 呼噜声 · 纸箱 · 窗台 · 猫薄荷' },
  '鱼': { subtitle: '安静的水下观察者', summary: '你喜欢秩序和干净，也喜欢一个人安静待着。养鱼的人往往有一种内敛的禅意——不需要互动，只需要存在。水族箱就是你的微型宇宙。', traits: ['安静', '有审美', '自律'], vibes: '水草 · 气泡 · 灯光 · 清澈 · 慢节奏' },
  '鹦鹉': { subtitle: '话痨又聪明的小机灵', summary: '你喜欢新鲜感，也喜欢被需要的感觉。一只会学你说话的鹦鹉能满足你的分享欲——它是你生活里的小喇叭，也是你最不需要设防的聊天对象。', traits: ['好奇', '善谈', '有童心'], vibes: '学舌 · 羽毛 · 阳光 · 笼子 · 零食' },
  '兔子': { subtitle: '柔软治愈的安静室友', summary: '你喜欢细水长流的陪伴，不追求轰轰烈烈的互动。一只兔子窝在脚边，耳朵轻轻颤动——这就是你想要的岁月静好。', traits: ['温柔', '治愈', '不争不抢'], vibes: '胡萝卜 · 绒毛 · 草垛 · 安静 · 午后' },
  '仓鼠': { subtitle: '小小只的独立世界', summary: '你生活空间不大，但内心丰富。一只仓鼠就能满足你看小生命忙碌的乐趣——它有自己的节奏，你也一样。', traits: ['自洽', '容易满足', '有趣'], vibes: '跑轮 · 颊囊 · 木屑 · 小窝 · 夜行' },
  '龟': { subtitle: '长寿的静默陪伴者', summary: '你是一个不急着赶路的人。龟的慢让你觉得安心——它不催你，你也别催它。几十年后它还在，就是最长情的告白。', traits: ['沉稳', '耐心', '长情'], vibes: '晒背 · 慢吞吞 · 长寿 · 龟壳 · 阳台' }
};

const FALLBACK_PROFILE = { subtitle: '独一无二的你', summary: '你适合一只属于你自己节奏的小生命。', traits: ['特别', '自由'], vibes: '属于你的缘分' };

const MAX_SCORE = 40; // 8 题 × 5

export function resultMapping(scores) {
  const sorted = Object.entries(scores).sort((a, b) => b[1] - a[1]);
  const [top, ...rest] = sorted;
  const topKey = top ? top[0] : '未知';
  return {
    primary: { key: topKey, name: topKey, score: top[1],
      matchPercent: calculateMatchPercent(top[1], MAX_SCORE),
      ...(petProfiles[topKey] || FALLBACK_PROFILE) },
    runners: rest.slice(0, 3).map(([key, score]) => ({ key, name: key, score,
      matchPercent: calculateMatchPercent(score, MAX_SCORE),
      ...(petProfiles[key] || { subtitle: '', traits: [], vibes: '' }) })),
    timestamp: new Date().toISOString()
  };
}
