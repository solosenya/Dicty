package ru.msu.physfac.biophys.g403.solonets.dicty.grid.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import ru.msu.physfac.biophys.g403.solonets.dicty.cells.amoebas.model.Amoebae;
import ru.msu.physfac.biophys.g403.solonets.dicty.cells.amoebas.repository.AmoebaeRepository;
import ru.msu.physfac.biophys.g403.solonets.dicty.cells.model.Cell;
import ru.msu.physfac.biophys.g403.solonets.dicty.cells.repository.CellRepository;
import ru.msu.physfac.biophys.g403.solonets.dicty.grid.view.Image;

import java.util.*;

import static ru.msu.physfac.biophys.g403.solonets.dicty.cells.amoebas.model.Amoebae.State.*;

@Getter
@NoArgsConstructor
public class Lattice {
    private final Random random = new Random();

    private final int RESTING_TIME = 10;

    private int width;
    private int length;

    @Autowired
    Image image;

    @Autowired
    AmoebaeRepository amoebaeRepository;

    @Autowired
    CellRepository cellRepository;

    @Setter
    private int population;
    private List<Cell> cellsList;

    public void setSize(int size) {
        this.width = size;
        this.length = size;
        cellsList = new ArrayList<>();
    }

    public byte[] createImage() {
        for (int i = 0; i < width; i++) {
        for (int j = 0; j < length; j++) {
            Cell cell = new Cell();
            cell.setX(i);
            cell.setY(j);
            cell.setCampLevel(0);
            cellRepository.save(cell);
        }
    }
        return image.createImage(width, length);
    }

    public void populate(int population) {
        int amountOfAmoebas = population * width * length / 100;
        int possibleAmountOfAmoebas = 4 * width * length;

        Set<Integer> indexes = new HashSet<>();
        while (indexes.size() < amountOfAmoebas) {
            indexes.add(
                random.nextInt(possibleAmountOfAmoebas)
            );
        }

        for (Integer cellIndex: indexes) {
            int position = cellIndex % 4;
            cellIndex -= position;
            cellIndex /= 4;

            int x = cellIndex % width;
            cellIndex -= x;
            cellIndex /= width;

            int y = cellIndex;

            Cell cell = cellRepository.findCellByXAndY(x, y);
            Optional<Amoebae> amoebaeOpt = amoebaeRepository.findByCellIdAndPosition(
                cell.getId(),
                position
            );
            if (amoebaeOpt.isEmpty()) {
                Amoebae amoebae = new Amoebae();
                amoebae.setCellId(cell.getId());
                amoebae.setPosition(position);
                amoebae.setState(Amoebae.State.READY);
                amoebae.setTime(0);
                amoebaeRepository.save(amoebae);
                image.dispose(amoebae, READY);
            }
        }
    }

    public void putCamp(int deviation) {
        List<Cell> cells = cellRepository.findAll();

        for (Cell cell : cells) {
            double level = Math.abs(
                random.nextGaussian(0, deviation)
            );

            if (level > 255) {
                level = 0;
            }

            Integer levelInt = (int) level;
            Integer cellId = cell.getId();
            cellRepository.setLevel(levelInt, cellId);
            image.putCampToCell(cell, levelInt);
        }
    }

    public void move(int threshold) throws Exception {
        List<Cell> cells = cellRepository.findAll();

        List<Amoebae> amoebas = amoebaeRepository.findAll();

        List<Amoebae> restingAmoebas = amoebaeRepository.findAllByState(RESTING)
            .orElse(new ArrayList<>());

        List<Amoebae> excitedAmoebas = amoebaeRepository.findAllByState(EXCITED)
            .orElse(new ArrayList<>());

        excitedToResting(excitedAmoebas, threshold, amoebas, cells);

        restingToReady(restingAmoebas);

        amoebas.removeAll(restingAmoebas);
        amoebas.removeAll(excitedAmoebas);

        for (Amoebae amoebae : amoebas) {
            Integer amoebaeId = amoebae.getId();

            Integer cellId = amoebae.getCellId();
            Optional<Cell> cellOpt = cellRepository.findById(cellId);

            if (cellOpt.isEmpty()) continue;

            Cell cell = cellOpt.get();
            List<Cell> neighbours = findNeighbours(cell, cells);

            List<Cell> neighboursCopy = new ArrayList<>(List.copyOf(neighbours));
            neighboursCopy.add(cell);

            Optional<Cell> targetCellOpt = getCellWithHighestLevel(neighboursCopy, threshold);
            if (targetCellOpt.isEmpty()) continue;
            Cell targetCell = targetCellOpt.get();

            if (targetCell.equals(cell)) {
                readyToResting(
                    targetCell,
                    threshold,
                    amoebaeId,
                    cell,
                    amoebae,
                    neighbours
                );
            } else readyToExcited(
                amoebae,
                amoebaeId,
                neighbours,
                threshold
            );
        }

        degradeCamp(cells);
    }

    private void degradeCamp(List<Cell> cells) {
        for (Cell cell: cells) {
            int newLevel = cell.getCampLevel() - 1;
            if (newLevel < 0) {
                newLevel = 0;
            }

            updateCellLevel(cell, newLevel);
        }
    }

