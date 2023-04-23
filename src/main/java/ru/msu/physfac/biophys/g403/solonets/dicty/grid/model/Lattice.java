package ru.msu.physfac.biophys.g403.solonets.dicty.grid.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import ru.msu.physfac.biophys.g403.solonets.dicty.cells.amoebas.model.Amoebae;
import ru.msu.physfac.biophys.g403.solonets.dicty.cells.amoebas.model.SystemInfoDto;
import ru.msu.physfac.biophys.g403.solonets.dicty.cells.amoebas.repository.AmoebaeRepository;
import ru.msu.physfac.biophys.g403.solonets.dicty.cells.model.Cell;
import ru.msu.physfac.biophys.g403.solonets.dicty.cells.repository.CellRepository;
import ru.msu.physfac.biophys.g403.solonets.dicty.grid.view.Image;

import java.util.Optional;
import java.util.Random;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

import static ru.msu.physfac.biophys.g403.solonets.dicty.cells.amoebas.model.Amoebae.State.*;
import static ru.msu.physfac.biophys.g403.solonets.dicty.cells.amoebas.model.Amoebae.Destination.*;

@Getter
@NoArgsConstructor
public class Lattice {
    private final Random random = new Random();

    private final int RESTING_TIME = 13;
    private final int EXCITED_TIME = 8;
    private final int PACESETTER_TIME = 50;
    private final int READY_TIME = 1;

    private int width;
    private int length;

    @Autowired
    Image image;

    @Autowired
    AmoebaeRepository amoebaeRepository;

    @Autowired
    CellRepository cellRepository;

    @Autowired
    PopulationService populationService;

    @Setter
    private int population;

