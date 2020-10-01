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
package ui.modeleditor.elements;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Base64;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import language.Language;
import simulator.editmodel.EditModel;
import ui.images.Images;
import ui.modeleditor.ModelClientData;
import ui.modeleditor.ModelSequences;
import ui.modeleditor.ModelSurface;
import ui.modeleditor.ScaledImageCache;
import ui.modeleditor.coreelements.ModelElement;
import ui.modeleditor.coreelements.ModelElementDecoration;
import ui.modeleditor.outputbuilder.HTMLOutputBuilder;
import ui.modeleditor.outputbuilder.SpecialOutputBuilder;

/**
 * Zeigt auf der Zeichenfläche ein statisches Bild als Dekoration an.
 * @author Alexander Herzog
 */
public class ModelElementImage extends ModelElementDecoration {
	private BufferedImage image; /* Wird Immutable behandelt, kann daher als Referenz an Clone von ModelElementImage weitergegeben werden. Bei Änderungen wird hier ein neuer Wert gesetzt. */
	private String imageHash;

	/**
	 * Konstruktor der Klasse <code>ModelElementPosition</code>
	 * @param model	Modell zu dem dieses Element gehören soll (kann später nicht mehr geändert werden)
	 * @param surface	Zeichenfläche zu dem dieses Element gehören soll (kann später nicht mehr geändert werden)
	 */
	public ModelElementImage(final EditModel model, final ModelSurface surface) {
		super(model,surface,new Dimension(50,50));
		lineWidth=1;
		loadDummyImage();
	}

	/**
	 * Icon, welches im "Element hinzufügen"-Dropdown-Menü angezeigt werden soll.
	 * @return	Icon für das Dropdown-Menü
	 */
	@Override
	public URL getAddElementIcon() {
		return Images.MODELEDITOR_ELEMENT_IMAGE.getURL();
	}

	/**
	 * Tooltip für den "Element hinzufügen"-Dropdown-Menü-Eintrag.
	 * @return Tooltip für den "Element hinzufügen"-Dropdown-Menüeintrag
	 */
	@Override
	public String getToolTip() {
		return Language.tr("Surface.Image.Tooltip");
	}

	/**
	 * Name des Elementtyps für die Anzeige im Kontextmenü
	 * @return	Name des Elementtyps
	 */
	@Override
	public String getContextMenuElementName() {
		return Language.tr("Surface.Image.Name");
	}

	/**
	 * Liefert das aktuelle Bild zurück
	 * @return	Aktuelles Bild
	 */
	public BufferedImage getImage() {
		requireImageLoaded();
		return image;
	}

	/**
	 * Stellt ein neues Bild ein
	 * @param image	Neues Bild
	 */
	public void setImage(final BufferedImage image) {
		requireImageLoaded();
		if (image!=null) this.image=copyImage(image);
		imageHash=null;
		fireChanged();
	}

	/**
	 * Minimal einstellbare Linienbreite (hier: 0)
	 * @return	Minimale Breite der Linie
	 */
	@Override
	protected int getMinLineWidth() {
		return 0;
	}

	/**
	 * Überprüft, ob das Element mit dem angegebenen Element inhaltlich identisch ist.
	 * @param element	Element mit dem dieses Element verglichen werden soll.
	 * @return	Gibt <code>true</code> zurück, wenn die beiden Elemente identisch sind.
	 */
	@Override
	public boolean equalsModelElement(ModelElement element) {
		if (!super.equalsModelElement(element)) return false;

		if (!(element instanceof ModelElementImage)) return false;

		((ModelElementImage)element).requireImageLoaded();
		requireImageLoaded();

		if (!(((ModelElementImage)element).image==null && image==null)) {
			if (((ModelElementImage)element).image==null || image==null) return false;
			if (!Objects.equals(((ModelElementImage)element).imageHash,imageHash)) return false;
			if (!ScaledImageCache.compare(((ModelElementImage)element).image,image)) return false;
		}

		return true;
	}

