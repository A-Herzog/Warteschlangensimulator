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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Point;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import language.Language;
import systemtools.SmallColorChooser;
import tools.IconListCellRenderer;
import ui.images.Images;
import ui.infopanel.InfoPanel;
import ui.modeleditor.ModelElementBaseDialog;

/**
 * Dialog, der Einstellungen für ein {@link ModelElementLine}-Element anbietet
 * @author Alexander Herzog
 * @see ModelElementLine
 */
public class ModelElementLineDialog extends ModelElementBaseDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = 7667068339479220507L;

	/** Auswahl der Linienbreite */
	private JComboBox<JLabel> lineWidth;
	/** Auswahl des Linientyps */
	private JComboBox<JLabel> lineType;
	/** Auswahl möglicher Pfeilspitzen am Linienbeginn */
	private JComboBox<String> arrowStart;
	/** Auswahl möglicher Pfeilspitzen am Linienende */
	private JComboBox<String> arrowEnd;
	/** Auswahl der Farbe der Linie */
	private SmallColorChooser colorChooser;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Fenster
	 * @param element	Zu bearbeitendes {@link ModelElementLine}
	 * @param readOnly	Wird dieser Parameter auf <code>true</code> gesetzt, so wird die "Ok"-Schaltfläche deaktiviert
	 */
	public ModelElementLineDialog(final Component owner, final ModelElementLine element, final boolean readOnly) {
		super(owner,Language.tr("Surface.Line.Dialog.Title"),element,"ModelElementLine",readOnly);
	}

	/**
	 * Stellt die Größe des Dialogfensters ein.
	 */
	@Override
	protected void setDialogSize() {
		setMinSizeRespectingScreensize(600,0);
	}

	/**
	 * Liefert die Beschriftung für die Pfeil-Typ-Auswahlbox.
	 * @param p	Punkt an Linienstart oder -ende
	 * @param p1	Linienstart
	 * @param p2	Linienende
	 * @return	Beschriftung für die Pfeil-Typ-Auswahlbox
	 */
	private String getArrowLabel(final Point p, final Point p1, final Point p2) {
		final double deltaX=Math.abs(p1.x-p2.x);
		final double deltaY=Math.abs(p1.y-p2.y);
		final double middleX=(p1.x+p2.x)/2;
		final double middleY=(p1.y+p2.y)/2;

		final List<String> data=new ArrayList<>();

		if (deltaX>deltaY) {
			/* links/rechts */
			if (p.x<middleX) data.add(Language.tr("Surface.Line.Dialog.ArrowPosition.Left")); else data.add(Language.tr("Surface.Line.Dialog.ArrowPosition.Right"));
			if (deltaY>2*deltaX/3) {
				/* alles */
				if (p.y<middleY) data.add(Language.tr("Surface.Line.Dialog.ArrowPosition.Top")); else data.add(Language.tr("Surface.Line.Dialog.ArrowPosition.Bottom"));
			}
		} else {
			/* oben/unten */
			if (p.y<middleY) data.add(Language.tr("Surface.Line.Dialog.ArrowPosition.Top")); else data.add(Language.tr("Surface.Line.Dialog.ArrowPosition.Bottom"));
			if (deltaX>2*deltaY/3) {
				/* alles */
				if (p.x<middleX) data.add(Language.tr("Surface.Line.Dialog.ArrowPosition.Left")); else data.add(Language.tr("Surface.Line.Dialog.ArrowPosition.Right"));
			}
		}

		return String.join(", ",data);
	}

	/**
	 * Erstellt eine Auswahlbox zur Auswahl der Pfeilgröße am Linienende
	 * @param parent	Übergeordnetes Element in das die Auswahlbox eingefügt werden soll
	 * @param title	Beschriftung der Auswahlbox
	 * @param arrowSize	Initial zu wählende Größe (0..3)
	 * @return	Auswahlbox zur Auswahl der Pfeilgröße am Linienende
	 */
	private JComboBox<String> addArrowDropdown(final JPanel parent, final String title, final int arrowSize) {
		final JPanel line=new JPanel(new FlowLayout(FlowLayout.LEFT));
		parent.add(line);

		final JLabel label=new JLabel(title);
		line.add(label);

		final JComboBox<String> combo=new JComboBox<>(new String[] {
				Language.tr("Surface.Line.Dialog.Arrow.Off"),
				Language.tr("Surface.Line.Dialog.Arrow.Small"),
				Language.tr("Surface.Line.Dialog.Arrow.Medium"),
				Language.tr("Surface.Line.Dialog.Arrow.Large")
		});
		combo.setRenderer(new IconListCellRenderer(new Images[]{
				Images.ARROW_SIZE_OFF,
				Images.ARROW_SIZE_SMALL,
				Images.ARROW_SIZE_MEDIUM,
				Images.ARROW_SIZE_LARGE
		}));
		line.add(combo);
		label.setLabelFor(combo);
		combo.setSelectedIndex(Math.max(0,Math.min(3,arrowSize)));

		return combo;
	}

	@Override
	protected String getInfoPanelID() {
		return InfoPanel.stationLine;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected JComponent getContentPanel() {
		final ModelElementLine lineElement=(ModelElementLine)element;

		final JPanel content=new JPanel(new BorderLayout());

		final JPanel sub=new JPanel();
		content.add(sub,BorderLayout.NORTH);
		sub.setLayout(new BoxLayout(sub,BoxLayout.PAGE_AXIS));

		Object[] data;

		data=getLineWidthInputPanel(Language.tr("Surface.Line.Dialog.LineWidth")+":",1,50,lineElement.getLineWidth());
		sub.add((JPanel)data[0]);
		lineWidth=(JComboBox<JLabel>)data[1];
		lineWidth.setEnabled(!readOnly);

		data=getLineWidthTypePanel(Language.tr("Surface.Line.Dialog.LineType")+":",lineElement.getLineType());
		sub.add((JPanel)data[0]);
		lineType=(JComboBox<JLabel>)data[1];
		lineType.setEnabled(!readOnly);

		final Point p1=lineElement.getPosition(true);
		final Point p2=lineElement.getLowerRightPosition();
		arrowStart=addArrowDropdown(sub,Language.tr("Surface.Line.Dialog.ArrowLineStart")+" ("+getArrowLabel(p1,p1,p2)+"):",lineElement.getArrowStart());
		arrowEnd=addArrowDropdown(sub,Language.tr("Surface.Line.Dialog.ArrowLineEnd")+" ("+getArrowLabel(p2,p1,p2)+"):",lineElement.getArrowEnd());

		content.add(colorChooser=new SmallColorChooser(lineElement.getColor()),BorderLayout.CENTER);
		colorChooser.setEnabled(!readOnly);

		return content;
	}

	/**
	 * Speichert die Dialog-Daten in dem zugehörigen Daten-Objekt.<br>
	 * (Diese Routine wird beim Klicken auf "Ok" nach <code>checkData</code> aufgerufen.
	 * @see #checkData()
	 */
	@Override
	protected void storeData() {
		super.storeData();

		final ModelElementLine line=(ModelElementLine)element;

		line.setLineWidth(lineWidth.getSelectedIndex()+1);
		line.setLineType(lineType.getSelectedIndex());
		line.setArrowStart(arrowStart.getSelectedIndex());
		line.setArrowEnd(arrowEnd.getSelectedIndex());
		line.setColor(colorChooser.getColor());
	}
}