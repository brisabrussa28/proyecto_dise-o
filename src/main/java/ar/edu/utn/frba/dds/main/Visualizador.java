package ar.edu.utn.frba.dds.main;

import ar.edu.utn.frba.dds.domain.Coleccion.Coleccion;
import ar.edu.utn.frba.dds.domain.fuentes.Fuente;
import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import ar.edu.utn.frba.dds.domain.info.Etiqueta;
import java.util.List;

public class Visualizador extends Persona {

  public Visualizador(String nombre, String email) {
    super(nombre, email);
  }
/*
  - Como persona visualizadora, deseo navegar todos los hechos disponibles de una colección.
  - Como persona visualizadora, deseo navegar los hechos disponibles de una colección, aplicando filtros.
*/
  public List<Hecho> visualizarHechos(Coleccion coleccion) {
    return coleccion.getHechos();
  }
  //TODO: no debe visualizar hechos de una fuente sino de una coleccion.


  public List<Hecho> filtrarPorEtiqueta(List<Hecho> hechos, Etiqueta etiqueta) {
    return hechos.stream()
        .filter(h -> h.tieneEtiqueta(etiqueta))
        .toList();
  }
  //TODO: El filtro y la aplicacion del mismo debe gestionarlo coleccion. El filtro debe ser un booleano que actua en un filter sobre los hechos

  public List<Hecho> filtrarPorCategoria(List<Hecho> hechos, String categoria) {
    return hechos.stream()
        .filter(h -> h.esDeCategoria(categoria))
        .toList();
  }


  // No requerirá identificarse, y podrá subir hechos si así lo quisiera manteniendo su anonimato
}
/*
Cada hecho representa una pieza de información, la cual debe contener mínimamente: título, descripción, categoría,
contenido multimedia opcional, lugar y fecha del acontecimiento, fecha de carga y su origen (carga manual,
proveniente de un dataset o provisto por un contribuyente). Idealmente, todos estos campos podrán ser utilizados por
cualquier visitante como filtros de búsqueda desde la interfaz gráfica y como criterios de pertenencia a una colección.



 */