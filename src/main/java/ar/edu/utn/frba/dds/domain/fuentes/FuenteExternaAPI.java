package ar.edu.utn.frba.dds.domain.fuentes;

import ar.edu.utn.frba.dds.domain.fuentes.apis.FuenteAdapter;
import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import java.util.Collections;
import java.util.List;

/**
 * Clase nueva para apis externas. No sabe si se conecta vía Retrofit o de forma manual.
 * Delega la responsabilidad de la consulta a un adaptador inyectado.
 * Permite la inyección de diferentes adaptadores para distintas APIs.
 * Hereda de FuenteDeCopiaLocal para mantener la funcionalidad de caché.
 */
public class FuenteExternaAPI extends FuenteDeCopiaLocal {

  private final FuenteAdapter adaptador;

  public FuenteExternaAPI(String nombre, FuenteAdapter adaptador, String jsonFilePathParaCopias) {
    super(nombre, jsonFilePathParaCopias);
    this.adaptador = adaptador;
  }

  @Override
  protected List<Hecho> consultarNuevosHechos() {
    try {
      // Delega la responsabilidad de la consulta al adaptador inyectado.
      return adaptador.consultarHechos();
    } catch (Exception e) {
      System.err.println("Error al consultar la fuente externa '" + this.getNombre() + "': " + e.getMessage());
      e.printStackTrace(); // Es buena idea loggear el stack trace para debug
      return Collections.emptyList();
    }
  }
}
