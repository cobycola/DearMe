import styles from "./Screen.module.css";

export default function Screen({ children, footer, footerFixed = true }) {
  return (
    <div className={styles.stage}>
      <main className={styles.body}>{children}</main>
      {footer && <div className={footerFixed ? styles.footerFixed : styles.footer}>{footer}</div>}
    </div>
  );
}