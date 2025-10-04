package ar.edu.utn.frba.dds;

import ar.edu.utn.frba.dds.domain.fuentes.FuenteExternaAPI;
import ar.edu.utn.frba.dds.domain.fuentes.apis.FuenteAdapter;
import ar.edu.utn.frba.dds.domain.fuentes.apis.configuracion.ConfiguracionAdapter;
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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class FuenteExternaApiTest {

  @Mock
  private FuenteAdapter adaptadorMock;
  @Mock
  private ConfiguracionAdapter configAdapterMock;

  private FuenteExternaAPI fuenteExterna;

  @BeforeEach
  void setUp() {
    // Configurar el mock para que devuelva el adaptador cuando se llame a build()
    when(configAdapterMock.build()).thenReturn(adaptadorMock);

    // Instanciar la fuente con su constructor correcto, alineado al nuevo diseño
    fuenteExterna = new FuenteExternaAPI("FuenteAPITest", configAdapterMock);
  }

  @Test
  @DisplayName("Obtiene y actualiza la copia local cuando el adaptador funciona")
  void obtieneYActualizaCopiaLocalExitosamente() throws IOException { // <-- CORRECCIÓN AQUÍ
    Hecho hecho = new HechoBuilder().conTitulo("Test").conFechaSuceso(LocalDateTime.now().minusWeeks(3)).build();
    List<Hecho> hechosEsperados = List.of(hecho);
    when(adaptadorMock.consultarHechos()).thenReturn(hechosEsperados);

    // Se ejecuta la lógica de actualización
    fuenteExterna.forzarActualizacionSincrona();
    List<Hecho> hechosObtenidos = fuenteExterna.obtenerHechos();

    // Se verifica que la copia local interna (persistida en BD) ahora contiene los hechos de la API
    assertEquals(1, hechosObtenidos.size());
    assertTrue(hechosObtenidos.contains(hecho));
    verify(adaptadorMock, times(1)).consultarHechos(); // Se verifica que se consultó la API
  }

  @Test
  @DisplayName("La copia local no se modifica si el adaptador falla")
  void noActualizaCopiaLocalSiAdaptadorFalla() throws IOException { // <-- CORRECCIÓN AQUÍ
    // Simular que la API lanza una excepción comprobada
    when(adaptadorMock.consultarHechos()).thenThrow(new IOException("Simulando error de red"));

    // La lista inicial está vacía
    assertTrue(fuenteExterna.obtenerHechos().isEmpty());

    // Se intenta actualizar, pero la llamada a la API fallará
    fuenteExterna.forzarActualizacionSincrona();
    List<Hecho> hechosObtenidos = fuenteExterna.obtenerHechos();

    // La copia local debe permanecer vacía, sin cambios.
    assertTrue(hechosObtenidos.isEmpty());
    verify(adaptadorMock, times(1)).consultarHechos(); // Se verifica que se intentó consultar la API
  }

  @Test
  @DisplayName("La copia local no se borra si el adaptador devuelve una lista vacía")
  void noBorraCopiaLocalSiAdaptadorDevuelveVacio() throws IOException { // <-- CORRECCIÓN AQUÍ
    // 1. Primera actualización exitosa con un hecho para poblar la copia local
    Hecho hechoInicial = new HechoBuilder().conTitulo("Hecho Inicial").conFechaSuceso(LocalDateTime.now()).build();
    when(adaptadorMock.consultarHechos()).thenReturn(List.of(hechoInicial));
    fuenteExterna.forzarActualizacionSincrona();
    assertEquals(1, fuenteExterna.obtenerHechos().size());

    // 2. Segunda actualización, la API no devuelve nada
    when(adaptadorMock.consultarHechos()).thenReturn(Collections.emptyList());
    fuenteExterna.forzarActualizacionSincrona();

    // 3. Se verifica que la copia local AÚN contiene el hecho inicial y no fue borrada.
    List<Hecho> hechosObtenidos = fuenteExterna.obtenerHechos();
    assertEquals(1, hechosObtenidos.size());
    assertTrue(hechosObtenidos.contains(hechoInicial));

    // 4. Verificamos que el mock fue llamado dos veces en total durante este test.
    verify(adaptadorMock, times(2)).consultarHechos();
  }
}

