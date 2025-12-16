package ar.edu.utn.frba.dds.model.lector.csv.filaconverter.model.exportador.csv.modoexportacion;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Crea un nuevo archivo con un timestamp.
 * Nunca anexa, siempre crea un archivo nuevo (sobrescribe).
 */
public class ModoTimestamp implements ModoExportacion {
  @Override
  public String obtenerPathFinal(String pathOriginal) {
    File file = new File(pathOriginal);
    String parentDir = file.getParent();
    String fileName = file.getName();
    String baseName;
    String extension;

    int dotIndex = fileName.lastIndexOf('.');
    if (dotIndex > 0) {
      baseName = fileName.substring(0, dotIndex);
      extension = fileName.substring(dotIndex);
    } else {
      baseName = fileName;
      extension = "";
    }

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss-SSS");
    String timestamp = LocalDateTime.now()
                                    .format(formatter);
    String newFileName = String.format("%s_%s%s", baseName, timestamp, extension);

    return (parentDir == null) ? newFileName : new File(parentDir, newFileName).getPath();
  }


  @Override
  public boolean debeAnexar() {
    return false;
  }
}
