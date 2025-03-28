/**
 * Copyright 2020 Alexander Herzog
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ui.mjpeg;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.apache.commons.math3.util.FastMath;

/**
 * Erstellt aus einer tempor�ren Pufferdatei eine MJPEG-Videodatei
 * @author Alexander Herzog
 */
public class MJPEGBuilder {
	/** Breite der Bilder */
	private int width;
	/** H�he der Bilder */
	private int height;
	/** Tempor�re Pufferdatei */
	private final File tempBuffer;
	/** Finale Ausgabedatei */
	private final File outputFile;

	/** Bilder/Sekunde */
	private static final double FRAME_RATE=30.0;
	/** Wiederholung aller Bilder? */
	private static final int FRAME_MULTIPLY=1;

	/** Anzahl an zu speichernden Bildern */
	private int writerCount;
	/** Anzahl an geschriebenen Bildern */
	private int writerPosition;

	/**
	 * Konstruktor der Klasse <code>MJPEGBuilder</code>
	 * @param tempBuffer	Pufferdatei
	 * @param outputFile	Ausgabedatei
	 */
	public MJPEGBuilder(final File tempBuffer, final File outputFile) {
		this.tempBuffer=tempBuffer;
		this.outputFile=outputFile;
		writerCount=0;
		writerPosition=0;
	}

	/**
	 * Lie�t eine Bilddatei aus dem tempor�ren Puffer
	 * @param size	Gr��e der Bilddatei in Bytes
	 * @param dataInput	Tempor�rer Puffer
	 * @return	Geladenes Bild
	 */
	private BufferedImage readImage(final int size, final DataInputStream dataInput) {
		try {
			final byte[] buf=new byte[size];
			final int result=dataInput.read(buf);
			if (result!=size) return null;
			final ByteArrayInputStream buffer=new ByteArrayInputStream(buf);
			final boolean useCache=ImageIO.getUseCache();
			try {
				ImageIO.setUseCache(false); /* Wird ben�tigt, wenn im Stream nicht gesprungen werden kann, was bei einem ByteArrayInputStream nun definitiv m�glich ist.  */
				return ImageIO.read(buffer);
			} finally {
				ImageIO.setUseCache(useCache);
			}
		} catch (IOException e) {
			return null;
		}
	}

	/**
	 * Lie�t die Bytes eines Bildes aus dem tempor�ren Puffer
	 * @param size	Gr��e der Bilddatei in Bytes
	 * @param dataInput	Tempor�rer Puffer
	 * @return	Geladenes Bild als Bytes
	 */
	private byte[] readImageBytes(final int size, final DataInputStream dataInput) {
		try {
			final byte[] buf=new byte[size];
			final int result=dataInput.read(buf);
			if (result!=size) return null;
			return buf;
		} catch (IOException e) {
			return null;
		}
	}

	/**
	 * Z�hlt wie viele Frames sich im Puffer befinden
	 * @param tempSize	Gr��e der Pufferdatei
	 * @param useAdditionalFrames	Gibt an, ob einzelne Frames wiederholt werden sollen, um l�ngere Zeitdauern zwischen �nderungen am System im Video abzubilden
	 * @return	Anzahl an Frames sich im Puffer
	 */
	private int countFrames(final long tempSize, final boolean useAdditionalFrames) {
		int frameCount=0;
		long bytesRead=0;

		try (FileInputStream fileInput=new FileInputStream(tempBuffer)) {
			try (DataInputStream dataInput=new DataInputStream(fileInput)) {

				long lastTimeStamp=-1;
				while (bytesRead<tempSize) {
					long timeStamp=dataInput.readLong();
					int imageBytes=dataInput.readInt();

					if (width==0) {
						final BufferedImage image=readImage(imageBytes,dataInput);
						if (image==null) return -1;
						width=image.getWidth();
						height=image.getHeight();
					} else {
						final int skipped=dataInput.skipBytes(imageBytes);
						if (skipped!=imageBytes) return -1;
					}

					if (lastTimeStamp<0) {
						frameCount++;
					} else {
						frameCount++;
						if (useAdditionalFrames) frameCount+=additionaFrameNumber(timeStamp-lastTimeStamp);
					}

					bytesRead+=8+4+imageBytes;
					lastTimeStamp=timeStamp;
				}

			} catch (IOException e) {return -1;}
		} catch (IOException e) {return -1;}

		return frameCount;
	}

	/**
	 * Berechnet die Anzahl an zus�tzlichen Bildern um l�ngere zeitliche L�cken zu �berbr�cken
	 * @param simTimeDelta	Zeitspanne
	 * @return	Anzahl an zus�tzlichen Bildern
	 */
	private int additionaFrameNumber(final long simTimeDelta) {
		return (int)FastMath.round(Math.log10(simTimeDelta))*3;
	}

