package ar.edu.utn.frba.dds;

import ar.edu.utn.frba.dds.domain.detectorSpam.DetectorSpam;
import ar.edu.utn.frba.dds.domain.filtro.*;
import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import ar.edu.utn.frba.dds.domain.info.PuntoGeografico;
import ar.edu.utn.frba.dds.domain.origen.Origen;
import ar.edu.utn.frba.dds.domain.fuentes.FuenteDinamica;
import ar.edu.utn.frba.dds.domain.coleccion.Coleccion;
import ar.edu.utn.frba.dds.domain.reportes.GestorDeReportes;
import ar.edu.utn.frba.dds.main.Usuario;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class FiltroTest {
  private DetectorSpam detectorSpam;
  private GestorDeReportes gestor = new GestorDeReportes(detectorSpam);
  Usuario contribuyenteA = new Usuario(null, null);
  PuntoGeografico pgAux = new PuntoGeografico(33.39627891281455, 44.48695991794239);
  FuenteDinamica fuenteAuxD = new FuenteDinamica("Julio Cesar", null);
  Date horaAux = Date.from(LocalDateTime.of(2025, 5, 6, 20, 9)
      .atZone(ZoneId.systemDefault())
      .toInstant());
  List<String> etiquetasAux = List.of(
      "#ancianita",
      "#robo_a_mano_armada",
      "#violencia",
      "#leyDeProtecciónALasAncianitas",
      "#NOalaVIOLENCIAcontraABUELITAS"
  );

  public List<Hecho> crearColeccionHechoYDevolverlo() {
    contribuyenteA.crearHecho("titulo", "Un día más siendo del conurbano", "Robos", "dire", pgAux, horaAux, etiquetasAux, fuenteAuxD);
    Coleccion bonaerense = new Usuario("△", "libellumcipher@incognito.com").crearColeccion("Robos", "Un día más siendo del conurbano", "Robos", fuenteAuxD);
    return new Usuario(null, null).visualizarHechos(bonaerense,gestor, new ServicioDeVisualizacion());
  }

  @Test
  public void filtraPorCategoriaCorrectamente() {
    List<Hecho> hechos = crearColeccionHechoYDevolverlo();
    FiltroDeCategoria filtroCategoria = new FiltroDeCategoria("Robos");
    assertFalse(filtroCategoria.filtrar(hechos).isEmpty());
  }

  @Test
  public void filtraPorDireccionCorrectamente() {
    List<Hecho> hechos = crearColeccionHechoYDevolverlo();
    FiltroDeDireccion filtroDireccion = new FiltroDeDireccion("Mozart 2300");
    assertTrue(filtroDireccion.filtrar(hechos).isEmpty());
  }

  @Test
  public void filtraPorEtiquetaCorrectamente() {
    List<Hecho> hechos = crearColeccionHechoYDevolverlo();
    FiltroDeEtiqueta filtroEtiqueta = new FiltroDeEtiqueta(etiquetasAux.get(0));
    assertFalse(filtroEtiqueta.filtrar(hechos).isEmpty());
  }

  @Test
  public void filtraPorFechaCorrectamente() {
    List<Hecho> hechos = crearColeccionHechoYDevolverlo();
    FiltroDeFecha filtroFecha = new FiltroDeFecha(horaAux);
    assertFalse(filtroFecha.filtrar(hechos).isEmpty());
  }

  @Test
  public void filtraPorFechaCargaCorrectamente() {
    List<Hecho> hechos = crearColeccionHechoYDevolverlo();
    Date fecha = new Date();
    FiltroDeFechaDeCarga filtroFecha = new FiltroDeFechaDeCarga(fecha);
    assertFalse(filtroFecha.filtrar(hechos).isEmpty());
  }

  @Test
  public void filtraPorLugarCorrectamente() {
    List<Hecho> hechos = crearColeccionHechoYDevolverlo();
    FiltroDeLugar filtroLugar = new FiltroDeLugar(pgAux);
    assertFalse(filtroLugar.filtrar(hechos).isEmpty());
  }

  @Test
  public void filtraPorOrigenCorrectamente() {
    List<Hecho> hechos = crearColeccionHechoYDevolverlo();
    FiltroDeOrigen filtroOrigen = new FiltroDeOrigen(Origen.CARGA_MANUAL);
    assertTrue(filtroOrigen.filtrar(hechos).isEmpty());
  }

  @Test
  public void filtraPorTituloCorrectamente() {
    List<Hecho> hechos = crearColeccionHechoYDevolverlo();
    FiltroDeTitulo filtroTitulo = new FiltroDeTitulo("titulo");
    assertFalse(filtroTitulo.filtrar(hechos).isEmpty());
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