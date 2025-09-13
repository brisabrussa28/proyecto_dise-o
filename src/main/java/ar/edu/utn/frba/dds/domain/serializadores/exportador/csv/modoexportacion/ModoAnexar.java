package ar.edu.utn.frba.dds.domain.serializadores.exportador.csv.modoexportacion;

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
