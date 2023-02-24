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

import static ru.msu.physfac.biophys.g403.solonets.dicty.cells.amoebas.model.Amoebae.State.READY;
import static ru.msu.physfac.biophys.g403.solonets.dicty.cells.amoebas.model.Amoebae.State.RESTING;

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
    private List<Cell> cells;

    public void setSize(int size) {
        this.width = size;
        this.length = size;
        cells = new ArrayList<>();
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

    public void move(int threshold) {
        List<Amoebae> amoebas = amoebaeRepository.findAll();

        List<Amoebae> restingAmoebas = amoebaeRepository.findAllByState(RESTING)
            .orElse(new ArrayList<>());

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

        amoebas.removeAll(restingAmoebas);
        List<Cell> cells = cellRepository.findAll();

        for (Amoebae amoebae : amoebas) {
            Integer amoebaeId = amoebae.getId();

            Integer cellId = amoebae.getCellId();
            Optional<Cell> cellOpt = cellRepository.findById(cellId);

            if (cellOpt.isEmpty()) {
                continue;
            }

            Cell cell = cellOpt.get();
            List<Cell> neighbours = findNeighbours(cell, cells);


            List<Cell> neighboursCopy = new ArrayList<>(List.copyOf(neighbours));
            neighboursCopy.add(cell);

            Cell targetCell = neighboursCopy
                .stream()
                .reduce(
                    (a, b) ->
                        b.getCampLevel() > a.getCampLevel() &&
                        a.getCampLevel() >= threshold ? b : a
                ).get();

            if (targetCell.equals(cell)) {
                Integer level = targetCell.getCampLevel();
                if (level >= threshold) {
                    Integer newLevel = level - threshold;
                    cellRepository.setLevel(newLevel, cellId);
                    amoebaeRepository.setState(RESTING, amoebaeId);
                    image.putCampToCell(cell, newLevel);
                    image.dispose(amoebae, RESTING);

                    for (Cell neighbour : neighbours) {
                        Integer newNeighbourLevel = threshold / 6 + neighbour.getCampLevel();
                        Integer neighbourId = neighbour.getId();
                        cellRepository.setLevel(newNeighbourLevel, neighbourId);
                        image.putCampToCell(neighbour, newNeighbourLevel);
                    }
                }
            }
        }
    }

    private List<Cell> findNeighbours(Cell targetCell, List<Cell> cells) {
        List<Cell> neighbours = new ArrayList<>();

        for (Cell cell : cells) {
            if (neighbours.size() == 8) break;

            if (
                cell.getX() - targetCell.getX() == 1 &&
                    cell.getY() - targetCell.getY() == 1
            ) {
                neighbours.add(cell);
                continue;
            }

            if (
                cell.getX() +
                    cell.getY() -
                    targetCell.getX() -
                    targetCell.getY() == 1
            ) {
                neighbours.add(cell);
            }
        }

        return neighbours;
    }
}
