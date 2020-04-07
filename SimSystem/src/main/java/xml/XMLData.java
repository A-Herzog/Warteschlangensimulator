package xml;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.math3.distribution.AbstractRealDistribution;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import mathtools.NumberTools;
import mathtools.distribution.tools.DistributionTools;
import xml.XMLTools.FileType;

/**
 * Diese Klasse stellt eine abstrakte Basis für Klassen, die ihre Daten aus xml-Dateien laden möchten bzw. in diesen speichern möchten, dar.
 * @author Alexander Herzog
 * @version 1.7
 */
public abstract class XMLData {
	/**
	 * Fehlermeldung, die zurückgegeben wird, wenn das Wurzelelement der XML-Datei einen falschen Namen hat.
	 * @see #loadFromXML(Element)
	 */
	public static String errorRootElementName="Das Wurzelelement der zu ladenden Daten muss \"%s\" heißen, ist hier aber \"%s\".";

	/**
	 * Fehlermeldung, die zurückgegeben wird, wenn während der Laden des Modells der Speicher erschöpft war.
	 * @see #loadFromXML(Element)
	 */
	public static String errorOutOfMemory="Aufgrund der Speicherlimitierung durch die Java VM kann die xml-Datei nicht geladen werden.\nErhöhen Sie das Speichermaximum über den Parameter -Xmx.";

	/**
	 * Liefert den Namen des Wurzel-Elements für xml-Dateien dieses Typs
	 * @return	Alle zulässigen Namen für das Wurzel-Element für xml-Dateien dieses Typs
	 */
	public abstract String[] getRootNodeNames();

	/**
	 * Setzt alle Datenfelder auf Vorgabewerte zurück.
	 * (Wird vom Konstruktor und vor dem Laden von Daten aufgerufen.)
	 */
	protected void resetData() {}

	/**
	 * Versucht Daten aus der angegebenen Datei zu laden.
	 * Das Dateiformat wird dabei von der Dateiendung abgeleitet.
	 * @param file	Dateiname der Datei, aus der die Daten geladen werden sollen
	 * @return	Tritt ein Fehler auf, so wird die Fehlermeldung als String zurückgegeben. Im Erfolgsfall wird <code>null</code> zurückgegeben.
	 */
	public final String loadFromFile(final File file) {
		XMLTools xml=new XMLTools(file);
		Element root=xml.load();
		if (root==null) return xml.getError();
		return loadFromXML(root);
	}

	/**
	 * Versucht Daten aus der angegebenen Datei zu laden.
	 * @param file	Dateiname der Datei, aus der die Daten geladen werden sollen
	 * @param fileType	Dateiformat
	 * @return	Tritt ein Fehler auf, so wird die Fehlermeldung als String zurückgegeben. Im Erfolgsfall wird <code>null</code> zurückgegeben.
	 * @see XMLTools.FileType
	 */
	public final String loadFromFile(final File file, final XMLTools.FileType fileType) {
		XMLTools xml=new XMLTools(file,fileType);
		Element root=xml.load();
		if (root==null) return xml.getError();
		return loadFromXML(root);
	}

	/**
	 * Versucht Daten aus dem angegebenen Stream zu laden
	 * @param stream	InputStream, aus dem die Daten geladen werden sollen
	 * @param fileType	Dateiformat
	 * @return	Tritt ein Fehler auf, so wird die Fehlermeldung als String zurückgegeben. Im Erfolgsfall wird <code>null</code> zurückgegeben.
	 */
	public final String loadFromStream(final InputStream stream, final FileType fileType) {
		XMLTools xml=new XMLTools(stream,fileType);
		Element root=xml.load();
		if (root==null) return xml.getError();
		return loadFromXML(root);
	}

	/**
	 * Versucht Daten aus dem angegebenen Stream zu laden
	 * @param stream	InputStream, aus dem die Daten geladen werden sollen
	 * @return	Tritt ein Fehler auf, so wird die Fehlermeldung als String zurückgegeben. Im Erfolgsfall wird <code>null</code> zurückgegeben.
	 */
	public final String loadFromStream(final InputStream stream) {
		return loadFromStream(stream,FileType.XML);
	}

	/**
	 * Versucht Daten aus dem angegebenen String zu laden
	 * @param text	String, aus dem die Daten geladen werden sollen
	 * @return	Tritt ein Fehler auf, so wird die Fehlermeldung als String zurückgegeben. Im Erfolgsfall wird <code>null</code> zurückgegeben.
	 */
	public final String loadFromString(final String text) {
		XMLTools xml=new XMLTools(text);
		Element root=xml.load();
		if (root==null) return xml.getError();
		return loadFromXML(root);
	}

