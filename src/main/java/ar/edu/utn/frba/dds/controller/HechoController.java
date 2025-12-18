package ar.edu.utn.frba.dds.controller;

import ar.edu.utn.frba.dds.model.fuentes.Fuente;
import ar.edu.utn.frba.dds.model.fuentes.FuenteDinamica;
import ar.edu.utn.frba.dds.model.hecho.Estado;
import ar.edu.utn.frba.dds.model.hecho.Hecho;
import ar.edu.utn.frba.dds.model.hecho.Origen;
import ar.edu.utn.frba.dds.model.hecho.etiqueta.Etiqueta;
import ar.edu.utn.frba.dds.model.hecho.multimedia.Multimedia;
import ar.edu.utn.frba.dds.model.info.PuntoGeografico;
import ar.edu.utn.frba.dds.repositories.ColeccionRepository;
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
import java.util.Optional;
import java.util.stream.Collectors;

public class HechoController {
  FuenteController fuenteController = new FuenteController();

  public Hecho subirHecho(Hecho hecho) {
    this.validarHecho(hecho);
    HechoRepository.instance()
                   .save(hecho);
    return hecho;
  }

  public Optional<Hecho> findAny() {
    return HechoRepository.instance()
                          .findAny();
  }

  public Hecho findById(Long id) {
    return HechoRepository.instance()
                          .findById(id);
  }

  public List<Hecho> findByTitle(String titulo) {
    return HechoRepository.instance()
                          .findByTitle(titulo);
  }

  public List<Hecho> findAll() {
    return HechoRepository.instance()
                          .findAll();
  }

  public Long countAll() {
    return HechoRepository.instance()
                          .countAll();
  }

  // En HechoController.java

  public Hecho modificarHecho(Hecho hechoOriginal, Hecho hechoModificado) {
    if (hechoModificado.getTitulo() != null && !hechoModificado.getTitulo()
                                                               .isBlank()) {
      hechoOriginal.setTitulo(hechoModificado.getTitulo());
    }

    if (hechoModificado.getDescripcion() != null && !hechoModificado.getDescripcion()
                                                                    .isBlank()) {
      hechoOriginal.setDescripcion(hechoModificado.getDescripcion());
    }

    if (hechoModificado.getEtiquetas() != null && !hechoModificado.getEtiquetas()
                                                                  .isEmpty()) {
      hechoOriginal.setEtiquetas(hechoModificado.getEtiquetas());
    }

    if (hechoModificado.getUbicacion() != null) {
      hechoOriginal.setUbicacion(hechoModificado.getUbicacion());
    }

    if (hechoModificado.getFechaSuceso() != null) {
      hechoOriginal.setFechasuceso(hechoModificado.getFechaSuceso());
    }

    List<Multimedia> fotosNuevas = hechoModificado.getFotos();
    if (fotosNuevas != null && !fotosNuevas.isEmpty()) {
      fotosNuevas.forEach(foto -> hechoOriginal.agregarFoto(foto));
    }

    if (hechoModificado.getCategoria() != null && !hechoModificado.getCategoria()
                                                                  .isBlank()) {
      hechoOriginal.setCategoria(hechoModificado.getCategoria());
    }

    if (hechoModificado.getDireccion() != null && !hechoModificado.getDireccion()
                                                                  .isBlank()) {
      hechoOriginal.setDireccion(hechoModificado.getDireccion());
    }

    if (hechoModificado.getProvincia() != null && !hechoModificado.getProvincia()
                                                                  .isBlank()) {
      hechoOriginal.setProvincia(hechoModificado.getProvincia());
    }

    if (hechoModificado.getEstado() != null && !hechoModificado.getEstado()
                                                               .toString()
                                                               .isBlank()) {
      hechoOriginal.setEstado(hechoModificado.getEstado());
    }

    return hechoOriginal;
  }

  private void validarHecho(Hecho hecho) {
    if (hecho.getFechaSuceso() != null && hecho.getFechaSuceso()
                                               .isAfter(LocalDateTime.now())) {
      throw new RuntimeException("EL HECHO NO PUEDE SUCEDER EN EL FUTURO");
    }
  }

