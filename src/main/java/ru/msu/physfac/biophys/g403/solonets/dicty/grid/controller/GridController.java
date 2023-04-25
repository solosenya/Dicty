package ru.msu.physfac.biophys.g403.solonets.dicty.grid.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.msu.physfac.biophys.g403.solonets.dicty.grid.model.Lattice;
import ru.msu.physfac.biophys.g403.solonets.dicty.grid.model.PopulationService;
import ru.msu.physfac.biophys.g403.solonets.dicty.grid.view.Image;

import javax.transaction.Transactional;

@Slf4j
@RestController("app/v1")
public class GridController {

    @Autowired
    private Lattice lattice;

    @Autowired
    private Image image;

    @Autowired
    private PopulationService populationService;

    private int size = 0;

    @GetMapping("/createLattice")
    public ResponseEntity<byte[]> createGrid(@RequestParam int size) {
        lattice.setSize(size);
        this.size = size;

        log.info("The lattice has been created!");

        return ResponseEntity
            .status(HttpStatus.OK)
            .header(
                HttpHeaders.CONTENT_DISPOSITION,
                image.getPathToRepresentation()
            )
            .contentType(MediaType.IMAGE_PNG)
            .body(
                lattice.createImage()
            );
    }

    @GetMapping("/populate")
    public ResponseEntity<byte[]> populateGrid(int population) {

        if (population > 100 || population < 0) {
            log.error("Population level MUST be an integer between 0 and 100!");
            throw new IllegalArgumentException("Population level MUST be an integer between 0 and 100!");
        }

        populationService.populate(population, size, size);

        return ResponseEntity
            .status(HttpStatus.OK)
            .header(
                HttpHeaders.CONTENT_DISPOSITION,
                image.getPathToRepresentation()
            )
            .contentType(MediaType.IMAGE_PNG)
            .body(
                lattice.getImage()
                    .getTmpImage()
            );
    }

    @Transactional
    @GetMapping("/putCAMP")
    public ResponseEntity<byte[]> putCamp(int deviation) {
        lattice.putCamp(deviation);

        return ResponseEntity
            .status(HttpStatus.OK)
            .header(
                HttpHeaders.CONTENT_DISPOSITION,
                image.getPathToRepresentation()
            )
            .contentType(MediaType.IMAGE_PNG)
            .body(
                lattice.getImage()
                    .getTmpImage()
            );
    }

    @Transactional
    @GetMapping("/move")
    public ResponseEntity<byte[]> move(int threshold) throws Exception {
        lattice.move(threshold);

        return ResponseEntity
            .status(HttpStatus.OK)
            .header(
                HttpHeaders.CONTENT_DISPOSITION,
                image.getPathToRepresentation()
            )
            .contentType(MediaType.IMAGE_PNG)
            .body(
                lattice.getImage()
                    .getTmpImage()
            );
    }
}
