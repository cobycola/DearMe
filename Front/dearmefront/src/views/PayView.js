import { useState } from "react";
import Screen from "../components/Screen";
import PrimaryButton from "../components/PrimaryButton";
import ErrorBanner from "../components/ErrorBanner";
import { findTopic } from "../config/topics";
import styles from "./PayView.module.css";

export default function PayView({ topicId, sessionId, loading, error, onPay, onRetry, onBack }) {
  const [showNotice, setShowNotice] = useState(true);
  const topic = findTopic(topicId) || findTopic("anime-character");
  const yuan = topic.priceYuan.toFixed(2);
  const shortId = sessionId ? sessionId.slice(-4) : "----";

  return (
    <Screen
      footer={
        <>
          {error && <div className={styles.errorWrap}><ErrorBanner message={error} onRetry={onRetry} /></div>}
          <PrimaryButton onClick={onPay} loading={loading} loadingLabel="正在确认支付…">
            确认支付 · ¥{yuan}
          </PrimaryButton>
        </>
      }
    >
      <button type="button" className={styles.back} onClick={onBack}>← 返回主题</button>

      <div className={styles.amount}>
        <span className={styles.currency}>¥</span>
        <span className={styles.num}>{yuan}</span>
      </div>
      <div className={styles.channel}>测试 · 模拟支付通道</div>

      <div className={styles.summary}>
        <div className={styles.summaryRow}>
          <span className={styles.summaryLabel}>主题</span>
          <span className={styles.summaryValue}>{topic.displayName}</span>
        </div>
        <div className={styles.divider} />
        <div className={styles.summaryRow}>
          <span className={styles.summaryLabel}>会话</span>
          <span className={styles.summaryValue}>…{shortId}</span>
        </div>
        <div className={styles.divider} />
        <div className={styles.summaryRow}>
          <span className={styles.summaryLabel}>说明</span>
          <span className={styles.summaryValue}>付费解锁一轮定制测评</span>
        </div>
      </div>

      <div className={styles.notice}>
        <button
          type="button"
          className={styles.noticeToggle}
          onClick={() => setShowNotice((v) => !v)}
        >
          购买须知 {showNotice ? "−" : "+"}
        </button>
        {showNotice && (
          <ul className={styles.noticeList}>
            <li>本次为体验版测评，采用模拟支付通道，不会发生真实扣款。</li>
            <li>答案一经生成报告，因测评内容的即时性与定制性，不予退款。</li>
            <li>建议在网络稳定环境下一次性作答；中途刷新可凭当前会话继续。</li>
          </ul>
        )}
      </div>
    </Screen>
  );
}