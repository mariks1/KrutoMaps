import create from "zustand";

const useStore = create((set) => ({
  filters: {},
  setFilters: (filters) => set({ filters }),
}));

export default useStore;