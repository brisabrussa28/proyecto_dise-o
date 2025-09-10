package ar.edu.utn.frba.dds.domain.fuentes.apis;

import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import java.io.IOException;
import java.util.List;

/**
 * Esta interfaz define el contrato para cualquier clase que actúe como
 * un adaptador entre nuestra aplicación y una fuente de datos de hechos externa.
 * Su única responsabilidad es saber cómo consultar y devolver una lista de Hechos.
 */
public interface FuenteAdapter {
  List<Hecho> consultarHechos() throws IOException;
}