  public List<String> getCategorias() {
    return HechoRepository.instance()
                          .getCategorias();
  }

  public List<String> getEtiquetas() {
    return HechoRepository.instance()
                          .getEtiquetas();
  }

  public void editarHecho(Context ctx, Map<String, Object> model) {
    try {
      Long idHecho = Long.parseLong(ctx.pathParam("id"));
      Long idUsuario = ctx.sessionAttribute("usuario_id");
      Boolean esAdmin = Boolean.TRUE.equals(ctx.sessionAttribute("esAdmin")); // Check seguro de rol

      Hecho hecho = this.findById(idHecho);

      if (hecho == null) {
        ctx.status(HttpStatus.NOT_FOUND)
           .result("Hecho no encontrado");
        return;
      }

      if (hecho.getFechaSuceso() != null) {
        String fechaFormateada = hecho.getFechaSuceso()
                                      .truncatedTo(ChronoUnit.MINUTES)
                                      .toString();
        model.put("fechaLimite", fechaFormateada);
      }

      boolean esAutor = false;
      if (hecho.getAutor() != null && idUsuario != null) {
        esAutor = hecho.getAutor()
                       .getId()
                       .equals(idUsuario);
      }

      if (!esAutor) {
        ctx.status(HttpStatus.FORBIDDEN)
           .result("No tienes permiso para editar este hecho.");
        ctx.redirect("/");
        return;
      }

      model.put("hecho", hecho);
      if (hecho.getEtiquetas() != null) {
        String etiquetasStr = String.join(
            ", ",
            hecho.getEtiquetas()
                 .stream()
                 .map(e -> e.getNombre())
                 .toList()
        );
        model.put("etiquetasTexto", etiquetasStr);
      }

      if (ctx.pathParamMap()
             .containsKey("fuenteId")) {
        try {
          Long fuenteId = Long.parseLong(ctx.pathParam("fuenteId"));
          Fuente fuente = fuenteController.findById(fuenteId);
          model.put("fuente", fuente);

        } catch (NumberFormatException e) {
          System.out.println("Error parseando fuenteId: " + e.getMessage());
        }
      }

      ctx.render(
          "hecho-editar.hbs",
          model
      );

    } catch (Exception e) {
      e.printStackTrace();
      ctx.status(HttpStatus.INTERNAL_SERVER_ERROR)
         .result("Error al cargar el editor.");
    }
  }

  public void actualizarHecho(Context ctx) {
    try {
      Long idHecho = Long.parseLong(ctx.pathParam("id"));
      Hecho hechoOriginal = findById(idHecho);

      if (hechoOriginal == null) {
        ctx.status(HttpStatus.NOT_FOUND)
           .result("Hecho no encontrado");
        return;
      }

      if (hechoOriginal.getFechacarga()
                       .isBefore(LocalDateTime.now()
                                              .minusWeeks(1))) {
        ctx.status(HttpStatus.FORBIDDEN);
        ctx.redirect("/");
        return;
      }

      Long idUsuario = ctx.sessionAttribute("usuario_id");
      Boolean esAdmin = Boolean.TRUE.equals(ctx.sessionAttribute("esAdmin"));

      boolean esAutor = false;
      if (hechoOriginal.getAutor() != null && idUsuario != null) {
        esAutor = hechoOriginal.getAutor()
                               .getId()
                               .equals(idUsuario);
      }

      if (!esAutor && !esAdmin) {
        ctx.status(HttpStatus.FORBIDDEN)
           .result("No tienes permiso para modificar este hecho.");
        return;
      }

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
          if (i >= 0 && i < hechoOriginal.getFotos()
                                         .size()) {
            hechoOriginal.quitar(i);
          }
        }
      }

