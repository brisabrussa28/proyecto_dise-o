package ar.edu.utn.frba.dds;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ar.edu.utn.frba.dds.domain.fuentes.FuenteMetaMapa;
import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import ar.edu.utn.frba.dds.domain.hecho.HechoQuerys;
import ar.edu.utn.frba.dds.domain.hecho.ListadoDeHechos;
import ar.edu.utn.frba.dds.domain.info.PuntoGeografico;
import ar.edu.utn.frba.dds.domain.origen.Origen;
import ar.edu.utn.frba.dds.domain.serviciometamapa.ServicioMetaMapa;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class FuenteMetaMapaTest {

  private ServicioMetaMapa servicioMock;
  private HechoQuerys queryMock;
  private ListadoDeHechos listadoMock;
  private FuenteMetaMapa fuenteMetaMapa;

  @BeforeEach
  public void setUp() {
    servicioMock = mock(ServicioMetaMapa.class);
    queryMock = mock(HechoQuerys.class);
    listadoMock = mock(ListadoDeHechos.class);
    fuenteMetaMapa = new FuenteMetaMapa("MetaMapa Fuente", servicioMock, queryMock);
  }

  @Test
  public void devuelveHechosDesdeServicioMetaMapa() throws IOException {
    PuntoGeografico ubicacion = new PuntoGeografico(1.0, 2.0);
    Origen origen = Origen.DATASET; // ✅ uso real en lugar de mock

    Hecho hecho1 = new Hecho(
        "Inundación grave",
        "Zona anegada tras lluvias",
        "inundacion",
        "Calle Falsa 123",
        ubicacion,
        LocalDateTime.now()
                     .minusDays(1),
        LocalDateTime.now(),
        origen,
        List.of("clima", "urgente")
    );

    Hecho hecho2 = new Hecho(
        "Terremoto leve",
        "Temblor sentido en el centro",
        "terremoto",
        "Avenida Siempre Viva",
        ubicacion,
        LocalDateTime.now()
                     .minusDays(2),
        LocalDateTime.now(),
        origen,
        List.of("movimiento", "alerta")
    );


    when(servicioMock.listadoDeHechos(queryMock)).thenReturn(listadoMock);
    when(listadoMock.getHechos()).thenReturn(List.of(hecho1, hecho2));

    List<Hecho> hechos = fuenteMetaMapa.obtenerHechos();

    assertEquals(2, hechos.size());
    assertTrue(hechos.contains(hecho1));
    assertTrue(hechos.contains(hecho2));
  }

  @Test
  public void devuelveListaVaciaSiServicioLanzaIOException() throws IOException {
    when(servicioMock.listadoDeHechos(queryMock)).thenThrow(new IOException("Simulando error"));

    List<Hecho> hechos = fuenteMetaMapa.obtenerHechos();

    assertNotNull(hechos);
    assertTrue(hechos.isEmpty());
  }
}