	/**
	 * Versucht Daten aus dem angegebenen xml-Element zu laden
	 * @param node	xml-Knoten, aus dem die Daten geladen werden sollen
	 * @return	Tritt ein Fehler auf, so wird die Fehlermeldung als String zurückgegeben. Im Erfolgsfall wird <code>null</code> zurückgegeben.
	 */
	public final String loadFromXML(final Element node) {
		boolean ok=false;
		final String nodeName=node.getNodeName();
		for (String test: getRootNodeNames()) if (nodeName.equalsIgnoreCase(test)) {ok=true; break;}
		if (!ok) return String.format(errorRootElementName,getRootNodeNames()[0],node.getNodeName());

		resetData();

		final NodeList l=node.getChildNodes();
		final int length=l.getLength();
		for (int i=0; i<length;i++) {
			final Node sub=l.item(i);
			if (!(sub instanceof Element)) continue;
			final Element e=(Element)sub;
			try {
				final String error=loadProperty(e.getNodeName(),e.getTextContent(),e);
				if (error!=null) return error;
			} catch (OutOfMemoryError e2) {
				return errorOutOfMemory;
			}
		}

		processLoadedData();

		return null;
	}

	/**
	 * Führt optionale Verarbeitungen nach dem Laden der Daten aus einer Datei aus.
	 */
	protected void processLoadedData() {}

	/**
	 * Lädt Daten aus einem einzelnen xml-Element.
	 * @param name	Name des xml-Elements
	 * @param text	Inhalt des xml-Elements als Text
	 * @param node	xml-Element, aus dem das Datum geladen werden soll
	 * @return	Tritt ein Fehler auf, so wird die Fehlermeldung als String zurückgegeben. Im Erfolgsfall wird <code>null</code> zurückgegeben.
	 */
	protected abstract String loadProperty(final String name, final String text, final Element node);

	/**
	 * Speichert die Daten des Objekts in der angegebenen Datei.
	 * Das Dateiformat wird dabei von der Dateiendung abgeleitet.
	 * @param file	Dateiname der Datei, in der die Daten gespeichert werden sollen
	 * @return	Gibt an, ob die Daten erfolgreich gespeichert werden konnten.
	 */
	public final boolean saveToFile(final File file) {
		XMLTools xml=new XMLTools(file);
		Element root=null;
		try {
			root=xml.generateRoot(getRootNodeNames()[0]);
		} catch (Exception e) {return false;}
		if (root==null) return false;
		addDataToXML(root.getOwnerDocument(),root,false,file);
		return xml.save(root);
	}

	/**
	 * Speichert die Daten des Objekts in der angegebenen Datei.
	 * @param file	Dateiname der Datei, in der die Daten gespeichert werden sollen
	 * @param fileType	Dateiformat
	 * @return	Gibt an, ob die Daten erfolgreich gespeichert werden konnten.
	 * @see XMLTools.FileType
	 */
	public final boolean saveToFile(final File file, final XMLTools.FileType fileType) {
		XMLTools xml=new XMLTools(file,fileType);
		Element root=null;
		try {
			root=xml.generateRoot(getRootNodeNames()[0]);
		} catch (Exception e) {return false;}
		if (root==null) return false;
		addDataToXML(root.getOwnerDocument(),root,false,file);
		return xml.save(root);
	}

	/**
	 * Speichert die Daten des Objekts in  dem angegebenen OutputStream.
	 * @param stream	OutputStream, in dem die Daten gespeichert werden sollen
	 * @return	Gibt an, ob die Daten erfolgreich gespeichert werden konnten.
	 */
	public final boolean saveToStream(final OutputStream stream) {
		return saveToStream(stream,FileType.XML);
	}

	/**
	 * Speichert die Daten des Objekts in  dem angegebenen OutputStream.
	 * @param stream	OutputStream, in dem die Daten gespeichert werden sollen
	 * @param fileType	Dateiformat
	 * @return	Gibt an, ob die Daten erfolgreich gespeichert werden konnten.
	 */
	public final boolean saveToStream(final OutputStream stream, final FileType fileType) {
		final XMLTools xml=new XMLTools(stream,fileType);
		Element root=null;
		try {
			root=xml.generateRoot(getRootNodeNames()[0]);
		} catch (Exception e) {return false;}
		if (root==null) return false;
		addDataToXML(root.getOwnerDocument(),root,false,null);
		return xml.save(root);
	}

