package cz.planovac.model;

import java.io.Serializable;
import java.time.DayOfWeek;
import java.util.HashSet;
import java.util.Set;

public class Preference implements Serializable {
    private static final long serialVersionUID = 1L;

    private final Set<Integer> nepreferovaniKolegove = new HashSet<>();
    private final Set<DayOfWeek> nepreferovaneDny = new HashSet<>();

    public void pridejNepreferovanehoKolegu(int kolegaId) {
        nepreferovaniKolegove.add(kolegaId);
    }

    public void odeberNepreferovanehoKolegu(int kolegaId) {
        nepreferovaniKolegove.remove(kolegaId);
    }

    public Set<Integer> getNepreferovaniKolegove() {
        return nepreferovaniKolegove;
    }

    public void pridejNepreferovanyDen(DayOfWeek den) {
        nepreferovaneDny.add(den);
    }

    public void odeberNepreferovanyDen(DayOfWeek den) {
        nepreferovaneDny.remove(den);
    }

    public Set<DayOfWeek> getNepreferovaneDny() {
        return nepreferovaneDny;
    }
}