	private static BufferedImage copyImage(final BufferedImage source) {
		final ColorModel cm=source.getColorModel();
		final boolean isAlphaPremultiplied=cm.isAlphaPremultiplied();
		final WritableRaster raster=source.copyData(null);
		return new BufferedImage(cm,raster,isAlphaPremultiplied,null);
	}

	/**
	 * Überträgt die Einstellungen von dem angegebenen Element auf dieses.
	 * @param element	Element, von dem alle Einstellungen übernommen werden sollen
	 */
	@Override
	public void copyDataFrom(ModelElement element) {
		super.copyDataFrom(element);
		if (element instanceof ModelElementImage) {
			((ModelElementImage)element).requireImageLoaded(); /* Die Quelle muss mit dem Laden fertig sein. */
			final BufferedImage source=((ModelElementImage)element).image;
			/* dauert lange: image=copyImage(source); */
			image=source; /* Dürfen wir, da in der Quelle bei Änderungen ein neues Objekt im image-Feld hinterlegt wird. Das bestehende Objekt selbst wird inhaltlich nie geändert. */
			imageHash=null;
		}
	}

	/**
	 * Erstellt eine Kopie des Elements
	 * @param model	Modell zu dem das kopierte Element gehören soll.
	 * @param surface	Zeichenfläche zu der das kopierte Element gehören soll.
	 * @return	Kopiertes Element
	 */
	@Override
	public ModelElementImage clone(final EditModel model, final ModelSurface surface) {
		final ModelElementImage element=new ModelElementImage(model,surface);
		requireImageLoaded();
		element.copyDataFrom(this);
		return element;
	}

	/**
	 * Stellt das Standard-Vorgabe-Bild ein.
	 */
	public void loadDummyImage() {
		requireImageLoaded();
		final URL url=Images.MODELEDITOR_ELEMENT_IMAGE_EXAMPLE.getURL();
		if (url==null) {
			image=new BufferedImage(100,100,BufferedImage.TYPE_4BYTE_ABGR);
		} else {
			try {
				image=ImageIO.read(url);
			} catch (IOException e) {
				image=new BufferedImage(100,100,BufferedImage.TYPE_4BYTE_ABGR);
			}
		}
		imageHash=null;
		fireChanged();
	}

	/**
	 * Lädt ein Bild aus einer Datei
	 * @param imageFile	Zu ladende Bilddatei
	 * @return	Gibt <code>true</code> zurück, wenn das Bild erfolgreich geladen werden konnte
	 */
	public boolean loadImageFromFile(final File imageFile) {
		requireImageLoaded();
		try {
			image=ImageIO.read(imageFile);
			imageHash=null;
		} catch (IOException e) {
			return false;
		}
		fireChanged();
		return true;
	}

	/**
	 * Prüft, ob sich ein Punkt innerhalb des Elements befindet
	 * @param point	Punkt, bei dem geprüft werden soll, ob er sich innerhalb des Elements befindet
	 * @param zoom	Zoomfaktor
	 * @return	Gibt <code>true</code> zurück, wenn sich der Punkt innerhalb des Elements befindet
	 */
	@Override
	public boolean containsPoint(final Point point, final double zoom) {
		if (super.containsPoint(point,zoom)) return true;

		final Point p=getPosition(true);
		final Dimension s=getSize();

		if (isNearLine(new Point(p.x,p.y),new Point(p.x+s.width,p.y),point,zoom,lineWidth)) return true;
		if (isNearLine(new Point(p.x+s.width,p.y),new Point(p.x+s.width,p.y+s.height),point,zoom,lineWidth)) return true;
		if (isNearLine(new Point(p.x+s.width,p.y+s.height),new Point(p.x,p.y+s.height),point,zoom,lineWidth)) return true;
		if (isNearLine(new Point(p.x,p.y+s.height),new Point(p.x,p.y),point,zoom,lineWidth)) return true;

		return false;
	}

