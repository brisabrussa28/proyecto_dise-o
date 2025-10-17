package ar.edu.utn.frba.dds.repositories;

import ar.edu.utn.frba.dds.model.estadisticas.Estadistica;
import io.github.flbulgarelli.jpa.extras.simple.WithSimplePersistenceUnit;
import java.util.List;

public class EstadisticaRepository implements WithSimplePersistenceUnit {
  private static final EstadisticaRepository INSTANCE = new EstadisticaRepository();

  public static EstadisticaRepository instance() {
    return INSTANCE;
  }

  public void save(Estadistica estadistica) {
    if (estadistica != null) {
      entityManager().persist(estadistica);
    }
  }

  public List<Estadistica> findAll() {
    return entityManager().createQuery("SELECT * FROM Estadistica", Estadistica.class)
                          .getResultList();

  }

  public Estadistica findById(Long id) {
    return entityManager().find(Estadistica.class, id);
  }
}
