package ar.edu.utn.frba.dds;

import static ar.edu.utn.frba.dds.domain.filtro.condiciones.Operador.IGUAL;
import static org.junit.jupiter.api.Assertions.assertEquals;

import ar.edu.utn.frba.dds.domain.filtro.Filtro;
import ar.edu.utn.frba.dds.domain.filtro.condiciones.Condicion;
import ar.edu.utn.frba.dds.domain.filtro.condiciones.CondicionGenerica;
import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import ar.edu.utn.frba.dds.domain.hecho.HechoBuilder;
import ar.edu.utn.frba.dds.domain.hecho.Origen;
import ar.edu.utn.frba.dds.domain.info.PuntoGeografico;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;

public class FiltroTest {

  private Hecho hechoDePrueba;
  private List<Hecho> listaDeHechos;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);

    hechoDePrueba = new HechoBuilder()
        .conTitulo("titulo")
        .conDescripcion("Un día más siendo del conurbano")
        .conCategoria("Robos")
        .conDireccion("dire")
        .conProvincia("Buenos Aires")
        .conUbicacion(new PuntoGeografico(33.39, 44.48))
        .conFechaSuceso(LocalDateTime.now().minusDays(1))
        .conFechaCarga(LocalDateTime.now())
        .conFuenteOrigen(Origen.PROVISTO_CONTRIBUYENTE)
        .build();

    listaDeHechos = List.of(hechoDePrueba);
  }

  @Test
  @DisplayName("Un filtro con una condición simple filtra correctamente")
  public void filtroConCondicionSimple() {
    Condicion condicionCategoria = new CondicionGenerica("categoria", IGUAL, "Robos");
    Filtro filtro = new Filtro(condicionCategoria);

    List<Hecho> resultado = filtro.filtrar(listaDeHechos);

    assertEquals(1, resultado.size());
    assertEquals("Robos", resultado.get(0).getHecho_categoria());
  }

  @Test
  @DisplayName("Un filtro sin condición raíz devuelve la lista original")
  public void filtroSinCondicionDevuelveOriginal() {
    Filtro filtro = new Filtro(null);
    List<Hecho> resultado = filtro.filtrar(listaDeHechos);
    assertEquals(1, resultado.size());
    assertEquals(listaDeHechos, resultado);
  }

  @Test
  @DisplayName("Un filtro con una condición que no matchea devuelve una lista vacía")
  public void filtroConCondicionSinMatch() {
    Condicion condicionFallida = new CondicionGenerica("categoria", IGUAL, "Hurtos");
    Filtro filtro = new Filtro(condicionFallida);
    List<Hecho> resultado = filtro.filtrar(listaDeHechos);
    assertEquals(0, resultado.size());
  }

  @Test
  @DisplayName("Filtrar una lista vacía devuelve una lista vacía")
  public void filtrarListaVacia() {
    Filtro filtro = new Filtro(new CondicionGenerica("categoria", IGUAL, "Robos"));
    List<Hecho> resultado = filtro.filtrar(Collections.emptyList());
    assertEquals(0, resultado.size());
  }
}