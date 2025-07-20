import { useState, useRef } from "react";
import Tag from "@shared/components/Tag";
import useClickOutside from "@shared/hooks/useClickOutside";
import styles from "@assets/css/style.module.css";

export default function AutocompleteInput({
  label,
  suggestions,
  tags,
  onAdd,
  onRemove,
  placeholder = "Начните вводить...",
}) {
  const [input, setInput] = useState("");
  const [open, setOpen]  = useState(false);

  const ref = useRef(null);
  useClickOutside(ref, () => setOpen(false));

  const filtered = suggestions.filter(
    (s) => s.toLowerCase().includes(input.toLowerCase()) && !tags.includes(s)
  );

  return (
    <div className={styles.paramsContainer} ref={ref}>
      <h3 className={styles.heading3}>{label}</h3>

      <div className={styles.autocomplete}>
        <input
          className={styles.input}
          type="text"
          placeholder={placeholder}
          value={input}
          onChange={(e) => { setInput(e.target.value); setOpen(true); }}
          onFocus={() => setOpen(true)}
          onKeyDown={(e) => {
            if (e.key === "Enter") {
              e.preventDefault();
              if (filtered[0]) { onAdd(filtered[0], setInput); setOpen(false); }
            }
          }}
        />

        {open && (
          <div className={styles.autocompleteList}>
            {filtered.length ? (
              filtered.map((s) => (
                <div
                  key={s}
                  className={styles.autocompleteItem}
                  onClick={() => { onAdd(s, setInput); setOpen(false); }}
                >
                  {s}
                </div>
              ))
            ) : (
              <div className={styles.autocompleteItem}>Нет совпадений</div>
            )}
          </div>
        )}
      </div>

      <div className={styles.tagsСontainer}>
        {tags.map((t) => (
          <Tag key={t} text={t} onRemove={() => onRemove(t)} />
        ))}
      </div>
    </div>
  );
}
