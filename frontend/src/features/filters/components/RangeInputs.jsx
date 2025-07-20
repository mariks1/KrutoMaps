import formatNumberWithSpaces from "@shared/utils/formatNumberWithSpaces";
import styles from "@assets/css/style.module.css";

export default function RangeInputs({
  label,
  from,
  to,
  min,
  max,
  onChangeFrom,
  onChangeTo,
  unit,
}) {
  return (
    <div className={styles.inputContainer}>
      <div className={styles.label}>{label}</div>

      <div className={styles.inputsRow}>
        <div className={styles.inputWrapper}>
          <input
            type="text"
            className={styles.input}
            placeholder={`от ${formatNumberWithSpaces(min)} ${unit}`}
            value={from}
            onChange={(e) =>
              onChangeFrom(e.target.value.replace(/[^\d]/g, ""))
            }
          />
          <span
            className={styles.clearBtn}
            onClick={() => onChangeFrom("")}
          >
            ×
          </span>
        </div>

        <div className={styles.inputWrapper}>
          <input
            type="text"
            className={styles.input}
            placeholder={`до ${formatNumberWithSpaces(max)} ${unit}`}
            value={to}
            onChange={(e) =>
              onChangeTo(e.target.value.replace(/[^\d]/g, ""))
            }
          />
          <span
            className={styles.clearBtn}
            onClick={() => onChangeTo("")}
          >
            ×
          </span>
        </div>
      </div>
    </div>
  );
}
