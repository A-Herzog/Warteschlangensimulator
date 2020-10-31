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
package ui.modeleditor;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.commons.math3.util.FastMath;
import org.apache.jena.ext.com.google.common.base.Objects;

/**
 * Hält Bilder in skalierter Größe vor, so dass nicht bei jedem Aufruf der Zeichenfunktion
 * erneut eine Skalierung vorgenommen werden muss.<br>
 * Die Klasse ist ein Singleton, der Konstruktor kann nicht direkt aufgerufen werden, sondern
 * es muss über die statische Methode <code>getScaledImageCache()</code> eine Instanz
 * angefordert werden.
 * @author Alexander Herzog
 * @see #getScaledImageCache()
 */
public class ScaledImageCache {
	/** Maximalzahl an Einträgen in dem Cache */
	private static final int MAX_CACHE_SIZE=20;

	/** Hash-Werte der Bilder im Cache */
	private String[] cacheHash;
	/** Bilder-Cache */
	private BufferedImage[] cacheData;
	/** Zeitpunkt des letzten Zugriffs auf einen Eintrag */
	private long[] cacheLastAccess;

	/**
	 * Konstruktor der Klasse <code>ScaledImageCache</code>, kann nicht direkt
	 * aufgerufen werden, sondern über die statische Methode <code>getScaledImageCache()</code>
	 * muss eine Instanz angefordert werden.
	 * @see #getScaledImageCache()
	 */
	private ScaledImageCache() {
		cacheHash=new String[MAX_CACHE_SIZE];
		cacheData=new BufferedImage[MAX_CACHE_SIZE];
		cacheLastAccess=new long[MAX_CACHE_SIZE];
	}

	/**
	 * Singleton-Instanz von {@link ScaledImageCache}
	 * @see #getScaledImageCache()
	 */
	private static ScaledImageCache scaledImageCache=null;

	/**
	 * Fordert die Singleton-Instanz von {@link ScaledImageCache} an.
	 * @return	Instanz von {@link ScaledImageCache}
	 */
	public static ScaledImageCache getScaledImageCache() {
		if (scaledImageCache==null) scaledImageCache=new ScaledImageCache();
		return scaledImageCache;
	}

	/**
	 * Zu verwendende Hash-Algorithmen zur Bestimmung der Hash-Werte der
	 * Bilder beim Cachen
	 */
	private static final String[] hashAlgorithms=new String[] {"SHA-256","SHA","MD5"};

	/**
	 * Berechnet den Hashwert zu einem Bild
	 * @param image	Bild, zu dem der Hash berechnet werden soll. (Kann <code>null</code> sein, dann wird "0" geliefert.)
	 * @return	Hashwert
	 */
	public static String getHash(final BufferedImage image) {
		if (image==null) return "0";
		final byte[] pixels=((DataBufferByte)image.getRaster().getDataBuffer()).getData();

		for (String algorithm: hashAlgorithms) try {
			final MessageDigest m=MessageDigest.getInstance(algorithm);
			m.update(pixels,0,pixels.length);
			return String.format("%1$032X",new BigInteger(1,m.digest()));
		} catch (NoSuchAlgorithmException e) {/* Algorithmus nicht verfügbar, zum nächsten in der Schleife */}

		return null; /* Alles fehlgeschlagen */
	}

	/**
	 * Prüft, ob zwei Bilder inhaltlich identisch sind
	 * @param image1	Bild 1 (kann <code>null</code> sein)
	 * @param image2	Bild 2 (kann <code>null</code> sein)
	 * @return	Liefert <code>true</code> wenn entweder beide Bilder <code>null</code> sind oder aber die Objekte inhaltlich identisch sind
	 */
	public static boolean compare(final BufferedImage image1, final BufferedImage image2) {
		if (image1==null && image2==null) return true;
		if (image1==null || image2==null) return false;

		return Objects.equal(getHash(image1),getHash(image2));
	}

	/**
	 * Liefert ein Bild aus dem Cache
	 * @param hash	Hash-Wert des Ausgangs-Bildes
	 * @param width	Breite des skalierten Bildes
	 * @param height	Höhe des skalierten Bildes
	 * @return	Liefert im Erfolgsfall das skalierte Bild oder <code>null</code>, wenn sich kein passender Datensatz im Cache befindet
	 */
	private BufferedImage getFromCache(final String hash, final int width, final int height) {
		final long time=System.currentTimeMillis();
		BufferedImage result=null;
		for (int i=0;i<cacheHash.length;i++) if (cacheHash[i]!=null) {
			if (result==null && cacheHash[i].equals(hash) && cacheData[i].getWidth()==width && cacheData[i].getHeight()==height) {
				cacheLastAccess[i]=time;
				result=cacheData[i];
				continue;
			}
			if (cacheLastAccess[i]+1000*60<time) {
				cacheHash[i]=null;
				cacheData[i]=null;
				cacheLastAccess[i]=0;
			}
		}
		return result;
	}

