package ru.msu.physfac.biophys.g403.solonets.dicty.cells.amoebas.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.msu.physfac.biophys.g403.solonets.dicty.cells.amoebas.model.Amoebae;

import java.util.Optional;

public interface AmoebaeRepository extends JpaRepository<Amoebae, Long> {

    Optional<Amoebae> findByCellIdAndPosition(Integer cellId, Integer position);
}
