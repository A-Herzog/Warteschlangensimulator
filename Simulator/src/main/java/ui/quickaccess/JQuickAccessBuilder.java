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
package ui.quickaccess;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import javax.swing.Icon;
import javax.swing.ImageIcon;

/**
 * Stellt eine Liste mit Schnellzugriffeinträgen basierend auf der Eingabe der Nutzers zusammen
 * @author Alexander Herzog
 * @see JQuickAccessTextField#getQuickAccessRecords(String)
 */
public class JQuickAccessBuilder {
	/** Liste der Schnellzugriffsergebnissen */
	private final List<JQuickAccessRecord> list;
	/** Eingegebener Text in Kleinbuchstaben */
	private final String quickAccessTextLower;
	private final boolean searchInPreText;

	/**
	 * Kategorie für die aktuell zu erzeugenden Einträge
	 */
	protected final String category;

	/**
	 * Vorgegebener Tooltip-Text für die Einträge in der aktuellen Kategorie
	 */
	protected final String categoryTooltip;

	/**
	 * Eingegebener Text
	 */
	protected final String quickAccessText;

	/**
	 * Konstruktor der Klasse
	 * @param category	Kategorie für die aktuell zu erzeugenden Einträge
	 * @param categoryTooltip	Tooltip für die aktuell zu erzeugenden Einträge
	 * @param quickAccessText	Eingegebener Text
	 * @param searchInPreText	Soll auch in den Vorspanntexten gesucht werden?
	 */
	public JQuickAccessBuilder(final String category, final String categoryTooltip, final String quickAccessText, final boolean searchInPreText) {
		this.list=new ArrayList<>();
		this.category=category;
		this.categoryTooltip=categoryTooltip;
		this.quickAccessText=(quickAccessText==null)?"":quickAccessText;
		this.quickAccessTextLower=(quickAccessText==null)?"":quickAccessText.toLowerCase();
		this.searchInPreText=searchInPreText;
	}

	private String buildResultText(final String pre, final String text, final int index1, final int index2) {
		final StringBuilder sb=new StringBuilder();
		sb.append("<html><body>");

		if (index1>=0 && pre!=null) {
			if (index1>0) sb.append(pre.substring(0,index1));
			sb.append("<b>");
			sb.append(pre.subSequence(index1,index1+quickAccessTextLower.length()));
			sb.append("</b>");
			if (index1+quickAccessTextLower.length()<pre.length()) sb.append(pre.substring(index1+quickAccessTextLower.length()));
		} else {
			if (pre!=null) sb.append(pre);
		}

		if (pre!=null) sb.append(" - ");
		sb.append("<span style='color: blue'>");

		if (index2>=0) {
			if (index2>0) sb.append(text.substring(0,index2));
			sb.append("<b>");
			sb.append(text.subSequence(index2,index2+quickAccessTextLower.length()));
			sb.append("</b>");
			if (index2+quickAccessTextLower.length()<text.length()) sb.append(text.substring(index2+quickAccessTextLower.length()));
		} else {
			sb.append(text);
		}

		sb.append("</span>");
		sb.append("</body></html>");

		return sb.toString();
	}

	private String test(final String pre, final String text) {
		final int index1=(pre!=null)?pre.toLowerCase().indexOf(quickAccessTextLower):-1;
		final int index2=text.toLowerCase().indexOf(quickAccessTextLower);
		if (searchInPreText) {
			if (index1<0 && index2<0) return null;
		} else {
			if (index2<0) return null;
		}

		return buildResultText(pre,text,index1,index2);
	}

	private String testPlainResult(final String pre, final String text) {
		final int index1=(pre!=null)?pre.toLowerCase().indexOf(quickAccessTextLower):-1;
		final int index2=text.toLowerCase().indexOf(quickAccessTextLower);
		if (searchInPreText) {
			if (index1<0 && index2<0) return null;
		} else {
			if (index2<0) return null;
		}

		return buildResultText(pre,text,-1,-1);
	}

