package ar.edu.utn.frba.dds.domain;

public class ErrorCSV {
  private final int numeroFila;
  private final String mensaje;

  public ErrorCSV(int numeroFila, String mensaje) {
    this.numeroFila = numeroFila;
    this.mensaje = mensaje;
  }

  public int getNumeroFila() {
    return numeroFila;
  }

  public String getMensaje() {
    return mensaje;
  }

  @Override
  public String toString() {
    return "Error en fila " + numeroFila + ": " + mensaje;
  }
}