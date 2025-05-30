package ar.edu.utn.frba.dds;

import ar.edu.utn.frba.dds.domain.exceptions.ArchivoVacioException;
import ar.edu.utn.frba.dds.domain.fuentes.Fuente;
import ar.edu.utn.frba.dds.domain.fuentes.FuenteDinamica;
import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import ar.edu.utn.frba.dds.domain.hecho.CampoHecho;
import ar.edu.utn.frba.dds.domain.filtro.FiltroDeDireccion;
import ar.edu.utn.frba.dds.main.Administrador;
import ar.edu.utn.frba.dds.domain.csv.UltraBindLector;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

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

        List<Hecho> csv = new UltraBindLector().importar("src/main/java/ar/edu/utn/frba/dds/domain/csv/ejemplo.csv", ',', "dd/MM/yyyy",mapeoColumnas);
        FiltroDeDireccion filtroDireccion = new FiltroDeDireccion("EL NESTORNAUTA");
        List<Hecho> hechosFiltrados = filtroDireccion.filtrar(csv);
        Hecho hecho = hechosFiltrados.get(0);

        boolean hechoCorrecto = Objects.equals(hecho.getCategoria(), "buenardo");
        boolean tiene5hechos = csv.size() == 5;
        assertTrue(tiene5hechos && hechoCorrecto);
    }
    @Test
    public void importarCSVformatoIncorrecto() {
        Map<CampoHecho, List<String>> mapeoColumnas = Map.of(
            CampoHecho.TITULO, List.of("tipo_persona_id"),
            CampoHecho.DESCRIPCION, List.of("tipo_persona","modo_produccion_hecho_ampliada","modo_produccion_hecho_otro"),
            CampoHecho.LATITUD, List.of("latitud"),
            CampoHecho.LONGITUD, List.of("longitud"),
            CampoHecho.FECHA_SUCESO, List.of("fecha_hecho"),
            CampoHecho.CATEGORIA, List.of("semaforo_estado"),
            CampoHecho.DIRECCION, List.of("provincia_nombre","departamento_nombre","localidad_nombre","calle_nombre","calle_altura")
        );

        List<Hecho> csv = new UltraBindLector().importar("src/main/java/ar/edu/utn/frba/dds/domain/csv/rarito.csv", ',', "dd/M/yyyy",mapeoColumnas);
        Hecho hecho = csv.get(0);

        boolean hechoCorrecto = Objects.equals(hecho.getCategoria(), "Imputado idRegistro 13483");
        assertTrue( hechoCorrecto);
    }


/*
    @Test
    public void importarCSV() {
        Fuente csv = admin.importardesdeCsv("src/main/java/ar/edu/utn/frba/dds/domain/csv/ejemplo.csv", ",", "bene");
        FiltroDeDireccion filtroDireccion = new FiltroDeDireccion("EL NESTORNAUTA");
        List<Hecho> hechosFiltrados = filtroDireccion.filtrar(csv.obtenerHechos());
        Hecho hecho = hechosFiltrados.get(0);

        boolean hechoCorrecto = Objects.equals(hecho.getCategoria(), "buenardo");
        boolean tiene5hechos = csv.obtenerHechos().size() == 5;
        assertTrue(tiene5hechos && hechoCorrecto);
    }

    @Test
    public void seImportaunaFuenteEstaticaCorrectamentePeroVacia() {
        assertThrows(ArchivoVacioException.class, () -> iluminati.importardesdeCsv("src/main/java/ar/edu/utn/frba/dds/domain/csv/prueba2.csv", ",", "datos.gob.ar"));
    }

    @Test
    public void seImportaUnaFuenteEstaticaIncorrectamente() {
        assertThrows(RuntimeException.class, () -> iluminati.importardesdeCsv("src/main/java/ar/edu/utn/frba/dds/domain/csv/aaa.csv", ",", "datos.gob.ar"));
    }

    */
}
