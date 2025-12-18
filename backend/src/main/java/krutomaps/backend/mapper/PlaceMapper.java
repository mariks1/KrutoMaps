package krutomaps.backend.mapper;

import krutomaps.backend.dto.PlaceMarkerDTO;
import krutomaps.backend.entity.PlaceEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PlaceMapper {

    @Mapping(target = "latitude", source = "entity.lat")
    @Mapping(target = "longitude", source = "entity.lon")
    @Mapping(target = "markerType", expression = "java(markerType)")
    PlaceMarkerDTO toMarkerDTO(PlaceEntity entity, PlaceMarkerDTO.MarkerType markerType);
}
