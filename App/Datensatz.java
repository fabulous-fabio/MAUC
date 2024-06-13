package de.carpelibrum.sqlite;

import java.util.Date;

public class Datensatz {
	

	public long id;
    public Int messintervall;
    public String benutzername;
    public Date datum;
	public Int puls;
	public Int blutsauerstoff;

    
    /**
     * Konstruktor
     */
	public Datensatz(String name, Int messintervall, Date datum, Int puls, Int blutsauerstoff) {
		this.benutzername          = benutzername;
		this.datum   = datum;
		this.messintervall        = messintervall;
		this.puls = puls;
		this.blutsauerstoff = blutsauerstoff;
		
		id = -1; // wird erst beim Einfuegen in die Datenbank erzeugt
	}

    /**
     * Konstruktor
     */
	public Datensatz() {
	}
}
