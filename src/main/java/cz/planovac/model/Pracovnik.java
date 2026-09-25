package cz.planovac.model;

import java.io.Serializable;

public class Pracovnik implements Serializable {
    private static final long serialVersionUID = 2L; // Změněno kvůli nové vlastnosti

    private final int id;
    private final String jmeno;
    private final String prijmeni;
    private final double uvazek; // NOVÉ: 1.0 = plný úvazek, 0.5 = poloviční atd.
    private final Preference preference; 

    public Pracovnik (int id, String jmeno, String prijmeni, double uvazek) {
        this.id = id;
        this.jmeno = jmeno;
        this.prijmeni = prijmeni;
        this.uvazek = uvazek;
        this.preference = new Preference(); 
    }

    public int getId() { return id; }
    public String getJmeno() { return jmeno; }
    public String getPrijmeni() { return prijmeni; }
    public double getUvazek() { return uvazek; } // NOVÉ

    @Override
    public String toString() {
        return jmeno + " " + prijmeni + " (ID: " + id + ", Úvazek: " + uvazek + ")";
    }

    public Preference getPreference() {
        return preference;
    }
}