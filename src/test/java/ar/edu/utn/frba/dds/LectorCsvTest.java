package ar.edu.utn.frba.dds;

import ar.edu.utn.frba.dds.domain.exceptions.ArchivoVacioException;
import ar.edu.utn.frba.dds.domain.fuentes.Fuente;
import ar.edu.utn.frba.dds.domain.fuentes.FuenteDinamica;
import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import ar.edu.utn.frba.dds.domain.filtro.FiltroDeDireccion;
import ar.edu.utn.frba.dds.main.Administrador;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

public class LectorCsvTest {
    Administrador iluminati = new Administrador("△", "libellumcipher@incognito.com");
    Administrador admin = new Administrador("pipocapo", "makenipipo@gmail.com");


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
}
