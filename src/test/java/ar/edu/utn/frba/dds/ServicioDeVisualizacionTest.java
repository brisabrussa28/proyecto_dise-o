package ar.edu.utn.frba.dds;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ar.edu.utn.frba.dds.domain.coleccion.Coleccion;
import ar.edu.utn.frba.dds.domain.filtro.FiltroPersistente;
import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import ar.edu.utn.frba.dds.domain.reportes.RepositorioDeSolicitudes;
import ar.edu.utn.frba.dds.domain.serviciodevisualizacion.ServicioDeVisualizacion;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ServicioDeVisualizacionTest {

  private ServicioDeVisualizacion servicio;
  private Coleccion coleccionMock;
  private RepositorioDeSolicitudes repositorioMock;
  private FiltroPersistente filtroMock;
  private Hecho hecho1;
  private Hecho hecho2;

  @BeforeEach
  public void setUp() {
    servicio = new ServicioDeVisualizacion();
    coleccionMock = mock(Coleccion.class);
    repositorioMock = mock(RepositorioDeSolicitudes.class);
    filtroMock = mock(FiltroPersistente.class);

    hecho1 = mock(Hecho.class);
    hecho2 = mock(Hecho.class);
  }

  @Test
  public void obtenerHechosDevuelveLosHechosDeLaColeccion() {
    List<Hecho> hechos = List.of(hecho1, hecho2);
    when(coleccionMock.getHechos(repositorioMock)).thenReturn(hechos);

    List<Hecho> resultado = servicio.obtenerHechosColeccion(coleccionMock, repositorioMock);

    assertEquals(2, resultado.size());
    assertTrue(resultado.contains(hecho1));
  }

  @Test
  public void filtrarHechosDevuelveHechosFiltrados() {
    List<Hecho> hechos = List.of(hecho1, hecho2);
    List<Hecho> filtrados = List.of(hecho2);

    when(coleccionMock.getHechos(repositorioMock)).thenReturn(hechos);
    when(filtroMock.filtrar(hechos)).thenReturn(filtrados);

    List<Hecho> resultado = servicio.filtrarHechosColeccion(coleccionMock, filtroMock, repositorioMock);

    assertEquals(1, resultado.size());
    assertEquals(hecho2, resultado.get(0));
  }

  @Test
  public void coleccionDevuelveListaVaciaYFiltrarTambien() {
    when(coleccionMock.getHechos(repositorioMock)).thenReturn(List.of());
    when(filtroMock.filtrar(List.of())).thenReturn(List.of());

    List<Hecho> resultado = servicio.filtrarHechosColeccion(coleccionMock, filtroMock, repositorioMock);

    assertTrue(resultado.isEmpty());
  }
}
