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

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Point;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import language.Language;
import mathtools.NumberTools;
import simulator.editmodel.EditModel;
import systemtools.BaseDialog;
import ui.AnimationPanel;
import ui.images.Images;
import ui.modeleditor.ModelClientData;
import ui.modeleditor.ModelDataRenameListener;
import ui.modeleditor.ModelSequences;
import ui.modeleditor.ModelSurface;
import ui.modeleditor.ModelSurfacePanel;
import ui.modeleditor.coreelements.ModelElement;
import ui.modeleditor.coreelements.ModelElementBox;
import ui.modeleditor.coreelements.ModelElementEdgeMultiIn;
import ui.modeleditor.coreelements.ModelElementEdgeMultiOut;
import ui.modeleditor.descriptionbuilder.ModelDescriptionBuilder;
import ui.modeleditor.fastpaint.Shapes;

/**
 * Dieses Element enthält ein vollständiges Untermodell
 * @author Alexander Herzog
 * @see ModelSurface
 */
public class ModelElementSub extends ModelElementBox implements ElementWithNewClientNames, ElementWithNewVariableNames, ModelDataRenameListener, ModelElementEdgeMultiIn, ModelElementEdgeMultiOut, ModelElementAnimationForceMove {
	private int countConnectionsIn;
	private int countConnectionsOut;
	private List<ModelElementEdge> connectionsIn;
	private List<ModelElementEdge> connectionsOut;

	/**
	 * Untermodell
	 * @see #getSubSurface()
	 */
	private ModelSurface subSurface;

	/* Wird nur beim Laden und Clonen verwendet. */
	private List<Integer> connectionsInIds=null;
	private List<Integer> connectionsOutIds=null;

	/**
	 * Konstruktor der Klasse <code>ModelElementMultiInSingleOutBox</code>
	 * @param model	Modell zu dem dieses Element gehören soll (kann später nicht mehr geändert werden)
	 * @param surface	Zeichenfläche zu dem dieses Element gehören soll (kann später nicht mehr geändert werden)
	 */
	public ModelElementSub(final EditModel model, final ModelSurface surface) {
		super(model,surface,Shapes.ShapeType.SHAPE_RECTANGLE);
		countConnectionsIn=0;
		countConnectionsOut=0;
		connectionsIn=new ArrayList<>();
		connectionsOut=new ArrayList<>();
		subSurface=new ModelSurface(getModel(),null,null,surface);
	}

	/**
	 * Icon, welches im "Element hinzufügen"-Dropdown-Menü angezeigt werden soll.
	 * @return	Icon für das Dropdown-Menü
	 */
	@Override
	public URL getAddElementIcon() {
		return Images.MODELEDITOR_ELEMENT_SUB.getURL();
	}

	/**
	 * Tooltip für den "Element hinzufügen"-Dropdown-Menü-Eintrag.
	 * @return Tooltip für den "Element hinzufügen"-Dropdown-Menüeintrag
	 */
	@Override
	public String getToolTip() {
		return Language.tr("Surface.Sub.Tooltip");
	}

	/**
	 * Muss aufgerufen werden, wenn sich eine Eigenschaft des Elements ändert.
	 */
	@Override
	public void fireChanged() {
		updateEdgeLabel();
		super.fireChanged();
	}

	/**
	 * Gibt die Möglichkeit, das Label an der auslaufenden Kante zu aktualisieren, nachdem sich im Element Veränderungen ergeben haben.
	 */
	protected void updateEdgeLabel() {
		if (connectionsOut==null) return;
		for (int i=0;i<connectionsOut.size();i++) connectionsOut.get(i).setName(Language.tr("Surface.Sub.LabelExit")+" "+(i+1));
	}

