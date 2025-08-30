package ar.edu.utn.frba.dds.domain.fuentes;


import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import ar.edu.utn.frba.dds.domain.serviciodebackup.ServicioDeBackup;
import com.fasterxml.jackson.core.type.TypeReference;
import java.util.List;

/**
 * Clase abstracta que encapsula la lógica de caching para una fuente de datos.
 * Ya NO maneja su propia programación de tareas. Su actualización debe ser
 * invocada desde un proceso externo.
 */
public abstract class FuenteDeCopiaLocal implements Fuente {

  protected final String nombre;
  protected List<Hecho> cacheDeHechos;
  protected final ServicioDeBackup servicioDeBackup;

  public FuenteDeCopiaLocal(String nombre, String jsonFilePathParaCopias) {
    this.validarFuente(nombre);
    this.nombre = nombre;
    this.servicioDeBackup = new ServicioDeBackup(jsonFilePathParaCopias);
    this.cacheDeHechos = this.servicioDeBackup
        .cargarCopiaLocalJson(new TypeReference<List<Hecho>>() {
        });
  }

  protected abstract List<Hecho> consultarNuevosHechos();

  @Override
  public List<Hecho> obtenerHechos() {
    return List.copyOf(this.cacheDeHechos);
  }

  /**
   * Actualizamos la fuente de forma sincronica.
   * Este es el método que llamamos desde el crontab.
   */
  public void forzarActualizacionSincrona() {
    List<Hecho> nuevosHechos = this.consultarNuevosHechos();
    this.cacheDeHechos = nuevosHechos;
    this.servicioDeBackup.guardarCopiaLocalJson(this.cacheDeHechos);
  }

  public String getNombre() {
    return this.nombre;
  }
}