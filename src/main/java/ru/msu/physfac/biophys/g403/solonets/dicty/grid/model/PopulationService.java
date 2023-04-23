package ru.msu.physfac.biophys.g403.solonets.dicty.grid.model;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.msu.physfac.biophys.g403.solonets.dicty.cells.amoebas.model.Amoebae;
import ru.msu.physfac.biophys.g403.solonets.dicty.cells.amoebas.repository.AmoebaeRepository;
import ru.msu.physfac.biophys.g403.solonets.dicty.cells.model.Cell;
import ru.msu.physfac.biophys.g403.solonets.dicty.cells.repository.CellRepository;
import ru.msu.physfac.biophys.g403.solonets.dicty.grid.view.Image;

import java.util.HashSet;
import java.util.Optional;
import java.util.Random;
import java.util.Set;

import static ru.msu.physfac.biophys.g403.solonets.dicty.cells.amoebas.model.Amoebae.State.PACESETTER;
import static ru.msu.physfac.biophys.g403.solonets.dicty.cells.amoebas.model.Amoebae.State.READY;

@Service
public class PopulationService {
    private final Random random = new Random();

    @Autowired
    private AmoebaeRepository amoebaeRepository;

    @Autowired
    private CellRepository cellRepository;

    @Autowired
    private Image image;

    private final int PACESETTER_TIME = 50;

    public void populate(int population, int width, int length) {
        int amountOfAmoebas = population * width * length / 100;
        int possibleAmountOfAmoebas = 4 * width * length;

        Set<Integer> indexes = new HashSet<>();
        while (indexes.size() < amountOfAmoebas) {
            indexes.add(
                random.nextInt(possibleAmountOfAmoebas)
            );
        }

        createReadies(indexes, width, length);
        createPacesetters(width, length);
    }

    private void createPacesetters(int width, int length) {
        Cell pacesetterCell = cellRepository.findCellByXAndY(width / 2, length / 2);
        for (int i = 0; i < 4; i++) {
            Amoebae amoebae = new Amoebae();
            amoebae.setCellId(pacesetterCell.getId());
            amoebae.setPosition(i);
            amoebae.setState(PACESETTER);
            amoebae.setTime(PACESETTER_TIME);
            amoebaeRepository.save(amoebae);
            image.dispose(amoebae, PACESETTER);
        }
    }

    private void  createReadies(Set<Integer> indexes, int width, int length) {
        for (Integer cellIndex: indexes) {
            int position = cellIndex % 4;
            cellIndex -= position;
            cellIndex /= 4;

            int x = cellIndex % width;
            cellIndex -= x;
            cellIndex /= width;

            int y = cellIndex;

            if (x == width / 2 && y == length / 2) {
                continue;
            }

            Cell cell = cellRepository.findCellByXAndY(x, y);
            Optional<Amoebae> amoebaeOpt = amoebaeRepository.findByCellIdAndPosition(
                cell.getId(),
                position
            );
            if (amoebaeOpt.isEmpty()) {
                createReadyAmoebae(
                    cell.getId(),
                    position
                );
            }
        }
    }

    private void createReadyAmoebae(Integer cellId, Integer position) {
        Amoebae amoebae = new Amoebae();
        amoebae.setCellId(cellId);
        amoebae.setPosition(position);
        amoebae.setState(READY);
        amoebae.setTime(0);
        amoebaeRepository.save(amoebae);
        image.dispose(amoebae, READY);
    }
}
