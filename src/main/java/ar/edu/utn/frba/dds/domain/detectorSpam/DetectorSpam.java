package ar.edu.utn.frba.dds.domain.detectorSpam;

public interface DetectorSpam {

    /**
     * Método para detectar si un texto es spam.
     *
     * @param texto El texto a evaluar.
     * @return true si el texto es considerado spam, false en caso contrario.
     */
    boolean esSpam(String texto);
}