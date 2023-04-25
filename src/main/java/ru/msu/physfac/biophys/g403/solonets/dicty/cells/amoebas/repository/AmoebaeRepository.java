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
    @Query("update Amoebae a set a.state = ?1, a.destination = ?2 where a.id = ?3")
    void setStateAndDestination(Amoebae.State state, Amoebae.Destination destination, Integer id);

    @Modifying
    @Query("update Amoebae a set a.time = ?1 where a.id = ?2")
    void setTime(Integer time, Integer id);

    @Modifying
    @Query("update Amoebae a set a.state = ?1, a.time = ?2, a.position = ?3, a.cellId = ?4 where a.id = ?5")
    void updateAmoebae(Amoebae.State state, Integer time, Integer position, Integer cellId, Integer id);

    @Modifying
    @Query("update Amoebae a set a.state = ?1, a.destination = ?2, a.time = ?3, a.position = ?4, a.cellId = ?5 where a.id = ?6")
    void updateAmoebaeFully(Amoebae.State state, Amoebae.Destination dest, Integer time, Integer position, Integer cellId, Integer id);
}
