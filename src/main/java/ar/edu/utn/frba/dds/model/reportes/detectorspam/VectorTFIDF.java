package ar.edu.utn.frba.dds.model.reportes.detectorspam;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Table(name = "tfidf_vectores")
public class VectorTFIDF {

  @Transient
  private static final ObjectMapper objectMapper = new ObjectMapper();

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "texto_original", columnDefinition = "TEXT", nullable = false)
  private String textoOriginal;

  @Column(name = "vector_json", columnDefinition = "TEXT", nullable = false)
  private String vectorJson;

  @Column(name = "es_spam", nullable = false)
  private boolean esSpam;

  @Column(name = "fecha_creacion", nullable = false)
  private LocalDateTime fechaCreacion;

  // Constructor por defecto requerido por JPA
  public VectorTFIDF() {
    this.fechaCreacion = LocalDateTime.now();
  }

  // Constructor útil
  public VectorTFIDF(String textoOriginal, Map<String, Double> vector, boolean esSpam) {
    this();
    this.textoOriginal = textoOriginal;
    this.esSpam = esSpam;
    setVector(vector);
  }

  // Método para obtener el vector deserializado
  @Transient
  public Map<String, Double> getVector() {
    try {
      if (vectorJson == null || vectorJson.isEmpty()) {
        return Map.of();
      }
      return objectMapper.readValue(vectorJson,
                                    new TypeReference<Map<String, Double>>() {});
    } catch (JsonProcessingException e) {
      throw new RuntimeException("Error al deserializar vector JSON: " + e.getMessage(), e);
    }
  }

  // Método para establecer el vector (serializa a JSON)
  @Transient
  public void setVector(Map<String, Double> vector) {
    try {
      this.vectorJson = objectMapper.writeValueAsString(vector);
    } catch (JsonProcessingException e) {
      throw new RuntimeException("Error al serializar vector a JSON: " + e.getMessage(), e);
    }
  }

  // Getters y Setters estándar
  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getTextoOriginal() {
    return textoOriginal;
  }

  public void setTextoOriginal(String textoOriginal) {
    this.textoOriginal = textoOriginal;
  }

  // Getter para el JSON (usado por JPA)
  public String getVectorJson() {
    return vectorJson;
  }

  // Setter para el JSON (usado por JPA)
  public void setVectorJson(String vectorJson) {
    this.vectorJson = vectorJson;
  }

  public boolean isEsSpam() {
    return esSpam;
  }

  public void setEsSpam(boolean esSpam) {
    this.esSpam = esSpam;
  }

  public LocalDateTime getFechaCreacion() {
    return fechaCreacion;
  }

  public void setFechaCreacion(LocalDateTime fechaCreacion) {
    this.fechaCreacion = fechaCreacion;
  }

  @Override
  public String toString() {
    return "VectorTFIDF{" +
        "id=" + id +
        ", textoOriginal='" + (textoOriginal != null ?
                               (textoOriginal.length() > 30 ? textoOriginal.substring(0, 30) + "..." : textoOriginal)
                                                     : "null") + "'" +
        ", esSpam=" + esSpam +
        ", fechaCreacion=" + fechaCreacion +
        ", vectorSize=" + (vectorJson != null ? vectorJson.length() : 0) + " chars" +
        '}';
  }
}