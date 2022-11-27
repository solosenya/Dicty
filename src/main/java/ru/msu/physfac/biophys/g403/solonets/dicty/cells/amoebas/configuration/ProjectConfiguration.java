package ru.msu.physfac.biophys.g403.solonets.dicty.cells.amoebas.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import ru.msu.physfac.biophys.g403.solonets.dicty.grid.model.Lattice;
import ru.msu.physfac.biophys.g403.solonets.dicty.grid.view.Image;

@Configuration
@ComponentScan("ru.msu.physfac.biophys.g403.solonets.dicty")
public class ProjectConfiguration {

    @Value("${path.to.representation}")
    private String pathToRepresentation;

    @Bean
    public Image image() {
        return new Image(pathToRepresentation);
    }

    @Bean
    public Lattice lattice() {
        return new Lattice();
    }
}
