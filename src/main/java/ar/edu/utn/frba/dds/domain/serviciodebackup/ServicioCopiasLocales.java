package ar.edu.utn.frba.dds.domain.serviciodebackup;

import ar.edu.utn.frba.dds.domain.serializadores.json.Exportador.ExportadorJson;
import ar.edu.utn.frba.dds.domain.serializadores.json.Lector.LectorJson;
import com.fasterxml.jackson.core.type.TypeReference;
import java.util.List;

public class ServicioCopiasLocales {

  private final String jsonFilePath;
  private final LectorJson lectorJson;
  private final ExportadorJson exportadorJson;

  public ServicioCopiasLocales(String jsonFilePath) {
    this.jsonFilePath = jsonFilePath;
    this.lectorJson = new LectorJson();
    this.exportadorJson = new ExportadorJson();
  }

  public <T> void guardarCopiaLocalJson(List<T> objetos) {
    exportadorJson.guardarCopiaLocalJson(jsonFilePath, objetos);
  }

  public <T> List<T> cargarCopiaLocalJson(TypeReference<List<T>> typeReference) {
    return lectorJson.cargarCopiaLocalJson(jsonFilePath, typeReference);
  }

  public String getJsonFilePath() {
    return jsonFilePath;
  }
}
