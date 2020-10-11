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
package ui.modelproperties;

import java.awt.Component;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.Serializable;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JTextField;

import language.Language;
import mathtools.NumberTools;
import systemtools.BaseDialog;
import systemtools.MsgBox;
import ui.modeleditor.ModelElementBaseDialog;

/**
 * Dialog zur Einstellung der Transporter-Fahrzeiten-Matrix auf Basis der
 * Abstände der Stationen auf der Zeichenfläche; Konfiguration von minimalem
 * und maximalem zeitlichem Abstand.
 * @author Alexander Herzog
 * @see TransporterDistancesTableModel
 */
public class TransporterDistancesTableModelScaleDialog extends BaseDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = 2381393842257178303L;

	private final JTextField minDistance;
	private final JTextField maxDistance;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Element
	 * @param help	Hilfe-Callback
	 */
	public TransporterDistancesTableModelScaleDialog(final Component owner, final Runnable help) {
		super(owner,Language.tr("Transporters.Group.Edit.Dialog.Distances.Dialog.Title"));

		final JPanel content=createGUI(help);
		content.setLayout(new BoxLayout(content,BoxLayout.PAGE_AXIS));

		Object[] data;

		data=ModelElementBaseDialog.getInputPanel(Language.tr("Transporters.Group.Edit.Dialog.Distances.Dialog.MinDistance")+":","10",10);
		content.add((JPanel)data[0]);
		minDistance=(JTextField)data[1];
		minDistance.addKeyListener(new KeyListener() {
			@Override public void keyTyped(KeyEvent e) {checkData(false);}
			@Override public void keyReleased(KeyEvent e) {checkData(false);}
			@Override public void keyPressed(KeyEvent e) {checkData(false);}
		});

		data=ModelElementBaseDialog.getInputPanel(Language.tr("Transporters.Group.Edit.Dialog.Distances.Dialog.MaxDistance")+":","100",10);
		content.add((JPanel)data[0]);
		maxDistance=(JTextField)data[1];
		maxDistance.addKeyListener(new KeyListener() {
			@Override public void keyTyped(KeyEvent e) {checkData(false);}
			@Override public void keyReleased(KeyEvent e) {checkData(false);}
			@Override public void keyPressed(KeyEvent e) {checkData(false);}
		});

		pack();
		setLocationRelativeTo(this.owner);
		setVisible(true);
	}

	/**
	 * Prüft, ob die eingegebenen Daten in Ordnung sind.
	 * @param showErrorMessage	Wird hier <code>true</code> übergeben, so wird eine Fehlermeldung ausgegeben, wenn die Daten nicht in Ordnung sind.
	 * @return	Gibt <code>true</code> zurück, wenn die Daten in Ordnung sind.
	 */
	private boolean checkData(final boolean showErrorMessage) {
		boolean ok=true;

		final Double D1=NumberTools.getPositiveDouble(minDistance,true);
		if (D1==null) {
			ok=false;
			if (showErrorMessage) {
				MsgBox.error(this,Language.tr("Transporters.Group.Edit.Dialog.Distances.Dialog.MinDistance.Error"),String.format(Language.tr("Transporters.Group.Edit.Dialog.Distances.Dialog.MinDistance.ErrorInfo"),minDistance.getText()));
				return false;
			}
		}

		final Double D2=NumberTools.getPositiveDouble(maxDistance,true);
		if (D2==null) {
			ok=false;
			if (showErrorMessage) {
				MsgBox.error(this,Language.tr("Transporters.Group.Edit.Dialog.Distances.Dialog.MaxDistance.Error"),String.format(Language.tr("Transporters.Group.Edit.Dialog.Distances.Dialog.MaxDistance.ErrorInfo"),maxDistance.getText()));
				return false;
			}
		}

		if (ok && D1!=null && D2!=null) {
			if (D1.doubleValue()>=D2.doubleValue()) {
				ok=false;
				if (showErrorMessage) {
					MsgBox.error(this,Language.tr("Transporters.Group.Edit.Dialog.Distances.Dialog.Distance.Error"),Language.tr("Transporters.Group.Edit.Dialog.Distances.Dialog.Distance.ErrorInfo"));
					return false;
				}
			}
		}

		return ok;
	}

	@Override
	protected boolean checkData() {
		return checkData(true);
	}

	/**
	 * Liefert den eingestellten minimalen zeitlichen Abstand
	 * @return	Minimaler zeitlicher Abstand
	 */
	public double getMinDistance() {
		return NumberTools.getPositiveDouble(minDistance,true);
	}

	/**
	 * Liefert den eingestellten maximalen zeitlichen Abstand
	 * @return	Maximaler zeitlicher Abstand
	 */
	public double getMaxDistance() {
		return NumberTools.getPositiveDouble(maxDistance,true);
	}
}