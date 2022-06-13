package rw.auth.v1.dtos;

import lombok.Data;

import java.util.UUID;

@Data
public class VerifyUserEmailDTO {

    private UUID userId;

}
