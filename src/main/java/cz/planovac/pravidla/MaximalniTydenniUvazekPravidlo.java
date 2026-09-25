package cz.planovac.pravidla;

import cz.planovac.model.Pracovnik;
import cz.planovac.model.TypSmeny;
import java.time.LocalDate;
import java.util.Map;

public class MaximalniTydenniUvazekPravidlo implements Pravidlo {
    private final double standartniTydenniUvazek;

    public MaximalniTydenniUvazekPravidlo() {
        this.standartniTydenniUvazek = 40.0; // Standardní plný úvazek v ČR
    }

    @Override
    public boolean jeValidni(Pracovnik pracovnik, LocalDate datum, TypSmeny typ, Map<LocalDate, Map<Integer, TypSmeny>> historie) {
        double odpracovano = typ.getDelkaHodin();
        
        for (int i = 1; i < 7; i++) {
            LocalDate den = datum.minusDays(i);
            Map<Integer, TypSmeny> smenyDne = historie.get(den);
            if (smenyDne != null && smenyDne.containsKey(pracovnik.getId())) {
                odpracovano += smenyDne.get(pracovnik.getId()).getDelkaHodin();
            }
        }
        
        // NOVÉ: Limit se teď násobí úvazkem. Pro 0.5 úvazek bude limit 20 hodin.
        double limitProPracovnika = standartniTydenniUvazek * pracovnik.getUvazek();
        
        return odpracovano <= limitProPracovnika;
    }
}