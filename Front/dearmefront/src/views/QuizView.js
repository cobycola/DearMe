import Screen from "../components/Screen";
import OptionButton from "../components/OptionButton";
import ProgressDots from "../components/ProgressDots";
import Spinner from "../components/Spinner";
import ErrorBanner from "../components/ErrorBanner";
import styles from "./QuizView.module.css";

const TOTAL = 10;

function probLabel(p) {
  if (p == null) return null;
  const pct = Math.round(p * 100);
  if (pct >= 70) return `当前倾向：高 · ${pct}%`;
  if (pct >= 40) return `当前倾向：中 · ${pct}%`;
  return `当前倾向：${pct}%`;
}

export default function QuizView({ question, answeredCount, topCandidateProbability, loading, error, onAnswer, onBack, onRetry }) {
  const submitting = loading;
  const currentIndex = answeredCount + 1;

  return (
    <Screen
      footer={
        error ? (
          <ErrorBanner message={error} onRetry={onRetry} />
        ) : null
      }
      footerFixed={false}
    >
      <div className={styles.topbar}>
        <button type="button" className={styles.back} onClick={onBack}>放弃</button>
        <div className={styles.right}>
          <ProgressDots current={Math.min(currentIndex, TOTAL)} total={TOTAL} />
        </div>
      </div>

      {topCandidateProbability != null && (
        <div className={styles.prob}>{probLabel(topCandidateProbability)}</div>
      )}

      <div className={styles.promptWrap}>
        <div className={styles.qLabel}>Q{currentIndex}</div>
        {question ? (
          <h1 className={styles.prompt}>{question.prompt}</h1>
        ) : (
          <div className={styles.loadingWrap}><Spinner label="加载题目…" /></div>
        )}
      </div>

      {question && (
        <div className={styles.options}>
          {question.options.map((opt, i) => (
            <OptionButton
              key={i}
              index={i}
              option={opt}
              disabled={submitting}
              onClick={(idx) => onAnswer(question.id, idx)}
            />
          ))}
        </div>
      )}

      {submitting && !error && (
        <div className={styles.submitting}><Spinner label="记录中…" /></div>
      )}
    </Screen>
  );
}