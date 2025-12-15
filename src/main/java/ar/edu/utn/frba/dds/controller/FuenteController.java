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
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.javalin.http.Context;
import io.javalin.http.UploadedFile;
import java.util.HashMap;
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

  public List<Map<String, Object>> obtenerFuentesConTipo(boolean soloSimples) {
    List<Fuente> fuentes = this.findAll(soloSimples);

    return fuentes.stream().map(f -> {
      Map<String, Object> dto = new HashMap<>();
      dto.put("id", f.getId());
      dto.put("nombre", f.getNombre());
      dto.put("tipo", determinarTipo(f));
      return dto;
    }).collect(Collectors.toList());
  }

  public Fuente findById(Long id) {
    return FuenteRepository.instance().findById(id);
  }

  public String determinarTipo(Fuente f) {
    if (f instanceof FuenteDinamica) return "DINAMICA";
    if (f instanceof FuenteEstatica) return "ESTATICA";
    if (f instanceof FuenteDeAgregacion) return "AGREGACION";
    if (f instanceof FuenteExternaAPI) return "API";
    return "OTRO";
  }

  // --- CREACIÓN ---

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
        String jsonMapeo = ctx.formParam("mapeo_columnas_json");
        String separadorStr = ctx.formParam("csv_separador");
        String formatoFecha = ctx.formParam("csv_formato_fecha");

        if (archivo != null && !archivo.filename().isEmpty()) {
          try {
            ConfiguracionLector configLector = determinarConfig(archivo.filename(), jsonMapeo, separadorStr, formatoFecha);
            Lector<Hecho> lector = configLector.build(Hecho.class);
            List<Hecho> hechosImportados = lector.importar(archivo.content());

            if (hechosImportados == null || hechosImportados.isEmpty()) {
              throw new RuntimeException("El archivo no contiene hechos válidos o no se pudieron leer. La fuente no será creada.");
            }

            // Validación de integridad
            for (int i = 0; i < hechosImportados.size(); i++) {
              Hecho h = hechosImportados.get(i);
              if (h.getTitulo() == null || h.getTitulo().isBlank()) {
                throw new RuntimeException("Error en fila " + (i+1) + ": Falta el título (hecho_titulo).");
              }
              if (h.getFechasuceso() == null) {
                throw new RuntimeException("Error en fila " + (i+1) + ": Falta la fecha de suceso (hecho_fecha_suceso).");
              }

              boolean tieneCoordenadas = (h.getUbicacion() != null);
              boolean tieneDireccion = (h.getDireccion() != null && !h.getDireccion().isBlank());
              boolean tieneProvincia = (h.getProvincia() != null && !h.getProvincia().isBlank());

              if (!tieneCoordenadas && !tieneDireccion && !tieneProvincia) {
                throw new RuntimeException("Error en fila " + (i+1) + ": Se requiere al menos Latitud/Longitud, Dirección o Provincia para ubicar el hecho.");
              }
            }

            hechosImportados.forEach(hecho -> hecho.setOrigen(Origen.DATASET));

            fuente = new FuenteEstatica(nombreFuente, hechosImportados);
            FuenteRepository.instance().save(fuente);

          } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error crítico al procesar la fuente estática: " + e.getMessage());
          }
        }
        break;

      case "API":
        String urlApi = ctx.formParam("url");
        if (urlApi != null && !urlApi.isBlank()) {
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

  // --- EDICIÓN Y GESTIÓN ---

  public void borrar(Fuente fuente) {
    FuenteRepository.instance().delete(fuente);
  }

  public void agregarHechoDinamico(Fuente fuente, Hecho hecho) {
    if (fuente instanceof FuenteDinamica) {
      ((FuenteDinamica) fuente).agregarHecho(hecho);
      FuenteRepository.instance().save(fuente);
    }
  }

  public void borrarHechoDinamico(Fuente fuente, Long idHecho) {
    if (fuente instanceof FuenteDinamica) {
      FuenteDinamica fd = (FuenteDinamica) fuente;
      fd.getHechos().removeIf(h -> h.getId().equals(idHecho));
      FuenteRepository.instance().save(fuente);
    }
  }

  /**
   * Agrega una fuente hija a una agregación existente.
   * Valida que no se agreguen agregaciones recursivas.
   */
  public void agregarFuenteAAgregacion(Fuente fuentePadre, Long idHija) {
    if (!(fuentePadre instanceof FuenteDeAgregacion)) {
      throw new RuntimeException("La fuente padre no es de tipo agregación.");
    }

    Fuente fuenteHija = this.findById(idHija);

    if (fuenteHija == null) {
      throw new RuntimeException("La fuente a agregar no existe.");
    }

    if (fuenteHija instanceof FuenteDeAgregacion) {
      throw new RuntimeException("No se puede agregar una fuente de agregación dentro de otra (evitar ciclos).");
    }

    if (fuenteHija.getId().equals(fuentePadre.getId())) {
      throw new RuntimeException("No se puede agregar una fuente a sí misma.");
    }

    // Verificar si ya está agregada
    FuenteDeAgregacion agregacion = (FuenteDeAgregacion) fuentePadre;
    boolean yaExiste = agregacion.getFuentesCargadas().stream()
                                 .anyMatch(f -> f.getId().equals(idHija));

    if (yaExiste) {
      throw new RuntimeException("La fuente ya está agregada.");
    }

    agregacion.agregarFuente(fuenteHija);
    FuenteRepository.instance().update(fuentePadre);
  }

  /**
   * Remueve una fuente hija de una agregación.
   */
  public void removerFuenteDeAgregacion(Fuente fuentePadre, Long idHija) {
    if (!(fuentePadre instanceof FuenteDeAgregacion)) {
      throw new RuntimeException("La fuente padre no es de tipo agregación.");
    }

    Fuente fuenteHija = this.findById(idHija);
    if (fuenteHija == null) {
      throw new RuntimeException("La fuente hija no existe.");
    }

    FuenteDeAgregacion agregacion = (FuenteDeAgregacion) fuentePadre;
    agregacion.removerFuente(fuenteHija);
    FuenteRepository.instance().update(fuentePadre);
  }

  /**
   * Actualiza una fuente existente (principalmente para cambiar nombre).
   */
  public void save(Fuente fuente) {
    if (fuente.getId() == null) {
      FuenteRepository.instance().save(fuente);
    } else {
      FuenteRepository.instance().update(fuente);
    }
  }

  // --- AUXILIARES ---

  private ConfiguracionLector determinarConfig(String fileName, String jsonMapeo, String separadorStr, String formatoFechaInput) {

    Map<String, List<String>> mapeoColumnas = null;
    if (jsonMapeo != null && !jsonMapeo.isBlank()) {
      try {
        mapeoColumnas = mapper.readValue(jsonMapeo, new TypeReference<Map<String, List<String>>>(){});
      } catch (Exception e) {
        mapeoColumnas = this.crearMapeoColumnasDefault();
      }
    } else {
      mapeoColumnas = this.crearMapeoColumnasDefault();
    }

    if (fileName.toLowerCase().endsWith(".csv")) {
      char separador = (separadorStr != null && !separadorStr.isEmpty()) ? separadorStr.charAt(0) : ',';
      String formatoFecha = (formatoFechaInput != null && !formatoFechaInput.isBlank()) ? formatoFechaInput : "dd/MM/yyyy HH:mm";
      return new ConfiguracionLectorCsv(separador, formatoFecha, mapeoColumnas);
    }
    else if (fileName.toLowerCase().endsWith(".json")) {
      return new ConfiguracionLectorJson();
    }

    throw new RuntimeException("Formato no soportado: " + fileName + ". Solo se permiten archivos .csv o .json");
  }

  private Map<String, List<String>> crearMapeoColumnasDefault() {
    return convertirMapeoAString(Map.of(
        CampoHecho.TITULO, List.of("titulo", "TITULO", "hecho_titulo"),
        CampoHecho.DESCRIPCION, List.of("descripcion", "DESCRIPCION", "hecho_descripcion"),
        CampoHecho.LATITUD, List.of("latitud", "LATITUD"),
        CampoHecho.LONGITUD, List.of("longitud", "LONGITUD"),
        CampoHecho.FECHA_SUCESO, List.of("fechaSuceso", "FECHASUCESO", "hecho_fecha_suceso"),
        CampoHecho.CATEGORIA, List.of("categoria", "CATEGORIA", "hecho_categoria"),
        CampoHecho.DIRECCION, List.of("direccion", "DIRECCION", "hecho_ubicacion", "hecho_direccion"),
        CampoHecho.PROVINCIA, List.of("provincia", "PROVINCIA", "hecho_provincia"),
        CampoHecho.ETIQUETAS, List.of("etiquetas", "ETIQUETAS", "hecho_etiquetas"),
        CampoHecho.FOTOS, List.of("fotos", "foto", "url_imagen", "IMAGEN")
    ));
  }

  private Map<String, List<String>> convertirMapeoAString(Map<CampoHecho, List<String>> mapeoEnum) {
    return mapeoEnum.entrySet().stream().collect(Collectors.toMap(
        entry -> entry.getKey().name(), Map.Entry::getValue
    ));
  }
}