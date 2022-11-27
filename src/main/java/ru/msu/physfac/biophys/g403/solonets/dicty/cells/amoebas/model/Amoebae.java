package ru.msu.physfac.biophys.g403.solonets.dicty.cells.amoebas.model;

import lombok.Data;
import ru.msu.physfac.biophys.g403.solonets.dicty.cells.model.Cell;

import javax.persistence.*;

@Data
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
    private String state;

    @ManyToOne
    private Cell cell;
}
