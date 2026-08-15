import styles from "./ErrorBanner.module.css";

export default function ErrorBanner({ message, onRetry }) {
  return (
    <div className={styles.banner}>
      <span className={styles.icon} aria-hidden>!</span>
      <span className={styles.text}>{message || "操作失败，请重试"}</span>
      {onRetry && (
        <button type="button" className={styles.retry} onClick={onRetry}>
          重试
        </button>
      )}
    </div>
  );
}