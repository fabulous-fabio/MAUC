package de.othaw.inventur;

import java.util.Date;
import java.sql.Time;

public class Datensatz {


	public long _id;
	public int messintervall;
	public String benutzername;
	public Date datum;
	public Time time;
	public int puls;
	public int blutsauerstoff;


	/**
	 * Konstruktor
	 */
	public Datensatz(String name, int messintervall, Date datum, Time time, int puls, int blutsauerstoff) {
		this.benutzername          = benutzername;
		this.datum = datum;
		this.time = time;
		this.messintervall = messintervall;
		this.puls = puls;
		this.blutsauerstoff = blutsauerstoff;

		_id = -1; // wird erst beim Einfuegen in die Datenbank erzeugt
	}

	/**
	 * Konstruktor
	 */
	public Datensatz() {
	}
}