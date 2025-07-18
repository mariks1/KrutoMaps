CREATE TEMP TABLE poi_raw (
    ids              bigint,
    ext_id           bigint,
    name             text,
    address_name     text,
    address_comment  text,
    lat              double precision,
    lon              double precision,
    rubrics          text
);

COPY poi_raw
    FROM '/csv/poi.csv'
    DELIMITER '|'
    CSV HEADER
    NULL 'NULL';

INSERT INTO place (
    name,
    address,
    address_comment,
    lat,
    lon,
    rubrics
)
SELECT
    p.name,
    p.address_name,
    NULLIF(p.address_comment, '') AS address_comment,
    p.lat,
    p.lon,
    string_to_array(
            regexp_replace(p.rubrics, '\[|\]|''', '', 'g'),
            ', '
    )::text[]
FROM poi_raw p;
