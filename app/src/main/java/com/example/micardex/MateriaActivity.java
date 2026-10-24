package com.example.micardex;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

/** Captura o edita una materia del cárdex. */
public class MateriaActivity extends AppCompatActivity {

    public static final String EXTRA_ID = "id";

    private DBHelper db;
    private long idMateria = -1;
    private EditText etNombre, etSemestre, etNormal, etRepeticion, etEspecial;
    private CheckBox cbRepeticion, cbEspecial;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_materia);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        db = new DBHelper(this);
        etNombre = findViewById(R.id.etNombre);
        etSemestre = findViewById(R.id.etSemestre);
        etNormal = findViewById(R.id.etNormal);
        etRepeticion = findViewById(R.id.etRepeticion);
        etEspecial = findViewById(R.id.etEspecial);
        cbRepeticion = findViewById(R.id.cbRepeticion);
        cbEspecial = findViewById(R.id.cbEspecial);

        cbRepeticion.setOnCheckedChangeListener((b, checked) ->
                etRepeticion.setVisibility(checked ? View.VISIBLE : View.GONE));
        cbEspecial.setOnCheckedChangeListener((b, checked) ->
                etEspecial.setVisibility(checked ? View.VISIBLE : View.GONE));

        idMateria = getIntent().getLongExtra(EXTRA_ID, -1);
        if (idMateria != -1) {
            setTitle("Editar materia");
            Materia m = db.obtener(idMateria);
            if (m != null) {
                etNombre.setText(m.nombre);
                etSemestre.setText(String.valueOf(m.semestre));
                if (m.normal != null) etNormal.setText(Materia.fmt(m.normal));
                if (m.repeticion != null) {
                    cbRepeticion.setChecked(true);
                    etRepeticion.setText(Materia.fmt(m.repeticion));
                }
                if (m.especial != null) {
                    cbEspecial.setChecked(true);
                    etEspecial.setText(Materia.fmt(m.especial));
                }
            }
        } else {
            setTitle("Nueva materia");
        }

        findViewById(R.id.btnGuardar).setOnClickListener(v -> guardar());
    }

    private void guardar() {
        try {
            String nombre = etNombre.getText().toString().trim();
            if (nombre.isEmpty()) {
                throw new IllegalArgumentException("Captura el nombre de la materia");
            }

            int semestre;
            try {
                semestre = Integer.parseInt(etSemestre.getText().toString().trim());
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Captura el semestre (número)");
            }
            if (semestre < 1 || semestre > 15) {
                throw new IllegalArgumentException("El semestre debe estar entre 1 y 15");
            }

            Materia m = new Materia();
            m.id = idMateria;
            m.nombre = nombre;
            m.semestre = semestre;
            m.normal = leer(etNormal, "La calificación normal", false);
            m.repeticion = cbRepeticion.isChecked()
                    ? leer(etRepeticion, "La calificación de repetición", true) : null;
            m.especial = cbEspecial.isChecked()
                    ? leer(etEspecial, "La calificación de especial", true) : null;

            if (idMateria == -1) db.insertar(m); else db.actualizar(m);
            Toast.makeText(this, "Materia guardada", Toast.LENGTH_SHORT).show();
            finish();
        } catch (IllegalArgumentException e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    /** Lee una calificación entre 0 y 100; vacío = null (si no es obligatoria). */
    private Double leer(EditText et, String etiqueta, boolean obligatoria) {
        String s = et.getText().toString().trim();
        if (s.isEmpty()) {
            if (obligatoria) {
                throw new IllegalArgumentException(etiqueta + " está vacía");
            }
            return null;
        }
        double v;
        try {
            v = Double.parseDouble(s);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(etiqueta + " no es un número válido");
        }
        if (v < 0 || v > 100) {
            throw new IllegalArgumentException(etiqueta + " debe estar entre 0 y 100");
        }
        return v;
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
