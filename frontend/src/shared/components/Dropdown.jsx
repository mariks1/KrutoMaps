import { useRef, useState } from "react";
import useClickOutside from "@shared/hooks/useClickOutside";
import styles from "@assets/css/style.module.css";

export default function Dropdown({ label, value, children }) {
  const [open, setOpen] = useState(false);
  const boxRef = useRef(null);
  useClickOutside(boxRef, () => setOpen(false));

  return (
    <div className={styles.dropdownContainer} ref={boxRef}>
      {label && <div className={styles.label}>{label}</div>}

      <button
        type="button"
        className={styles.dropbtn}
        onClick={() => setOpen((o) => !o)}
      >
        {value}
        <span className={styles.arrow}>▾</span>
      </button>

      {open && <div className={styles.dropdownContent}>{children}</div>}
    </div>
  );
}
