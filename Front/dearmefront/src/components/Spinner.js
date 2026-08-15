export default function Spinner({ label }) {
  return (
    <span style={{
      display: "inline-flex",
      alignItems: "center",
      justifyContent: "center",
      gap: 8,
    }}>
      <span style={{
        width: 16,
        height: 16,
        borderRadius: "50%",
        border: "2px solid rgba(255,255,255,0.25)",
        borderTopColor: "var(--pink)",
        animation: "spin 700ms linear infinite",
        display: "inline-block",
      }} aria-hidden
      />
      {label && <span style={{ fontSize: "var(--fs-caption)", color: "var(--text-dim)" }}>{label}</span>}
      <style>{`@keyframes spin{to{transform:rotate(360deg)}}`}</style>
    </span>
  );
}