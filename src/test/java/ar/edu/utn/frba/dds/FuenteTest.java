package ar.edu.utn.frba.dds;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import ar.edu.utn.frba.dds.domain.csv.LectorCSV;
import ar.edu.utn.frba.dds.domain.fuentes.Fuente;
import ar.edu.utn.frba.dds.domain.fuentes.FuenteDinamica;
import ar.edu.utn.frba.dds.domain.fuentes.FuenteEstatica;
import ar.edu.utn.frba.dds.domain.fuentes.FuenteDeAgregacion;
import ar.edu.utn.frba.dds.domain.hecho.Hecho;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

public class FuenteTest {

  // Tests FuenteDinamica

  @Test
  public void fuenteDinamicaIniciaConListaVaciaSiNoSeProporciona() {
    FuenteDinamica fuente = new FuenteDinamica("MiFuente", null);
    assertTrue(fuente.obtenerHechos()
                     .isEmpty());
  }

  @Test
  public void fuenteDinamicaAgregaHechoCorrectamente() {
    FuenteDinamica fuente = new FuenteDinamica("Fuente1", null);
    Hecho hechoMock = mock(Hecho.class);
    fuente.agregarHecho(hechoMock);
    assertEquals(
        1,
        fuente.obtenerHechos()
              .size()
    );
    assertEquals(
        hechoMock,
        fuente.obtenerHechos()
              .get(0)
    );
  }

  @Test
  public void obtenerHechosDevuelveListaInmutableDinamica() {
    FuenteDinamica fuente = new FuenteDinamica("FuenteInmutable", null);
    Hecho hecho = mock(Hecho.class);
    fuente.agregarHecho(hecho);
    List<Hecho> hechos = fuente.obtenerHechos();
    assertThrows(UnsupportedOperationException.class, () -> hechos.add(mock(Hecho.class)));
  }

  @Test
  public void fuenteDinamicaPuedeIniciarseConHechos() {
    Hecho hecho = mock(Hecho.class);
    FuenteDinamica fuente = new FuenteDinamica("FuenteConHechos", List.of(hecho));
    assertEquals(
        1,
        fuente.obtenerHechos()
              .size()
    );
    assertTrue(fuente.obtenerHechos()
                     .contains(hecho));
  }

  @Test
  public void agregarHechoNoAgregaNulo() {
    FuenteDinamica fuente = new FuenteDinamica("Fuente", null);
    fuente.agregarHecho(null);
    assertEquals(
        1,
        fuente.obtenerHechos()
              .size()
    );
    assertNull(fuente.obtenerHechos()
                     .get(0));
  }

  // Tests FuenteEstatica

@Test
public void fuenteEstaticaUsaLectorCSVCorrectamente() {
    Hecho hechoMock = mock(Hecho.class);
    LectorCSV lectorMock = mock(LectorCSV.class);

    // Ajusta los argumentos según los cambios en el método importar
    when(lectorMock.importar("ruta.csv")).thenReturn(List.of(hechoMock));

    // Ajusta el constructor de FuenteEstatica si cambió
    FuenteEstatica fuente = new FuenteEstatica("MiFuente", "ruta.csv", lectorMock);

    List<Hecho> hechos = fuente.obtenerHechos();

    assertEquals(1, hechos.size());
    assertEquals(hechoMock, hechos.get(0));
    verify(lectorMock).importar("ruta.csv");
}

@Test
public void fuenteEstaticaDevuelveListaVaciaSiCSVEstaVacio() {
    LectorCSV lectorMock = mock(LectorCSV.class);

    // Ajusta los argumentos según los cambios en el método importar
    when(lectorMock.importar(anyString())).thenReturn(List.of());

    // Ajusta el constructor de FuenteEstatica si cambió
    FuenteEstatica fuente = new FuenteEstatica("Vacia", "vac.csv", lectorMock);

    List<Hecho> hechos = fuente.obtenerHechos();

    assertTrue(hechos.isEmpty());
}
  @Test

  public void fuenteDeAgregacionCombinaHechosDeTodasLasFuentes() {
    Hecho hecho1 = mock(Hecho.class);
    Hecho hecho2 = mock(Hecho.class);
    Fuente fuente1 = mock(Fuente.class);
    Fuente fuente2 = mock(Fuente.class);
    when(fuente1.obtenerHechos()).thenReturn(List.of(hecho1));
    when(fuente2.obtenerHechos()).thenReturn(List.of(hecho2));
    FuenteDeAgregacion agregadora = new FuenteDeAgregacion("Agregada");
    agregadora.agregarFuente(fuente1);
    agregadora.agregarFuente(fuente2);
    List<Hecho> todos = agregadora.obtenerHechos();
    assertEquals(2, todos.size());
    assertTrue(todos.contains(hecho1));
    assertTrue(todos.contains(hecho2));
  }

  @Test
  public void fuenteDeAgregacionConFuentesVacias() {
    Fuente vacia1 = mock(Fuente.class);
    Fuente vacia2 = mock(Fuente.class);
    when(vacia1.obtenerHechos()).thenReturn(List.of());
    when(vacia2.obtenerHechos()).thenReturn(List.of());
    FuenteDeAgregacion agregadora = new FuenteDeAgregacion("AgregadoraVacia");
    agregadora.agregarFuente(vacia1);
    agregadora.agregarFuente(vacia2);
    assertTrue(agregadora.obtenerHechos()
                         .isEmpty());
  }

  @Test
  public void fuenteDeAgregacionAdmiteFuentesDinamicamente() {
    Fuente fuente = mock(Fuente.class);
    when(fuente.obtenerHechos()).thenReturn(List.of(mock(Hecho.class)));
    FuenteDeAgregacion agregadora = new FuenteDeAgregacion("Dinámica");
    agregadora.agregarFuente(fuente);
    assertEquals(
        1,
        agregadora.obtenerHechos()
                  .size()
    );
  }
}