    private void readyToResting(
        Cell targetCell,
        int threshold,
        Integer amoebaeId,
        Cell cell,
        Amoebae amoebae,
        List<Cell> neighbours
    ) {
        Integer level = targetCell.getCampLevel();
        if (level >= threshold) {
            int newLevel = level - threshold;
            if (newLevel < 0) {
                newLevel = 0;
            }

            amoebaeRepository.setState(RESTING, amoebaeId);
            image.dispose(amoebae, RESTING);

            updateNeighboursLevel(neighbours, threshold);
            updateCellLevel(cell, newLevel);
        }
    }

    private void updateNeighboursLevel(List<Cell> neighbours, int threshold) {
        for (Cell neighbour : neighbours) {
            Integer newNeighbourLevel = threshold / 2 + neighbour.getCampLevel();
            Integer neighbourId = neighbour.getId();
            cellRepository.setLevel(newNeighbourLevel, neighbourId);
            image.putCampToCell(neighbour, newNeighbourLevel);
        }
    }

    private void readyToExcited(
        Amoebae amoebae,
        Integer amoebaeId,
        List<Cell> neighbours,
        int threshold
    ) {
        amoebaeRepository.setState(EXCITED, amoebaeId);
        image.dispose(amoebae, EXCITED);

        updateNeighboursLevel(neighbours, threshold);
    }

    private void excitedToResting(
        List<Amoebae> excitedAmoebas,
        Integer threshold,
        List<Amoebae> amoebas,
        List<Cell> cells
    ) throws Exception {
        for (Amoebae amoebae : excitedAmoebas) {
            Integer cellId = amoebae.getCellId();
            Integer amoebaeId = amoebae.getId();
            Optional<Cell> cellOpt = cellRepository.findById(cellId);

            if (cellOpt.isEmpty()) continue;
            Cell cell = cellOpt.get();

            List<Cell> neighbours = findNeighbours(cell, cells);
            Optional<Cell> targetCellOpt = getCellWithHighestLevel(neighbours, threshold);
            if (targetCellOpt.isEmpty()) continue;
            Cell targetCell = targetCellOpt.get();

            Integer targetCellId = targetCell.getId();
            Optional<Integer> positionOpt = getPosition(targetCellId, amoebas);

            if (positionOpt.isEmpty()) continue;

            int position = positionOpt.get();
            int lastPosition = amoebae.getPosition();

            amoebae.setState(RESTING);
            amoebae.setTime(0);
            amoebae.setPosition(position);
            amoebae.setCellId(targetCellId);

            amoebaeRepository.updateAmoebae(
                RESTING,
                0,
                position,
                targetCellId,
                amoebaeId
            );
            image.replaceAmoebae(amoebae, cellId, lastPosition);

            int newLevel = cell.getCampLevel() - threshold;
            if (newLevel < 0) {
                newLevel = 0;
            }

            updateCellLevel(cell, newLevel);
        }
    }

    private void updateCellLevel(Cell cell, Integer newLevel) {
        Integer cellId = cell.getId();
        cellRepository.setLevel(newLevel, cellId);
        image.putCampToCell(cell, newLevel);
    }

    private Optional<Integer> getPosition(Integer cellId, List<Amoebae> amoebas) {
        List<Amoebae> foundAmoebas = new ArrayList<>();
        List<Integer> positions = new ArrayList<>(List.of(1, 2, 3, 4));
        for (Amoebae amoebae: amoebas) {
            if (amoebae.getCellId().equals(cellId)) {
                foundAmoebas.add(amoebae);
                Integer position = amoebae.getPosition();
                positions.remove(position);
            }
        }

        if (foundAmoebas.size() > 3) return Optional.empty();

        int positionsSize = positions.size();
        int position = random.nextInt(0, positionsSize);

        return Optional.of(position);
    }

    private Optional<Cell> getCellWithHighestLevel(List<Cell> cells, int threshold) throws Exception {
        Optional<Cell> highestLevelCellOpt = cells
            .stream()
                .filter(cell -> cell.getCampLevel() >= threshold)
            .reduce((a, b) ->
                b.getCampLevel() > a.getCampLevel() ? b : a
            );

        if (highestLevelCellOpt.isEmpty()) return highestLevelCellOpt;

        Cell highestLevelCell = highestLevelCellOpt.get();

        Integer level = highestLevelCell.getCampLevel();
        boolean levelIsHigherThenThreshold = level >= threshold;

        if (!levelIsHigherThenThreshold) {
            return Optional.empty();
        }

        return Optional.of(highestLevelCell);
    }

    private void restingToReady(List<Amoebae> restingAmoebas) {
        for (Amoebae amoebae : restingAmoebas) {
            int time = amoebae.getTime();
            Integer amoebaeId = amoebae.getId();

            if (time >= RESTING_TIME) {
                amoebaeRepository.setState(READY, amoebaeId);
                amoebaeRepository.setTime(0, amoebaeId);
                image.dispose(amoebae, READY);
                continue;
            }

            time = time + 1;
            amoebaeRepository.setTime(time, amoebaeId);
        }
    }

    private List<Cell> findNeighbours(Cell targetCell, List<Cell> cells) {
        List<Cell> neighbours = new ArrayList<>();

        for (Cell cell : cells) {
            if (neighbours.size() == 8) break;
            if (cell.equals(targetCell)) {
                continue;
            }

            int xDif = cell.getX() - targetCell.getX();
            int yDif = cell.getY() - targetCell.getY();

            boolean xDifIsValid = xDif >= -1 && xDif <= 1;
            boolean yDifIsValid = yDif >= -1 && yDif <= 1;
            if (xDifIsValid && yDifIsValid) {
                neighbours.add(cell);
            }
        }

        return neighbours;
    }
}
