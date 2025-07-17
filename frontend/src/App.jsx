import { useState, useEffect, useRef, useCallback } from "react";
import "./assets/css/reset.css";
import "./assets/css/style.css";
import map from "./assets/images/map.png";
function formatNumberWithSpaces(number) {
	return number.toString().replace(/\B(?=(\d{3})+(?!\d))/g, " ");
}

const defaultPlaceOptions = [
	{ value: "Любой", label: "Любой" },
	{ value: "Офисные", label: "Офисные" },
	{ value: "Производственные", label: "Производственные" },
	{ value: "Торговые", label: "Торговые" },
];

const MAP_BOUNDS = {
	topLeft: { lat: 59.991791, lng: 30.202104 },
	bottomRight: { lat: 59.840498, lng: 30.502827 },
};

function convertCoordsToPixels(lat, lng, mapWidth, mapHeight) {
	const xRatio =
		(lng - MAP_BOUNDS.topLeft.lng) /
		(MAP_BOUNDS.bottomRight.lng - MAP_BOUNDS.topLeft.lng);
	const yRatio =
		(MAP_BOUNDS.topLeft.lat - lat) /
		(MAP_BOUNDS.topLeft.lat - MAP_BOUNDS.bottomRight.lat);
	return {
		x: xRatio * mapWidth,
		y: yRatio * mapHeight,
	};
}

function clusterMarkers(data, mapWidth, mapHeight) {
	const pixelData = data.map((item) => {
		const { x, y } = convertCoordsToPixels(
			item.pointY || item.lat,
			item.pointX || item.lon,
			mapWidth,
			mapHeight
		);
		return { ...item, px: x, py: y };
	});
	const THRESHOLD = 20;
	const graph = Array.from({ length: pixelData.length }, () => []);
	for (let i = 0; i < pixelData.length; i++) {
		for (let j = i + 1; j < pixelData.length; j++) {
			const dx = pixelData[i].px - pixelData[j].px;
			const dy = pixelData[i].py - pixelData[j].py;
			const distance = Math.sqrt(dx * dx + dy * dy);
			if (distance < THRESHOLD) {
				graph[i].push(j);
				graph[j].push(i);
			}
		}
	}
	const visited = new Array(pixelData.length).fill(false);
	const clusters = [];
	function dfs(node, component) {
		if (visited[node]) return;
		visited[node] = true;
		component.push(node);
		for (const neighbor of graph[node]) {
			dfs(neighbor, component);
		}
	}
	for (let i = 0; i < pixelData.length; i++) {
		if (!visited[i]) {
			const component = [];
			dfs(i, component);
			clusters.push(component.map((idx) => pixelData[idx]));
		}
	}
	return clusters;
}

