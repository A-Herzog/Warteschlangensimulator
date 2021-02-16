/**
 * Copyright 2021 Alexander Herzog
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
package ui.tools;

import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.event.FocusEvent;
import java.awt.im.InputContext;
import java.beans.Transient;
import java.lang.Character.Subset;
import java.util.Locale;

import javax.swing.JTextField;

/**
 * Diese InputContext-Implementierung kann die Standard-InputContext-Implementierung
 * von {@link JTextField}-Elementen, die sich im Hauptfenster befinden ersetzen.
 * Sie filtert einige Nachrichten aus, die sonst bei Drag&amp;Drop-Operationen über
 * das Fenster zum plötzlichen Einfrieren des AWT-Threads führen können.
 * <br><br>
 * Zur Verwendung dieser Klasse muss {@link JTextField#getInputContext()} überschrieben werden:
 * <pre>
 * new JTextField() {
 *   public InputContext getInputContext() {
 *     return new InputContextFix(super.getInputContext());
 *   }
 * }
 * </pre>
 * @author Alexander Herzog
 */
public class InputContextFix extends InputContext {
	/**
	 * Original-Input-Context an den die Funktionsaufrufe weiter geleitet werden
	 */
	private final InputContext originalInputContext;

	/**
	 * Haupt-Java-Versionsnummer
	 */
	private final int javaVersion;

	/**
	 * Konstruktor der Klasse
	 * @param originalInputContext	Original-Input-Context an den die Funktionsaufrufe weiter geleitet werden
	 */
	public InputContextFix(final InputContext originalInputContext) {
		this.originalInputContext=originalInputContext;

		/* Schutzfunktion soll nur bei Java&gt;11 verwendet werden */
		final String version=System.getProperty("java.version");
		if (version==null) {
			javaVersion=99;
		} else {
			if (version.startsWith("1.8")) {
				javaVersion=8;
			} else {
				int ver=99;
				try {ver=Integer.parseInt(version.split("\\.")[0]);} catch (Exception e) {ver=99;}
				javaVersion=ver;
			}
		}
	}

	@Override
	public boolean selectInputMethod(Locale locale) {
		return originalInputContext.selectInputMethod(locale);
	}

	@Override
	public Locale getLocale() {
		return originalInputContext.getLocale();
	}

	@Override
	public void setCharacterSubsets(Subset[] subsets) {
		originalInputContext.setCharacterSubsets(subsets);
	}

	@Override
	public void setCompositionEnabled(boolean enable) {
		originalInputContext.setCompositionEnabled(enable);
	}

	@Override
	@Transient
	public boolean isCompositionEnabled() {
		return originalInputContext.isCompositionEnabled();
	}

	@Override
	public void reconvert() {
		originalInputContext.reconvert();
	}

	@Override
	public void dispatchEvent(AWTEvent event) {
		/* Diese Ereignisse führen in Java&gt;11 dazu, dass der AWT-Thread innerhalb einer nativen Methode blockiert. Daher müssen wir sie ausfiltern. */
		final int id=event.getID();
		if ((javaVersion>11) && (id==FocusEvent.FOCUS_LOST || id==FocusEvent.FOCUS_GAINED)) return;

		originalInputContext.dispatchEvent(event);
	}

	@Override
	public void removeNotify(Component client) {
		originalInputContext.removeNotify(client);
	}

	@Override
	public void endComposition() {
		originalInputContext.endComposition();
	}

	@Override
	public void dispose() {
		originalInputContext.dispose();
	}

	@Override
	public Object getInputMethodControlObject() {
		return originalInputContext.getInputMethodControlObject();
	}
}
