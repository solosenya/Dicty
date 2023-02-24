package ru.msu.physfac.biophys.g403.solonets.dicty.cells.amoebas.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import ru.msu.physfac.biophys.g403.solonets.dicty.cells.amoebas.model.Amoebae;

import java.util.List;
import java.util.Optional;

public interface AmoebaeRepository extends JpaRepository<Amoebae, Integer> {

    Optional<Amoebae> findByCellIdAndPosition(Integer cellId, Integer position);

    Optional<List<Amoebae>> findAllByState(Amoebae.State state);

    @Modifying
    @Query("update Amoebae a set a.state = ?1 where a.id = ?2")
    void setState(Amoebae.State state, Integer id);

    @Modifying
    @Query("update Amoebae a set a.time = ?1 where a.id = ?2")
    void setTime(Integer time, Integer id);
}
