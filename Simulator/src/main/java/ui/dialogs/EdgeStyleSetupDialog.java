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
package ui.dialogs;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.io.Serializable;
import java.util.Arrays;

import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import language.Language;
import simulator.editmodel.EditModel;
import systemtools.BaseDialog;
import systemtools.LabeledColorChooserButton;
import tools.IconListCellRenderer;
import ui.help.Help;
import ui.images.Images;
import ui.modeleditor.ModelElementBaseDialog;
import ui.modeleditor.elements.ComplexLine;
import ui.modeleditor.elements.ModelElementEdge;

/**
 * Diese Klasse stellt einen Dialog zum Bearbeiten des Aussehens der
 * Verbindungskanten bereit.
 * @author Alexander Herzog
 * @see EditModel#edgePainterNormal
 * @see EditModel#edgePainterSelected
 *
 */
public class EdgeStyleSetupDialog extends BaseDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = 5850107078223112820L;

	/** Darstellung von nicht-selektierten Verbindungskanten */
	private final ComplexLine edgeNormal;
	/** Darstellung von selektierten Verbindungskanten */
	private final ComplexLine edgeSelected;

	/** Auswahl der Darstellung von Verbindungskanten im Allgemeinen (gerade, abgewinkelt, ...) */
	private final JComboBox<String> lineMode;

	/** Auswahl der Gr��e des Pfeils am Ende der Verkn�pfungslinie */
	private final JComboBox<String> arrowMode;

	/** Auswahl der Linienbreite von nicht-selektierten Verbindungskanten */
	private final JComboBox<JLabel> lineWidthNormal;
	/** Auswahl der Darstellung von nicht-selektierten Verbindungskanten */
	private final JComboBox<JLabel> lineTypeNormal;
	/** Auswahl der Linienbreite von selektierten Verbindungskanten */
	private final JComboBox<JLabel> lineWidthSelected;
	/** Auswahl der Darstellung von selektierten Verbindungskanten */
	private final JComboBox<JLabel> lineTypeSelected;

	/** Farbw�hler */
	private final LabeledColorChooserButton colorChooser;

	/**
	 * Konstruktor der Klasse
	 * @param owner	�bergeordnetes Element
	 * @param edgeNormal	Kantenzeichner f�r normale Kanten (wird beim Schlie�en mit "Ok" modifiziert; hier sollte {@link EditModel#edgePainterNormal} �bergeben werden)
	 * @param edgeSelected	Kantenzeichner f�r selektierte Kanten (wird beim Schlie�en mit "Ok" modifiziert; hier sollte {@link EditModel#edgePainterSelected} �bergeben werden)
	 * @param lineMode	Darstellungsart der Verbindungkanten (direkt, abgewinkelt, ...)
	 * @param arrowMode	Gr��e des Pfeils am Ende der Verkn�pfungslinie
	 */
	@SuppressWarnings("unchecked")
	public EdgeStyleSetupDialog(final Component owner, final ComplexLine edgeNormal, final ComplexLine edgeSelected, final ModelElementEdge.LineMode lineMode, final ModelElementEdge.ArrowMode arrowMode) {
		super(owner,Language.tr("Window.EdgeStyle.Title"));

		this.edgeNormal=edgeNormal;
		this.edgeSelected=edgeSelected;

		final JPanel content=createGUI(()->Help.topicModal(this,"EdgeStyleSetupDialog"));
		content.setLayout(new BorderLayout());

		final JPanel sub=new JPanel();
		content.add(sub,BorderLayout.NORTH);
		sub.setLayout(new BoxLayout(sub,BoxLayout.PAGE_AXIS));

		Object[] data;

		final String[] lineModeArray=new String[] {
				Language.tr("Surface.Connection.LineMode.Direct"),
				Language.tr("Surface.Connection.LineMode.MultiLine"),
				Language.tr("Surface.Connection.LineMode.MultiLineRounded"),
				Language.tr("Surface.Connection.LineMode.CubicCurve")
		};
		data=ModelElementBaseDialog.getComboBoxPanel(Language.tr("Surface.Connection.LineMode")+":","",Arrays.asList(lineModeArray));
		sub.add((JPanel)data[0]);
		this.lineMode=(JComboBox<String>)data[1];
		this.lineMode.setEditable(false);
		switch (lineMode) {
		case DIRECT: this.lineMode.setSelectedIndex(0); break;
		case MULTI_LINE: this.lineMode.setSelectedIndex(1); break;
		case MULTI_LINE_ROUNDED: this.lineMode.setSelectedIndex(2); break;
		case CUBIC_CURVE: this.lineMode.setSelectedIndex(3); break;
		default: this.lineMode.setSelectedIndex(2); break;
		}
		this.lineMode.setRenderer(new IconListCellRenderer(new Images[]{
				Images.EDGE_MODE_DIRECT,
				Images.EDGE_MODE_MULTI_LINE,
				Images.EDGE_MODE_MULTI_LINE_ROUNDED,
				Images.EDGE_MODE_CUBIC_CURVE
		}));

		final String[] arrowModeArray=new String[] {
				Language.tr("Surface.Connection.ArrowMode.Off"),
				Language.tr("Surface.Connection.ArrowMode.Small"),
				Language.tr("Surface.Connection.ArrowMode.Medium"),
				Language.tr("Surface.Connection.ArrowMode.Large")
		};
		data=ModelElementBaseDialog.getComboBoxPanel(Language.tr("Surface.Connection.ArrowMode")+":","",Arrays.asList(arrowModeArray));
		sub.add((JPanel)data[0]);
		this.arrowMode=(JComboBox<String>)data[1];
		this.arrowMode.setEditable(false);
		switch (arrowMode) {
		case OFF: this.arrowMode.setSelectedIndex(0); break;
		case SMALL: this.arrowMode.setSelectedIndex(1); break;
		case MEDIUM: this.arrowMode.setSelectedIndex(2); break;
		case LARGE: this.arrowMode.setSelectedIndex(3); break;
		default: this.arrowMode.setSelectedIndex(3); break;
		}
		this.arrowMode.setRenderer(new IconListCellRenderer(new Images[]{
				Images.ARROW_SIZE_OFF,
				Images.ARROW_SIZE_SMALL,
				Images.ARROW_SIZE_MEDIUM,
				Images.ARROW_SIZE_LARGE
		}));

		data=ModelElementBaseDialog.getLineWidthInputPanel(Language.tr("Window.EdgeStyle.LineWidth.Normal")+":",1,5,edgeNormal.getWidth());
		sub.add((JPanel)data[0]);
		lineWidthNormal=(JComboBox<JLabel>)data[1];

		data=ModelElementBaseDialog.getLineWidthTypePanel(Language.tr("Window.EdgeStyle.LineType.Normal")+":",edgeNormal.getType());
		sub.add((JPanel)data[0]);
		lineTypeNormal=(JComboBox<JLabel>)data[1];

		data=ModelElementBaseDialog.getLineWidthInputPanel(Language.tr("Window.EdgeStyle.LineWidth.Selected")+":",1,5,edgeSelected.getWidth());
		sub.add((JPanel)data[0]);
		lineWidthSelected=(JComboBox<JLabel>)data[1];

		data=ModelElementBaseDialog.getLineWidthTypePanel(Language.tr("Window.EdgeStyle.LineType.Selected")+":",edgeSelected.getType());
		sub.add((JPanel)data[0]);
		lineTypeSelected=(JComboBox<JLabel>)data[1];

		final JPanel line=new JPanel(new FlowLayout(FlowLayout.LEFT));
		sub.add(line);
		line.add(colorChooser=new LabeledColorChooserButton(Language.tr("Window.EdgeStyle.LineColor")+":",edgeNormal.getColor()));

		pack();
	}

	@Override
	protected void storeData() {
		edgeNormal.setWidth(lineWidthNormal.getSelectedIndex()+1);
		edgeNormal.setType(lineTypeNormal.getSelectedIndex());

		edgeSelected.setWidth(lineWidthSelected.getSelectedIndex()+1);
		edgeSelected.setType(lineTypeSelected.getSelectedIndex());

		edgeNormal.setColor(colorChooser.getColor());
	}

	/**
	 * Liefert die eingestellte Darstellungsart der Verbindungkanten (direkt, abgewinkelt, ...)
	 * @return	Darstellungsart der Verbindungkanten (direkt, abgewinkelt, ...)
	 */
	public ModelElementEdge.LineMode getLineMode() {
		switch (lineMode.getSelectedIndex()) {
		case 0: return ModelElementEdge.LineMode.DIRECT;
		case 1: return ModelElementEdge.LineMode.MULTI_LINE;
		case 2: return ModelElementEdge.LineMode.MULTI_LINE_ROUNDED;
		case 3: return ModelElementEdge.LineMode.CUBIC_CURVE;
		default: return ModelElementEdge.LineMode.DIRECT;
		}
	}

	/**
	 * Liefert die eingestellte Gr��e des Pfeils am Ende der Verkn�pfungslinie.
	 * @return	Gr��e des Pfeils am Ende der Verkn�pfungslinie
	 */
	public ModelElementEdge.ArrowMode getArrowMode() {
		switch (arrowMode.getSelectedIndex()) {
		case 0: return ModelElementEdge.ArrowMode.OFF;
		case 1: return ModelElementEdge.ArrowMode.SMALL;
		case 2: return ModelElementEdge.ArrowMode.MEDIUM;
		case 3: return ModelElementEdge.ArrowMode.LARGE;
		default: return ModelElementEdge.ArrowMode.LARGE;
		}
	}
}
