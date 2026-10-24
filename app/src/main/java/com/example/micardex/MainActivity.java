package com.example.micardex;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    public static final String PREFS = "perfil_alumno";
    public static final String KEY_NOMBRE = "nombre";
    public static final String KEY_CONTROL = "control";

    private DBHelper db;
    private SharedPreferences prefs;
    private TextView tvAlumno, tvControl, tvVacio;
    private ListView lvMaterias;
    private ArrayAdapter<Materia> adapter;
    private final List<Materia> datos = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        db = new DBHelper(this);
        prefs = getSharedPreferences(PREFS, MODE_PRIVATE);

        tvAlumno = findViewById(R.id.tvAlumno);
        tvControl = findViewById(R.id.tvControl);
        tvVacio = findViewById(R.id.tvVacio);
        lvMaterias = findViewById(R.id.lvMaterias);

        adapter = new ArrayAdapter<Materia>(this, android.R.layout.simple_list_item_2,
                android.R.id.text1, datos) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View v = super.getView(position, convertView, parent);
                Materia m = getItem(position);
                ((TextView) v.findViewById(android.R.id.text1)).setText(m.nombre);
                ((TextView) v.findViewById(android.R.id.text2)).setText(m.resumen());
                return v;
            }
        };
        lvMaterias.setAdapter(adapter);
        lvMaterias.setEmptyView(tvVacio);

        lvMaterias.setOnItemClickListener((parent, view, position, id) -> {
            Intent i = new Intent(this, MateriaActivity.class);
            i.putExtra(MateriaActivity.EXTRA_ID, datos.get(position).id);
            startActivity(i);
        });

        lvMaterias.setOnItemLongClickListener((parent, view, position, id) -> {
            Materia m = datos.get(position);
            new AlertDialog.Builder(this)
                    .setTitle("Eliminar materia")
                    .setMessage("¿Eliminar \"" + m.nombre + "\" del cárdex?")
                    .setPositiveButton("Eliminar", (d, w) -> {
                        db.eliminar(m.id);
                        cargarLista();
                    })
                    .setNegativeButton("Cancelar", null)
                    .show();
            return true;
        });

        findViewById(R.id.btnAgregar).setOnClickListener(v ->
                startActivity(new Intent(this, MateriaActivity.class)));
        findViewById(R.id.navConsultas).setOnClickListener(v ->
                startActivity(new Intent(this, ConsultasActivity.class)));
        findViewById(R.id.navInicio).setOnClickListener(v -> {});
        findViewById(R.id.btnPerfil).setOnClickListener(v -> mostrarDialogoPerfil(false));

        // Primera vez: pedir nombre y número de control
        if (prefs.getString(KEY_NOMBRE, "").isEmpty()) {
            mostrarDialogoPerfil(true);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        cargarPerfil();
        cargarLista();
    }

    private void cargarPerfil() {
        String nombre = prefs.getString(KEY_NOMBRE, "");
        String control = prefs.getString(KEY_CONTROL, "");
        tvAlumno.setText(nombre.isEmpty() ? "Alumno sin registrar" : nombre);
        tvControl.setText(control.isEmpty() ? "" : "No. de control: " + control);
    }

    private void cargarLista() {
        datos.clear();
        datos.addAll(db.listar());
        adapter.notifyDataSetChanged();
    }

    private void mostrarDialogoPerfil(boolean obligatorio) {
        View vista = getLayoutInflater().inflate(R.layout.dialog_perfil, null);
        EditText etNombre = vista.findViewById(R.id.etNombreAlumno);
        EditText etControl = vista.findViewById(R.id.etControl);
        etNombre.setText(prefs.getString(KEY_NOMBRE, ""));
        etControl.setText(prefs.getString(KEY_CONTROL, ""));

        AlertDialog.Builder b = new AlertDialog.Builder(this)
                .setTitle("Datos del alumno")
                .setView(vista)
                .setCancelable(!obligatorio)
                .setPositiveButton("Guardar", null);
        if (!obligatorio) b.setNegativeButton("Cancelar", null);

        AlertDialog dialog = b.create();
        dialog.show();
        // Se asigna aquí para que el diálogo no se cierre si hay datos inválidos
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String nombre = etNombre.getText().toString().trim();
            String control = etControl.getText().toString().trim();
            if (nombre.isEmpty() || control.isEmpty()) {
                Toast.makeText(this, "Captura el nombre y el número de control",
                        Toast.LENGTH_SHORT).show();
                return;
            }
            prefs.edit()
                    .putString(KEY_NOMBRE, nombre)
                    .putString(KEY_CONTROL, control)
                    .apply();
            cargarPerfil();
            dialog.dismiss();
        });
    }
}
