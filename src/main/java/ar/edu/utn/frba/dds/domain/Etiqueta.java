package ar.edu.utn.frba.dds.domain;

import ar.edu.utn.frba.dds.domain.exceptions.EtiquetaInvalidaException;

public class Etiqueta {
  private final String valor;

  public Etiqueta(String valor) {
    if (valor == null || valor.isBlank())
      throw new EtiquetaInvalidaException("Etiqueta vacía o inválida");
    this.valor = valor.trim().toLowerCase(); // normalización básica
  }

  public String getValor() {
    return valor;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Etiqueta e)) return false;
    return valor.equals(e.valor);
  }

  @Override
  public int hashCode() {
    return valor.hashCode();
  }

  @Override
  public String toString() {
    return valor;
  }
}
