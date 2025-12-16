package ar.edu.utn.frba.dds.model.lector.csv.filaconverter.model.filtro.condiciones;

import ar.edu.utn.frba.dds.model.lector.csv.filaconverter.model.hecho.Hecho;
import java.util.function.Predicate;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Transient;

/**
 * Condición basada en un Predicate.
 * Esta clase ES UNA ENTIDAD, pero su lógica de predicado NO SE GUARDA en la BD.
 * El campo 'predicado' es transitorio y debe ser asignado en tiempo de ejecución.
 */
@Entity
@DiscriminatorValue("Cond_predicado")
public class CondicionPredicado extends Condicion {

  @Transient
  private Predicate<Hecho> predicado;

  public CondicionPredicado() {
  }

  public CondicionPredicado(Predicate<Hecho> predicado) {
    this.predicado = predicado;
  }

  @Override
  public boolean evaluar(Hecho hecho) {
    // Si la entidad se cargó desde la BD, el predicado será null.
    if (predicado == null) {
      return false; // O lanzar una excepción, ya que la lógica no está definida.
    }
    return predicado.test(hecho);
  }

  public Predicate<Hecho> getPredicado() {
    return predicado;
  }

  public void setPredicado(Predicate<Hecho> predicado) {
    this.predicado = predicado;
  }
}