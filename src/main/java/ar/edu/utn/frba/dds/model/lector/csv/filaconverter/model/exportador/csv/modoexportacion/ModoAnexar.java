package ar.edu.utn.frba.dds.model.lector.csv.filaconverter.model.exportador.csv.modoexportacion;

import ar.edu.utn.frba.dds.model.exportador.csv.modoexportacion.ModoExportacion;

/**
 * Anexa los items al final del archivo.
 */
public class ModoAnexar implements ModoExportacion {
  @Override
  public String obtenerPathFinal(String pathOriginal) {
    return pathOriginal;
  }

  @Override
  public boolean debeAnexar() {
    return true;
  }
}
