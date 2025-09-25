package ar.edu.utn.frba.dds.domain.filtro.condiciones;

import ar.edu.utn.frba.dds.domain.hecho.Hecho;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

@Entity
@DiscriminatorValue("Cond_generica")
public class CondicionGenerica extends Condicion {
  private String campo;

  @Enumerated(EnumType.STRING)
  private Operador operador;

  private String valor;

  public CondicionGenerica() {
  }

  public CondicionGenerica(String campo, Operador operador, Object valor) {
    this.campo = campo;
    this.operador = operador;
    this.valor = String.valueOf(valor);
  }

  @Override
  public boolean evaluar(Hecho hecho) {
    try {
      String nombreMetodoGetter = "get" + campo.substring(0, 1).toUpperCase() + campo.substring(1);
      Method getter = Hecho.class.getMethod(nombreMetodoGetter);
      Object valorHecho = getter.invoke(hecho);

      if (valorHecho == null) {
        return false;
      }

      switch (operador) {
        case IGUAL:
          return String.valueOf(valorHecho).equals(this.valor);
        case DISTINTO:
          return !String.valueOf(valorHecho).equals(this.valor);

        case MAYOR_QUE:
          if (valorHecho instanceof Comparable) {
            return comparar(valorHecho) > 0;
          }
          return false;
        case MENOR_QUE:
          if (valorHecho instanceof Comparable) {
            return comparar(valorHecho) < 0;
          }
          return false;

        default:
          throw new IllegalArgumentException("Operador no soportado: " + operador);
      }
    } catch (Exception e) {
      e.printStackTrace();
      return false;
    }
  }

  /**
   * Nuevo método helper para manejar la comparación de tipos de forma segura.
   */
  private int comparar(Object valorHecho) {
    if (valorHecho instanceof LocalDateTime) {
      LocalDateTime fechaHecho = (LocalDateTime) valorHecho;
      LocalDateTime fechaValor = LocalDateTime.parse(this.valor); // Convierte el String a LocalDateTime
      return fechaHecho.compareTo(fechaValor);
    }

    if (valorHecho instanceof Number) {
      Double numeroHecho = ((Number) valorHecho).doubleValue();
      Double numeroValor = Double.parseDouble(this.valor); // Convierte el String a Double
      return numeroHecho.compareTo(numeroValor);
    }

    // Comparación genérica para otros tipos (ej. String)
    return ((Comparable) valorHecho).compareTo(this.valor);
  }


  // --- Getters y Setters ---
  public String getCampo() {
    return campo;
  }

  public void setCampo(String campo) {
    this.campo = campo;
  }

  public Operador getOperador() {
    return operador;
  }

  public void setOperador(Operador operador) {
    this.operador = operador;
  }

  public String getValor() {
    return valor;
  }

  public void setValor(String valor) {
    this.valor = valor;
  }
}