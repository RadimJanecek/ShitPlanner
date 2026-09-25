package cz.planovac.pravidla;

import cz.planovac.model.Pracovnik;
import cz.planovac.model.TypSmeny;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public class NepreferovanyKolegaPravidlo implements Pravidlo {
    private final List<int[]> zakazanePary;

    public NepreferovanyKolegaPravidlo(List<int[]> zakazanePary) {
        this.zakazanePary = zakazanePary;
    }

    @Override
    public boolean jeValidni(Pracovnik pracovnik, LocalDate datum, TypSmeny typ, Map<LocalDate, Map<Integer, TypSmeny>> historie) {
        Map<Integer, TypSmeny> smenyDne = historie.get(datum);
        if (smenyDne == null) {
            return true;
        }

        for (Map.Entry<Integer, TypSmeny> entry : smenyDne.entrySet()) {
            int kolegovaId = entry.getKey();
            TypSmeny kolegovaSmena = entry.getValue();

            if (kolegovaId == pracovnik.getId()) {
                continue;
            }
            if (kolegovaSmena == typ) {
                for (int[] zakazanyPar : zakazanePary) {
                    if ((zakazanyPar[0] == pracovnik.getId() && zakazanyPar[1] == kolegovaId) ||
                        (zakazanyPar[1] == pracovnik.getId() && zakazanyPar[0] == kolegovaId)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }
}