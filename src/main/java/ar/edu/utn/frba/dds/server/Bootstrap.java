package ar.edu.utn.frba.dds.server;

import ar.edu.utn.frba.dds.model.coleccion.Coleccion;
import ar.edu.utn.frba.dds.model.coleccion.algoritmosconsenso.Verdadero;
import ar.edu.utn.frba.dds.model.fuentes.Fuente;
import ar.edu.utn.frba.dds.model.fuentes.FuenteDinamica;
import ar.edu.utn.frba.dds.repositories.ColeccionRepository;
import ar.edu.utn.frba.dds.repositories.FuenteRepository;
import io.github.flbulgarelli.jpa.extras.simple.WithSimplePersistenceUnit;

public class Bootstrap implements WithSimplePersistenceUnit {

  public void init() {
    withTransaction(() -> {
      Fuente fuenteGlobal = FuenteRepository.instance()
                                            .findFuenteByNombre("Fuente global de hechos");
      if (fuenteGlobal == null) {
        fuenteGlobal = new FuenteDinamica("Fuente global de hechos");
        fuenteGlobal = entityManager().merge(fuenteGlobal);
      }
      Coleccion coleccionGlobal = ColeccionRepository.instance()
                                                     .findColeccionByTitulo(
                                                         "Coleccion global");
      if (coleccionGlobal == null) {
        coleccionGlobal = new Coleccion(
            "Coleccion global",
            fuenteGlobal,
            "Coleccion que engloba todos los hechos subidos al sistema.",
            "GLOBAL",
            new Verdadero()
      );
        entityManager().merge(coleccionGlobal);
      }
    });
  }
}