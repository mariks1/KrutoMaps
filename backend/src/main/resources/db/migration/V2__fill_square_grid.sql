WITH params AS (
    SELECT
        10                                AS n,
        59.991791  ::double precision     AS top_lat,
    59.840498  ::double precision     AS bottom_lat,
    30.202104  ::double precision     AS left_lon,
    30.502827  ::double precision     AS right_lon
    ),
    grid AS (
SELECT
    i, j,
    top_lat  -  i     *  (top_lat  - bottom_lat) / n   AS cell_top_lat,
    left_lon +  j     *  (right_lon - left_lon) / n    AS cell_left_lon,
    top_lat  - (i+1) *  (top_lat  - bottom_lat) / n   AS cell_bottom_lat,
    left_lon + (j+1) *  (right_lon - left_lon) / n    AS cell_right_lon
FROM params,
    generate_series(0, (SELECT n-1 FROM params)) AS i,
    generate_series(0, (SELECT n-1 FROM params)) AS j
    )
INSERT INTO square (
    i, j,
    top_left_lat,  top_left_lon,
    bottom_right_lat, bottom_right_lon,
    center_lat, center_lon
)
SELECT
    i, j,
    cell_top_lat,  cell_left_lon,
    cell_bottom_lat, cell_right_lon,
    (cell_top_lat + cell_bottom_lat) / 2,
    (cell_left_lon + cell_right_lon) / 2
FROM grid
ORDER BY i, j;
