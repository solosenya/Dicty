package ru.msu.physfac.biophys.g403.solonets.dicty.cells.amoebas.model;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import ru.msu.physfac.biophys.g403.solonets.dicty.cells.model.Cell;

import javax.persistence.*;

@Getter
@Setter
@Entity
@Table(name = "amoebas")
public class Amoebae {

    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "position")
    private Integer position;

    @Column(name = "state")
    private State state;

    @Column(name = "cell_id")
    private Integer cellId;

    public enum State {
        EXCITED,
        RESTING,
        READY
    }
}
