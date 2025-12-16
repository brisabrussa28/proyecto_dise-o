package ar.edu.utn.frba.dds.model.lector.csv.filaconverter.model.hibernate;

import ar.edu.utn.frba.dds.model.hecho.Hecho;
import ar.edu.utn.frba.dds.repositories.HechoRepository;
import java.util.List;
import javax.persistence.EntityManager;
import org.hibernate.search.jpa.FullTextEntityManager;
import org.hibernate.search.jpa.Search;
import org.hibernate.search.query.dsl.QueryBuilder;

public class AccesoHecho {
  public final EntityManager em;
  private HechoRepository repo = new HechoRepository();

  public AccesoHecho(EntityManager em) {
    this.em = em;
  }

  public void guardar(Hecho hecho) {
    repo.save(hecho);
  }

  public List<Hecho> fullTextSearch(String palabraClave, int cantidadResultados) {
    FullTextEntityManager fullTextEntityManager = Search.getFullTextEntityManager(em);

    QueryBuilder qb = fullTextEntityManager.getSearchFactory()
                                           .buildQueryBuilder()
                                           .forEntity(Hecho.class)
                                           .get();

    org.apache.lucene.search.Query luceneQuery = qb
        .keyword()
        .fuzzy()
        .withEditDistanceUpTo(2)
        .onFields("hecho_titulo", "hecho_descripcion")
        .matching(palabraClave)
        .createQuery();

    javax.persistence.Query jpaQuery =
        fullTextEntityManager.createFullTextQuery(luceneQuery, Hecho.class);

    jpaQuery.setMaxResults(cantidadResultados);

    return jpaQuery.getResultList();
  }
}