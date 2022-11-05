package ru.msu.physfac.biophys.g403.solonets.dicty.representation.view;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.msu.physfac.biophys.g403.solonets.dicty.representation.model.LatticeParams;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

@Slf4j
@Setter
@Getter
@Component("lattice")
public class ImageCreator {
    private LatticeParams latticeParams;
    BufferedImage image;
    byte[] imageInByte;

    public void createImage() {

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

        int realWidth = this.latticeParams.getWidth() * 3;
        int realLength = this.latticeParams.getLength() * 3;

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
    }
}
