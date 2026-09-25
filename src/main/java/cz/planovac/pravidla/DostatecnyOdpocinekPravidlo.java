package cz.planovac.pravidla;

import cz.planovac.model.Pracovnik;
import cz.planovac.model.TypSmeny;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Map;

public class DostatecnyOdpocinekPravidlo implements Pravidlo {

    private final int minimalniOdpocinekHodin;

    public DostatecnyOdpocinekPravidlo(int minimalniOdpocinekHodin) {
        this.minimalniOdpocinekHodin = minimalniOdpocinekHodin;
    }

    public DostatecnyOdpocinekPravidlo() {
        this(11);
    }


    @Override
    public boolean jeValidni(Pracovnik pracovnik, LocalDate datum, TypSmeny typ, Map<LocalDate, Map<Integer, TypSmeny>> historie) {
        LocalDate vcera = datum.minusDays(1);
        Map<Integer, TypSmeny> smenyVcera = historie.get(vcera);

        if (smenyVcera == null || !smenyVcera.containsKey(pracovnik.getId())) {
            return true;
        }

        TypSmeny predchoziSmena = smenyVcera.get(pracovnik.getId());

        LocalDateTime konecPredchoziSmeny = LocalDateTime.of(vcera, predchoziSmena.getKonec());

        if (predchoziSmena.getKonec().isBefore(predchoziSmena.getZacatek())) {
            konecPredchoziSmeny = konecPredchoziSmeny.plusDays(1);
        }

        LocalDateTime zacatekAktualniSmeny = LocalDateTime.of(datum, typ.getZacatek());

        long odpocinekVMinitach = ChronoUnit.MINUTES.between(konecPredchoziSmeny, zacatekAktualniSmeny);
        long odpocinekVHodinach = odpocinekVMinitach / 60;

        return odpocinekVHodinach >= minimalniOdpocinekHodin;
    }
}