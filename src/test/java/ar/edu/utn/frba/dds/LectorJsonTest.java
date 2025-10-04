package ar.edu.utn.frba.dds;

import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import ar.edu.utn.frba.dds.domain.hecho.Origen;
import ar.edu.utn.frba.dds.domain.info.PuntoGeografico;
import ar.edu.utn.frba.dds.domain.lector.json.LectorJson;
import com.fasterxml.jackson.core.type.TypeReference;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class LectorJsonTest {
  Logger logger = Logger.getLogger(LectorJson.class.getName());


  @Test
  void elLectorDeserializaUnHechoCorrectamente() {
    var lector = new LectorJson<>(new TypeReference<List<Hecho>>() {
    });
    logger.info(String.valueOf(LocalDateTime.now()));
    var hechoImportado = lector.importar("src/test/resources/hechoPrueba.json");

    logger.info(String.valueOf(hechoImportado.get(0)));
    var hecho = hechoImportado.get(0);
    Assertions.assertEquals("Prueba", hecho.getTitulo());
    Assertions.assertEquals("Esto es una prueba para ver si importa bien los json", hecho.getDescripcion());
    Assertions.assertEquals("Prueba", hecho.getCategoria());
    Assertions.assertEquals("Prueba 123", hecho.getDireccion());
    Assertions.assertEquals("CABA", hecho.getProvincia());
    Assertions.assertEquals(12.3456, hecho.getUbicacion().getLatitud());
    Assertions.assertEquals(-12.3456, hecho.getUbicacion().getLongitud());
    Assertions.assertEquals(LocalDate.parse("2025-09-17"), hecho.getFechasuceso().toLocalDate());
    Assertions.assertEquals(LocalDate.parse("2025-09-17"), hecho.getFechacarga().toLocalDate());
    Assertions.assertEquals(Origen.PROVISTO_CONTRIBUYENTE, hecho.getOrigen());
    Assertions.assertEquals(new ArrayList<>(), hecho.getEtiquetas());
  }
}
