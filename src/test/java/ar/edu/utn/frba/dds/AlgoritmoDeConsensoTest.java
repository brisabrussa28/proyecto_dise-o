package ar.edu.utn.frba.dds;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ar.edu.utn.frba.dds.domain.coleccion.Coleccion;
import ar.edu.utn.frba.dds.domain.coleccion.algoritmosconsenso.Absoluta;
import ar.edu.utn.frba.dds.domain.coleccion.algoritmosconsenso.AlgoritmoDeConsenso;
import ar.edu.utn.frba.dds.domain.coleccion.algoritmosconsenso.MayoriaSimple;
import ar.edu.utn.frba.dds.domain.coleccion.algoritmosconsenso.MultiplesMenciones;
import ar.edu.utn.frba.dds.domain.exportador.Exportador;
import ar.edu.utn.frba.dds.domain.exportador.configuracion.ConfiguracionExportador;
import ar.edu.utn.frba.dds.domain.filtro.Filtro;
import ar.edu.utn.frba.dds.domain.fuentes.Fuente;
import ar.edu.utn.frba.dds.domain.fuentes.FuenteDeAgregacion;
import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import ar.edu.utn.frba.dds.domain.hecho.HechoBuilder;
import ar.edu.utn.frba.dds.domain.lector.Lector;
import ar.edu.utn.frba.dds.domain.lector.configuracion.ConfiguracionLector;
import ar.edu.utn.frba.dds.domain.reportes.RepositorioDeSolicitudes;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class AlgoritmoDeConsensoTest {
    private RepositorioDeSolicitudes repo;
    private Filtro filtroExcluyente;
    private AlgoritmoDeConsenso absoluta;
    private AlgoritmoDeConsenso mayoriaSimple;
    private AlgoritmoDeConsenso multiplesMenciones;
    private static final LocalDateTime fecha = LocalDateTime.of(2023, 1, 1, 0, 0);


    @BeforeEach
    public void setUp() {
        repo = mock(RepositorioDeSolicitudes.class);
        filtroExcluyente = mock(Filtro.class);
        when(filtroExcluyente.filtrar(anyList()))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(repo.filtroExcluyente()).thenReturn(filtroExcluyente);

        absoluta = new Absoluta();
        mayoriaSimple = new MayoriaSimple();
        multiplesMenciones = new MultiplesMenciones();
    }

    private Hecho crearHecho(String titulo) {
        return new HechoBuilder()
                .conTitulo(titulo)
                .conFechaSuceso(fecha)
                .build();
    }

    private FuenteDeAgregacion crearAgregadorConFuentes(List<Hecho>... listasDeHechos) {
        // 1. Mockear las entidades de CONFIGURACIÓN y de LÓGICA
        ConfiguracionLector configLectorMock = mock(ConfiguracionLector.class);
        ConfiguracionExportador configExportadorMock = mock(ConfiguracionExportador.class);
        Lector<Hecho> lectorMock = mock(Lector.class);
        Exportador<Hecho> exportadorMock = mock(Exportador.class); // <-- CORRECCIÓN: Mockear el Exportador

        // 2. Programar los mocks de configuración para que CONSTRUYAN los mocks de lógica
        doReturn(lectorMock).when(configLectorMock).build(Hecho.class);
        doReturn(exportadorMock).when(configExportadorMock).build(); // <-- CORRECCIÓN: Programar el mock

        when(lectorMock.importar(anyString())).thenReturn(new ArrayList<>());

        // 3. Usar el nuevo constructor con las entidades de configuración
        FuenteDeAgregacion agregador = new FuenteDeAgregacion(
                "AgregadorDeTest", "fake-path.json", configLectorMock, configExportadorMock
        );

        for (List<Hecho> lista : listasDeHechos) {
            Fuente f = mock(Fuente.class);
            when(f.obtenerHechos()).thenReturn(lista);
            agregador.agregarFuente(f);
        }
        return agregador;
    }

    @Test
    public void AlgoritmoAbsolutaHechosConsensuados() {
        Hecho h1 = crearHecho("H1");
        Hecho h2 = crearHecho("H2");
        FuenteDeAgregacion agregador = crearAgregadorConFuentes(
                List.of(h1, h2),
                List.of(h1, h2)
        );
        agregador.forzarActualizacionSincrona();

    Coleccion coleccion = new Coleccion(
        "AbsolutaOk",
        agregador,
        "Desc",
        "Categoria");
    coleccion.setAlgoritmoDeCoscenso(absoluta);
    coleccion.recalcularHechosConsensuados(repo);
    List<Hecho> result = coleccion.getHechosConsensuados();
    assertEquals(2, result.size());
    assertTrue(result.contains(h1));
    assertTrue(result.contains(h2));
  }

    @Test
    public void AlgoritmoAbsolutaHechosNoConsensuados() {
        Hecho h1 = crearHecho("H1");
        Hecho h2 = crearHecho("H2");
        FuenteDeAgregacion agregador = crearAgregadorConFuentes(
                List.of(h1, h2),
                List.of(h1) // h2 falta en esta fuente
        );
        agregador.forzarActualizacionSincrona();

    Coleccion coleccion = new Coleccion(
        "AbsolutaNoOk",
        agregador,
        "Desc",
        "Categoria");
    coleccion.setAlgoritmoDeCoscenso(absoluta);
    coleccion.recalcularHechosConsensuados(repo);
    List<Hecho> result = coleccion.getHechosConsensuados();
    assertEquals(1, result.size());
    assertTrue(result.contains(h1));
    assertFalse(result.contains(h2));
  }

    @Test
    public void AlgoritmoMayoriaSimpleConsensuado() {
        Hecho h1 = crearHecho("H1");
        FuenteDeAgregacion agregador = crearAgregadorConFuentes(
                List.of(h1), // Aparece en 2 de 3 fuentes
                List.of(h1),
                List.of()
        );
        agregador.forzarActualizacionSincrona();

    Coleccion coleccion = new Coleccion(
        "MayoriaOk",
        agregador,
        "Desc",
        "Categoria");
    coleccion.setAlgoritmoDeCoscenso(mayoriaSimple);
    coleccion.recalcularHechosConsensuados(repo);
    List<Hecho> result = coleccion.getHechosConsensuados();
    assertEquals(1, result.size());
    assertTrue(result.contains(h1));
  }

    @Test
    public void AlgoritmoMayoriaSimpleNoConsensuado() {
        Hecho h1 = crearHecho("H1");
        FuenteDeAgregacion agregador = crearAgregadorConFuentes(
                List.of(h1), // Aparece en 1 de 3 fuentes, no es mayoría
                List.of(),
                List.of()
        );
        agregador.forzarActualizacionSincrona();

    Coleccion coleccion = new Coleccion(
        "MayoriaNoOk",
        agregador,
        "Desc",
        "Categoria");
    coleccion.setAlgoritmoDeCoscenso(mayoriaSimple);
    coleccion.recalcularHechosConsensuados(repo);
    List<Hecho> result = coleccion.getHechosConsensuados();
    assertEquals(0, result.size());
  }

    @Test
    public void testMultiplesMencionesConsensuado() {
        Hecho h1 = crearHecho("H1");
        FuenteDeAgregacion agregador = crearAgregadorConFuentes(
                List.of(h1), // Aparece en 2 de 3 fuentes
                List.of(h1),
                List.of()
        );
        agregador.forzarActualizacionSincrona();

    Coleccion coleccion = new Coleccion(
        "MultiplesOk",
        agregador,
        "Desc",
        "Categoria"
    );
    coleccion.setAlgoritmoDeCoscenso(multiplesMenciones);
    coleccion.recalcularHechosConsensuados(repo);
    List<Hecho> result = coleccion.getHechosConsensuados();
    assertEquals(1, result.size());
    assertTrue(result.contains(h1));
  }

    @Test
    public void testMultiplesMencionesNoConsensuado() {
        Hecho h1 = crearHecho("H1");
        FuenteDeAgregacion agregador = crearAgregadorConFuentes(
                List.of(h1), // Aparece solo en 1 de 2 fuentes
                List.of()
        );
        agregador.forzarActualizacionSincrona();

    Coleccion coleccion = new Coleccion(
        "MultiplesNoOk",
        agregador,
        "Desc",
        "Categoria"
    );
    coleccion.setAlgoritmoDeCoscenso(multiplesMenciones);
    coleccion.recalcularHechosConsensuados(repo);
    List<Hecho> result = coleccion.getHechosConsensuados();

        // El hecho está en solo una fuente -> no hay consenso de "múltiples menciones"
        assertEquals(0, result.size());
    }
}
