/**
 * Copyright 2022 Alexander Herzog
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

import java.io.Serializable;
import java.util.Arrays;

import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JTextField;

import language.Language;
import tools.IconListCellRenderer;
import ui.images.Images;
import ui.modeleditor.ModelElementBaseDialog;

/**
 * Stellt Dialogelemente zur Bearbeitung der Funktionen in {@link AxisDrawer} bereit.
 * @author Alexander Herzog
 * @see AxisDrawer
 */
public class AxisDrawerEdit extends JPanel {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID=-3338560238487581613L;

	/**
	 * Um welche Achse handelt es sich?
	 */
	public enum AxisName {
		/** X-Achse */
		X,
		/** Y-Achse */
		Y
	}

	/**
	 * Beschriftungsmodus
	 */
	private final JComboBox<String> mode;

	/**
	 * Textbeschriftung an der x-Achse (kann <code>null</code> sein)
	 */
	private final JTextField xLabel;

	/**
	 * Textbeschriftung an der y-Achse
	 */
	private final JTextField yLabel;

	/**
	 * Konstruktor der Klasse
	 * @param mode	Beschriftungsmodus
	 * @param xLabel	Textbeschriftung an der x-Achse (kann <code>null</code> sein, wenn es keine x-Achse gibt)
	 * @param yLabel	Textbeschriftung an der y-Achse (kann <code>null</code> sein, wenn es keine y-Achse gibt)
	 * @param readOnly	Nur-Lese-Modus
	 */
	public AxisDrawerEdit(final AxisDrawer.Mode mode, final String xLabel, final String yLabel, final boolean readOnly) {
		this(null,mode,xLabel,yLabel,readOnly);
	}

	/**
	 * Konstruktor der Klasse
	 * @param axisName	Um welche Achse handelt es sich? (Darf <code>null</code>, sein, dann wird nur von der "Achse" ohne weitere Spezifikation gesprochen.)
	 * @param mode	Beschriftungsmodus
	 * @param xLabel	Textbeschriftung an der x-Achse (kann <code>null</code> sein, wenn es keine x-Achse gibt)
	 * @param yLabel	Textbeschriftung an der y-Achse (kann <code>null</code> sein, wenn es keine y-Achse gibt)
	 * @param readOnly	Nur-Lese-Modus
	 */
	@SuppressWarnings("unchecked")
	public AxisDrawerEdit(final AxisName axisName, final AxisDrawer.Mode mode, final String xLabel, final String yLabel, final boolean readOnly) {
		setLayout(new BoxLayout(this,BoxLayout.PAGE_AXIS));

		Object[] data;

		/* Beschriftungsmodus */
		final String labelText;
		if (axisName==null) {
			labelText=Language.tr("AxisDrawer.Mode");
		} else {
			switch (axisName) {
			case X: labelText=Language.tr("AxisDrawer.ModeX"); break;
			case Y: labelText=Language.tr("AxisDrawer.ModeY"); break;
			default: labelText=Language.tr("AxisDrawer.Mode"); break;
			}
		}
		data=ModelElementBaseDialog.getComboBoxPanel(labelText+":",Arrays.asList(
				Language.tr("AxisDrawer.Mode.Off"),
				Language.tr("AxisDrawer.Mode.MinMax"),
				Language.tr("AxisDrawer.Mode.Full")
				));
		add((JPanel)data[0]);
		this.mode=(JComboBox<String>)data[1];
		this.mode.setRenderer(new IconListCellRenderer(new Images[] {
				Images.AXIS_OFF,
				Images.AXIS_MIN_MAX,
				Images.AXIS_FULL
		}));
		this.mode.setSelectedIndex(mode.nr);
		this.mode.setEnabled(!readOnly);

		/* Textbeschriftung an der x-Achse */
		if (xLabel==null) {
			this.xLabel=null;
		} else {
			data=ModelElementBaseDialog.getInputPanel(Language.tr("AxisDrawer.LabelX")+":",xLabel);
			add((JPanel)data[0]);
			this.xLabel=(JTextField)data[1];
			this.xLabel.setEnabled(!readOnly);
		}

		/* Textbeschriftung an der y-Achse */
		if (yLabel==null) {
			this.yLabel=null;
		} else {
			data=ModelElementBaseDialog.getInputPanel(Language.tr("AxisDrawer.LabelY")+":",yLabel);
			add((JPanel)data[0]);
			this.yLabel=(JTextField)data[1];
			this.yLabel.setEnabled(!readOnly);
		}
	}

	/**
	 * Stellt die Werte in dem Dialogelement ein.
	 * @param mode	Beschriftungsmodus
	 * @param xLabel	Textbeschriftung an der x-Achse (kann <code>null</code> sein, wenn es keine x-Achse gibt)
	 * @param yLabel	Textbeschriftung an der y-Achse (kann <code>null</code> sein, wenn es keine y-Achse gibt)
	 */
	public void set(final AxisDrawer.Mode mode, final String xLabel, final String yLabel) {
		this.mode.setSelectedIndex(mode.nr);
		if (this.xLabel!=null && xLabel!=null) this.xLabel.setText(xLabel);
		if (this.yLabel!=null && yLabel!=null) this.yLabel.setText(yLabel);
	}

	/**
	 * Liefert den Beschriftungsmodus.
	 * @return	Beschriftungsmodus
	 */
	public AxisDrawer.Mode getMode() {
		return AxisDrawer.Mode.fromNr(mode.getSelectedIndex());
	}

	/**
	 * Liefert die Textbeschriftung an der x-Achse.
	 * @return	Textbeschriftung an der x-Achse (ist <code>null</code>, wenn es keine x-Achse gibt)
	 */
	public String getXLabel() {
		if (xLabel==null) return null;
		return xLabel.getText().trim();
	}

	/**
	 * Liefert die Textbeschriftung an der y-Achse.
	 * @return	Textbeschriftung an der y-Achse (ist <code>null</code>, wenn es keine y-Achse gibt)
	 */
	public String getYLabel() {
		if (yLabel==null) return null;
		return yLabel.getText().trim();
	}
}
