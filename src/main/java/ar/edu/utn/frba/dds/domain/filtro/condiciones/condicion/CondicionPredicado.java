package ar.edu.utn.frba.dds.domain.filtro.condiciones.condicion;

import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Predicate;

public class CondicionPredicado extends Condicion {
  private final Predicate<Hecho> predicado;

  public CondicionPredicado(Predicate<Hecho> predicado) {
    this.predicado = predicado;
  }

  @Override
  public boolean evaluar(Hecho hecho) {
    return predicado.test(hecho);
  }

  @Override
  public Map<String, Object> unMap() {
    Map<String, Object> mapa = new LinkedHashMap<>();
    mapa.put("type", "PREDICADO");
    mapa.put("descripcion", "Esta condición no puede ser serializada a JSON porque contiene código (un Predicate).");
    return mapa;
  }
}