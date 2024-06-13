

package de.carpelibrum.sqlite;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

public class SQLiteDemo extends Activity implements OnClickListener {
	
	private final int NEUER_EINTRAG_DIALOG      = 0;

	
	private DBZugriff dbZugriff;
	private ListView anzeigeListe;
	private SimpleCursorAdapter adapter;
	private Cursor cursor;

    private SimpleDateFormat datumFormat;
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_sqlitedemo);
 
        // Buttons
        Button button = (Button) this.findViewById(R.id.buttonNeu);
        button.setOnClickListener(this);
        button = (Button) this.findViewById(R.id.buttonHeute);
        button.setOnClickListener(this);
        
        String sql = "CREATE TABLE data (_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
										"benutzername VARCHAR(20) NOT NULL, " +
										"datum DATE, " +
										"messintervall INT NOT NULL, " +
										"puls INT NOT NULL" +
										"blutsauerstoff INT NOT NULL)";
        	       
        dbZugriff     = new DBZugriff(this, "messwerte.dat", sql);
        
        
        // Anzeige initialisieren: wird aus Cursor gefuettert
        datumFormat  = new SimpleDateFormat("dd.MM.yyyy");
        cursor       = dbZugriff.erzeugeListViewCursor();       
        anzeigeListe = (ListView) this.findViewById(R.id.listView1);
        
        String[] anzeigeSpalten = new String[]{"benutzername", "datum", "messintervall", "puls", "blutsauerstoff"};
        int[] anzeigeViews      = new int[]{ R.id.textViewBenutzername, R.id.textViewVorname, R.id.textViewGeburtstag};
        adapter                 = new SimpleCursorAdapter(this, R.layout.datensatz, cursor, 
        		                                          anzeigeSpalten, anzeigeViews, 
        		                                          CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER );
                         
        
        adapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
        	 public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
        	 

        	   if (columnIndex == 3) {
        		 // Geburtsdatum umformatieren
        		 try {
        			long datum = cursor.getLong(columnIndex); 
        			
        			if(datum != 0) {
        			  String str = datumFormat.format(new Date(datum));
        			  TextView anzeige = (TextView) view;
        			  anzeige.setText(str);
        			  return true; 
        			}
        		 }
                 catch(Exception ex) {}
        	 }
       	
        	 return false; //keine Aenderung
        	
        	}
        });
        
        
        anzeigeListe.setAdapter(adapter); 
    }
    
    
    
    
    
    
	public void onClick(View v) {
		switch(v.getId()) {
		   case R.id.buttonNeu  : showDialog(NEUER_EINTRAG_DIALOG);
		                          break;
		   case R.id.buttonHeute: Dialog dialog = erzeugeGeburtstagsKinderDialog();
			                      dialog.setOwnerActivity(this);
			                      dialog.show();
		                          break; 
		}
		
	}


    
    
	@Override
	protected Dialog onCreateDialog(int id) {
        Dialog ergebnis = null;
        
		switch(id) {
		   case NEUER_EINTRAG_DIALOG    : ergebnis = erzeugeNeuerEintragDialog();
		                                  break; 
	
		}
		
		return ergebnis; 
	}
	
	
	
	

	@Override
	protected void onPrepareDialog(int id, Dialog dialog) {
		switch(id) {
	 	   case NEUER_EINTRAG_DIALOG: // Werte zuruecksetzen
	 		                          TextView name = (TextView) dialog.findViewById(R.id.editTextName);
	 	                              name.setText("Name"); 
	 	                              TextView vorname = (TextView) dialog.findViewById(R.id.editTextVorname);
	 	                              vorname.setText("Vorname");
	 	                              TextView datum = (TextView) dialog.findViewById(R.id.dialogLabelGeburtstagDatum);
	 	                              datum.setText(""); 
	 	                              break; 
		}
		
		super.onPrepareDialog(id, dialog);
	}





	// Dialog zur Anzeige wer heute Geburtstag hat 
	private Dialog erzeugeGeburtstagsKinderDialog() {
		final Dialog dialog = new Dialog(this); 
		dialog.setContentView(R.layout.geburtstagskinder_dialog);
		dialog.setCancelable(false);
		dialog.setTitle("Heutige Geburtstagskinder:");
		
		
		Button button = (Button) dialog.findViewById(R.id.buttonSchliessen);
		final ScrollView scrollView = (ScrollView) dialog.findViewById(R.id.scrollView1);
		scrollView.removeAllViews();
		
		button.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {				
				dialog.dismiss();
			}
		});
		
		

		Calendar jetzt = Calendar.getInstance();
		int heuteJahr  = jetzt.get(Calendar.YEAR);
		int heuteMonat = jetzt.get(Calendar.MONTH);
		int heuteTag   = jetzt.get(Calendar.DAY_OF_MONTH);

		TextView textView   = new TextView(this);
		textView.setPadding(0,0,0,10);
		scrollView.addView(textView); 
		
		List<Datensatz> eintraege = dbZugriff.leseDatensaetze();
		
		boolean relevanteEintraege = false; 
		
		   for(Datensatz d : eintraege) {
			   GregorianCalendar geburtstagCal = new GregorianCalendar();
			   geburtstagCal.setTime(d.geburtsdatum);
			   int geburtsmonat = geburtstagCal.get(Calendar.MONTH);
			   int geburtstag   = geburtstagCal.get(Calendar.DAY_OF_MONTH);
			
			   if(geburtsmonat == heuteMonat && geburtstag == heuteTag) {
				   // hat heute Geburtstag -> anzeigen 
				   relevanteEintraege = true; 
		           int alter = heuteJahr - geburtstagCal.get(Calendar.YEAR);
			       textView.append(d.vorname + " " + d.name + " wird " + alter + "." + "\n");		
			   }
		   }

			
	    if(!relevanteEintraege) {
		   textView.setText("Niemand!\n");
	    }
			
			

		
		return dialog; 
	}
	
	
	// Dialog zur Eingabe eines neuen Datensatzes
	private Dialog erzeugeNeuerEintragDialog() {
		final Dialog dialog = new Dialog(this);
        final Activity activity = this;
        
		dialog.setContentView(R.layout.neuereintrag_dialog);
		dialog.setCancelable(false);
		dialog.setTitle("Neuer Eintrag");
		
		final TextView geburtstagText = (TextView) dialog.findViewById(R.id.dialogLabelGeburtstagDatum);
		geburtstagText.setText("");
		
		Button button = (Button) dialog.findViewById(R.id.buttonGeburtstag);
		button.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				// DatePickerDialog anzeigen zur Auswahl des Geburtsdatums
				final DatePickerDialog.OnDateSetListener listener;
				
			    listener = new DatePickerDialog.OnDateSetListener() {
				               public void onDateSet(DatePicker view, int year, 
		                                             int monthOfYear, int dayOfMonth) {
				            	   int geburtstag   = view.getDayOfMonth();
				            	   int geburtsmonat = view.getMonth();
				            	   int geburtsjahr  = view.getYear();
				            	   Calendar calendar = new GregorianCalendar(geburtsjahr, geburtsmonat, geburtstag);
				            	   String str = datumFormat.format(calendar.getTime());
				            	   geburtstagText.setText(str);
				               }
				 };

			  // Dialog erzeugen und Datum auf heute setzen
		      Calendar c = Calendar.getInstance();
			  int jahr   = c.get(Calendar.YEAR);
			  int monat  = c.get(Calendar.MONTH);
			  int tag    = c.get(Calendar.DAY_OF_MONTH);

			  DatePickerDialog dialog = new DatePickerDialog(activity, listener, jahr, monat, tag); 
			  dialog.setCancelable(false);
			  dialog.setOwnerActivity(activity);
			  dialog.show();
			}
		});
		
		
		button = (Button) dialog.findViewById(R.id.buttonOK);
		button.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				EditText editText = (EditText) dialog.findViewById(R.id.editTextName);
				String name       = editText.getEditableText().toString();
				editText          = (EditText) dialog.findViewById(R.id.editTextVorname);
				String vorname    = editText.getEditableText().toString();
				String datumString = geburtstagText.getText().toString();
				Date datum;
				
				try {
	               datum = datumFormat.parse(datumString);
				}
				catch(Exception x) {
					datum = null;
				}
				
				Datensatz datensatz = new Datensatz(name, vorname, datum);
				dbZugriff.datensatzEinfuegen(datensatz);
				anzeigeAktualisieren();				
				dialog.dismiss(); // Dialog schliessen
				
			}
			
		  });
		
		button = (Button) dialog.findViewById(R.id.buttonAbbrechen);
		button.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				dialog.dismiss(); // Dialog schliessen
			}
			
		  });
	
		return dialog; 
	}
    
    
	private void anzeigeAktualisieren() {
		if(cursor != null) {
			cursor.close();
		}
		
		cursor = dbZugriff.erzeugeListViewCursor();
		adapter.changeCursor(cursor);
	}


	@Override
	protected void onDestroy() {
		if(cursor != null && !cursor.isClosed()) {
			cursor.close();
		}
    	if(dbZugriff != null) {
			dbZugriff.close();
		}
		
		super.onDestroy();
	}





	
}