	/**
	 * Liefert die Position eines bestimmten Randpunktes
	 * @param index	0-basierender Index des Randpunktes
	 * @return	Position des Randpunktes oder <code>null</code>, wenn der Index außerhalb des gültigen Bereichs liegt
	 */
	@Override
	public Point getBorderPointPosition(final int index) {
		final Point p=getPosition(true);
		final Dimension s=getSize();

		switch (index) {
		case 0: return new Point(p);
		case 1: return new Point(p.x+s.width,p.y);
		case 2: return new Point(p.x+s.width,p.y+s.height);
		case 3: return new Point(p.x,p.y+s.height);
		default: return null;
		}
	}

	/**
	 * Setzt die Position eines Randpunktes
	 * @param index	0-basierender Index des Randpunktes
	 * @param point	Neue Position des Randpunktes
	 */
	@Override
	public void setBorderPointPosition(final int index, final Point point) {
		final Point p1=getPosition(true);
		final Point p2=getLowerRightPosition();

		switch (index) {
		case 0:
			setPosition(point);
			setSize(new Dimension(p2.x-point.x,p2.y-point.y));
			break;
		case 1:
			setPosition(new Point(p1.x,point.y));
			setSize(new Dimension(point.x-p1.x,p2.y-point.y));
			break;
		case 2:
			setSize(new Dimension(point.x-p1.x,point.y-p1.y));
			break;
		case 3:
			setPosition(new Point(point.x,p1.y));
			setSize(new Dimension(p2.x-point.x,point.y-p1.y));
			break;
		}
	}

	private void drawImage(final Graphics graphics, final double zoom) {
		requireImageLoaded();
		if (image==null) return;

		final Point p1=getPosition(true);
		final Dimension s1=getSize();

		final Point p2=new Point(p1.x+Math.min(0,s1.width),p1.y+Math.min(0,s1.height));
		final Dimension s2=new Dimension(Math.abs(s1.width),Math.abs(s1.height));

		final Point p3=new Point((int)Math.round(p2.x*zoom),(int)Math.round(p2.y*zoom));
		final Dimension s3=new Dimension((int)Math.round(s2.width*zoom),(int)Math.round(s2.height*zoom));

		final Object[] data=ScaledImageCache.getScaledImageCache().getScaledImage(imageHash,image,s3.width,s3.height);
		final BufferedImage scaledImage=(BufferedImage)data[0];
		imageHash=(String)data[1];

		graphics.drawImage(scaledImage,p3.x,p3.y,null);
	}

	private void drawFrame(final Graphics graphics, final double zoom, final boolean showSelectionFrames) {
		if (lineWidth==0 && !isSelected() && !isSelectedArea()) return;

		final Point p=getPosition(true);
		final Dimension s=getSize();

		final Graphics2D g2=(Graphics2D)graphics;
		final Stroke saveStroke=g2.getStroke();

		Color lineColor=color;
		g2.setStroke(new BasicStroke((float)(lineWidth*zoom)));

		if (isSelected() && showSelectionFrames) {
			lineColor=Color.GREEN;
			g2.setStroke(new BasicStroke((float)(Math.max(lineWidth,3)*zoom)));
		} else {
			if (isSelectedArea() && showSelectionFrames) {
				lineColor=Color.BLUE;
				g2.setStroke(new BasicStroke((float)(Math.max(lineWidth,3)*zoom)));
			}
		}

		Rectangle rectangle=new Rectangle((int)Math.round(Math.min(p.x,p.x+s.width)*zoom),(int)Math.round(Math.min(p.y,p.y+s.height)*zoom),(int)Math.round(Math.abs(s.width)*zoom),(int)Math.round(Math.abs(s.height)*zoom));
		g2.setColor(lineColor);
		g2.draw(rectangle);

		if (isSelected() && showSelectionFrames) {
			drawBorderBox(g2,p,zoom);
			drawBorderBox(g2,new Point(p.x+s.width,p.y),zoom);
			drawBorderBox(g2,new Point(p.x,p.y+s.height),zoom);
			drawBorderBox(g2,new Point(p.x+s.width,p.y+s.height),zoom);
		}
		g2.setStroke(saveStroke);
	}

