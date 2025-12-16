package ar.edu.utn.frba.dds.model.lector.csv.filaconverter.model.hecho;

import ar.edu.utn.frba.dds.model.hecho.Estado;
import ar.edu.utn.frba.dds.model.hecho.Hecho;
import ar.edu.utn.frba.dds.model.hecho.etiqueta.Etiqueta;
import ar.edu.utn.frba.dds.model.info.PuntoGeografico;
import ar.edu.utn.frba.dds.model.usuario.Usuario;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Gestiona el proceso de edición de una instancia de Hecho.
 * Acumula los cambios deseados y los aplica todos juntos en el método finalizar().
 * Se asegura de que el objeto Hecho no quede en un estado inconsistente.
 */
public class HechoEditor {
  public final Hecho hecho;

  // Campos para almacenar los cambios pendientes. Se usan Optionals para representar
  // de forma explícita que un cambio puede estar presente o no.
  private Optional<String> titulo = Optional.empty();
  private Optional<String> descripcion = Optional.empty();
  private Optional<String> categoria = Optional.empty();
  private Optional<String> direccion = Optional.empty();
  private Optional<PuntoGeografico> ubicacion = Optional.empty();
  private Optional<List<Etiqueta>> etiquetas = Optional.empty();
  private Optional<LocalDateTime> fechaSuceso = Optional.empty();

  /**
   * Crea un editor para un Hecho específico.
   *
   * @param hechoAEditar La instancia de Hecho que se va a modificar.
   * @throws IllegalStateException si el Hecho ya no es editable.
   */
  public HechoEditor(Hecho hechoAEditar, Usuario usuarioEditor) {
    if (!hechoAEditar.esEditable(usuarioEditor)) {
      throw new IllegalStateException(
          "El hecho ya no puede ser editado. Pasó más de una semana desde su carga.");
    }
    this.hecho = hechoAEditar;
  }

  // Los métodos "con..." de edicion
  public HechoEditor conTitulo(String titulo) {
    this.titulo = Optional.ofNullable(titulo);
    return this;
  }

  public HechoEditor conDescripcion(String descripcion) {
    this.descripcion = Optional.ofNullable(descripcion);
    return this;
  }

  public HechoEditor conCategoria(String categoria) {
    this.categoria = Optional.ofNullable(categoria);
    return this;
  }

  public HechoEditor conDireccion(String direccion) {
    this.direccion = Optional.ofNullable(direccion);
    return this;
  }

  public HechoEditor conUbicacion(PuntoGeografico ubicacion) {
    this.ubicacion = Optional.ofNullable(ubicacion);
    return this;
  }

  public HechoEditor conEtiquetas(List<Etiqueta> etiquetas) {
    this.etiquetas = Optional.ofNullable(etiquetas);
    return this;
  }

  public HechoEditor conFechaSuceso(LocalDateTime fechaSuceso) {
    this.fechaSuceso = Optional.ofNullable(fechaSuceso);
    return this;
  }

  /**
   * Finaliza el proceso de edición, aplicando todos los cambios sobre el Hecho original.
   *
   * @return El Hecho modificado.
   */
  public Hecho finalizar() {
    // Se utiliza el método ifPresent() de Optional para aplicar cada cambio
    // solo si fue especificado. Esto resulta en un código muy limpio y declarativo.
    titulo.ifPresent(hecho::setTitulo);
    descripcion.ifPresent(hecho::setDescripcion);
    categoria.ifPresent(hecho::setCategoria);
    direccion.ifPresent(hecho::setDireccion);
    ubicacion.ifPresent(hecho::setUbicacion);
    etiquetas.ifPresent(hecho::setEtiquetas);
    fechaSuceso.ifPresent(hecho::setFechasuceso);

    // Una vez aplicados todos los cambios, se actualiza el estado.
    hecho.setEstado(Estado.EDITADO);
    return this.hecho;
  }
}

