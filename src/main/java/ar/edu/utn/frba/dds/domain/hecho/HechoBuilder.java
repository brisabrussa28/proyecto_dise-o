package ar.edu.utn.frba.dds.domain.hecho;

import ar.edu.utn.frba.dds.domain.hecho.etiqueta.Etiqueta;
import ar.edu.utn.frba.dds.domain.info.PuntoGeografico;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Implementación del Patrón Builder para la creación  Hechos (reduce acoplamiento en especial en CSV).
 */
public class HechoBuilder {
  private String titulo;
  private String descripcion;
  private String categoria;
  private String direccion;
  private String provincia;
  private PuntoGeografico ubicacion;
  private LocalDateTime fechaSuceso;
  private LocalDateTime fechaCarga = LocalDateTime.now(); // Valor por defecto
  private Origen fuenteOrigen;
  private List<Etiqueta> etiquetas = new ArrayList<>();

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
    this.etiquetas = original.getEtiquetas();
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


  /**
   * Construye y devuelve un objeto Hecho a partir de los datos proporcionados.
   *
   * @return una nueva instancia de Hecho.
   * @throws IllegalStateException si los datos obligatorios no se proporcionan o son inválidos.
   */
  public Hecho build() {
    if (titulo == null || titulo.isBlank()) {
      throw new IllegalStateException("El título es obligatorio para crear un Hecho.");
    }

    if (fechaSuceso == null) {
      throw new IllegalStateException("La fecha del suceso es obligatoria para crear un Hecho.");
    }

    if (fechaCarga == null) {
      throw new IllegalStateException("La fecha del suceso es obligatoria para crear un Hecho.");
    }

    if (fechaSuceso.isAfter(fechaCarga)) {
      throw new IllegalStateException(
          "La fecha del suceso no puede ser posterior a la fecha de carga.");
    }

    if (fechaSuceso.isAfter(LocalDateTime.now())) {
      throw new IllegalStateException("La fecha del suceso no puede ser una fecha futura.");
    }

    if (fechaCarga.isAfter(LocalDateTime.now())) {
      throw new IllegalStateException("La fecha de carga no puede ser una fecha futura.");
    }

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
        etiquetas
    );
  }
}
