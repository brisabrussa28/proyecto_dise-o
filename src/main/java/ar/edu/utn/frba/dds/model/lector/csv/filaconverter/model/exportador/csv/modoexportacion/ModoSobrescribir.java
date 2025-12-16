package ar.edu.utn.frba.dds.model.lector.csv.filaconverter.model.exportador.csv.modoexportacion;

import ar.edu.utn.frba.dds.model.exportador.csv.modoexportacion.ModoExportacion;

/**
 * Sobrescribe el archivo viejo con el nuevo o lo crea si no existe.
 */
public class ModoSobrescribir implements ModoExportacion {
  @Override
  public String obtenerPathFinal(String pathOriginal) {
    return pathOriginal;
  }

  @Override
  public boolean debeAnexar() {
    return false;
  }
}
