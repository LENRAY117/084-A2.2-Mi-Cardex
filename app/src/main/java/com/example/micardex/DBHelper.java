package com.example.micardex;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/** Base de datos SQLite local con la tabla de materias del cárdex. */
public class DBHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "cardex.db";
    private static final int DB_VERSION = 1;
    private static final String TABLA = "materias";

    public DBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLA + " ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "nombre TEXT NOT NULL, "
                + "semestre INTEGER NOT NULL, "
                + "cal_normal REAL, "
                + "cal_repeticion REAL, "
                + "cal_especial REAL)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLA);
        onCreate(db);
    }

    private ContentValues valores(Materia m) {
        ContentValues cv = new ContentValues();
        cv.put("nombre", m.nombre);
        cv.put("semestre", m.semestre);
        if (m.normal == null) cv.putNull("cal_normal"); else cv.put("cal_normal", m.normal);
        if (m.repeticion == null) cv.putNull("cal_repeticion"); else cv.put("cal_repeticion", m.repeticion);
        if (m.especial == null) cv.putNull("cal_especial"); else cv.put("cal_especial", m.especial);
        return cv;
    }

    private Double dbl(Cursor c, int i) {
        return c.isNull(i) ? null : c.getDouble(i);
    }

    private Materia desdeCursor(Cursor c) {
        Materia m = new Materia();
        m.id = c.getLong(0);
        m.nombre = c.getString(1);
        m.semestre = c.getInt(2);
        m.normal = dbl(c, 3);
        m.repeticion = dbl(c, 4);
        m.especial = dbl(c, 5);
        return m;
    }

    public long insertar(Materia m) {
        return getWritableDatabase().insert(TABLA, null, valores(m));
    }

    public int actualizar(Materia m) {
        return getWritableDatabase().update(TABLA, valores(m), "id = ?",
                new String[]{String.valueOf(m.id)});
    }

    public int eliminar(long id) {
        return getWritableDatabase().delete(TABLA, "id = ?", new String[]{String.valueOf(id)});
    }

    public Materia obtener(long id) {
        Cursor c = getReadableDatabase().rawQuery(
                "SELECT id, nombre, semestre, cal_normal, cal_repeticion, cal_especial "
                        + "FROM " + TABLA + " WHERE id = ?", new String[]{String.valueOf(id)});
        try {
            return c.moveToFirst() ? desdeCursor(c) : null;
        } finally {
            c.close();
        }
    }

    public List<Materia> listar() {
        List<Materia> lista = new ArrayList<>();
        Cursor c = getReadableDatabase().rawQuery(
                "SELECT id, nombre, semestre, cal_normal, cal_repeticion, cal_especial "
                        + "FROM " + TABLA + " ORDER BY semestre, nombre COLLATE NOCASE", null);
        try {
            while (c.moveToNext()) lista.add(desdeCursor(c));
        } finally {
            c.close();
        }
        return lista;
    }

    /** Promedio general de las calificaciones finales; null si no hay calificaciones. */
    public Double promedioGeneral() {
        double suma = 0;
        int n = 0;
        for (Materia m : listar()) {
            Double f = m.calFinal();
            if (f != null) {
                suma += f;
                n++;
            }
        }
        return n == 0 ? null : suma / n;
    }

    /** Devuelve, por semestre, {semestre, promedio, materias con calificación}. */
    public List<double[]> promediosPorSemestre() {
        Map<Integer, double[]> acum = new TreeMap<>();
        for (Materia m : listar()) {
            Double f = m.calFinal();
            if (f == null) continue;
            double[] a = acum.get(m.semestre);
            if (a == null) {
                a = new double[]{0, 0};
                acum.put(m.semestre, a);
            }
            a[0] += f;
            a[1] += 1;
        }
        List<double[]> res = new ArrayList<>();
        for (Map.Entry<Integer, double[]> e : acum.entrySet()) {
            double[] a = e.getValue();
            res.add(new double[]{e.getKey(), a[0] / a[1], a[1]});
        }
        return res;
    }
}
