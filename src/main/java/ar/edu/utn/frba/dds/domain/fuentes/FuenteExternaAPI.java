package ar.edu.utn.frba.dds.domain.fuentes;

import ar.edu.utn.frba.dds.domain.exportador.Exportador;
import ar.edu.utn.frba.dds.domain.fuentes.apis.AdapterFactory;
import ar.edu.utn.frba.dds.domain.fuentes.apis.FuenteAdapter;
import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import ar.edu.utn.frba.dds.domain.lector.Lector;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.PostLoad;
import javax.persistence.Transient;

/**
 * Clase para fuentes de datos externas que consultan a través de una API.
 * Persiste la configuración de su Lector, Exportador y FuenteAdapter como JSON.
 */
@Entity
@DiscriminatorValue("API_EXTERNA")
public class FuenteExternaAPI extends FuenteDeCopiaLocal {

  private static final Logger logger = Logger.getLogger(FuenteExternaAPI.class.getName());

  @Transient
  private FuenteAdapter adaptador;

  // Nuevo campo para persistir la configuración del Adapter.
  @Lob
  private String jsonAdapter;

  // Constructor para JPA.
  protected FuenteExternaAPI() {
    super();
  }

  /**
   * Constructor de la fuente externa.
   *
   * @param nombre     Nombre de la fuente.
   * @param adaptador  Adaptador específico para la API que se va a consumir.
   * @param rutaCopia  Ruta del archivo para la copia local (caché).
   * @param lector     Lector para manejar la persistencia de la caché.
   * @param exportador Exportador para guardar la caché.
   */
  public FuenteExternaAPI(
      String nombre,
      FuenteAdapter adaptador,
      String rutaCopia,
      Lector<Hecho> lector,
      Exportador<Hecho> exportador
  ) {
    super(nombre, rutaCopia, lector, exportador);
    if (adaptador == null) {
      throw new IllegalArgumentException("El adaptador no puede ser nulo.");
    }
    this.adaptador = adaptador;
    // Guardamos la configuración del adaptador para persistirla.
    this.jsonAdapter = adaptador.getConfiguracionJson();
  }

  /**
   * Reconstruye el Adapter y otras dependencias al cargar desde la BD.
   * Llama primero al método de la clase padre.
   */
  @Override
  @PostLoad
  protected void reconstruirDependencias() {
    // 1. Reconstruir Lector y Exportador de la clase padre.
    super.reconstruirDependencias();

    // 2. Reconstruir el Adaptador específico de esta clase.
    if (this.jsonAdapter != null && !this.jsonAdapter.isEmpty()) {
      ObjectMapper mapper = new ObjectMapper();
      AdapterFactory adapterFactory = new AdapterFactory();
      try {
        JsonNode adapterNode = mapper.readTree(this.jsonAdapter);
        this.adaptador = adapterFactory.create(adapterNode);
      } catch (IOException e) {
        throw new RuntimeException("Error al reconstruir el adaptador desde JSON", e);
      }
    }
  }


  /**
   * Consulta los nuevos hechos a través del adaptador de la API.
   * Si la consulta falla, devuelve una lista vacía para evitar
   * corromper la caché con datos antiguos.
   *
   * @return Una lista de nuevos Hechos o una lista vacía en caso de error.
   */
  @Override
  protected List<Hecho> consultarNuevosHechos() {
    try {
      // Nos aseguramos de que el adaptador se haya reconstruido antes de usarlo.
      if (this.adaptador == null) {
        logger.log(
            Level.WARNING,
            "El adaptador para la fuente '" + this.getNombre() + "' es nulo. No se pueden consultar nuevos hechos."
        );
        return Collections.emptyList();
      }
      List<Hecho> hechos = this.adaptador.consultarHechos();
      return hechos;
    } catch (Exception e) {
      logger.log(
          Level.SEVERE,
          "Error al consultar la fuente externa '" + this.getNombre() + "'",
          e
      );
      return Collections.emptyList(); // Devolver vacío para no afectar la caché en caso de error
    }
  }
}

