package ar.edu.utn.frba.dds.domain.hibernate;

import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import ar.edu.utn.frba.dds.domain.repos.HechoRepository;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import javax.persistence.EntityManager;
import org.hibernate.search.mapper.orm.Search;
import org.hibernate.search.mapper.orm.session.SearchSession;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.FullTextField;


/**
 * AccesoHecho.
 */
public class AccesoHecho {
  public final EntityManager em;
  private static final String[] campos = obtenerCamposIndexados(Hecho.class);
  private HechoRepository repo = new HechoRepository();

  public AccesoHecho(EntityManager em) {
    this.em = em;
  }

  /**
  * Guarda un hecho
  *
  * @param hecho Hecho a guardar
  */
  public void guardar(Hecho hecho) {
    repo.save(hecho);
  }

  public List<Hecho> fullTextSearch(String palabraClave, int cantidadResultados) {
    // Obtiene una sesión de búsqueda de Hibernate Search
    SearchSession searchSession = Search.session(em);
    // Define que se va a buscar en la entidad Texto
    return searchSession.search(Hecho.class)
                        // Realiza una búsqueda de coincidencia en los campos especificados
                        .where(f -> f.match()
                                     .fields(campos)
                                     .matching(palabraClave)
                                     .fuzzy(2))
                        // Devuelve una lista de resultados que coinciden con la búsqueda
                        .fetchHits(cantidadResultados);
  }

  /**
   * Devuelve una cadena String con los nombres de las columnas indexadas
   */
  public static String[] obtenerCamposIndexados(Class<?> clase) {
    return Arrays.stream(clase.getDeclaredFields())
                 .filter(f -> f.isAnnotationPresent(FullTextField.class))
                 .map(Field::getName)
                 .toArray(String[]::new);
  }
}
