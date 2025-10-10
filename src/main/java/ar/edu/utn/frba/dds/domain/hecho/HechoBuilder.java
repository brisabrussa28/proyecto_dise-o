package ar.edu.utn.frba.dds.domain.hecho;

import ar.edu.utn.frba.dds.domain.geolocalizacion.GeoApi;
import ar.edu.utn.frba.dds.domain.hecho.etiqueta.Etiqueta;
import ar.edu.utn.frba.dds.domain.info.PuntoGeografico;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Implementación del Patrón Builder para la creación de Hechos.
 * Permite la resolución asíncrona de datos geográficos.
 */
public class HechoBuilder {
  private String titulo;
  private String descripcion;
  private String categoria;
  private String direccion;
  private String provincia;
  private PuntoGeografico ubicacion;
  private LocalDateTime fechaSuceso;
  private LocalDateTime fechaCarga = LocalDateTime.now();
  private Origen fuenteOrigen;
  private List<Etiqueta> etiquetas = new ArrayList<>();
  private GeoApi geoApi; // Servicio para consultas geográficas

  public HechoBuilder copiar(Hecho original) {
    this.titulo = original.getTitulo();
    this.descripcion = original.getDescripcion();
    this.categoria = original.getCategoria();
    this.direccion = original.getDireccion();
    this.provincia = original.getProvincia();
    this.ubicacion = original.getUbicacion();
    this.fechaSuceso = original.getFechasuceso();
    this.fechaCarga = original.getFechacarga();
    this.fuenteOrigen = original.getOrigen();
    this.etiquetas = new ArrayList<>(original.getEtiquetas());
    return this;
  }

  public HechoBuilder conTitulo(String titulo) {
    this.titulo = titulo;
    return this;
  }

  public HechoBuilder conDescripcion(String descripcion) {
    this.descripcion = descripcion;
    return this;
  }

  public HechoBuilder conCategoria(String categoria) {
    this.categoria = categoria;
    return this;
  }

  public HechoBuilder conDireccion(String direccion) {
    this.direccion = direccion;
    return this;
  }

  public HechoBuilder conProvincia(String provincia) {
    this.provincia = provincia;
    return this;
  }

  public HechoBuilder conUbicacion(PuntoGeografico ubicacion) {
    this.ubicacion = ubicacion;
    return this;
  }

  public HechoBuilder conFechaSuceso(LocalDateTime fechaSuceso) {
    this.fechaSuceso = fechaSuceso;
    return this;
  }

  public HechoBuilder conFechaCarga(LocalDateTime fechaCarga) {
    this.fechaCarga = fechaCarga;
    return this;
  }

  public HechoBuilder conFuenteOrigen(Origen fuenteOrigen) {
    this.fuenteOrigen = fuenteOrigen;
    return this;
  }

  public HechoBuilder conEtiquetas(Collection<Etiqueta> etiquetas) {
    if (etiquetas != null) {
      this.etiquetas.addAll(etiquetas);
    }
    return this;
  }

  public HechoBuilder conEtiquetas(List<String> etiquetas) {
    if (etiquetas != null) {
      etiquetas.stream()
               .filter(nombre -> nombre != null && !nombre.isBlank())
               .map(Etiqueta::new)
               .forEach(this.etiquetas::add);
    }
    return this;
  }

  public HechoBuilder conOrigen(Origen origen) {
    if (origen != null) {
      this.fuenteOrigen = origen;
    }
    return this;
  }

  /**
   * Inyecta el servicio de geolocalización que se utilizará para completar datos.
   * @param geoApi La implementación de GeoApi a utilizar.
   * @return El propio builder para encadenar llamadas.
   */
  public HechoBuilder conGeoApi(GeoApi geoApi) {
    this.geoApi = geoApi;
    return this;
  }

  /**
   * Construye un objeto Hecho de forma asíncrona.
   * Si falta información geográfica, la buscará utilizando la GeoApi proporcionada.
   * @return Un CompletableFuture que se completará con la instancia de Hecho.
   */
  public CompletableFuture<Hecho> build() {
    try {
      validarCampos();
    } catch (IllegalStateException e) {
      return CompletableFuture.failedFuture(e);
    }

    if (provincia == null && ubicacion != null) {
      if (geoApi == null) {
        return CompletableFuture.failedFuture(
            new IllegalStateException("Se necesita un GeoApi para buscar la provincia por ubicación."));
      }
      return geoApi.obtenerProvincia(ubicacion.getLatitud(), ubicacion.getLongitud())
                   .thenApply(provinciaObtenida -> {
                     this.provincia = provinciaObtenida;
                     return construirHechoFinal();
                   });
    }

    // TODO: Se podría implementar la lógica inversa: buscar ubicación a partir de provincia/dirección.

    return CompletableFuture.completedFuture(construirHechoFinal());
  }

  private void validarCampos() {
    if (titulo == null || titulo.isBlank()) {
      throw new IllegalStateException("El título es obligatorio para crear un Hecho.");
    }
    if (fechaSuceso == null) {
      throw new IllegalStateException("La fecha del suceso es obligatoria para crear un Hecho.");
    }
    if (fechaCarga == null) {
      throw new IllegalStateException("La fecha de carga es obligatoria para crear un Hecho.");
    }
    if (fechaSuceso.isAfter(fechaCarga)) {
      throw new IllegalStateException("La fecha del suceso no puede ser posterior a la fecha de carga.");
    }
    if (fechaSuceso.isAfter(LocalDateTime.now())) {
      throw new IllegalStateException("La fecha del suceso no puede ser una fecha futura.");
    }
    if (fechaCarga.isAfter(LocalDateTime.now())) {
      throw new IllegalStateException("La fecha de carga no puede ser una fecha futura.");
    }
  }

  private Hecho construirHechoFinal() {
    return new Hecho(
        titulo,
        descripcion,
        categoria,
        direccion,
        provincia,
        ubicacion,
        fechaSuceso,
        fechaCarga,
        fuenteOrigen,
        etiquetas);
  }
}

