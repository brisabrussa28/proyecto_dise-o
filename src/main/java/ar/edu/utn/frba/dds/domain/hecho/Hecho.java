package ar.edu.utn.frba.dds.domain.hecho;
import ar.edu.utn.frba.dds.domain.info.PuntoGeografico;
import ar.edu.utn.frba.dds.domain.coleccion.Coleccion;
import ar.edu.utn.frba.dds.domain.origen.Origen;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Hecho.
 * */
public class Hecho {
  boolean vigencia;
  private final String titulo;
  private final String descripcion;
  private final String categoria;
  private final String direccion;
  private final PuntoGeografico ubicacion;
  private final LocalDateTime fechaSuceso;
  private final LocalDateTime fechaCarga;
  private Origen fuenteOrigen;
  private final List<String> etiquetas;
  private final UUID id;

  /**
   * Hecho.
   * */
  public Hecho(
      String titulo,
      String descripcion,
      String categoria,
      String direccion,
      PuntoGeografico ubicacion,
      LocalDateTime fechaSuceso,
      LocalDateTime fechaCarga,
      Origen fuenteOrigen,
      List<String> etiquetas
  ) {
    this.titulo = titulo;
    this.descripcion = descripcion;
    this.categoria = categoria;
    this.ubicacion = ubicacion;
    this.direccion = direccion;
    this.fechaSuceso = fechaSuceso;
    this.fechaCarga = fechaCarga;
    this.fuenteOrigen = fuenteOrigen;
    this.etiquetas = etiquetas;
    this.id = UUID.randomUUID();
    this.vigencia = true;
  }

  public UUID getId() {
    return id;
  }

  public String getTitulo() {
    return titulo;
  }

  public String getDescripcion() {
    return descripcion;
  }

  public String getCategoria() {
    return categoria;
  }

  public String getDireccion() {
    return direccion;
  }

  public PuntoGeografico getUbicacion() {
    return ubicacion;
  }

  public LocalDateTime getFechaSuceso() {
    return fechaSuceso;
  }

  public LocalDateTime getFechaCarga() {
    return fechaCarga;
  }


  public Origen getOrigen() {
    return fuenteOrigen;
  }

  public void setOrigen(Origen origen) {
    this.fuenteOrigen = origen;
  }

  public boolean esDeCategoria(String categoria) {
    return this.categoria.equals(categoria);
  }

  public boolean tieneEtiqueta(String unaEtiqueta) {
    return this.etiquetas.contains(unaEtiqueta);
  }

  public boolean esDeTitulo(String unTitulo) {
    return this.titulo.equals(unTitulo);
  }

  public boolean sucedioEn(String unaDireccion) {
    return this.direccion.equals(unaDireccion);
  }

  public boolean esDeFecha(LocalDateTime unaFecha) {
    return this.fechaSuceso.equals(unaFecha);
  }

  public boolean seCargoEl(LocalDateTime unaFecha) {
    return this.fechaCarga.getYear() == unaFecha.getYear()
        && this.fechaCarga.getMonth() == unaFecha.getMonth()
        && this.fechaCarga.getDayOfMonth() == unaFecha.getDayOfMonth();
  }

  public boolean seCargoAntesDe(LocalDateTime unaFecha) {
    return this.fechaCarga.isBefore(unaFecha);
  }

  public boolean esDeOrigen(Origen unaOrigen) {
    return this.fuenteOrigen.equals(unaOrigen);
  }

  public boolean esDeLugar(PuntoGeografico lugar) {
    return this.ubicacion.equals(lugar);
  }

  public List<String> getEtiquetas() {
    return etiquetas;
  }

  boolean perteneceA(Coleccion unaColeccion) {
    return unaColeccion.contieneA(this);
  }

}
