package ar.edu.utn.frba.dds;

import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import ar.edu.utn.frba.dds.domain.hecho.CampoHecho;
import ar.edu.utn.frba.dds.domain.filtro.FiltroDeDireccion;
import ar.edu.utn.frba.dds.main.Administrador;
import ar.edu.utn.frba.dds.domain.csv.LectorCSV;
import org.junit.jupiter.api.Test;



import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import java.util.Objects;

public class LectorCsvTest {
    Administrador iluminati = new Administrador("△", "libellumcipher@incognito.com");
    Administrador admin = new Administrador("pipocapo", "makenipipo@gmail.com");

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

        List<Hecho> csv = new LectorCSV().importar("src/main/java/ar/edu/utn/frba/dds/domain/csv/ejemplo.csv", ',', "dd/MM/yyyy",mapeoColumnas);
        FiltroDeDireccion filtroDireccion = new FiltroDeDireccion("EL NESTORNAUTA");
        List<Hecho> hechosFiltrados = filtroDireccion.filtrar(csv);
        Hecho hecho = hechosFiltrados.get(0);

        boolean hechoCorrecto = Objects.equals(hecho.getCategoria(), "buenardo");
        boolean tiene5hechos = csv.size() == 5;
        assertTrue(tiene5hechos && hechoCorrecto);
    }
    @Test
    public void importarCSVformatoExtraño() {
        Map<CampoHecho, List<String>> mapeoColumnas = Map.of(
            CampoHecho.TITULO, List.of("tipo_persona_id"),
            CampoHecho.DESCRIPCION, List.of("tipo_persona","modo_produccion_hecho_ampliada","modo_produccion_hecho_otro"),
            CampoHecho.LATITUD, List.of("latitud"),
            CampoHecho.LONGITUD, List.of("longitud"),
            CampoHecho.FECHA_SUCESO, List.of("fecha_hecho"),
            CampoHecho.CATEGORIA, List.of("semaforo_estado"),
            CampoHecho.DIRECCION, List.of("provincia_nombre","departamento_nombre","localidad_nombre","calle_nombre","calle_altura")
        );

        List<Hecho> csv = new LectorCSV().importar("src/main/java/ar/edu/utn/frba/dds/domain/csv/rarito.csv", ',', "dd-MM-yy",mapeoColumnas);
        Hecho hecho = csv.get(0);
        System.out.println("Hecho importado:");
        System.out.println(hecho.getTitulo() + " - " + hecho.getDescripcion() + " - " + hecho.getCategoria() + " - " + hecho.getFechaSuceso() + " - " + hecho.getDireccion());
        boolean hechoCorrecto = Objects.equals(hecho.getTitulo(), "Imputado idRegistro 13483");
        assertTrue( hechoCorrecto);
    }


    @Test
    public void testCantidadDeHechosEnEjemploCsv() {
        Map<CampoHecho, List<String>> mapeoEjemplo = Map.of(
            CampoHecho.TITULO, List.of("titulo"),
            CampoHecho.DESCRIPCION, List.of("descripcion"),
            CampoHecho.LATITUD, List.of("latitud"),
            CampoHecho.LONGITUD, List.of("longitud"),
            CampoHecho.FECHA_SUCESO, List.of("fechaSuceso"),
            CampoHecho.CATEGORIA, List.of("categoria"),
            CampoHecho.DIRECCION, List.of("direccion")
        );
        List<Hecho> hechos = new LectorCSV().importar("src/main/java/ar/edu/utn/frba/dds/domain/csv/ejemplo.csv", ',', "dd/MM/yyyy", mapeoEjemplo);
        assertEquals(5, hechos.size());
    }

    @Test
    public void testLecturaConColumnasReordenadas() {
        Map<CampoHecho, List<String>> mapeoEjemplo = Map.of(
            CampoHecho.TITULO, List.of("titulo"),
            CampoHecho.DESCRIPCION, List.of("descripcion"),
            CampoHecho.LATITUD, List.of("latitud"),
            CampoHecho.LONGITUD, List.of("longitud"),
            CampoHecho.FECHA_SUCESO, List.of("fechaSuceso"),
            CampoHecho.CATEGORIA, List.of("categoria"),
            CampoHecho.DIRECCION, List.of("direccion")
        );
        List<Hecho> hechos = new LectorCSV().importar("src/main/java/ar/edu/utn/frba/dds/domain/csv/ejemploReordenado.csv", ',', "dd/MM/yyyy", mapeoEjemplo);
        assertEquals(5, hechos.size());
        assertTrue(hechos.stream().anyMatch(h -> "EL NESTORNAUTA".equals(h.getDireccion())));
    }

    @Test
    public void testCsvConFilasVaciasYLecturaEspecial() {
        Map<CampoHecho, List<String>> mapeo = Map.of(
            CampoHecho.TITULO, List.of("tipo_persona_id"),
            CampoHecho.DESCRIPCION, List.of("tipo_persona"),
            CampoHecho.LATITUD, List.of("latitud"),
            CampoHecho.LONGITUD, List.of("longitud"),
            CampoHecho.FECHA_SUCESO, List.of("fecha_hecho"),
            CampoHecho.CATEGORIA, List.of("semaforo_estado"),
            CampoHecho.DIRECCION, List.of("provincia_nombre", "departamento_nombre", "localidad_nombre", "calle_nombre", "calle_altura")
        );

        List<Hecho> hechos = new LectorCSV().importar("src/main/java/ar/edu/utn/frba/dds/domain/csv/luciano.csv", ',', "dd-MM-yy", mapeo);

        // Solo debe haber 1 hecho, los otros están vacíos
        assertEquals(1, hechos.size());
        assertEquals("Imputado idRegistro 13483", hechos.get(0).getTitulo());
    }
    private final String dir = "src/main/java/ar/edu/utn/frba/dds/domain/csv/";

    @Test
    public void testCsvSinEncabezado() throws IOException {
        String path = dir + "sinEncabezado.csv";
        try (FileWriter writer = new FileWriter(path)) {
            writer.write(""); // archivo vacío
        }

        assertThrows(IllegalArgumentException.class, () -> {
            new LectorCSV().importar(path, ',', "dd/MM/yyyy", Map.of());
        });
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

        List<Hecho> hechos = new LectorCSV().importar(path, ',', "dd/MM/yyyy", mapeo);
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

        List<Hecho> hechos = new LectorCSV().importar(path, ',', "dd/MM/yyyy", mapeo);
        assertEquals(1, hechos.size()); // Se crea igual porque lat/lon son opcionales
        assertNull(hechos.get(0).getUbicacion());
    }

    @Test
    public void testCsvConFechaInvalida() throws IOException {
        String path = dir + "fechaInvalida.csv";
        try (FileWriter writer = new FileWriter(path)) {
            writer.write("titulo,fechaSuceso\nEvento,32/13/2020\n");
        }

        Map<CampoHecho, List<String>> mapeo = Map.of(
            CampoHecho.TITULO, List.of("titulo"),
            CampoHecho.FECHA_SUCESO, List.of("fechaSuceso")
        );

        List<Hecho> hechos = new LectorCSV().importar(path, ',', "dd/MM/yyyy", mapeo);
        assertEquals(0, hechos.size()); // No se crea por fecha inválida
    }

    @Test
    public void testCsvConFilaVacia() throws IOException {
        String path = dir + "filaVacia.csv";
        try (FileWriter writer = new FileWriter(path)) {
            writer.write("titulo,fechaSuceso\nEvento,25/12/2020\n,\n\n");
        }

        Map<CampoHecho, List<String>> mapeo = Map.of(
            CampoHecho.TITULO, List.of("titulo"),
            CampoHecho.FECHA_SUCESO, List.of("fechaSuceso")
        );

        List<Hecho> hechos = new LectorCSV().importar(path, ',', "dd/MM/yyyy", mapeo);
        assertEquals(1, hechos.size());
    }
}
