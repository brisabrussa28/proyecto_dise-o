package ar.edu.utn.frba.dds;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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
import ar.edu.utn.frba.dds.domain.reportes.GestorDeReportes;
import ar.edu.utn.frba.dds.domain.rol.Rol;
import ar.edu.utn.frba.dds.domain.serviciodevisualizacion.ServicioDeVisualizacion;
import ar.edu.utn.frba.dds.main.Usuario;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class FiltroTest {
  Usuario contribuyenteA = new Usuario("Jorge", "jorgesampa@outlook.com", Set.of(Rol.ADMINISTRADOR, Rol.CONTRIBUYENTE));
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
  private DetectorSpam detectorSpam ;
  private GestorDeReportes gestor = new GestorDeReportes(detectorSpam);

  public List<Hecho> crearColeccionHechoYDevolverlo() {
    contribuyenteA.crearHecho(
        "titulo",
        "Un día más siendo del conurbano",
        "Robos",
        "dire",
        pgAux,
        horaAux,
        etiquetasAux,
        fuenteAuxD
    );
    Usuario illuminati = new Usuario(
        "△",
        "libellumcipher@incognito.com",
        Set.of(Rol.CONTRIBUYENTE, Rol.ADMINISTRADOR, Rol.VISUALIZADOR)
    );
    Coleccion bonaerense = illuminati.crearColeccion(
        "Robos",
        "Un día más siendo del conurbano",
        "Robos",
        fuenteAuxD
    );
    return illuminati.visualizarHechos(bonaerense, gestor, new ServicioDeVisualizacion());
  }

  @Test
  public void filtraPorCategoriaCorrectamente() {
    List<Hecho> hechos = crearColeccionHechoYDevolverlo();
    FiltroDeCategoria filtroCategoria = new FiltroDeCategoria("Robos");
    assertFalse(filtroCategoria.filtrar(hechos)
                               .isEmpty());
  }

  @Test
  public void filtraPorDireccionCorrectamente() {
    List<Hecho> hechos = crearColeccionHechoYDevolverlo();
    FiltroDeDireccion filtroDireccion = new FiltroDeDireccion("Mozart 2300");
    assertTrue(filtroDireccion.filtrar(hechos)
                              .isEmpty());
  }

  @Test
  public void filtraPorEtiquetaCorrectamente() {
    List<Hecho> hechos = crearColeccionHechoYDevolverlo();
    FiltroDeEtiqueta filtroEtiqueta = new FiltroDeEtiqueta(etiquetasAux.get(0));
    assertFalse(filtroEtiqueta.filtrar(hechos)
                              .isEmpty());
  }

  @Test
  public void filtraPorFechaCorrectamente() {
    List<Hecho> hechos = crearColeccionHechoYDevolverlo();
    FiltroDeFecha filtroFecha = new FiltroDeFecha(horaAux);
    assertFalse(filtroFecha.filtrar(hechos)
                           .isEmpty());
  }

  @Test
  public void filtraPorFechaCargaCorrectamente() {
    List<Hecho> hechos = crearColeccionHechoYDevolverlo();
    LocalDateTime fecha = LocalDateTime.now();
    FiltroDeFechaDeCarga filtroFecha = new FiltroDeFechaDeCarga(fecha);
    assertFalse(filtroFecha.filtrar(hechos)
                           .isEmpty());
  }

  @Test
  public void filtraPorLugarCorrectamente() {
    List<Hecho> hechos = crearColeccionHechoYDevolverlo();
    FiltroDeLugar filtroLugar = new FiltroDeLugar(pgAux);
    assertFalse(filtroLugar.filtrar(hechos)
                           .isEmpty());
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
    assertFalse(filtroTitulo.filtrar(hechos)
                            .isEmpty());
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
    assertFalse(filtroListaAnd.filtrar(hechos)
                              .isEmpty());
  }
}