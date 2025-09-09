package ar.edu.utn.frba.dds;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import ar.edu.utn.frba.dds.domain.csv.LectorCSV;
import ar.edu.utn.frba.dds.domain.filtro.FiltroPredicado;
import ar.edu.utn.frba.dds.domain.hecho.CampoHecho;
import ar.edu.utn.frba.dds.domain.hecho.Hecho;
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

    List<Hecho> csv = new LectorCSV(',', "dd/MM/yyyy", mapeoColumnas).importar(
        dir + "ejemplo.csv");
    FiltroPredicado filtroDireccion = new FiltroPredicado(h -> h.getDireccion().equals("EL NESTORNAUTA"));
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

    List<Hecho> csv = new LectorCSV(',', "dd-MM-yy", mapeoColumnas).importar(
        dir + "rarito.csv");
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

    List<Hecho> hechos = new LectorCSV(',', "dd/MM/yyyy", mapeo).importar(tempFile.toString());
    assertEquals(1, hechos.size());
    assertNull(hechos.get(0).getDescripcion());
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

    List<Hecho> hechos = new LectorCSV(',', "dd/MM/yyyy", mapeo).importar(tempFile.toString());
    assertEquals(1, hechos.size());
    assertNull(hechos.get(0).getUbicacion());
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

    assertThrows(
        IllegalArgumentException.class, () -> new LectorCSV(',', "dd/MM/yyyy", mapeo).importar(tempFile.toString())
    );
  }

  @Test
  public void testCsvConEncabezadosDesordenados() throws IOException {
    Path tempFile = tempDir.resolve("encabezadosDesordenados.csv");
    try (FileWriter writer = new FileWriter(tempFile.toFile())) {
      writer.write("latitud,fechaSuceso,titulo,provincia\n-34.6,10/10/2020,Evento Desordenado,CABA\n");
    }

    Map<CampoHecho, List<String>> mapeo = Map.of(
        CampoHecho.TITULO, List.of("titulo"),
        CampoHecho.LATITUD, List.of("latitud"),
        CampoHecho.FECHA_SUCESO, List.of("fechaSuceso"),
        CampoHecho.PROVINCIA, List.of("provincia")
    );

    List<Hecho> hechos = new LectorCSV(',', "dd/MM/yyyy", mapeo).importar(tempFile.toString());
    assertEquals(1, hechos.size());
    assertEquals("Evento Desordenado", hechos.get(0).getTitulo());
  }

  @Test
  public void testCsvConFilaMasLargaQueEncabezado() throws IOException {
    Path tempFile = tempDir.resolve("filaLarga.csv");
    try (FileWriter writer = new FileWriter(tempFile.toFile())) {
      writer.write("titulo,fechaSuceso,provincia\nEvento Largo,01/01/2020,BsAs,Extra\n");
    }

    Map<CampoHecho, List<String>> mapeo = Map.of(
        CampoHecho.TITULO, List.of("titulo"),
        CampoHecho.FECHA_SUCESO, List.of("fechaSuceso"),
        CampoHecho.PROVINCIA, List.of("provincia")
    );

    List<Hecho> hechos = new LectorCSV(',', "dd/MM/yyyy", mapeo).importar(tempFile.toString());
    assertEquals(1, hechos.size());
    assertEquals("Evento Largo", hechos.get(0).getTitulo());
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

    List<Hecho> hechos = new LectorCSV(',', "dd/MM/yyyy", mapeo).importar(tempFile.toString());
    assertEquals(0, hechos.size()); // no debería crear el hecho
  }

  @Test
  public void testCsvConVariosCasosEspeciales() throws IOException {
    Path tempFile = tempDir.resolve("csvUltraRaro.csv");
    try (FileWriter writer = new FileWriter(tempFile.toFile())) {
      writer.write(
          "titulo,descripcion,latitud,longitud,fechaSuceso,categoria,direccion,provincia\n" +
              "\"Evento, extraño\", \"Descripción con , comas y \"\"comillas\"\"\", , ,not-a-date,bizarra,,\n" + // fila 1: muchos errores
              "Evento Normal,Descripcion Normal,-34.6,-58.4,01/01/2021,Categoria Normal,\"Av. Siempre Viva 123\",CABA\n" + // fila 2: válida
              ", , , , , , ,\n" + // fila 3: completamente vacía
              "Evento con pocos campos,,,,01/01/2020,,\n" // fila 4: truncada y sin título
      );
    }

    Map<CampoHecho, List<String>> mapeo = Map.of(
        CampoHecho.TITULO, List.of("titulo"),
        CampoHecho.DESCRIPCION, List.of("descripcion"),
        CampoHecho.LATITUD, List.of("latitud"),
        CampoHecho.LONGITUD, List.of("longitud"),
        CampoHecho.FECHA_SUCESO, List.of("fechaSuceso"),
        CampoHecho.CATEGORIA, List.of("categoria"),
        CampoHecho.DIRECCION, List.of("direccion"),
        CampoHecho.PROVINCIA, List.of("provincia")
    );

    LectorCSV lector = new LectorCSV(',', "dd/MM/yyyy", mapeo);
    List<Hecho> hechos = lector.importar(tempFile.toString());

    // Debería procesar solo la segunda fila (válida)
    assertEquals(2, hechos.size());
    assertEquals("Evento Normal", hechos.get(0).getTitulo());
  }
}