	/**
	 * Überprüft, ob das Element mit dem angegebenen Element inhaltlich identisch ist.
	 * @param element	Element mit dem dieses Element verglichen werden soll.
	 * @return	Gibt <code>true</code> zurück, wenn die beiden Elemente identisch sind.
	 */
	@Override
	public boolean equalsModelElement(ModelElement element) {
		if (!super.equalsModelElement(element)) return false;
		if (!(element instanceof ModelElementSub)) return false;

		if (countConnectionsIn!=((ModelElementSub)element).countConnectionsIn) return false;
		if (countConnectionsOut!=((ModelElementSub)element).countConnectionsOut) return false;

		final List<ModelElementEdge> connectionsIn2=((ModelElementSub)element).connectionsIn;
		if (connectionsIn==null || connectionsIn2==null || connectionsIn.size()!=connectionsIn2.size()) return false;
		for (int i=0;i<connectionsIn.size();i++) if (connectionsIn.get(i).getId()!=connectionsIn2.get(i).getId()) return false;

		final List<ModelElementEdge> connectionsOut2=((ModelElementSub)element).connectionsOut;
		if (connectionsOut==null || connectionsOut2==null || connectionsOut.size()!=connectionsOut2.size()) return false;
		for (int i=0;i<connectionsOut.size();i++) if (connectionsOut.get(i).getId()!=connectionsOut2.get(i).getId()) return false;

		if (!((ModelElementSub)element).subSurface.equalsModelSurface(subSurface)) return false;

		return true;
	}

	/**
	 * Überträgt die Einstellungen von dem angegebenen Element auf dieses.
	 * @param element	Element, von dem alle Einstellungen übernommen werden sollen
	 */
	@Override
	public void copyDataFrom(ModelElement element) {
		super.copyDataFrom(element);
		if (element instanceof ModelElementSub) {

			countConnectionsIn=((ModelElementSub)element).countConnectionsIn;
			countConnectionsOut=((ModelElementSub)element).countConnectionsOut;

			connectionsIn.clear();
			final List<ModelElementEdge> connectionsIn2=((ModelElementSub)element).connectionsIn;
			if (connectionsIn2!=null) {
				connectionsInIds=new ArrayList<>();
				for (int i=0;i<connectionsIn2.size();i++) connectionsInIds.add(connectionsIn2.get(i).getId());
			}

			connectionsOut.clear();
			final List<ModelElementEdge> connectionsOut2=((ModelElementSub)element).connectionsOut;
			if (connectionsOut2!=null) {
				connectionsOutIds=new ArrayList<>();
				for (int i=0;i<connectionsOut2.size();i++) connectionsOutIds.add(connectionsOut2.get(i).getId());
			}

			subSurface=((ModelElementSub)element).subSurface.clone(false,null,null,surface,getModel());
		}
	}

	/**
	 * Erstellt eine Kopie des Elements
	 * @param model	Modell zu dem das kopierte Element gehören soll.
	 * @param surface	Zeichenfläche zu der das kopierte Element gehören soll.
	 * @return	Kopiertes Element
	 */
	@Override
	public ModelElementSub clone(final EditModel model, final ModelSurface surface) {
		final ModelElementSub element=new ModelElementSub(model,surface);
		element.copyDataFrom(this);
		return element;
	}

	/**
	 * Optionale Initialisierungen nach dem Laden bzw. Clonen.
	 */
	@Override
	public void initAfterLoadOrClone() {
		super.initAfterLoadOrClone();

		ModelElement element;

		if (countConnectionsIn==0) countConnectionsIn=1;
		if (countConnectionsOut==0) countConnectionsOut=1;

		if (connectionsInIds!=null) {
			for (int i=0;i<connectionsInIds.size();i++) {
				element=surface.getById(connectionsInIds.get(i));
				if (element instanceof ModelElementEdge) connectionsIn.add((ModelElementEdge)element);
			}
			connectionsInIds=null;
		}

		if (connectionsOutIds!=null) {
			for (int i=0;i<connectionsOutIds.size();i++) {
				element=surface.getById(connectionsOutIds.get(i));
				if (element instanceof ModelElementEdge) connectionsOut.add((ModelElementEdge)element);
			}
			connectionsOutIds=null;
		}

		int countIn=0;
		int countOut=0;
		for (ModelElement e: subSurface.getElements()) {
			if (e instanceof ModelElementSubIn) {
				((ModelElementSubIn)e).setConnectionData(countIn,((ModelElementSubIn)e).getConnectionStationID());
				countIn++;
			}
			if (e instanceof ModelElementSubOut) {
				((ModelElementSubOut)e).setConnectionData(countOut,((ModelElementSubOut)e).getConnectionStationID());
				countOut++;
			}
		}
	}

