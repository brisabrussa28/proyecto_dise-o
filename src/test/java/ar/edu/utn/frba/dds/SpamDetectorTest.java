package ar.edu.utn.frba.dds;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ar.edu.utn.frba.dds.domain.detectorspam.DetectorSpam;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class SpamDetectorTest {

  @Test
  @DisplayName("Un mock de DetectorSpam configurado para devolver 'true' funciona correctamente")
  public void mockDetectaSpamCuandoSeConfiguraParaSpam() {
    DetectorSpam detectorMock = mock(DetectorSpam.class);
    String mensajeSpam = "Este es un mensaje spam";
    when(detectorMock.esSpam(mensajeSpam)).thenReturn(true);

    boolean resultado = detectorMock.esSpam(mensajeSpam);

    assertTrue(resultado, "El mock debería haber devuelto true como se configuró.");

    verify(detectorMock).esSpam(mensajeSpam);
  }

  @Test
  @DisplayName("Un mock de DetectorSpam configurado para devolver 'false' funciona correctamente")
  public void mockNoDetectaSpamCuandoSeConfiguraParaNoSpam() {
    // Arrange
    DetectorSpam detectorMock = mock(DetectorSpam.class);
    String mensajeNormal = "Mensaje normal";
    when(detectorMock.esSpam(mensajeNormal)).thenReturn(false);

    // Act
    boolean resultado = detectorMock.esSpam(mensajeNormal);

    // Assert
    assertFalse(resultado, "El mock debería haber devuelto false como se configuró.");

    // Verify
    verify(detectorMock).esSpam(mensajeNormal);
  }
}
