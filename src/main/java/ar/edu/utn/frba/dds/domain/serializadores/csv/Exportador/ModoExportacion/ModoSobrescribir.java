package ar.edu.utn.frba.dds.domain.serializadores.csv.Exportador.ModoExportacion;

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
