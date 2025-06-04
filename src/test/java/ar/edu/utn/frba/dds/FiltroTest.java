package ar.edu.utn.frba.dds;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import ar.edu.utn.frba.dds.domain.coleccion.Coleccion;
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
import ar.edu.utn.frba.dds.main.Administrador;
import ar.edu.utn.frba.dds.main.Contribuyente;
import ar.edu.utn.frba.dds.main.Visualizador;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

public class FiltroTest {
  Contribuyente contribuyenteA = new Contribuyente(null, null);
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
  Hecho hechoAux = new Hecho(
      "titulo",
      "desc",
      "Robo",
      "Av Corrientes 1234",
      pgAux,
      horaAux,
      LocalDateTime.now(),
      Origen.DATASET,
      etiquetasAux
  );

  public List<Hecho> crearColeccionHechoYDevolverlo() {
    contribuyenteA.crearHecho(
        "titulo",
        "Un día más siendo del conurbano",
        "Robos",
        "Mozart 2300",
        pgAux,
        horaAux,
        etiquetasAux,
        fuenteAuxD
    );

    Coleccion bonaerense = new Administrador("△", "libellumcipher@incognito.com").crearColeccion("Robos", "Un día más siendo del conurbano", "Robos", fuenteAuxD);
    return new Visualizador(null, null).visualizarHechos(bonaerense);
  }

  @Test
  public void filtraPorCategoriaCorrectamente() {
    List<Hecho> hechos = crearColeccionHechoYDevolverlo();
    FiltroDeCategoria filtroCategoria = new FiltroDeCategoria("Robos");
    assertEquals("Robos", filtroCategoria.filtrar(hechos).get(0).getCategoria());
  }

  @Test
  public void filtraPorDireccionCorrectamente() {
    List<Hecho> hechos = crearColeccionHechoYDevolverlo();
    FiltroDeDireccion filtroDireccion = new FiltroDeDireccion("Mozart 2300");
    assertEquals("Mozart 2300", filtroDireccion.filtrar(hechos).get(0).getDireccion());
  }

  @Test
  public void filtraPorEtiquetaCorrectamente() {
    List<Hecho> hechos = crearColeccionHechoYDevolverlo();
    FiltroDeEtiqueta filtroEtiqueta = new FiltroDeEtiqueta(etiquetasAux.get(0));
    assertEquals("#ancianita", filtroEtiqueta.filtrar(hechos).get(0).getEtiquetas().get(0));
  }

  @Test
  public void filtraPorFechaCorrectamente() {
    List<Hecho> hechos = crearColeccionHechoYDevolverlo();
    FiltroDeFecha filtroFecha = new FiltroDeFecha(horaAux);
    assertEquals(horaAux, filtroFecha.filtrar(hechos).get(0).getFechaSuceso());
  }

  @Test
  public void filtraPorFechaCargaCorrectamente() {
    List<Hecho> hechos = crearColeccionHechoYDevolverlo();
    FiltroDeFechaDeCarga filtroFecha = new FiltroDeFechaDeCarga(LocalDateTime.now());
    assertEquals(LocalDateTime.now().getHour(), filtroFecha.filtrar(hechos).get(0).getFechaCarga().getHour());
    assertEquals(LocalDateTime.now().getMinute(), filtroFecha.filtrar(hechos).get(0).getFechaCarga().getMinute());
  }

  @Test
  public void filtraPorLugarCorrectamente() {
    List<Hecho> hechos = crearColeccionHechoYDevolverlo();
    FiltroDeLugar filtroLugar = new FiltroDeLugar(pgAux);
    assertEquals(pgAux, filtroLugar.filtrar(hechos).get(0).getUbicacion());
  }

  @Test
  public void filtraPorOrigenCorrectamente() {
    List<Hecho> hechos = crearColeccionHechoYDevolverlo();
    hechos.add(hechoAux);
    FiltroDeOrigen filtroOrigen = new FiltroDeOrigen(Origen.PROVISTO_CONTRIBUYENTE);
    List<Hecho> hechosFiltrados = filtroOrigen.filtrar(hechos);
    assertEquals(1, hechosFiltrados.size());
    assertEquals(Origen.PROVISTO_CONTRIBUYENTE, hechosFiltrados.get(0).getOrigen());
  }

  @Test
  public void filtraPorTituloCorrectamente() {
    List<Hecho> hechos = crearColeccionHechoYDevolverlo();
    FiltroDeTitulo filtroTitulo = new FiltroDeTitulo("titulo");
    assertEquals("titulo", filtroTitulo.filtrar(hechos).get(0).getTitulo());
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
    assertFalse(filtroListaAnd.filtrar(hechos).isEmpty());
  }
}