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
    DetectorSpam detectorMock = mock(DetectorSpam.class);

    when(detectorMock.esSpam("Este es un mensaje spam")).thenReturn(true);

    verify(detectorMock).esSpam("Este es un mensaje spam");

    assertTrue(detectorMock.esSpam("Este es un mensaje spam"));
  }

  @Test
  public void noDetectaSpamCuandoEsMensajeNormal() {
    DetectorSpam detectorMock = mock(DetectorSpam.class);

    when(detectorMock.esSpam("Mensaje normal")).thenReturn(false);

    verify(detectorMock).esSpam("Mensaje normal");

    assertFalse(detectorMock.esSpam("Mensaje normal"));
  }
}