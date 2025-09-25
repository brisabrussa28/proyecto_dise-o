package ar.edu.utn.frba.dds.domain.fuentes.apis.serviciometamapa;

import ar.edu.utn.frba.dds.domain.info.PuntoGeografico;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;

/**
 * Value Object que representa los parámetros de consulta para el ServicioMetaMapa.
 */
@JsonInclude(JsonInclude.Include.NON_NULL) // No incluirá campos nulos en el JSON
public class HechoQuerys {
    private final String categoria;
    private final String provincia;
    private final PuntoGeografico ubicacion;
    private final LocalDateTime fechaAcontecimientoDesde;
    private final LocalDateTime fechaAcontecimientoHasta;
    private final LocalDateTime fechaCargaDesde;
    private final LocalDateTime fechaCargaHasta;

    public HechoQuerys(
            String categoria, String provincia, PuntoGeografico ubicacion,
            LocalDateTime fechaAcontecimientoDesde, LocalDateTime fechaAcontecimientoHasta,
            LocalDateTime fechaCargaDesde, LocalDateTime fechaCargaHasta) {

        if (fechaAcontecimientoDesde == null || fechaCargaDesde == null) {
            throw new RuntimeException(
                    "Debe tener fecha de acontecimiento  y/o fecha de carga");
        }

        this.categoria = categoria;
        this.provincia = provincia;
        this.ubicacion = ubicacion;
        this.fechaAcontecimientoDesde = fechaAcontecimientoDesde;
        this.fechaAcontecimientoHasta = fechaAcontecimientoHasta;
        this.fechaCargaDesde = fechaCargaDesde;
        this.fechaCargaHasta = fechaCargaHasta;
    }

    public String getCategoria() {
        return categoria;
    }

    public String getProvincia() {
        return provincia;
    }

    public PuntoGeografico getUbicacion() {
        return ubicacion;
    }

    public LocalDateTime getFechaAcontecimientoDesde() {
        return fechaAcontecimientoDesde;
    }

    public LocalDateTime getFechaAcontecimientoHasta() {
        return fechaAcontecimientoHasta;
    }

    public LocalDateTime getFechaCargaDesde() {
        return fechaCargaDesde;
    }

    public LocalDateTime getFechaCargaHasta() {
        return fechaCargaHasta;
    }
}
