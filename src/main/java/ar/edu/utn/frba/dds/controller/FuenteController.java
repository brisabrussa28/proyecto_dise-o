package ar.edu.utn.frba.dds.controller;

import ar.edu.utn.frba.dds.model.fuentes.Fuente;
import ar.edu.utn.frba.dds.model.fuentes.FuenteDeAgregacion;
import ar.edu.utn.frba.dds.model.fuentes.FuenteDinamica;
import ar.edu.utn.frba.dds.model.fuentes.FuenteEstatica;
import ar.edu.utn.frba.dds.model.fuentes.FuenteExternaAPI;
import ar.edu.utn.frba.dds.model.fuentes.apis.configuracion.ConfiguracionAdapter;
import ar.edu.utn.frba.dds.model.fuentes.apis.configuracion.ConfiguracionAdapterDemo;
import ar.edu.utn.frba.dds.model.hecho.CampoHecho;
import ar.edu.utn.frba.dds.model.hecho.Hecho;
import ar.edu.utn.frba.dds.model.hecho.Origen;
import ar.edu.utn.frba.dds.model.lector.Lector;
import ar.edu.utn.frba.dds.model.lector.configuracion.ConfiguracionLector;
import ar.edu.utn.frba.dds.model.lector.configuracion.ConfiguracionLectorCsv;
import ar.edu.utn.frba.dds.model.lector.configuracion.ConfiguracionLectorJson;
import ar.edu.utn.frba.dds.repositories.FuenteRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.javalin.http.Context;
import io.javalin.http.UploadedFile;
import java.util.HashMap; // Necesario para el mapa manual
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class FuenteController {

  private static final ObjectMapper mapper = new ObjectMapper();

  // --- MÉTODOS DE BÚSQUEDA ---

  public List<Fuente> findAll() {
    return FuenteRepository.instance().findAll();
  }

  public List<Fuente> findAll(boolean soloSimples) {
    List<Fuente> todas = this.findAll();
    if (soloSimples) {
      return todas.stream()
                  .filter(f -> !(f instanceof FuenteDeAgregacion))
                  .collect(Collectors.toList());
    }
    return todas;
  }

  /**
   * Método CLAVE para el frontend: Convierte las fuentes a Map e inyecta el campo "tipo".
   * CORRECCIÓN: Se construye el Map manualmente para evitar la serialización profunda
   * de 'hechos' que causa errores con LocalDateTime y Jackson sin el módulo JSR310.
   */
  public List<Map<String, Object>> obtenerFuentesConTipo(boolean soloSimples) {
    List<Fuente> fuentes = this.findAll(soloSimples);

    return fuentes.stream().map(f -> {
      // 1. Crear Map manualmente.
      // Esto evita que Jackson intente serializar f.getHechos() y falle con las fechas.
      Map<String, Object> dto = new HashMap<>();
      dto.put("id", f.getId());
      dto.put("nombre", f.getNombre());
      // Si la propiedad en tu clase Fuente se llama distinto (ej: getFuente_nombre), ajusta aquí.
      // Asumimos getNombre() por convención estándar.

      // 2. Determinar Tipo explícitamente
      String tipo = "OTRO";
      if (f instanceof FuenteDinamica) tipo = "DINAMICA";
      else if (f instanceof FuenteEstatica) tipo = "ESTATICA";
      else if (f instanceof FuenteDeAgregacion) tipo = "AGREGACION";
      else if (f instanceof FuenteExternaAPI) tipo = "API";

      // 3. Inyectar campo
      dto.put("tipo", tipo);
      return dto;
    }).collect(Collectors.toList());
  }

  public Fuente findById(Long id) {
    return FuenteRepository.instance().findById(id);
  }

  // --- MÉTODOS DE CREACIÓN ---

  public Fuente crearFuente(Context ctx) {
    String nombreFuente = ctx.formParam("nueva_fuente_nombre");
    String tipoFuente = ctx.formParam("nueva_fuente_tipo");
    Fuente fuente = null;

    switch (tipoFuente) {
      case "DINAMICA":
        fuente = new FuenteDinamica(nombreFuente);
        FuenteRepository.instance().save(fuente);
        break;

      case "ESTATICA":
        UploadedFile archivo = ctx.uploadedFile("archivo_fuente");
        if (archivo != null && !archivo.filename().isEmpty()) {
          ConfiguracionLector configLector = determinarConfig(archivo.filename());
          Lector<Hecho> lector = configLector.build(Hecho.class);
          List<Hecho> hechosImportados = lector.importar(archivo.content());
          hechosImportados.forEach(hecho -> {
            if (hecho.getOrigen() == null) hecho.setOrigen(Origen.DATASET);
          });
          fuente = new FuenteEstatica(nombreFuente, hechosImportados);
          FuenteRepository.instance().save(fuente);
        }
        break;

      case "API":
        String urlApi = ctx.formParam("url");
        if (urlApi != null && !urlApi.isBlank()) {
          // CORRECCIÓN URL: Validar Protocolo para evitar MalformedURLException
          if (!urlApi.startsWith("http://") && !urlApi.startsWith("https://")) {
            urlApi = "http://" + urlApi;
          }

          ConfiguracionAdapter config = new ConfiguracionAdapterDemo(urlApi);
          fuente = new FuenteExternaAPI(nombreFuente, config);
          FuenteRepository.instance().save(fuente);
        }
        break;

      case "AGREGACION":
        FuenteDeAgregacion fuenteAgregacion = new FuenteDeAgregacion(nombreFuente);
        List<String> idsSeleccionados = ctx.formParams("fuentes_seleccionadas");

        if (idsSeleccionados != null && !idsSeleccionados.isEmpty()) {
          for (String idStr : idsSeleccionados) {
            try {
              Long idHija = Long.parseLong(idStr);
              Fuente fuenteHija = this.findById(idHija);
              if (fuenteHija != null && !(fuenteHija instanceof FuenteDeAgregacion)) {
                fuenteAgregacion.agregarFuente(fuenteHija);
              }
            } catch (NumberFormatException e) {
              System.out.println("Error ID fuente: " + idStr);
            }
          }
        }
        fuente = fuenteAgregacion;
        FuenteRepository.instance().save(fuente);
        break;
    }
    return fuente;
  }

  // --- AUXILIARES ---
  private ConfiguracionLector determinarConfig(String fileName) {
    if (fileName.toLowerCase().endsWith(".csv")) {
      String formatoPolimorfico = "[yyyy-MM-dd'T'HH:mm:ss.SSSSSSSSS]" + "[yyyy-MM-dd'T'HH:mm:ss]" + "[yyyy-MM-dd HH:mm]" + "[yyyy-MM-dd]" + "[dd/MM/yyyy]" + "[dd/MM/yyyy HH:mm]";
      return new ConfiguracionLectorCsv(',', formatoPolimorfico, this.crearMapeoColumnas());
    } else if (fileName.toLowerCase().endsWith(".json")) {
      return new ConfiguracionLectorJson();
    }
    throw new RuntimeException("Formato no soportado: " + fileName);
  }

  private Map<String, List<String>> crearMapeoColumnas() {
    return convertirMapeoAString(Map.of(
        CampoHecho.TITULO, List.of("titulo", "TITULO", "hecho_titulo"),
        CampoHecho.DESCRIPCION, List.of("descripcion", "DESCRIPCION", "hecho_descripcion"),
        CampoHecho.LATITUD, List.of("latitud", "LATITUD"),
        CampoHecho.LONGITUD, List.of("longitud", "LONGITUD"),
        CampoHecho.FECHA_SUCESO, List.of("fechaSuceso", "FECHASUCESO", "hecho_fecha_suceso"),
        CampoHecho.CATEGORIA, List.of("categoria", "CATEGORIA", "hecho_categoria"),
        CampoHecho.DIRECCION, List.of("direccion", "DIRECCION", "hecho_ubicacion", "hecho_direccion"),
        CampoHecho.PROVINCIA, List.of("provincia", "PROVINCIA", "hecho_provincia"),
        CampoHecho.ETIQUETAS, List.of("etiquetas", "ETIQUETAS", "hecho_etiquetas")
    ));
  }

  private Map<String, List<String>> convertirMapeoAString(Map<CampoHecho, List<String>> mapeoEnum) {
    return mapeoEnum.entrySet().stream().collect(Collectors.toMap(
        entry -> entry.getKey().name(), Map.Entry::getValue
    ));
  }

  public void save(Fuente fuente) {
    FuenteRepository.instance().save(fuente);
  }
}