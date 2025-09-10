package ar.edu.utn.frba.dds;

import ar.edu.utn.frba.dds.domain.fuentes.apis.AdapterMetaMapa;
import ar.edu.utn.frba.dds.domain.fuentes.apis.serviciometamapa.HechoQuerys;
import ar.edu.utn.frba.dds.domain.fuentes.apis.serviciometamapa.ServicioMetaMapa;
import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import ar.edu.utn.frba.dds.domain.hecho.HechoBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AdapterMetaMapaTest {

  @Mock
  private ServicioMetaMapa servicioMetaMapaMock;
  @Mock
  private HechoQuerys queryMock;

  private AdapterMetaMapa adapter;

  @BeforeEach
  void setUp() {
    adapter = new AdapterMetaMapa(servicioMetaMapaMock, queryMock);
  }

  @Test
  @DisplayName("Devuelve correctamente la lista de hechos del ServicioMetaMapa")
  void devuelveHechosDelServicio() throws IOException {
    Hecho hecho1 = new HechoBuilder().conTitulo("Hecho 1").conCategoria("Cat1").conFechaSuceso(LocalDateTime.now().minusDays(1)).build();
    Hecho hecho2 = new HechoBuilder().conTitulo("Hecho 2").conCategoria("Cat2").conFechaSuceso(LocalDateTime.now().minusDays(1)).build();
    List<Hecho> listaEsperada = List.of(hecho1, hecho2);

    when(servicioMetaMapaMock.listadoDeHechos(queryMock)).thenReturn(listaEsperada);

    // Act
    List<Hecho> resultado = adapter.consultarHechos();

    // Assert
    assertEquals(2, resultado.size());
    assertEquals(listaEsperada, resultado);

    // Verificamos que el adaptador efectivamente llamó al método correcto de su dependencia
    verify(servicioMetaMapaMock).listadoDeHechos(queryMock);
  }

  @Test
  @DisplayName("Devuelve lista vacía si el servicio no retorna hechos")
  void devuelveListaVaciaSiServicioNoRetornaNada() throws IOException {
    // Arrange
    when(servicioMetaMapaMock.listadoDeHechos(queryMock)).thenReturn(Collections.emptyList());

    // Act
    List<Hecho> resultado = adapter.consultarHechos();

    // Assert
    assertTrue(resultado.isEmpty());
  }

  @Test
  @DisplayName("Propaga la IOException si el servicio falla")
  void propagaExcepcionDelServicio() throws IOException {
    // Arrange
    when(servicioMetaMapaMock.listadoDeHechos(queryMock)).thenThrow(new IOException("Fallo de red simulado"));

    // Act & Assert
    // Verificamos que el adaptador lanza la misma excepción que recibió del servicio
    assertThrows(IOException.class, () -> {
      adapter.consultarHechos();
    });
  }
}
