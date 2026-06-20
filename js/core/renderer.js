import { createApp, reactive, computed } from 'vue';

const ProgressGrove = {
  props: { current: Number, total: Number, answered: Number },
  template: `
    <div class="progress-grove">
      <div class="progress-grove__track">
        <div class="progress-grove__bar"
          :style="{ width: (answered / total * 100) + '%' }"></div>
      </div>
      <div class="progress-grove__branches">
        <span v-for="i in total" :key="i" class="progress-grove__leaf"
          :class="{ 'is-grown': i <= answered, 'is-current': i === current }"></span>
      </div>
      <p class="progress-grove__hint">{{ answered === 0 ? '刚刚种下一颗种子' :
        answered < total ? '小芽正在生长...' : '已亭亭如盖' }}</p>
    </div>`
};

const QuestionCard = {
  props: { question: Object, selectedId: String },
  emits: ['select'],
  template: `
    <div class="question-card">
      <p class="question-card__text">{{ question.text }}</p>
      <div class="question-card__options">
        <button v-for="opt in question.options" :key="opt.id"
          class="option-leaf"
          :class="{ 'is-picked': selectedId === opt.id }"
          @click="$emit('select', opt.id)">
          <span class="option-leaf__text">{{ opt.text }}</span>
        </button>
      </div>
    </div>`
};

const TestCover = {
  props: { meta: Object },
  emits: ['start'],
  template: `
    <div class="test-cover">
      <div class="test-cover__inner">
        <h1 class="test-cover__title">{{ meta.title }}</h1>
        <p class="test-cover__desc">{{ meta.description }}</p>
        <button class="btn-start" @click="$emit('start')">开始探索</button>
      </div>
    </div>`
};

const TestNav = {
  props: { isFirst: Boolean, isLast: Boolean, canGoNext: Boolean },
  emits: ['prev', 'next'],
  template: `
    <div class="test-nav">
      <button class="btn-nav btn-nav--prev" :disabled="isFirst"
        @click="$emit('prev')">← 上一题</button>
      <button class="btn-nav btn-nav--next" :disabled="!canGoNext"
        @click="$emit('next')">{{ isLast ? '查看结果 →' : '下一题 →' }}</button>
    </div>`
};

const ErrorScreen = {
  props: { message: String, detail: String },
  emits: ['reload'],
  template: `
    <div class="error-screen"><div class="error-screen__inner">
      <p class="error-screen__icon">😌</p>
      <p class="error-screen__text">{{ message }}</p>
      <p class="error-screen__detail" v-if="detail">{{ detail }}</p>
      <button class="btn-start" @click="$emit('reload');location.reload()">重新加载</button>
    </div></div>`
};

const ResultPage = {
  props: { result: Object },
  emits: ['retake'],
  template: `
    <div class="result-page">
      <div id="result-screenshot-anchor" class="result-card">
        <p class="result-card__brow">你的灵魂之城</p>
        <h2 class="result-card__city">{{ result.primary.name }}</h2>
        <p class="result-card__subtitle">{{ result.primary.subtitle }}</p>
        <div class="result-card__match">
          <span class="result-card__percent">{{ result.primary.matchPercent }}%</span>
          <span class="result-card__label">匹配度</span>
        </div>
        <p class="result-card__summary">{{ result.primary.summary }}</p>
        <div class="result-card__traits">
          <span v-for="t in result.primary.traits" :key="t" class="trait-chip">{{ t }}</span>
        </div>
        <p class="result-card__vibes">{{ result.primary.vibes }}</p>
        <div class="result-card__runners" v-if="result.runners.length">
          <div class="runner-item" v-for="r in result.runners" :key="r.key">
            <div class="runner-item__bar">
              <div class="runner-item__fill" :style="{ width: r.matchPercent + '%' }"></div>
            </div>
            <span class="runner-item__name">{{ r.name }}</span>
            <span class="runner-item__pct">{{ r.matchPercent }}%</span>
          </div>
        </div>
      </div>
      <div class="result-actions">
        <div class="share-panel">
          <button class="btn-action" @click="$emit('retake')">重新探索</button>
          <span class="share-panel__divider">— 分享 —</span>
          <button class="btn-action btn-action--ghost btn-action--placeholder">保存截图</button>
          <button class="btn-action btn-action--ghost btn-action--placeholder">复制链接</button>
        </div>
      </div>
    </div>`
};

export class Renderer {
  constructor() {
    this.app = null;
    this.state = null;
  }

  mount(selector, stateManager, meta) {
    const appState = reactive({
      phase: 'cover',
      currentIndex: 0,
      question: stateManager.getCurrentQuestion(),
      progress: stateManager.progress,
      selectedAnswer: stateManager.getAnswer(stateManager.getCurrentQuestion().id),
      isFirst: true,
      isLast: stateManager.isLast,
      result: null,
      error: null,
      meta
    });

    const canGoNext = computed(() => !!appState.selectedAnswer || appState.phase === 'result');

    this.app = createApp({
      components: { TestCover, QuestionCard, ProgressGrove, TestNav, ResultPage, ErrorScreen },
      setup() {
        const syncState = () => {
          appState.currentIndex = stateManager.currentIndex;
          appState.question = stateManager.getCurrentQuestion();
          appState.selectedAnswer = stateManager.getAnswer(appState.question.id);
          appState.isFirst = stateManager.isFirst;
          appState.isLast = stateManager.isLast;
          appState.progress = { ...stateManager.progress };
        };

        const onStart = () => { appState.phase = 'question'; syncState(); };

        const onSelect = (oid) => {
          stateManager.saveAnswer(stateManager.getCurrentQuestion().id, oid);
          appState.selectedAnswer = oid;
          appState.progress = { ...stateManager.progress };
        };

        const onNext = () => {
          if (stateManager.isLast && canGoNext.value) {
            const snapshot = stateManager.finalize();
            const scores = stateManager._scoring.calculate(snapshot.answers, stateManager._questions);
            appState.result = stateManager._resultMapping(scores);
            appState.phase = 'result';
          } else if (!stateManager.isLast && canGoNext.value) {
            stateManager.goNext();
            syncState();
          }
        };

        const onPrev = () => { stateManager.goPrev(); syncState(); };
        const onRetake = () => { window.location.reload(); };

        return { appState, canGoNext, onStart, onSelect, onNext, onPrev, onRetake };
      },
      template: `
        <div class="app-shell">
          <TestCover v-if="appState.phase === 'cover'" :meta="appState.meta" @start="onStart" />
          <template v-if="appState.phase === 'question'">
            <ProgressGrove :current="appState.progress.current"
              :total="appState.progress.total" :answered="appState.progress.answered" />
            <transition name="fade-flow" mode="out-in">
              <QuestionCard :key="appState.question.id"
                :question="appState.question" :selected-id="appState.selectedAnswer"
                @select="onSelect" />
            </transition>
            <TestNav :is-first="appState.isFirst" :is-last="appState.isLast"
              :can-go-next="canGoNext" @prev="onPrev" @next="onNext" />
          </template>
          <ResultPage v-if="appState.phase === 'result'"
            :result="appState.result" @retake="onRetake" />
          <ErrorScreen v-if="appState.phase === 'error'"
            :message="appState.error?.message || '出现了意外情况'"
            :detail="appState.error?.detail || ''"
            @reload="() => location.reload()" />
        </div>`
    });

    this.app.mount(selector);
    this.state = appState;
  }

  showError(err) {
    if (this.state) {
      this.state.phase = 'error';
      this.state.error = { message: '出现了意外情况', detail: err?.message || String(err) };
    }
  }
}
