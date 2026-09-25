package cz.planovac.pravidla;

import cz.planovac.model.Pracovnik;
import cz.planovac.model.TypSmeny;
import java.time.LocalDate;
import java.util.Map;

public class DveNocniPoSobePravidlo implements Pravidlo {

    @Override
    public boolean jeValidni(Pracovnik pracovnik, LocalDate datum, TypSmeny typ, Map<LocalDate, Map<Integer, TypSmeny>> historie) {
        // Kontrolujeme to podle názvu (ignorujeme velikost písmen)
        if (!typ.getNazev().equalsIgnoreCase("Noční")) {
            return true;
        }

        LocalDate vcera = datum.minusDays(1);
        Map<Integer, TypSmeny> smenyVcera = historie.get(vcera);
        if (smenyVcera == null) {
            return true; // Včera se nepracovalo
        }
        
        TypSmeny smenaVcera = smenyVcera.get(pracovnik.getId());
        // Pokud měl včera noční a dnes má mít noční, vyhodíme false (nevalidní)
        return smenaVcera == null || !smenaVcera.getNazev().equalsIgnoreCase("Noční");
    }
}