function App() {
	// Состояния для фильтров и данных
	const [rubrics, setRubrics] = useState([]);
	const [minPrice, setMinPrice] = useState(1000);
	const [maxPrice, setMaxPrice] = useState(1000000);
	const [minArea, setMinArea] = useState(100);
	const [maxArea, setMaxArea] = useState(1000);

	const [wantToSee, setWantToSee] = useState([]);
	const [dontWantToSee, setDontWantToSee] = useState([]);
	const [wantToSeeInput, setWantToSeeInput] = useState("");
	const [dontWantToSeeInput, setDontWantToSeeInput] = useState("");
	const [showWantDropdown, setShowWantDropdown] = useState(false);
	const [showDontDropdown, setShowDontDropdown] = useState(false);

	const [priceFrom, setPriceFrom] = useState("");
	const [priceTo, setPriceTo] = useState("");
	const [areaFrom, setAreaFrom] = useState("");
	const [areaTo, setAreaTo] = useState("");
	const [floorOption, setFloorOption] = useState("Любой");
	const [placeOptions, setPlaceOptions] = useState(["Любой"]);

	// Получение данных с сервера
	useEffect(() => {
		fetch("http://localhost:8080/rubrics")
			.then((res) => res.json())
			.then((data) =>
				setRubrics(data.sort((a, b) => a.localeCompare(b, "ru")))
			)
			.catch(() => setRubrics([]));
		fetch("http://localhost:8080/price-range")
			.then((res) => res.json())
			.then((data) => {
				setMinPrice(data.minPrice || 1000);
				setMaxPrice(data.maxPrice || 1000000);
			});
		fetch("http://localhost:8080/area-range")
			.then((res) => res.json())
			.then((data) => {
				setMinArea(data.minArea || 100);
				setMaxArea(data.maxArea || 1000);
			});
	}, []);

	// --- Маркеры и карта ---
	const [selectionData, setSelectionData] = useState(null); // ответ от сервера
	const [showRealty, setShowRealty] = useState(true);
	const [showPreferred, setShowPreferred] = useState(false);
	const [showAvoided, setShowAvoided] = useState(false);
	const mapImgRef = useRef(null);
	const [mapSize, setMapSize] = useState({ width: 0, height: 0 });

	// Обновлять размер карты при ресайзе
	useEffect(() => {
		function updateSize() {
			if (mapImgRef.current) {
				setMapSize({
					width: mapImgRef.current.clientWidth,
					height: mapImgRef.current.clientHeight,
				});
			}
		}
		updateSize();
		window.addEventListener("resize", updateSize);
		return () => window.removeEventListener("resize", updateSize);
	}, []);

	// Обновлять размер при загрузке картинки
	const handleMapImgLoad = useCallback(() => {
		if (mapImgRef.current) {
			setMapSize({
				width: mapImgRef.current.clientWidth,
				height: mapImgRef.current.clientHeight,
			});
		}
	}, []);

	// Обработчики тегов
	const handleAddTag = (input, setInput, tags, setTags) => {
		if (input && !tags.includes(input)) {
			setTags([...tags, input]);
			setInput("");
		}
	};
	const handleRemoveTag = (tag, tags, setTags) => {
		setTags(tags.filter((t) => t !== tag));
	};

	// Обработчики выпадающих списков
	const filteredWantSuggestions = rubrics.filter(
		(r) =>
			r.toLowerCase().includes(wantToSeeInput.toLowerCase()) &&
			!wantToSee.includes(r)
	);
	const filteredDontSuggestions = rubrics.filter(
		(r) =>
			r.toLowerCase().includes(dontWantToSeeInput.toLowerCase()) &&
			!dontWantToSee.includes(r)
	);

	// Обработчики чекбоксов типа помещения
	const handlePlaceOptionChange = (value) => {
		if (value === "Любой") {
			setPlaceOptions(["Любой"]);
		} else {
			let newOptions = placeOptions.includes("Любой")
				? [value]
				: placeOptions.includes(value)
					? placeOptions.filter((v) => v !== value)
					: [...placeOptions, value];
			if (newOptions.length === 0) newOptions = ["Любой"];
			setPlaceOptions(newOptions);
		}
	};

	// Обработчик кнопки "Подобрать место"
	const handleSubmit = async (e) => {
		e.preventDefault();
		const data = {
			wantToSee,
			dontWantToSee,
			priceFrom: priceFrom ? parseInt(priceFrom) : minPrice,
			priceTo: priceTo ? parseInt(priceTo) : maxPrice,
			floorOption,
			placeOptions,
			areaFrom: areaFrom ? parseInt(areaFrom) : minArea,
			areaTo: areaTo ? parseInt(areaTo) : maxArea,
		};
		try {
			const response = await fetch("http://localhost:8080/select-realty", {
				method: "POST",
				headers: { "Content-Type": "application/json" },
				body: JSON.stringify(data),
			});
			if (!response.ok) throw new Error("Ошибка сети");
			const result = await response.json();
			setSelectionData(result);
			setShowRealty(!!(result.realtyList && result.realtyList.length));
			setShowPreferred(false);
			setShowAvoided(false);
		} catch (error) {
      console.log(error);      
			setSelectionData(null);
			setShowRealty(false);
			setShowPreferred(false);
			setShowAvoided(false);
		}
	};

	function MarkerCluster({ clusters, color, type }) {
		return clusters.map((cluster, idx) => {
			if (!cluster.length) return null;
			const sumX = cluster.reduce((sum, item) => sum + item.px, 0);
			const sumY = cluster.reduce((sum, item) => sum + item.py, 0);
			const avgX = sumX / cluster.length;
			const avgY = sumY / cluster.length;
			return (
				<div
					key={idx}
					className={
						type === "realty"
							? `marker${cluster.length > 1 ? " group-marker" : ""}`
							: `place-marker ${color}-marker`
					}
					style={{ left: avgX, top: avgY, position: "absolute" }}
				>
					{type === "realty" && cluster.length > 1 && (
						<div className="marker-count">{cluster.length}</div>
					)}
					<div className="tooltip">
						{type === "realty"
							? cluster.map((item, i) => (
									<div className="tooltip-item" key={i}>
										<strong>{item.entityName}</strong>
										<br />
										Адрес: {item.address}
										<br />
										Площадь: {item.totalArea} м²
										<br />
										Цена:{" "}
										{formatNumberWithSpaces(
											item.leasePrice
										)}{" "}
										₽
									</div>
								))
							: cluster.map((place, i) => (
									<div key={i}>
										<strong>{place.name}</strong>
										<br />
										Адрес: {place.address}
										<br />
										Рубрики:{" "}
										{place.rubrics &&
											place.rubrics.join(", ")}
									</div>
								))}
					</div>
				</div>
			);
		});
	}

	const handleMapCheckbox = (type) => {
		if (type === "realty") setShowRealty((v) => !v);
		if (type === "preferred") setShowPreferred((v) => !v);
		if (type === "avoided") setShowAvoided((v) => !v);
	};

	let realtyClusters = [],
		preferredClusters = [],
		avoidedClusters = [];
	if (selectionData && mapSize.width && mapSize.height) {
		if (
			showRealty &&
			selectionData.realtyList &&
			selectionData.realtyList.length
		)
			realtyClusters = clusterMarkers(
				selectionData.realtyList,
				mapSize.width,
				mapSize.height
			);
		if (
			showPreferred &&
			selectionData.preferredPlaces &&
			selectionData.preferredPlaces.length
		)
			preferredClusters = clusterMarkers(
				selectionData.preferredPlaces,
				mapSize.width,
				mapSize.height
			);
		if (
			showAvoided &&
			selectionData.avoidedPlaces &&
			selectionData.avoidedPlaces.length
		)
			avoidedClusters = clusterMarkers(
				selectionData.avoidedPlaces,
				mapSize.width,
				mapSize.height
			);
	}

	const clearPriceFrom = () => setPriceFrom("");
	const clearPriceTo = () => setPriceTo("");
	const clearAreaFrom = () => setAreaFrom("");
	const clearAreaTo = () => setAreaTo("");

	return (
		<main className="main">
			<div className="container">
				<form className="map-settings" onSubmit={handleSubmit}>
					<h2>Настройки карты</h2>
					<div className="params_container">
						<h3>То, что вы хотите видеть рядом</h3>
						<div className="autocomplete">
							<input
								type="text"
								className="input"
								placeholder="Начните вводить..."
								value={wantToSeeInput}
								onChange={(e) => {
									setWantToSeeInput(e.target.value);
									setShowWantDropdown(true);
								}}
								onFocus={() => setShowWantDropdown(true)}
								onBlur={() =>
									setTimeout(
										() => setShowWantDropdown(false),
										200
									)
								}
								onKeyDown={(e) => {
									if (e.key === "Enter") {
										e.preventDefault();
										handleAddTag(
											wantToSeeInput,
											setWantToSeeInput,
											wantToSee,
											setWantToSee
										);
									}
								}}
							/>
							{showWantDropdown && (
								<div className="dropdown-content autocomplete-dropdown">
									{filteredWantSuggestions.length > 0 ? (
										filteredWantSuggestions.map((s) => (
											<div
												key={s}
												onClick={() =>
													handleAddTag(
														s,
														setWantToSeeInput,
														wantToSee,
														setWantToSee
													)
												}
											>
												{s}
											</div>
										))
									) : (
										<div>Нет совпадений</div>
									)}
								</div>
							)}
						</div>
						<div className="tags_container">
							{wantToSee.map((tag) => (
								<div className="tag" key={tag}>
									<span>{tag}</span>
									<button
										type="button"
										className="remove-btn"
										onClick={() =>
											handleRemoveTag(
												tag,
												wantToSee,
												setWantToSee
											)
										}
									>
										×
									</button>
								</div>
							))}
						</div>
					</div>
					<div className="params_container">
						<h3>
							То, что вы хотите <span>не видеть</span> рядом
						</h3>
						<div className="autocomplete">
							<input
								type="text"
								className="input"
								placeholder="Начните вводить..."
								value={dontWantToSeeInput}
								onChange={(e) => {
									setDontWantToSeeInput(e.target.value);
									setShowDontDropdown(true);
								}}
								onFocus={() => setShowDontDropdown(true)}
								onBlur={() =>
									setTimeout(
										() => setShowDontDropdown(false),
										200
									)
								}
								onKeyDown={(e) => {
									if (e.key === "Enter") {
										e.preventDefault();
										handleAddTag(
											dontWantToSeeInput,
											setDontWantToSeeInput,
											dontWantToSee,
											setDontWantToSee
										);
									}
								}}
							/>
							{showDontDropdown && (
								<div className="dropdown-content autocomplete-dropdown">
									{filteredDontSuggestions.length > 0 ? (
										filteredDontSuggestions.map((s) => (
											<div
												key={s}
												onClick={() =>
													handleAddTag(
														s,
														setDontWantToSeeInput,
														dontWantToSee,
														setDontWantToSee
													)
												}
											>
												{s}
											</div>
										))
									) : (
										<div>Нет совпадений</div>
									)}
								</div>
							)}
						</div>
						<div className="tags_container">
							{dontWantToSee.map((tag) => (
								<div className="tag" key={tag}>
									<span>{tag}</span>
									<button
										type="button"
										className="remove-btn"
										onClick={() =>
											handleRemoveTag(
												tag,
												dontWantToSee,
												setDontWantToSee
											)
										}
									>
										×
									</button>
								</div>
							))}
						</div>
					</div>
					<div className="params_container" id="extraSettings">
						<h3>Дополнительные параметры</h3>
						<div className="input_container price-container">
							<div className="label">Цена:</div>
							<div className="inputs-row">
								<div className="input-wrapper">
									<input
										type="text"
										className="input"
										placeholder={`от ${formatNumberWithSpaces(minPrice)} ₽`}
										value={priceFrom}
										onChange={(e) =>
											setPriceFrom(
												e.target.value.replace(
													/[^\d]/g,
													""
												)
											)
										}
									/>
									<span
										className="clear-btn"
										onClick={clearPriceFrom}
									>
										<svg
											width="12"
											height="12"
											viewBox="0 0 12 12"
											fill="none"
											xmlns="http://www.w3.org/2000/svg"
										>
											<path
												d="M1 1L11 11M11 1L1 11"
												stroke="#888"
												strokeWidth="2"
												strokeLinecap="round"
											/>
										</svg>
									</span>
								</div>
								<div className="input-wrapper">
									<input
										type="text"
										className="input"
										placeholder={`до ${formatNumberWithSpaces(maxPrice)} ₽`}
										value={priceTo}
										onChange={(e) =>
											setPriceTo(
												e.target.value.replace(
													/[^\d]/g,
													""
												)
											)
										}
									/>
									<span
										className="clear-btn"
										onClick={clearPriceTo}
									>
										<svg
											width="12"
											height="12"
											viewBox="0 0 12 12"
											fill="none"
											xmlns="http://www.w3.org/2000/svg"
										>
											<path
												d="M1 1L11 11M11 1L1 11"
												stroke="#888"
												strokeWidth="2"
												strokeLinecap="round"
											/>
										</svg>
									</span>
								</div>
							</div>
						</div>
						<div
							className="dropdown_container floor-container"
							id="floorOption"
						>
							<div className="label">Этаж:</div>
							<div className="dropdown" id="dropdown1">
								{[
									"Любой",
									"Только 1-й",
									"До 5-го",
									"Не 1-й",
									"Не последний",
								].map((opt) => (
									<label key={opt}>
										<input
											type="radio"
											name="floorOption"
											value={opt}
											checked={floorOption === opt}
											onChange={() => setFloorOption(opt)}
										/>{" "}
										{opt}
									</label>
								))}
							</div>
						</div>
						<div
							className="dropdown_container type-container"
							id="placeOption"
						>
							<div className="label">Тип помещения: </div>
							<div className="dropdown" id="dropdown2">
								{defaultPlaceOptions.map((opt) => (
									<label key={opt.value}>
										<input
											className="checkbox"
											type="checkbox"
											value={opt.value}
											checked={placeOptions.includes(
												opt.value
											)}
											onChange={() =>
												handlePlaceOptionChange(
													opt.value
												)
											}
										/>{" "}
										{opt.label}
									</label>
								))}
							</div>
						</div>
						<div className="input_container area-container">
							<div className="label">Площадь:</div>
							<div className="inputs-row">
								<div className="input-wrapper">
									<input
										type="text"
										className="input"
										placeholder={`от ${formatNumberWithSpaces(minArea)} м²`}
										value={areaFrom}
										onChange={(e) =>
											setAreaFrom(
												e.target.value.replace(
													/[^\d]/g,
													""
												)
											)
										}
									/>
									<span
										className="clear-btn"
										onClick={clearAreaFrom}
									>
										<svg
											width="12"
											height="12"
											viewBox="0 0 12 12"
											fill="none"
											xmlns="http://www.w3.org/2000/svg"
										>
											<path
												d="M1 1L11 11M11 1L1 11"
												stroke="#888"
												strokeWidth="2"
												strokeLinecap="round"
											/>
										</svg>
									</span>
								</div>
								<div className="input-wrapper">
									<input
										type="text"
										className="input"
										placeholder={`до ${formatNumberWithSpaces(maxArea)} м²`}
										value={areaTo}
										onChange={(e) =>
											setAreaTo(
												e.target.value.replace(
													/[^\d]/g,
													""
												)
											)
										}
									/>
									<span
										className="clear-btn"
										onClick={clearAreaTo}
									>
										<svg
											width="12"
											height="12"
											viewBox="0 0 12 12"
											fill="none"
											xmlns="http://www.w3.org/2000/svg"
										>
											<path
												d="M1 1L11 11M11 1L1 11"
												stroke="#888"
												strokeWidth="2"
												strokeLinecap="round"
											/>
										</svg>
									</span>
								</div>
							</div>
						</div>
					</div>
					<button className="btn" type="submit">
						<span>Подобрать место</span>
						<svg
							className="arrow-right"
							width="25"
							height="24"
							viewBox="0 0 25 24"
							fill="none"
							xmlns="http://www.w3.org/2000/svg"
						>
							<path
								d="M5.5 12H19.5"
								stroke="currentColor"
								strokeWidth="2"
								strokeLinecap="round"
								strokeLinejoin="round"
							/>
							<path
								d="M12.5 5L19.5 12L12.5 19"
								stroke="currentColor"
								strokeWidth="2"
								strokeLinecap="round"
								strokeLinejoin="round"
							/>
						</svg>
					</button>
				</form>
				<div className="map" style={{ position: "relative" }}>
					<img
						src={map}
						alt=""
						ref={mapImgRef}
						onLoad={handleMapImgLoad}
						style={{ width: "100%", display: "block" }}
					/>
					<div
						className="markers-container"
						style={{
							position: "absolute",
							left: 0,
							top: 0,
							width: "100%",
							height: "100%",
						}}
					>
						<MarkerCluster
							clusters={realtyClusters}
							color=""
							type="realty"
						/>
						<MarkerCluster
							clusters={preferredClusters}
							color="green"
							type="preferred"
						/>
						<MarkerCluster
							clusters={avoidedClusters}
							color="red"
							type="avoided"
						/>
					</div>
				</div>
				<div className="map-descr">
					<h1>KrutoMaps</h1>
					<p>Аналитика крутости места для вашего бизнеса</p>
					<div className="best_places">
						<h2 className="place">Крутейшие места</h2>
						<button className="place-btn">
							<span>ул. Эскобара</span>
							<svg
								width="25"
								height="24"
								viewBox="0 0 25 24"
								fill="none"
								xmlns="http://www.w3.org/2000/svg"
							>
								<path
									d="M21.5 10C21.5 17 12.5 23 12.5 23C12.5 23 3.5 17 3.5 10C3.5 7.61305 4.44821 5.32387 6.13604 3.63604C7.82387 1.94821 10.1131 1 12.5 1C14.8869 1 17.1761 1.94821 18.864 3.63604C20.5518 5.32387 21.5 7.61305 21.5 10Z"
									stroke="white"
									strokeWidth="2"
									strokeLinecap="round"
									strokeLinejoin="round"
								/>
								<path
									d="M12.5 13C14.1569 13 15.5 11.6569 15.5 10C15.5 8.34315 14.1569 7 12.5 7C10.8431 7 9.5 8.34315 9.5 10C9.5 11.6569 10.8431 13 12.5 13Z"
									stroke="white"
									strokeWidth="2"
									strokeLinecap="round"
									strokeLinejoin="round"
								/>
							</svg>
						</button>
						<button className="place-btn">
							<span>ул. Чпок Стрит</span>
							<svg
								width="25"
								height="24"
								viewBox="0 0 25 24"
								fill="none"
								xmlns="http://www.w3.org/2000/svg"
							>
								<path
									d="M21.5 10C21.5 17 12.5 23 12.5 23C12.5 23 3.5 17 3.5 10C3.5 7.61305 4.44821 5.32387 6.13604 3.63604C7.82387 1.94821 10.1131 1 12.5 1C14.8869 1 17.1761 1.94821 18.864 3.63604C20.5518 5.32387 21.5 7.61305 21.5 10Z"
									stroke="white"
									strokeWidth="2"
									strokeLinecap="round"
									strokeLinejoin="round"
								/>
								<path
									d="M12.5 13C14.1569 13 15.5 11.6569 15.5 10C15.5 8.34315 14.1569 7 12.5 7C10.8431 7 9.5 8.34315 9.5 10C9.5 11.6569 10.8431 13 12.5 13Z"
									stroke="white"
									strokeWidth="2"
									strokeLinecap="round"
									strokeLinejoin="round"
								/>
							</svg>
						</button>
						<button className="place-btn">
							<span>пер. Трубный</span>
							<svg
								width="25"
								height="24"
								viewBox="0 0 25 24"
								fill="none"
								xmlns="http://www.w3.org/2000/svg"
							>
								<path
									d="M21.5 10C21.5 17 12.5 23 12.5 23C12.5 23 3.5 17 3.5 10C3.5 7.61305 4.44821 5.32387 6.13604 3.63604C7.82387 1.94821 10.1131 1 12.5 1C14.8869 1 17.1761 1.94821 18.864 3.63604C20.5518 5.32387 21.5 7.61305 21.5 10Z"
									stroke="white"
									strokeWidth="2"
									strokeLinecap="round"
									strokeLinejoin="round"
								/>
								<path
									d="M12.5 13C14.1569 13 15.5 11.6569 15.5 10C15.5 8.34315 14.1569 7 12.5 7C10.8431 7 9.5 8.34315 9.5 10C9.5 11.6569 10.8431 13 12.5 13Z"
									stroke="white"
									strokeWidth="2"
									strokeLinecap="round"
									strokeLinejoin="round"
								/>
							</svg>
						</button>
						<button className="place-btn">
							<span>пр. Мэтью Саливан</span>
							<svg
								width="25"
								height="24"
								viewBox="0 0 25 24"
								fill="none"
								xmlns="http://www.w3.org/2000/svg"
							>
								<path
									d="M21.5 10C21.5 17 12.5 23 12.5 23C12.5 23 3.5 17 3.5 10C3.5 7.61305 4.44821 5.32387 6.13604 3.63604C7.82387 1.94821 10.1131 1 12.5 1C14.8869 1 17.1761 1.94821 18.864 3.63604C20.5518 5.32387 21.5 7.61305 21.5 10Z"
									stroke="white"
									strokeWidth="2"
									strokeLinecap="round"
									strokeLinejoin="round"
								/>
								<path
									d="M12.5 13C14.1569 13 15.5 11.6569 15.5 10C15.5 8.34315 14.1569 7 12.5 7C10.8431 7 9.5 8.34315 9.5 10C9.5 11.6569 10.8431 13 12.5 13Z"
									stroke="white"
									strokeWidth="2"
									strokeLinecap="round"
									strokeLinejoin="round"
								/>
							</svg>
						</button>
					</div>
					<form className="params_container">
						<h3>Отобразить на карте</h3>
						<label className="seeOnMap">
							<input
								className="checkbox"
								type="checkbox"
								checked={showRealty}
								onChange={() => handleMapCheckbox("realty")}
							/>{" "}
							Недвижимость
						</label>
						<label className="seeOnMap">
							<input
								className="checkbox"
								type="checkbox"
								checked={showPreferred}
								onChange={() => handleMapCheckbox("preferred")}
							/>{" "}
							Предпочтительные объекты
						</label>
						<label className="seeOnMap">
							<input
								className="checkbox"
								type="checkbox"
								checked={showAvoided}
								onChange={() => handleMapCheckbox("avoided")}
							/>{" "}
							Непредпочтительные объекты
						</label>
					</form>
				</div>
			</div>
		</main>
	);
}

export default App;
