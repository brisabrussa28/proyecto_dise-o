package ar.edu.utn.frba.dds.domain.filtro.condiciones.condicion;

import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Condición genérica que utiliza reflexión para comparar un campo de un Hecho.
 * Esto evita tener que crear una clase de condición por cada campo a filtrar.
 */
public class CondicionGenerica extends Condicion {
  private final String campo;
  private final String operador;
  private final Object valor;

  public CondicionGenerica(String campo, String operador, Object valor) {
    this.campo = campo;
    this.operador = operador;
    this.valor = valor;
  }

  @Override
  public boolean evaluar(Hecho hecho) {
    try {
      // Construye el nombre del mét0do getter (ej: "estado" -> "getEstado")
      // Solo usa camel case en la primera letra. No considera casos especiales. Cuidadini bananini.
      String nombreMetodoGetter = "get" + campo.substring(0, 1)
                                               .toUpperCase() + campo.substring(1);
      Method getter = Hecho.class.getMethod(nombreMetodoGetter);
      Object valorHecho = getter.invoke(hecho);

      if (valorHecho == null) {
        return false;
      }

      // Realiza la comparación según el operador
      switch (operador.toUpperCase()) {
        case "IGUAL":
          return valorHecho.equals(valor);
        case "DISTINTO":
          return !valorHecho.equals(valor);
        case "MAYOR_QUE":
          if (valorHecho instanceof Comparable) {
            return ((Comparable) valorHecho).compareTo(valor) > 0;
          }
          return false;
        case "MENOR_QUE":
          if (valorHecho instanceof Comparable) {
            return ((Comparable) valorHecho).compareTo(valor) < 0;
          }
          return false;
        default:
          throw new IllegalArgumentException("Operador no soportado: " + operador);
      }
    } catch (Exception e) {
      // Manejo de errores si el campo no existe o hay problemas de invocación.
      e.printStackTrace();
      return false;
    }
  }

  @Override
  public Map<String, Object> aMapa() {
    Map<String, Object> mapa = new LinkedHashMap<>();
    mapa.put("campo", this.campo);
    mapa.put("operador", this.operador);

    if (this.valor instanceof LocalDateTime) {
      mapa.put("valor", ((LocalDateTime) this.valor).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
    } else if (this.valor instanceof Enum) {
      mapa.put("valor", ((Enum<?>) this.valor).name());
    } else {
      mapa.put("valor", this.valor);
    }

    return mapa;
  }

}
