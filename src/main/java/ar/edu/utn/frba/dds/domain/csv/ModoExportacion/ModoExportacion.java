package ar.edu.utn.frba.dds.domain.csv.ModoExportacion;

public interface ModoExportacion {
  /**
   * Obtiene la ruta final del archivo.
   * @param pathOriginal La ruta del archivo original.
   * @return La ruta final para el archivo.
   */
  String obtenerPathFinal(String pathOriginal);

  /**
   * Determina si debe anexar o sobreescribir segun el modo.
   * @return true si debe anexar, false si debe sobrescribir.
   */
  boolean debeAnexar();
}
