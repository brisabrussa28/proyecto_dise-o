package ar.edu.utn.frba.dds;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ar.edu.utn.frba.dds.domain.csv.LectorCSV;
import ar.edu.utn.frba.dds.domain.filtro.FiltroDeDireccion;
import ar.edu.utn.frba.dds.domain.hecho.CampoHecho;
import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import ar.edu.utn.frba.dds.main.Usuario;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

public class LectorCsvTest {
  private final String dir = "src/test/java/ar/edu/utn/frba/dds/CsvDePrueba/";
  Usuario iluminati = new Usuario("△", "libellumcipher@incognito.com");
  Usuario admin = new Usuario("pipocapo", "makenipipo@gmail.com");

  @Test
  public void importarCSVformatoCorrecto() {
    Map<CampoHecho, List<String>> mapeoColumnas = Map.of(
        CampoHecho.TITULO, List.of("titulo"),
        CampoHecho.DESCRIPCION, List.of("descripcion"),
        CampoHecho.LATITUD, List.of("latitud"),
        CampoHecho.LONGITUD, List.of("longitud"),
        CampoHecho.FECHA_SUCESO, List.of("fechaSuceso"),
        CampoHecho.CATEGORIA, List.of("categoria"),
        CampoHecho.DIRECCION, List.of("direccion")
    );

    List<Hecho> csv = new LectorCSV(',', "dd/MM/yyyy", mapeoColumnas).importar(
        dir + "ejemplo.csv");
    FiltroDeDireccion filtroDireccion = new FiltroDeDireccion("EL NESTORNAUTA");
    List<Hecho> hechosFiltrados = filtroDireccion.filtrar(csv);
    Hecho hecho = hechosFiltrados.get(0);

    assertEquals("buenardo", hecho.getCategoria());
    assertEquals(5, csv.size());
  }

  @Test
  public void importarCSVformatoExtraño() {
    Map<CampoHecho, List<String>> mapeoColumnas = Map.of(
        CampoHecho.TITULO, List.of("tipo_persona_id"),
        CampoHecho.DESCRIPCION, List.of("tipo_persona", "modo_produccion_hecho_ampliada", "modo_produccion_hecho_otro"),
        CampoHecho.LATITUD, List.of("latitud"),
        CampoHecho.LONGITUD, List.of("longitud"),
        CampoHecho.FECHA_SUCESO, List.of("fecha_hecho"),
        CampoHecho.CATEGORIA, List.of("semaforo_estado"),
        CampoHecho.DIRECCION, List.of("provincia_nombre", "departamento_nombre", "localidad_nombre", "calle_nombre", "calle_altura")
    );

    List<Hecho> csv = new LectorCSV(',', "dd-MM-yy", mapeoColumnas).importar(
        dir + "rarito.csv");
    Hecho hecho = csv.get(0);

    assertEquals("Imputado idRegistro 13483", hecho.getTitulo());
  }

  @Test
  public void testCsvConColumnaInexistente() throws IOException {
    String path = dir + "columnaInexistente.csv";
    try (FileWriter writer = new FileWriter(path)) {
      writer.write("nombre,apellido\nJuan,Perez\n");
    }

    Map<CampoHecho, List<String>> mapeo = Map.of(
        CampoHecho.TITULO, List.of("columna_que_no_existe")
    );

    List<Hecho> hechos = new LectorCSV(',', "dd/MM/yyyy", mapeo).importar(path);
    assertEquals(0, hechos.size());
  }

  @Test
  public void testCsvConDoubleInvalido() throws IOException {
    String path = dir + "latitudInvalida.csv";
    try (FileWriter writer = new FileWriter(path)) {
      writer.write("latitud,longitud,titulo,fechaSuceso\nnot_a_number,-58.3,Evento,25/12/2020\n");
    }

    Map<CampoHecho, List<String>> mapeo = Map.of(
        CampoHecho.LATITUD, List.of("latitud"),
        CampoHecho.LONGITUD, List.of("longitud"),
        CampoHecho.TITULO, List.of("titulo"),
        CampoHecho.FECHA_SUCESO, List.of("fechaSuceso")
    );

    List<Hecho> hechos = new LectorCSV(',', "dd/MM/yyyy", mapeo).importar(path);
    assertEquals(1, hechos.size());
    assertNull(hechos.get(0).getUbicacion());
  }
}