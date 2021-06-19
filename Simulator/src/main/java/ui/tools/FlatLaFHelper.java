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

import java.awt.Color;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;

import javax.swing.UIManager;

import org.oxbow.swingbits.dialog.task.ICommandLinkPainter;
import org.oxbow.swingbits.dialog.task.TaskDialog;
import org.oxbow.swingbits.dialog.task.design.WindowsCommandLinkPainter;
import org.oxbow.swingbits.dialog.task.design.WindowsContentDesign;
import org.oxbow.swingbits.util.OperatingSystem;

import com.formdev.flatlaf.FlatDarculaLaf;
import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatIntelliJLaf;
import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.FlatLightLaf;

/**
 * Helferklasse die die Nutzung der Flag Look &amp; Feels
 * im Simulator erleichtert
 * @author Alexander Herzog
 */
public class FlatLaFHelper {
	/**
	 * Liste der verfügbaren Flat Look &amp; Feel Klassen
	 */
	private static List<Class<? extends FlatLaf>> lafs=Arrays.asList(
			FlatLightLaf.class,
			FlatDarkLaf.class,
			FlatDarculaLaf.class,
			FlatIntelliJLaf.class
			);

	/**
	 * Liefert den Namen eines Flat Look &amp; Feels
	 * @param laf	Flat Look &amp; Feel Klasse zu der der Name bestimmt werden soll
	 * @return	Name des Look &amp; Feels
	 */
	private static String getName(Class<? extends FlatLaf> laf) {
		try {
			return laf.getDeclaredConstructor().newInstance().getName();
		} catch (InstantiationException|IllegalAccessException|IllegalArgumentException|InvocationTargetException|NoSuchMethodException|SecurityException e) {
			return "";
		}
	}

	/**
	 * Registriert die Flat Look &amp; Feels für den UI-Manager.
	 */
	public static void init() {
		lafs.forEach(laf->{try {UIManager.installLookAndFeel(new UIManager.LookAndFeelInfo(getName(laf),laf.getName()));} catch (SecurityException | IllegalArgumentException e) {}});
	}

	/**
	 * Prüft, ob eines der Flat Look &amp; Feels aktiv ist.
	 * @return	Liefert <code>true</code>, wenn eines der Flat Look &amp; Feels aktiv ist
	 */
	public static boolean isActive() {
		final String currentLaFName=UIManager.getLookAndFeel().getName();
		return lafs.stream().map(laf->getName(laf)).filter(name->name.equals(currentLaFName)).findFirst().isPresent();
	}

	/**
	 * Nimmt, wenn eines der Flat Look &amp; Feels aktiv ist,
	 * weitere Konfigurationen vor.
	 */
	public static void setup() {
		if (!isActive()) return;

		UIManager.put("ScrollBar.showButtons",true);
		UIManager.put("ScrollBar.width",16);

		if (isDark()) {
			UIManager.put("TaskDialog.messageBackground",Color.DARK_GRAY);
			UIManager.put("TaskDialog.instructionForeground",Color.WHITE);
			switch(OperatingSystem.getCurrent()) {
			case MACOS:
				/* Es wird der MacOsCommandLinkPainter verwendet, der keine Farbanpassungen vorsieht */
				break;
			case LINUX:
				/* Es wird der MacOsCommandLinkPainter verwendet (ja, für Linux), der keine Farbanpassungen vorsieht */
				break;
			case WINDOWS:
				System.setProperty(TaskDialog.DESIGN_PROPERTY,TaskDialogWindowsDarkDesign.class.getName());
				break;
			default:
				/* Keine Layoutanpassungen */
				break;
			}
		}
	}

	/**
	 * Sollen Titelzeile und Menüzeile kombiniert werden?
	 * @return	Liefert <code>true</code>, wenn Titelzeile und Menüzeile kombiniert werden sollen
	 * @see #setCombinedMenuBar(boolean)
	 */
	public static boolean isCombinedMenuBar() {
		if (!isActive()) return false;

		final Object o=UIManager.get("TitlePane.useWindowDecorations");
		if (!(o instanceof Boolean)) return true;
		final boolean b=(Boolean)o;
		return b;
	}