	/**
	 * Prüft, ob ein Datensatz zu dem Suchbegriff passt und erstellt ggf. einen Schnellzugriffeintrag
	 * @param pre	Vorspanntext (darf <code>null</code> sein)
	 * @param text	Eigentlicher Text für den Eintrag
	 * @param moreTexts	Optionale weitere Text, die bei der Suche berücksichtigt werden, aber nicht in die Ergebnisanzeige einbezogen werden
	 * @param tooltip	Anzuzeigender Tooltip (wird <code>null</code> übergeben, so wird der Standardtext verwendet)
	 * @param icon	Optionales Icon (darf <code>null</code> sein)
	 * @param action	Beim Anklicken auszuführende Aktion
	 * @param data	Zusätzliche Daten die in dem Schnellzugriffeintrag hinterlegt werden sollen (und später von der Aktion ausgewertet werden können)
	 */
	public void test(final String pre, final String text, final String[] moreTexts, final String tooltip, final Icon icon, final Consumer<JQuickAccessRecord> action, final Object data) {
		final String textDisplay=test(pre,text);
		if (textDisplay!=null) {
			list.add(new JQuickAccessRecord(category,text,textDisplay,(tooltip==null)?categoryTooltip:tooltip,icon,action,data));
			return;
		}

		if (moreTexts!=null) for (String more: moreTexts) {
			final String moreDisplay=testPlainResult(pre,more);
			if (moreDisplay!=null) {
				list.add(new JQuickAccessRecord(category,text,textDisplay,(tooltip==null)?categoryTooltip:tooltip,icon,action,data));
				return;
			}
		}
	}

	/**
	 * Prüft, ob ein Datensatz zu dem Suchbegriff passt und erstellt ggf. einen Schnellzugriffeintrag
	 * @param pre	Vorspanntext (darf <code>null</code> sein)
	 * @param text	Eigentlicher Text für den Eintrag
	 * @param tooltip	Anzuzeigender Tooltip (wird <code>null</code> übergeben, so wird der Standardtext verwendet)
	 * @param icon	Optionales Icon (darf <code>null</code> sein)
	 * @param action	Beim Anklicken auszuführende Aktion
	 * @param data	Zusätzliche Daten die in dem Schnellzugriffeintrag hinterlegt werden sollen (und später von der Aktion ausgewertet werden können)
	 */
	public void test(final String pre, final String text, final String tooltip, final URL icon, final Consumer<JQuickAccessRecord> action, final Object data) {
		final String textDisplay=test(pre,text);
		if (textDisplay==null) return;

		ImageIcon iconObj=null;
		if (icon!=null) iconObj=new ImageIcon(icon);

		list.add(new JQuickAccessRecord(category,text,textDisplay,(tooltip==null)?categoryTooltip:tooltip,iconObj,action,data));
	}

	/**
	 * Prüft, ob ein Datensatz zu dem Suchbegriff passt und erstellt ggf. einen Schnellzugriffeintrag
	 * @param pre	Vorspanntext (darf <code>null</code> sein)
	 * @param text	Eigentlicher Text für den Eintrag
	 * @param icon	Optionales Icon (darf <code>null</code> sein)
	 * @param action	Beim Anklicken auszuführende Aktion
	 * @param data	Zusätzliche Daten die in dem Schnellzugriffeintrag hinterlegt werden sollen (und später von der Aktion ausgewertet werden können)
	 */
	public void test(final String pre, final String text, final Icon icon, final Consumer<JQuickAccessRecord> action, final Object data) {
		test(pre,text,null,null,icon,action,data);
	}

	/**
	 * Prüft, ob ein Datensatz zu dem Suchbegriff passt und erstellt ggf. einen Schnellzugriffeintrag
	 * @param pre	Vorspanntext (darf <code>null</code> sein)
	 * @param text	Eigentlicher Text für den Eintrag
	 * @param moreTexts	Optionale weitere Text, die bei der Suche berücksichtigt werden, aber nicht in die Ergebnisanzeige einbezogen werden
	 * @param icon	Optionales Icon (darf <code>null</code> sein)
	 * @param action	Beim Anklicken auszuführende Aktion
	 * @param data	Zusätzliche Daten die in dem Schnellzugriffeintrag hinterlegt werden sollen (und später von der Aktion ausgewertet werden können)
	 */
	public void test(final String pre, final String text, final String[] moreTexts, final Icon icon, final Consumer<JQuickAccessRecord> action, final Object data) {
		test(pre,text,moreTexts,null,icon,action,data);
	}

