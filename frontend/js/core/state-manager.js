export class StateManager {
  #questions;
  #answers;
  #currentIndex;
  #finalized;
  #subscribers;

  constructor(questions) {
    if (!Array.isArray(questions) || questions.length === 0) {
      throw new Error('questions must be a non-empty array');
    }
    this.#questions = questions;
    this.#answers = new Map();
    this.#currentIndex = 0;
    this.#finalized = false;
    this.#subscribers = new Set();
  }

  saveAnswer(qid, oid) {
    if (this.#finalized) return;
    const q = this.#questions.find(q => q.id === qid);
    if (!q) throw new Error(`Unknown question: ${qid}`);
    if (!q.options.find(o => o.id === oid)) {
      throw new Error(`Unknown option: ${oid} for ${qid}`);
    }
    this.#answers.set(qid, oid);
    this.#notify();
  }

  goNext() {
    if (!this.isLast) { this.#currentIndex++; this.#notify(); }
  }

  goPrev() {
    if (!this.isFirst) { this.#currentIndex--; this.#notify(); }
  }

  goTo(index) {
    if (index >= 0 && index < this.#questions.length) {
      this.#currentIndex = index;
      this.#notify();
    }
  }

  finalize() {
    this.#finalized = true;
    return {
      answers: new Map(this.#answers),
      metadata: {
        total: this.#questions.length,
        answered: this.#answers.size,
        skipped: this.#questions.length - this.#answers.size
      }
    };
  }

  subscribe(fn) {
    this.#subscribers.add(fn);
    return () => this.#subscribers.delete(fn);
  }

  get currentIndex()  { return this.#currentIndex; }
  get questionCount()  { return this.#questions.length; }
  get isFirst()        { return this.#currentIndex === 0; }
  get isLast()         { return this.#currentIndex === this.#questions.length - 1; }
  get isFinalized()    { return this.#finalized; }
  get progress() {
    return { current: this.#currentIndex + 1, total: this.#questions.length, answered: this.#answers.size };
  }

  getCurrentQuestion() { return this.#questions[this.#currentIndex]; }
  getAnswer(qid)       { return this.#answers.get(qid) || null; }

  #notify() {
    const state = {
      currentIndex: this.#currentIndex,
      question: this.getCurrentQuestion(),
      progress: this.progress,
      selectedAnswer: this.getAnswer(this.getCurrentQuestion().id),
      isFirst: this.isFirst,
      isLast: this.isLast
    };
    this.#subscribers.forEach(fn => fn(state));
  }
}
