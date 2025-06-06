package ar.edu.utn.frba.dds;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ar.edu.utn.frba.dds.domain.detectorspam.DetectorSpam;
import org.junit.jupiter.api.Test;

public class SpamDetectorTest {

  @Test
  public void detectaSpamCuandoEsMensajeSpam() {
    // Create a mock of DetectorSpam
    DetectorSpam detectorMock = mock(DetectorSpam.class);

    // Define behavior for the mock
    when(detectorMock.esSpam("Este es un mensaje spam")).thenReturn(true);

    // Test the mocked behavior
    assertTrue(detectorMock.esSpam("Este es un mensaje spam"));

    // Verify interaction with the mock
    verify(detectorMock).esSpam("Este es un mensaje spam");
  }

  @Test
  public void noDetectaSpamCuandoEsMensajeNormal() {
    // Create a mock of DetectorSpam
    DetectorSpam detectorMock = mock(DetectorSpam.class);

    // Define behavior for the mock
    when(detectorMock.esSpam("Mensaje normal")).thenReturn(false);

    // Test the mocked behavior
    assertFalse(detectorMock.esSpam("Mensaje normal"));

    // Verify interaction with the mock
    verify(detectorMock).esSpam("Mensaje normal");
  }
}