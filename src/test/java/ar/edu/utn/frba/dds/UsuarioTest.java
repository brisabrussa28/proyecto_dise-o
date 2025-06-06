package ar.edu.utn.frba.dds;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ar.edu.utn.frba.dds.domain.coleccion.Coleccion;
import ar.edu.utn.frba.dds.domain.csv.LectorCSV;
import ar.edu.utn.frba.dds.domain.detectorspam.DetectorSpam;
import ar.edu.utn.frba.dds.domain.filtro.Filtro;
import ar.edu.utn.frba.dds.domain.filtro.FiltroIdentidad;
import ar.edu.utn.frba.dds.domain.fuentes.Fuente;
import ar.edu.utn.frba.dds.domain.fuentes.FuenteDinamica;
import ar.edu.utn.frba.dds.domain.fuentes.FuenteEstatica;
import ar.edu.utn.frba.dds.domain.hecho.CampoHecho;
import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import ar.edu.utn.frba.dds.domain.reportes.GestorDeReportes;
import ar.edu.utn.frba.dds.domain.reportes.Solicitud;
import ar.edu.utn.frba.dds.domain.rol.Rol;
import ar.edu.utn.frba.dds.domain.serviciodevisualizacion.ServicioDeVisualizacion;
import ar.edu.utn.frba.dds.usuario.Usuario;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class UsuarioTest {

  Usuario visualizador;
  Usuario contribuyente;
  Usuario admin;
  Coleccion coleccion;
  GestorDeReportes gestor;
  ServicioDeVisualizacion servicio;
  Filtro filtro;
  DetectorSpam spam;

  Map<CampoHecho, List<String>> mapeoCsvEjemplo = Map.of(
      CampoHecho.TITULO, List.of("titulo"),
      CampoHecho.DESCRIPCION, List.of("descripcion"),
      CampoHecho.LATITUD, List.of("latitud"),
      CampoHecho.LONGITUD, List.of("longitud"),
      CampoHecho.FECHA_SUCESO, List.of("fechaSuceso"),
      CampoHecho.CATEGORIA, List.of("categoria"),
      CampoHecho.DIRECCION, List.of("direccion")
  );

  @BeforeEach
  void setUp() {
    visualizador = new Usuario("Visualizador", "visualizador@mail.com", Set.of(Rol.VISUALIZADOR));
    contribuyente = new Usuario("Contribuyente", "contribuyente@mail.com", Set.of(Rol.CONTRIBUYENTE));
    admin = new Usuario("Admin", "admin@mail.com", Set.of(Rol.ADMINISTRADOR, Rol.VISUALIZADOR));
    coleccion = mock(Coleccion.class);
    gestor = mock(GestorDeReportes.class);
    servicio = new ServicioDeVisualizacion();
    filtro = mock(Filtro.class);
  }

  @Test
  void usuarioPuedeTenerMultiplesRoles() {
    Usuario usuario = new Usuario("Multi", "multi@mail.com", Set.of(Rol.ADMINISTRADOR, Rol.VISUALIZADOR));
    assertTrue(usuario.tieneRol(Rol.ADMINISTRADOR));
    assertTrue(usuario.tieneRol(Rol.VISUALIZADOR));
  }

  @Test
  public void usuarioVisualizadorConsultaUnCsv() {
    assertThrows(
        RuntimeException.class, () -> visualizador.importardesdeCsv(
            "src/test/java/ar/edu/utn/frba/dds/CsvDePrueba/ejemplo.csv",
            "ejemplo.csv",
            new LectorCSV(',', "dd/mm/yyyy", mapeoCsvEjemplo)
        )
    );
  }

  @Test
  public void usuarioAdminPuedeConsultarCsv() {
    LectorCSV lector = new LectorCSV(',', "dd/mm/yyyy", mapeoCsvEjemplo);
    assertDoesNotThrow(
        () -> admin.importardesdeCsv(
            "src/test/java/ar/edu/utn/frba/dds/CsvDePrueba/ejemplo.csv",
            "ejemplo.csv",
            lector
        )
    );
    FuenteEstatica fuente = new FuenteEstatica(
        "Fuente",
        "src/test/java/ar/edu/utn/frba/dds/CsvDePrueba/ejemplo.csv",
        lector
    );

    Coleccion coleccion = admin.crearColeccion(
        "Coleccion de Fuente Estática",
        "Es una colección de prueba",
        "PRUEBA",
        fuente
    );

    //when(coleccion.getHechos(gestor)).thenReturn(fuente.obtenerHechos());
//    List<Hecho> hechosTest = fuente.obtenerHechos();
    Coleccion coleccionTest = admin.crearColeccion(
        "PRUEBA",
        "ESTO ES UNA PRUEBA",
        "TEST",
        fuente
    );

    List<Hecho> hechosTest = admin.visualizarHechos(coleccionTest,gestor, servicio);
    assertEquals(
        "EL NESTORNAUTA",
        hechosTest.get(4)
                  .getDireccion()
    );
  }

  @Test
  void usuarioSinRolesNoTienePermisos() {
    Usuario usuario = new Usuario("SinRol", "sinrol@mail.com", Set.of());
    assertFalse(usuario.tieneRol(Rol.ADMINISTRADOR));
    assertFalse(usuario.tieneRol(Rol.VISUALIZADOR));
    assertThrows(RuntimeException.class, () -> usuario.visualizarHechos(coleccion, gestor, servicio));
  }

  @Test
  void visualizadorPuedeVisualizarHechos() {
    when(servicio.obtenerHechosColeccion(coleccion, gestor)).thenReturn(List.of());
    List<Hecho> hechos = visualizador.visualizarHechos(coleccion, gestor, servicio);
    assertNotNull(hechos);
  }

  @Test
  void usuarioSinRolVisualizadorNoPuedeVisualizar() {
    assertThrows(RuntimeException.class, () -> contribuyente.visualizarHechos(coleccion, gestor, servicio));
  }

  @Test
  void visualizarHechosInvocaServicioCorrespondiente() {
    visualizador.visualizarHechos(coleccion, gestor, servicio);
    verify(servicio).obtenerHechosColeccion(coleccion, gestor);
  }

  @Test
  void visualizadorFiltraHechos() {
    Hecho hecho = mock(Hecho.class);
    FiltroIdentidad filtroIdentidad = new FiltroIdentidad();
    when(servicio.filtrarHechosColeccion(coleccion, filtroIdentidad, gestor)).thenReturn(List.of(hecho));

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

    FuenteDinamica fuenteDinamica = mock(FuenteDinamica.class);
    Hecho hecho = usuario.crearHecho(
        "TituloHecho",
        "DescripcionHecho",
        "CategoriaHecho",
        "Direccion",
        null,
        java.time.LocalDateTime.now(),
        List.of("etiqueta1"),
        fuenteDinamica
    );
    assertNotNull(hecho);
    verify(fuenteDinamica).agregarHecho(hecho);

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
        "Titulo",
        "Descripcion",
        "Categoria",
        "Direccion",
        null,
        LocalDateTime.now(),
        List.of("etiqueta1"),
        fuente
    );

    assertNotNull(hecho);
    verify(fuente).agregarHecho(hecho);
  }

  @Test
  void noContribuyenteNoPuedeCrearHecho() {
    Usuario usuario = new Usuario("NoContribuyente", "nc@mail.com", Set.of(Rol.VISUALIZADOR));
    FuenteDinamica fuente = mock(FuenteDinamica.class);

    assertThrows(
        RuntimeException.class,
        () -> usuario.crearHecho(
            "Titulo",
            "Descripcion",
            "Categoria",
            "Direccion",
            null,
            LocalDateTime.now(),
            List.of("etiqueta1"),
            fuente
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

    assertThrows(RuntimeException.class, () -> usuario.crearColeccion("Titulo", "Descripcion", "Categoria", fuente));
  }

  @Test
  void administradorPuedeGestionarSolicitudesCorrectamente() {
    Usuario usuario = new Usuario("Admin", "admin@mail.com", Set.of(Rol.ADMINISTRADOR));
    GestorDeReportes gestorMock = mock(GestorDeReportes.class);
    Solicitud solicitudMock = mock(Solicitud.class);

    when(gestorMock.obtenerSolicitud()).thenReturn(solicitudMock);

    Solicitud solicitud = usuario.obtenerSolicitud(gestorMock);
    assertNotNull(solicitud);
    verify(gestorMock).obtenerSolicitud();

    usuario.gestionarSolicitud(solicitud, true, gestorMock);
    verify(gestorMock).gestionarSolicitud(solicitud, true);
  }

  @Test
  void contribuyenteNoPuedeGestionarSolicitudes() {
    Usuario usuario = new Usuario("Contribuyente", "contribuyente@mail.com", Set.of(Rol.CONTRIBUYENTE));
    GestorDeReportes gestorMock = mock(GestorDeReportes.class);
    Solicitud solicitudMock = mock(Solicitud.class);

    assertThrows(RuntimeException.class, () -> usuario.gestionarSolicitud(solicitudMock, true, gestorMock));
  }

  @Test
  void visualizadorNoPuedeCrearNiGestionarPeroPuedeFiltrar() {
    Usuario usuario = new Usuario("Visualizador", "visualizador@mail.com", Set.of(Rol.VISUALIZADOR));
    Filtro filtroMock = mock(Filtro.class);
    Hecho hechoMock = mock(Hecho.class);

    when(servicio.filtrarHechosColeccion(coleccion, filtroMock, gestor)).thenReturn(List.of(hechoMock));

    List<Hecho> hechosFiltrados = usuario.filtrarHechos(coleccion, filtroMock, gestor, servicio);
    assertEquals(1, hechosFiltrados.size());
    assertEquals(hechoMock, hechosFiltrados.get(0));

    assertThrows(
        RuntimeException.class,
        () -> usuario.crearColeccion("Titulo", "Descripcion", "Categoria", mock(Fuente.class))
    );

    assertThrows(RuntimeException.class, () -> usuario.gestionarSolicitud(mock(Solicitud.class), true, gestor));
  }


}

