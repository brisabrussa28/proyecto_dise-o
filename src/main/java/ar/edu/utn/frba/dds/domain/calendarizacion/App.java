package ar.edu.utn.frba.dds.domain.calendarizacion;

import ar.edu.utn.frba.dds.domain.fuentes.FuenteDeAgregacion;
import ar.edu.utn.frba.dds.domain.fuentes.FuenteDeCopiaLocal;
import ar.edu.utn.frba.dds.domain.fuentes.FuenteDinamica;
import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import ar.edu.utn.frba.dds.domain.hecho.Origen;
import ar.edu.utn.frba.dds.domain.hecho.etiqueta.Etiqueta;
import ar.edu.utn.frba.dds.domain.info.PuntoGeografico;
import ar.edu.utn.frba.dds.domain.serializadores.Serializador;
import ar.edu.utn.frba.dds.domain.serializadores.SerializadorJson;
import ar.edu.utn.frba.dds.domain.serializadores.json.Exportador.ExportadorJson;
import ar.edu.utn.frba.dds.domain.serializadores.json.Lector.LectorJson;
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
   */
  public void ejecutarActualizacionTodasLasFuentes() {
    fuentesRegistradas.values()
                      .forEach(fuente -> {
                        try {
                          fuente.forzarActualizacionSincrona();
                        } catch (Exception e) {
                          System.err.println("Error al actualizar la fuente '" + fuente.getNombre());
                        }
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

    Serializador<Hecho> serializadorJsonHechos = new SerializadorJson<>(
        new LectorJson<>(new TypeReference<List<Hecho>>() {
        }),
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
   * Esto ejecuta el crontab periodicamente.
   *
   * @param args Argumentos de la línea de comandos. Se espera el nombre de la fuente a actualizar.
   */
  public static void main(String[] args) {
    App aplicacion = configurarAplicacion();

    if (args.length == 0) {
      aplicacion.ejecutarActualizacionTodasLasFuentes();
    } else {
      aplicacion.ejecutarActualizacion(args[0]);
    }
  }

  public Map<String, FuenteDeCopiaLocal> getFuentesRegistradas() {
    // Retorna una copia para evitar modificaciones externas
    return new HashMap<>(fuentesRegistradas);
  }
}
