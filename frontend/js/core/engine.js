import { StateManager } from './state-manager.js';
import { Renderer } from './renderer.js';

export function createTestApp(selector, testDef) {
  const errors = [];
  if (!testDef.meta?.id)          errors.push('testDef.meta.id 缺失');
  if (!Array.isArray(testDef.questions) || testDef.questions.length === 0) errors.push('testDef.questions 非空数组');
  if (typeof testDef.scoring?.calculate !== 'function') errors.push('testDef.scoring 须实现 calculate()');
  if (typeof testDef.resultMapping !== 'function')      errors.push('testDef.resultMapping 须为函数');

  if (errors.length > 0) {
    const el = document.querySelector(selector);
    if (el) el.innerHTML = `<div class="error-screen"><div class="error-screen__inner">
      <p class="error-screen__icon">😌</p>
      <p class="error-screen__text">测试配置有误</p>
      <p class="error-screen__detail">${errors.join('<br>')}</p>
      <button class="btn-start" onclick="location.reload()">重新加载</button>
    </div></div>`;
    console.error('testDef validation:', errors);
    return { unmount() {} };
  }

  try {
    const sm = new StateManager(testDef.questions);
    sm._scoring = testDef.scoring;
    sm._questions = testDef.questions;
    sm._resultMapping = testDef.resultMapping;

    const renderer = new Renderer();
    renderer.mount(selector, sm, testDef.meta);
    return { unmount() { renderer.app?.unmount(); } };
  } catch (err) {
    console.error('createTestApp:', err);
    const el = document.querySelector(selector);
    if (el) el.innerHTML = `<div class="error-screen"><div class="error-screen__inner">
      <p class="error-screen__icon">😌</p><p class="error-screen__text">加载失败</p>
      <p class="error-screen__detail">${err.message}</p>
      <button class="btn-start" onclick="location.reload()">重新加载</button>
    </div></div>`;
    return { unmount() {} };
  }
}
