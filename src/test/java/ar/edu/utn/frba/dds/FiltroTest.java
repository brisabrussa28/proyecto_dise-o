package ar.edu.utn.frba.dds;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import ar.edu.utn.frba.dds.domain.coleccion.Coleccion;
import ar.edu.utn.frba.dds.domain.detectorspam.DetectorSpam;
import ar.edu.utn.frba.dds.domain.filtro.Filtro;
import ar.edu.utn.frba.dds.domain.filtro.FiltroDeCategoria;
import ar.edu.utn.frba.dds.domain.filtro.FiltroDeDireccion;
import ar.edu.utn.frba.dds.domain.filtro.FiltroDeEtiqueta;
import ar.edu.utn.frba.dds.domain.filtro.FiltroDeFecha;
import ar.edu.utn.frba.dds.domain.filtro.FiltroDeFechaDeCarga;
import ar.edu.utn.frba.dds.domain.filtro.FiltroDeLugar;
import ar.edu.utn.frba.dds.domain.filtro.FiltroDeOrigen;
import ar.edu.utn.frba.dds.domain.filtro.FiltroDeTitulo;
import ar.edu.utn.frba.dds.domain.filtro.FiltroListaAnd;
import ar.edu.utn.frba.dds.domain.fuentes.FuenteDinamica;
import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import ar.edu.utn.frba.dds.domain.info.PuntoGeografico;
import ar.edu.utn.frba.dds.domain.origen.Origen;
import ar.edu.utn.frba.dds.domain.reportes.RepositorioDeSolicitudes;
import ar.edu.utn.frba.dds.domain.serviciodevisualizacion.ServicioDeVisualizacion;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

public class FiltroTest {
  PuntoGeografico pgAux = new PuntoGeografico(33.39627891281455, 44.48695991794239);
  FuenteDinamica fuenteAuxD = new FuenteDinamica("Julio Cesar", null);
  LocalDateTime horaAux = LocalDateTime.of(2025, 5, 6, 20, 9);
  List<String> etiquetasAux = List.of(
      "#ancianita",
      "#robo_a_mano_armada",
      "#violencia",
      "#leyDeProtecciónALasAncianitas",
      "#NOalaVIOLENCIAcontraABUELITAS"
  );
  private DetectorSpam detectorSpam;
  private final RepositorioDeSolicitudes repositorio = new RepositorioDeSolicitudes(detectorSpam);

  public List<Hecho> crearColeccionHechoYDevolverlo() {
    fuenteAuxD.crearHecho(
        "titulo",
        "Un día más siendo del conurbano",
        "Robos",
        "dire",
        pgAux,
        horaAux,
        etiquetasAux
    );

    Coleccion bonaerense = fuenteAuxD.crearColeccion(
        "Robos",
        "Un día más siendo del conurbano",
        "Robos"
    );

    ServicioDeVisualizacion servicio = new ServicioDeVisualizacion();
    return servicio.obtenerHechosColeccion(bonaerense, repositorio);
  }

  @Test
  public void filtraPorCategoriaCorrectamente() {
    List<Hecho> hechos = crearColeccionHechoYDevolverlo();
    FiltroDeCategoria filtroCategoria = new FiltroDeCategoria("Robos");
    assertNotEquals(
        0,
        filtroCategoria.filtrar(hechos)
                       .size()
    );
  }

  @Test
  public void filtraPorDireccionCorrectamente() {
    List<Hecho> hechos = crearColeccionHechoYDevolverlo();
    FiltroDeDireccion filtroDireccion = new FiltroDeDireccion("Mozart 2300");
    assertEquals(
        0,
        filtroDireccion.filtrar(hechos)
                       .size()
    );
  }

  @Test
  public void filtraPorEtiquetaCorrectamente() {
    List<Hecho> hechos = crearColeccionHechoYDevolverlo();
    FiltroDeEtiqueta filtroEtiqueta = new FiltroDeEtiqueta(etiquetasAux.get(0));
    assertNotEquals(
        0,
        filtroEtiqueta.filtrar(hechos)
                      .size()
    );
  }

  @Test
  public void filtraPorFechaCorrectamente() {
    List<Hecho> hechos = crearColeccionHechoYDevolverlo();
    FiltroDeFecha filtroFecha = new FiltroDeFecha(horaAux);
    assertNotEquals(
        0,
        filtroFecha.filtrar(hechos)
                   .size()
    );
  }

  @Test
  public void filtraPorFechaCargaCorrectamente() {
    List<Hecho> hechos = crearColeccionHechoYDevolverlo();
    LocalDateTime fecha = LocalDateTime.now();
    FiltroDeFechaDeCarga filtroFecha = new FiltroDeFechaDeCarga(fecha);
    assertNotEquals(
        0,
        filtroFecha.filtrar(hechos)
                   .size()
    );
  }

  @Test
  public void filtraPorLugarCorrectamente() {
    List<Hecho> hechos = crearColeccionHechoYDevolverlo();
    FiltroDeLugar filtroLugar = new FiltroDeLugar(pgAux);
    assertNotEquals(
        0,
        filtroLugar.filtrar(hechos)
                   .size()
    );
  }

  @Test
  public void filtraPorOrigenCorrectamente() {
    List<Hecho> hechos = crearColeccionHechoYDevolverlo();
    FiltroDeOrigen filtroOrigen = new FiltroDeOrigen(Origen.DATASET);
    assertEquals(
        0,
        filtroOrigen.filtrar(hechos)
                    .size()
    );
  }

  @Test
  public void filtraPorTituloCorrectamente() {
    List<Hecho> hechos = crearColeccionHechoYDevolverlo();
    FiltroDeTitulo filtroTitulo = new FiltroDeTitulo("titulo");
    assertNotEquals(
        0,
        filtroTitulo.filtrar(hechos)
                    .size()
    );
  }

  @Test
  public void aplicaVariosFiltrosCorrectamente() {
    List<Hecho> hechos = crearColeccionHechoYDevolverlo();
    FiltroDeCategoria filtroCategoria = new FiltroDeCategoria("Robos");
    FiltroDeDireccion filtroDireccion = new FiltroDeDireccion("dire");
    FiltroDeEtiqueta filtroEtiqueta = new FiltroDeEtiqueta(etiquetasAux.get(0));
    List<Filtro> filtros = new ArrayList<>();
    filtros.add(filtroCategoria);
    filtros.add(filtroDireccion);
    filtros.add(filtroEtiqueta);
    FiltroListaAnd filtroListaAnd = new FiltroListaAnd(filtros);
    assertNotEquals(
        0,
        filtroListaAnd.filtrar(hechos)
                      .size()
    );
  }
}