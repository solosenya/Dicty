package ru.msu.physfac.biophys.g403.solonets.dicty.representation.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.msu.physfac.biophys.g403.solonets.dicty.representation.model.LatticeParams;
import ru.msu.physfac.biophys.g403.solonets.dicty.representation.view.ImageCreator;

@Slf4j
@RestController
public class GridController {

    @Autowired
    ImageCreator imageCreator;

    @GetMapping("/createLattice")
    public ResponseEntity<byte[]> createGrid(int size) {
        LatticeParams latticeParams = new LatticeParams();
        latticeParams.setParams(size);
        imageCreator.setLatticeParams(latticeParams);

        imageCreator.createImage();

        log.info("The lattice has been created!");

        return ResponseEntity.status(HttpStatus.OK)
                .header(HttpHeaders.CONTENT_DISPOSITION, "C:\\" +
                        "Users\\" +
                        "solos\\" +
                        "IdeaProjects\\" +
                        "Dicty\\" +
                        "src\\" +
                        "main\\" +
                        "resources\\" +
                        "representation\\" +
                        "representation.png")
                .contentType(MediaType.IMAGE_PNG)
                .body(imageCreator.getImageInByte());
    }
}
