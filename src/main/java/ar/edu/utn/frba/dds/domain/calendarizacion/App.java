package ar.edu.utn.frba.dds.domain.calendarizacion;

import ar.edu.utn.frba.dds.domain.fuentes.Conexion;
import ar.edu.utn.frba.dds.domain.fuentes.FuenteCacheable;
import ar.edu.utn.frba.dds.domain.fuentes.FuenteDeAgregacion;
import ar.edu.utn.frba.dds.domain.fuentes.FuenteDemo;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * Punto de entrada para ejecutar actualizaciones de fuentes desde una tarea programada.
 * Actúa como un orquestador que se configura y luego ejecuta una tarea específica.
 */
public class App {

  // El registro de fuentes ahora es una variable de instancia, no estática.
  private final Map<String, FuenteCacheable> fuentesRegistradas = new HashMap<>();

  /**
   * Registra una nueva fuente cacheable para que pueda ser actualizada por esta aplicación.
   * @param fuente La instancia de la fuente a registrar.
   */
  public void registrarFuente(FuenteCacheable fuente) {
    if (fuente != null) {
      this.fuentesRegistradas.put(fuente.getNombre(), fuente);
    }
  }

  /**
   * Ejecuta la actualización para una fuente específica por su nombre.
   * @param nombreFuente El nombre de la fuente a actualizar.
   */
  public void ejecutarActualizacion(String nombreFuente) {
    FuenteCacheable fuenteAActualizar = fuentesRegistradas.get(nombreFuente);

    if (fuenteAActualizar != null) {
      System.out.println("Iniciando actualización para la fuente: " + nombreFuente);
      fuenteAActualizar.forzarActualizacionSincrona();
      System.out.println("Actualización para " + nombreFuente + " completada.");
    } else {
      System.err.println("Error: No se encontró una fuente registrada con el nombre '" + nombreFuente + "'.");
      System.err.println("Fuentes disponibles: " + fuentesRegistradas.keySet());
      System.exit(1);
    }
  }

  /**
   * Configura la aplicación creando y registrando todas las fuentes disponibles.
   * Este es el lugar central para añadir nuevas fuentes al sistema.
   * @return una instancia de App completamente configurada.
   */
  private static App configurarAplicacion() {
    App aplicacion = new App();

    try {
      // --- Bloque de Configuración de Fuentes ---
      // Para agregar una nueva fuente, hágalo aquí.

      // 1. Crear y registrar la fuente de agregación.
      FuenteDeAgregacion agregadora = new FuenteDeAgregacion("agregadora_principal", "copias/agregados.json");
      aplicacion.registrarFuente(agregadora);

      // 2. Crear y registrar la fuente demo externa.
      URL url = new URL("http://localhost:8080/hechos");
      Conexion conexion = new Conexion(); // Se asume que esta clase existe
      FuenteDemo demo = new FuenteDemo("fuente_externa_demo", url, conexion, "copias/demo.json");
      aplicacion.registrarFuente(demo);

      // 3. (Ejemplo) Crear y registrar otra fuente si existiera.
      // FuenteEstatica estatica = new FuenteEstatica("fuente_estatica_csv", "datos/hechos.csv", new LectorCSV());
      // aplicacion.registrarFuente(estatica);

    } catch (MalformedURLException e) {
      System.err.println("Error fatal en la configuración: La URL de una fuente es inválida. " + e.getMessage());
      System.exit(1);
    }

    return aplicacion;
  }

  /**
   * El método main que será ejecutado por el Programador de Tareas o Crontab.
   * Su responsabilidad es configurar la app y luego ejecutar la tarea.
   * @param args Argumentos de la línea de comandos. Se espera el nombre de la fuente a actualizar.
   */
  public static void main(String[] args) {
    if (args.length == 0) {
      System.err.println("Error: Se requiere el nombre de la fuente a actualizar como argumento.");
      System.exit(1);
    }

    // 1. Configurar la aplicación obteniendo una instancia lista para usar.
    App aplicacion = configurarAplicacion();

    // 2. Ejecución: Usar el primer argumento para decidir qué fuente actualizar.
    String nombreFuenteAActualizar = args[0];
    aplicacion.ejecutarActualizacion(nombreFuenteAActualizar);
  }
}
