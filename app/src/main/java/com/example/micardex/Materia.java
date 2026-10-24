package com.example.micardex;

import java.util.Locale;

/** Una materia del cárdex con sus tres posibles calificaciones. */
public class Materia {
    public long id = -1;
    public String nombre;
    public int semestre;
    public Double normal;      // calificación en curso normal
    public Double repeticion;  // calificación en repetición (null si no aplica)
    public Double especial;    // calificación en especial (null si no aplica)

    /**
     * Calificación final de la materia: la última oportunidad registrada.
     * Especial > Repetición > Normal.
     */
    public Double calFinal() {
        if (especial != null) return especial;
        if (repeticion != null) return repeticion;
        return normal;
    }

    public static String fmt(Double d) {
        if (d == null) return "—";
        if (d == Math.floor(d)) return String.valueOf(d.intValue());
        return String.format(Locale.US, "%.1f", d);
    }

    public String resumen() {
        StringBuilder sb = new StringBuilder();
        sb.append("Semestre ").append(semestre);
        sb.append("  ·  Normal: ").append(fmt(normal));
        if (repeticion != null) sb.append("  ·  Rep: ").append(fmt(repeticion));
        if (especial != null) sb.append("  ·  Esp: ").append(fmt(especial));
        sb.append("  ·  Final: ").append(fmt(calFinal()));
        return sb.toString();
    }
}
