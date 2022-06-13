package rw.auth.v1.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import rw.auth.v1.enums.ELocationType;

import javax.persistence.*;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name="location_address")
public class LocationAddress {

    @Id
    @Column(name="location_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "location_type")
    @Enumerated(EnumType.STRING)
    private ELocationType locationType;

    @Column(name="name")
    private String name;

    @Column(name="name_french")
    private String nameFrench;

    @Column(name ="name_kiny")
    private String nameKiny;

    @Transient
    private List<LocationAddress> residentialAddress;

    @JsonIgnore
    @ManyToOne(cascade = CascadeType.PERSIST)
    @JoinColumn(name = "parent_id")
    private LocationAddress parentId;
}