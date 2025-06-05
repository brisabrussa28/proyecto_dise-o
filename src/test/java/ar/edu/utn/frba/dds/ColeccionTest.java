package ar.edu.utn.frba.dds;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ar.edu.utn.frba.dds.domain.coleccion.Coleccion;
import ar.edu.utn.frba.dds.domain.detectorspam.DetectorSpam;
import ar.edu.utn.frba.dds.domain.filtro.Filtro;
import ar.edu.utn.frba.dds.domain.fuentes.Fuente;
import ar.edu.utn.frba.dds.domain.fuentes.FuenteDinamica;
import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import ar.edu.utn.frba.dds.domain.info.PuntoGeografico;
import ar.edu.utn.frba.dds.domain.origen.Origen;
import ar.edu.utn.frba.dds.domain.reportes.GestorDeReportes;
import ar.edu.utn.frba.dds.domain.rol.Rol;
import ar.edu.utn.frba.dds.main.Usuario;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ColeccionTest {
  Usuario iluminati = new Usuario("△", "libellumcipher@incognito.com", Set.of(Rol.ADMINISTRADOR, Rol.CONTRIBUYENTE));
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
  private GestorDeReportes gestor;
  private DetectorSpam detectorSpam;

  @BeforeEach
  void initFileSystem() {
    detectorSpam = mock(DetectorSpam.class);
    gestor = new GestorDeReportes(detectorSpam);
  }


  @Test
  public void coleccionCreadaCorrectamente() {
    Coleccion bonaerense = iluminati.crearColeccion(
        "Robos",
        "Un día más siendo del conurbano",
        "Robos",
        fuenteAuxD
    );

    assertEquals("Robos", bonaerense.getTitulo());
    assertEquals("Un día más siendo del conurbano", bonaerense.getDescripcion());
    assertEquals("Robos", bonaerense.getCategoria());
  }

  @Test
  public void coleccionContieneUnHecho() {
    Coleccion coleccion = iluminati.crearColeccion("Robos", "Descripcion", "Robos", fuenteAuxD);
    Hecho hecho = new Hecho(
        "titulo",
        "desc",
        "Robos",
        "direccion",
        null,
        horaAux,
        horaAux,
        null,
        etiquetasAux
    );
    fuenteAuxD.agregarHecho(hecho);
    assertTrue(coleccion.contieneA(hecho, gestor));
  }

  @Test
  public void coleccionEsDeCategoriaCorrectamente() {
    Coleccion coleccion = iluminati.crearColeccion("Robos", "Descripcion", "Robos", fuenteAuxD);
    assertEquals("Robos", coleccion.getCategoria());
    assertNotEquals("Violencia", coleccion.getCategoria());
  }

  @Test
  public void siCreoUnaColeccionSinTituloLanzaExcepcion() {
    assertThrows(RuntimeException.class, () -> iluminati.crearColeccion("", "hola", "Robos", fuenteAuxD));
  }

  @Test
  public void siCreoUnaColeccionSinDescripcionLanzaExcepcion() {
    assertThrows(RuntimeException.class, () -> iluminati.crearColeccion("Robos", "", "Robos", fuenteAuxD));
  }

  @Test
  public void siCreoUnaColeccionSinCategoriaLanzaExcepcion() {
    assertThrows(RuntimeException.class, () -> iluminati.crearColeccion("Robos", "hola", "", fuenteAuxD));
  }

  @Test
  public void nombreColeccionNoEsNull() {
    Coleccion coleccion = iluminati.crearColeccion("Robos", "Descripcion", "Robos", fuenteAuxD);
    assertNotNull(coleccion.getTitulo());
  }

  @Test
  public void coleccionYaNoContieneHechoEliminadoPorGestor() {
    Coleccion coleccion = iluminati.crearColeccion("Robos", "Descripcion", "Robos", fuenteAuxD);
    Hecho hecho = new Hecho(
        "titulo",
        "desc",
        "Robos",
        "direccion",
        pgAux,
        horaAux,
        horaAux,
        Origen.PROVISTO_CONTRIBUYENTE,
        etiquetasAux
    );
    fuenteAuxD.agregarHecho(hecho);
    when(detectorSpam.esSpam(anyString())).thenReturn(false);
    gestor.marcarComoEliminado(hecho);
    assertFalse(coleccion.contieneA(hecho, gestor));
  }

  @Test
  public void coleccionContieneFuenteCorrecta() {
    Coleccion coleccion = iluminati.crearColeccion("Robos", "Descripcion", "Robos", fuenteAuxD);
    assertTrue(coleccion.contieneFuente(fuenteAuxD));
  }

  @Test
  public void testFiltradoYSpamDetectadoCorrectamente() {
    Fuente fuente = mock(Fuente.class);
    Hecho valido = mock(Hecho.class);
    Hecho spam = mock(Hecho.class);
    when(fuente.obtenerHechos()).thenReturn(List.of(valido, spam));

    Coleccion coleccion = new Coleccion("Test", fuente, "Descripcion", "Categoria");

    Filtro filtroMock = mock(Filtro.class);
    when(filtroMock.filtrar(List.of(valido, spam))).thenReturn(List.of(valido));
    coleccion.setFiltro(filtroMock);

    GestorDeReportes gestorDeReportes = mock(GestorDeReportes.class);
    Filtro filtroExcluyente = mock(Filtro.class);
    when(gestorDeReportes.filtroExcluyente()).thenReturn(filtroExcluyente);
    when(filtroExcluyente.filtrar(List.of(valido))).thenReturn(List.of(valido));

    List<Hecho> hechosFinales = coleccion.getHechos(gestorDeReportes);

    assertEquals(1, hechosFinales.size());
    assertTrue(hechosFinales.contains(valido));
  }

  @Test
  public void testHechosCambianConFuente() {
    Fuente fuente = mock(Fuente.class);
    Hecho hecho1 = mock(Hecho.class);
    Hecho hecho2 = mock(Hecho.class);
    GestorDeReportes gestor = new GestorDeReportes(detectorSpam);

    when(fuente.obtenerHechos()).thenReturn(List.of(hecho1));

    Coleccion coleccion = new Coleccion("Test", fuente, "Descripcion", "Categoria");

    when(coleccion.getHechos(gestor)).thenReturn(List.of(hecho1));
    assertEquals(
        1,
        coleccion.getHechos(gestor)
                 .size()
    );
    assertTrue(coleccion.getHechos(gestor)
                        .contains(hecho1));

    when(fuente.obtenerHechos()).thenReturn(List.of(hecho1, hecho2));

    List<Hecho> hechosActualizados = coleccion.getHechos(gestor);
    assertEquals(2, hechosActualizados.size());
    assertTrue(hechosActualizados.contains(hecho1));
    assertTrue(hechosActualizados.contains(hecho2));
  }
}
