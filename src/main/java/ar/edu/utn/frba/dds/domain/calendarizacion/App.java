package ar.edu.utn.frba.dds.domain.calendarizacion;

import ar.edu.utn.frba.dds.domain.coleccion.Coleccion;
import ar.edu.utn.frba.dds.domain.estadisicas.CentralDeEstadisticas;
import ar.edu.utn.frba.dds.domain.estadisicas.Estadistica;
import ar.edu.utn.frba.dds.domain.exportador.Exportador;
import ar.edu.utn.frba.dds.domain.exportador.json.ExportadorJson;
import ar.edu.utn.frba.dds.domain.fuentes.FuenteDeAgregacion;
import ar.edu.utn.frba.dds.domain.fuentes.FuenteDeCopiaLocal;
import ar.edu.utn.frba.dds.domain.fuentes.FuenteDinamica;
import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import ar.edu.utn.frba.dds.domain.hecho.Origen;
import ar.edu.utn.frba.dds.domain.hecho.etiqueta.Etiqueta;
import ar.edu.utn.frba.dds.domain.info.PuntoGeografico;
import ar.edu.utn.frba.dds.domain.lector.Lector;
import ar.edu.utn.frba.dds.domain.lector.json.LectorJson;
import ar.edu.utn.frba.dds.domain.reportes.RepositorioDeSolicitudes;
import ar.edu.utn.frba.dds.domain.reportes.detectorspam.DetectorSpam;
import com.fasterxml.jackson.core.type.TypeReference;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Punto de entrada para nuestro crontab.
 */
public class App {
  static DetectorSpam detector = new DetectorSpam() {
    @Override
    public boolean esSpam(String texto) {
      return false;
    }
  };
  static RepositorioDeSolicitudes repo = new RepositorioDeSolicitudes(detector);
  static CentralDeEstadisticas estadisticas = new CentralDeEstadisticas();
  static List<Coleccion> colecciones;
  // El registro de fuentes ahora es una variable de instancia, no estática.
  private final Map<String, FuenteDeCopiaLocal> fuentesRegistradas = new HashMap<>();
  Logger logger = Logger.getLogger(App.class.getName());

  public static void setColecciones(List<Coleccion> colecciones) {
    App.colecciones = new ArrayList<>(colecciones);
  }

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
                          logger.info("Fuente '" + fuente.getNombre() + "' actualizada correctamente.");
                        } catch (Exception e) {
                          // Se maneja el error por cada fuente para no detener el proceso completo.
                          logger.warning("Error al actualizar la fuente '"
                                             + fuente.getNombre() + "': " + e.getMessage());
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

    estadisticas.setRepo(repo);

    // -- Agrego Colecciones por ahora
    Coleccion pruebas = new Coleccion(
        "Pruebas",
        dinamica,
        "Un día más haciendo pruebas",
        "Pruebas"
    );
    List<Coleccion> colecciones = List.of(pruebas);
    setColecciones(colecciones);

    return aplicacion;
  }

  public Map<String, FuenteDeCopiaLocal> getFuentesRegistradas() {
    // Retorna una copia para evitar modificaciones externas
    return new HashMap<>(fuentesRegistradas);
  }

  /**
   * Ejecuta el crontab periodicamente.
   *
   * @param args Argumentos de la línea de comandos. Si no se proveen, se actualizan todas
   *             las fuentes. Si se provee un argumento, se usa como el nombre de la fuente a
   *             actualizar.
   */
  public static void main(String[] args) {
    Logger logger = Logger.getLogger(App.class.getName());
    try {
      App aplicacion = configurarAplicacion();


      if (args.length == 0) {
        logger.info("Iniciando actualización de todas las fuentes...");
        aplicacion.ejecutarActualizacionTodasLasFuentes();
        logger.info("Actualización de todas las fuentes completada.");
      } else {
        String nombreFuente = args[0];
        logger.info("Iniciando actualización de la fuente: " + nombreFuente + "...");
        try {
          aplicacion.ejecutarActualizacion(nombreFuente);
          logger.info("Actualización de la fuente '" + nombreFuente + "' completada.");
        } catch (Exception e) {
          logger.severe("Error al actualizar fuente: " + e.getMessage());
        }
      }

      if (colecciones != null && !colecciones.isEmpty()) {
        Estadistica provinciaMax = estadisticas.provinciaConMasHechos(colecciones.get(0));
        Estadistica categoriaMax = estadisticas.categoriaConMasHechos(colecciones);
        Estadistica provinciaPorCategoria = estadisticas.provinciaConMasHechosDeCiertaCategoria(colecciones, "Robo");
        Estadistica horaPico = estadisticas.horaConMasHechosDeCiertaCategoria(colecciones, "Robo");
        Estadistica porcentajeSpam = new Estadistica("Porcentaje de solicitudes spam", Math.round(estadisticas.porcentajeDeSolicitudesSpam()));

        estadisticas.export(List.of(provinciaMax), "provincia_max.csv");
        estadisticas.export(List.of(categoriaMax), "categoria_max.csv");
        estadisticas.export(List.of(provinciaPorCategoria), "provincia_por_categoria.csv");
        estadisticas.export(List.of(horaPico), "hora_pico.csv");
        estadisticas.export(List.of(porcentajeSpam), "porcentaje_spam.csv");
      } else {
        logger.warning("No hay colecciones disponibles para calcular estadísticas.");
      }
    } catch (Exception e) {
      logger.severe("Error inesperado en App.main: " + e.getMessage());
    }
  }
}