      List<UploadedFile> archivos = ctx.uploadedFiles("nuevas_fotos");
      List<Multimedia> listaNuevasFotos = new ArrayList<>();
      for (UploadedFile archivo : archivos) {
        if (archivo.size() > 0) {
          try {
            byte[] bytesImagen = archivo.content()
                                        .readAllBytes();
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

      String latStr = ctx.formParam("latitud");
      String lngStr = ctx.formParam("longitud");

      if (latStr != null && !latStr.isBlank() && lngStr != null && !lngStr.isBlank()) {
        try {
          Double lat = Double.valueOf(latStr);
          Double lng = Double.valueOf(lngStr);
          hechoModificado.setUbicacion(new PuntoGeografico(lat, lng));
        } catch (NumberFormatException e) {
          throw new RuntimeException(e.getMessage());
        }
      }
      String provincia = ctx.formParam("provincia");
      hechoModificado.setProvincia(provincia);
      hechoModificado.setEstado(Estado.EDITADO);

      modificarHecho(hechoOriginal, hechoModificado);

      HechoRepository.instance()
                     .save(hechoOriginal);


      String redirect = ctx.formParam("redirectUrl");
      if (redirect != null && !redirect.isBlank()) {
        ctx.redirect(redirect);
      } else {
        ctx.redirect("/hechos/" + idHecho);
      }
    } catch (Exception e) {
      e.printStackTrace();
      ctx.status(HttpStatus.INTERNAL_SERVER_ERROR)
         .result("Error al actualizar: " + e.getMessage());
    }
  }

  public void getFoto(Context ctx) {
    Long idHecho = Long.parseLong(ctx.pathParam("id"));
    int indice = Integer.parseInt(ctx.pathParam("indice"));

    Hecho hecho = this.findById(idHecho);

    if (hecho != null && hecho.getFotos() != null && indice < hecho.getFotos()
                                                                   .size()) {
      Multimedia foto = hecho.getFotos()
                             .get(indice);

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

    Long usuarioId = (Long) ctx.sessionAttribute("usuario_id");
    boolean esAdmin = Boolean.TRUE.equals(ctx.sessionAttribute("esAdmin"));

    // Lógica segura para determinar propiedad
    boolean esEditable = false;
    if (hecho.getAutor() != null && usuarioId != null) {
      esEditable = hecho.getAutor()
                           .getId()
                        .equals(usuarioId) && hecho.getFechacarga()
                                                   .isAfter(LocalDateTime.now()
                                                                         .minusWeeks(1));
    }

    model.put("esEditable", esEditable);

    if (hecho.getFechaSuceso() != null) {
      java.time.format.DateTimeFormatter formatter =
          java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
      model.put(
          "fechaFormateada",
          hecho.getFechaSuceso()
               .format(formatter)
      );
    }

    ctx.render("hecho-detail.hbs", model);
  }

  public void crearHecho(Context ctx) {
    try {
      String titulo = ctx.formParam("titulo");
      String descripcion = ctx.formParam("descripcion");
      String categoria = ctx.formParam("categoria");
      String direccion = ctx.formParam("direccion");
      String provincia = ctx.formParam("provincia");

      String fechaSucesoStr = ctx.formParam("fechaSuceso");
      LocalDateTime fechaSuceso = fechaSucesoStr != null && !fechaSucesoStr.isBlank()
                                  ? LocalDateTime.parse(fechaSucesoStr)
                                  : LocalDateTime.now();

      Double lat = ctx.formParam("latitud") != null ?
                   Double.parseDouble(ctx.formParam("latitud")) :
                   null;
      Double lng = ctx.formParam("longitud") != null ?
                   Double.parseDouble(ctx.formParam("longitud")) :
                   null;

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
          byte[] bytesImagen = archivo.content()
                                      .readAllBytes();
          Multimedia foto = new Multimedia(
              archivo.filename(),
              archivo.contentType(),
              bytesImagen
          );
          fotos.add(foto);
        }
      }
      nuevoHecho.setFotos(fotos);

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
          FuenteRepository.instance()
                          .update(fuenteNueva);

        } else {
          fuenteController.agregarHechoDinamico(fuente, nuevoHecho);
        }

      }

