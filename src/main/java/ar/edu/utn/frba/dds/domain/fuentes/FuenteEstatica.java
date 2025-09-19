package ar.edu.utn.frba.dds.domain.fuentes;

import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import ar.edu.utn.frba.dds.domain.lector.Lector;
import ar.edu.utn.frba.dds.domain.lector.LectorFactory;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Transient;

/**
 * Fuente de datos estática que lee desde un archivo utilizando un lector.
 * Persiste la configuración de su Lector como JSON.
 */
@Entity
@DiscriminatorValue("ESTATICA")
public class FuenteEstatica extends Fuente {

  private String rutaArchivo;

  @Transient // El lector no es persistible.
  private Lector<Hecho> lector;

  @Column(name = "json_lector")
  protected String jsonLector; // Campo para persistir la configuración del Lector.

  // Constructor para JPA.
  protected FuenteEstatica() {
    super();
  }

  /**
   * Constructor de la fuente estática.
   *
   * @param nombre      Nombre de la fuente.
   * @param rutaArchivo Ruta del archivo que contiene los datos (e.g., "datos.csv").
   * @param lector      Implementación de lector para leer los datos.
   */
  public FuenteEstatica(String nombre, String rutaArchivo, Lector<Hecho> lector) {
    super(nombre);
    if (rutaArchivo == null || lector == null) {
      throw new IllegalArgumentException("La ruta del archivo y el lector deben estar definidos.");
    }
    this.rutaArchivo = rutaArchivo;
    this.lector = lector;
    this.jsonLector = lector.getConfiguracionJson();
  }

  /**
   * Reconstruye el Lector al cargar la entidad desde la base de datos.
   */
  protected void reconstruirDependencias() {
    if (this.jsonLector != null && !this.jsonLector.isEmpty()) {
      ObjectMapper mapper = new ObjectMapper();
      LectorFactory lectorFactory = new LectorFactory();
      try {
        JsonNode lectorNode = mapper.readTree(this.jsonLector);
        this.lector = lectorFactory.create(lectorNode, Hecho.class);
      } catch (IOException e) {
        throw new RuntimeException(
            "Error al reconstruir el lector desde JSON para FuenteEstatica",
            e
        );
      }
    }
  }

  /**
   * Trae los hechos de la fuente estática utilizando el lector.
   *
   * @return Lista de hechos importados desde el archivo.
   */
  @Override
  public List<Hecho> obtenerHechos() {
    if (this.lector == null) {
      throw new IllegalStateException(
          "El lector no fue inicializado. La reconstrucción post-carga podría haber fallado.");
    }
    return lector.importar(rutaArchivo);
  }
}

