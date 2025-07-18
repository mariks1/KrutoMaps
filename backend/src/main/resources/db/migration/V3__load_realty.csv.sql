CREATE TEMP TABLE realty_raw (
    unid            bigint,
    point_x         double precision,
    point_y         double precision,
    main_type       text,
    segment_type    text,
    entity_name     text,
    total_area      double precision,
    floor           int,
    lease_price     double precision,
    additional_info text,
    source_info     text,
    address         text,
    update_date     date
);

COPY realty_raw
    FROM '/csv/realty.csv'
    CSV HEADER
    NULL 'NULL';

INSERT INTO realty (
    point_x, point_y,
    main_type, segment_type,
    entity_name, total_area,
    floor, lease_price,
    additional_info, source_info,
    address, update_date,
    square_num
)
SELECT
    r.point_x,
    r.point_y,
    r.main_type,
    r.segment_type,
    r.entity_name,
    r.total_area,
    r.floor,
    r.lease_price,
    r.additional_info,
    r.source_info,
    r.address,
    r.update_date,
    s.id AS square_num
FROM realty_raw r
         JOIN square s
              ON r.point_y <= s.top_left_lat
                  AND r.point_y >= s.bottom_right_lat
                  AND r.point_x >= s.top_left_lon
                  AND r.point_x <= s.bottom_right_lon;