package ar.edu.utn.frba.dds.controller;

import ar.edu.utn.frba.dds.model.fuentes.Fuente;
import ar.edu.utn.frba.dds.model.fuentes.FuenteDinamica;
import ar.edu.utn.frba.dds.repositories.FuenteRepository;
import java.util.List;

public class FuenteController {
  public List<Fuente> findAll() {
    return FuenteRepository.instance()
                           .findAll();
  }

//  public Fuente crearFuente(String nombre, UploadedFile archivo) {
//    InputStream contenido = archivo.content();
//    LectorCSV<Hecho> lector = new LectorCSV<Hecho>(',', /*Aca va el converter*/);
//    List<Hecho> hechosImportados = lector.importar(contenido);
//    Fuente fuente = new FuenteEstatica(nombre, hechosImportados);
//    FuenteRepository.instance()
//                    .save(fuente);
//    return fuente;
//  }

  public Fuente crearFuente(String nombre) {
    Fuente fuente = new FuenteDinamica(nombre);
    FuenteRepository.instance()
                    .save(fuente);
    return fuente;
  }
}
