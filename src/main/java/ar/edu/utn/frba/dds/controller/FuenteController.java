package ar.edu.utn.frba.dds.controller;

import ar.edu.utn.frba.dds.model.fuentes.Fuente;
import ar.edu.utn.frba.dds.model.fuentes.FuenteDinamica;
import ar.edu.utn.frba.dds.model.fuentes.FuenteEstatica;
import ar.edu.utn.frba.dds.model.hecho.CampoHecho;
import ar.edu.utn.frba.dds.model.hecho.Hecho;
import ar.edu.utn.frba.dds.model.hecho.Origen;
import ar.edu.utn.frba.dds.model.lector.Lector;
import ar.edu.utn.frba.dds.model.lector.configuracion.ConfiguracionLector;
import ar.edu.utn.frba.dds.model.lector.configuracion.ConfiguracionLectorCsv;
import ar.edu.utn.frba.dds.model.lector.configuracion.ConfiguracionLectorJson;
import ar.edu.utn.frba.dds.model.lector.csv.LectorCSV;
import ar.edu.utn.frba.dds.model.lector.csv.filaconverter.HechoFilaConverter;
import ar.edu.utn.frba.dds.repositories.FuenteRepository;
import io.javalin.http.Context;
import io.javalin.http.UploadedFile;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class FuenteController {
  public List<Fuente> findAll() {
    return FuenteRepository.instance()
                           .findAll();
  }

  public Fuente crearFuente(Context ctx) {
    String nombreFuente = ctx.formParam("nueva_fuente_nombre");
    String tipoFuente = ctx.formParam("nueva_fuente_tipo");
    Fuente fuente = null;

    switch (tipoFuente) {
      case "DINAMICA":
        fuente = new FuenteDinamica(nombreFuente);
        FuenteRepository.instance()
                        .save(fuente);
        break;
      case "ESTATICA":
        UploadedFile archivo = ctx.uploadedFile("archivo_fuente");
        if (archivo != null && !archivo.filename()
                                       .isEmpty()) {
          ConfiguracionLector configLector = determinarConfig(archivo.filename());
          Lector<Hecho> lector = configLector.build(Hecho.class);
          List<Hecho> hechosImportados = lector.importar(archivo.content());
          hechosImportados.forEach(hecho -> {
            if (hecho.getOrigen() == null) {
              hecho.setOrigen(Origen.DATASET);
            }
          });
          System.out.println(hechosImportados);
          fuente = new FuenteEstatica(nombreFuente, hechosImportados);
          FuenteRepository.instance()
                          .save(fuente);
        }
        break;
      case "API":
        break;
    }
    return fuente;
  }

  public Fuente findById(Long id) {
    return FuenteRepository.instance()
                           .findById(id);
  }

  private ConfiguracionLector determinarConfig(String fileName) {
    if (fileName.toLowerCase()
                .endsWith(".csv")) {

      String formatoPolimorfico = "[yyyy-MM-dd'T'HH:mm:ss.SSSSSSSSS]" + "[yyyy-MM-dd'T'HH:mm:ss]" + "[yyyy-MM-dd HH:mm]" + "[yyyy-MM-dd]" + "[dd/MM/yyyy]" + "[dd/MM/yyyy HH:mm]";
      return new ConfiguracionLectorCsv(
          ',',
          formatoPolimorfico,
          this.crearMapeoColumnas()
      );
    } else if (fileName.toLowerCase()
                       .endsWith(".json")) {
      return new ConfiguracionLectorJson();
    }
    throw new RuntimeException("Formato de archivo no soportado: " + fileName);
  }

  private Map<String, List<String>> crearMapeoColumnas() {
    Map<String, List<String>> mapeoColumnas = convertirMapeoAString(Map.of(
        CampoHecho.TITULO,
        List.of("titulo", "TITULO", "hecho_titulo"),
        CampoHecho.DESCRIPCION,
        List.of("descripcion", "DESCRIPCION", "hecho_descripcion"),
        CampoHecho.LATITUD,
        List.of("latitud", "LATITUD"),
        CampoHecho.LONGITUD,
        List.of("longitud", "LONGITUD"),
        CampoHecho.FECHA_SUCESO,
        List.of("fechaSuceso", "FECHASUCESO", "hecho_fecha_suceso"),
        CampoHecho.CATEGORIA,
        List.of("categoria", "CATEGORIA", "hecho_categoria"),
        CampoHecho.DIRECCION,
        List.of("direccion", "DIRECCION", "hecho_ubicacion", "hecho_direccion"),
        CampoHecho.PROVINCIA,
        List.of("provincia", "PROVINCIA", "hecho_provincia"),
        CampoHecho.ETIQUETAS,
        List.of("etiquetas", "ETIQUETAS", "hecho_etiquetas")
    ));
    return mapeoColumnas;
  }

  private Map<String, List<String>> convertirMapeoAString(Map<CampoHecho, List<String>> mapeoEnum) {
    return mapeoEnum.entrySet()
                    .stream()
                    .collect(Collectors.toMap(
                        entry -> entry.getKey()
                                      .name(), Map.Entry::getValue
                    ));
  }

  public void save(Fuente fuente) {
    FuenteRepository.instance()
                    .save(fuente);
  }

}
