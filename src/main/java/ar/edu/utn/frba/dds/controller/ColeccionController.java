package ar.edu.utn.frba.dds.controller;

import ar.edu.utn.frba.dds.dto.ColeccionDTO;
import ar.edu.utn.frba.dds.model.coleccion.Coleccion;
import ar.edu.utn.frba.dds.model.coleccion.algoritmosconsenso.Absoluta;
import ar.edu.utn.frba.dds.model.coleccion.algoritmosconsenso.AlgoritmoDeConsenso;
import ar.edu.utn.frba.dds.model.coleccion.algoritmosconsenso.MayoriaSimple;
import ar.edu.utn.frba.dds.model.coleccion.algoritmosconsenso.MultiplesMenciones;
import ar.edu.utn.frba.dds.model.fuentes.Fuente;
import ar.edu.utn.frba.dds.repositories.AlgoritmoRepository;
import ar.edu.utn.frba.dds.repositories.ColeccionRepository;
import ar.edu.utn.frba.dds.repositories.FuenteRepository;

public class ColeccionController {
  public Coleccion crearColeccion(ColeccionDTO coleccionDTO) {
    Fuente fuente = FuenteRepository.instance()
                                    .findById(coleccionDTO.coleccion_fuente);
    AlgoritmoDeConsenso algoritmo;
    switch (coleccionDTO.coleccion_algoritmo) {
      case "May_simple":
        algoritmo = new MayoriaSimple();
      case "Absoluta":
        algoritmo = new Absoluta();
      case "Mult_menciones":
        algoritmo = new MultiplesMenciones();
      default:
        algoritmo = new MayoriaSimple();
    }
    Coleccion coleccion = new Coleccion(
        coleccionDTO.coleccion_titulo,
        fuente,
        coleccionDTO.coleccion_descripcion,
        coleccionDTO.coleccion_categoria,
        algoritmo
    );
    AlgoritmoRepository.instance()
                       .save(algoritmo);
    ColeccionRepository.instance()
                       .save(coleccion);
    return coleccion;
  }
}
