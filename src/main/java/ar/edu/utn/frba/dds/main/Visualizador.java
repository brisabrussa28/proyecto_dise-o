package ar.edu.utn.frba.dds.main;

import ar.edu.utn.frba.dds.domain.coleccion.Coleccion;
import ar.edu.utn.frba.dds.domain.filtro.Filtro;
import ar.edu.utn.frba.dds.domain.fuentes.FuenteDinamica;
import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import java.util.List;

/**
 * Visualizador.
 */
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
  //[✔️] TODO: no debe visualizar hechos de una fuente sino de una coleccion.

  // No requerirá identificarse, y podrá subir hechos si así lo quisiera manteniendo su anonimato

  public void agregarHechoAFuente(FuenteDinamica fuente, Hecho hecho) {
    fuente.agregarHecho(hecho);
  }


  public List<Hecho> filtrar(Coleccion coleccion, Filtro filtro) {
    return filtro.filtrar(coleccion.getHechos());
  }

}
