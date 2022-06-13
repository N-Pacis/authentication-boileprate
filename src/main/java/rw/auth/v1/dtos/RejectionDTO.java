package rw.auth.v1.dtos;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class RejectionDTO {

    @NotBlank
    private String rejectionMessage;
}
