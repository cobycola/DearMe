import Screen from "../components/Screen";
import PrimaryButton from "../components/PrimaryButton";
import ErrorBanner from "../components/ErrorBanner";
import { findTopic } from "../config/topics";
import styles from "./TopicView.module.css";

export default function TopicView({ topicId, loading, error, onStart, onRetry }) {
  const topic = findTopic(topicId) || findTopic("anime-character");
  return (
    <Screen
      footer={
        <>
          {error && <div className={styles.errorWrap}><ErrorBanner message={error} onRetry={onRetry} /></div>}
          <PrimaryButton onClick={onStart} loading={loading} loadingLabel="准备中">
            开始作答
          </PrimaryButton>
        </>
      }
    >
      <div className={styles.brand}>DearMe</div>
      <div className={styles.hero}>
        <h1 className={styles.title}>{topic.displayName}</h1>
        <p className={styles.desc}>{topic.description}</p>
        <p className={styles.mood}>{topic.mood}</p>
      </div>
    </Screen>
  );
}