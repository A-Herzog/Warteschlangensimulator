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

import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.imageio.ImageIO;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import language.Language;

/**
 * Diese Klasse hält die benutzerdefinierten Animationsicons vor.
 * @author Alexander Herzog
 */
public final class ModelAnimationImages implements Cloneable {
	/**
	 * Name des XML-Elements, das die Animationsicons enthält
	 */
	public static String[] XML_NODE_NAME=new String[] {"ModellAnimationsBilder"};

	/** Namen der lokal gespeicherten Bilder */
	private final List<String> names;
	/** Hashwerte zu den Bilddaten */
	private final List<String> imagesHashes;
	/** Bilddaten */
	private final List<BufferedImage> images;

	/**
	 * Konstruktor der Klasse
	 */
	public ModelAnimationImages() {
		names=new ArrayList<>();
		imagesHashes=new ArrayList<>();
		images=new ArrayList<>();
	}

	/**
	 * Löscht alle momentan vorhandenen benutzerdefinierten Animationsicons.
	 */
	public void clear() {
		names.clear();
		imagesHashes.clear();
		images.clear();
	}

	/**
	 * Vergleicht zwei Animationsicons-Listen
	 * @param otherImages	Anderes Animationsicons-Listen-Objekt für den Vergleich
	 * @return	Liefert <code>true</code>, wenn die beiden Animationsicons-Listen-Objekt inhaltlich identisch sind
	 */
	public boolean equalsModelAnimationImages(final ModelAnimationImages otherImages) {
		if (otherImages==null) return false;

		if (!Objects.deepEquals(names,otherImages.names)) return false;
		if (!Objects.deepEquals(imagesHashes,otherImages.imagesHashes)) return false;
		/* Die Bilder selbst werden aus Zeitgründen nicht verglichen, nur die Hashes. */

		return true;
	}

	/**
	 * Erstellt eine Kopie des Bildes
	 * @param original	Ausgangsbild
	 * @return	Kopie des Bildes
	 */
	public static BufferedImage copyImage(final BufferedImage original) {
		if (original==null) return new BufferedImage(0,0,BufferedImage.TYPE_4BYTE_ABGR);
		final ColorModel cm=original.getColorModel();
		boolean isAlphaPremultiplied=cm.isAlphaPremultiplied();
		final WritableRaster raster=original.copyData(null);
		return new BufferedImage(cm,raster,isAlphaPremultiplied,null);
	}

	/**
	 * Kopiert die Daten eines anderen Animationsicons-Listen-Objekts in dieses (und ersetzt dabei evtl. vorhandene bisherige Daten)
	 * @param otherImages	Animationsicons-Listen-Objekt aus dem die Daten kopiert werden sollen
	 */
	public void setDataFrom(final ModelAnimationImages otherImages) {
		clear();

		if (otherImages!=null) {
			names.addAll(otherImages.names);
			imagesHashes.addAll(otherImages.imagesHashes);
			images.addAll(otherImages.images.stream().map(image->copyImage(image)).collect(Collectors.toList()));
		}
	}

	@Override
	public ModelAnimationImages clone() {
		final ModelAnimationImages clone=new ModelAnimationImages();
		clone.setDataFrom(this);
		return clone;
	}

	/**
	 * Liefert die Anzahl an gespeicherten benutzerdefinierten Animationsicons
	 * @return	Anzahl an gespeicherten benutzerdefinierten Animationsicons
	 */
	public int size() {
		return names.size();
	}

	/**
	 * Liefert die Namen der hier lokal gespeicherten Bilder
	 * @return	Namen der hier lokal gespeicherten Bilder
	 */
	public String[] getLocalNames() {
		return names.toArray(String[]::new);
	}

	/**
	 * Liefert die Namen der vordefinierten Bilder.<br>
	 * Diese Liste ist nicht für die Anzeige geeignet; es werden
	 * die lokalisierten und die sprachunabhängigen Namen geliefert.
	 * Die Liste ist dafür gedacht zu prüfen, welche Namen schon blockiert sind.
	 * @return	Liste an auf globaler Basis blockierten Namen
	 */
	public String[] getGlobalNames() {
		final List<String> names=new ArrayList<>();
		for (Map.Entry<String,String> entry: AnimationImageSource.ICONS.entrySet()) {
			names.add(entry.getKey());
			names.add(entry.getValue());
		}
		return names.toArray(String[]::new);
	}

	/**
	 * Liefert ein benutzerdefiniertes Bild auf Basis eines Namens
	 * @param name	Name des Bildes, welches geliefert werden soll
	 * @return	Bild oder <code>null</code>, wenn es kein Bild zu dem Namen gibt
	 */
	public BufferedImage getLocal(final String name) {
		final int index=names.indexOf(name);
		if (index<0) return null;
		return images.get(index);
	}

	/**
	 * Liefert ein benutzerdefiniertes Bild in skalierter Form auf Basis eines Namens
	 * @param name	Name des Bildes, welches geliefert werden soll
	 * @param size	Größe, auf die skaliert werden soll
	 * @return	Bild oder <code>null</code>, wenn es kein Bild zu dem Namen gibt
	 */
	public BufferedImage getLocalSize(final String name, final int size) {
		final BufferedImage image=getLocal(name);
		if (image==null) return null;
		return AnimationImageSource.scaleImage(image,size);
	}

