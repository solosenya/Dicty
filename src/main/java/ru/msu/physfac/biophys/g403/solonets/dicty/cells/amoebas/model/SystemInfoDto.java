package ru.msu.physfac.biophys.g403.solonets.dicty.cells.amoebas.model;

import lombok.Getter;
import ru.msu.physfac.biophys.g403.solonets.dicty.cells.model.Cell;

import java.util.*;
import java.util.stream.Collectors;

import static ru.msu.physfac.biophys.g403.solonets.dicty.cells.amoebas.model.Amoebae.State.RESTING;
import static ru.msu.physfac.biophys.g403.solonets.dicty.cells.amoebas.model.Amoebae.State.READY;
import static ru.msu.physfac.biophys.g403.solonets.dicty.cells.amoebas.model.Amoebae.State.PACESETTER;
import static ru.msu.physfac.biophys.g403.solonets.dicty.cells.amoebas.model.Amoebae.State.EXCITED;

import static ru.msu.physfac.biophys.g403.solonets.dicty.cells.amoebas.model.Amoebae.Destination.RIGHT;
import static ru.msu.physfac.biophys.g403.solonets.dicty.cells.amoebas.model.Amoebae.Destination.LEFT;
import static ru.msu.physfac.biophys.g403.solonets.dicty.cells.amoebas.model.Amoebae.Destination.TOP;
import static ru.msu.physfac.biophys.g403.solonets.dicty.cells.amoebas.model.Amoebae.Destination.BOTTOM;

@Getter
public class SystemInfoDto {
    private final List<Amoebae> restingAmoebas;
    private final List<Amoebae> excitedAmoebas;
    private final List<Amoebae> pacesetters;
    private final List<Amoebae> readies;
    private final Map<Integer, List<Amoebae>> groupedAmoebasByCellId;
    private final List<Cell> cells;
    private final Map<Cell, Integer> newCells = new HashMap<>();
    private final Map<Integer, Cell> cellsById;
    private Map<Integer, List<Cell>> neighboursByCentral;
    private Map<Integer, List<Cell>> farNeighboursByCentral;

    public SystemInfoDto(List<Cell> cells, List<Amoebae> amoebas) {
        this.cells = cells;
        cellsById = cells.stream()
            .collect(Collectors.toMap(
                Cell::getId,
                c -> c
            ));

        Map<Amoebae.State, List<Amoebae>> amoebasByState = amoebas.stream()
            .collect(Collectors.groupingBy(Amoebae::getState));
        restingAmoebas = Optional.ofNullable(amoebasByState.get(RESTING))
            .orElse(new ArrayList<>());
        excitedAmoebas = Optional.ofNullable(amoebasByState.get(EXCITED))
            .orElse(new ArrayList<>());
        pacesetters = Optional.ofNullable(amoebasByState.get(PACESETTER))
            .orElse(new ArrayList<>());
        readies = Optional.ofNullable(amoebasByState.get(READY))
            .orElse(new ArrayList<>());

        groupedAmoebasByCellId = amoebas.stream()
            .collect(Collectors.groupingBy(Amoebae::getCellId));

        findNeighboursForEveryCell(cells);
    }

    public Cell findCellByAmoebae(Amoebae amoebae) {
        Integer cellId = amoebae.getCellId();
        return cellsById.get(cellId);
    }

    public Integer findCellIdByAmoebae(Amoebae amoebae) {
        Integer cellId = amoebae.getCellId();
        return cellsById.get(cellId)
            .getId();
    }

    public List<Cell> getNeighboursByCentralId(Integer centralId) {
        return neighboursByCentral.get(centralId);
    }

    public List<Cell> getNeighboursByCentralIdWithDest(Integer centralId, Amoebae.Destination dest) {
        List<Cell> allNeighbours = neighboursByCentral.get(centralId);
        List<Cell> correctNeighbours = new ArrayList<>();
        Cell central = cellsById.get(centralId);
        for (Cell neighbour : allNeighbours) {
            int xDif = neighbour.getX() - central.getX();
            int yDif = neighbour.getY() - central.getY();
            if (dest.equals(RIGHT) && xDif == RIGHT.getDifX()) {
                correctNeighbours.add(neighbour);
                continue;
            }
            if (dest.equals(LEFT) && xDif == LEFT.getDifX()) {
                correctNeighbours.add(neighbour);
                continue;
            }
            if (dest.equals(TOP) && yDif == TOP.getDifY()) {
                correctNeighbours.add(neighbour);
                continue;
            }
            if (dest.equals(BOTTOM) && yDif == BOTTOM.getDifY()) {
                correctNeighbours.add(neighbour);
            }
        }
        return correctNeighbours;
    }

    public List<Cell> getFarNeighboursByCentralId(Integer centralId) {
        return farNeighboursByCentral.get(centralId);
    }

    private void findNeighboursForEveryCell(List<Cell> cellsList) {
        neighboursByCentral = new HashMap<>();
        farNeighboursByCentral = new HashMap<>();
        for (Cell cell: cellsList) {
            neighboursByCentral.put(cell.getId(), new ArrayList<>());
            farNeighboursByCentral.put(cell.getId(), new ArrayList<>());
        }

        List<Cell> cellsCopy = new ArrayList<>(
                List.copyOf(cellsList)
        );

        for (Cell cell : cellsList) {
            cellsCopy.remove(cell);
            for (Cell otherCell : cellsCopy) {
                if (otherCell.equals(cell)) continue;

                if (areNeighbours(cell, otherCell)) {
                    neighboursByCentral.get(cell.getId()).add(otherCell);
                    neighboursByCentral.get(otherCell.getId()).add(cell);
                }

                if (areFarNeighbours(cell, otherCell)) {
                    farNeighboursByCentral.get(cell.getId()).add(otherCell);
                    farNeighboursByCentral.get(otherCell.getId()).add(cell);
                }
            }
        }
    }

    private boolean areNeighbours(Cell cell, Cell otherCell) {
        int xDif = cell.getX() - otherCell.getX();
        int yDif = cell.getY() - otherCell.getY();

        boolean xDifIsValid = xDif >= -1 && xDif <= 1;
        boolean yDifIsValid = yDif >= -1 && yDif <= 1;
        return xDifIsValid && yDifIsValid;
    }

    private boolean areFarNeighbours(Cell cell, Cell otherCell) {
        if (areNeighbours(cell, otherCell)) {
            return false;
        }
        int xDif = cell.getX() - otherCell.getX();
        int yDif = cell.getY() - otherCell.getY();

        boolean xDifIsValid = xDif >= -3 && xDif <= 3;
        boolean yDifIsValid = yDif >= -3 && yDif <= 3;

        return xDifIsValid && yDifIsValid;
    }

    public void updateCell(Cell cell, Integer newLevel) {
        if (!newCells.containsKey(cell)) {
            newCells.put(cell, newLevel);
            return;
        }

        newCells.put(
            cell,
            newCells.get(cell) + newLevel
        );
    }
}
