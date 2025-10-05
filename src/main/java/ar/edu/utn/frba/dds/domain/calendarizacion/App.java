package ar.edu.utn.frba.dds.domain.calendarizacion;

import ar.edu.utn.frba.dds.domain.fuentes.Fuente;
import ar.edu.utn.frba.dds.domain.fuentes.FuenteDeAgregacion;
import ar.edu.utn.frba.dds.domain.fuentes.FuenteDeCopiaLocal;
import ar.edu.utn.frba.dds.domain.fuentes.FuenteDinamica;
import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import ar.edu.utn.frba.dds.domain.hecho.HechoBuilder;
import ar.edu.utn.frba.dds.domain.hecho.Origen;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.logging.Logger;

/**
 * Clase principal que configura y gestiona las fuentes de datos de la aplicación.
 */
public class App {

  // El registro de fuentes de la aplicación.
  private final Map<String, Fuente> fuentesRegistradas = new HashMap<>();
  private static final Logger logger = Logger.getLogger(App.class.getName());

  /**
   * Registra una nueva fuente en la aplicación.
   *
   * @param fuente La fuente a registrar.
   */
  public void registrarFuente(Fuente fuente) {
    if (fuente != null) {
      this.fuentesRegistradas.put(fuente.getNombre(), fuente);
      logger.info("Fuente registrada: '" + fuente.getNombre() + "'");
    }
  }

  /**
   * Recorre todas las fuentes registradas y ejecuta la actualización síncrona
   * solo para aquellas que son de tipo 'FuenteDeCopiaLocal'.
   */
  public void ejecutarActualizacionTodasLasFuentes() {
    logger.info("Iniciando actualización de todas las fuentes con copia local...");
    fuentesRegistradas.values().forEach(fuente -> {
      if (fuente instanceof FuenteDeCopiaLocal) {
        try {
          ((FuenteDeCopiaLocal) fuente).forzarActualizacionSincrona();
          logger.info("Fuente '" + fuente.getNombre() + "' actualizada correctamente.");
        } catch (Exception e) {
          logger.warning("Error al actualizar la fuente '" + fuente.getNombre() + "': " + e.getMessage());
        }
      } else {
        logger.info("Fuente '" + fuente.getNombre() + "' es de tipo '" + fuente.getClass().getSimpleName() + "' y no requiere actualización síncrona.");
      }
    });
    logger.info("Proceso de actualización finalizado.");
  }

  /**
   * Método estático que crea y configura una instancia de la aplicación con
   * fuentes de ejemplo y lógica de negocio inicial.
   *
   * @return Una instancia de App configurada.
   */
  public static App configurarAplicacion() {
    App aplicacion = new App();

    // --- Configuración Fuente de Agregación ---
    FuenteDeAgregacion agregadora = new FuenteDeAgregacion("agregadora_principal");
    aplicacion.registrarFuente(agregadora);

    // --- Configuración Fuente Dinámica ---
    FuenteDinamica dinamica = new FuenteDinamica("dinamica_principal");
    aplicacion.registrarFuente(dinamica);

    // --- Lógica de negocio de ejemplo ---
    // 1. Se agrega la fuente dinámica a la fuente de agregación.
    agregadora.agregarFuente(dinamica);
    logger.info("'" + dinamica.getNombre() + "' ha sido agregada a '" + agregadora.getNombre() + "'");

    // 2. Se crea un nuevo 'Hecho' y se agrega a la fuente dinámica.
    Hecho nuevoHecho = new HechoBuilder()
        .conTitulo("Nuevo evento dinámico")
        .conDescripcion("Descripción del evento agregado en App.")
        .conFechaSuceso(LocalDateTime.now().minusDays(1))
        .conFuenteOrigen(Origen.PROVISTO_CONTRIBUYENTE)
        .build();
    dinamica.agregarHecho(nuevoHecho);
    logger.info("Nuevo hecho '" + nuevoHecho.getTitulo() + "' agregado a la fuente dinámica.");

    return aplicacion;
  }

  /**
   * Devuelve una copia del mapa de fuentes registradas.
   *
   * @return Un nuevo mapa con las fuentes.
   */
  public Map<String, Fuente> getFuentesRegistradas() {
    return new HashMap<>(fuentesRegistradas);
  }

  /**
   * Punto de entrada principal para ejecutar la aplicación de ejemplo.
   */
  public static void main(String[] args) {
    // ✅ SOLUCIÓN GENERAL PARA DEPLOY Y EJECUCIÓN
    // Se establece la zona horaria correcta como la PRIMERA acción de toda la aplicación.
    TimeZone.setDefault(TimeZone.getTimeZone("America/Argentina/Buenos_Aires"));

    logger.info("======================================================");
    logger.info("== INICIANDO CONFIGURACIÓN DE LA APLICACIÓN DE DATOS ==");
    logger.info("======================================================");

    App miApp = configurarAplicacion();

    logger.info("------------------------------------------------------");
    logger.info("Fuentes registradas: " + miApp.getFuentesRegistradas().keySet());
    logger.info("------------------------------------------------------");

    // Ejemplo de uso: Obtener los hechos de la fuente de agregación.
    FuenteDeAgregacion agregadora = (FuenteDeAgregacion) miApp.getFuentesRegistradas().get("agregadora_principal");
    if (agregadora != null) {
      List<Hecho> hechosAgregados = agregadora.obtenerHechos();
      logger.info("Consultando hechos de '" + agregadora.getNombre() + "':");
      logger.info("Total de hechos encontrados: " + hechosAgregados.size());
      hechosAgregados.forEach(hecho ->
                                  logger.info("  -> Hecho: '" + hecho.getTitulo())
      );
    }
  }
}
