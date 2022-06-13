package rw.auth.v1.dtos;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.util.List;
import java.util.UUID;

@Data
public class ApproveManyUsersDTO {

    @NotEmpty
    public List<UUID> userIds;
}
