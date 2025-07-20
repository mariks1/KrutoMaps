import Checkbox from "@shared/components/Checkbox";
import styles from "@assets/css/style.module.css";

export default function MapControls({
  showRealty,   onToggleRealty,
  showPreferred,onTogglePreferred,
  showAvoided,  onToggleAvoided,
}) {
  return (
    <div className={styles.paramsContainer}>
      <Checkbox label="Недвижимость"   checked={showRealty}    onChange={onToggleRealty}/>
      <Checkbox label="Предпочтительные" checked={showPreferred} onChange={onTogglePreferred}/>
      <Checkbox label="Непредпочтительные" checked={showAvoided}   onChange={onToggleAvoided}/>
    </div>
  );
}
