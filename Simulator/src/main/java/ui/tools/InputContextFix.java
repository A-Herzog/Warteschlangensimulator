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
import java.awt.event.MouseEvent;
import java.awt.im.InputContext;
import java.beans.Transient;
import java.lang.Character.Subset;
import java.util.Locale;

import javax.swing.JTextField;

/**
 * Diese InputContext-Implementierung kann die Standard-InputContext-Implementierung
 * von {@link JTextField}-Elementen, die sich im Hauptfenster befinden ersetzen.
 * Sie filtert einige Nachrichten aus, die sonst bei Drag&amp;Drop-Operationen �ber
 * das Fenster zum pl�tzlichen Einfrieren des AWT-Threads f�hren k�nnen.
 * <br><br>
 * Zur Verwendung dieser Klasse muss {@link JTextField#getInputContext()} �berschrieben werden:
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
	 * Wie viele Sekunden nach dem Programmstart soll diese Schutzfunktion aktiv bleiben?
	 */
	private final static long CRITICAL_SECONDS=10;

	/**
	 * Original-Input-Context an den die Funktionsaufrufe weiter geleitet werden
	 */
	private final InputContext originalInputContext;

	/**
	 * Haupt-Java-Versionsnummer
	 */
	private final int javaVersion;

	/**
	 * Zeitpunkt zu dem diese Umleitung aktiviert wurde.
	 */
	private final long startTime;

	/**
	 * Wurde die Umleitung bereits deaktiviert?
	 * (Dann muss nicht mehr bei jedem Ereignis erneut gepr�ft werden, ob sie jetzt deaktiviert werden kann.)
	 */
	private boolean backToNormal;

	/**
	 * Konstruktor der Klasse
	 * @param originalInputContext	Original-Input-Context an den die Funktionsaufrufe weiter geleitet werden
	 */
	public InputContextFix(final InputContext originalInputContext) {
		this.originalInputContext=originalInputContext;

		startTime=System.currentTimeMillis();
		backToNormal=false;

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
		if (originalInputContext==null) return false;
		return originalInputContext.selectInputMethod(locale);
	}

	@Override
	public Locale getLocale() {
		if (originalInputContext==null) return Locale.getDefault();
		return originalInputContext.getLocale();
	}

	@Override
	public void setCharacterSubsets(Subset[] subsets) {
		if (originalInputContext!=null) originalInputContext.setCharacterSubsets(subsets);
	}

	@Override
	public void setCompositionEnabled(boolean enable) {
		if (originalInputContext!=null) originalInputContext.setCompositionEnabled(enable);
	}

	@Override
	@Transient
	public boolean isCompositionEnabled() {
		if (originalInputContext==null) return false;
		return originalInputContext.isCompositionEnabled();
	}

	@Override
	public void reconvert() {
		if (originalInputContext!=null) originalInputContext.reconvert();
	}

	@Override
	public void dispatchEvent(AWTEvent event) {
		if (!backToNormal && javaVersion>11) {
			final long time=System.currentTimeMillis();
			if (time>startTime+CRITICAL_SECONDS*1000L) {
				backToNormal=true;
			} else {
				/* Diese Ereignisse f�hren in Java&gt;11 dazu, dass der AWT-Thread innerhalb einer nativen Methode blockiert. Daher m�ssen wir sie ausfiltern. */
				final int id=event.getID();
				if (id==FocusEvent.FOCUS_LOST || id==FocusEvent.FOCUS_GAINED) return;
				if (id==MouseEvent.MOUSE_ENTERED || id==MouseEvent.MOUSE_MOVED) return;
			}
		}

		if (originalInputContext!=null) originalInputContext.dispatchEvent(event);
	}

	@Override
	public void removeNotify(Component client) {
		if (originalInputContext!=null) originalInputContext.removeNotify(client);
	}

	@Override
	public void endComposition() {
		if (originalInputContext!=null) originalInputContext.endComposition();
	}

	@Override
	public void dispose() {
		if (originalInputContext!=null) originalInputContext.dispose();
	}

	@Override
	public Object getInputMethodControlObject() {
		if (originalInputContext==null) return null;
		return originalInputContext.getInputMethodControlObject();
	}
}
