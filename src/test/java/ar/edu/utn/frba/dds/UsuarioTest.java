package ar.edu.utn.frba.dds;

import ar.edu.utn.frba.dds.domain.coleccion.Coleccion;
import ar.edu.utn.frba.dds.domain.filtro.Filtro;
import ar.edu.utn.frba.dds.domain.filtro.FiltroIdentidad;
import ar.edu.utn.frba.dds.domain.fuentes.Fuente;
import ar.edu.utn.frba.dds.domain.fuentes.FuenteDinamica;
import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import ar.edu.utn.frba.dds.domain.reportes.GestorDeReportes;
import ar.edu.utn.frba.dds.domain.rol.Rol;
import ar.edu.utn.frba.dds.domain.serviciodevisualizacion.ServicioDeVisualizacion;
import ar.edu.utn.frba.dds.main.Usuario;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Set;

public class UsuarioTest {

  Usuario visualizador;
  Usuario contribuyente;
  Usuario admin;
  Coleccion coleccion;
  GestorDeReportes gestor;
  ServicioDeVisualizacion servicio;
  Filtro filtro;

  @BeforeEach
  void setUp() {
    visualizador = new Usuario("Visualizador", "visualizador@mail.com", Set.of(Rol.VISUALIZADOR));
    contribuyente = new Usuario("Contribuyente", "contribuyente@mail.com", Set.of(Rol.CONTRIBUYENTE));
    admin = new Usuario("Admin", "admin@mail.com", Set.of(Rol.ADMINISTRADOR));
    coleccion = mock(Coleccion.class);
    gestor = mock(GestorDeReportes.class);
    servicio = mock(ServicioDeVisualizacion.class);
    filtro = mock(Filtro.class);
  }

  @Test
  void usuarioPuedeTenerMultiplesRoles() {
    Usuario usuario = new Usuario("Multi", "multi@mail.com", Set.of(Rol.ADMINISTRADOR, Rol.VISUALIZADOR));
    assertTrue(usuario.tieneRol(Rol.ADMINISTRADOR));
    assertTrue(usuario.tieneRol(Rol.VISUALIZADOR));
  }

  @Test
  void usuarioSinRolesNoTienePermisos() {
    Usuario usuario = new Usuario("SinRol", "sinrol@mail.com", Set.of());
    assertFalse(usuario.tieneRol(Rol.ADMINISTRADOR));
    assertFalse(usuario.tieneRol(Rol.VISUALIZADOR));
    // Should not be able to visualize hechos
    assertThrows(
        RuntimeException.class, () ->
            usuario.visualizarHechos(coleccion, gestor, servicio)
    );
  }

  @Test
  void visualizadorPuedeVisualizarHechos() {
    when(servicio.obtenerHechosColeccion(coleccion, gestor)).thenReturn(List.of());
    List<Hecho> hechos = visualizador.visualizarHechos(coleccion, gestor, servicio);
    assertNotNull(hechos);
  }

  @Test
  void usuarioSinRolVisualizadorNoPuedeVisualizar() {
    assertThrows(
        RuntimeException.class, () -> contribuyente.visualizarHechos(coleccion, gestor, servicio)
    );
  }

  @Test
  void visualizarHechosInvocaServicioCorrespondiente() {
    visualizador.visualizarHechos(coleccion, gestor, servicio);
    verify(servicio).obtenerHechosColeccion(coleccion, gestor);
  }

  @Test
  void visualizadorFiltraHechos() {
    Hecho hecho = mock(Hecho.class);
    when(servicio.obtenerHechosColeccion(coleccion, gestor)).thenReturn(List.of(hecho));
    FiltroIdentidad filtroIdentidad = new FiltroIdentidad();

    List<Hecho> filtrados = visualizador.filtrarHechos(coleccion, filtroIdentidad, gestor, servicio);

    assertEquals(1, filtrados.size());
    assertEquals(hecho, filtrados.get(0));
  }


  @Test
  void usuarioNoPuedeVisualizarSiElServicioRetornaNull() {
    Usuario usuario = new Usuario("Visualizador", "visualizador@mail.com", Set.of(Rol.VISUALIZADOR));
    ServicioDeVisualizacion servicioMock = mock(ServicioDeVisualizacion.class);
    when(servicioMock.obtenerHechosColeccion(any(), any())).thenReturn(null);

    assertNull(usuario.visualizarHechos(coleccion, gestor, servicioMock));
  }


