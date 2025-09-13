package ar.edu.utn.frba.dds.domain.calendarizacion;

import ar.edu.utn.frba.dds.domain.fuentes.FuenteDeAgregacion;
import ar.edu.utn.frba.dds.domain.fuentes.FuenteDeCopiaLocal;
import ar.edu.utn.frba.dds.domain.fuentes.FuenteDinamica;
import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import ar.edu.utn.frba.dds.domain.hecho.Origen;
import ar.edu.utn.frba.dds.domain.hecho.etiqueta.Etiqueta;
import ar.edu.utn.frba.dds.domain.info.PuntoGeografico;
import ar.edu.utn.frba.dds.domain.serializadores.exportador.Exportador;
import ar.edu.utn.frba.dds.domain.serializadores.exportador.json.ExportadorJson;
import ar.edu.utn.frba.dds.domain.serializadores.lector.Lector;
import ar.edu.utn.frba.dds.domain.serializadores.lector.json.LectorJson;
import com.fasterxml.jackson.core.type.TypeReference;
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
  @SuppressWarnings("checkstyle:AbbreviationAsWordInName")
  public void ejecutarActualizacion(String nombreFuente) {
    FuenteDeCopiaLocal fuenteAActualizar = fuentesRegistradas.get(nombreFuente);

    if (fuenteAActualizar != null) {
      fuenteAActualizar.forzarActualizacionSincrona();
    } else {
      // Se lanza una excepción más descriptiva si la fuente no se encuentra.
      throw new IllegalArgumentException(
          "Error al actualizar: No se encontró la fuente con el nombre '" + nombreFuente + "'");
    }
  }

  /**
   * Ejecuta la actualización para todas las fuentes registradas.
   */
  public void ejecutarActualizacionTodasLasFuentes() {
    fuentesRegistradas.values()
                      .forEach(fuente -> {
                        try {
                          fuente.forzarActualizacionSincrona();
                          System.out.println(
                              "Fuente '" + fuente.getNombre() + "' actualizada correctamente.");
                        } catch (Exception e) {
                          // Se maneja el error por cada fuente para no detener el proceso completo.
                          System.err.println(
                              "Error al actualizar la fuente '"
                                  + fuente.getNombre() + "': " + e.getMessage()
                          );
                        }
                      });
  }


  /**
   * Configura la aplicación creando y registrando todas las fuentes disponibles.
   * Este es el lugar central para añadir nuevas fuentes al sistema.
   *
   * @return una instancia de App completamente configurada.
   */
  public static App configurarAplicacion() {
    App aplicacion = new App();
    // --- Bloque de Configuración de Fuentes ---

    // Se eliminó la clase Serializador. Ahora creamos Lector y Exportador por separado.
    Lector<Hecho> lectorJsonHechos = new LectorJson<>(new TypeReference<>() {
    });
    // Asumiendo constructor por defecto
    Exportador<Hecho> exportadorJsonHechos = new ExportadorJson<>();

    // --- Configuración Fuente de Agregación ---
    // Se actualiza el constructor para que reciba Lector y Exportador por separado.
    FuenteDeAgregacion agregadora = new FuenteDeAgregacion(
        "agregadora_principal",
        "agregados.json",
        lectorJsonHechos,
        exportadorJsonHechos
    );
    aplicacion.registrarFuente(agregadora);

    // --- Configuración Fuente Dinámica ---
    // Se actualiza el constructor para que reciba Lector y Exportador por separado.
    FuenteDinamica dinamica = new FuenteDinamica(
        "dinamica_principal",
        "dinamica.json",
        lectorJsonHechos,
        exportadorJsonHechos
    );
    aplicacion.registrarFuente(dinamica);

    var etiqueta1 = new Etiqueta("#PRUEBA");
    var etiqueta2 = new Etiqueta("#ESTOESUNAPRUEBA");
    Hecho hecho = new Hecho(
        "Un hecho",
        "Descripcion",
        "PRUEBA",
        "Direccion de prueba 123",
        "aaa",
        new PuntoGeografico(-123123, 123123),
        LocalDateTime.now()
                     .minusWeeks(2),
        LocalDateTime.now(),
        Origen.PROVISTO_CONTRIBUYENTE,
        List.of(etiqueta1, etiqueta2)
    );

    dinamica.agregarHecho(hecho);

    return aplicacion;
  }

  /**
   * Ejecuta el crontab periodicamente.
   *
   * @param args Argumentos de la línea de comandos. Si no se proveen, se actualizan todas
   *             las fuentes. Si se provee un argumento, se usa como el nombre de la fuente a
   *             actualizar.
   */
  public static void main(String[] args) {
    App aplicacion = configurarAplicacion();

    if (args.length == 0) {
      System.out.println("Iniciando actualización de todas las fuentes...");
      aplicacion.ejecutarActualizacionTodasLasFuentes();
      System.out.println("Actualización de todas las fuentes completada.");
    } else {
      String nombreFuente = args[0];
      System.out.println("Iniciando actualización de la fuente: " + nombreFuente + "...");
      try {
        aplicacion.ejecutarActualizacion(nombreFuente);
        System.out.println("Actualización de la fuente '" + nombreFuente + "' completada.");
      } catch (Exception e) {
        System.err.println(e.getMessage());
      }
    }
  }

  public Map<String, FuenteDeCopiaLocal> getFuentesRegistradas() {
    // Retorna una copia para evitar modificaciones externas
    return new HashMap<>(fuentesRegistradas);
  }
}
