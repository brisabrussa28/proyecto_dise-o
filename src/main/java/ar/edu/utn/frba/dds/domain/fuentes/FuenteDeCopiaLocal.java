package ar.edu.utn.frba.dds.domain.fuentes;

import ar.edu.utn.frba.dds.domain.exportador.Exportador;
import ar.edu.utn.frba.dds.domain.exportador.ExportadorFactory;
import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import ar.edu.utn.frba.dds.domain.lector.Lector;
import ar.edu.utn.frba.dds.domain.lector.LectorFactory;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Lob;
import javax.persistence.MappedSuperclass;
import javax.persistence.PostLoad;
import javax.persistence.Transient;

/**
 * Clase abstracta que encapsula la lógica de caching para una fuente de datos,
 * persistiendo la configuración de su Lector y Exportador como JSON.
 */
@MappedSuperclass
public abstract class FuenteDeCopiaLocal extends Fuente {

  @Transient // La caché en sí no se persiste directamente, se carga desde el archivo.
  protected List<Hecho> cacheDeHechos;

  @Transient // El lector no es persistible.
  protected Lector<Hecho> lector;

  @Transient // El exportador no es persistible.
  protected Exportador<Hecho> exportador;

  @Lob
  @Column(name = "json_lector")
  protected String jsonLector; // Campo para persistir la configuración del Lector.

  @Lob
  @Column(name = "json_exportador")
  protected String jsonExportador; // Campo para persistir la configuración del Exportador.

  protected String rutaCopiaLocal; // Ruta al archivo de caché.

  // Constructor para JPA.
  protected FuenteDeCopiaLocal() {
    super();
  }

  /**
   * Constructor que recibe un lector y exportador para manejar la copia local.
   *
   * @param nombre         Nombre de la fuente.
   * @param rutaCopiaLocal Ruta del archivo para guardar/cargar la copia (e.g., "copia.json").
   * @param lector         Lector para manejar la persistencia de la caché.
   * @param exportador     Exportador para guardar la caché.
   */
  public FuenteDeCopiaLocal(
      String nombre,
      String rutaCopiaLocal,
      Lector<Hecho> lector,
      Exportador<Hecho> exportador
  ) {
    super(nombre);
    this.rutaCopiaLocal = rutaCopiaLocal;
    this.lector = lector;
    this.exportador = exportador;
    this.jsonLector = lector.getConfiguracionJson();
    this.jsonExportador = exportador.getConfiguracionJson();
    this.cacheDeHechos = this.lector.importar(this.rutaCopiaLocal);
  }

  /**
   * Mét0do que se ejecuta automáticamente después de cargar la entidad desde la BD.
   * Se encarga de reconstruir el lector y el exportador a partir del JSON almacenado.
   */
  @PostLoad
  protected void reconstruirDependencias() {
    ObjectMapper mapper = new ObjectMapper();
    LectorFactory lectorFactory = new LectorFactory();
    ExportadorFactory exportadorFactory = new ExportadorFactory();

    try {
      // Reconstruir Lector
      if (this.jsonLector != null && !this.jsonLector.isEmpty()) {
        JsonNode lectorNode = mapper.readTree(this.jsonLector);
        this.lector = lectorFactory.create(lectorNode, Hecho.class);
      }
      // Reconstruir Exportador
      if (this.jsonExportador != null && !this.jsonExportador.isEmpty()) {
        JsonNode exportadorNode = mapper.readTree(this.jsonExportador);
        this.exportador = exportadorFactory.create(exportadorNode);
      }
    } catch (IOException e) {
      throw new RuntimeException(
          "Error al reconstruir dependencias (Lector/Exportador) desde JSON",
          e
      );
    }
  }

  /**
   * Lógica para consultar los nuevos hechos desde el origen real (API, etc.).
   *
   * @return La lista de hechos actualizada.
   */
  protected abstract List<Hecho> consultarNuevosHechos();

  @Override
  public List<Hecho> obtenerHechos() {
    // Siempre devuelve una lista no modificable para proteger la caché.
    return Collections.unmodifiableList(this.cacheDeHechos);
  }

  /**
   * Actualiza la fuente de forma síncrona: consulta nuevos hechos,
   * actualiza la caché en memoria y persiste la caché en disco usando el exportador.
   */


  public void forzarActualizacionSincrona() {
    List<Hecho> nuevosHechos = this.consultarNuevosHechos();
    if (nuevosHechos != null && !nuevosHechos.isEmpty()) {
      this.cacheDeHechos = nuevosHechos;
      this.exportador.exportar(this.cacheDeHechos, this.rutaCopiaLocal);
    }
  }


}

