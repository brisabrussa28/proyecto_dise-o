package ar.edu.utn.frba.dds;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ar.edu.utn.frba.dds.domain.fuentes.FuenteMetaMapa;
import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import ar.edu.utn.frba.dds.domain.hecho.HechoBuilder;
import ar.edu.utn.frba.dds.domain.hecho.HechoQuerys;
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
  private FuenteMetaMapa fuenteMetaMapa;

  @BeforeEach
  public void setUp() {
    servicioMock = mock(ServicioMetaMapa.class);
    queryMock = mock(HechoQuerys.class);
    fuenteMetaMapa = new FuenteMetaMapa("MetaMapa Fuente", servicioMock, queryMock);
  }

  @Test
  public void devuelveHechosDesdeServicioMetaMapa() throws IOException {
    PuntoGeografico ubicacion = new PuntoGeografico(1.0, 2.0);
    Origen origen = Origen.DATASET;

    Hecho hecho1 = new HechoBuilder()
        .conTitulo("Inundación grave")
        .conDescripcion("Zona anegada tras lluvias")
        .conCategoria("inundacion")
        .conDireccion("Calle Falsa 123")
        .conProvincia("Buenos Aires") // Sume provincia
        .conUbicacion(ubicacion)
        .conFechaSuceso(LocalDateTime.now().minusDays(1))
        .conFechaCarga(LocalDateTime.now())
        .conFuenteOrigen(origen)
        .conEtiquetas(List.of("clima", "urgente"))
        .build();

    Hecho hecho2 = new HechoBuilder()
        .conTitulo("Terremoto leve")
        .conDescripcion("Temblor sentido en el centro")
        .conCategoria("terremoto")
        .conDireccion("Avenida Siempre Viva")
        .conProvincia("Mendoza") // Sume provincia
        .conUbicacion(ubicacion)
        .conFechaSuceso(LocalDateTime.now().minusDays(2))
        .conFechaCarga(LocalDateTime.now())
        .conFuenteOrigen(origen)
        .conEtiquetas(List.of("movimiento", "alerta"))
        .build();

    when(servicioMock.listadoDeHechos(queryMock)).thenReturn(List.of(hecho1, hecho2));

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