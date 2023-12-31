package ai.giskard.domain.ml;


import ai.giskard.domain.AbstractAuditingEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import java.io.Serial;

@Getter
@Setter
@Entity(name = "inspections")
public class Inspection extends AbstractAuditingEntity {
    @Serial
    private static final long serialVersionUID = 0L;

    @Id
    @JsonIgnore
    @GeneratedValue
    private Long id;

    private String name;

    @ManyToOne
    private Dataset dataset;

    @ManyToOne
    private ProjectModel model;

    private boolean sample;
}
