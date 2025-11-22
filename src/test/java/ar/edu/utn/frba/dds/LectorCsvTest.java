package ar.edu.utn.frba.dds;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import ar.edu.utn.frba.dds.model.hecho.CampoHecho;
import ar.edu.utn.frba.dds.model.hecho.Hecho;
import ar.edu.utn.frba.dds.model.lector.csv.LectorCSV;
import ar.edu.utn.frba.dds.model.lector.csv.filaconverter.HechoFilaConverter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class LectorCsvTest {
    private final String dir = "src/test/java/ar/edu/utn/frba/dds/CsvDePrueba/";

    @TempDir
    Path tempDir; // Directorio temporal gestionado por JUnit

    /**
     * Helper para crear un lector. Ahora recibe un mapa con claves String.
     */
    private LectorCSV<Hecho> crearLector(String dateFormat, Map<String, List<String>> mapeo) {
        HechoFilaConverter converter = new HechoFilaConverter(dateFormat, mapeo);
        return new LectorCSV<>(',', converter);
    }

    /**
     * Helper para convertir un Map<CampoHecho, List<String>> a Map<String, List<String>>.
     */
    private Map<String, List<String>> convertirMapeoAString(Map<CampoHecho, List<String>> mapeoEnum) {
        return mapeoEnum.entrySet().stream()
                .collect(Collectors.toMap(entry -> entry.getKey().name(), Map.Entry::getValue));
    }

    @Test
    public void importarCSVformatoCorrecto() {
        Map<String, List<String>> mapeoColumnas = convertirMapeoAString(Map.of(
                CampoHecho.TITULO, List.of("titulo"),
                CampoHecho.DESCRIPCION, List.of("descripcion"),
                CampoHecho.LATITUD, List.of("latitud"),
                CampoHecho.LONGITUD, List.of("longitud"),
                CampoHecho.FECHA_SUCESO, List.of("fechaSuceso"),
                CampoHecho.CATEGORIA, List.of("categoria"),
                CampoHecho.DIRECCION, List.of("direccion"),
                CampoHecho.PROVINCIA, List.of("provincia")
        ));

        LectorCSV<Hecho> lector = crearLector("dd/MM/yyyy", mapeoColumnas);
        List<Hecho> csv = lector.importar(dir + "ejemplo.csv");
        Hecho primerHecho = csv.get(0);

        assertEquals("auto", primerHecho.getCategoria());
        assertEquals(5, csv.size());
    }

    @Test
    public void importarCSVformatoExtraño() {
        Map<String, List<String>> mapeoColumnas = convertirMapeoAString(Map.of(
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
                List.of(
                        "provincia_nombre",
                        "departamento_nombre",
                        "localidad_nombre",
                        "calle_nombre",
                        "calle_altura"
                ),
                CampoHecho.PROVINCIA,
                List.of("provincia_nombre")
        ));

        LectorCSV<Hecho> lector = crearLector("dd-MM-yy", mapeoColumnas);
        List<Hecho> csv = lector.importar(dir + "rarito.csv");
        Hecho hecho = csv.get(0);

        assertEquals("Imputado idRegistro 13483", hecho.getTitulo());
    }

    @Test
    public void testCsvConColumnaInexistente() throws IOException {
        Path tempFile = tempDir.resolve("columnaInexistente.csv");
        try (FileWriter writer = new FileWriter(tempFile.toFile())) {
            writer.write(
                    "nombre,apellido,titulo,fechaSuceso,provincia\nJuan,Perez,Mi Titulo,25/12/2024,BsAs\n");
        }

        Map<String, List<String>> mapeo = convertirMapeoAString(Map.of(
                CampoHecho.TITULO, List.of("titulo"),
                CampoHecho.FECHA_SUCESO, List.of("fechaSuceso"),
                CampoHecho.PROVINCIA, List.of("provincia"),
                CampoHecho.DESCRIPCION, List.of("columna_que_no_existe")
        ));

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
            writer.write(
                    "latitud,longitud,titulo,fechaSuceso,provincia\nnot_a_number,-58.3,Evento,25/12/2020,CABA\n");
        }

        Map<String, List<String>> mapeo = convertirMapeoAString(Map.of(
                CampoHecho.LATITUD, List.of("latitud"),
                CampoHecho.LONGITUD, List.of("longitud"),
                CampoHecho.TITULO, List.of("titulo"),
                CampoHecho.FECHA_SUCESO, List.of("fechaSuceso")
        ));

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
            writer.write(
                    "titulo,titulo,fechaSuceso,provincia\nEvento duplicado,Repetido,01/01/2020,CABA\n");
        }

        Map<String, List<String>> mapeo = convertirMapeoAString(Map.of(
                CampoHecho.TITULO, List.of("titulo"),
                CampoHecho.FECHA_SUCESO, List.of("fechaSuceso"),
                CampoHecho.PROVINCIA, List.of("provincia")
        ));

        LectorCSV<Hecho> lector = crearLector("dd/MM/yyyy", mapeo);
        assertThrows(IllegalArgumentException.class, () -> lector.importar(tempFile.toString()));
    }

    @Test
    public void testFilaSinTituloNoSeCreaHecho() throws IOException {
        Path tempFile = tempDir.resolve("sinTitulo.csv");
        try (FileWriter writer = new FileWriter(tempFile.toFile())) {
            writer.write("titulo,fechaSuceso,provincia\n,01/01/2020,CABA\n");
        }

        Map<String, List<String>> mapeo = convertirMapeoAString(Map.of(
                CampoHecho.TITULO, List.of("titulo"),
                CampoHecho.FECHA_SUCESO, List.of("fechaSuceso"),
                CampoHecho.PROVINCIA, List.of("provincia")
        ));

        LectorCSV<Hecho> lector = crearLector("dd/MM/yyyy", mapeo);
        List<Hecho> hechos = lector.importar(tempFile.toString());
        assertEquals(0, hechos.size()); // no debería crear el hecho
    }
}
