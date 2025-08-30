package ar.edu.utn.frba.dds.domain.calendarizacion;

import ar.edu.utn.frba.dds.domain.fuentes.FuenteDeCopiaLocal;
import ar.edu.utn.frba.dds.domain.fuentes.FuenteDeAgregacion;
import ar.edu.utn.frba.dds.domain.fuentes.FuenteDinamica;
import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import ar.edu.utn.frba.dds.domain.info.PuntoGeografico;
import ar.edu.utn.frba.dds.domain.origen.Origen;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Punto de entrada para nuestro crontab.
 */
public class App {

  // El registro de fuentes ahora es una variable de instancia, no estática.
  private final Map<String, FuenteDeCopiaLocal> fuentesRegistradas = new HashMap<>();

  /**
   * Registra una nueva fuente cacheable para que pueda ser actualizada por esta aplicación.
   *
   * @param fuente La instancia de la fuente a registrar.
   */
  public void registrarFuente(FuenteDeCopiaLocal fuente) {
    if (fuente != null) {
      this.fuentesRegistradas.put(fuente.getNombre(), fuente);
    }
  }

  /**
   * Ejecuta la actualización para una fuente específica por su nombre.
   *
   * @param nombreFuente El nombre de la fuente a actualizar.
   */
// no se si dejarlo pero creo que debriamos borrarlo
  public void ejecutarActualizacion(String nombreFuente) {
    FuenteDeCopiaLocal fuenteAActualizar = fuentesRegistradas.get(nombreFuente);

    if (fuenteAActualizar != null) {
      fuenteAActualizar.forzarActualizacionSincrona();
      }
    //agregar log
    else {
      throw new IllegalStateException("Error al actualizar...");
    }
  }

  /**
   * Ejecuta la actualización para una todas las fuentes.
   *
   */
  public void ejecutarActualizacionTodasLasFuentes() {
    fuentesRegistradas.values().forEach(fuente -> {
      fuente.forzarActualizacionSincrona();
      // agregar log
    });
  }

  // TODO: Aca dice que podriamos poner un try catch, ustds lo pondrian dentro del forEach o afuera?


  /**
   * Configura la aplicación creando y registrando todas las fuentes disponibles.
   * Este es el lugar central para añadir nuevas fuentes al sistema.
   *
   * @return una instancia de App completamente configurada.
   */
  public static App configurarAplicacion() {
    App aplicacion = new App();
    // --- Bloque de Configuración de Fuentes ---

    FuenteDeAgregacion agregadora = new FuenteDeAgregacion(
        "agregadora_principal",
        "agregados.json"
    );
    aplicacion.registrarFuente(agregadora);

    FuenteDinamica dinamica = new FuenteDinamica("dinamica_principal", "dinamica.json");
    aplicacion.registrarFuente(dinamica);

    Hecho hecho = new Hecho(
        "Un hecho",
        "Descripcion",
        "PRUEBA",
        "Direccion de prueba 123",
        new PuntoGeografico(-123123, 123123),
        LocalDateTime.now().minusWeeks(2),
        LocalDateTime.now(),
        Origen.PROVISTO_CONTRIBUYENTE,
        List.of("#PRUEBA", "#ESTOESUNAPRUEBA")
    );

    dinamica.agregarHecho(hecho);

    return aplicacion;
  }

  /**
   * Esto ejecuta el crontab periodicamente.
   *
   * @param args Argumentos de la línea de comandos. Se espera el nombre de la fuente a actualizar.
   */
  public static void main(String[] args) {
    if (args.length == 0) {
      throw new IllegalStateException(
          "Error: Se requiere el nombre de la fuente a actualizar como argumento.");
    }

    App aplicacion = configurarAplicacion();

    aplicacion.ejecutarActualizacionTodasLasFuentes();
  }

  public Map<String, FuenteDeCopiaLocal> getFuentesRegistradas() {
    // Retorna una copia para evitar modificaciones externas
    return new HashMap<>(fuentesRegistradas);
  }
}