	/**
	 * Liefert eine base64-codierte Fassung eines der Bilder aus {@link #images}
	 * @param index	Index des Bildes aus {@link #images}
	 * @return	base64-codierte Fassung des Bildes
	 */
	private String getLocalBase64(final int index) {
		if (index<0) return null;

		try {
			final ByteArrayOutputStream stream=new ByteArrayOutputStream();
			ImageIO.write(images.get(index),"png",stream);
			return new String(Base64.getEncoder().encode(stream.toByteArray()));
		} catch (IOException e) {return null;}
	}

	/*
	public String getLocalBase64(final String name) {
		final int index=names.indexOf(name);
		if (index<0) return null;
		return getLocalBase64(index);
	}
	 */

	/**
	 * Prüft, ob ein gegebener Name entweder für ein hier gespeichertes Bild oder für die globalen Bilddaten bereits vergeben ist
	 * @param name	Zu prüfender Name
	 * @return	Gibt <code>true</code> zurück, wenn der Name bereits verwendet wird
	 */
	public boolean nameInUse(final String name) {
		if (names.contains(name)) return true;
		if (Arrays.asList(getGlobalNames()).contains(name)) return true;
		return false;
	}

	/**
	 * Versucht ein Bild in die Animationsicons-Liste aufzunehmen
	 * @param name	Name des Bildes
	 * @param image	Bilddaten
	 * @return	Gibt <code>true</code> zurück, wenn das Bild gespeichert werden konnte.
	 */
	public boolean set(final String name, final BufferedImage image) {
		if (Arrays.asList(getGlobalNames()).contains(name)) return false; /* keine globalen Namen überschreiben */

		final BufferedImage imageClone=copyImage(image);
		final int index=names.indexOf(name);
		if (index<0) {
			names.add(name);
			imagesHashes.add(ScaledImageCache.getHash(image));
			images.add(imageClone);
		} else {
			imagesHashes.set(index,ScaledImageCache.getHash(image));
			images.set(index,imageClone);
		}

		return true;
	}

	/**
	 * Entfernt ein Bild aus der Animationsicons-Liste
	 * @param name	Name des Bildes, das entfernt werden soll
	 */
	public void remove(final String name) {
		remove(names.indexOf(name));
	}

	/**
	 * Entfernt ein Bild aus der Animationsicons-Liste
	 * @param index	Index des Bildes in der Liste der Bilder
	 */
	public void remove(final int index) {
		if (index<0 || index>=names.size()) return;
		names.remove(index);
		imagesHashes.remove(index);
		images.remove(index);
	}

	/**
	 * Speichert die Animationsicons-Liste in einem xml-Knoten
	 * @param doc	Übergeordnetes xml-Dokument
	 * @param parent	Knoten, in dem die Daten des Objekts gespeichert werden sollen
	 */
	public void addDataToXML(final Document doc, final Element parent) {
		if (names.isEmpty()) return;

		final Element node=doc.createElement(XML_NODE_NAME[0]);
		parent.appendChild(node);

		for (int i=0;i<names.size();i++) {
			final String imageString=getLocalBase64(i);
			if (imageString!=null) {
				final Element sub=doc.createElement(Language.trPrimary("Animation.XML.Image"));
				node.appendChild(sub);
				sub.setAttribute(Language.tr("Animation.XML.Image.Name"),names.get(i));
				sub.setTextContent(imageString);
			}
		}
	}

	/**
	 * Wandelt ein base64-encodiertes Bild in ein Bildobjekt um
	 * @param imageString	Base64-encodiertes Bild
	 * @return	Bildobjekt
	 */
	private BufferedImage imageFromBase64(final String imageString) {
		try {
			final boolean useCache=ImageIO.getUseCache();
			try {
				ImageIO.setUseCache(false); /* Wird benötigt, wenn im Stream nicht gesprungen werden kann, was bei einem ByteArrayInputStream nun definitiv möglich ist.  */
				return ImageIO.read(new ByteArrayInputStream(Base64.getDecoder().decode(imageString)));
			} finally {
				ImageIO.setUseCache(useCache);
			}
		} catch (IOException | IllegalArgumentException e) {return null;}
	}

	/**
	 * Fügt ein Bild zu der Liste der verfügbaren Bilder hinzu.
	 * @param name	Name des Bildes
	 * @param imageString	Base64-codierte Daten des Bildes
	 * @see #loadFromXML(Element)
	 */
	private void addLoadedImage(final String name, final String imageString) {
		if (name==null || name.isEmpty()) return;
		if (imageString==null || imageString.isEmpty()) return;
		if (nameInUse(name)) return;

		final BufferedImage image=imageFromBase64(imageString);
		if (image!=null) set(name,image);
	}

	/**
	 * Versucht die Daten der Animationsicons-Liste aus einem xml-Element zu laden
	 * @param node	XML-Element, das die Animationsicons-Liste enthält
	 * @return	Tritt ein Fehler auf, so wird die Fehlermeldung als String zurückgegeben. Im Erfolgsfall wird <code>null</code> zurückgegeben.
	 */
	public String loadFromXML(final Element node) {
		clear();

		final NodeList l=node.getChildNodes();
		for (int i=0; i<l.getLength();i++) {
			if (!(l.item(i) instanceof Element)) continue;
			final Element e=(Element)l.item(i);

			if (Language.trAll("Animation.XML.Image",e.getNodeName())) {
				final String name=Language.trAllAttribute("Animation.XML.Image.Name",e);
				addLoadedImage(name,e.getTextContent());
				continue;
			}
		}

		return null;
	}
}