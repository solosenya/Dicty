package ru.msu.physfac.biophys.g403.solonets.dicty.cells.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.msu.physfac.biophys.g403.solonets.dicty.cells.model.Cell;

public interface CellRepository extends JpaRepository<Cell, Integer> {
    Cell findCellByXAndY(Integer x, Integer y);
}
