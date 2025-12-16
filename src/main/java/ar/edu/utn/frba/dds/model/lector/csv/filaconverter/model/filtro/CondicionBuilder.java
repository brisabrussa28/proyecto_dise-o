package ar.edu.utn.frba.dds.model.lector.csv.filaconverter.model.filtro;

import ar.edu.utn.frba.dds.model.filtro.condiciones.Condicion;
import ar.edu.utn.frba.dds.model.filtro.condiciones.CondicionAnd;
import ar.edu.utn.frba.dds.model.filtro.condiciones.CondicionGenerica;
import ar.edu.utn.frba.dds.model.filtro.condiciones.CondicionOr;
import ar.edu.utn.frba.dds.model.filtro.condiciones.CondicionTrue;
import ar.edu.utn.frba.dds.model.filtro.condiciones.Operador;

public class CondicionBuilder {
  private Condicion raiz;

  public CondicionBuilder(Condicion condicionInicial) {
    // Si es null o no hay nada, empezamos con True (base neutra)
    this.raiz = condicionInicial != null ? condicionInicial : new CondicionTrue();
  }

  public CondicionBuilder and(String campo, Operador operador, String valor) {
    CondicionGenerica nueva = new CondicionGenerica(campo, operador, valor);
    return agregarLogica(nueva, true);
  }

  public CondicionBuilder or(String campo, Operador operador, String valor) {
    CondicionGenerica nueva = new CondicionGenerica(campo, operador, valor);
    return agregarLogica(nueva, false);
  }

  private CondicionBuilder agregarLogica(Condicion nueva, boolean esAnd) {
    // 1. Si la raíz actual es True (vacía), la nueva condición la reemplaza directamente.
    if (this.raiz instanceof CondicionTrue) {
      this.raiz = nueva;
      return this;
    }

    // 2. Conectar con AND
    if (esAnd) {
      if (this.raiz instanceof CondicionAnd) {
        // Aplanar: Si ya es un AND, agregamos a la lista existente
        ((CondicionAnd) this.raiz).agregarCondicion(nueva);
      } else {
        // Envolver: Crear nuevo AND con la raíz actual y la nueva
        CondicionAnd nuevoPadre = new CondicionAnd();
        nuevoPadre.agregarCondicion(this.raiz);
        nuevoPadre.agregarCondicion(nueva);
        this.raiz = nuevoPadre;
      }
    }
    // 3. Conectar con OR
    else {
      if (this.raiz instanceof CondicionOr) {
        // Aplanar: Si ya es un OR, agregamos a la lista existente
        ((CondicionOr) this.raiz).agregarCondicion(nueva);
      } else {
        // Envolver: Crear nuevo OR con la raíz actual y la nueva
        CondicionOr nuevoPadre = new CondicionOr();
        nuevoPadre.agregarCondicion(this.raiz);
        nuevoPadre.agregarCondicion(nueva);
        this.raiz = nuevoPadre;
      }
    }
    return this;
  }

  public Condicion build() {
    return this.raiz;
  }
}