	/**
	 * Speichert ein Skaliertes Bild im Cache
	 * @param hash	Hash-Wert des Ausgangs-Bildes
	 * @param image	Skaliertes, zu speicherndes Bild
	 */
	private void storeToCache(final String hash, final BufferedImage image) {
		boolean done=false;
		int oldestIndex=0;
		final long time=System.currentTimeMillis();
		long oldestValue=time+1;

		for (int i=0;i<cacheHash.length;i++) {
			if (cacheHash[i]!=null && cacheLastAccess[i]+1000*60<time) {
				cacheHash[i]=null;
				cacheData[i]=null;
				cacheLastAccess[i]=0;
			}
			if (!done && (cacheHash[i]==null || cacheHash[i].equals(hash))) {
				cacheHash[i]=hash;
				cacheData[i]=image;
				cacheLastAccess[i]=time;
				done=true;
			}
			if (cacheHash[i]!=null && cacheLastAccess[i]<oldestValue) {
				oldestValue=cacheLastAccess[i];
				oldestIndex=i;
			}
		}

		if (!done) {
			cacheHash[oldestIndex]=hash;
			cacheData[oldestIndex]=image;
			cacheLastAccess[oldestIndex]=time;
		}
	}

	/**
	 * Skaliert ein Bild
	 * @param original	Ausgangsgröße
	 * @param width	Breite des skalierten Bildes
	 * @param height	Höhe des skalierten Bildes
	 * @return	Skaliertes Bild
	 */
	private Image getDirectScaledImage(final BufferedImage original, final int width, final int height) {
		int oldW=original.getWidth();
		int oldH=original.getHeight();
		int newW, newH;
		if (oldW/oldH*height>width) {
			newH=(int)FastMath.round(((double)oldH)/oldW*width);
			newW=width;
		} else {
			newW=(int)FastMath.round(((double)oldW)/oldH*height);
			newH=height;
		}

		return original.getScaledInstance(newW,newH,Image.SCALE_SMOOTH);
	}

	/**
	 * Liefert eine auf vorgegebene Maße skalierte Fassung eines Bildes
	 * @param originalHash	Hash des Ausgangsbildes (kann <code>null</code> oder leer sein, dann wird der Hash neu berechnet)
	 * @param original	Ausgangsbild
	 * @param width	Gewünschte Breite
	 * @param height	Gewünschte Höhe
	 * @return	2-elementiges Array aus: 1. skaliertem Bild (<code>BufferedImage</code>), 2. Hash des Ausgangsbildes (<code>String</code>), der bei Folgeaufrufen übergeben werden kann, um das Bild im Cache schneller zu finden
	 */
	public Object[] getScaledImage(final String originalHash, final BufferedImage original, final int width, final int height) {
		if (width<=0 || height<=0) return new Object[]{new BufferedImage(1,1,BufferedImage.TYPE_4BYTE_ABGR),""};

		/* Hash berechnen */
		final String hash=(originalHash==null || originalHash.isEmpty())?getHash(original):originalHash;

		/* Gibt's das passend skalierte Bild schon im Cache? */
		final BufferedImage fromCache=getFromCache(hash,width,height);
		if (fromCache!=null) return new Object[]{fromCache,hash};

		/* Skalieren (unter Beibehaltung des Seitenverhältnisses) */
		final Image scaledTooSmallImage=getDirectScaledImage(original,width,height);

		/* In passend großes Bild einfügen */
		final BufferedImage scaledImage=new BufferedImage(width,height,BufferedImage.TYPE_4BYTE_ABGR);
		scaledImage.getGraphics().drawImage(scaledTooSmallImage,(width-scaledTooSmallImage.getWidth(null))/2,(height-scaledTooSmallImage.getHeight(null))/2,null);

		/* In Cache speichern und zurück geben */
		storeToCache(hash,scaledImage);
		return new Object[]{scaledImage,hash};
	}

	/**
	 * Liefert eine auf vorgegebene Maße skalierte Fassung eines Bildes
	 * @param original	Ausgangsbild
	 * @param width	Gewünschte Breite
	 * @param height	Gewünschte Höhe
	 * @return	Skaliertes Bild
	 */
	public BufferedImage getScaledImage(final BufferedImage original, final int width, final int height) {
		final Object[] data=getScaledImage(null,original,width,height);
		return (BufferedImage)data[0];
	}

	/**
	 * Liefert eine auf einen vorgegebenen Faktor skalierte Fassung eines Bildes
	 * @param original	Ausgangsbild
	 * @param zoom	Zoomfaktor gegenüber der Originalbild
	 * @return	Skaliertes Bild
	 */
	public BufferedImage getScaledImage(final BufferedImage original, final double zoom) {
		return getScaledImage(original,(int)FastMath.round(original.getWidth()*zoom),(int)FastMath.round(original.getHeight()*zoom));
	}
}