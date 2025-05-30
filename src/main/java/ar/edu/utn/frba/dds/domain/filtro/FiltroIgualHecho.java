package ar.edu.utn.frba.dds.domain.filtro;

import ar.edu.utn.frba.dds.domain.hecho.Hecho;

import java.util.Objects;

public class FiltroIgualHecho extends Filtro {

  private final Hecho referencia;

  public FiltroIgualHecho(Hecho referencia) {
    this.referencia = referencia;
  }

  @Override
  public boolean cumple(Hecho hecho) {
    if (hecho == null) return false;

    return Objects.equals(referencia.getTitulo(), hecho.getTitulo()) &&
        Objects.equals(referencia.getDescripcion(), hecho.getDescripcion()) &&
        Objects.equals(referencia.getCategoria(), hecho.getCategoria()) &&
        Objects.equals(referencia.getDireccion(), hecho.getDireccion()) &&
        Objects.equals(referencia.getUbicacion(), hecho.getUbicacion()) &&
        Objects.equals(referencia.getFechaSuceso(), hecho.getFechaSuceso()) &&
        Objects.equals(referencia.getFechaCarga(), hecho.getFechaCarga()) &&
        Objects.equals(referencia.getOrigen(), hecho.getOrigen()) &&
        Objects.equals(referencia.getEtiquetas(), hecho.getEtiquetas());
  }
}