      String redirect = ctx.formParam("redirectUrl");
      if (redirect != null && !redirect.isEmpty()) {
        ctx.redirect(redirect);
      } else {
        ctx.redirect("/");
      }

    } catch (Exception e) {
      e.printStackTrace();
      ctx.status(400)
         .result("Error al crear hecho: " + e.getMessage());
    }
  }

  public Map<String, Object> buscarAvanzadoCompleto(
      String titulo,
      String categoria,
      String fuente,
      String coleccion,
      Long coleccionId,
      String fechaDesdeStr,
      String fechaHastaStr,
      Boolean soloConsensuados,
      Boolean incluirEliminados
  ) {

    try {
      LocalDate fechaDesde = null;
      LocalDate fechaHasta = null;

      if (fechaDesdeStr != null && !fechaDesdeStr.isBlank()) {
        fechaDesde = LocalDate.parse(fechaDesdeStr);
      }
      if (fechaHastaStr != null && !fechaHastaStr.isBlank()) {
        fechaHasta = LocalDate.parse(fechaHastaStr);
      }

      List<Hecho> resultados = HechoRepository.instance()
                                              .buscarAvanzadoCompleto(
                                                  titulo, categoria, fuente, coleccion, coleccionId,
                                                  fechaDesde, fechaHasta, soloConsensuados,
                                                  incluirEliminados
                                              );

      List<Map<String, Object>> resultadosMapeados = resultados.stream()
                                                               .map(this::mapHechoToFrontend)
                                                               .collect(Collectors.toList());

      Map<String, Object> respuesta = new HashMap<>();
      respuesta.put("resultados", resultadosMapeados);
      respuesta.put("total", resultados.size());

      return respuesta;

    } catch (Exception e) {
      e.printStackTrace();
      throw new RuntimeException("Error en búsqueda avanzada: " + e.getMessage());
    }
  }

  public Map<String, Object> buscarAvanzadoCompletoPaginated(
      String titulo, String categoria, String fuente, String coleccion,
      String fechaDesdeStr, String fechaHastaStr, Boolean soloConsensuados,
      Boolean incluirEliminados,
      int page, int pageSize
  ) {
    try {
      LocalDate fechaDesde = null;
      LocalDate fechaHasta = null;

      if (fechaDesdeStr != null && !fechaDesdeStr.isBlank()) {
        fechaDesde = LocalDate.parse(fechaDesdeStr);
      }
      if (fechaHastaStr != null && !fechaHastaStr.isBlank()) {
        fechaHasta = LocalDate.parse(fechaHastaStr);
      }

      Map<String, Object> resultadoRepo = HechoRepository.instance()
                                                         .buscarAvanzadoCompletoPaginated(
                                                             titulo,
                                                             categoria,
                                                             fuente,
                                                             coleccion,
                                                             fechaDesde,
                                                             fechaHasta,
                                                             soloConsensuados,
                                                             incluirEliminados,
                                                             page,
                                                             pageSize
                                                         );

      List<Hecho> hechos = (List<Hecho>) resultadoRepo.get("resultados");

      List<Map<String, Object>> resultadosMapeados = hechos.stream()
                                                           .map(this::mapHechoToFrontend)
                                                           .collect(Collectors.toList());

      Map<String, Object> respuesta = new HashMap<>(resultadoRepo);
      respuesta.put("resultados", resultadosMapeados);

      return respuesta;

    } catch (Exception e) {
      e.printStackTrace();
      throw new RuntimeException("Error en búsqueda paginada: " + e.getMessage());
    }
  }

  public List<Hecho> buscarRapido(String titulo, Boolean soloConsensuados) {
    List<Hecho> resultados;

    if (soloConsensuados != null && soloConsensuados) {
      resultados = ColeccionRepository.instance()
                                      .findHechosConsensuados();
    } else {
      resultados = HechoRepository.instance()
                                  .findAll();
    }
    if (titulo != null && !titulo.isBlank()) {
      resultados = resultados.stream()
                             .filter(h -> h.getTitulo() != null &&
                                 h.getTitulo()
                                  .toLowerCase()
                                  .contains(titulo.toLowerCase()))
                             .collect(Collectors.toList());
    }

    return resultados;
  }

  public List<String> getFuentesDisponibles() {
    return HechoRepository.instance()
                          .getFuentesDisponibles();
  }

  public List<String> getColeccionesDisponibles() {
    return HechoRepository.instance()
                          .getColeccionesDisponibles();
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

      return HechoRepository.instance()
                            .countByFiltros(categoria, fechaDesde, fechaHasta);

    } catch (Exception e) {
      throw new RuntimeException("Formato de fecha inválido");
    }
  }

  public static Long getLastTotalResults() {
    return HechoRepository.getLastTotalResults();
  }

  public void getMultimedia(Context ctx) {
    int index = ctx.pathParamAsClass("indice", Integer.class)
                   .get();
    Long id = ctx.pathParamAsClass("id", Long.class)
                 .get();

    Multimedia item = HechoRepository.instance()
                                     .findById(id)
                                     .getFotos()
                                     .get(index);

    if (item.esVideo()) {
      ctx.contentType("video/mp4");
    } else {
      ctx.contentType("image/jpeg");
    }

    ctx.result(item.getDatos());
  }

  private List<Hecho> obtenerHechosFiltrados(
      String titulo, String categoria, String fuente, String coleccion,
      String fechaDesdeStr, String fechaHastaStr, Boolean soloConsensuados
  ) {
    List<Hecho> todos = this.findAll();

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

    if (h.getUbicacion() != null) {
      Map<String, Object> ubicacion = new HashMap<>();
      ubicacion.put(
          "latitud",
          h.getUbicacion()
           .getLatitud()
      );
      ubicacion.put(
          "longitud",
          h.getUbicacion()
           .getLongitud()
      );
      map.put("hecho_ubicacion", ubicacion);
    }

    if (h.getEtiquetas() != null) {
      List<Map<String, String>> etiquetasList = h.getEtiquetas()
                                                 .stream()
                                                 .map(e -> {
                                                   Map<String, String> etiq = new HashMap<>();
                                                   etiq.put("nombre", e.getNombre());
                                                   return etiq;
                                                 })
                                                 .collect(Collectors.toList());
      map.put("etiquetas", etiquetasList);
    }

    if (h.getColecciones() != null && !h.getColecciones()
                                        .isEmpty()) {
      List<Map<String, Object>> coleccionesList = h.getColecciones()
                                                   .stream()
                                                   .map(c -> {
                                                     Map<String, Object> col = new HashMap<>();
                                                     col.put("id", c.getId());
                                                     col.put("titulo", c.getTitulo());
                                                     return col;
                                                   })
                                                   .collect(Collectors.toList());
      map.put("colecciones", coleccionesList);
    } else {
      map.put("colecciones", new ArrayList<>());
    }

    if (h.getAutor() != null) {
      Map<String, Object> autor = new HashMap<>();
      autor.put(
          "id",
          h.getAutor()
           .getId()
      );
      autor.put(
          "userName",
          h.getAutor()
           .getUserName()
      );
      map.put("autor", autor);
    }

    if (h.getFotos() != null && !h.getFotos()
                                  .isEmpty()) {
      List<Map<String, Object>> fotosList = new ArrayList<>();
      for (int i = 0; i < h.getFotos()
                           .size(); i++) {
        Multimedia foto = h.getFotos()
                           .get(i);
        Map<String, Object> fotoMap = new HashMap<>();
        fotoMap.put("indice", i);
        fotoMap.put("esVideo", foto.esVideo());
        fotoMap.put("mimetype", foto.getMimetype());
        fotosList.add(fotoMap);
      }
      map.put("fotos", fotosList);
    }


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

  public List<Hecho> findAllPublicos() {
    return HechoRepository.instance()
                          .findAllPublicos();
  }

  public void findAndDelete(Long idHecho) {
    HechoRepository.instance()
                   .findAndDelete(idHecho);
  }
}