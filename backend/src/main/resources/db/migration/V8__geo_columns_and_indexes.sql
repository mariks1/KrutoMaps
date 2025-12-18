ALTER TABLE place
    ADD COLUMN IF NOT EXISTS geom geography(Point,4326)
    GENERATED ALWAYS AS (ST_SetSRID(ST_MakePoint(lon, lat), 4326)::geography) STORED;

CREATE INDEX IF NOT EXISTS place_geom_gist ON place USING GIST (geom);

ALTER TABLE realty
    ADD COLUMN IF NOT EXISTS geom geography(Point,4326)
    GENERATED ALWAYS AS (ST_SetSRID(ST_MakePoint(point_x, point_y), 4326)::geography) STORED;

CREATE INDEX IF NOT EXISTS realty_geom_gist ON realty USING GIST (geom);

CREATE INDEX IF NOT EXISTS place_rubrics_gin ON place USING GIN (rubrics);

CREATE INDEX IF NOT EXISTS realty_price_idx ON realty (lease_price);
CREATE INDEX IF NOT EXISTS realty_area_idx  ON realty (total_area);
CREATE INDEX IF NOT EXISTS realty_floor_idx ON realty (floor);
CREATE INDEX IF NOT EXISTS realty_segment_idx ON realty (segment_type);