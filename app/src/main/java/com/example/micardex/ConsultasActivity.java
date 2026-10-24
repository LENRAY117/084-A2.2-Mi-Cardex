package com.example.micardex;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;
import java.util.Locale;

/** Consultas: promedio general y promedio por semestre. */
public class ConsultasActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_consultas);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        SharedPreferences prefs = getSharedPreferences(MainActivity.PREFS, MODE_PRIVATE);
        TextView tvPerfil = findViewById(R.id.tvPerfilCons);
        TextView tvGeneral = findViewById(R.id.tvGeneral);
        TextView tvSemestres = findViewById(R.id.tvSemestres);

        tvPerfil.setText(prefs.getString(MainActivity.KEY_NOMBRE, "") + "\nNo. de control: "
                + prefs.getString(MainActivity.KEY_CONTROL, ""));

        findViewById(R.id.navInicio).setOnClickListener(v -> finish());
        findViewById(R.id.navConsultas).setOnClickListener(v -> {});

        DBHelper db = new DBHelper(this);

        Double general = db.promedioGeneral();
        tvGeneral.setText(general == null
                ? "Sin calificaciones registradas"
                : String.format(Locale.US, "%.2f", general));

        List<double[]> porSemestre = db.promediosPorSemestre();
        if (porSemestre.isEmpty()) {
            tvSemestres.setText("Sin calificaciones registradas");
        } else {
            StringBuilder sb = new StringBuilder();
            for (double[] fila : porSemestre) {
                sb.append(String.format(Locale.US, "Semestre %d:  %.2f   (%d materia%s)\n",
                        (int) fila[0], fila[1], (int) fila[2], fila[2] == 1 ? "" : "s"));
            }
            tvSemestres.setText(sb.toString().trim());
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
