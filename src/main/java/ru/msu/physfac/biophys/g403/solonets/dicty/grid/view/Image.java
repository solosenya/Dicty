package ru.msu.physfac.biophys.g403.solonets.dicty.grid.view;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import ru.msu.physfac.biophys.g403.solonets.dicty.cells.repository.CellRepository;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

@Slf4j
@Setter
@Getter
public class Image {
    BufferedImage image;
    byte[] imageInByte;

    @Autowired
    CellRepository cellRepository;

    private final String pathToRepresentation;

    public Image(String pathToRepresentation) {
        this.pathToRepresentation = pathToRepresentation;
    }

    public byte[] createImage(int width, int length) {
        File file = new File("C:\\" +
                "Users\\" +
                "solos\\" +
                "IdeaProjects\\" +
                "Dicty\\" +
                "src\\" +
                "main\\" +
                "resources\\" +
                "representation\\" +
                "representation.png");

        int realWidth = width * 3;
        int realLength = length * 3;

        BufferedImage image = new BufferedImage(realWidth, realLength, BufferedImage.TYPE_INT_RGB);
        Graphics2D lattice = image.createGraphics();

        lattice.setColor(new Color(255, 255, 255));
        lattice.fillRect(0, 0, realWidth, realLength);
        lattice.dispose();

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        try {
            ImageIO.write(image, "png", file);
            ImageIO.write(image, "png", byteArrayOutputStream);
        } catch (IOException e) {
            log.error("Could not create the lattice");
        }

        this.image = image;
        this.imageInByte = byteArrayOutputStream.toByteArray();

        return this.imageInByte;
    }

    public void populateImage(int population) {

    }
}