  @Test
  void usuarioConTodosLosRolesPuedeEjecutarAccionesDeTodosLosRoles() {
    Usuario usuario = new Usuario(
        "Todos",
        "todos@mail.com",
        Set.of(Rol.ADMINISTRADOR, Rol.CONTRIBUYENTE, Rol.VISUALIZADOR)
    );
    assertTrue(usuario.tieneRol(Rol.ADMINISTRADOR));
    assertTrue(usuario.tieneRol(Rol.CONTRIBUYENTE));
    assertTrue(usuario.tieneRol(Rol.VISUALIZADOR));

    // ADMINISTRADOR: puede crear coleccion
    Fuente fuente = mock(Fuente.class);
    Coleccion coleccion = usuario.crearColeccion("Titulo", "Descripcion", "Categoria", fuente);
    assertNotNull(coleccion);

    // CONTRIBUYENTE: puede crear hecho
    FuenteDinamica fuenteDinamica = mock(FuenteDinamica.class);
    Hecho hecho = usuario.crearHecho(
        "TituloHecho", "DescripcionHecho", "CategoriaHecho", "Direccion", null,
        java.time.LocalDateTime.now(), List.of("etiqueta1"), fuenteDinamica
    );
    assertNotNull(hecho);
    verify(fuenteDinamica).agregarHecho(hecho);

    // VISUALIZADOR: puede visualizar y filtrar hechos
    GestorDeReportes gestor = mock(GestorDeReportes.class);
    ServicioDeVisualizacion servicio = mock(ServicioDeVisualizacion.class);
    Filtro filtro = new FiltroIdentidad();
    when(servicio.obtenerHechosColeccion(coleccion, gestor)).thenReturn(List.of(hecho));
    when(servicio.filtrarHechosColeccion(coleccion, filtro, gestor)).thenReturn(List.of(hecho));

    List<Hecho> visualizados = usuario.visualizarHechos(coleccion, gestor, servicio);
    List<Hecho> resultado = usuario.filtrarHechos(coleccion, filtro, gestor, servicio);

    assertEquals(1, visualizados.size());
    assertEquals(hecho, visualizados.get(0));
    assertEquals(1, resultado.size());
    assertEquals(hecho, resultado.get(0));
  }

  @Test
  void contribuyentePuedeCrearHecho() {
    Usuario usuario = new Usuario("Contribuyente", "c@mail.com", Set.of(Rol.CONTRIBUYENTE));
    FuenteDinamica fuente = mock(FuenteDinamica.class);

    Hecho hecho = usuario.crearHecho(
        "Titulo", "Descripcion", "Categoria", "Direccion", null,
        LocalDateTime.now(), List.of("etiqueta1"), fuente
    );

    assertNotNull(hecho);
    verify(fuente).agregarHecho(hecho);
  }

  @Test
  void noContribuyenteNoPuedeCrearHecho() {
    Usuario usuario = new Usuario("NoContribuyente", "nc@mail.com", Set.of(Rol.VISUALIZADOR));
    FuenteDinamica fuente = mock(FuenteDinamica.class);

    assertThrows(
        RuntimeException.class, () -> usuario.crearHecho(
            "Titulo", "Descripcion", "Categoria", "Direccion", null,
            LocalDateTime.now(), List.of("etiqueta1"), fuente
        )
    );
  }

  @Test
  void administradorPuedeCrearColeccion() {
    Usuario usuario = new Usuario("Admin", "admin@mail.com", Set.of(Rol.ADMINISTRADOR));
    Fuente fuente = mock(Fuente.class);

    Coleccion coleccion = usuario.crearColeccion("Titulo", "Descripcion", "Categoria", fuente);

    assertNotNull(coleccion);
    assertEquals("Titulo", coleccion.getTitulo());
  }

  @Test
  void noAdministradorNoPuedeCrearColeccion() {
    Usuario usuario = new Usuario("NoAdmin", "noadmin@mail.com", Set.of(Rol.CONTRIBUYENTE));
    Fuente fuente = mock(Fuente.class);

    assertThrows(
        RuntimeException.class, () ->
            usuario.crearColeccion("Titulo", "Descripcion", "Categoria", fuente)
    );
  }
}

