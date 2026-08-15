import styles from "./OptionButton.module.css";

export default function OptionButton({ index, option, selected = false, disabled = false, onClick }) {
  return (
    <button
      type="button"
      className={`${styles.option} ${selected ? styles.selected : ""}`}
      disabled={disabled}
      onClick={() => onClick(index)}
      aria-pressed={selected}
    >
      <span className={styles.marker}>{String.fromCharCode(65 + index)}</span>
      <span className={styles.text}>{option}</span>
    </button>
  );
}