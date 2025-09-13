package ar.edu.utn.frba.dds.domain.fuentes.apis;

import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import java.io.IOException;
import java.util.List;

/**
 * Esta interfaz define el contrato para cualquier clase que actúe como
 * un adaptador entre nuestra aplicación y una fuente de datos de hechos externa.
 */
public interface FuenteAdapter {

  /**
   * Consulta la fuente externa y devuelve una lista de Hechos.
   *
   * @return Lista de hechos obtenidos.
   * @throws IOException Si ocurre un error de comunicación.
   */
  List<Hecho> consultarHechos() throws IOException;

  /**
   * Devuelve la configuración del adaptador en formato JSON para poder persistirla.
   *
   * @return Un String con la configuración en formato JSON.
   */
  String getConfiguracionJson();
}
