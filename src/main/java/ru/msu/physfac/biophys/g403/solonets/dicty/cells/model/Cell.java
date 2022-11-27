package ru.msu.physfac.biophys.g403.solonets.dicty.cells.model;

import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.msu.physfac.biophys.g403.solonets.dicty.cells.amoebas.model.Amoebae;

import javax.persistence.*;
import java.util.Set;

@Setter
@NoArgsConstructor
@Entity
@Table(name = "cells")
public class Cell {

    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "x", nullable = false)
    private Integer x;

    @Column(name = "y", nullable = false)
    private Integer y;

    @Column(name = "camp_level", nullable = false)
    private Integer campLevel;

    @OneToMany
    private Set<Amoebae> amoebas;
}
