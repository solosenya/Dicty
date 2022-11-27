package ru.msu.physfac.biophys.g403.solonets.dicty.grid.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import ru.msu.physfac.biophys.g403.solonets.dicty.cells.model.Cell;
import ru.msu.physfac.biophys.g403.solonets.dicty.grid.view.Image;

import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor
public class Lattice {
    private int width;
    private int length;

    @Autowired
    Image image;

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
            cells.add(cell);
        }
    }
        return image.createImage(width, length);
    }

//    public void populate(int population) {
//
//    }
}