    public void setSize(int size) {
        this.width = size;
        this.length = size;
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

    public void move(int threshold) {
        List<Cell> cells = cellRepository.findAll();
        List<Amoebae> amoebas = amoebaeRepository.findAll();
        SystemInfoDto systemInfoDto = new SystemInfoDto(cells, amoebas);

//        degradeCamp(cells, systemInfoDto);
//        diffuseCamp(cells, threshold, systemInfoDto);
        setDuties(systemInfoDto, threshold);
        createPseudoBatch(systemInfoDto);
    }

    private void setDuties(SystemInfoDto systemInfoDto, int threshold) {
        setPace(systemInfoDto, threshold);
        excitedToResting(systemInfoDto, threshold);
        restingToReady(systemInfoDto);
        readyToDestination(systemInfoDto, threshold);
    }

    private void createPseudoBatch(SystemInfoDto systemInfoDto) {
        Map<Cell, Integer> newCells = systemInfoDto.getNewCells();

        for (Map.Entry<Cell, Integer> newCell : newCells.entrySet()) {
            Integer newLevel = newCell.getKey().getCampLevel() + newCell.getValue();
            cellRepository.setLevel(
                newLevel,
                newCell.getKey().getId()
            );
        }
    }

    private void setPace(
        SystemInfoDto systemInfoDto,
        int threshold
    ) {
        List<Amoebae> pacesetters = systemInfoDto.getPacesetters();
        for (Amoebae amoebae : pacesetters) {
            int time = amoebae.getTime();
            if (time != PACESETTER_TIME) {
                int newTime = ++time;
                int id = amoebae.getId();
                amoebaeRepository.setTime(newTime, id);
                continue;
            }
            amoebae.setTime(0);
            Integer cellId = amoebae.getCellId();
            List<Cell> neighbours = systemInfoDto.getNeighboursByCentralId(cellId);
            List<Cell> farNeighbours = systemInfoDto.getFarNeighboursByCentralId(cellId);

            neighbours.forEach(n -> updateCellLevel(n, threshold * 6, systemInfoDto));

            farNeighbours.forEach(fn -> updateCellLevel(fn, threshold * 3, systemInfoDto));
        }
    }

    private void excitedToResting(SystemInfoDto systemInfo, int threshold) {
        List<Amoebae> excitedAmoebas = systemInfo.getExcitedAmoebas();
        Map<Integer, List<Amoebae>> groupedAmoebasByCellId = systemInfo.getGroupedAmoebasByCellId();
        for (Amoebae amoebae : excitedAmoebas) {
            int time = amoebae.getTime();
            if (time < EXCITED_TIME) {
                int newTime = ++time;
                int id = amoebae.getId();
                amoebaeRepository.setTime(newTime, id);
                continue;
            }

            Integer cellId = amoebae.getCellId();
            Integer amoebaeId = amoebae.getId();
            Cell cell = systemInfo.findCellByAmoebae(amoebae);
            Amoebae.Destination dest = amoebae.getDestination();

            Optional<Cell> targetCellOpt = getCellWithHighestLevelForExcited(cell, systemInfo, dest);
            if (targetCellOpt.isEmpty()) continue;
            Cell targetCell = targetCellOpt.get();

            Integer targetCellId = targetCell.getId();
            Optional<Integer> positionOpt = getPosition(targetCellId, groupedAmoebasByCellId);

            if (positionOpt.isEmpty()) continue;
            int position = positionOpt.get();

            int lastPosition = amoebae.getPosition();

            amoebae.setState(RESTING);
            amoebae.setTime(0);
            amoebae.setPosition(position);
            amoebae.setCellId(targetCellId);
            groupedAmoebasByCellId.get(cellId).remove(amoebae);

            if (!groupedAmoebasByCellId.containsKey(targetCellId)) {
                groupedAmoebasByCellId.put(targetCellId, new ArrayList<>());
            }
            groupedAmoebasByCellId.get(targetCellId).add(amoebae);

            amoebaeRepository.updateAmoebae(
                RESTING,
                0,
                position,
                targetCellId,
                amoebaeId
            );
            image.replaceAmoebae(amoebae, cellId, lastPosition);

            updateCellLevel(cell, threshold, systemInfo);
        }
    }

    private void restingToReady(SystemInfoDto systemInfo) {
        List<Amoebae> restingAmoebas = systemInfo.getRestingAmoebas();
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

    private void readyToDestination(SystemInfoDto systemInfoDto, int threshold) {
        List<Amoebae> readies = systemInfoDto.getReadies();
        for (Amoebae amoebae : readies) {
            int time = amoebae.getTime();
            boolean tooEarly = time < READY_TIME;
            if (tooEarly){
                int newTime = ++time;
                int id = amoebae.getId();
                amoebaeRepository.setTime(newTime, id);
                continue;
            }

            Cell cell = systemInfoDto.findCellByAmoebae(amoebae);

            Optional<Cell> targetCellOpt = getCellWithHighestLevel(
                cell,
                threshold,
                systemInfoDto
            );
            if (targetCellOpt.isEmpty()) continue;
            Cell targetCell = targetCellOpt.get();

            if (targetCell.equals(cell)) {
                readyToResting(threshold, cell, amoebae, systemInfoDto);
            } else {
                Amoebae.Destination dest = calcDestination(cell, targetCell);
                readyToExcited(amoebae, threshold, cell, systemInfoDto, dest);
            }
        }
    }

    private void readyToResting(
        int threshold,
        Cell cell,
        Amoebae amoebae,
        SystemInfoDto systemInfoDto
    ) {
        Integer amoebaeId = amoebae.getId();
        Integer cellId = cell.getId();
        List<Cell> neighbours = systemInfoDto.getNeighboursByCentralId(cellId);
        List<Cell> farNeighbours = systemInfoDto.getFarNeighboursByCentralId(cellId);

        Integer level = cell.getCampLevel();
        if (level >= threshold) {
            amoebaeRepository.setState(RESTING, amoebaeId);
            image.dispose(amoebae, RESTING);

            updateNeighboursLevel(neighbours, threshold, farNeighbours, systemInfoDto);
            updateCellLevel(cell, threshold, systemInfoDto);
        }
    }

    private void readyToExcited(
        Amoebae amoebae,
        int threshold,
        Cell cell,
        SystemInfoDto systemInfoDto,
        Amoebae.Destination dest
    ) {
        Integer cellId = cell.getId();
        Integer amoebaeId = amoebae.getId();
        List<Cell> neighbours = systemInfoDto.getNeighboursByCentralId(cellId);
        List<Cell> farNeighbours = systemInfoDto.getFarNeighboursByCentralId(cellId);

        amoebaeRepository.setStateAndDestination(EXCITED, dest, amoebaeId);
        image.dispose(amoebae, EXCITED);

        updateNeighboursLevel(neighbours, threshold, farNeighbours, systemInfoDto);
    }

    private Amoebae.Destination calcDestination(Cell cell, Cell targetCell) {
        int xDif = targetCell.getX() - cell.getX();
        int yDif = targetCell.getY() - cell.getY();

        if (xDif == 1 && yDif == 1) {
            return random.nextInt(2) == 1 ? TOP : RIGHT;
        }
        if (xDif == 1 && yDif == -1) {
            return random.nextInt(2) == 1 ? BOTTOM : RIGHT;
        }
        if (xDif == -1 && yDif == 1) {
            return random.nextInt(2) == 1 ? TOP : LEFT;
        }
        if (xDif == -1 && yDif == -1) {
            return random.nextInt(2) == 1 ? BOTTOM : LEFT;
        }
        if (xDif == 1) return RIGHT;
        if (xDif == -1) return LEFT;
        if (yDif == 1) return TOP;
        if (yDif == -1) return BOTTOM;

        return null;
    }

    private void degradeCamp(List<Cell> cells, SystemInfoDto systemInfo) {
        for (Cell cell: cells) {
            boolean doDegrade = random.nextInt(1, 101) > 0;

            if (doDegrade) {
                updateCellLevel(cell, -3, systemInfo);
            }
        }
    }

    private void updateNeighboursLevel(
        List<Cell> neighbours,
        int threshold,
        List<Cell> farNeighbours,
        SystemInfoDto systemInfoDto
    ) {
        for (Cell neighbour : neighbours) {
            updateCellLevel(neighbour, threshold, systemInfoDto);
        }

        for (Cell farNeighbour: farNeighbours) {
            updateCellLevel(farNeighbour, threshold / 2, systemInfoDto);
        }
    }

    private void updateCellLevel(Cell cell, Integer addition, SystemInfoDto systemInfo) {
        systemInfo.updateCell(cell, addition);
        image.putCampToCell(cell, cell.getCampLevel() + addition);
    }

    private Optional<Integer> getPosition(Integer cellId, Map<Integer, List<Amoebae>> groupedAmoebasByCellId) {
        List<Integer> positions = new ArrayList<>(List.of(0, 1, 2, 3));

        List<Amoebae> amoebas = groupedAmoebasByCellId.get(cellId);
        if (amoebas == null || amoebas.size() < 4) {
            if (amoebas != null) {
                amoebas.forEach(a -> positions.remove(a.getPosition()));
            }
            return Optional.of(
                positions.get(
                    random.nextInt(
                        0,
                        positions.size()
                    )));
        }

        return Optional.empty();
    }

    private Optional<Cell> getCellWithHighestLevel(
        Cell central,
        int threshold,
        SystemInfoDto systemInfoDto
    ) {
        Integer centralId = central.getId();
        Optional<Cell> targetOpt = systemInfoDto.getNeighboursByCentralId(centralId)
            .stream()
            .filter(cell -> cell.getCampLevel() >= threshold)
            .reduce((a, b) -> b.getCampLevel() > a.getCampLevel() ?
                b : a
            );

        boolean noTarget = targetOpt.isEmpty();
        if (noTarget) return Optional.empty();

        Cell target = targetOpt.get();
        boolean shouldStay = central.getCampLevel() > target.getCampLevel();
        if (shouldStay) return Optional.of(central);

        return Optional.of(target);
    }

    private Optional<Cell> getCellWithHighestLevelForExcited(
        Cell central,
        SystemInfoDto systemInfoDto,
        Amoebae.Destination dest
    ) {
        Integer centralId = central.getId();
        Optional<Cell> targetOpt = systemInfoDto.getNeighboursByCentralIdWithDest(centralId, dest)
            .stream()
            .reduce((a, b) -> b.getCampLevel() > a.getCampLevel() ?
                b : a
            );

        boolean noTarget = targetOpt.isEmpty();
        if (noTarget) return Optional.empty();
//
        Cell target = targetOpt.get();
//        boolean shouldStay = central.getCampLevel() > target.getCampLevel();
//        if (shouldStay) return Optional.of(central);
//
        return Optional.of(target);
    }

    private Optional<Cell> getCellWithLowestLevel(List<Cell> cells) {
        Optional<Cell> lowestLevelCellOpt = cells
            .stream()
            .reduce((a, b) ->
                b.getCampLevel() > a.getCampLevel() ? a : b
            );

        if (lowestLevelCellOpt.isEmpty()) return lowestLevelCellOpt;
        Cell highestLevelCell = lowestLevelCellOpt.get();

        return Optional.of(highestLevelCell);
    }

    public void diffuseCamp(List<Cell> cells, int threshold, SystemInfoDto systemInfoDto) {
        List<Cell> highCells = cells
            .stream()
            .filter(c -> c.getCampLevel() > threshold)
            .limit(30)
            .toList();

        for (Cell cell : highCells) {
            Integer cellId = cell.getId();
            List<Cell> neighbours = systemInfoDto.getNeighboursByCentralId(cellId);

            Cell lowestCell = getCellWithLowestLevel(neighbours)
                .orElse(cell);

            if (lowestCell.equals(cell)) return;

            updateCellLevel(cell, - threshold / 30, systemInfoDto);
            updateCellLevel(lowestCell, + threshold / 30, systemInfoDto);
        }
    }
}