	/**
	 * Erstellt unterhalb des übergebenen xml-Knotens einen neuen Knoten, der die Daten enthält.
	 * @param parent	Eltern-xml-Knoten
	 * @param isPartOfOtherFile	Gibt an, ob die Daten Teil einer übergeordneten Datei sein werden
	 */
	public final void saveToXML(final Element parent, final boolean isPartOfOtherFile) {
		final Document doc=parent.getOwnerDocument();
		final Element node=doc.createElement(getRootNodeNames()[0]); parent.appendChild(node);
		addDataToXML(doc,node,isPartOfOtherFile,null);
	}

	/**
	 * Speichert die Daten in einem Objekt vom Typ <code>Document</code>.
	 * @return	Liefert im Erfolgsfall das <code>Document</code>-Element und im Fehlerfall <code>null</code>.
	 */
	public final Document saveToXMLDocument() {
		XMLTools xml=new XMLTools();
		Element root=null;
		try {
			root=xml.generateRoot(getRootNodeNames()[0]);
		} catch (Exception e) {return null;}
		if (root==null) return null;
		addDataToXML(root.getOwnerDocument(),root,false,null);
		return root.getOwnerDocument();
	}

	/**
	 * Fügt ein einfaches Element zu der XML-Datei hinzu
	 * @param doc	XML-Dokument, zu dem der neue Knoten hinzugefügt werden soll
	 * @param parent	Eltern-Knoten des hinzuzufügenden Knotens
	 * @param nodeName	Name des neuen XML-Knotens
	 * @param nodeText	Textinhalt des neuen XML-Knotens
	 * @return	Liefert das neu hinzugefügte Elemente zurück
	 */
	protected final Element addTextToXML(final Document doc, final Element parent, final String nodeName, final String nodeText) {
		final Element node=doc.createElement(nodeName);
		parent.appendChild(node);
		node.setTextContent(nodeText);
		return node;
	}

	/**
	 * Fügt ein einfaches Element zu der XML-Datei hinzu
	 * @param doc	XML-Dokument, zu dem der neue Knoten hinzugefügt werden soll
	 * @param parent	Eltern-Knoten des hinzuzufügenden Knotens
	 * @param nodeName	Name des neuen XML-Knotens
	 * @param nodeText	Textinhalt des neuen XML-Knotens
	 * @return	Liefert das neu hinzugefügte Elemente zurück
	 */
	protected final Element addTextToXML(final Document doc, final Element parent, final String nodeName, final long nodeText) {
		return addTextToXML(doc,parent,nodeName,""+nodeText);
	}

	/**
	 * Fügt ein einfaches Element zu der XML-Datei hinzu
	 * @param doc	XML-Dokument, zu dem der neue Knoten hinzugefügt werden soll
	 * @param parent	Eltern-Knoten des hinzuzufügenden Knotens
	 * @param nodeName	Name des neuen XML-Knotens
	 * @param nodeText	Textinhalt des neuen XML-Knotens
	 * @return	Liefert das neu hinzugefügte Elemente zurück
	 */
	protected final Element addTextToXML(final Document doc, final Element parent, final String nodeName, final double nodeText) {
		return addTextToXML(doc,parent,nodeName,NumberTools.formatSystemNumber(nodeText));
	}

	/**
	 * Fügt ein einfaches Element zu der XML-Datei hinzu
	 * @param doc	XML-Dokument, zu dem der neue Knoten hinzugefügt werden soll
	 * @param parent	Eltern-Knoten des hinzuzufügenden Knotens
	 * @param nodeName	Name des neuen XML-Knotens
	 * @param nodeText	Textinhalt des neuen XML-Knotens
	 * @return	Liefert das neu hinzugefügte Elemente zurück
	 */
	protected final Element addTextToXML(final Document doc, final Element parent, final String nodeName, final AbstractRealDistribution nodeText) {
		return addTextToXML(doc,parent,nodeName,DistributionTools.distributionToString(nodeText));
	}

	/**
	 * Speichert die Daten in einem xml-Knoten
	 * @param doc	Übergeordnetes xml-Dokument
	 * @param node	Knoten, in dem die Daten des Objekts gespeichert werden sollen
	 * @param isPartOfOtherFile	Gibt an, ob die Daten Teil einer übergeordneten Datei sein werden
	 * @param file	Dateiname der Ausgabedatei (nur zur Info; kann <code>null</code> sein)
	 */
	protected abstract void addDataToXML(final Document doc, final Element node, final boolean isPartOfOtherFile, final File file);
}
