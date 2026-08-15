import styles from "./ProgressDots.module.css";

export default function ProgressDots({ current, total }) {
  return (
    <div className={styles.wrap} aria-label={`第 ${current} 题，共 ${total} 题`}>
      <span className={styles.current}>{current}</span>
      <span className={styles.slash}>/</span>
      <span className={styles.total}>{total}</span>
    </div>
  );
}