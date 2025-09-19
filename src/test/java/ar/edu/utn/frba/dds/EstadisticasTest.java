package ar.edu.utn.frba.dds;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ar.edu.utn.frba.dds.domain.algoritmosconsenso.Absoluta;
import ar.edu.utn.frba.dds.domain.algoritmosconsenso.AlgoritmoDeConsenso;
import ar.edu.utn.frba.dds.domain.centralDeEstadisicas.CentralDeEstadisticas;
import ar.edu.utn.frba.dds.domain.coleccion.Coleccion;
import ar.edu.utn.frba.dds.domain.detectorspam.DetectorSpam;
import ar.edu.utn.frba.dds.domain.fuentes.FuenteDinamica;
import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import ar.edu.utn.frba.dds.domain.hecho.HechoBuilder;
import ar.edu.utn.frba.dds.domain.info.PuntoGeografico;
import ar.edu.utn.frba.dds.domain.origen.Origen;
import ar.edu.utn.frba.dds.domain.reportes.RepositorioDeSolicitudes;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class EstadisticasTest {
  CentralDeEstadisticas calculadora = new CentralDeEstadisticas();
  PuntoGeografico pg = new PuntoGeografico(33.0, 44.0);
  LocalDateTime hora = LocalDateTime.now();
  List<String> etiquetas = List.of("#robo");
  Hecho hecho = new HechoBuilder()
      .conTitulo("titulo")
      .conDescripcion("desc")
      .conCategoria("Robos")
      .conDireccion("direccion")
      .conProvincia("Provincia")
      .conUbicacion(pg)
      .conFechaSuceso(hora)
      .conFechaCarga(hora)
      .conFuenteOrigen(Origen.PROVISTO_CONTRIBUYENTE)
      .conEtiquetas(etiquetas)
      .build();
  private RepositorioDeSolicitudes repo;
  private UUID solicitante;
  private DetectorSpam detector = new DetectorSpam() {
    @Override
    public boolean esSpam(String texto) {
      return texto.contains("Troll");
    }
  };
  PuntoGeografico pgAux = new PuntoGeografico(33.39627891281455, 44.48695991794239);
  // FuenteDinamica se inicializará en setUp para asegurar un estado limpio en cada test
  FuenteDinamica fuenteAuxD;
  LocalDateTime horaAux = LocalDateTime.now().minusDays(1);
  List<String> etiquetasAux = List.of(
      "#ancianita",
      "#robo_a_mano_armada",
      "#violencia",
      "#leyDeProtecciónALasAncianitas",
      "#NOalaVIOLENCIAcontraABUELITAS"
  );
  private RepositorioDeSolicitudes repositorio;
  private DetectorSpam detectorSpam;
  private AlgoritmoDeConsenso absoluta;
  private Path tempJsonFile; // Declarar la variable para el archivo temporal
  Coleccion coleccion = new Coleccion("Robos", fuenteAuxD, "Descripcion", "Robos");
  List<Coleccion> colecciones = new ArrayList<>();
  Hecho hecho2 = new HechoBuilder()
      .conTitulo("titulo")
      .conDescripcion("desc")
      .conCategoria("Robos")
      .conDireccion("direccion")
      .conProvincia("Provincia")
      .conUbicacion(null)
      .conFechaSuceso(horaAux)
      .conFechaCarga(LocalDateTime.now()) // Se añade fechaCarga
      .conFuenteOrigen(Origen.DATASET)
      .conEtiquetas(etiquetasAux)
      .build();

  @BeforeEach
  public void setUp() throws IOException {
    repo = new RepositorioDeSolicitudes(detector);
    solicitante = UUID.randomUUID();
    detectorSpam = mock(DetectorSpam.class);
    repositorio = new RepositorioDeSolicitudes(detectorSpam);
    absoluta = new Absoluta();
    // Initialize FuenteDinamica with a temporary JSON file path
    tempJsonFile = Files.createTempFile("test_fuente_dinamica_", ".json"); // Crear archivo temporal
    fuenteAuxD = new FuenteDinamica("Julio Cesar", tempJsonFile.toString());
    fuenteAuxD.agregarHecho(hecho2);
  }

  @Test
  public void estadisticasSpam() {

    repo.agregarSolicitud(solicitante, hecho, "motivo1".repeat(100));
    repo.agregarSolicitud(solicitante, hecho, "motivo2".repeat(100));
    repo.agregarSolicitud(solicitante, hecho, "motivo3".repeat(100));
    repo.agregarSolicitud(solicitante, hecho, "motivo4".repeat(100));

    repo.agregarSolicitud(solicitante, hecho, "Troll".repeat(100));

    calculadora.setRepo(repo);
    assertEquals(1, detector.cantidadDetectada());
    assertEquals(20, calculadora.porcentajeDeSolicitudesSpam());
  }

  @Test
  public void estadisticasDeProvinciaConMasHechos() {
    assertEquals("", calculadora.provinciaConMasHechos(coleccion));
  }

  @Test
  public void estadisticasCategoriaConMasHechos() {
    assertEquals("", calculadora.categoriaConMasHechos(colecciones));
  }

  @Test
  public void estadisticasHechosDeCiertaCategoria() {
    assertEquals("", calculadora.provinciaConMasHechosDeCiertaCategoria(colecciones, "Robos"));
  }

  @Test
  public void estadisticasHoraConMasHechosDeCiertaCategoria() {
    assertEquals("", calculadora.horaConMasHechosDeCiertaCategoria(colecciones, "Robos"));
  }
}
