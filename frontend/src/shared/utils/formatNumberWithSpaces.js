export default function formatNumberWithSpaces(num) {
  return num.toString().replace(/\B(?=(\d{3})+(?!\d))/g, " ");
}