	/**
	 * Zeichnet das Element in ein <code>Graphics</code>-Objekt
	 * @param graphics	<code>Graphics</code>-Objekt in das das Element eingezeichnet werden soll
	 * @param drawRect	Tatsächlich sichtbarer Ausschnitt
	 * @param zoom	Zoomfaktor
	 * @param showSelectionFrames	Rahmen anzeigen, wenn etwas ausgewählt ist
	 */
	@Override
	public void drawToGraphics(final Graphics graphics, final Rectangle drawRect, final double zoom, final boolean showSelectionFrames) {
		setClip(graphics,drawRect,null);

		/* Bild zeichnen */
		drawImage(graphics,zoom);

		/* Rahmen zeichnen */
		drawFrame(graphics,zoom,showSelectionFrames);
	}

	/**
	 * Liefert ein <code>Runnable</code>-Objekt zurück, welches aufgerufen werden kann, wenn die Eigenschaften des Elements verändert werden sollen.
	 * @param owner	Übergeordnetes Fenster
	 * @param readOnly	Wird dieser Parameter auf <code>true</code> gesetzt, so wird die "Ok"-Schaltfläche deaktiviert
	 * @param clientData	Kundendaten-Objekt
	 * @param sequences	Fertigungspläne-Liste
	 * @return	<code>Runnable</code>-Objekt zur Einstellung der Eigenschaften oder <code>null</code>, wenn das Element keine Eigenschaften besitzt
	 */
	@Override
	public Runnable getProperties(final Component owner, final boolean readOnly, final ModelClientData clientData, final ModelSequences sequences) {
		return ()->{
			requireImageLoaded();
			new ModelElementImageDialog(owner,ModelElementImage.this,readOnly);
		};
	}

	/**
	 * Liefert den jeweiligen xml-Element-Namen für das Modell-Element
	 * @return	xml-Element-Namen, der diesem Modell-Element zugeordnet werden soll
	 */
	@Override
	public String[] getXMLNodeNames() {
		return Language.trAll("Surface.Image.XML.Root");
	}

	/**
	 * Speichert die Eigenschaften des Modell-Elements als Untereinträge eines xml-Knotens
	 * @param doc	Übergeordnetes xml-Dokument
	 * @param node	Übergeordneter xml-Knoten, in dessen Kindelementen die Daten des Objekts gespeichert werden sollen
	 */
	@Override
	protected void addPropertiesDataToXML(final Document doc, final Element node) {
		super.addPropertiesDataToXML(doc,node);

		requireImageLoaded();

		if (image!=null) {
			Element sub=doc.createElement(Language.trPrimary("Surface.Image.XML.Data"));
			node.appendChild(sub);
			try {
				final ByteArrayOutputStream stream=new ByteArrayOutputStream();
				ImageIO.write(image,"png",stream);
				sub.setTextContent(new String(Base64.getEncoder().encode(stream.toByteArray())));
			} catch (IOException e) {}
		}
	}

	private static final boolean USE_BACKGROUND_LOAD=true;

	private static final ExecutorService loadService=new ThreadPoolExecutor(0,Integer.MAX_VALUE,60L,TimeUnit.SECONDS,new SynchronousQueue<>(),new ThreadFactory() {
		@Override public Thread newThread(Runnable r) {return new Thread(r,"Model Element Image Loader");}
	});
	private Future<Integer> loadFuture=null;

	/**
	 * Beendet explizit die Threads, die zum Hintergrundladen von Bildern verwendet werden.
	 * (Ist nur für den Kommandozeilenbetrieb notwendig.)
	 */
	public static void shutdownBackgroundLoadService() {
		if (USE_BACKGROUND_LOAD && loadService!=null) loadService.shutdown();
	}

	private void requireImageLoaded() {
		if (loadFuture==null) return;
		try {
			loadFuture.get();
		} catch (InterruptedException | ExecutionException e) {
		}
		loadFuture=null;
	}