	/**
	 * Prüft, ob ein Datensatz zu dem Suchbegriff passt und erstellt ggf. einen Schnellzugriffeintrag
	 * @param pre	Vorspanntext (darf <code>null</code> sein)
	 * @param text	Eigentlicher Text für den Eintrag
	 * @param icon	Optionales Icon (darf <code>null</code> sein)
	 * @param action	Beim Anklicken auszuführende Aktion
	 * @param data	Zusätzliche Daten die in dem Schnellzugriffeintrag hinterlegt werden sollen (und später von der Aktion ausgewertet werden können)
	 */
	public void test(final String pre, final String text, final URL icon, final Consumer<JQuickAccessRecord> action, final Object data) {
		test(pre,text,null,icon,action,data);
	}

	/**
	 * Prüft, ob ein Datensatz zu dem Suchbegriff passt und erstellt ggf. einen Schnellzugriffeintrag
	 * @param pre	Vorspanntext (darf <code>null</code> sein)
	 * @param text	Eigentlicher Text für den Eintrag
	 * @param tooltip	Anzuzeigender Tooltip (wird <code>null</code> übergeben, so wird der Standardtext verwendet)
	 * @param icon	Optionales Icon (darf <code>null</code> sein)
	 * @param action	Beim Anklicken auszuführende Aktion
	 */
	public void test(final String pre, final String text, final String tooltip, final Icon icon, final Consumer<JQuickAccessRecord> action) {
		test(pre,text,null,tooltip,icon,action,null);
	}

	/**
	 * Prüft, ob ein Datensatz zu dem Suchbegriff passt und erstellt ggf. einen Schnellzugriffeintrag
	 * @param pre	Vorspanntext (darf <code>null</code> sein)
	 * @param text	Eigentlicher Text für den Eintrag
	 * @param tooltip	Anzuzeigender Tooltip (wird <code>null</code> übergeben, so wird der Standardtext verwendet)
	 * @param icon	Optionales Icon (darf <code>null</code> sein)
	 * @param action	Beim Anklicken auszuführende Aktion
	 */
	public void test(final String pre, final String text, final String tooltip, final URL icon, final Consumer<JQuickAccessRecord> action) {
		test(pre,text,tooltip,icon,action,null);
	}

	/**
	 * Prüft, ob ein Datensatz zu dem Suchbegriff passt und erstellt ggf. einen Schnellzugriffeintrag
	 * @param pre	Vorspanntext (darf <code>null</code> sein)
	 * @param text	Eigentlicher Text für den Eintrag
	 * @param icon	Optionales Icon (darf <code>null</code> sein)
	 * @param action	Beim Anklicken auszuführende Aktion
	 */
	public void test(final String pre, final String text, final Icon icon, final Consumer<JQuickAccessRecord> action) {
		test(pre,text,null,icon,action,null);
	}

	/**
	 * Prüft, ob ein Datensatz zu dem Suchbegriff passt und erstellt ggf. einen Schnellzugriffeintrag
	 * @param pre	Vorspanntext (darf <code>null</code> sein)
	 * @param text	Eigentlicher Text für den Eintrag
	 * @param icon	Optionales Icon (darf <code>null</code> sein)
	 * @param action	Beim Anklicken auszuführende Aktion
	 */
	public void test(final String pre, final String text, final URL icon, final Consumer<JQuickAccessRecord> action) {
		test(pre,text,null,icon,action,null);
	}

	/**
	 * Liefert die erstellte Liste mit den Schnellzugriffeinträgen
	 * @return	Liste mit den Schnellzugriffeinträgen
	 */
	public List<JQuickAccessRecord> getList() {
		return list;
	}

	/**
	 * Liefert die erstellte Liste mit den Schnellzugriffeinträgen
	 * @param maxCount	Maximalzahl an zu liefernden Einträgen
	 * @return	Liste mit den Schnellzugriffeinträgen
	 */
	public List<JQuickAccessRecord> getList(final int maxCount) {
		if (list.size()<=maxCount || maxCount<=0) return list;
		return list.stream().limit(maxCount).collect(Collectors.toList());
	}

	/**
	 * Liefert den Namen der Kategorie für die aktuell zu erzeugenden Einträge
	 * @return	Kategorie für die aktuell zu erzeugenden Einträge
	 */
	public String getCategory() {
		return category;
	}
}