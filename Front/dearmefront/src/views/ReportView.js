import { useState } from "react";
import ReactMarkdown from "react-markdown";
import Screen from "../components/Screen";
import ErrorBanner from "../components/ErrorBanner";
import styles from "./ReportView.module.css";

export default function ReportView({ reportMarkdown, loading, error, onRetry, onReset }) {
  const [copied, setCopied] = useState(false);

  async function copy() {
    if (!reportMarkdown) return;
    try {
      await navigator.clipboard.writeText(reportMarkdown);
      setCopied(true);
      setTimeout(() => setCopied(false), 2000);
    } catch {
      // ignore
    }
  }

  return (
    <Screen
      footer={
        <div className={styles.actions}>
          {error && <ErrorBanner message={error} onRetry={onRetry} />}
          <button type="button" className={styles.secondary} onClick={copy} disabled={!reportMarkdown}>
            {copied ? "已复制" : "复制结果"}
          </button>
          <button type="button" className={styles.secondary} onClick={onReset}>
            重新测评
          </button>
        </div>
      }
    >
      <div className={styles.heading}>测评结果</div>

      {loading && !reportMarkdown && (
        <div className={styles.skeleton}>
          <div className={styles.skLine} style={{ width: "60%" }} />
          <div className={styles.skPara} />
          <div className={styles.skPara} />
          <div className={styles.skLine} style={{ width: "45%" }} />
          <div className={styles.skPara} />
        </div>
      )}

      {reportMarkdown && (
        <article className={styles.report}>
          <ReactMarkdown
            components={{
              h1: ({ node, ...p }) => <h1 className={styles.h1}>{p.children}</h1>,
              h2: ({ node, ...p }) => <h2 className={styles.h2}>{p.children}</h2>,
              h3: ({ node, ...p }) => <h3 className={styles.h3}>{p.children}</h3>,
              p: ({ node, ...p }) => <p className={styles.p}>{p.children}</p>,
              ul: ({ node, ...p }) => <ul className={styles.ul}>{p.children}</ul>,
              ol: ({ node, ...p }) => <ol className={styles.ol}>{p.children}</ol>,
              li: ({ node, ...p }) => <li className={styles.li}>{p.children}</li>,
              strong: ({ node, ...p }) => <strong className={styles.strong}>{p.children}</strong>,
              blockquote: ({ node, ...p }) => <blockquote className={styles.quote}>{p.children}</blockquote>,
            }}
          >
            {reportMarkdown}
          </ReactMarkdown>
        </article>
      )}
    </Screen>
  );
}