package ar.edu.utn.frba.dds;

import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import ar.edu.utn.frba.dds.domain.hecho.Origen;
import ar.edu.utn.frba.dds.domain.info.PuntoGeografico;
import ar.edu.utn.frba.dds.domain.serializadores.exportador.csv.ExportadorCSV;
import ar.edu.utn.frba.dds.domain.serializadores.exportador.csv.modoexportacion.ModoAnexar;
import ar.edu.utn.frba.dds.domain.serializadores.exportador.csv.modoexportacion.ModoNumerar;
import ar.edu.utn.frba.dds.domain.serializadores.exportador.csv.modoexportacion.ModoSobrescribir;
import ar.edu.utn.frba.dds.domain.serializadores.exportador.csv.modoexportacion.ModoTimestamp;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.logging.Logger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Tests sobre el exportador de CSVs")
public class ExportadorCsvTest {
  @Test
  void exportadorExportaConModoAnexar() {
    var exportador = new ExportadorCSV<>(new ModoAnexar());
    var hechoDePrueba = new Hecho();
    Assertions.assertDoesNotThrow(
        () -> exportador.exportar(
            List.of(hechoDePrueba),
            "src/test/resources/csvDePrueba.csv"
        )
    );
  }

  @Test
  void siNoPuedeExportarLanzaExcepcion() {
    var exportador = new ExportadorCSV<>(new ModoAnexar());
    var hechoDePrueba = new Hecho();
    //Pruebo con un path no válido.
    Assertions.assertThrows(
        RuntimeException.class,
        () -> exportador.exportar(
            List.of(hechoDePrueba),
            ""
        )
    );
  }

  @Test
  void exportoUsandoElModoNumerar() {
    var exportador = new ExportadorCSV<>(new ModoNumerar());
    var hechoDePrueba = new Hecho();
    Assertions.assertDoesNotThrow(
        () -> exportador.exportar(
            List.of(hechoDePrueba),
            "src/test/resources/csvDePruebaNumerar.csv"
        )
    );
  }

  @Test
  void laConfiguracionDelExportadorSeGuardaBien() {
    var exportador = new ExportadorCSV<>(',', '/', new ModoNumerar());
    var config = exportador.getConfiguracionJson();
    Assertions.assertNotNull(config);
    Logger.getLogger(ExportadorCSV.class.getName())
          .info(config);
  }

  @Test
  void exportoUsandoModoSobrescribir() {
    var exportador = new ExportadorCSV<>(new ModoSobrescribir());
    var hecho1 = new Hecho(
        "Prueba hecho 1",
        "este test es una prueba",
        "Prueba",
        "direcciondeprueba",
        "Buenos Aires",
        new PuntoGeografico(1234456, -1234456),
        LocalDateTime.now(),
        LocalDateTime.now(),
        Origen.PROVISTO_CONTRIBUYENTE,
        List.of()
    );

    var hecho2 = new Hecho(
        "Prueba hecho 2",
        "este test es otra prueba",
        "Prueba",
        "direcciondeprueba",
        "Ciudad Autonoma de Buenos Aires",
        new PuntoGeografico(1234456, -1234456),
        LocalDateTime.now(),
        LocalDateTime.now(),
        Origen.PROVISTO_CONTRIBUYENTE,
        List.of()
    );

    exportador.exportar(
        List.of(hecho1),
        "src/test/resources/csvDePruebaSobrescribir.csv"
    );
    String contenido = null;
    try {
      contenido = java.nio.file.Files.readString(java.nio.file.Path.of(
          "src/test/resources/csvDePruebaSobrescribir.csv"));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    Logger.getLogger(ExportadorCSV.class.getName())
          .info(contenido);
    Assertions.assertTrue(contenido.contains("Prueba hecho 1"));


    exportador.exportar(
        List.of(hecho2),
        "C:\\Users\\Jere\\Desktop\\csvs de prueba\\csvDePruebaSobrescribir.csv"
    );
    contenido = null;
    try {
      contenido = java.nio.file.Files.readString(java.nio.file.Path.of(
          "C:\\Users\\Jere\\Desktop\\csvs de prueba\\csvDePruebaSobrescribir.csv"));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    Logger.getLogger(ExportadorCSV.class.getName())
          .info(contenido);
    Assertions.assertTrue(contenido.contains("Prueba hecho 2"));
  }

  @Test
  void exportoEnModoTimestamp() {
    var exportador = new ExportadorCSV<>(new ModoTimestamp());
    var hechoDePrueba = new Hecho();
    Assertions.assertDoesNotThrow(() -> exportador.exportar(
        List.of(hechoDePrueba),
        "src/test/resources/csvDePruebaTimestamp.csv"
    ));
  }
}
