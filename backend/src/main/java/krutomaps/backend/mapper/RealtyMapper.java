package krutomaps.backend.mapper;

import krutomaps.backend.dto.RealtySummaryDTO;
import krutomaps.backend.entity.RealtyEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RealtyMapper {

    @Mapping(target = "latitude", source = "pointY")
    @Mapping(target = "longitude", source = "pointX")
    RealtySummaryDTO toSummary(RealtyEntity entity);
}
