package ar.edu.utn.frba.dds.domain.estadisicas;


import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;

@Entity
public class Estadistica {
  @Id
  @SequenceGenerator(name = "estadistica_seq", sequenceName = "estadistica_sequence", allocationSize = 1)
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "estadistica_seq")
  Long estadistica_id;

  public final String dimension;
  public final Long valor;

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

  public Long getId() {
    return this.estadistica_id;
  }
}
