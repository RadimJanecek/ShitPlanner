package cz.planovac.pravidla;

import cz.planovac.model.Pracovnik;
import cz.planovac.model.TypSmeny;
import java.time.LocalDate;
import java.util.Map;

public interface Pravidlo {
    boolean jeValidni (Pracovnik pracovnik, LocalDate datum, TypSmeny typ, Map<LocalDate, Map<Integer, TypSmeny>> historie);
}