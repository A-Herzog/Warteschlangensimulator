/**
 * Copyright 2023 Alexander Herzog
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
package systemtools.statistics;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.io.Serializable;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import systemtools.BaseDialog;

/**
 * Dialog zur Eingabe eines Suchbegriffs und weiterer Einstellungen zur Suche
 * @author Alexander Herzog
 * @see StatisticViewerText#search(Component)
 * @see StatisticViewerTable#search(Component)
 */
public class StatisticViewerSearchDialog extends BaseDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID=-2432502895042476915L;

	/**
	 * Eingabezeile für Suchbegriff
	 */
	private final JTextField editSearchString;

	/**
	 * Option: "Groß- und Kleinschreibung berücksichtigen"
	 */
	private final JCheckBox optionCaseSensitive;

	/**
	 * Option: "Suchbegriff ist regulärer Ausdruck"
	 */
	private final JCheckBox optionRegularExpression;

	/**
	 * Konstruktor
	 * @param owner	Übergeordnetes Element
	 * @param lastSearchString	Suchbegriff beim letzten Aufruf der Suchfunktion (darf <code>null</code> sein)
	 * @param lastCaseSensitive	Status "Groß- und Kleinschreibung beachten" beim letzten Aufruf der Suchfunktion
	 * @param lastRegularExpression	Status "Suchbegriff ist regulärer Ausdruck" beim letzten Aufruf der Suchfunktion
	 */
	public StatisticViewerSearchDialog(final Component owner, final String lastSearchString, final boolean lastCaseSensitive, final boolean lastRegularExpression) {
		super(owner,StatisticsBasePanel.viewersToolbarSearchTitle);

		/* GUI */
		final JPanel content=createGUI(null);
		content.setLayout(new BorderLayout());
		final JPanel settings=new JPanel();
		content.add(settings,BorderLayout.NORTH);
		settings.setLayout(new BoxLayout(settings,BoxLayout.PAGE_AXIS));

		JPanel line;

		/* Eingabezeile für Suchbegriff */
		settings.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		final JLabel label=new JLabel(StatisticsBasePanel.viewersToolbarSearchString+":");
		line.add(label);
		editSearchString=new JTextField((lastSearchString==null)?"":lastSearchString,20);
		BaseDialog.addUndoFeature(editSearchString);
		line.add(editSearchString);
		label.setLabelFor(editSearchString);

		/* Option: "Groß- und Kleinschreibung berücksichtigen" */
		settings.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(optionCaseSensitive=new JCheckBox(StatisticsBasePanel.viewersToolbarSearchCaseSensitive,lastCaseSensitive));

		/* Option: "Suchbegriff ist regulärer Ausdruck" */
		settings.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(optionRegularExpression=new JCheckBox(StatisticsBasePanel.viewersToolbarSearchRegEx,lastRegularExpression));

		/* Dialog starten */
		pack();
		setLocationRelativeTo(getOwner());
		setVisible(true);
	}

	/**
	 * Liefert den eingegebenen neuen Suchbegriff
	 * @return	Suchbegriff
	 */
	public String getSearchString() {
		return editSearchString.getText().trim();
	}

	/**
	 * Liefert den Wert der Einstellung "Groß- und Kleinschreibung berücksichtigen"
	 * @return	Groß- und Kleinschreibung berücksichtigen
	 */
	public boolean isCaseSensitive() {
		return optionCaseSensitive.isSelected();
	}

	/**
	 * Liefert den Wert der Einstellung "Suchbegriff ist regulärer Ausdruck"
	 * @return	Suchbegriff ist regulärer Ausdruck
	 */
	public boolean isRegularExpression() {
		return optionRegularExpression.isSelected();
	}

}
