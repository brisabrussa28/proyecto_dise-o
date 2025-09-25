package ar.edu.utn.frba.dds;

import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import ar.edu.utn.frba.dds.domain.hecho.Origen;
import ar.edu.utn.frba.dds.domain.info.PuntoGeografico;
import ar.edu.utn.frba.dds.domain.exportador.csv.ExportadorCSV;
import ar.edu.utn.frba.dds.domain.exportador.csv.modoexportacion.ModoAnexar;
import ar.edu.utn.frba.dds.domain.exportador.csv.modoexportacion.ModoNumerar;
import ar.edu.utn.frba.dds.domain.exportador.csv.modoexportacion.ModoSobrescribir;
import ar.edu.utn.frba.dds.domain.exportador.csv.modoexportacion.ModoTimestamp;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

@DisplayName("Tests sobre el exportador de CSVs")
public class ExportadorCsvTest {

  private Hecho hechoSimple;
  private Hecho hechoCompleto1;
  private Hecho hechoCompleto2;

  // Crea una carpeta temporal para cada prueba y la elimina al final.
  @TempDir
  Path tempDir;

  @BeforeEach
  void setUp() {
    hechoSimple = new Hecho();

    LocalDateTime fechaSuceso = LocalDateTime.now().minusDays(1);
    LocalDateTime fechaCarga = LocalDateTime.now();

    hechoCompleto1 = new Hecho(
        "Prueba hecho 1", "este test es una prueba", "Prueba",
        "direcciondeprueba", "Buenos Aires", new PuntoGeografico(1234456, -1234456),
        fechaSuceso, fechaCarga, Origen.PROVISTO_CONTRIBUYENTE, Collections.emptyList()
    );

    hechoCompleto2 = new Hecho(
        "Prueba hecho 2", "este test es otra prueba", "Prueba",
        "direcciondeprueba", "Ciudad Autonoma de Buenos Aires", new PuntoGeografico(1234456, -1234456),
        fechaSuceso, fechaCarga, Origen.PROVISTO_CONTRIBUYENTE, Collections.emptyList()
    );
  }

  @Test
  void exportadorExportaConModoAnexar() {
    var exportador = new ExportadorCSV<>(new ModoAnexar());
    Path outputPath = tempDir.resolve("csvDePrueba.csv");
    Assertions.assertDoesNotThrow(
        () -> exportador.exportar(
            new ArrayList<>(List.of(hechoSimple)),
            outputPath.toString()
        )
    );
  }

  @Test
  void siNoPuedeExportarLanzaExcepcion() {
    var exportador = new ExportadorCSV<>(new ModoAnexar());
    //Pruebo con un path no válido.
    Assertions.assertThrows(
        RuntimeException.class,
        () -> exportador.exportar(
            new ArrayList<>(List.of(hechoSimple)),
            ""
        )
    );
  }

  @Test
  void exportoUsandoElModoNumerar() {
    var exportador = new ExportadorCSV<>(new ModoNumerar());
    Path outputPath = tempDir.resolve("csvDePruebaNumerar.csv");
    Assertions.assertDoesNotThrow(
        () -> exportador.exportar(
            new ArrayList<>(List.of(hechoSimple)),
            outputPath.toString()
        )
    );
  }


  @Test
  void exportoUsandoModoSobrescribir() {
    var exportador = new ExportadorCSV<>(new ModoSobrescribir());
    Path outputPath = tempDir.resolve("csvDePruebaSobrescribir.csv");

    exportador.exportar(
        new ArrayList<>(List.of(hechoCompleto1)),
        outputPath.toString()
    );
    String contenido = null;
    try {
      contenido = Files.readString(outputPath);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    Logger.getLogger(ExportadorCSV.class.getName())
          .info(contenido);
    Assertions.assertTrue(contenido.contains("Prueba hecho 1"));


    exportador.exportar(
        new ArrayList<>(List.of(hechoCompleto2)),
        outputPath.toString()
    );
    contenido = null;
    try {
      contenido = Files.readString(outputPath);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    Logger.getLogger(ExportadorCSV.class.getName())
          .info(contenido);
    Assertions.assertTrue(contenido.contains("Prueba hecho 2"));
    Assertions.assertFalse(contenido.contains("Prueba hecho 1"));
  }

  @Test
  void exportoEnModoTimestamp() {
    var exportador = new ExportadorCSV<>(new ModoTimestamp());
    Path outputPath = tempDir.resolve("csvDePruebaTimestamp.csv");
    Assertions.assertDoesNotThrow(() -> exportador.exportar(
        new ArrayList<>(List.of(hechoSimple)),
        outputPath.toString()
    ));
  }
}

