package de.othaw.inventur;
import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

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
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

/*import de.carpelibrum.sqlite.R;*/

public class SQLiteDemo extends Activity implements OnClickListener {

    private static final int DIALOG_NEUER_EINTRAG = 1;

    private DBZugriff dbZugriff;
    private ListView anzeigeListe;
    private SimpleCursorAdapter adapter;
    private Cursor cursor;
    private SimpleDateFormat datumFormat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_sqlitedemo);

        Button buttonNeu = findViewById(R.id.buttonNeu);
        buttonNeu.setOnClickListener(this);

        String sql = "CREATE TABLE data (_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "benutzername VARCHAR(20) NOT NULL, " +
                "datum DATE, " +
                "time TIME" +
                "messintervall INT NOT NULL, " +
                "puls INT NOT NULL, " +
                "blutsauerstoff INT NOT NULL)";

        dbZugriff = new DBZugriff(this, "messwerte.db", sql);

        datumFormat = new SimpleDateFormat("dd.MM.yyyy");
        cursor = dbZugriff.erzeugeListViewCursor();
        anzeigeListe = findViewById(R.id.listView1);

        String[] anzeigeSpalten = new String[]{"benutzername", "datum", "messintervall", "puls", "blutsauerstoff"};
        int[] anzeigeViews = new int[]{R.id.textViewBenutzername, R.id.textViewDatum, R.id.textViewMessintervall, R.id.textViewPuls, R.id.textViewBlutsauerstoff};
        adapter = new SimpleCursorAdapter(this, R.layout.datensatz, cursor, anzeigeSpalten, anzeigeViews, CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);

        adapter.setViewBinder((view, cursor, columnIndex) -> {
            if (columnIndex == cursor.getColumnIndex("datum")) {
                long datum = cursor.getLong(columnIndex);
                if (datum != 0) {
                    String str = datumFormat.format(new Date(datum));
                    TextView anzeige = (TextView) view;
                    anzeige.setText(str);
                    return true;
                }
            }
            return false;
        });

        anzeigeListe.setAdapter(adapter);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.buttonNeu) {
            showDialog(DIALOG_NEUER_EINTRAG);
        }
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        if (id == DIALOG_NEUER_EINTRAG) {
            return erzeugeNeuerEintragDialog();
        }
        return null;
    }

    @Override
    protected void onPrepareDialog(int id, Dialog dialog) {
        if (id == DIALOG_NEUER_EINTRAG) {
            ((EditText) dialog.findViewById(R.id.editTextName)).setText("");
            ((EditText) dialog.findViewById(R.id.editTextMessintervall)).setText("");
            ((EditText) dialog.findViewById(R.id.editTextPuls)).setText("");
            ((EditText) dialog.findViewById(R.id.editTextBlutsauerstoff)).setText("");
            //((TextView) dialog.findViewById(R.id.dialogLabelGeburtstagDatum)).setText("");
        }
    }

    private Dialog erzeugeNeuerEintragDialog() {
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.neuereintrag_dialog);
        dialog.setCancelable(false);
        dialog.setTitle("Neuer Eintrag");

        final TextView datumText = dialog.findViewById(R.id.dialogLabelGeburtstagDatum);

        Button buttonGeburtstag = dialog.findViewById(R.id.buttonGeburtstag);
        buttonGeburtstag.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            int jahr = c.get(Calendar.YEAR);
            int monat = c.get(Calendar.MONTH);
            int tag = c.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(SQLiteDemo.this, (view, year, monthOfYear, dayOfMonth) -> {
                Calendar selectedDate = new GregorianCalendar(year, monthOfYear, dayOfMonth);
                datumText.setText(datumFormat.format(selectedDate.getTime()));
            }, jahr, monat, tag);
            datePickerDialog.show();
        });

        Button buttonOk = dialog.findViewById(R.id.buttonOK);
        buttonOk.setOnClickListener(v -> {
            EditText editName = dialog.findViewById(R.id.editTextName);
            EditText editMessintervall = dialog.findViewById(R.id.editTextMessintervall);
            EditText editPuls = dialog.findViewById(R.id.editTextPuls);
            EditText editBlutsauerstoff = dialog.findViewById(R.id.editTextBlutsauerstoff);
            String name = editName.getText().toString();
            String messintervall = editMessintervall.getText().toString();
            String puls = editPuls.getText().toString();
            String blutsauerstoff = editBlutsauerstoff.getText().toString();

            if (!name.isEmpty() && !messintervall.isEmpty() && !puls.isEmpty() && !blutsauerstoff.isEmpty()) {
                try {
                    // Automatisch das aktuelle Datum und Uhrzeit setzen
                    Calendar calendar = Calendar.getInstance();
                    long dateMillis = calendar.getTimeInMillis(); // Aktuelles Datum und Uhrzeit als Millisekunden seit der Epoch

                    dbZugriff.datensatzEinfuegen(name, Integer.parseInt(messintervall), dateMillis, Integer.parseInt(puls), Integer.parseInt(blutsauerstoff), calendar.getTime().toString());
                    // Aktualisierung der Ansicht
                    cursor.requery();
                    adapter.notifyDataSetChanged();
                    dialog.dismiss();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });


        return dialog;
    }
}
