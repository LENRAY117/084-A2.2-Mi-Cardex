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
    private static final int DB_VERSION = 7;
    private static final String TABLA = "materias";
    private static final String TABLA_SEMESTRES = "semestres";

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
        crearTablaSemestres(db);
        insertarMateriasSemestre2(db);
        insertarMateriasSemestre3(db);
        insertarMateriasSemestre4(db);
        insertarMateriasSemestre6(db);
        insertarMateriasSemestre7(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 7) {
            crearTablaSemestres(db);
        }
        if (oldVersion < 2) {
            insertarMateriasSemestre2(db);
        }
        if (oldVersion < 3) insertarMateriasSemestre3(db);
        if (oldVersion < 4) insertarMateriasSemestre4(db);
        if (oldVersion < 5) insertarMateriasSemestre6(db);
        if (oldVersion < 6) insertarMateriasSemestre7(db);
        if (oldVersion < 7) {
            db.execSQL("INSERT OR IGNORE INTO " + TABLA_SEMESTRES + " (numero) "
                    + "SELECT DISTINCT semestre FROM " + TABLA);
        }
    }

    private void crearTablaSemestres(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLA_SEMESTRES + " ("
                + "numero INTEGER PRIMARY KEY)");
    }

    private void insertarMateriasSemestre2(SQLiteDatabase db) {
        insertarMateriasSemestre(db, 2, new String[][]{
                {"CALC.INTEGRAL", "75"},
                {"PROG. ORIEN. A OBJ.", "95"},
                {"CONTAB.FINANC.", "94"},
                {"QUIMICA", "78"},
                {"ALG.LINEAL", "92"},
                {"FISICA GRAL.", "70"},
                {"DESARROLLO SUSTENTABLE", "100"}
        });
    }

    private void insertarMateriasSemestre3(SQLiteDatabase db) {
        insertarMateriasSemestre(db, 3, new String[][]{
                {"CALC.VECTORIAL", "75"},
                {"TALLER DE SIST.OPERA", "82"},
                {"INV.DE OPERAC.", "87"},
                {"EST.DATOS", "90"},
                {"FUND.BASES.DATOS", "100"},
                {"PRINC.ELEC.APLIC.DIG", "73"},
                {"PROB.Y ESTAD.", "85"}
        });
    }

    private void insertarMateriasSemestre4(SQLiteDatabase db) {
        insertarMateriasSemestre(db, 4, new String[][]{
                {"METOD.NUMERICOS", "99"},
                {"TALLER.BASES.DATOS", "100"},
                {"TOP.AVANZ.DE PROG.", "87"},
                {"SIMULACION", "88"},
                {"ARQ.DE COMPUTADORAS", "80"},
                {"FUND. DE TELEC.", "80"}
        });
    }

    private void insertarMateriasSemestre6(SQLiteDatabase db) {
        insertarMateriasSemestre(db, 6, new String[][]{
                {"LENGUAJES Y AUT, II", "84"},
                {"CONM.ENRUT.RED.DATOS", "92"},
                {"CULT.EMPRES.", "100"},
                {"ING.DE SOFTWARE", "100"},
                {"LENGUAJES DE INTERFAZ", "78"},
                {"GRAFICACION", "97"}
        });
    }

    private void insertarMateriasSemestre7(SQLiteDatabase db) {
        insertarMateriasSemestre(db, 7, new String[][]{
                {"PRO. PROY. Y PLNS. TECNOLOGICOS", "93"},
                {"E-BUSINESS", "95"},
                {"TALL.INVEST.I", "88"},
                {"ADMON.DE REDES", "98"},
                {"PROG. LOG. Y FUNCIONAL", "89"},
                {"SISTEMAS PROGR.", "94"},
                {"GEST. PROY.SOFT.", "81"},
                {"ACTIVIDADES COMPLEMENTARIAS", "100"}
        });
    }

    private void insertarMateriasSemestre(SQLiteDatabase db, int semestre, String[][] materias) {
        registrarSemestre(db, semestre);

        for (String[] datos : materias) {
            Cursor c = db.rawQuery(
                    "SELECT 1 FROM " + TABLA
                            + " WHERE semestre = ? AND nombre = ? COLLATE NOCASE LIMIT 1",
                    new String[]{String.valueOf(semestre), datos[0]});
            boolean existe;
            try {
                existe = c.moveToFirst();
            } finally {
                c.close();
            }

            if (!existe) {
                ContentValues valores = new ContentValues();
                valores.put("nombre", datos[0]);
                valores.put("semestre", semestre);
                valores.put("cal_normal", Double.parseDouble(datos[1]));
                db.insertOrThrow(TABLA, null, valores);
            }
        }
    }

    private void registrarSemestre(SQLiteDatabase db, int semestre) {
        ContentValues valores = new ContentValues();
        valores.put("numero", semestre);
        db.insertWithOnConflict(TABLA_SEMESTRES, null, valores,
                SQLiteDatabase.CONFLICT_IGNORE);
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
        SQLiteDatabase db = getWritableDatabase();
        long id = db.insert(TABLA, null, valores(m));
        if (id != -1) registrarSemestre(db, m.semestre);
        return id;
    }

    public int actualizar(Materia m) {
        SQLiteDatabase db = getWritableDatabase();
        int actualizadas = db.update(TABLA, valores(m), "id = ?",
                new String[]{String.valueOf(m.id)});
        if (actualizadas > 0) registrarSemestre(db, m.semestre);
        return actualizadas;
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

    public List<Integer> listarSemestres() {
        List<Integer> semestres = new ArrayList<>();
        Cursor c = getReadableDatabase().rawQuery(
                "SELECT numero FROM " + TABLA_SEMESTRES + " ORDER BY numero", null);
        try {
            while (c.moveToNext()) semestres.add(c.getInt(0));
        } finally {
            c.close();
        }
        return semestres;
    }

    public boolean agregarSemestre(int semestre) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues valores = new ContentValues();
        valores.put("numero", semestre);
        return db.insertWithOnConflict(TABLA_SEMESTRES, null, valores,
                SQLiteDatabase.CONFLICT_IGNORE) != -1;
    }

    public List<Materia> listarPorSemestre(int semestre) {
        List<Materia> lista = new ArrayList<>();
        Cursor c = getReadableDatabase().rawQuery(
                "SELECT id, nombre, semestre, cal_normal, cal_repeticion, cal_especial "
                        + "FROM " + TABLA + " WHERE semestre = ? ORDER BY nombre COLLATE NOCASE",
                new String[]{String.valueOf(semestre)});
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
