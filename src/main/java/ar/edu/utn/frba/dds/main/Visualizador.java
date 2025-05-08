package ar.edu.utn.frba.dds.main;

import ar.edu.utn.frba.dds.domain.Coleccion.Coleccion;
import ar.edu.utn.frba.dds.domain.Origen.Origen;
import ar.edu.utn.frba.dds.domain.fuentes.FuenteDinamica;
import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import java.time.LocalDateTime;
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
  //[✔️] TODO: no debe visualizar hechos de una fuente sino de una coleccion.

// No requerirá identificarse, y podrá subir hechos si así lo quisiera manteniendo su anonimato

  public void agregarHechoAFuente(FuenteDinamica fuente, Hecho hecho) {
    fuente.agregarHecho(hecho);
  }


  public List<Hecho> filtrar(List<Hecho> hechos, String etiqueta) {
    return hechos.stream().filter(h -> h.tieneEtiqueta(etiqueta)).toList();
  }
  //[✔️] TODO: El filtro y la aplicacion del mismo debe gestionarlo coleccion. El filtro debe ser un booleano que actua en un filter sobre los hechos

  public List<Hecho> filtrarPorCategoria(List<Hecho> hechos, String categoria) {
    return hechos.stream().filter(h -> h.esDeCategoria(categoria)).toList();
  }

  public List<Hecho> filtrarPorTitulo(List<Hecho> hechos, String titulo) {
    return hechos.stream().filter(hecho -> hecho.esDeTitulo(titulo)).toList();
  }

  public List<Hecho> filtrarPorLugar(List<Hecho> hechos, String direccion) {
    return hechos.stream().filter(hecho -> hecho.sucedioEn(direccion)).toList();
  }

  public List<Hecho> filtrarPorFecha(List<Hecho> hechos, LocalDateTime fecha) {
    return hechos.stream().filter(hecho -> hecho.esDeFecha(fecha)).toList();
  }

  public List<Hecho> filtrarPorFechaDeCarga(List<Hecho> hechos, LocalDateTime fechaDeCarga) {
    return hechos.stream().filter(hecho -> hecho.seCargoEn(fechaDeCarga)).toList();
  }

  public List<Hecho> filtrarPorOrigen(List<Hecho> hechos, Origen origen) {
    return hechos.stream().filter(hecho -> hecho.esDeOrigen(origen)).toList();
  }

}
/*
  Cada hecho representa una pieza de información, la cual debe contener mínimamente:
  título, descripción, categoría, contenido multimedia opcional, lugar y fecha del
  acontecimiento,fecha de carga y su origen (carga manual,
  proveniente de un dataset o provisto por un contribuyente). Idealmente,
  todos estos campos podrán ser utilizados por
  cualquier visitante como filtros de búsqueda desde la interfaz gráfica y como
  criterios de pertenencia a una colección.
*/