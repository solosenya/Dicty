package ru.msu.physfac.biophys.g403.solonets.dicty.cells.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import ru.msu.physfac.biophys.g403.solonets.dicty.cells.model.Cell;

public interface CellRepository extends JpaRepository<Cell, Integer> {
    Cell findCellByXAndY(Integer x, Integer y);

    @Modifying
    @Query("update Cell c set c.campLevel = ?1 where c.id = ?2")
    void setLevel(Integer level, Integer cellId);
}
