package ar.edu.utn.frba.dds.domain.calendarizacion;

import ar.edu.utn.frba.dds.domain.exportador.configuracion.ConfiguracionExportador;
import ar.edu.utn.frba.dds.domain.exportador.configuracion.ConfiguracionExportadorJson;
import ar.edu.utn.frba.dds.domain.fuentes.Fuente;
import ar.edu.utn.frba.dds.domain.fuentes.FuenteDeAgregacion;
import ar.edu.utn.frba.dds.domain.fuentes.FuenteDeCopiaLocal;
import ar.edu.utn.frba.dds.domain.fuentes.FuenteDinamica;
import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import ar.edu.utn.frba.dds.domain.lector.configuracion.ConfiguracionLector;
import ar.edu.utn.frba.dds.domain.lector.configuracion.ConfiguracionLectorJson;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
// ... otros imports

public class App {
  // ... (otras propiedades como detector, repo, estadisticas, etc. siguen igual)

  // El registro ahora es de tipo Fuente, más genérico
  private final Map<String, Fuente> fuentesRegistradas = new HashMap<>();
  Logger logger = Logger.getLogger(App.class.getName());

  public void registrarFuente(Fuente fuente) {
    if (fuente != null) {
      this.fuentesRegistradas.put(fuente.getNombre(), fuente);
    }
  }

  public void ejecutarActualizacionTodasLasFuentes() {
    fuentesRegistradas.values().forEach(fuente -> {
      // Solo las que usan caché se pueden forzar a actualizar
      if (fuente instanceof FuenteDeCopiaLocal) {
        try {
          ((FuenteDeCopiaLocal) fuente).forzarActualizacionSincrona();
          logger.info("Fuente '" + fuente.getNombre() + "' actualizada correctamente.");
        } catch (Exception e) {
          logger.warning("Error al actualizar la fuente '" + fuente.getNombre() + "': " + e.getMessage());
        }
      }
    });
  }

  public static App configurarAplicacion() {
    App aplicacion = new App();

    // --- Bloque de Configuración ---
    // 1. Se crean las ENTIDADES DE CONFIGURACIÓN
    ConfiguracionLector configLectorJson = new ConfiguracionLectorJson();
    ConfiguracionExportador configExportadorJson = new ConfiguracionExportadorJson();

    // --- Configuración Fuente de Agregación ---
    // 2. Se pasan las configuraciones a los constructores
    FuenteDeAgregacion agregadora = new FuenteDeAgregacion(
        "agregadora_principal",
        "/home/jeremias/Desktop/TpDDSi/agregados.json",
        configLectorJson,
        configExportadorJson
    );
    aplicacion.registrarFuente(agregadora);

    // --- Configuración Fuente Dinámica ---
    // 3. El constructor de FuenteDinamica ahora es simple
    FuenteDinamica dinamica = new FuenteDinamica("dinamica_principal");
    aplicacion.registrarFuente(dinamica);

    // --- Lógica de negocio ---
    agregadora.agregarFuente(dinamica);
    // ... (código para agregar Hechos, etc. sigue igual)

    return aplicacion;
  }

  public Map<String, Fuente> getFuentesRegistradas() {
    return new HashMap<>(fuentesRegistradas);
  }

  // El método main sigue igual
  public static void main(String[] args) {
    // ...
  }
}