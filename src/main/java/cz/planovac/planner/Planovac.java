package cz.planovac.planner;

import cz.planovac.model.*;
import cz.planovac.pravidla.DostatecnyOdpocinekPravidlo;
import cz.planovac.pravidla.NepreferovanyKolegaPravidlo;
import cz.planovac.pravidla.Pravidlo;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class Planovac {
    public static Map<LocalDate, Map<Integer, TypSmeny>> naplanuj(
            List<Pracovnik> pracovnici,
            List<Nedostupnost> nedostupnosti,
            LocalDate od,
            LocalDate do_,
            List<Pravidlo> externiPravidla,
            List<TypSmeny> typySmen,
            List<int[]> zakazanePary,
            List<PozadavekNaObsazeniSmeny> pozadavkyNaObsazeni,
            Map<LocalDate, Map<Integer, TypSmeny>> dosavadniPlan,
            boolean brigadnikNesmiBytSam
    ) {
        Map<LocalDate, Map<Integer, TypSmeny>> plan = new TreeMap<>();
        if (dosavadniPlan != null) {
            for (Map.Entry<LocalDate, Map<Integer, TypSmeny>> entry : dosavadniPlan.entrySet()) {
                plan.put(entry.getKey(), new HashMap<>(entry.getValue()));
            }
        }

        for (LocalDate d = od; !d.isAfter(do_); d = d.plusDays(1)) {
            plan.remove(d);
        }

        List<Pravidlo> platnaPravidla = new ArrayList<>(externiPravidla);
        platnaPravidla.add(new NepreferovanyKolegaPravidlo(zakazanePary));
        platnaPravidla.add(new DostatecnyOdpocinekPravidlo());

        Random random = new Random();
        Map<Integer, Double> odpracovaneHodiny = new HashMap<>();
        for (Pracovnik p : pracovnici) {
            odpracovaneHodiny.put(p.getId(), 0.0);
        }

        // --- ZDE JE OPRAVA: Ochrana proti duplikátům z historických chyb JSONu ---
        Map<TypSmeny, Integer> pozadavkyMapa = pozadavkyNaObsazeni.stream()
                .collect(Collectors.toMap(
                        PozadavekNaObsazeniSmeny::getTypSmeny, 
                        PozadavekNaObsazeniSmeny::getPocetZamestnancu,
                        (staraHodnota, novaHodnota) -> Math.max(staraHodnota, novaHodnota) // Vždy vezme to nejvyšší číslo
                ));

        for (LocalDate datum = od; !datum.isAfter(do_); datum = datum.plusDays(1)) {
            final LocalDate aktualniDatum = datum; 
            
            Map<Integer, TypSmeny> smenyDne = new HashMap<>();
            Map<TypSmeny, Integer> aktualniObsazeniSmen = new HashMap<>();
            typySmen.forEach(typ -> aktualniObsazeniSmen.put(typ, 0));

            List<Pracovnik> pracovniciNaDen = new ArrayList<>(pracovnici);
            Collections.shuffle(pracovniciNaDen, random);
            pracovniciNaDen.sort(Comparator.comparingDouble(p -> odpracovaneHodiny.get(p.getId()) / Math.max(0.01, p.getUvazek())));

            Map<Integer, TypSmeny> finalSmenyDne = smenyDne;

            for (Pracovnik pracovnik : pracovniciNaDen) {
                boolean jeNedostupny = nedostupnosti.stream().anyMatch(n -> 
                    n.getPracovnikId() == pracovnik.getId() && !aktualniDatum.isBefore(n.getOdDatum()) && !aktualniDatum.isAfter(n.getDoDatum())
                );
                
                if (jeNedostupny) continue;

                List<TypSmeny> smenyNaPokus = new ArrayList<>(typySmen);
                Collections.shuffle(smenyNaPokus, random);

                for (TypSmeny typ : smenyNaPokus) {
                    int pocetPozadovany = pozadavkyMapa.getOrDefault(typ, 0);
                    int pocetAktualni = aktualniObsazeniSmen.getOrDefault(typ, 0);

                    if (pocetPozadovany == 0 || pocetAktualni >= pocetPozadovany) continue;

                    // Ochrana pro brigádníky
                    if (brigadnikNesmiBytSam && pracovnik.getUvazek() < 1.0) {
                        if (pocetPozadovany == 1) continue; 
                        
                        boolean jeTamPlnyUvazek = false;
                        for (Map.Entry<Integer, TypSmeny> entry : finalSmenyDne.entrySet()) {
                            if (entry.getValue().equals(typ)) {
                                Pracovnik obsazeny = pracovnici.stream().filter(pr -> pr.getId() == entry.getKey()).findFirst().orElse(null);
                                if (obsazeny != null && obsazeny.getUvazek() >= 1.0) {
                                    jeTamPlnyUvazek = true;
                                    break;
                                }
                            }
                        }
                        if (pocetAktualni == pocetPozadovany - 1 && !jeTamPlnyUvazek) continue;
                    }

                    boolean ok = true;
                    for (Pravidlo r : platnaPravidla) {
                        Map<LocalDate, Map<Integer, TypSmeny>> aktualniHistorie = new TreeMap<>(plan);
                        aktualniHistorie.put(aktualniDatum, new HashMap<>(finalSmenyDne));
                        aktualniHistorie.get(aktualniDatum).put(pracovnik.getId(), typ);

                        if (!r.jeValidni(pracovnik, aktualniDatum, typ, aktualniHistorie)) {
                            ok = false;
                            break;
                        }
                    }

                    if (ok) {
                        finalSmenyDne.put(pracovnik.getId(), typ);
                        aktualniObsazeniSmen.put(typ, aktualniObsazeniSmen.get(typ) + 1);
                        odpracovaneHodiny.put(pracovnik.getId(), odpracovaneHodiny.get(pracovnik.getId()) + typ.getDelkaHodin());
                        break;
                    }
                }
            }
            if (!finalSmenyDne.isEmpty()) {
                plan.put(aktualniDatum, finalSmenyDne);
            }
        }
        return plan;
    }
}