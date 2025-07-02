package ar.edu.utn.frba.dds;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import ar.edu.utn.frba.dds.domain.coleccion.Coleccion;
import ar.edu.utn.frba.dds.domain.filtro.Filtro;
import ar.edu.utn.frba.dds.domain.filtro.FiltroIdentidad;
import ar.edu.utn.frba.dds.domain.fuentes.FuenteDinamica;
import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import ar.edu.utn.frba.dds.domain.origen.Origen;
import ar.edu.utn.frba.dds.domain.reportes.Aceptar_Solicitud;
import ar.edu.utn.frba.dds.domain.reportes.RepositorioDeSolicitudes;
import ar.edu.utn.frba.dds.domain.reportes.Solicitud;
import ar.edu.utn.frba.dds.domain.serviciodevisualizacion.ServicioDeVisualizacion;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class UsuarioTest {

  Coleccion coleccion;
  RepositorioDeSolicitudes repositorio;
  ServicioDeVisualizacion servicio;
  Filtro filtro;

  @BeforeEach
  void setUp() {
    coleccion = mock(Coleccion.class);
    repositorio = mock(RepositorioDeSolicitudes.class);
    servicio = mock(ServicioDeVisualizacion.class);
    filtro = mock(Filtro.class);
  }

  @Test
  void visualizadorPuedeVisualizarHechos() {
    when(servicio.obtenerHechosColeccion(coleccion, repositorio)).thenReturn(List.of());
    List<Hecho> hechos = servicio.obtenerHechosColeccion(coleccion, repositorio);
    assertNotNull(hechos);
  }

  @Test
  void visualizadorFiltraHechos() {
    Hecho hecho = mock(Hecho.class);
    FiltroIdentidad filtroIdentidad = new FiltroIdentidad();
    when(servicio.filtrarHechosColeccion(coleccion, filtroIdentidad, repositorio)).thenReturn(List.of(hecho));

    List<Hecho> filtrados = servicio.filtrarHechosColeccion(coleccion, filtroIdentidad, repositorio);

    assertEquals(1, filtrados.size());
    assertEquals(hecho, filtrados.get(0));
  }

  @Test
  void usuarioNoPuedeVisualizarSiElServicioRetornaNull() {
    ServicioDeVisualizacion servicioMock = mock(ServicioDeVisualizacion.class);
    when(servicioMock.obtenerHechosColeccion(any(), any())).thenReturn(null);

    assertNull(servicioMock.obtenerHechosColeccion(any(), any()));
  }

  //MODFICAR TEST -- Modificar Agregar hecho para que lo cree antes de subirlo y que diga ProvistoContribuyente (idea
  // xd)
  @Test
  void contribuyentePuedeCrearHecho() {
    FuenteDinamica fuente = new FuenteDinamica("Fuente", null);

    Hecho hecho = fuente.crearHecho(
        "Titulo",
        "Descripcion",
        "Categoria",
        "Direccion",
        null,
        LocalDateTime.now(),
        List.of("etiqueta1")
    );

    assertNotNull(hecho);
    assertTrue(fuente.obtenerHechos().contains(hecho));
    assertEquals(Origen.PROVISTO_CONTRIBUYENTE, hecho.getOrigen());
  }

  @Test
  void administradorPuedeCrearColeccion() {
    FuenteDinamica fuente = new FuenteDinamica("Fuente", null);

    Coleccion coleccion = fuente.crearColeccion("Titulo","Descripcion", "Categoria");

    assertNotNull(coleccion);
    assertEquals("Titulo", coleccion.getTitulo());
  }

  @Test
  void administradorPuedeGestionarSolicitudesCorrectamente() {
    RepositorioDeSolicitudes repositorioMock = mock(RepositorioDeSolicitudes.class);
    Solicitud solicitudMock = mock(Solicitud.class);

    when(repositorioMock.obtenerSolicitud()).thenReturn(solicitudMock);

    Solicitud solicitud = repositorioMock.obtenerSolicitud();
    assertNotNull(solicitud);
    verify(repositorioMock).obtenerSolicitud();

    repositorioMock.gestionarSolicitud(solicitud, Aceptar_Solicitud.ACEPTAR);
    verify(repositorioMock).gestionarSolicitud(solicitud, Aceptar_Solicitud.ACEPTAR);
  }

}

