import { useMutation } from "@tanstack/react-query";
import { postSelection } from "@services/selection";
import useFiltersState from "@features/filters/hooks/useFiltersState";
import AutocompleteInput from "./AutocompleteInput";
import RangeInputs from "./RangeInputs";
import Dropdown from "@shared/components/Dropdown";
import styles from "@assets/css/style.module.css";

const floorOptions = ["Любой", "Только 1-й", "До 5-го", "Не 1-й", "Не последний"];

const defaultPlaceOptions = [
  { value: "Любой", label: "Любой" },
  { value: "Офисные", label: "Офисные" },
  { value: "Производственные", label: "Производственные" },
  { value: "Торговые", label: "Торговые" },
];

export default function FiltersForm({ onSelection }) {
  const {
    rubrics,
    minPrice,
    maxPrice,
    minArea,
    maxArea,
    state,
    setState,
  } = useFiltersState();

  const mutation = useMutation({ mutationFn: postSelection });

  const handleSubmit = async () => {
    const payload = {
      ...state,
      priceFrom: state.priceFrom || minPrice,
      priceTo: state.priceTo || maxPrice,
      areaFrom: state.areaFrom || minArea,
      areaTo: state.areaTo || maxArea,
    };
    try {
      const result = await mutation.mutateAsync(payload);
      onSelection(result);
    } catch (error) {
      console.error("Selection request error:", error);
    }
  };

  /* helpers */
  const addTag = (field) => (v, reset) => {
    if (!state[field].includes(v)) setState({ ...state, [field]: [...state[field], v] });
    reset("");
  };
  const removeTag = (field) => (v) =>
    setState({ ...state, [field]: state[field].filter((t) => t !== v) });

  const togglePlaceOption = (value) => {
    const cur = state.placeOptions;
    let next;
    if (value === "Любой") next = ["Любой"];
    else if (cur.includes(value)) next = cur.filter((o) => o !== value);
    else next = cur[0] === "Любой" ? [value] : [...cur, value];
    if (!next.length) next = ["Любой"];
    setState({ ...state, placeOptions: next });
  };

  return (
    <form className={styles.mapSettings} onSubmit={(e) => e.preventDefault()}>
      <h2 className={styles.heading2}>Настройки карты</h2>

      <AutocompleteInput
        label="То, что вы хотите видеть рядом"
        suggestions={rubrics}
        tags={state.wantToSee}
        onAdd={addTag("wantToSee")}
        onRemove={removeTag("wantToSee")}
      />

      <AutocompleteInput
        label={<>То, что вы хотите <span>не видеть</span> рядом</>}
        suggestions={rubrics}
        tags={state.dontWantToSee}
        onAdd={addTag("dontWantToSee")}
        onRemove={removeTag("dontWantToSee")}
      />

      <div className={styles.paramsContainer}>
        <h3 className={styles.heading3}>Дополнительные параметры</h3>

        <RangeInputs
          label="Цена:"
          from={state.priceFrom}
          to={state.priceTo}
          min={minPrice}
          max={maxPrice}
          onChangeFrom={(v) => setState({ ...state, priceFrom: v })}
          onChangeTo={(v) => setState({ ...state, priceTo: v })}
          unit="₽"
        />

        <Dropdown label="Этаж:" value={state.floorOption}>
          {floorOptions.map((opt) => (
            <label key={opt}>
              <input
                type="radio"
                name="floorOption"
                value={opt}
                checked={state.floorOption === opt}
                onChange={() => setState({ ...state, floorOption: opt })}
              />{" "}
              {opt}
            </label>
          ))}
        </Dropdown>

        <Dropdown label="Тип помещения:" value={state.placeOptions.join(", ")}>
          {defaultPlaceOptions.map((opt) => (
            <label key={opt.value}>
              <input
                type="checkbox"
                checked={state.placeOptions.includes(opt.value)}
                onChange={() => togglePlaceOption(opt.value)}
              />{" "}
              {opt.label}
            </label>
          ))}
        </Dropdown>

        <RangeInputs
          label="Площадь:"
          from={state.areaFrom}
          to={state.areaTo}
          min={minArea}
          max={maxArea}
          onChangeFrom={(v) => setState({ ...state, areaFrom: v })}
          onChangeTo={(v) => setState({ ...state, areaTo: v })}
          unit="м²"
        />
      </div>

      <button type="button" className={styles.btn} onClick={handleSubmit}>
        Подобрать место
      </button>
    </form>
  );
}
