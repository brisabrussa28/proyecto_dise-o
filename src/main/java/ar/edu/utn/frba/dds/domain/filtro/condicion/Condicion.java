package ar.edu.utn.frba.dds.domain.filtro.condicion;

import ar.edu.utn.frba.dds.domain.hecho.Hecho;

public abstract class Condicion {
    /**
     * Método abstracto que cada tipo de condición debe implementar para
     * determinar si un Hecho cumple con el criterio.
     *
     * @param hecho El Hecho a evaluar.
     * @return true si el Hecho cumple la condición, false en caso contrario.
     */
    public abstract boolean evaluar(Hecho hecho);
}
