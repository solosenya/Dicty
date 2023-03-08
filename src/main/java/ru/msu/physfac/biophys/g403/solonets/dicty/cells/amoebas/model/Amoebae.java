package ru.msu.physfac.biophys.g403.solonets.dicty.cells.amoebas.model;

import lombok.Getter;
import lombok.Setter;

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
    @Enumerated(EnumType.STRING)
    private State state;

    @Column(name = "cell_id")
    private Integer cellId;

    @Column(name = "time")
    private Integer time;

    public enum State {
        EXCITED,
        RESTING,
        READY,
        EMPTY
    }
}
