package ar.edu.utn.frba.dds;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ar.edu.utn.frba.dds.domain.coleccion.Coleccion;
import ar.edu.utn.frba.dds.domain.estadisicas.CentralDeEstadisticas;
import ar.edu.utn.frba.dds.domain.estadisicas.Estadistica;
import ar.edu.utn.frba.dds.domain.exportador.Exportador;
import ar.edu.utn.frba.dds.domain.exportador.csv.ExportadorCSV;
import ar.edu.utn.frba.dds.domain.exportador.csv.modoexportacion.ModoSobrescribir;
import ar.edu.utn.frba.dds.domain.fuentes.FuenteDinamica;
import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import ar.edu.utn.frba.dds.domain.hecho.HechoBuilder;
import ar.edu.utn.frba.dds.domain.reportes.RepositorioDeSolicitudes;
import ar.edu.utn.frba.dds.domain.reportes.detectorspam.DetectorSpam;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class EstadisticasTest {
    private CentralDeEstadisticas calculadora;
    private RepositorioDeSolicitudes repo;
    private FuenteDinamica fuente;
    private Coleccion coleccion;
    private List<Coleccion> colecciones;
    private Hecho hecho1, hecho2, hecho3;
    private final DetectorSpam detector = texto -> texto.contains("Troll");

    @TempDir
    Path tempDir; // Directorio temporal para el test de exportación

    @BeforeEach
    public void setUp() {
        fuente = new FuenteDinamica("Fuente para Estadísticas");

        LocalDateTime hora = LocalDateTime.now();

        hecho1 = new HechoBuilder()
                .conTitulo("Robo en Almagro")
                .conCategoria("Robos")
                .conProvincia("CABA")
                .conFechaSuceso(hora.minusHours(1))
                .build();

        hecho2 = new HechoBuilder()
                .conTitulo("Robo en Caballito")
                .conCategoria("Robos")
                .conProvincia("CABA")
                .conFechaSuceso(hora.minusHours(1))
                .build();

        hecho3 = new HechoBuilder()
                .conTitulo("Hurto en Avellaneda")
                .conCategoria("Hurtos")
                .conProvincia("PBA")
                .conFechaSuceso(hora.minusHours(2))
                .build();

        fuente.agregarHecho(hecho1);
        fuente.agregarHecho(hecho2);
        fuente.agregarHecho(hecho3);

        repo = new RepositorioDeSolicitudes(detector);
        calculadora = new CentralDeEstadisticas();
        calculadora.setRepo(repo);

        Exportador<Estadistica> exportadorCsv = new ExportadorCSV<>(new ModoSobrescribir());
        calculadora.setExportador(exportadorCsv);

        coleccion = new Coleccion("Coleccion de Hechos", fuente, "Descripcion de prueba", "General");
        colecciones = new ArrayList<>();
        colecciones.add(coleccion);
    }

    @Test
    public void estadisticasSpam() {
        UUID solicitante = UUID.randomUUID();

        // --- CORRECCIÓN: Se usan motivos con la longitud correcta (más de 500 caracteres) ---
        String motivoLargoValido1 = "Este es un motivo extremadamente detallado para solicitar la eliminación de un hecho, asegurando así que cumplimos con la validación de longitud mínima de 500 caracteres. La razón principal de esta solicitud es que la información presentada en el hecho es incorrecta y puede llevar a conclusiones erróneas por parte de quienes la consulten. Hemos verificado internamente y podemos proveer documentación de respaldo que demuestra la inconsistencia de los datos reportados. Es crucial para la integridad de la plataforma que la información sea precisa y confiable. Esperamos su pronta respuesta y agradecemos su atención. Saludos cordiales. Fin del comunicado extendido para validación.";
        String motivoLargoValido2 = "Solicito formalmente la remoción del hecho previamente identificado, ya que contiene datos que han sido desactualizados por eventos posteriores. Para cumplir con el requisito de los 500 caracteres, procedo a detallar exhaustivamente el contexto. La situación ha evolucionado y mantener el reporte original sin una actualización o eliminación podría ser perjudicial para la correcta interpretación de la secuencia de eventos. Consideramos que la transparencia y la precisión son pilares fundamentales de este sistema de información. Muchas gracias por su colaboración en mantener la calidad de los datos. Atentamente, el equipo de verificación de datos.";
        String motivoLargoSpam = "Este es un comentario Troll que, además, es suficientemente largo para pasar la validación de longitud. El propósito de este texto es simplemente demostrar que un comentario puede ser spam independientemente de su longitud, y que la detección debe basarse en el contenido y no solo en la cantidad de caracteres. Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur.";


        repo.agregarSolicitud(solicitante, hecho1, motivoLargoValido1);
        repo.agregarSolicitud(solicitante, hecho1, motivoLargoValido2);
        repo.agregarSolicitud(solicitante, hecho1, motivoLargoSpam);

        // 1 de 3 solicitudes es spam (33.33%)
        assertEquals(33.33, calculadora.porcentajeDeSolicitudesSpam(), 0.01);
    }

    @Test
    public void estadisticasDeProvinciaConMasHechos() {
        Estadistica resultado = calculadora.provinciaConMasHechos(coleccion);
        assertNotNull(resultado);
        assertEquals("CABA", resultado.getDimension());
        assertEquals(2, resultado.getValor());
    }

    @Test
    public void estadisticasCategoriaConMasHechos() {
        Estadistica resultado = calculadora.categoriaConMasHechos(colecciones);
        assertNotNull(resultado);
        assertEquals("Robos", resultado.getDimension());
        assertEquals(2, resultado.getValor());
    }

    @Test
    public void estadisticasHechosDeCiertaCategoria() {
        Estadistica resultado = calculadora.provinciaConMasHechosDeCiertaCategoria(colecciones, "Robos");
        assertNotNull(resultado);
        assertEquals("CABA", resultado.getDimension());
        assertEquals(2, resultado.getValor());
    }

    @Test
    public void estadisticasHoraConMasHechosDeCiertaCategoria() {
        Estadistica resultado = calculadora.horaConMasHechosDeCiertaCategoria(colecciones, "Robos");
        String horaEsperada = String.format("%02d", LocalDateTime.now().minusHours(1).getHour());
        assertNotNull(resultado);
        assertEquals(horaEsperada, resultado.getDimension());
        assertEquals(2, resultado.getValor());
    }

    @Test
    public void seExportaCorrectamente() throws IOException {
        List<Estadistica> datos = calculadora.hechosPorCategoria(colecciones);
        Path outputPath = tempDir.resolve("export_test.csv");

        calculadora.export(datos, outputPath.toString());

        File exportedFile = outputPath.toFile();
        assertTrue(exportedFile.exists());
        assertTrue(exportedFile.length() > 0);

        List<String> lines = Files.readAllLines(exportedFile.toPath());
        assertEquals(3, lines.size()); // Cabecera + 2 filas de datos
        assertEquals("\"DIMENSION\",\"VALOR\"", lines.get(0));
        assertTrue(lines.contains("\"Hurtos\",\"1\""));
        assertTrue(lines.contains("\"Robos\",\"2\""));
    }
}