	/**
	 * Name des Elementtyps für die Anzeige im Kontextmenü
	 * @return	Name des Elementtyps
	 */
	@Override
	public String getContextMenuElementName() {
		return Language.tr("Surface.Sub.Name");
	}

	/**
	 * Liefert die Bezeichnung des Typs des Elemente (zur Anzeige in der Element-Box)
	 * @return	Name des Typs
	 */
	@Override
	public String getTypeName() {
		return Language.tr("Surface.Sub.Name");
	}

	/**
	 * Vorgabe-Hintergrundfarbe für die Box
	 * @see #getTypeDefaultBackgroundColor()
	 */
	private static final Color defaultBackgroundColor=new Color(250,250,250);

	/**
	 * Liefert die Vorgabe-Hintergrundfarbe für die Box
	 * @return	Vorgabe-Hintergrundfarbe für die Box
	 */
	@Override
	public Color getTypeDefaultBackgroundColor() {
		return defaultBackgroundColor;
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
			if (countConnectionsIn==0) countConnectionsIn=1;
			if (countConnectionsOut==0) countConnectionsOut=1;
			final ModelElementSubDialog dialog=new ModelElementSubDialog(owner,ModelElementSub.this,readOnly);
			fireChanged();
			if (dialog.getClosedBy()==BaseDialog.CLOSED_BY_OK && dialog.getOpenEditor()) {
				showSubEditDialog(owner,readOnly);
			}
		};
	}

	/**
	 * Fügt optional weitere Einträge zum Kontextmenü hinzu
	 * @param owner	Übergeordnetes Element
	 * @param popupMenu	Kontextmenü zu dem die Einträge hinzugefügt werden sollen
	 * @param surfacePanel	Zeichenfläche
	 * @param point	Punkt auf den geklickt wurde
	 * @param readOnly	Wird dieser Parameter auf <code>true</code> gesetzt, so können über das Kontextmenü keine Änderungen an dem Modell vorgenommen werden
	 */
	@Override
	protected void addContextMenuItems(final Component owner, final JPopupMenu popupMenu, final ModelSurfacePanel surfacePanel, final Point point, final boolean readOnly) {
		if (addRemoveEdgesContextMenuItems(owner,popupMenu,readOnly)) popupMenu.addSeparator();
	}

	/**
	 * Zeigt den Dialog zum Bearbeiten der Elemente des Untermodells an
	 * @param owner	Übergeordnetes Fenster für den Dialog
	 * @param readOnly	Wird dieser Parameter auf <code>true</code> gesetzt, so können die Einträge nur angezeigt und nicht geändert werden
	 */
	public void showSubEditDialog(Component owner, boolean readOnly) {
		/* Ggf. Anzahl der Ein- und Ausgänge auf mindestens 1 setzen */
		if (countConnectionsIn==0) countConnectionsIn=1;
		if (countConnectionsOut==0) countConnectionsOut=1;
		setInputCount(countConnectionsIn);
		setOutputCount(countConnectionsOut);

		final int[] idsIn=new int[countConnectionsIn];
		final ModelElementEdge[] edgesIn=getEdgesIn();
		ModelElement element;
		for (int i=0;i<countConnectionsIn;i++) {
			idsIn[i]=-1;
			if (i>=edgesIn.length) continue;
			if (edgesIn[i]==null) continue;
			element=edgesIn[i].getConnectionEnd();
			if (element!=null) idsIn[i]=element.getId();
		}
		final int[] idsOut=new int[countConnectionsOut];
		final ModelElementEdge[] edgesOut=getEdgesOut();
		for (int i=0;i<countConnectionsOut;i++) {
			idsOut[i]=-1;
			if (i>=edgesOut.length) continue;
			if (edgesOut[i]==null) continue;
			element=edgesOut[i].getConnectionEnd();
			if (element!=null) idsOut[i]=element.getId();
		}

		final AnimationPanel mainAnimationPanel=surface.getAnimationPanel();
		if (mainAnimationPanel==null) {
			/* Bearbeiten */
			ModelSurface temp=subSurface.clone(false,null,null,subSurface.getParentSurface(),getModel());
			final ModelElementSubEditDialog dialog=new ModelElementSubEditDialog(owner,getModel(),surface,subSurface,idsIn,idsOut,readOnly);
			dialog.setVisible(true);
			if (dialog.getClosedBy()==BaseDialog.CLOSED_BY_OK) {
				subSurface=dialog.getSurface();
				surface.getResources().setDataFrom(subSurface.getResources());
				surface.fireStateChangeListener();
			} else {
				subSurface=temp.clone(false,subSurface.getResources(),subSurface.getSchedules(),subSurface.getParentSurface(),getModel());
			}
			fireChanged();
		} else {
			/* Ansicht der Animation */
			final ModelElementSubAnimationDialog dialog=new ModelElementSubAnimationDialog(owner,surface,subSurface,mainAnimationPanel);
			mainAnimationPanel.setSubViewer(dialog);
			dialog.setVisible(true);
			mainAnimationPanel.setSubViewer(null);
		}
	}

	/**
	 * Fügt Menüpunkte zum Hinzufügen von einlaufenden und auslaufender Kante zum Kontextmenü
	 * @param owner	Übergeordnetes Element
	 * @param popupMenu	Kontextmenü zu dem die Einträge hinzugefügt werden sollen
	 * @param readOnly	Wird dieser Parameter auf <code>true</code> gesetzt, so können über das Kontextmenü keine Änderungen an dem Modell vorgenommen werden
	 * @return	Gibt <code>true</code> zurück, wenn Elemente in das Kontextmenü eingefügt wurden (und ggf. ein Separator vor dem nächsten Abschnitt gesetzt werden sollte)
	 */
	protected final boolean addRemoveEdgesContextMenuItems(final Component owner, final JPopupMenu popupMenu, final boolean readOnly) {
		JMenuItem item;
		final URL imgURL=Images.EDIT_EDGES_DELETE.getURL();
		boolean needSeparator=false;

		needSeparator=true;
		popupMenu.add(item=new JMenuItem(Language.tr("Surface.Sub.EditSub")));
		item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER,InputEvent.SHIFT_DOWN_MASK));
		item.setFont(item.getFont().deriveFont(Font.BOLD));
		item.setIcon(Images.MODELEDITOR_ELEMENT_SUB_EDIT.getIcon());
		item.addActionListener(e->showSubEditDialog(owner,readOnly));

		if (connectionsIn!=null && connectionsIn.size()>0) {
			popupMenu.add(item=new JMenuItem(Language.tr("Surface.PopupMenu.RemoveEdgesIn")));
			item.addActionListener(e->{for (ModelElementEdge element : new ArrayList<>(connectionsIn)) surface.remove(element);});
			if (imgURL!=null) item.setIcon(new ImageIcon(imgURL));
			item.setEnabled(!readOnly);
			needSeparator=true;
		}
		if (connectionsOut!=null && connectionsOut.size()>0) {
			popupMenu.add(item=new JMenuItem(Language.tr("Surface.PopupMenu.RemoveEdgesOut")));
			item.addActionListener(e->{for (ModelElementEdge element : new ArrayList<>(connectionsOut)) surface.remove(element);});
			if (imgURL!=null) item.setIcon(new ImageIcon(imgURL));
			item.setEnabled(!readOnly);
			needSeparator=true;
		}
		return needSeparator;
	}


	/**
	 * Benachrichtigt das Element, dass es aus der Surface-Liste ausgetragen wurde.
	 */
	@Override
	public void removeNotify() {
		if (connectionsIn!=null) {
			while (connectionsIn.size()>0) {
				ModelElement element=connectionsIn.remove(0);
				surface.remove(element);
			}
		}
		if (connectionsOut!=null) {
			while (connectionsOut.size()>0) {
				ModelElement element=connectionsOut.remove(0);
				surface.remove(element);
			}
		}
	}

	/**
	 * Benachrichtigt das Element, dass ein mit ihm in Verbindung stehendes Element entfernt wurde.
	 */
	@Override
	public void removeConnectionNotify(final ModelElement element) {
		if (connectionsIn!=null && connectionsIn.indexOf(element)>=0) {connectionsIn.remove(element); fireChanged();}
		if (connectionsOut!=null && connectionsOut.indexOf(element)>=0) {connectionsOut.remove(element); fireChanged();}
	}

	/**
	 * Liefert den jeweiligen xml-Element-Namen für das Modell-Element
	 * @return	xml-Element-Namen, der diesem Modell-Element zugeordnet werden soll
	 */
	@Override
	public String[] getXMLNodeNames() {
		return Language.trAll("Surface.Sub.XML.Root");
	}

	/**
	 * Speichert die Eigenschaften des Modell-Elements als Untereinträge eines xml-Knotens
	 * @param doc	Übergeordnetes xml-Dokument
	 * @param node	Übergeordneter xml-Knoten, in dessen Kindelementen die Daten des Objekts gespeichert werden sollen
	 */
	@Override
	protected void addPropertiesDataToXML(final Document doc, final Element node) {
		super.addPropertiesDataToXML(doc,node);

		Element sub;

		node.appendChild(sub=doc.createElement(Language.trPrimary("Surface.Sub.XML.ConnectionCount")));
		sub.setAttribute(Language.trPrimary("Surface.Sub.XML.ConnectionCount.In"),""+countConnectionsIn);
		sub.setAttribute(Language.trPrimary("Surface.Sub.XML.ConnectionCount.Out"),""+countConnectionsOut);

		if (connectionsIn!=null) for (ModelElementEdge element: connectionsIn) {
			node.appendChild(sub=doc.createElement(Language.trPrimary("Surface.XML.Connection")));
			sub.setAttribute(Language.trPrimary("Surface.XML.Connection.Element"),""+element.getId());
			sub.setAttribute(Language.trPrimary("Surface.XML.Connection.Type"),Language.trPrimary("Surface.XML.Connection.Type.In"));
		}

		if (connectionsOut!=null) for (ModelElementEdge element: connectionsOut) {
			node.appendChild(sub=doc.createElement(Language.trPrimary("Surface.XML.Connection")));
			sub.setAttribute(Language.trPrimary("Surface.XML.Connection.Element"),""+element.getId());
			sub.setAttribute(Language.trPrimary("Surface.XML.Connection.Type"),Language.trPrimary("Surface.XML.Connection.Type.Out"));
		}

		if (subSurface!=null) subSurface.addDataToXML(doc,node);
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

		for (String test: ModelSurface.XML_NODE_NAME) if (name.equalsIgnoreCase(test)) {
			return subSurface.loadFromXML(node);
		}

		if (Language.trAll("Surface.Sub.XML.ConnectionCount",name)) {
			final String inString=Language.trAllAttribute("Surface.Sub.XML.ConnectionCount.In",node);
			final String outString=Language.trAllAttribute("Surface.Sub.XML.ConnectionCount.Out",node);
			if (!inString.isEmpty()) {
				final Long in=NumberTools.getPositiveLong(inString);
				if (in==null) return String.format(Language.tr("Surface.XML.AttributeSubError"),Language.trPrimary("Surface.Sub.XML.ConnectionCount.In"),name,node.getParentNode().getNodeName());
				countConnectionsIn=(int)(long)in;
			}
			if (!outString.isEmpty()) {
				final Long out=NumberTools.getPositiveLong(outString);
				if (out==null) return String.format(Language.tr("Surface.XML.AttributeSubError"),Language.trPrimary("Surface.Sub.XML.ConnectionCount.Out"),name,node.getParentNode().getNodeName());
				countConnectionsOut=(int)(long)out;
			}
			return null;
		}

		if (Language.trAll("Surface.XML.Connection",name)) {
			Integer I;
			I=NumberTools.getNotNegativeInteger(Language.trAllAttribute("Surface.XML.Connection.Element",node));
			if (I==null) return String.format(Language.tr("Surface.XML.AttributeSubError"),Language.trPrimary("Surface.XML.Connection.Element"),name,node.getParentNode().getNodeName());
			final String s=Language.trAllAttribute("Surface.XML.Connection.Type",node);
			if (Language.trAll("Surface.XML.Connection.Type.In",s)) {
				if (connectionsInIds==null) connectionsInIds=new ArrayList<>();
				connectionsInIds.add(I);
				if (countConnectionsIn<connectionsInIds.size()) countConnectionsIn=connectionsInIds.size();
			}
			if (Language.trAll("Surface.XML.Connection.Type.Out",s)) {
				if (connectionsOutIds==null) connectionsOutIds=new ArrayList<>();
				connectionsOutIds.add(I);
				if (countConnectionsOut<connectionsOutIds.size()) countConnectionsOut=connectionsOutIds.size();
			}
			return null;
		}

		return null;
	}

	/**
	 * Gibt an, ob das Element momentan eine (weitere) einlaufende Kante annehmen kann.
	 * @return	Gibt <code>true</code> zurück, wenn eine (weitere) einlaufende Kante angenommen werden kann.
	 */
	@Override
	public boolean canAddEdgeIn() {
		if (countConnectionsIn==0) countConnectionsIn=1;
		if (countConnectionsOut==0) countConnectionsOut=1;

		return connectionsIn.size()<countConnectionsIn;
	}

	/**
	 * Fügt eine einlaufende Kante hinzu.
	 * @param edge	Hinzuzufügende Kante
	 * @return	Gibt <code>true</code> zurück, wenn die einlaufende Kante hinzugefügt werden konnte.
	 */
	@Override
	public boolean addEdgeIn(ModelElementEdge edge) {
		if (edge!=null && connectionsIn.indexOf(edge)<0 && connectionsIn.size()<countConnectionsIn) {
			connectionsIn.add(edge);
			fireChanged();
			return true;
		}
		return false;
	}

	/**
	 * Gibt an, ob das Element momentan eine (weitere) auslaufende Kante annehmen kann.
	 * @return	Gibt <code>true</code> zurück, wenn eine (weitere) auslaufende Kante angenommen werden kann.
	 */
	@Override
	public boolean canAddEdgeOut() {
		if (countConnectionsIn==0) countConnectionsIn=1;
		if (countConnectionsOut==0) countConnectionsOut=1;

		return connectionsOut.size()<countConnectionsOut;
	}

	/**
	 * Fügt eine auslaufende Kante hinzu.
	 * @param edge	Hinzuzufügende Kante
	 * @return	Gibt <code>true</code> zurück, wenn die auslaufende Kante hinzugefügt werden konnte.
	 */
	@Override
	public boolean addEdgeOut(ModelElementEdge edge) {
		if (edge!=null && connectionsOut.indexOf(edge)<0 && connectionsOut.size()<countConnectionsOut) {
			connectionsOut.add(edge);
			fireChanged();
			return true;
		}
		return false;
	}

	@Override
	public String[] getNewClientTypes() {
		return subSurface.getClientTypes().toArray(new String[0]);
	}

	@Override
	public String[] getVariables() {
		return subSurface.getVariableNames(null);
	}

	/**
	 * Liefert die Anzahl an in das Untermodell einlaufende Kanten.<br>
	 * Diese Anzahl kann höher sein, als die momentan verbundenen einlaufenden Kanten. In diesem Fall ist das Modell
	 * momentan nicht simulierbar.
	 * @return	Anzahl an Verknüpfungspunkten, die in das Untermodell einlaufen
	 */
	public int getInputCount() {
		return countConnectionsIn;
	}

	/**
	 * Stellt die Anzahl an in das Untermodell einlaufende Kanten ein.<br>
	 * Diese Anzahl kann höher sein, als die momentan verbundenen einlaufenden Kanten. In diesem Fall ist das Modell
	 * momentan nicht simulierbar.
	 * @param inputCount Anzahl an Verknüpfungspunkten, die in das Untermodell einlaufen
	 */
	public void setInputCount(final int inputCount) {
		if (inputCount>0) countConnectionsIn=inputCount;
		while (connectionsIn.size()>countConnectionsIn) surface.remove(connectionsIn.get(connectionsIn.size()-1));
		fireChanged();
	}

	/**
	 * Liefert die Anzahl an aus dem Untermodell auslaufenden Kanten.<br>
	 * Diese Anzahl kann höher sein, als die momentan verbundenen auslaufenden Kanten. In diesem Fall ist das Modell
	 * momentan nicht simulierbar.
	 * @return	Anzahl an Verknüpfungspunkten, die aus dem Untermodell auslaufen
	 */
	public int getOutputCount() {
		return countConnectionsOut;
	}

	/**
	 * Stellt die Anzahl an aus dem Untermodell auslaufenden Kanten ein.<br>
	 * Diese Anzahl kann höher sein, als die momentan verbundenen auslaufenden Kanten. In diesem Fall ist das Modell
	 * momentan nicht simulierbar.
	 * @param outputCount	Anzahl an Verknüpfungspunkten, die aus dem Untermodell auslaufen
	 */
	public void setOutputCount(final int outputCount) {
		if (outputCount>0) countConnectionsOut=outputCount;
		while (connectionsOut.size()>countConnectionsOut) surface.remove(connectionsOut.get(connectionsOut.size()-1));
		fireChanged();
	}

	/**
	 * Liefert das aktuell verwendete Untermodell zurück
	 * @return	Untermodell
	 */
	public ModelSurface getSubSurface() {
		return subSurface.clone(false,null,null,surface,getModel());
	}

	/**
	 * Stell ein neues Untermodell ein
	 * @param subSurface	Neues Untermodell
	 */
	public void setSubSurface(final ModelSurface subSurface) {
		if (subSurface!=null) this.subSurface=subSurface.clone(false,null,null,surface,getModel());
	}

	/**
	 * Liefert eine Liste aller einlaufenden Kanten
	 * @return	Liste der einlaufenden Kanten
	 */
	@Override
	public ModelElementEdge[] getEdgesIn() {
		if (connectionsIn==null) return new ModelElementEdge[0];
		return connectionsIn.toArray(new ModelElementEdge[0]);
	}

	/**
	 * Liefert eine Liste aller auslaufenden Kanten
	 * @return	Liste der auslaufenden Kanten
	 */
	@Override
	public ModelElementEdge[] getEdgesOut() {
		if (connectionsOut==null) return new ModelElementEdge[0];
		return connectionsOut.toArray(new ModelElementEdge[0]);
	}

	/**
	 * Gibt an, ob eine bestimmte Id in diesem oder einem untergeordneten Element vergeben ist
	 * @param id	Zu prüfende Id
	 * @return	Gibt <code>true</code> zurück, wenn die ID in dem aktuellen und den untergeordneten Elementen noch nicht vergeben ist
	 */
	public boolean isFreeId(final int id) {
		if (id==getId()) return false;
		if (subSurface==null) return true;
		return subSurface.isFreeId(id);
	}

	@Override
	public void addedToSurface() {
		for (ModelElement element: subSurface.getElements()) {
			int id=element.getId();
			element.setId(-1);
			if (surface.isFreeId(id)) {
				element.setId(id);
			} else {
				int newId=surface.getNextFreeId();
				element.setId(newId);
				/* Debug: System.out.println(id+" -> "+newId+" für "+element.getClass().getName()); */
			}
		}
	}

	@Override
	public boolean canAddToSub() {
		return false; /* Sorry, keine verschachtelten Submodelle. */
	}

	/**
	 * Gibt an, ob es in das Element einlaufende Kanten gibt.<br><br>
	 * Wenn nicht, kann es in der Simulation überhaupt nicht erreicht werden und kann daher
	 * bei der Initialisierung übersprungen werden, d.h. in diesem Fall ist es dann egal,
	 * ob das Element in Bezug auf die Konfiguration fehlerhaft ist, z.B. keine auslaufenden
	 * Kanten hat.<br><br>
	 * Bei Variablenzuweisungen wird die Liste der Zuweisungen dennoch bei der Initialisierung
	 * der Simulation berücksichtigt: Es wird so ermittelt, welche Variablennamen in im Modell
	 * vorkommen (d.h. auf diese Variablen kann an anderer Stelle zugegriffen werden, ohne dass
	 * sie noch einmal deklariert werden müssten).
	 * @return	Gibt <code>true</code> zurück, wenn es mindestens eine in das Element einlaufende
	 * Kante gibt.
	 */
	@Override
	public boolean inputConnected() {
		return connectionsIn.size()>0;
	}

	/**
	 * Gibt an, ob nur die Anzahl an Kunden, die diese Station passiert haben
	 * oder aber zusätzlich auch die aktuelle Anzahl an Kunden an der Station
	 * während der Animation angezeigt werden sollen.
	 * @return	Nur Gesamtanzahl (<code>false</code>) oder Gesamtanzahl und aktueller Wert (<code>true</code>)
	 */
	@Override
	public boolean showFullAnimationRunData() {
		return true;
	}

	@Override
	public void objectRenamed(String oldName, String newName, ModelDataRenameListener.RenameType type) {
		subSurface.objectRenamed(oldName,newName,type,false);
	}

	@Override
	public String getHelpPageName() {
		return "ModelElementSub";
	}

	/**
	 * Setzt ein neues {@link EditModel}-Element für dieses Element
	 * @param model	Neues zugehöriges {@link EditModel}-Element
	 */
	@Override
	public void setModel(final EditModel model) {
		super.setModel(model);
		subSurface.updateElements(model);
	}

	/**
	 * Erstellt eine Beschreibung für das aktuelle Element
	 * @param descriptionBuilder	Description-Builder, der die Beschreibungsdaten zusammenfasst
	 */
	@Override
	public void buildDescription(final ModelDescriptionBuilder descriptionBuilder) {
		super.buildDescription(descriptionBuilder);

		for (int i=0;i<connectionsOut.size();i++) {
			final ModelElementEdge edge=connectionsOut.get(i);
			descriptionBuilder.addConditionalEdgeOut(String.format(Language.tr("ModelDescription.Sub.EdgeOut"),i+1),edge);
		}
	}

	@Override
	public boolean setReferenceEdges(List<ModelElementEdge> connectionsIn, List<ModelElementEdge> connectionsOut) {
		if (countConnectionsIn==0) countConnectionsIn=1;
		if (countConnectionsOut==0) countConnectionsOut=1;

		if (connectionsIn.size()>countConnectionsIn) return false;
		if (connectionsOut.size()>countConnectionsOut) return false;

		this.connectionsIn.clear();
		this.connectionsIn.addAll(connectionsIn);

		this.connectionsOut.clear();
		this.connectionsOut.addAll(connectionsOut);

		return true;
	}
}