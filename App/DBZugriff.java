package de.carpelibrum.sqlite;


import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DBZugriff extends SQLiteOpenHelper {

	private SQLiteDatabase db;
	private String tabellenSQL;
	private String tabelle;

	
    /**
     * Konstruktor
     * @param activity: aufrufende Activity
     * @param dbName: Name der Datenbank (wenn nicht vorhanden, dann wird sie neu erstellt)
     * @param sql: SQL-Kommando zum Erzeugen der gew?nschten Tabelle (oder null bei ?ffnen
     *                      einer vorhandenen Datenbank)
     */
	public DBZugriff(Context activity, String dbName, String tabellenSQL) {
	   	super(activity, dbName, null, 1);
	   	this.tabellenSQL = tabellenSQL; 
	   	bestimmeTabelle();
	   	db = this.getWritableDatabase();
	}
	

	/** Aus dem Tabellen-Anlage-SQL den Namen der Tabelle  extrahieren
	 * 
	 */
	private void bestimmeTabelle() {
		String sql                = tabellenSQL.toUpperCase();
		StringTokenizer tokenizer = new StringTokenizer(sql); 
		
		//  den Tabellennamen suchen
		while(tokenizer.hasMoreTokens()) {
			String token = tokenizer.nextToken();
			
			if(token.equals("TABLE")) {
				tabelle = tokenizer.nextToken();
				break;
			}
		}
	}

	@Override
	/**
	 * Wird nur aufgerufen, wenn eine Datenbank neu erzeugt wird
	 */
	public void onCreate(SQLiteDatabase db) {
		try {
			// Tabelle anlegen 
			 db.execSQL(tabellenSQL); 
			 Log.d("carpelibrum", "datenbank wird angelegt");
		}
		catch(Exception ex) {
			Log.e("carpelibrum", ex.getMessage());
		}
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS " + tabelle);
		onCreate(db); 
	}


	/**
	 * Hier noch evtl. eigene Aufr?umarbeiten durchf?hren
	 */
	@Override
	public synchronized void close() {
		if(db != null) {
			db.close();
			db = null; 
	    }
	
		super.close();	
	}





    /**
     * Gegebenen Datensatz in die  Tabelle eingeben
     * @param datensatz
     * @return ID des neuen Datensatzes oder -1 bei Fehler
     */
	public long datensatzEinfuegen(Datensatz datensatz) {
		try {
		   ContentValues daten = erzeugeDatenObjekt(datensatz); 
		   return db.insert(tabelle, null, daten); // id wird automatisch von SQLite gef?llt
		}
		catch(Exception ex) {
			Log.d("carpelibrum", ex.getMessage());
			return -1; 
		}
	}
	
	


	
	/**
	 * Liefert Cursor zum Zugriff auf alle Eintr?ge, alphabetisch geordnet nach Spalte "Name"
	 * @return
	 */
	public Cursor erzeugeListViewCursor() {	
		String[] spalten = new String[]{"_id", "benutzername", "datum", "messintervall", "puls", "blutsauerstoff"};
		return  db.query(tabelle, spalten, null, null, null, null, "benutzername");
	}
	
	/**
	 * Alle Datens?tze  liefern 
	 * @return Liste an Datens?tzen
	 */
	public List<Datensatz> leseDatensaetze() {
		List<Datensatz> ergebnis = new ArrayList<Datensatz>();
		Cursor cursor = null;
		
		try {
			cursor = db.query(tabelle, null, null, null, null, null, null);
			int anzahl = cursor.getCount();
			cursor.moveToFirst();
			
			for(int i = 0; i < anzahl; i++) {
                Datensatz ds = erzeugeDatensatz(cursor);
				ergebnis.add(ds);				
				cursor.moveToNext();
			}
		}
		catch(Exception ex) {
			Log.e("carpelibrum", ex.getMessage());
		}
		finally {
			// egal ob Erfolg oder Exception:: cursor schlie?en
			if(cursor != null && !cursor.isClosed()) {
				cursor.close();
			}
		}
		
		return ergebnis;
	}
	
	
	/**
	 * Existierenden Datensatz mit aktualisiertem Inhalt beschreiben
	 */
	public void aktualisiereDatensatz(Datensatz ds) {
		try {
			ContentValues daten = erzeugeDatenObjekt(ds); 
			db.update(tabelle, daten, "id = " + ds.id, null);
		}
		catch(Exception ex) {
			  Log.e("carpelibrum", ex.getMessage()); 	
		}
	}
	
	public void loescheDatensatz(Datensatz ds) {
		try {
	  	  db.delete(tabelle, "id = " + ds.id, null);
		}
		catch(Exception ex) {
		  Log.e("carpelibrum", ex.getMessage()); 	
		}
	}
	
	
	/**
	 * Aus dem Cursor an der aktuellen Position lesen und Datensatz erzeugen
	 * @param cursor
	 * @return
	 */
	private Datensatz erzeugeDatensatz(Cursor cursor) {
		Datensatz ds        = new Datensatz();
		ds.id               = cursor.getLong(0);
		ds.benutzername             = cursor.getString(1);
		ds.messintervall          = cursor.getInt(3);
		ds.puls = cursor.getInt(4);
		ds.blutsauerstoff = cursor.getInt(5);

		try { // Geburtsdatum darf null sein, was beim Zugriff eine Exception werfen w?rde
		   long datum   = cursor.getLong(2);
		   ds.datum     = new Date(datum);
		}
		catch(Exception ex) {
			// Geburtsdatum ist nicht gesetzt
			ds.datum = null;
		}
		
		return ds; 
	}
	
	/**
	 * Erzeugt ein SQLite Datenobjekt
	 * @param datensatz
	 * @return
	 */
	private ContentValues erzeugeDatenObjekt(Datensatz datensatz) {
		ContentValues daten = new ContentValues();
		daten.put("benutzername", datensatz.benutzername);
		if(datensatz.datum != null) {
			daten.put("datum", datensatz.datum.getTime());
		}
		daten.put("messintervall", datensatz.messintervall);
		daten.put("puls", datensatz.puls);
		daten.put("blutsauerstoff", datensatz.blutsauerstoff);

		return daten; 
	}
	
}
