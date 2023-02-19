package ru.msu.physfac.biophys.g403.solonets.dicty.grid.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import ru.msu.physfac.biophys.g403.solonets.dicty.cells.amoebas.model.Amoebae;
import ru.msu.physfac.biophys.g403.solonets.dicty.cells.amoebas.repository.AmoebaeRepository;
import ru.msu.physfac.biophys.g403.solonets.dicty.cells.model.Cell;
import ru.msu.physfac.biophys.g403.solonets.dicty.cells.repository.CellRepository;
import ru.msu.physfac.biophys.g403.solonets.dicty.grid.view.Image;

import java.util.*;

@Getter
@NoArgsConstructor
public class Lattice {
    private final Random random = new Random();

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
                amoebaeRepository.save(amoebae);
                image.dispose(amoebae);
            }
        }
    }
}
