package ar.edu.utn.frba.dds;

import ar.edu.utn.frba.dds.domain.coleccion.Coleccion;
import ar.edu.utn.frba.dds.domain.info.PuntoGeografico;
import ar.edu.utn.frba.dds.domain.origen.Origen;
import ar.edu.utn.frba.dds.domain.reportes.GestorDeReportes;
import ar.edu.utn.frba.dds.domain.reportes.Solicitud;
import ar.edu.utn.frba.dds.domain.exceptions.SolicitudInexistenteException;
import ar.edu.utn.frba.dds.domain.exceptions.RazonInvalidaException;
import ar.edu.utn.frba.dds.domain.fuentes.FuenteDinamica;
import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import ar.edu.utn.frba.dds.main.Usuario;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class SolicitudTest {
    Usuario contribuyenteA = new Usuario(null, null);
    Usuario iluminati = new Usuario("△", "libellumcipher@incognito.com");
    PuntoGeografico pgAux = new PuntoGeografico(33.39627891281455, 44.48695991794239);
    FuenteDinamica fuenteAuxD = new FuenteDinamica("Julio Cesar", null);
    Date horaAux = Date.from(LocalDateTime.of(2025, 5, 6, 20, 9)
        .atZone(ZoneId.systemDefault())
        .toInstant());
    List<String> etiquetasAux = List.of(
        "#ancianita",
        "#robo_a_mano_armada",
        "#violencia",
        "#leyDeProtecciónALasAncianitas",
        "#NOalaVIOLENCIAcontraABUELITAS"
    );

    @Test
    public void solicitarEliminacionDeHechoCorrectamente() {
        String motivo = "a".repeat(600);
        Hecho hecho = new Hecho("titulo", "desc", "Robos", "direccion", pgAux, horaAux, horaAux, Origen.CARGA_MANUAL, etiquetasAux);
        FuenteDinamica fuente = new FuenteDinamica("MiFuente", null);
        fuente.agregarHecho(hecho);

        Solicitud solicitud = new Solicitud(null, hecho, motivo);
        GestorDeReportes.getInstancia().agregarSolicitud(solicitud);

        assertEquals(1, GestorDeReportes.getInstancia().cantidadSolicitudes());
    }

    @Test
    public void gestorDeReportesNoObtieneSolicitudes() {
        String motivo = "a".repeat(5); // motivo inválido
        Hecho hecho = new Hecho("titulo", "desc", "Robos", "direccion", pgAux, horaAux, horaAux, Origen.CARGA_MANUAL, etiquetasAux);
        FuenteDinamica fuente = new FuenteDinamica("MiFuente", null);
        fuente.agregarHecho(hecho);

        assertThrows(RazonInvalidaException.class, () -> {
            new Solicitud(null, hecho, motivo);
        });
    }

    @Test
    public void gestorDeReportesNoTieneSolicitud() {
        String motivo = "perú es clave".repeat(50);
        Solicitud solicitud = new Solicitud(null, null, motivo);

        assertThrows(SolicitudInexistenteException.class, () -> {
            GestorDeReportes.gestionarSolicitud(solicitud, true);
        });
    }
}