	/**
	 * Verarbeitet die Daten<br>
	 * Bei Erfolg wird die Pufferdatei am Ende gel�scht
	 * @param tempSize	Gr��e der Pufferdatei
	 * @param generator	Ausgabesystem zur finalen Datei
	 * @param useAdditionalFrames	Gibt an, ob einzelne Frames wiederholt werden sollen, um l�ngere Zeitdauern zwischen �nderungen am System im Video abzubilden
	 * @return	Gibt <code>true</code> zur�ck, wenn die Daten erfolgreich verarbeitet werden konnten
	 */
	private boolean processImages(final long tempSize, final MJPEGGenerator generator, final boolean useAdditionalFrames) {
		long bytesRead=0;
		BufferedImage lastImage=null;

		try (FileInputStream fileInput=new FileInputStream(tempBuffer)) {
			try (DataInputStream dataInput=new DataInputStream(fileInput)) {

				long lastTimeStamp=-1;
				while (bytesRead<tempSize) {
					long timeStamp=dataInput.readLong();
					int imageBytes=dataInput.readInt();

					final BufferedImage image=readImage(imageBytes,dataInput);
					if (image==null) return false;

					int additionalFrames=(lastTimeStamp<0 || !useAdditionalFrames)?0:additionaFrameNumber(timeStamp-lastTimeStamp);
					for (int i=0;i<additionalFrames;i++) {
						if (lastImage!=null) generator.addImage(lastImage); else generator.addImage(image);
						writerPosition++;
					}

					generator.addImage(image);
					writerPosition++;

					lastImage=image;
					bytesRead+=8+4+imageBytes;
					lastTimeStamp=timeStamp;
				}

			} catch (Exception e) {return false;}
		} catch (Exception e) {return false;}

		return true;
	}

	/**
	 * Verarbeitet die Daten<br>
	 * Bei Erfolg wird die Pufferdatei am Ende gel�scht
	 * @param tempSize	Gr��e der Pufferdatei
	 * @param generator	Ausgabesystem zur finalen Datei
	 * @param useAdditionalFrames	Gibt an, ob einzelne Frames wiederholt werden sollen, um l�ngere Zeitdauern zwischen �nderungen am System im Video abzubilden
	 * @return	Gibt <code>true</code> zur�ck, wenn die Daten erfolgreich verarbeitet werden konnten
	 */
	private boolean processImagesDirect(final long tempSize, final MJPEGGenerator generator, final boolean useAdditionalFrames) {
		long bytesRead=0;
		byte[] lastImage=null;

		try (FileInputStream fileInput=new FileInputStream(tempBuffer)) {
			try (DataInputStream dataInput=new DataInputStream(fileInput)) {

				long lastTimeStamp=-1;
				while (bytesRead<tempSize) {
					long timeStamp=dataInput.readLong();
					int imageBytes=dataInput.readInt();

					final byte[] image=readImageBytes(imageBytes,dataInput);
					if (image==null) return false;

					int additionalFrames=(lastTimeStamp<0 || !useAdditionalFrames)?0:additionaFrameNumber(timeStamp-lastTimeStamp);
					/*
					for (int i=0;i<additionalFrames;i++) {
						if (lastImage!=null) generator.addImage(lastImage); else generator.addImage(image);
						writerPosition++;
					}
					 */
					if (additionalFrames>0) {
						if (lastImage!=null) generator.addImage(lastImage,additionalFrames*FRAME_MULTIPLY); else generator.addImage(image,additionalFrames*FRAME_MULTIPLY);
						writerPosition+=(additionalFrames*FRAME_MULTIPLY);
					}

					generator.addImage(image,1*FRAME_MULTIPLY);
					writerPosition+=(1*FRAME_MULTIPLY);

					lastImage=image;
					bytesRead+=8+4+imageBytes;
					lastTimeStamp=timeStamp;
				}

			} catch (Exception e) {return false;}
		} catch (Exception e) {return false;}

		return true;
	}

	/**
	 * Verarbeitet die Daten<br>
	 * Bei Erfolg wird die Pufferdatei am Ende gel�scht
	 * @param tempSize	Gr��e der Pufferdatei
	 * @param useAdditionalFrames	Gibt an, ob einzelne Frames wiederholt werden sollen, um l�ngere Zeitdauern zwischen �nderungen am System im Video abzubilden
	 * @param useDirectPath	Wurden die Bilder als 3-Byte-JPEGS aufgezeichnet, so k�nnen diese direkt in die avi-Datei geschrieben werden.
	 * @return	Gibt <code>true</code> zur�ck, wenn die Daten erfolgreich verarbeitet werden konnten
	 */
	public boolean process(final long tempSize, final boolean useAdditionalFrames, final boolean useDirectPath) {
		if (tempBuffer==null || outputFile==null) return false;

		int frames=countFrames(tempSize,useAdditionalFrames);
		if (frames<0) return false;
		writerCount=frames*FRAME_MULTIPLY;

		MJPEGGenerator generator;
		try {
			generator=new MJPEGGenerator(outputFile,width,height,FRAME_RATE,frames);
		} catch (Exception e) {
			return false;
		}

		if (useDirectPath) {
			if (!processImagesDirect(tempSize,generator,useAdditionalFrames)) return false;
		} else {
			if (!processImages(tempSize,generator,useAdditionalFrames)) return false;
		}

		try {
			generator.finishAVI();
		} catch (Exception e) {
			return false;
		}
		writerPosition=-1;

		return tempBuffer.delete();
	}

	/**
	 * Liefert die Anzahl an zu speichernden Bildern
	 * @return	Anzahl an zu speichernden Bildern
	 */
	public int getFramesToWrite() {
		return writerCount;
	}

	/**
	 * Liefert die Anzahl an geschriebenen Bildern
	 * @return Anzahl an geschriebenen Bildern
	 */
	public int getFramesWritten() {
		return writerPosition;
	}
}