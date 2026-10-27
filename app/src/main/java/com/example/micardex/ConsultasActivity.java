package com.example.micardex;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Consultas: promedios y materias filtradas por semestre. */
public class ConsultasActivity extends AppCompatActivity {

    private DBHelper db;
    private Spinner spSemestre;
    private LinearLayout listaMaterias;
    private TextView tvMateriasVacio;
    private final List<Integer> semestres = new ArrayList<>();

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
        spSemestre = findViewById(R.id.spSemestre);
        listaMaterias = findViewById(R.id.listaMaterias);
        tvMateriasVacio = findViewById(R.id.tvMateriasVacio);

        tvPerfil.setText(prefs.getString(MainActivity.KEY_NOMBRE, "") + "\nNo. de control: "
                + prefs.getString(MainActivity.KEY_CONTROL, ""));

        findViewById(R.id.navInicio).setOnClickListener(v -> finish());
        findViewById(R.id.navConsultas).setOnClickListener(v -> {});

        db = new DBHelper(this);

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

        cargarSemestres();
    }

    private void cargarSemestres() {
        semestres.clear();
        semestres.addAll(db.listarSemestres());

        List<String> opciones = new ArrayList<>();
        for (int semestre : semestres) {
            opciones.add("Semestre " + semestre);
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, opciones);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spSemestre.setAdapter(adapter);
        spSemestre.setEnabled(!semestres.isEmpty());

        if (semestres.isEmpty()) {
            tvMateriasVacio.setText("Aún no has registrado materias");
            tvMateriasVacio.setVisibility(View.VISIBLE);
            listaMaterias.setVisibility(View.GONE);
        } else {
            spSemestre.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(android.widget.AdapterView<?> parent, View view,
                                           int position, long id) {
                    mostrarMaterias(semestres.get(position));
                }

                @Override
                public void onNothingSelected(android.widget.AdapterView<?> parent) {
                    listaMaterias.removeAllViews();
                }
            });
        }
    }

    private void mostrarMaterias(int semestre) {
        listaMaterias.removeAllViews();
        List<Materia> materias = db.listarPorSemestre(semestre);
        if (materias.isEmpty()) {
            tvMateriasVacio.setText("No hay materias registradas en este semestre");
            tvMateriasVacio.setVisibility(View.VISIBLE);
            listaMaterias.setVisibility(View.GONE);
            return;
        }

        tvMateriasVacio.setVisibility(View.GONE);
        listaMaterias.setVisibility(View.VISIBLE);
        for (int i = 0; i < materias.size(); i++) {
            Materia materia = materias.get(i);
            TextView item = new TextView(this);
            item.setText(materia.nombre + "\nCalificación final: "
                    + Materia.fmt(materia.calFinal()));
            item.setTextColor(getColor(R.color.text_primary));
            item.setTextSize(15);
            item.setPadding(12, 12, 12, 12);
            listaMaterias.addView(item);

            if (i < materias.size() - 1) {
                View separador = new View(this);
                separador.setBackgroundColor(getColor(R.color.border));
                listaMaterias.addView(separador, new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, 1));
            }
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
