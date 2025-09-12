package ar.edu.utn.frba.dds.domain.serializadores.exportador;

import ar.edu.utn.frba.dds.domain.serializadores.exportador.csv.ExportadorCSV;
import ar.edu.utn.frba.dds.domain.serializadores.exportador.csv.ModoExportacion.ModoAnexar;
import ar.edu.utn.frba.dds.domain.serializadores.exportador.csv.ModoExportacion.ModoExportacion;
import ar.edu.utn.frba.dds.domain.serializadores.exportador.csv.ModoExportacion.ModoNumerar;
import ar.edu.utn.frba.dds.domain.serializadores.exportador.csv.ModoExportacion.ModoSobrescribir;
import ar.edu.utn.frba.dds.domain.serializadores.exportador.csv.ModoExportacion.ModoTimestamp;
import ar.edu.utn.frba.dds.domain.serializadores.exportador.json.ExportadorJson;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * Factory dedicada a la creación de instancias de Exportador
 * a partir de una configuración JSON.
 */
public class ExportadorFactory {

  /**
   * Crea un Exportador genérico a partir de su nodo de configuración JSON.
   * @param exportadorNode El nodo JSON con la configuración del exportador.
   * @return Una instancia de Exportador configurada.
   */
  public <T> Exportador<T> create(JsonNode exportadorNode) {
    String formato = exportadorNode.path("formato").asText().toUpperCase();
    switch (formato) {
      case "JSON":
        return new ExportadorJson<>();
      case "CSV":
        char separador = exportadorNode.path("separador").asText(",").charAt(0);
        char quote = exportadorNode.path("quote").asText("\"").charAt(0);
        String modoStr = exportadorNode.path("modo").asText("SOBREESCRIBIR").toUpperCase();

        ModoExportacion modo;
        switch (modoStr) {
          case "ANEXAR":
            modo = new ModoAnexar();
            break;
          case "SOBREESCRIBIR":
            modo = new ModoSobrescribir();
            break;
          case "NUMERAR":
            modo = new ModoNumerar();
            break;
          case "TIMESTAMP":
            modo = new ModoTimestamp();
            break;
          default:
            throw new IllegalArgumentException("Modo de exportación no válido: " + modoStr);
        }
        return new ExportadorCSV<>(separador, quote, modo);
      default:
        throw new IllegalArgumentException("Formato de exportador no soportado: " + formato);
    }
  }
}

