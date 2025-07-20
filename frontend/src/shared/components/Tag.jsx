// src/shared/components/Tag.jsx
import React from "react";
import styles from "@assets/css/style.module.css";

export default function Tag({ text, onRemove }) {
  return (
    <div className={styles.tag}>
      <span className={styles.tagText}>{text}</span>
      <button
        type="button"
        className={styles.removeBtn}
        onClick={onRemove}
      >
        ×
      </button>
    </div>
  );
}