	/**
	 * Stellt ein, ob Titelzeile und Menüzeile kombiniert werden sollen.
	 * @param combined	Titelzeile und Menüzeile kombinieren?
	 * @see #isCombinedMenuBar()
	 */
	public static void setCombinedMenuBar(final boolean combined) {
		UIManager.put("TitlePane.useWindowDecorations",combined);
	}

	/**
	 * Prüft, ob eines der Flat Look &amp; Feels aktiv ist und dieses ein Dark-Theme ist.
	 * @return	Liefert <code>true</code>, wenn eines der Flat Look &amp; Feels aktiv ist und dieses ein Dark-Theme ist
	 */
	public static boolean isDark() {
		if (!isActive()) return false;
		if (!(UIManager.getLookAndFeel() instanceof FlatLaf)) return false;
		final FlatLaf laf=(FlatLaf)UIManager.getLookAndFeel();
		return laf.isDark();
	}

	/**
	 * Angepasstes Layout für die TaskDialog-Meldungen unter Windows im Dark-Modus
	 * @see FlatLaFHelper#setup()
	 */
	public static class TaskDialogWindowsDarkDesign extends WindowsContentDesign {
		@Override
		public ICommandLinkPainter getCommandLinkPainter() {
			if (commandButtonPainter==null) commandButtonPainter=new TaskDialogWindowsDarkCommandLinkPainter();
			return commandButtonPainter;
		}
	}

	/**
	 * Farbschema für die einzelnen Einträge in einem TaskDialog unter Windows im Dark-Modus
	 * @see TaskDialogWindowsDarkDesign
	 * @see FlatLaFHelper#setup()
	 */
	public static class TaskDialogWindowsDarkCommandLinkPainter extends WindowsCommandLinkPainter {
		/** {@link WindowsCommandLinkPainter} */
		private LinkChrome selectedChrome=new LinkChrome(Color.GRAY,Color.DARK_GRAY,Color.GRAY,1,7);
		/** {@link WindowsCommandLinkPainter} */
		private LinkChrome armedChrome=new LinkChrome(Color.LIGHT_GRAY,Color.GRAY,new Color(0xB9D7FC),1,7);
		/** {@link WindowsCommandLinkPainter} */
		private LinkChrome rolloverChrome=new LinkChrome(Color.LIGHT_GRAY,Color.GRAY,new Color(0xB9D7FC),1,7);

		@Override
		protected LinkChrome getLinkChrome(LinkState linkState) {
			if (linkState==LinkState.SELECTED) return selectedChrome;
			if (linkState==LinkState.ARMED) return armedChrome;
			if (linkState==LinkState.ROLLOVER) return rolloverChrome;
			return selectedChrome;
			/* Die folgende Zeile geht nicht, da LinkState protected ist und daher LinkState.values() nicht aufrufbar ist, diese aber in switch verwendet wird. */
			/*
			switch( linkState ) {
			case SELECTED: return selectedChrome;
			case ARMED   : return armedChrome;
			case ROLLOVER: return rolloverChrome;
			default      : return selectedChrome;
			}
			 */
		}
	}

	/**
	 * Stellt die Farbe für die kombinierte Titel- und Menüzeile ein.
	 * @param color	Farbe
	 */
	public static void setTitleColor(final Color color) {
		UIManager.put("TitlePane.background",color);
		UIManager.put("TitlePane.inactiveBackground",color.darker());
		UIManager.put("TitlePane.foreground",Color.WHITE);
		UIManager.put("TitlePane.inactiveForeground",Color.LIGHT_GRAY);
		UIManager.put("TitlePane.activeForeground",Color.WHITE);
		UIManager.put("MenuBar.foreground",Color.WHITE);
		UIManager.put("TitlePane.embeddedForeground",Color.WHITE);
	}

	/**
	 * Stellt die Farbe für die kombinierte Titel- und Menüzeile ein.
	 * @param color	Farbe (wird vorher noch abgedunkelt)
	 */
	public static void setTitleColorDarker(final Color color) {
		setTitleColor(color.darker().darker());
	}
}
