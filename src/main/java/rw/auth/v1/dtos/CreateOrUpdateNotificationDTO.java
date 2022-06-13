package rw.auth.v1.dtos;

import com.sun.istack.NotNull;
import lombok.*;
import rw.auth.v1.enums.ENotificationStatus;
import rw.auth.v1.enums.ENotificationType;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.UUID;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrUpdateNotificationDTO {

    @NotBlank
    @Size(max=50, min = 3)
    private String message;

    @NonNull
    private UUID userId;

    @NotNull
    private ENotificationType type;

    private ENotificationStatus status = ENotificationStatus.UNREAD;

}
