package ar.edu.utn.frba.dds.controller;

import ar.edu.utn.frba.dds.model.fuentes.Fuente;
import ar.edu.utn.frba.dds.model.fuentes.FuenteDinamica;
import ar.edu.utn.frba.dds.model.fuentes.FuenteEstatica;
import ar.edu.utn.frba.dds.model.hecho.Hecho;
import ar.edu.utn.frba.dds.model.lector.csv.LectorCSV;
import ar.edu.utn.frba.dds.model.lector.csv.filaconverter.HechoFilaConverter;
import ar.edu.utn.frba.dds.repositories.FuenteRepository;
import io.javalin.http.UploadedFile;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

public class FuenteController {
  public List<Fuente> findAll() {
    return FuenteRepository.instance()
                           .findAll();
  }

  public Fuente crearFuente(
      String nombre,
      UploadedFile archivo,
      String formatoFecha,
      Map<String, List<String>> columnas,
      char separador
  ) {
    InputStream contenido = archivo.content();
    HechoFilaConverter hechoFilaConverter = new HechoFilaConverter(formatoFecha, columnas);
    LectorCSV<Hecho> lector = new LectorCSV<Hecho>(separador, hechoFilaConverter);
    List<Hecho> hechosImportados = lector.importar(contenido);
    Fuente fuente = new FuenteEstatica(nombre, hechosImportados);
    FuenteRepository.instance()
                    .save(fuente);
    return fuente;
  }

  public Fuente crearFuente(String nombre) {
    Fuente fuente = new FuenteDinamica(nombre);
    FuenteRepository.instance()
                    .save(fuente);
    return fuente;
  }

  public Fuente findById(Long id) {
    return FuenteRepository.instance()
                           .findById(id);
  }
}
