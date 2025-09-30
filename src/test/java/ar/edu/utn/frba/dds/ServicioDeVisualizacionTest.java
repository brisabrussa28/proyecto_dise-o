package ar.edu.utn.frba.dds;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ar.edu.utn.frba.dds.domain.coleccion.Coleccion;
import ar.edu.utn.frba.dds.domain.filtro.Filtro;
import ar.edu.utn.frba.dds.domain.filtro.condiciones.CondicionTrue;
import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import ar.edu.utn.frba.dds.domain.reportes.RepositorioDeSolicitudes;
import ar.edu.utn.frba.dds.domain.serviciodevisualizacion.ServicioDeVisualizacion;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class ServicioDeVisualizacionTest {

  private ServicioDeVisualizacion servicio;
  private Coleccion coleccionMock;
  private RepositorioDeSolicitudes repositorioMock;
  private Filtro filtroAdicionalMock;
  private Hecho hecho1;
  private Hecho hecho2;

  @BeforeEach
  public void setUp() {
    // Inicialización de objetos
    servicio = new ServicioDeVisualizacion();
    coleccionMock = mock(Coleccion.class);
    repositorioMock = mock(RepositorioDeSolicitudes.class);
    filtroAdicionalMock = mock(Filtro.class);
    hecho1 = mock(Hecho.class);
    hecho2 = mock(Hecho.class);
    Filtro filtroNulo = new Filtro(new CondicionTrue());
    when(repositorioMock.filtroExcluyente()).thenReturn(filtroNulo);
  }

  @Test
  @DisplayName("obtenerHechosDeColeccion() devuelve la lista de hechos que provee la colección")
  public void obtenerHechosDevuelveLosHechosDeLaColeccion() {
    // Arrange
    List<Hecho> hechosEsperados = List.of(hecho1, hecho2);
    when(coleccionMock.obtenerHechosFiltrados(any(Filtro.class))).thenReturn(hechosEsperados);

    // Act
    List<Hecho> resultado = servicio.obtenerHechosDeColeccion(coleccionMock, repositorioMock);

    // Assert
    assertEquals(2, resultado.size());
    assertTrue(resultado.contains(hecho1));
    assertTrue(resultado.contains(hecho2));
  }

  @Test
  @DisplayName("filtrarHechosDeColeccion() aplica un filtro adicional correctamente")
  public void filtrarHechosDevuelveHechosFiltrados() {
    // Arrange
    List<Hecho> hechosBase = List.of(hecho1, hecho2);
    List<Hecho> hechosFiltradosEsperados = List.of(hecho2);
    when(coleccionMock.obtenerHechosFiltrados(any(Filtro.class))).thenReturn(hechosBase);
    when(filtroAdicionalMock.filtrar(hechosBase)).thenReturn(hechosFiltradosEsperados);

    // Act
    List<Hecho> resultado = servicio.filtrarHechosDeColeccion(coleccionMock, filtroAdicionalMock, repositorioMock);

    // Assert
    assertEquals(1, resultado.size());
    assertEquals(hecho2, resultado.get(0));
  }

  @Test
  @DisplayName("Si la colección devuelve una lista vacía, el resultado final también es vacío")
  public void coleccionDevuelveListaVaciaYFiltrarTambien() {
    // Arrange
    when(coleccionMock.obtenerHechosFiltrados(any(Filtro.class))).thenReturn(List.of());
    when(filtroAdicionalMock.filtrar(List.of())).thenReturn(List.of());

    // Act
    List<Hecho> resultado = servicio.filtrarHechosDeColeccion(coleccionMock, filtroAdicionalMock, repositorioMock);

    // Assert
    assertTrue(resultado.isEmpty());
  }
}
