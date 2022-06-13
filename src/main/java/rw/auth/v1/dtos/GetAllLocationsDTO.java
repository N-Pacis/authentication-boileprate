package rw.auth.v1.dtos;

import lombok.Getter;
import lombok.Setter;
import rw.auth.v1.models.LocationAddress;

@Getter
@Setter
public class GetAllLocationsDTO {

    private LocationAddress province;

    private LocationAddress district;

    private LocationAddress sector;

    private LocationAddress cell;

    private LocationAddress village;
}
