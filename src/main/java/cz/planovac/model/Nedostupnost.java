package cz.planovac.model;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;

public class Nedostupnost implements Serializable {
    private static final long serialVersionUID = 1L;

    private final int pracovnikId;
    private final LocalDate odDatum;
    private final LocalDate doDatum;

    public Nedostupnost(int pracovnikId, LocalDate odDatum, LocalDate doDatum) {
        this.pracovnikId = pracovnikId;
        this.odDatum = odDatum;
        this.doDatum = doDatum;
    }

    public int getPracovnikId() {
        return pracovnikId;
    }

    public LocalDate getOdDatum() {
        return odDatum;
    }

    public LocalDate getDoDatum() {
        return doDatum;
    }

    @Override
    public String toString() {
        return "ID pracovníka: " + pracovnikId + ", od: " + odDatum + ", do: " + doDatum;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Nedostupnost that = (Nedostupnost) o;
        return pracovnikId == that.pracovnikId &&
               Objects.equals(odDatum, that.odDatum) &&
               Objects.equals(doDatum, that.doDatum);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pracovnikId, odDatum, doDatum);
    }
}