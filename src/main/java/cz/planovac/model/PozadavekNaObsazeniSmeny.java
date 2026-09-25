package cz.planovac.model;

import java.io.Serializable;
import java.util.Objects;

public class PozadavekNaObsazeniSmeny implements Serializable {
    private static final long serialVersionUID = 1L;

    private final TypSmeny typSmeny;
    private int pocetZamestnancu;

    public PozadavekNaObsazeniSmeny(TypSmeny typSmeny, int pocetZamestnancu) {
        this.typSmeny = typSmeny;
        this.pocetZamestnancu = pocetZamestnancu;
    }

    public TypSmeny getTypSmeny() {
        return typSmeny;
    }

    public int getPocetZamestnancu() {
        return pocetZamestnancu;
    }

    public void setPocetZamestnancu(int pocetZamestnancu) {
        this.pocetZamestnancu = pocetZamestnancu;
    }

    @Override
    public String toString() {
        return typSmeny.getNazev() + ": " + pocetZamestnancu + " zaměstnanců";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PozadavekNaObsazeniSmeny that = (PozadavekNaObsazeniSmeny) o;
        return typSmeny == that.typSmeny;
    }

    @Override
    public int hashCode() {
        return Objects.hash(typSmeny);
    }
}