	/**
	 * Lädt eine einzelne Einstellung des Modell-Elements aus einem einzelnen xml-Element.
	 * @param name	Name des xml-Elements
	 * @param content	Inhalt des xml-Elements als Text
	 * @param node	xml-Element, aus dem das Datum geladen werden soll
	 * @return	Tritt ein Fehler auf, so wird die Fehlermeldung als String zurückgegeben. Im Erfolgsfall wird <code>null</code> zurückgegeben.
	 */
	@Override
	protected String loadProperty(final String name, final String content, final Element node) {
		String error=super.loadProperty(name,content,node);
		if (error!=null) return error;

		if (Language.trAll("Surface.Image.XML.Data",name)) {

			final Callable<Integer> loader=new Callable<Integer>() {
				@Override
				public Integer call() throws Exception {
					try {
						final ByteArrayInputStream stream=new ByteArrayInputStream(Base64.getDecoder().decode(content));
						final BufferedImage image=ImageIO.read(stream);
						if (image!=null) ModelElementImage.this.image=image;
					} catch (IOException | IllegalArgumentException e) {}
					return null;
				}
			};

			if (USE_BACKGROUND_LOAD) {
				loadFuture=loadService.submit(loader);
			} else {
				try {
					loader.call();
				} catch (Exception e) {}
			}

			return null;
		}

		return null;
	}

	@Override
	public String getHelpPageName() {
		return "ModelElementImage";
	}

	private String getHTMLDrawImage(final HTMLOutputBuilder outputBuilder) {
		final StringBuilder sb=new StringBuilder();

		sb.append("function drawPlainImage(rect,borderColor,borderWidth,image) {\n");

		sb.append("  if (borderWidth>0) {\n");
		sb.append("    context.strokeStyle=borderColor;\n");
		sb.append("    context.lineWidth=borderWidth;\n");
		sb.append("    context.beginPath();\n");
		sb.append("    context.moveTo(rect.x,rect.y);\n");
		sb.append("    context.lineTo(rect.x+rect.w,rect.y);\n");
		sb.append("    context.lineTo(rect.x+rect.w,rect.y+rect.h);\n");
		sb.append("    context.lineTo(rect.x,rect.y+rect.h);\n");
		sb.append("    context.closePath();\n");
		sb.append("    context.stroke();\n");
		sb.append("  }\n");

		sb.append("  var img=new Image();\n");
		sb.append("  img.src=image;\n");
		sb.append("  img.onload=function(){context.drawImage(img,rect.x,rect.y,rect.w,rect.h);};\n");

		sb.append("}\n");

		return sb.toString();
	}

	private void specialOutputHTML(final HTMLOutputBuilder outputBuilder) {
		outputBuilder.addJSUserFunction("drawPlainImage",builder->getHTMLDrawImage(builder));

		final Point p=getPosition(true);
		final Dimension s=getSize();

		String image;
		try {
			requireImageLoaded();
			final ByteArrayOutputStream out=new ByteArrayOutputStream();
			ImageIO.write(this.image,"png",out);
			final byte[] bytes=out.toByteArray();
			final String base64bytes=Base64.getEncoder().encodeToString(bytes);
			image="data:image/png;base64,"+base64bytes;
		} catch (IOException e) {
			image="";
		}

		outputBuilder.outputBody.append("drawPlainImage({x: "+p.x+",y: "+p.y+", w:"+s.width+",h: "+s.height+"},\""+HTMLOutputBuilder.colorToHTML(color)+"\","+lineWidth+",\""+image+"\");\n");
	}

	/**
	 * Zeichnet das Element in einem {@link SpecialOutputBuilder}
	 * @param outputBuilder	Builder, der die Daten aufnehmen soll
	 */
	@Override
	public void specialOutput(final SpecialOutputBuilder outputBuilder) {
		if (outputBuilder instanceof HTMLOutputBuilder) specialOutputHTML((HTMLOutputBuilder)outputBuilder);
	}
}