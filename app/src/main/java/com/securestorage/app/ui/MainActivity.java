package com.securestorage.app.ui;

import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.securestorage.app.R;
import com.securestorage.app.cache.CacheStore;
import com.securestorage.app.external.ExternalAppFilesStore;
import com.securestorage.app.files.InternalTextStore;
import com.securestorage.app.files.StudentsJsonStore;
import com.securestorage.app.model.Student;
import com.securestorage.app.prefs.AppPrefs;
import com.securestorage.app.prefs.SecurePrefs;

import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "SecureStorage";

    private final List<String> langs = Arrays.asList("fr", "en", "ar");

    private EditText etName;
    private EditText etToken;
    private Spinner  spLang;
    private Switch   swDark;
    private TextView tvResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etName   = findViewById(R.id.etName);
        etToken  = findViewById(R.id.etToken);
        spLang   = findViewById(R.id.spLang);
        swDark   = findViewById(R.id.swDark);
        tvResult = findViewById(R.id.tvResult);

        spLang.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, langs));

        Button btnSavePrefs = findViewById(R.id.btnSavePrefs);
        Button btnLoadPrefs = findViewById(R.id.btnLoadPrefs);
        Button btnSaveJson  = findViewById(R.id.btnSaveJson);
        Button btnLoadJson  = findViewById(R.id.btnLoadJson);
        Button btnClear     = findViewById(R.id.btnClear);

        btnSavePrefs.setOnClickListener(v -> savePrefs());
        btnLoadPrefs.setOnClickListener(v -> loadPrefsToUi());
        btnSaveJson .setOnClickListener(v -> saveJsonFile());
        btnLoadJson .setOnClickListener(v -> loadJsonFile());
        btnClear    .setOnClickListener(v -> clearAll());

        loadPrefsToUi();
    }

    private void savePrefs() {
        String name  = etName.getText().toString().trim();
        String lang  = langs.get(Math.max(0, spLang.getSelectedItemPosition()));
        String theme = swDark.isChecked() ? "dark" : "light";

        boolean ok = AppPrefs.save(this, name, lang, theme, false);

        String token = etToken.getText().toString();
        if (!token.isBlank()) {
            try {
                SecurePrefs.saveToken(this, token);
            } catch (Exception e) {
                tvResult.setText("Erreur chiffrement token : " + e.getMessage());
                return;
            }
        }

        Log.d(TAG, "Prefs sauvegardées ok=" + ok + " name=" + name + " lang=" + lang + " theme=" + theme);

        try {
            CacheStore.write(this, "last_ui.txt", "name=" + name + ", lang=" + lang + ", theme=" + theme);
        } catch (Exception ignored) {}

        try {
            ExternalAppFilesStore.write(this, "export_prefs.txt",
                    "name=" + name + "\nlang=" + lang + "\ntheme=" + theme);
        } catch (Exception ignored) {}

        tvResult.setText(
                "Sauvegarde prefs terminée.\n\n"
                + "name  = " + name  + "\n"
                + "lang  = " + lang  + "\n"
                + "theme = " + theme + "\n\n"
                + "token : stocké chiffré si non vide (non affiché)"
        );
    }

    private void loadPrefsToUi() {
        AppPrefs.Triple triple = AppPrefs.load(this);

        etName.setText(triple.name);
        swDark.setChecked("dark".equals(triple.theme));

        int idx = langs.indexOf(triple.lang);
        spLang.setSelection(idx >= 0 ? idx : 0);

        int tokenLen = 0;
        try {
            String token = SecurePrefs.loadToken(this);
            tokenLen = (token != null) ? token.length() : 0;
        } catch (Exception ignored) {}

        tvResult.setText(
                "Chargement prefs terminé.\n\n"
                + "name        = " + triple.name  + "\n"
                + "lang        = " + triple.lang  + "\n"
                + "theme       = " + triple.theme + "\n"
                + "tokenLength = " + tokenLen
        );

        Log.d(TAG, "Prefs chargées name=" + triple.name + " lang=" + triple.lang
                + " theme=" + triple.theme + " tokenLength=" + tokenLen);
    }

    private void saveJsonFile() {
        List<Student> students = Arrays.asList(
                new Student(1, "Amina", 20),
                new Student(2, "Omar",  21),
                new Student(3, "Sara",  19)
        );

        try {
            StudentsJsonStore.save(this, students);
            InternalTextStore.writeUtf8(this, "note.txt", "Sauvegarde JSON effectuée (UTF-8).");
        } catch (Exception e) {
            tvResult.setText("Erreur sauvegarde JSON : " + e.getMessage());
            return;
        }

        Log.d(TAG, "Fichiers écrits : students.json, note.txt");
        tvResult.setText(
                "Sauvegarde fichier JSON terminée.\n\n"
                + "students.json : " + students.size() + " entrée(s)\n"
                + "note.txt : écrit\n\n"
                + "Chemin : /data/data/com.securestorage.app/files/"
        );
    }

    private void loadJsonFile() {
        List<Student> students = StudentsJsonStore.load(this);

        String note;
        try {
            note = InternalTextStore.readUtf8(this, "note.txt");
        } catch (Exception e) {
            note = "(note.txt absent)";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Chargement fichier JSON terminé.\n\n");
        sb.append("note = ").append(note).append("\n\n");
        sb.append("students (").append(students.size()).append(") :\n");
        for (Student s : students) {
            sb.append("  id=").append(s.id)
              .append("  name=").append(s.name)
              .append("  age=").append(s.age).append("\n");
        }
        if (students.isEmpty()) {
            sb.append("  (aucun étudiant — sauvegardez d'abord)");
        }

        tvResult.setText(sb.toString());
        Log.d(TAG, "JSON chargé students=" + students.size());
    }

    private void clearAll() {
        AppPrefs.clear(this);
        try { SecurePrefs.clear(this); } catch (Exception ignored) {}
        StudentsJsonStore.delete(this);
        InternalTextStore.delete(this, "note.txt");
        int purged = CacheStore.purge(this);
        ExternalAppFilesStore.delete(this, "export_prefs.txt");

        etName.setText("");
        etToken.setText("");
        swDark.setChecked(false);
        spLang.setSelection(0);

        tvResult.setText(
                "Nettoyage terminé.\n\n"
                + "prefs            : clear()\n"
                + "secure_prefs     : clear()\n"
                + "students.json    : delete\n"
                + "note.txt         : delete\n"
                + "export_prefs.txt : delete\n"
                + "cache purgé      : " + purged + " fichier(s)"
        );

        Log.d(TAG, "Nettoyage terminé cachePurged=" + purged);
    }
}
