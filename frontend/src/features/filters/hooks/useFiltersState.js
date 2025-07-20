import { useQuery } from "@tanstack/react-query";
import { useState } from "react";
import { getRubrics, getPriceRange, getAreaRange } from "@services/index";

export default function useFiltersState() {
  const { data: rubrics = [] } = useQuery({
    queryKey: ["rubrics"],
    queryFn: getRubrics,
    select: (data) => data.sort((a, b) => a.localeCompare(b, "ru")),
  });

  const { data: priceRange } = useQuery({
    queryKey: ["price-range"],
    queryFn: getPriceRange,
  });

  const { data: areaRange } = useQuery({
    queryKey: ["area-range"],
    queryFn: getAreaRange,
  });

  const [state, setState] = useState({
    wantToSee: [],
    dontWantToSee: [],
    priceFrom: "",
    priceTo: "",
    areaFrom: "",
    areaTo: "",
    floorOption: "Любой",
    placeOptions: ["Любой"],
  });

  return {
    rubrics,
    minPrice: priceRange?.minPrice ?? 1000,
    maxPrice: priceRange?.maxPrice ?? 1000000,
    minArea: areaRange?.minArea ?? 100,
    maxArea: areaRange?.maxArea ?? 1000,
    state,
    setState,
  };
}