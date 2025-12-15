package ar.edu.utn.frba.dds.controller;

import ar.edu.utn.frba.dds.model.fuentes.Fuente;
import ar.edu.utn.frba.dds.model.fuentes.FuenteDinamica;
import ar.edu.utn.frba.dds.model.hecho.Hecho;
import ar.edu.utn.frba.dds.model.hecho.Origen;
import ar.edu.utn.frba.dds.model.hecho.etiqueta.Etiqueta;
import ar.edu.utn.frba.dds.model.hecho.multimedia.Multimedia;
import ar.edu.utn.frba.dds.model.info.PuntoGeografico;
import ar.edu.utn.frba.dds.repositories.FuenteRepository;
import ar.edu.utn.frba.dds.repositories.HechoRepository;
import ar.edu.utn.frba.dds.repositories.UserRepository;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import io.javalin.http.NotFoundResponse;
import io.javalin.http.UploadedFile;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class HechoController {
  FuenteController fuenteController = new FuenteController();

  public Hecho subirHecho(Hecho hecho) {
    this.validarHecho(hecho);
    HechoRepository.instance().save(hecho);
    return hecho;
  }

  public Optional<Hecho> findAny() {
    return HechoRepository.instance().findAny();
  }

  public Hecho findById(Long id) {
    return HechoRepository.instance().findById(id);
  }

  public List<Hecho> findByTitle(String titulo) {
    return HechoRepository.instance().findByTitle(titulo);
  }

  public List<Hecho> findAll() {
    return HechoRepository.instance().findAll();
  }

  public Long countAll() {
    return HechoRepository.instance().countAll();
  }

  public Hecho modificarHecho(Hecho hechoOriginal, Hecho hechoModificado) {
    if (hechoModificado.getTitulo() != null) {
      hechoOriginal.setTitulo(hechoModificado.getTitulo());
    }
    if (hechoModificado.getDescripcion() != null) {
      hechoOriginal.setDescripcion(hechoModificado.getDescripcion());
    }
    if (hechoModificado.getEtiquetas() != null) {
      hechoOriginal.setEtiquetas(hechoModificado.getEtiquetas());
    }
    if (hechoModificado.getUbicacion() != null) {
      hechoOriginal.setUbicacion(hechoModificado.getUbicacion());
    }
    if (hechoModificado.getFechaSuceso() != null) {
      if (hechoModificado.getFechaSuceso()
                         .isBefore(hechoOriginal.getFechaSuceso())) {
        hechoOriginal.setFechasuceso(hechoModificado.getFechaSuceso());
      }
    }
    List<Multimedia> fotosNuevas = hechoModificado.getFotos();
    if (fotosNuevas != null && !fotosNuevas.isEmpty()) {
      fotosNuevas.forEach(foto -> hechoOriginal.agregarFoto(foto));
    }
    if (!hechoModificado.getCategoria().isBlank()) {
      hechoOriginal.setCategoria(hechoModificado.getCategoria());
    }
    if (!hechoModificado.getDireccion().isBlank()) {
      hechoOriginal.setDireccion(hechoModificado.getDireccion());
    }
    if (hechoModificado.getProvincia() != null && !hechoModificado.getProvincia().isBlank()) {
      hechoOriginal.setProvincia(hechoModificado.getProvincia());
    }
    return hechoOriginal;
  }

  private void validarHecho(Hecho hecho) {
    if (hecho.getFechaSuceso()
             .isAfter(LocalDateTime.now())) {
      throw new RuntimeException("EL HECHO NO PUEDE SUCEDER EN EL FUTURO");
    }
  }

  public List<String> getCategorias() {
    return HechoRepository.instance().getCategorias();
  }

  public List<String> getEtiquetas() {
    return HechoRepository.instance().getEtiquetas();
  }

  public void editarHecho(Context ctx, Map<String, Object> model) {
    Long idHecho = Long.parseLong(ctx.pathParam("id"));
    Long idUsuario = ctx.sessionAttribute("usuario_id");
    Hecho hecho = this.findById(idHecho);

    if (hecho.getFechaSuceso() != null) {
      String fechaFormateada = hecho.getFechaSuceso()
                                    .truncatedTo(ChronoUnit.MINUTES) // Elimina segundos y nanos
                                    .toString();

      model.put("fechaLimite", fechaFormateada);
    }

    if (!hecho.getAutor().getId().equals(idUsuario)) {
      ctx.status(HttpStatus.FORBIDDEN);
    }
    model.put("hecho", hecho);
    if (hecho.getEtiquetas() != null) {
      String etiquetasStr = String.join(", ",
                                        hecho.getEtiquetas()
                                             .stream()
                                             .map(e -> e.getNombre())
                                             .toList()
      );
      model.put("etiquetasTexto", etiquetasStr);
    }
    ctx.render("/hecho-editar.hbs", model);
  }

  public void actualizarHecho(Context ctx) {
    Long idHecho = Long.parseLong(ctx.pathParam("id"));
    Hecho hechoOriginal = findById(idHecho);
    Hecho hechoModificado = new Hecho();

    hechoModificado.setTitulo(ctx.formParam("titulo"));
    hechoModificado.setCategoria(ctx.formParam("categoria"));
    hechoModificado.setDescripcion(ctx.formParam("descripcion"));

    String etiquetasInput = ctx.formParam("etiquetas");
    if (etiquetasInput != null) {
      List<Etiqueta> nuevasEtiquetas = Arrays.stream(etiquetasInput.split(","))
                                             .map(String::trim)
                                             .filter(s -> !s.isEmpty())
                                             .map(nombre -> new Etiqueta(nombre))
                                             .collect(Collectors.toList());

      hechoModificado.setEtiquetas(nuevasEtiquetas);
    }
    String fechaStr = ctx.formParam("fechaSuceso");

    if (fechaStr != null && !fechaStr.isBlank()) {
      hechoModificado.setFechasuceso(LocalDateTime.parse(fechaStr));
    }

    List<String> indicesBorrar = ctx.formParams("fotos_borrar");
    if (!indicesBorrar.isEmpty() && hechoOriginal.getFotos() != null) {
      List<Integer> indices = indicesBorrar.stream()
                                           .map(Integer::parseInt)
                                           .sorted(Comparator.reverseOrder())
                                           .collect(Collectors.toList());

      for (int i : indices) {
        if (i >= 0 && i < hechoOriginal.getFotos().size()) {
          hechoOriginal.quitar(i);
        }
      }
    }

    List<UploadedFile> archivos = ctx.uploadedFiles("nuevas_fotos");
    List<Multimedia> listaNuevasFotos = new ArrayList<>();
    for (UploadedFile archivo : archivos) {
      if (archivo.size() > 0) {
        try {
          byte[] bytesImagen = archivo.content().readAllBytes();
          Multimedia nuevaFoto = new Multimedia(
              archivo.filename(),
              archivo.contentType(),
              bytesImagen
          );
          listaNuevasFotos.add(nuevaFoto);
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    }
    if (!listaNuevasFotos.isEmpty()) {
      hechoModificado.setFotos(listaNuevasFotos);
    }

    String direccion = ctx.formParam("direccion");
    hechoModificado.setDireccion(direccion);
    Double lat = Double.valueOf(Objects.requireNonNull(ctx.formParam("latitud")));
    Double lng = Double.valueOf(Objects.requireNonNull(ctx.formParam("longitud")));

    hechoModificado.setUbicacion(new PuntoGeografico(lat, lng));
    String provincia = ctx.formParam("provincia");
    if (provincia != null & !provincia.isBlank()) {
      hechoModificado.setProvincia(provincia);
    }

    modificarHecho(hechoOriginal, hechoModificado);
    HechoRepository.instance().save(hechoOriginal);
    ctx.redirect("/");
  }

  public void getFoto(Context ctx) {
    Long idHecho = Long.parseLong(ctx.pathParam("id"));
    int indice = Integer.parseInt(ctx.pathParam("indice"));

    Hecho hecho = this.findById(idHecho);

    if (hecho != null && hecho.getFotos() != null && indice < hecho.getFotos().size()) {
      Multimedia foto = hecho.getFotos().get(indice);

      ctx.contentType(foto.getMimetype());
      ctx.result(foto.getDatos());
    } else {
      ctx.status(404);
    }
  }

  public void verHecho(Context ctx, Map<String, Object> model) {
    Long idHecho = Long.parseLong(ctx.pathParam("id"));
    Hecho hecho = findById(idHecho);
    if (hecho == null) {
      throw new NotFoundResponse("Hecho no encontrado");
    }

    model.put("hecho", hecho);

    Long usuarioId = ctx.sessionAttribute("usuario_id");
    boolean esPropietario = hecho.getAutor() != null && hecho.getAutor().getId().equals(usuarioId);
    model.put("esPropietario", esPropietario);

    if (hecho.getFechaSuceso() != null) {
      java.time.format.DateTimeFormatter formatter =
          java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
      model.put("fechaFormateada",
                hecho.getFechaSuceso()
                     .format(formatter)
      );
    }

    ctx.render("hecho-detail.hbs", model);
  }

  // NUEVO MÉTODO PARA CREAR HECHO (con o sin fuente)
  public void crearHecho(Context ctx) {
    try {
      // Obtener datos del formulario
      String titulo = ctx.formParam("titulo");
      String descripcion = ctx.formParam("descripcion");
      String categoria = ctx.formParam("categoria");
      String direccion = ctx.formParam("direccion");
      String provincia = ctx.formParam("provincia");

      // Parsear fecha
      String fechaSucesoStr = ctx.formParam("fechaSuceso");
      LocalDateTime fechaSuceso = fechaSucesoStr != null && !fechaSucesoStr.isBlank()
                                  ? LocalDateTime.parse(fechaSucesoStr)
                                  : LocalDateTime.now();

      // Parsear coordenadas
      Double lat = ctx.formParam("latitud") != null ? Double.parseDouble(ctx.formParam("latitud")) : null;
      Double lng = ctx.formParam("longitud") != null ? Double.parseDouble(ctx.formParam("longitud")) : null;

      if (lat == null || lng == null) {
        throw new RuntimeException("Las coordenadas son obligatorias");
      }

      PuntoGeografico ubicacion = new PuntoGeografico(lat, lng);

      // Obtener usuario autor
      Long usuarioId = ctx.sessionAttribute("usuario_id");
      if (usuarioId == null) {
        throw new RuntimeException("Usuario no autenticado");
      }

      // Crear hecho base
      Hecho nuevoHecho = new Hecho();
      nuevoHecho.setTitulo(titulo);
      nuevoHecho.setDescripcion(descripcion);
      nuevoHecho.setCategoria(categoria);
      nuevoHecho.setDireccion(direccion);
      nuevoHecho.setProvincia(provincia);
      nuevoHecho.setUbicacion(ubicacion);
      nuevoHecho.setFechasuceso(fechaSuceso);
      nuevoHecho.setFechacarga(LocalDateTime.now());
      nuevoHecho.setOrigen(Origen.PROVISTO_CONTRIBUYENTE);

      // Procesar etiquetas
      String etiquetasInput = ctx.formParam("etiquetas");
      if (etiquetasInput != null && !etiquetasInput.isBlank()) {
        List<Etiqueta> etiquetas = Arrays.stream(etiquetasInput.split(","))
                                         .map(String::trim)
                                         .filter(s -> !s.isEmpty())
                                         .map(nombre -> new Etiqueta(nombre))
                                         .collect(Collectors.toList());
        nuevoHecho.setEtiquetas(etiquetas);
      }

      // Procesar fotos
      List<UploadedFile> archivos = ctx.uploadedFiles("fotos");
      List<Multimedia> fotos = new ArrayList<>();
      for (UploadedFile archivo : archivos) {
        if (archivo != null && archivo.size() > 0) {
          byte[] bytesImagen = archivo.content().readAllBytes();
          Multimedia foto = new Multimedia(
              archivo.filename(),
              archivo.contentType(),
              bytesImagen
          );
          fotos.add(foto);
        }
      }
      nuevoHecho.setFotos(fotos);

      // Validar hecho
      nuevoHecho.setAutor(UserRepository.instance()
                                        .findById(usuarioId));
      validarHecho(nuevoHecho);

      this.subirHecho(nuevoHecho);

      String fuenteStr = ctx.formParam("fuente");

      if (fuenteStr != null && !fuenteStr.isEmpty()) {
        Fuente fuente = fuenteController.findByName(fuenteStr);

        if (fuente == null) {
          FuenteDinamica fuenteNueva = new FuenteDinamica(fuenteStr);
          fuenteNueva.agregarHecho(nuevoHecho);
          FuenteRepository.instance().update(fuenteNueva);

        } else {
          fuenteController.agregarHechoDinamico(fuente, nuevoHecho);
        }
      }

      ctx.redirect("/");

    } catch (Exception e) {
      e.printStackTrace();
      ctx.status(400).result("Error al crear hecho: " + e.getMessage());
    }
  }

  public Map<String, Object> buscarAvanzadoCompleto(
      String titulo, String categoria, String fuente, String coleccion,
      String fechaDesdeStr, String fechaHastaStr, Boolean soloConsensuados
  ) {
    // Versión sin paginar (devuelve todo)
    List<Hecho> filtrados = obtenerHechosFiltrados(
        titulo,
        categoria,
        fuente,
        coleccion,
        fechaDesdeStr,
        fechaHastaStr,
        soloConsensuados
    );

    List<Map<String, Object>> resultadosMapeados = filtrados.stream()
                                                            .map(this::mapHechoToFrontend)
                                                            .collect(Collectors.toList());

    Map<String, Object> respuesta = new HashMap<>();
    respuesta.put("resultados", resultadosMapeados);
    respuesta.put("total", filtrados.size());

    return respuesta;
  }

  public Map<String, Object> buscarAvanzadoCompletoPaginated(
      String titulo, String categoria, String fuente, String coleccion,
      String fechaDesdeStr, String fechaHastaStr, Boolean soloConsensuados,
      int page, int pageSize
  ) {

    // 1. Reutilizamos la lógica de filtrado (ver método de abajo)
    List<Hecho> filtrados = obtenerHechosFiltrados(
        titulo,
        categoria,
        fuente,
        coleccion,
        fechaDesdeStr,
        fechaHastaStr,
        soloConsensuados
    );

    // 2. Calcular Paginación
    int total = filtrados.size();
    int totalPaginas = (int) Math.ceil((double) total / pageSize);

    int fromIndex = (page - 1) * pageSize;
    if (fromIndex < 0) {
      fromIndex = 0;
    }
    int toIndex = Math.min(fromIndex + pageSize, total);

    List<Hecho> paginaHechos = (fromIndex >= total) ?
                               new ArrayList<>() :
                               filtrados.subList(fromIndex, toIndex);

    // 3. Mapear resultados para el Frontend
    List<Map<String, Object>> resultadosMapeados = paginaHechos.stream()
                                                               .map(this::mapHechoToFrontend)
                                                               .collect(Collectors.toList());

    Map<String, Object> respuesta = new HashMap<>();
    respuesta.put("resultados", resultadosMapeados);
    respuesta.put("total", total);
    respuesta.put("paginaActual", page);
    respuesta.put("totalPaginas", totalPaginas);

    return respuesta;
  }

  public List<Hecho> buscarRapido(String titulo, Boolean soloConsensuados) {
    return HechoRepository.instance().buscarRapido(titulo, soloConsensuados);
  }

  public List<String> getFuentesDisponibles() {
    return HechoRepository.instance().getFuentesDisponibles();
  }

  public List<String> getColeccionesDisponibles() {
    return HechoRepository.instance().getColeccionesDisponibles();
  }

  public Long countByFiltros(String categoria, String fechaDesdeStr, String fechaHastaStr) {
    LocalDate fechaDesde = null;
    LocalDate fechaHasta = null;

    try {
      if (fechaDesdeStr != null && !fechaDesdeStr.isBlank()) {
        fechaDesde = LocalDate.parse(fechaDesdeStr);
      }
      if (fechaHastaStr != null && !fechaHastaStr.isBlank()) {
        fechaHasta = LocalDate.parse(fechaHastaStr);
      }
    } catch (Exception e) {
      throw new RuntimeException("Formato de fecha inválido");
    }

    return HechoRepository.instance().countByFiltros(categoria, fechaDesde, fechaHasta);
  }

  public static Long getLastTotalResults() {
    return HechoRepository.getLastTotalResults();
  }

  public void getMultimedia(Context ctx) {
    int index = ctx.pathParamAsClass("indice", Integer.class)
                   .get();
    Long id = ctx.pathParamAsClass("id", Long.class)
                 .get();

    // Buscas el item en la lista unificada
    Multimedia item = HechoRepository.instance()
                                     .findById(id)
                                     .getFotos()
                                     .get(index);

    // IMPORTANTE: Definir el Content-Type correcto dinámicamente
    if (item.esVideo()) {
      ctx.contentType("video/mp4");
    } else {
      ctx.contentType("image/jpeg"); // O image/png según corresponda
    }

    ctx.result(item.getDatos()); // Devuelves los bytes
  }

  private List<Hecho> obtenerHechosFiltrados(
      String titulo, String categoria, String fuente, String coleccion,
      String fechaDesdeStr, String fechaHastaStr, Boolean soloConsensuados
  ) {
    List<Hecho> todos = this.findAll();

    // Parseo seguro de fechas
    LocalDateTime fechaDesde = (fechaDesdeStr != null && !fechaDesdeStr.isBlank()) ?
                               LocalDateTime.parse(fechaDesdeStr + "T00:00:00") :
                               null;
    LocalDateTime fechaHasta = (fechaHastaStr != null && !fechaHastaStr.isBlank()) ?
                               LocalDateTime.parse(fechaHastaStr + "T23:59:59") :
                               null;

    return todos.stream()
                .filter(h -> titulo == null || titulo.isBlank() || (h.getTitulo() != null && h.getTitulo()
                                                                                              .toLowerCase()
                                                                                              .contains(
                                                                                                  titulo.toLowerCase())))
                .filter(h -> categoria == null || "0".equals(categoria) || categoria.equals(h.getCategoria()))
                // Filtro de Colección (verificar si el hecho pertenece a la colección por título o ID)
                .filter(h -> coleccion == null || "0".equals(coleccion) || (h.getColecciones() != null && h.getColecciones()
                                                                                                           .stream()
                                                                                                           .anyMatch(
                                                                                                               c -> c.getTitulo()
                                                                                                                     .equals(
                                                                                                                         coleccion))))
                // Filtro de Fechas
                .filter(h -> fechaDesde == null || (h.getFechaSuceso() != null && !h.getFechaSuceso()
                                                                                    .isBefore(
                                                                                        fechaDesde)))
                .filter(h -> fechaHasta == null || (h.getFechaSuceso() != null && !h.getFechaSuceso()
                                                                                    .isAfter(
                                                                                        fechaHasta)))
                // Filtro Consensuados
                .filter(h -> !soloConsensuados || (h.getColecciones() != null && !h.getColecciones()
                                                                                   .isEmpty()))
                .collect(Collectors.toList());
  }

  private Map<String, Object> mapHechoToFrontend(Hecho h) {
    Map<String, Object> map = new HashMap<>();
    map.put("id", h.getId());
    map.put("hecho_titulo", h.getTitulo());
    map.put("hecho_descripcion", h.getDescripcion());
    map.put("hecho_categoria", h.getCategoria());
    map.put("hecho_fecha_suceso", h.getFechaSuceso());
    map.put("hecho_provincia", h.getProvincia());
    map.put("hecho_direccion", h.getDireccion());

    map.put("etiquetas", h.getEtiquetas() != null ? h.getEtiquetas() : new ArrayList<>());

    if (h.getColecciones() != null) {
      try {
        List<Map<String, Object>> colsSimple = h.getColecciones()
                                                .stream()
                                                .map(c -> {
                                                  Map<String, Object> m = new HashMap<>();
                                                  m.put("id", c.getId());
                                                  m.put("titulo", c.getTitulo());
                                                  return m;
                                                })
                                                .collect(Collectors.toList());
        map.put("colecciones", colsSimple);
      } catch (Exception e) {
        map.put("colecciones", new ArrayList<>());
      }
    } else {
      map.put("colecciones", new ArrayList<>());
    }

    return map;
  }
}