package ru.msu.physfac.biophys.g403.solonets.dicty.grid.view;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import ru.msu.physfac.biophys.g403.solonets.dicty.cells.amoebas.model.Amoebae;
import ru.msu.physfac.biophys.g403.solonets.dicty.cells.model.Cell;
import ru.msu.physfac.biophys.g403.solonets.dicty.cells.repository.CellRepository;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Optional;

@Slf4j
@Setter
@Getter
public class Image {
    BufferedImage image;
    byte[] imageInByte;
    Graphics2D lattice;
    private final File FILE = new File(
        "C:\\" +
        "Users\\" +
        "solos\\" +
        "IdeaProjects\\" +
        "Dicty\\" +
        "src\\" +
        "main\\" +
        "resources\\" +
        "representation\\" +
        "representation.png"
    );

    @Autowired
    CellRepository cellRepository;

    private final String pathToRepresentation;

    public Image(String pathToRepresentation) {
        this.pathToRepresentation = pathToRepresentation;
    }

    public byte[] createImage(int width, int length) {
        int realWidth = width * 3;
        int realLength = length * 3;

        image = new BufferedImage(realWidth, realLength, BufferedImage.TYPE_INT_RGB);
        lattice = image.createGraphics();

        lattice.setColor(new Color(255, 255, 255));
        lattice.fillRect(0, 0, realWidth, realLength);
        lattice.dispose();

        return getTmpImage();
    }

    public void dispose(Amoebae amoebae, Amoebae.State state) {
        Optional<Cell> cellOpt = cellRepository.findById(amoebae.getCellId());
        if (cellOpt.isEmpty()) {
            log.error("К амебе не привязана клетка!");
            throw new IllegalArgumentException("К амебе не привязана клетка!");
        }
        Cell cell = cellOpt.get();
        int x = cell.getX();
        int y = cell.getY();
        int position = amoebae.getPosition();


        CellOffsetService cellOffsetService = new CellOffsetService(position, state);
        int realX = 3 * x
            + cellOffsetService.xOffset;

        int realY = 3 * y
            + cellOffsetService.yOffset;

        lattice = image.createGraphics();
        lattice.setColor(cellOffsetService.color);
        if (cellOffsetService.vertical) {
            lattice.fillRect(realX, realY, 1, 2);
        } else {
            lattice.fillRect(realX, realY, 2, 1);
        }
        lattice.dispose();
    }

    @Getter
    class CellOffsetService {
        private final int xOffset;
        private final int yOffset;
        final boolean vertical;
        final Color color;

        CellOffsetService(int position, Amoebae.State state) {
            switch (position) {
                case 0 -> {
                    xOffset = 0;
                    yOffset = 0;
                    vertical = false;
                }
                case 1 -> {
                    xOffset = 2;
                    yOffset = 0;
                    vertical = true;
                }
                case 2 -> {
                    xOffset = 1;
                    yOffset = 2;
                    vertical = false;
                }
                case 3 -> {
                    xOffset = 0;
                    yOffset = 1;
                    vertical = true;
                }
                default -> {
                    log.error("Номер позиции не может быть больше 3!");
                    throw new IllegalArgumentException("Номер позиции не может быть больше 3!");
                }
            }
            switch (state) {
                case READY -> color = Color.BLUE;
                case RESTING -> color = Color.YELLOW;
                case EXCITED -> color = Color.GREEN;
                case EMPTY -> color = Color.WHITE;
                default -> {
                    log.error("Других состояний быть не может!");
                    throw new IllegalArgumentException("Других состояний быть не может!");
                }
            }
        }
    }

    public void replaceAmoebae(Amoebae amoebae, int lastCellId, int lastPosition) {
        Amoebae lastPlace = new Amoebae();
        lastPlace.setCellId(lastCellId);
        lastPlace.setPosition(lastPosition);

        dispose(lastPlace, Amoebae.State.EMPTY);
        Amoebae.State newState = amoebae.getState();
        dispose(amoebae, newState);
    }

    public void putCampToCell(Cell cell, Integer level) {
        int realX = 3 * cell.getX() + 1;
        int realY = 3 * cell.getY() + 1;

        int greyness = 255 - level;

        Color grey = new Color(greyness, greyness, greyness);
        lattice = image.createGraphics();
        lattice.setColor(grey);

        lattice.fillRect(realX, realY, 1, 1);
        lattice.dispose();
    }

    public byte[] getTmpImage() {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        try {
            ImageIO.write(image, "png", FILE);
            ImageIO.write(image, "png", byteArrayOutputStream);
        } catch (IOException e) {
            log.error("Could not create the lattice");
        }

        this.imageInByte = byteArrayOutputStream.toByteArray();
        return this.imageInByte;
    }
}
