package rw.auth.v1.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import rw.auth.v1.audits.InitiatorAudit;
import rw.auth.v1.dtos.CreateOrUpdateNotificationDTO;
import rw.auth.v1.enums.ENotificationStatus;
import rw.auth.v1.enums.ENotificationType;

import javax.persistence.*;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "notifications")
public class Notification extends InitiatorAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "message")
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(name = "type")
    private ENotificationType type;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private ENotificationStatus status = ENotificationStatus.UNREAD;

    public Notification(User user, String message, String description, ENotificationType type, String url, ENotificationStatus status) {
        this.user = user;
        this.message = message;
        this.type = type;
        this.status = status;
    }

    public Notification(CreateOrUpdateNotificationDTO notificationDTO, User user){
        this.user = user;
        this.message = notificationDTO.getMessage();
        this.type = notificationDTO.getType();
        this.status = notificationDTO.getStatus();
    }
}
