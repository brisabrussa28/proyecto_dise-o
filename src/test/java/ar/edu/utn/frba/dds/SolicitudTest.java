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
import ar.edu.utn.frba.dds.main.Administrador; //ELIMINAR
import ar.edu.utn.frba.dds.main.Contribuyente; //ELIMINAR
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class SolicitudTest {
    Contribuyente contribuyenteA = new Contribuyente(null, null); //esta mal tener contribuyentes eliminarlos dsp
    Administrador iluminati = new Administrador("△", "libellumcipher@incognito.com");
    PuntoGeografico pgAux = new PuntoGeografico(33.39627891281455, 44.48695991794239);
    FuenteDinamica fuenteAuxD = new FuenteDinamica("Julio Cesar", null);
    LocalDateTime horaAux = LocalDateTime.of(2025, 5, 6, 20, 9);
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

        Solicitud solicitud = new Solicitud(null, hecho.getId(), fuente, motivo);
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
            new Solicitud(null, hecho.getId(), fuente, motivo);
        });
    }

    @Test
    public void gestorDeReportesNoTieneSolicitud() {
        String motivo = "perú es clave".repeat(50);
        Solicitud solicitud = new Solicitud(null, null, null, motivo);

        assertThrows(SolicitudInexistenteException.class, () -> {
            GestorDeReportes.gestionarSolicitud(solicitud, true);
        });
    }
}