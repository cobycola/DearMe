import styles from "./PrimaryButton.module.css";

export default function PrimaryButton({ children, onClick, disabled, loading, loadingLabel }) {
  return (
    <button
      type="button"
      className={styles.btn}
      onClick={onClick}
      disabled={disabled || loading}
    >
      {loading ? (
        <span className={styles.loading}>
          <span className={styles.spinner} aria-hidden />
          {loadingLabel && <span className={styles.loadingLabel}>{loadingLabel}</span>}
        </span>
      ) : (
        children
      )}
    </button>
  );
}