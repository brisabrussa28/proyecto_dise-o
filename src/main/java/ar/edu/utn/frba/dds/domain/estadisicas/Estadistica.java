package ar.edu.utn.frba.dds.domain.estadisicas;

public class Estadistica {
  private final String dimension;
  private final Long valor;

  public Estadistica(String dimension, Long valor) {
    this.dimension = dimension;
    this.valor = valor;
  }

  public String getDimension() {
    return dimension;
  }

  public Long getValor() {
    return valor;
  }
}
