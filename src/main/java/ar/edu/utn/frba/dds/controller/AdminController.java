package ar.edu.utn.frba.dds.controller;

import ar.edu.utn.frba.dds.model.coleccion.Coleccion;
import ar.edu.utn.frba.dds.model.coleccion.algoritmosconsenso.Absoluta;
import ar.edu.utn.frba.dds.model.coleccion.algoritmosconsenso.AlgoritmoDeConsenso;
import ar.edu.utn.frba.dds.model.coleccion.algoritmosconsenso.MayoriaSimple;
import ar.edu.utn.frba.dds.model.coleccion.algoritmosconsenso.MultiplesMenciones;
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
import ar.edu.utn.frba.dds.repositories.ColeccionRepository;
import ar.edu.utn.frba.dds.repositories.FuenteRepository;
import ar.edu.utn.frba.dds.repositories.HechoRepository;
import ar.edu.utn.frba.dds.repositories.SolicitudesRepository;
import io.javalin.http.Context;
import io.javalin.http.UploadedFile;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AdminController {
  public void mostrarDashboard(Context ctx, Map<String, Object> model) {

    Long totalHechos = HechoRepository.instance()
                                      .countAll();
    Long totalColecciones = ColeccionRepository.instance()
                                               .countAll();
    Long totalReportes = SolicitudesRepository.instance()
                                              .cantidadTotal();

    model.put("cantHechos", totalHechos);
    model.put("cantColecciones", totalColecciones);
    model.put("cantReportes", totalReportes);
    ctx.render("admin/dashboard.hbs", model);
  }

  public void listarColecciones(Context ctx, Map<String, Object> model) {
    List<Coleccion> colecciones = ColeccionRepository.instance()
                                                     .findAll();
    List<Fuente> fuentes = FuenteRepository.instance()
                                           .findAll();
    model.put("colecciones", colecciones);
    model.put("fuentes", fuentes);
    ctx.render("/admin/colecciones-lista.hbs", model);
  }

  public void editarColeccion(Context ctx, Map<String, Object> model) {
    Long id = Long.parseLong(ctx.pathParam("id"));
    Coleccion coleccion = ColeccionRepository.instance()
                                             .findById(id);
    List<Fuente> fuentes = FuenteRepository.instance()
                                           .findAll();
    model.put("coleccion", coleccion);
    model.put("fuentesDisponibles", fuentes);


    ctx.render("/admin/coleccion-detalle.hbs", model);
  }

  public void agregarHechoAColeccion(Context ctx) {
    Long idColeccion = Long.parseLong(ctx.pathParam("id"));
    Long idHecho = Long.parseLong(ctx.pathParam("hecho_id"));

    Coleccion col = ColeccionRepository.instance()
                                       .findById(idColeccion);
    Hecho hecho = HechoRepository.instance()
                                 .findById(idHecho);

    if (col != null && hecho != null) {
      col.getHechosConsensuados()
         .add(hecho);
      ColeccionRepository.instance()
                         .save(col);
    }

    ctx.redirect("/admin/colecciones/" + idColeccion);
  }

  public void removerHechoDeColeccion(Context ctx) {
    Long idColeccion = Long.parseLong(ctx.pathParam("id"));
    Long idHecho = Long.parseLong(ctx.pathParam("hecho_id"));

    Coleccion col = ColeccionRepository.instance()
                                       .findById(idColeccion);
    col.getHechosConsensuados()
       .removeIf(hecho -> hecho.getId()
                               .equals(idHecho));

    ColeccionRepository.instance()
                       .save(col);
    ctx.redirect("/admin/colecciones" + idColeccion);
  }

  public void crearColeccion(Context ctx) {
    try {
      String nombre = ctx.formParam("nombre");
      String descripcion = ctx.formParam("descripcion");
      String categoria = ctx.formParam("categoria");

      String algoTipo = ctx.formParam("algoritmo_tipo");
      AlgoritmoDeConsenso algoritmo = null;
      switch (algoTipo) {
        case "Absoluta":
          algoritmo = new Absoluta();
          break;
        case "May_simple":
          algoritmo = new MayoriaSimple();
          break;
        case "Mult_menciones":
          algoritmo = new MultiplesMenciones();
          break;
        default:
          algoritmo = new MayoriaSimple(); // Default seguro
      }

      String modoFuente = ctx.formParam("modo_fuente");
      Fuente fuente;

      if ("nueva".equals(modoFuente)) {
        fuente = this.nuevaFuente(ctx);
        FuenteRepository.instance()
                        .save(fuente);
      } else {
        Long fuenteId = Long.parseLong(ctx.formParam("fuente_id"));
        fuente = FuenteRepository.instance()
                                 .findById(fuenteId);
      }

      ColeccionRepository.instance()
                         .save(new Coleccion(nombre, fuente, descripcion, categoria, algoritmo));
      ctx.redirect("/admin/colecciones");
    } catch (Exception e) {
      e.printStackTrace();
      ctx.status(400)
         .result("Error al crear colección: " + e.getMessage());
    }
  }

  public void listarFuentes(Context ctx, Map<String, Object> model) {
    List<Fuente> fuentes = FuenteRepository.instance()
                                           .findAll();
    model.put("fuentes", fuentes);
    ctx.render("/admin/fuentes-lista.hbs", model);
  }

  public void crearFuente(Context ctx) {
    Fuente nuevaFuente = this.nuevaFuente(ctx);

    FuenteRepository.instance()
                    .save(nuevaFuente);
    ctx.redirect("/admin/fuentes");
  }

  public void configurarColeccion(Context ctx) {
    try {
      Long id = Long.parseLong(ctx.pathParam("id"));
      Coleccion col = ColeccionRepository.instance()
                                         .findById(id);
      // A. Actualizar FUENTE
      String fuenteIdStr = ctx.formParam("fuente_id");
      if (fuenteIdStr != null) {
        Long fuenteId = Long.parseLong(fuenteIdStr);
        Fuente nuevaFuente = FuenteRepository.instance()
                                             .findById(fuenteId);
        col.setFuente(nuevaFuente); // Asegúrate de tener este setter en Coleccion
      }

      // B. Actualizar ALGORITMO
      String algoTipo = ctx.formParam("algoritmo_tipo");
      if (algoTipo != null) {
        AlgoritmoDeConsenso nuevoAlgo = null;
        switch (algoTipo) {
          case "Absoluta":
            nuevoAlgo = new Absoluta();
            break;
          case "May_simple":
            nuevoAlgo = new MayoriaSimple();
            break;
          case "Mult_menciones":
            nuevoAlgo = new MultiplesMenciones();
            break;
        }
        // Importante: Reemplazamos el algoritmo anterior
        col.setAlgoritmoDeConsenso(nuevoAlgo);
      }

      // Guardamos los cambios
      ColeccionRepository.instance()
                         .save(col);

      // Volvemos a la misma pantalla con mensaje de éxito (opcional) o redirect simple
      ctx.redirect("/admin/colecciones/" + id);

    } catch (Exception e) {
      e.printStackTrace();
      ctx.status(500)
         .result("Error al actualizar colección: " + e.getMessage());
    }
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

  private Fuente nuevaFuente(Context ctx) {
    String nombreFuente = ctx.formParam("nueva_fuente_nombre");
    String tipoFuente = ctx.formParam("nueva_fuente_tipo");

    switch (tipoFuente) {
      case "DINAMICA":
        return new FuenteDinamica(nombreFuente);
      case "ESTATICA":
        UploadedFile archivo = ctx.uploadedFile("archivo_fuente");

        ConfiguracionLector configLector = determinarConfig(archivo.filename());
        Lector<Hecho> lector = configLector.build(Hecho.class);
        List<Hecho> hechosImportados = lector.importar(archivo.content());
        hechosImportados.forEach(hecho -> {
          if (hecho.getOrigen() == null) {
            hecho.setOrigen(Origen.DATASET);
          }
        });
        System.out.println(hechosImportados);
        return new FuenteEstatica(nombreFuente, hechosImportados);
      case "API":
        break;
    }

    throw new RuntimeException("NO HAY UN FORMATO DE FUENTE COMPATIBLE");
  }

  private Map<String, List<String>> convertirMapeoAString(Map<CampoHecho, List<String>> mapeoEnum) {
    return mapeoEnum.entrySet()
                    .stream()
                    .collect(Collectors.toMap(
                        entry -> entry.getKey()
                                      .name(), Map.Entry::getValue
                    ));
  }
}
