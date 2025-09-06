package ar.edu.utn.frba.dds.domain.csv.ModoExportacion;

import java.io.File;

/**
 * Busca el siguiente numero disponible para el nombre de archivo.
 * Nunca anexa, siempre crea un archivo nuevo (sobrescribe).
 */
public class ModoNumerar implements ModoExportacion {
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

    int contador = 1;
    File nuevoArchivo;
    String nuevoPath;

    do {
      String nuevoNombre = String.format("%s_%d%s", baseName, contador, extension);
      nuevoPath = (parentDir == null) ? nuevoNombre : new File(parentDir, nuevoNombre).getPath();
      nuevoArchivo = new File(nuevoPath);
      contador++;
    } while (nuevoArchivo.exists());

    return nuevoPath;
  }

  @Override
  public boolean debeAnexar() {
    return false;
  }
}
