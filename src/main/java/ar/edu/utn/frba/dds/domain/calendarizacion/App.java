package ar.edu.utn.frba.dds.domain.calendarizacion;

import ar.edu.utn.frba.dds.domain.fuentes.FuenteDeAgregacion;
import ar.edu.utn.frba.dds.domain.fuentes.FuenteDeCopiaLocal;
import ar.edu.utn.frba.dds.domain.fuentes.FuenteDinamica;
import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import ar.edu.utn.frba.dds.domain.hecho.HechoBuilder;
import ar.edu.utn.frba.dds.domain.serializadores.Serializador;
import ar.edu.utn.frba.dds.domain.serializadores.SerializadorJson;
import ar.edu.utn.frba.dds.domain.serializadores.json.Exportador.ExportadorJson;
import ar.edu.utn.frba.dds.domain.serializadores.json.Lector.LectorJson;
import com.fasterxml.jackson.core.type.TypeReference;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Punto de entrada para la ejecución de tareas programadas (crontab).
 * Se encarga de registrar y actualizar las fuentes de datos que requieren caché.
 */
public class App {

  private static final Logger logger = Logger.getLogger(App.class.getName());
  private final Map<String, FuenteDeCopiaLocal> fuentesRegistradas = new HashMap<>();

  public void registrarFuente(FuenteDeCopiaLocal fuente) {
    if (fuente != null) {
      this.fuentesRegistradas.put(fuente.getNombre(), fuente);
    }
  }

  public void ejecutarActualizacionTodasLasFuentes() {
    logger.info("Iniciando la actualización de todas las fuentes registradas...");
    fuentesRegistradas.values().forEach(fuente -> {
      try {
        logger.info("Actualizando fuente: '" + fuente.getNombre() + "'...");
        fuente.forzarActualizacionSincrona();
        logger.info("Fuente '" + fuente.getNombre() + "' actualizada correctamente.");
      } catch (Exception e) {
        logger.log(Level.SEVERE, "Error al actualizar la fuente '" + fuente.getNombre() + "'", e);
      }
    });
    logger.info("Proceso de actualización de fuentes finalizado.");
  }

  public static App configurarAplicacion() {
    App aplicacion = new App();

    // --- Serializador JSON Genérico para Hechos ---
    Serializador<Hecho> serializadorJsonHechos = new SerializadorJson<>(
        new LectorJson<>(new TypeReference<List<Hecho>>() {}),
        new ExportadorJson()
    );

    // --- Configuración Fuente de Agregación ---
    FuenteDeAgregacion agregadora = new FuenteDeAgregacion(
        "agregadora_principal",
        "agregados.json",
        serializadorJsonHechos
    );
    aplicacion.registrarFuente(agregadora);

    // --- Configuración Fuente Dinámica ---
    FuenteDinamica dinamica = new FuenteDinamica(
        "dinamica_principal",
        "dinamica.json",
        serializadorJsonHechos
    );
    aplicacion.registrarFuente(dinamica);

    // --- Ejemplo de uso: Agregar un hecho a la fuente dinámica ---
    Hecho hechoDePrueba = new HechoBuilder()
        .conTitulo("Hecho de prueba para la fuente dinámica")
        .conFechaSuceso(LocalDateTime.now().minusWeeks(2))
        .build();
    dinamica.agregarHecho(hechoDePrueba);

    // Se podrían agregar aquí fuentes estáticas o de API a la agregadora
    // agregadora.agregarFuente(fuenteEstatica);

    return aplicacion;
  }

  /**
   * Punto de entrada principal para el proceso de actualización.
   * Configura la aplicación y ejecuta la actualización de todas las fuentes.
   *
   * @param args Argumentos de la línea de comandos (no se utilizan).
   */
  public static void main(String[] args) {
    logger.info("Iniciando el proceso de calendarización.");
    App aplicacion = configurarAplicacion();
    aplicacion.ejecutarActualizacionTodasLasFuentes();
  }

  public Map<String, FuenteDeCopiaLocal> getFuentesRegistradas() {
    return new HashMap<>(fuentesRegistradas);
  }
}
