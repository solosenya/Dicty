package ru.msu.physfac.biophys.g403.solonets.dicty.representation.model;

import lombok.Getter;
import org.springframework.stereotype.Component;

@Component
@Getter
public class LatticeParams {
    private int width;
    private int length;

    public LatticeParams() {
    }

    public void setParams(int size) {
        this.width = size;
        this.length = size;
    }
}
