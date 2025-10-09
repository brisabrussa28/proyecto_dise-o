package ar.edu.utn.frba.dds;

import static ar.edu.utn.frba.dds.domain.filtro.condiciones.Operador.DISTINTO;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ar.edu.utn.frba.dds.domain.coleccion.Coleccion;
import ar.edu.utn.frba.dds.domain.filtro.Filtro;
import ar.edu.utn.frba.dds.domain.filtro.condiciones.CondicionGenerica;
import ar.edu.utn.frba.dds.domain.filtro.condiciones.CondicionPredicado;
import ar.edu.utn.frba.dds.domain.filtro.condiciones.CondicionTrue;
import ar.edu.utn.frba.dds.domain.fuentes.Fuente;
import ar.edu.utn.frba.dds.domain.fuentes.FuenteDinamica;
import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import ar.edu.utn.frba.dds.domain.hecho.HechoBuilder;
import ar.edu.utn.frba.dds.domain.hecho.Origen;
import ar.edu.utn.frba.dds.domain.info.PuntoGeografico;
import ar.edu.utn.frba.dds.domain.utils.DBUtils;
import java.time.LocalDateTime;
import java.util.List;
import javax.persistence.EntityManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;

public class ColeccionTest {
  private FuenteDinamica fuente;
  private Filtro filtroExcluyenteVacio;
  private final LocalDateTime horaAux = LocalDateTime.now().minusDays(1);

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
    fuente = new FuenteDinamica("Fuente para Colecciones");
    // Un filtro que no hace nada, para tests que no necesitan exclusión.
    filtroExcluyenteVacio = new Filtro(new CondicionTrue());
  }

  /**
   * Se agrega un método de limpieza para garantizar el aislamiento entre tests,
   * especialmente si alguno interactúa con la base de datos.
   */
  @AfterEach
  public void tearDown() {
    EntityManager em = DBUtils.getEntityManager();
    DBUtils.comenzarTransaccion(em);
    em.createQuery("DELETE FROM Solicitud").executeUpdate();
    DBUtils.commit(em);
  }

  // Método auxiliar para crear Hechos válidos y completos.
  private Hecho crearHechoCompleto(String titulo) {
    return new HechoBuilder()
        .conTitulo(titulo)
        .conDescripcion("Desc valida")
        .conCategoria("Cat de Prueba")
        .conDireccion("Dir de Prueba")
        .conProvincia("Prov de Prueba")
        .conUbicacion(new PuntoGeografico(1.0, 1.0))
        .conFechaSuceso(horaAux)
        .conFechaCarga(LocalDateTime.now())
        .conFuenteOrigen(Origen.PROVISTO_CONTRIBUYENTE)
        .conEtiquetas(List.of("#test"))
        .build();
  }

  @Test
  public void coleccionCreadaCorrectamente() {
    Coleccion bonaerense = new Coleccion("Robos", fuente, "Un día más siendo del conurbano", "Robos");
    assertEquals("Robos", bonaerense.getTitulo());
    assertEquals("Un día más siendo del conurbano", bonaerense.getDescripcion());
    assertEquals("Robos", bonaerense.getCategoria());
  }

  @Test
  public void coleccionContieneUnHecho() {
    Coleccion coleccion = new Coleccion("Robos", fuente, "Descripcion", "Robos");
    Hecho hecho = crearHechoCompleto("titulo");
    fuente.agregarHecho(hecho);
    assertTrue(coleccion.contieneHechoFiltrado(hecho, filtroExcluyenteVacio));
  }

  @Test
  public void coleccionEsDeCategoriaCorrectamente() {
    Coleccion coleccion = new Coleccion("Robos", fuente, "Descripcion", "Robos");
    assertEquals("Robos", coleccion.getCategoria());
    assertNotEquals("Violencia", coleccion.getCategoria());
  }

  @Test
  public void siCreoUnaColeccionSinTituloLanzaExcepcion() {
    assertThrows(IllegalArgumentException.class, () -> new Coleccion("", fuente, "hola", "Robos"));
  }

  @Test
  public void siCreoUnaColeccionSinDescripcionLanzaExcepcion() {
    assertThrows(IllegalArgumentException.class, () -> new Coleccion("Robos", fuente, "", "Robos"));
  }

  @Test
  public void siCreoUnaColeccionSinCategoriaLanzaExcepcion() {
    assertThrows(IllegalArgumentException.class, () -> new Coleccion("Robos", fuente, "hola", ""));
  }

  @Test
  public void nombreColeccionNoEsNull() {
    Coleccion coleccion = new Coleccion("Robos", fuente, "Descripcion", "Robos");
    assertNotNull(coleccion.getTitulo());
  }

  @Test
  public void coleccionAplicaCorrectamenteUnFiltroDeExclusion() {
    // Arrange
    Coleccion coleccion = new Coleccion("Robos", fuente, "Descripcion", "Robos");
    Hecho hechoAIncluir = crearHechoCompleto("Hecho a incluir");
    Hecho hechoAExcluir = crearHechoCompleto("Hecho a excluir");
    fuente.agregarHecho(hechoAIncluir);
    fuente.agregarHecho(hechoAExcluir);

    // Act: Se crea manualmente un filtro que excluye un hecho específico.
    Filtro filtroDeExclusionReal = new Filtro(new CondicionPredicado(h -> !h.equals(hechoAExcluir)));


    // Assert: Se verifica que la colección aplica correctamente el filtro externo.
    assertTrue(coleccion.contieneHechoFiltrado(hechoAIncluir, filtroDeExclusionReal));
    assertFalse(coleccion.contieneHechoFiltrado(hechoAExcluir, filtroDeExclusionReal));
  }

  @Test
  public void coleccionContieneFuenteCorrecta() {
    Coleccion coleccion = new Coleccion("Robos", fuente, "Descripcion", "Robos");
    assertTrue(coleccion.contieneFuente(fuente));
  }

  @Test
  public void testFiltradoYSpamDetectadoCorrectamente() {
    // Arrange
    Fuente fuenteMock = mock(Fuente.class);
    Hecho valido = crearHechoCompleto("valido");
    Hecho spam = crearHechoCompleto("spam");
    Hecho filtrado = crearHechoCompleto("filtrado");
    when(fuenteMock.getHechos()).thenReturn(List.of(valido, spam, filtrado));

    Coleccion coleccion = new Coleccion("Test", fuenteMock, "Descripcion", "Categoria");
    coleccion.setCondicion(new CondicionGenerica("titulo", DISTINTO, "filtrado"));

    Filtro filtroExterno = new Filtro(new CondicionGenerica("titulo", DISTINTO, "spam"));

    // Act
    List<Hecho> hechosFinales = coleccion.obtenerHechosFiltrados(filtroExterno);

    // Assert
    assertEquals(1, hechosFinales.size());
    assertTrue(hechosFinales.contains(valido));
    assertFalse(hechosFinales.contains(spam));
    assertFalse(hechosFinales.contains(filtrado));
  }

  @Test
  public void testHechosCambianConFuente() {
    Fuente fuenteMock = mock(Fuente.class);
    Hecho hecho1 = mock(Hecho.class);
    Hecho hecho2 = mock(Hecho.class);
    when(fuenteMock.getHechos()).thenReturn(List.of(hecho1));

    Coleccion coleccion = new Coleccion("Test", fuenteMock, "Descripcion", "Categoria");
    assertEquals(1, coleccion.obtenerHechosFiltrados(filtroExcluyenteVacio).size());

    when(fuenteMock.getHechos()).thenReturn(List.of(hecho1, hecho2));
    assertEquals(2, coleccion.obtenerHechosFiltrados(filtroExcluyenteVacio).size());
  }
}

