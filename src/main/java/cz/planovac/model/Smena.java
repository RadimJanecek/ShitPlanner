package cz.planovac.model;

import java.io.Serializable;
import java.time.LocalDate;

public class Smena implements Serializable {
    private static final long serialVersionUID = 1L;
    private final LocalDate datum;
    private final TypSmeny typ;

    public Smena(LocalDate datum, TypSmeny typ) {
        this.datum = datum;
        this.typ = typ;
    }

    public LocalDate getDatum() {
        return datum;
    }

    public TypSmeny getTyp() {
        return typ;
    }
}