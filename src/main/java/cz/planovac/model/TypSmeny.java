package cz.planovac.model;

import java.io.Serializable;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit; // Přidáno pro výpočet délky směny
import java.util.Objects; // Přidáno pro equals a hashCode

public class TypSmeny implements Serializable {
    private static final long serialVersionUID = 2L; // Změna serialVersionUID

    private final int id; // Nové ID pro jednoznačnou identifikaci
    private String nazev;
    private LocalTime zacatek;
    private LocalTime konec;
    private double delkaHodin; // Nové pole pro délku směny v hodinách

    // Statická proměnná pro generování unikátních ID
    private static int nextId = 1;

    // Konstruktor pro vytváření nových typů směn
    public TypSmeny(String nazev, LocalTime zacatek, LocalTime konec) {
        this.id = nextId++; // Přiřadíme unikátní ID
        this.nazev = nazev;
        this.zacatek = zacatek;
        this.konec = konec;
        vypocitejDelkuHodin();
    }

    // Konstruktor pro načítání z persistentního úložiště (s již existujícím ID)
    public TypSmeny(int id, String nazev, LocalTime zacatek, LocalTime konec) {
        this.id = id;
        this.nazev = nazev;
        this.zacatek = zacatek;
        this.konec = konec;
        vypocitejDelkuHodin();
        // Aktualizujeme nextId, aby se zajistilo, že nová ID budou unikátní
        if (id >= nextId) {
            nextId = id + 1;
        }
    }


    // Metoda pro výpočet délky směny v hodinách
    private void vypocitejDelkuHodin() {
        if (zacatek.isBefore(konec)) {
            this.delkaHodin = zacatek.until(konec, ChronoUnit.MINUTES) / 60.0;
        } else {
            // Směna přes půlnoc
            this.delkaHodin = (zacatek.until(LocalTime.MAX, ChronoUnit.MINUTES) + 1 +
                               LocalTime.MIDNIGHT.until(konec, ChronoUnit.MINUTES)) / 60.0;
        }
    }

    public int getId() {
        return id;
    }

    public String getNazev() {
        return nazev;
    }

    public void setNazev(String nazev) {
        this.nazev = nazev;
    }

    public LocalTime getZacatek() {
        return zacatek;
    }

    public void setZacatek(LocalTime zacatek) {
        this.zacatek = zacatek;
        vypocitejDelkuHodin(); // Přepočítat délku při změně času
    }

    public LocalTime getKonec() {
        return konec;
    }

    public void setKonec(LocalTime konec) {
        this.konec = konec;
        vypocitejDelkuHodin(); // Přepočítat délku při změně času
    }

    public double getDelkaHodin() {
        return delkaHodin;
    }

    @Override
    public String toString() {
        return nazev + " (" + zacatek + " - " + konec + ", " + String.format("%.1f", delkaHodin) + "h)";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TypSmeny typSmeny = (TypSmeny) o;
        return id == typSmeny.id; // Porovnáváme podle ID
    }

    @Override
    public int hashCode() {
        return Objects.hash(id); // Hash kód podle ID
    }

    // Metoda pro nastavení nextId při načítání dat, aby se zajistila unikátnost ID
    public static void setNextId(int id) {
        if (id >= nextId) {
            nextId = id + 1;
        }
    }
}