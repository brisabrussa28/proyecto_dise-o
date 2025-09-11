package ar.edu.utn.frba.dds;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import ar.edu.utn.frba.dds.domain.filtro.Filtro;
import ar.edu.utn.frba.dds.domain.filtro.condiciones.condicion.Condicion;
import ar.edu.utn.frba.dds.domain.filtro.condiciones.condicion.CondicionGenerica;
import ar.edu.utn.frba.dds.domain.hecho.CampoHecho;
import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import ar.edu.utn.frba.dds.domain.serializadores.csv.Lector.FilaConverter.HechoFilaConverter;
import ar.edu.utn.frba.dds.domain.serializadores.csv.Lector.LectorCSV;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class LectorCsvTest {
  private final String dir = "src/test/java/ar/edu/utn/frba/dds/CsvDePrueba/";

  @TempDir
  Path tempDir; // Directorio temporal gestionado por JUnit

  private LectorCSV<Hecho> crearLector(String dateFormat, Map<CampoHecho, List<String>> mapeo) {
    HechoFilaConverter converter = new HechoFilaConverter(dateFormat, mapeo);
    return new LectorCSV<>(',', converter);
  }

  @Test
  public void importarCSVformatoCorrecto() {
    Map<CampoHecho, List<String>> mapeoColumnas = Map.of(
        CampoHecho.TITULO, List.of("titulo"),
        CampoHecho.DESCRIPCION, List.of("descripcion"),
        CampoHecho.LATITUD, List.of("latitud"),
        CampoHecho.LONGITUD, List.of("longitud"),
        CampoHecho.FECHA_SUCESO, List.of("fechaSuceso"),
        CampoHecho.CATEGORIA, List.of("categoria"),
        CampoHecho.DIRECCION, List.of("direccion"),
        CampoHecho.PROVINCIA, List.of("provincia")
    );

    LectorCSV<Hecho> lector = crearLector("dd/MM/yyyy", mapeoColumnas);
    List<Hecho> csv = lector.importar(dir + "ejemplo.csv");

    Condicion condicion = new CondicionGenerica("direccion", "IGUAL", "EL NESTORNAUTA");
    Filtro filtroDireccion = new Filtro(condicion);

    List<Hecho> hechosFiltrados = filtroDireccion.filtrar(csv);
    Hecho hecho = hechosFiltrados.get(0);

    assertEquals("buenardo", hecho.getCategoria());
    assertEquals(5, csv.size());
  }

  @Test
  public void importarCSVformatoExtraño() {
    Map<CampoHecho, List<String>> mapeoColumnas = Map.of(
        CampoHecho.TITULO,
        List.of("tipo_persona_id"),
        CampoHecho.DESCRIPCION,
        List.of("tipo_persona", "modo_produccion_hecho_ampliada", "modo_produccion_hecho_otro"),
        CampoHecho.LATITUD,
        List.of("latitud"),
        CampoHecho.LONGITUD,
        List.of("longitud"),
        CampoHecho.FECHA_SUCESO,
        List.of("fecha_hecho"),
        CampoHecho.CATEGORIA,
        List.of("semaforo_estado"),
        CampoHecho.DIRECCION,
        List.of("provincia_nombre", "departamento_nombre", "localidad_nombre", "calle_nombre", "calle_altura"),
        CampoHecho.PROVINCIA,
        List.of("provincia_nombre")
    );

    LectorCSV<Hecho> lector = crearLector("dd-MM-yy", mapeoColumnas);
    List<Hecho> csv = lector.importar(dir + "rarito.csv");
    Hecho hecho = csv.get(0);

    assertEquals("Imputado idRegistro 13483", hecho.getTitulo());
  }

  @Test
  public void testCsvConColumnaInexistente() throws IOException {
    Path tempFile = tempDir.resolve("columnaInexistente.csv");
    try (FileWriter writer = new FileWriter(tempFile.toFile())) {
      writer.write("nombre,apellido,titulo,fechaSuceso,provincia\nJuan,Perez,Mi Titulo,25/12/2024,BsAs\n");
    }

    Map<CampoHecho, List<String>> mapeo = Map.of(
        CampoHecho.TITULO, List.of("titulo"),
        CampoHecho.FECHA_SUCESO, List.of("fechaSuceso"),
        CampoHecho.PROVINCIA, List.of("provincia"),
        CampoHecho.DESCRIPCION, List.of("columna_que_no_existe")
    );

    LectorCSV<Hecho> lector = crearLector("dd/MM/yyyy", mapeo);
    List<Hecho> hechos = lector.importar(tempFile.toString());
    assertEquals(1, hechos.size());
    assertNull(hechos.get(0)
                     .getDescripcion());
  }

  @Test
  public void testCsvConDoubleInvalido() throws IOException {
    Path tempFile = tempDir.resolve("latitudInvalida.csv");
    try (FileWriter writer = new FileWriter(tempFile.toFile())) {
      writer.write("latitud,longitud,titulo,fechaSuceso,provincia\nnot_a_number,-58.3,Evento,25/12/2020,CABA\n");
    }

    Map<CampoHecho, List<String>> mapeo = Map.of(
        CampoHecho.LATITUD, List.of("latitud"),
        CampoHecho.LONGITUD, List.of("longitud"),
        CampoHecho.TITULO, List.of("titulo"),
        CampoHecho.FECHA_SUCESO, List.of("fechaSuceso"),
        CampoHecho.PROVINCIA, List.of("provincia")
    );

    LectorCSV<Hecho> lector = crearLector("dd/MM/yyyy", mapeo);
    List<Hecho> hechos = lector.importar(tempFile.toString());
    assertEquals(1, hechos.size());
    assertNull(hechos.get(0)
                     .getUbicacion());
  }

  @Test
  public void testCsvConEncabezadosDuplicados() throws IOException {
    Path tempFile = tempDir.resolve("encabezadosDuplicados.csv");
    try (FileWriter writer = new FileWriter(tempFile.toFile())) {
      writer.write("titulo,titulo,fechaSuceso,provincia\nEvento duplicado,Repetido,01/01/2020,CABA\n");
    }

    Map<CampoHecho, List<String>> mapeo = Map.of(
        CampoHecho.TITULO, List.of("titulo"),
        CampoHecho.FECHA_SUCESO, List.of("fechaSuceso"),
        CampoHecho.PROVINCIA, List.of("provincia")
    );

    LectorCSV<Hecho> lector = crearLector("dd/MM/yyyy", mapeo);
    assertThrows(IllegalArgumentException.class, () -> lector.importar(tempFile.toString()));
  }

  @Test
  public void testFilaSinTituloNoSeCreaHecho() throws IOException {
    Path tempFile = tempDir.resolve("sinTitulo.csv");
    try (FileWriter writer = new FileWriter(tempFile.toFile())) {
      writer.write("titulo,fechaSuceso,provincia\n,01/01/2020,CABA\n");
    }

    Map<CampoHecho, List<String>> mapeo = Map.of(
        CampoHecho.TITULO, List.of("titulo"),
        CampoHecho.FECHA_SUCESO, List.of("fechaSuceso"),
        CampoHecho.PROVINCIA, List.of("provincia")
    );

    LectorCSV<Hecho> lector = crearLector("dd/MM/yyyy", mapeo);
    List<Hecho> hechos = lector.importar(tempFile.toString());
    assertEquals(0, hechos.size()); // no debería crear el hecho
  }
}

