package ar.edu.utn.frba.dds.controller;

import ar.edu.utn.frba.dds.model.fuentes.Fuente;
import ar.edu.utn.frba.dds.model.fuentes.FuenteDinamica;
import ar.edu.utn.frba.dds.model.hecho.Hecho;
import ar.edu.utn.frba.dds.model.hecho.Origen;
import ar.edu.utn.frba.dds.model.hecho.etiqueta.Etiqueta;
import ar.edu.utn.frba.dds.model.hecho.multimedia.Multimedia;
import ar.edu.utn.frba.dds.model.info.PuntoGeografico;
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
    if (hechoModificado.getFechasuceso() != null) {
      if (hechoModificado.getFechasuceso().isBefore(hechoOriginal.getFechasuceso())) {
        hechoOriginal.setFechasuceso(hechoModificado.getFechasuceso());
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
    if (hecho.getFechasuceso().isAfter(LocalDateTime.now())) {
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

    if (hecho.getFechasuceso() != null) {
      String fechaFormateada = hecho.getFechasuceso()
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

    if (hecho.getFechasuceso() != null) {
      java.time.format.DateTimeFormatter formatter =
          java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
      model.put("fechaFormateada", hecho.getFechasuceso().format(formatter));
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

      // Obtener fuente_id si existe
      String fuenteIdStr = ctx.formParam("fuente_id");
      if (fuenteIdStr != null && !fuenteIdStr.isEmpty()) {
        Long fuenteId = Long.parseLong(fuenteIdStr);
        Fuente fuente = fuenteController.findById(fuenteId);

        if (fuente instanceof FuenteDinamica) {
          // Asignar el hecho a la fuente dinámica
          ((FuenteDinamica) fuente).agregarHecho(nuevoHecho);
          fuenteController.save(fuente);
          ctx.redirect("/admin/fuentes/" + fuenteId);
          return;
        }
      }

      // Si no hay fuente dinámica, guardar como hecho normal
      this.subirHecho(nuevoHecho);
      ctx.redirect("/");

    } catch (Exception e) {
      e.printStackTrace();
      ctx.status(400).result("Error al crear hecho: " + e.getMessage());
    }
  }

  public Map<String, Object> buscarAvanzadoCompleto(
      String titulo,
      String categoria,
      String fuente,
      String coleccion,
      String fechaDesdeStr,
      String fechaHastaStr,
      Boolean soloConsensuados) {

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
      throw new RuntimeException("Formato de fecha inválido. Use YYYY-MM-DD");
    }

    List<Hecho> resultados = HechoRepository.instance().buscarAvanzadoCompleto(
        titulo, categoria, fuente, coleccion,
        fechaDesde, fechaHasta, soloConsensuados
    );

    Map<String, Object> response = new HashMap<>();
    response.put("resultados", resultados);
    response.put("total", resultados.size());

    return response;
  }

  public Map<String, Object> buscarAvanzadoCompletoPaginated(
      String titulo,
      String categoria,
      String fuente,
      String coleccion,
      String fechaDesdeStr,
      String fechaHastaStr,
      Boolean soloConsensuados,
      int page,
      int pageSize) {

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
      throw new RuntimeException("Formato de fecha inválido. Use YYYY-MM-DD");
    }

    return HechoRepository.instance().buscarAvanzadoCompletoPaginated(
        titulo, categoria, fuente, coleccion,
        fechaDesde, fechaHasta, soloConsensuados,
        page, pageSize
    );
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
}