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
package ui.script;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rtextarea.RTextScrollPane;

import language.Language;
import mathtools.distribution.swing.CommonVariables;
import mathtools.distribution.tools.FileDropperData;
import scripting.java.DynamicFactory;
import scripting.js.JSRunDataFilterTools;
import simulator.editmodel.EditModel;
import simulator.statistics.Statistics;
import systemtools.MsgBox;
import tools.IconListCellRenderer;
import ui.help.Help;
import ui.images.Images;

/**
 * Erm�glicht das Bearbeiten eines Javascript- oder Java-Codeabschnitts
 * in einem Dialog.
 * @author Alexander Herzog
 */
public class ScriptEditorPanel extends JPanel {
	private static final long serialVersionUID = -7826682241784296994L;

	/**
	 * Standard-Rahmen f�r einen Java-Codeschnippsel<br>
	 * (W�hrend bei Javascript direkt losgelegt werden kann, muss der Java-Code in eine Methode verpackt werden (die dann von der Ausf�hrungsschicht noch in eine Klasse eingebaut wird).
	 */
	public final static String DEFAULT_JAVA="void function(SimulationInterface sim) {\n\n}\n";

	private final List<Runnable> keyActionListeners;

	/**
	 * Im Vorlagen-Popupmen� sollen die Befehle f�r eine einfache Station (ohne Kundenkontakt) angeboten werden.
	 * @see ScriptPopup.ScriptFeature
	 */
	public static ScriptPopup.ScriptFeature[] featuresPlainStation=new ScriptPopup.ScriptFeature[] {
			ScriptPopup.ScriptFeature.Simulation
	};

	/**
	 * Im Vorlagen-Popupmen� sollen die Befehle f�r eine normale, durch einen Kunden angetriggerte Station angeboten werden.
	 * @see ScriptPopup.ScriptFeature
	 */
	public static ScriptPopup.ScriptFeature[] featuresClientStation=new ScriptPopup.ScriptFeature[] {
			ScriptPopup.ScriptFeature.Simulation,
			ScriptPopup.ScriptFeature.Client
	};

	/**
	 * Im Vorlagen-Popupmen� sollen die Befehle f�r eine durch einen Kunden angetriggerte Eingabe-Station angeboten werden.
	 * @see ScriptPopup.ScriptFeature
	 */
	public static ScriptPopup.ScriptFeature[] featuresClientStationInput=new ScriptPopup.ScriptFeature[] {
			ScriptPopup.ScriptFeature.Simulation,
			ScriptPopup.ScriptFeature.Client,
			ScriptPopup.ScriptFeature.InputValue
	};

	/**
	 * Im Vorlagen-Popupmen� sollen die Befehle f�r eine durch einen Kunden angetriggerte Ausgabe-Station angeboten werden.
	 * @see ScriptPopup.ScriptFeature
	 */
	public static ScriptPopup.ScriptFeature[] featuresClientStationOutput=new ScriptPopup.ScriptFeature[] {
			ScriptPopup.ScriptFeature.Simulation,
			ScriptPopup.ScriptFeature.Client,
			ScriptPopup.ScriptFeature.Output
	};

	/**
	 * Im Vorlagen-Popupmen� sollen die Befehle f�r eine Station (ohne Kundenkontakt) angeboten werden.
	 * @see ScriptPopup.ScriptFeature
	 */
	public static ScriptPopup.ScriptFeature[] featuresPlainStationOutput=new ScriptPopup.ScriptFeature[] {
			ScriptPopup.ScriptFeature.Simulation,
			ScriptPopup.ScriptFeature.Output
	};

	/**
	 * Im Vorlagen-Popupmen� sollen die Befehle f�r eine Station, die Kunden aufh�lt, angeboten werden.
	 * @see ScriptPopup.ScriptFeature
	 */
	public static ScriptPopup.ScriptFeature[] featuresClientStationHold=new ScriptPopup.ScriptFeature[] {
			ScriptPopup.ScriptFeature.Simulation,
			ScriptPopup.ScriptFeature.Client,
			ScriptPopup.ScriptFeature.ClientsList
	};

	/**
	 * Im Vorlagen-Popupmen� sollen die Befehle zur Verarbeitung der Statistikergebnisse angeboten werden.
	 * @see ScriptPopup.ScriptFeature
	 */
	public static ScriptPopup.ScriptFeature[] featuresFilter=new ScriptPopup.ScriptFeature[] {
			ScriptPopup.ScriptFeature.JSSystem,
			ScriptPopup.ScriptFeature.Statistics
	};

	/**
	 * Im Vorlagen-Popupmen� sollen die im Script-Runner verf�gbaren Befehle dargestellt werden.
	 * @see ScriptPopup.ScriptFeature
	 */
	public static ScriptPopup.ScriptFeature[] featuresScriptRunner=new ScriptPopup.ScriptFeature[] {
			ScriptPopup.ScriptFeature.JSSystem,
			ScriptPopup.ScriptFeature.Output,
			ScriptPopup.ScriptFeature.FileOutput,
			ScriptPopup.ScriptFeature.Model,
			ScriptPopup.ScriptFeature.Statistics,
			ScriptPopup.ScriptFeature.Save
	};

	/**
	 * Skriptmodus
	 * @author Alexander Herzog
	 */
	public enum ScriptMode {
		/** Skriptmodus "Javascript" */
		Javascript,
		/** Skriptmodus "Java" */
		Java
	}

	/** Nur-Lese-Status */
	private final boolean readOnly;
	/** Hilfe-Runnable das in Dialogen verwendet wird */
	private final Runnable helpRunnalbe;
	/** Optionales Modell-Objekt, welches f�r den Aufbau eines Vorlagen-Popup-Men�s verwendet wird */
	private final EditModel model;
	/** Optionales Statistik-Objekt, welches f�r den Aufbau eines Vorlagen-Popup-Men�s verwendet wird */
	private final Statistics statistics;
	/** Skriptfunktionen, die im Vorlagen-Popupmen� angeboten werden sollen */
	private final ScriptPopup.ScriptFeature[] scriptFeatures;

	/** Toolbar in dem Panel */
	private final JToolBar toolbar;
	/** "Neu"-Schaltfl�che */
	private final JButton buttonNew;
	/** "Laden"-Schaltfl�che */
	private final JButton buttonLoad;
	/** "Speichern"-Schaltfl�che */
	private final JButton buttonSave;
	/** "Tools"-Schaltfl�che */
	private final JButton buttonTools;
	/** "Hilfe"-Schaltfl�che */
	private final JButton buttonHelp;
	/** Eingabefeld f�r Javascript-Code */
	private final RSyntaxTextArea scriptEditJavascript;
	/** Eingabefeld f�r Java-Code */
	private final RSyntaxTextArea scriptEditJava;
	private final JPanel scriptEditMulti;
	private final CardLayout scriptEditMultiLayout;
	private final JComboBox<String> languageCombo;

	private String lastScript="";

	/**
	 * Konstruktor der Klasse
	 * @param script	Bisheriges Skript
	 * @param mode	Skriptmodus
	 * @param readOnly	Nur-Lese-Status
	 * @param scriptName	�berschrift �ber dem Skript (kann <code>null</code> sein, wenn keine �berschrift angezeigt werden soll)
	 * @param model	Optionales Modell-Objekt, welches f�r den Aufbau eines Vorlagen-Popup-Men�s verwendet wird
	 * @param statistics	Optionales Statistik-Objekt, welches f�r den Aufbau eines Vorlagen-Popup-Men�s verwendet wird
	 * @param helpRunnalbe	Hilfe-Runnable das in Dialogen verwendet wird
	 * @param scriptFeatures	Skriptfunktionen, die im Vorlagen-Popupmen� angeboten werden sollen
	 */
	public ScriptEditorPanel(final String script, final ScriptMode mode, final boolean readOnly, final String scriptName, final EditModel model, final Statistics statistics, final Runnable helpRunnalbe, final ScriptPopup.ScriptFeature[] scriptFeatures) {
		super();
		keyActionListeners=new ArrayList<>();
		setLayout(new BorderLayout());
		this.readOnly=readOnly;
		this.helpRunnalbe=helpRunnalbe;
		this.model=model;
		this.statistics=statistics;
		this.scriptFeatures=scriptFeatures;

		JPanel line;
		JLabel label;

		if (scriptName!=null && !scriptName.trim().isEmpty()) {
			add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)),BorderLayout.NORTH);
			line.add(new JLabel(scriptName+":"));
		}

		final JPanel sub=new JPanel(new BorderLayout());
		add(sub,BorderLayout.CENTER);

		toolbar=new JToolBar();
		toolbar.setFloatable(false);
		sub.add(toolbar,BorderLayout.NORTH);

		toolbar.add(label=new JLabel(Language.tr("Surface.ScriptEditor.Language")+":"));
		toolbar.add(Box.createHorizontalStrut(5));
		toolbar.add(languageCombo=new JComboBox<>(new String[]{
				Language.tr("Surface.ScriptEditor.Language.Javascript"),
				Language.tr("Surface.ScriptEditor.Language.Java")
		}));
		languageCombo.setRenderer(new IconListCellRenderer(new Images[]{
				Images.SCRIPT_MODE_JAVASCRIPT,
				Images.SCRIPT_MODE_JAVA
		}));
		label.setLabelFor(languageCombo);
		switch (mode) {
		case Javascript: languageCombo.setSelectedIndex(0); break;
		case Java: languageCombo.setSelectedIndex(1); break;
		default: languageCombo.setSelectedIndex(0); break;
		}
		languageCombo.setMaximumSize(new Dimension(languageCombo.getPreferredSize().width,languageCombo.getMaximumSize().height));
		toolbar.add(Box.createHorizontalStrut(5));

		buttonNew=addToolbarButton(toolbar,Language.tr("Surface.ScriptEditor.New"),Images.SCRIPT_NEW.getIcon(),Language.tr("Surface.ScriptEditor.New.Hint"),readOnly);
		buttonLoad=addToolbarButton(toolbar,Language.tr("Surface.ScriptEditor.Load"),Images.SCRIPT_LOAD.getIcon(),Language.tr("Surface.ScriptEditor.Load.Hint"),readOnly);
		buttonSave=addToolbarButton(toolbar,Language.tr("Surface.ScriptEditor.Save"),Images.SCRIPT_SAVE.getIcon(),Language.tr("Surface.ScriptEditor.Save.Hint"),readOnly);
		addCustomToolbarButtons(toolbar);
		toolbar.addSeparator();
		buttonTools=addToolbarButton(toolbar,Language.tr("Surface.ScriptEditor.Tools"),Images.SCRIPT_TOOLS.getIcon(),Language.tr("Surface.ScriptEditor.Tools.Hint"),readOnly);
		toolbar.addSeparator();
		buttonHelp=addToolbarButton(toolbar,Language.tr("Main.Toolbar.Help"),Images.HELP.getIcon(),Language.tr("Surface.ScriptEditor.Help.Hint"),readOnly);

		ScriptEditorAreaBuilder builder;

		builder=new ScriptEditorAreaBuilder(ScriptPopup.ScriptMode.Javascript,readOnly,e->fireKeyAction());
		builder.addAutoCompleteFeatures(scriptFeatures);
		if (script!=null && mode==ScriptMode.Javascript) builder.setText(script);
		builder.addFileDropper(new ButtonListener());
		scriptEditJavascript=builder.get();

		builder=new ScriptEditorAreaBuilder(ScriptPopup.ScriptMode.Java,readOnly,e->fireKeyAction());
		builder.addAutoCompleteFeatures(scriptFeatures);
		builder.setText(DEFAULT_JAVA);
		if (script!=null && mode==ScriptMode.Java) builder.setText(script);
		builder.addFileDropper(new ButtonListener());
		scriptEditJava=builder.get();

		lastScript=(script==null)?"":script;

		sub.add(scriptEditMulti=new JPanel(),BorderLayout.CENTER);
		scriptEditMulti.setLayout(scriptEditMultiLayout=new CardLayout());
		final RTextScrollPane scriptEditJavascriptScroll;
		scriptEditMulti.add(scriptEditJavascriptScroll=new RTextScrollPane(scriptEditJavascript),"0");
		scriptEditJavascriptScroll.setLineNumbersEnabled(true);
		final RTextScrollPane scriptEditJavaScroll;
		scriptEditMulti.add(scriptEditJavaScroll=new RTextScrollPane(scriptEditJava),"1");
		scriptEditJavaScroll.setLineNumbersEnabled(true);
		scriptEditMultiLayout.show(scriptEditMulti,""+languageCombo.getSelectedIndex());

		languageCombo.addActionListener(e->languageChanged());
		languageCombo.setEnabled(!readOnly);
		languageChanged();
	}

	/**
	 * Konstruktor der Klasse
	 * @param script	Bisheriges Skript
	 * @param mode	Skriptmodus
	 * @param readOnly	Nur-Lese-Status
	 * @param scriptName	�berschrift �ber dem Skript (kann <code>null</code> sein, wenn keine �berschrift angezeigt werden soll)
	 * @param model	Optionales Modell-Objekt, welches f�r den Aufbau eines Vorlagen-Popup-Men�s verwendet wird
	 * @param helpRunnable	Hilfe-Runnable das in Dialogen verwendet wird
	 * @param scriptFeatures	Skriptfunktionen, die im Vorlagen-Popupmen� angeboten werden sollen
	 */
	public ScriptEditorPanel(final String script, final ScriptMode mode, final boolean readOnly, final String scriptName, final EditModel model, final Runnable helpRunnable, final ScriptPopup.ScriptFeature[] scriptFeatures) {
		this(script,mode,readOnly,scriptName,model,null,helpRunnable,scriptFeatures);
	}

	/**
	 * Erm�glicht das Hinzuf�gen von Schaltfl�chen zur Symbolleiste durch abgeleitete Klassen
	 * @param toolbar	Symbolleiste zu der durch diese Methode weitere Schaltfl�chen hinzugef�gt werden sollen
	 */
	protected void addCustomToolbarButtons(final JToolBar toolbar) {
	}

	private JButton addToolbarButton(final JToolBar toolbar, final String title, final Icon icon, final String hint, final boolean readOnly) {
		final JButton button=new JButton(title);
		toolbar.add(button);
		if (hint!=null) button.setToolTipText(hint);
		if (icon!=null) button.setIcon(icon);
		button.addActionListener(new ButtonListener());
		button.setEnabled(!readOnly);
		return button;
	}

	private void languageChanged() {
		scriptEditMultiLayout.show(scriptEditMulti,""+languageCombo.getSelectedIndex());
	}

	private void setCurrentScript(final String script) {
		switch (languageCombo.getSelectedIndex()) {
		case 0: scriptEditJavascript.setText(script); break;
		case 1: scriptEditJava.setText(script); break;
		}
		lastScript=script;
	}

	/**
	 * Pr�ft, ob das Skript im Editor verworfen werden darf.
	 * @return	Gibt <code>true</code> zur�ck, wenn das Skript verworfen werden darf.
	 */
	public boolean allowDiscard() {
		if (lastScript.equals(getScript())) return true;
		if (getMode()==ScriptMode.Java && getScript().equals(DEFAULT_JAVA)) return true;

		final String title;
		final String info;
		switch (languageCombo.getSelectedIndex()) {
		case 0:
			title=Language.tr("Surface.ScriptEditor.Language.JavaScript.DiscardConfirmation.Title");
			info=Language.tr("Surface.ScriptEditor.Language.JavaScript.DiscardConfirmation.Info");
			break;
		case 1:
			title=Language.tr("Surface.ScriptEditor.Language.Java.DiscardConfirmation.Title");
			info=Language.tr("Surface.ScriptEditor.Language.Java.DiscardConfirmation.Info");
			break;
		default:
			title=Language.tr("Surface.ScriptEditor.Language.JavaScript.DiscardConfirmation.Title");
			info=Language.tr("Surface.ScriptEditor.Language.JavaScript.DiscardConfirmation.Info");
			break;
		}

		switch (MsgBox.confirmSave(this,title,info)) {
		case JOptionPane.YES_OPTION: commandSave(); return allowDiscard();
		case JOptionPane.NO_OPTION: return true;
		case JOptionPane.CANCEL_OPTION: return false;
		default: return false;
		}
	}

	private boolean commandLoad(File file) {
		if (file==null) {
			final JFileChooser fc=new JFileChooser();
			CommonVariables.initialDirectoryToJFileChooser(fc);
			final FileFilter filter;
			final String defaultExt;
			switch (languageCombo.getSelectedIndex()) {
			case 0:
				fc.setDialogTitle(Language.tr("FileType.Load.JS"));
				filter=new FileNameExtensionFilter(Language.tr("FileType.JS")+" (*.js)","js");
				defaultExt="js";
				break;
			case 1:
				fc.setDialogTitle(Language.tr("FileType.Load.Java"));
				filter=new FileNameExtensionFilter(Language.tr("FileType.Java")+" (*.java)","java");
				defaultExt="java";
				break;
			default:
				fc.setDialogTitle(Language.tr("FileType.Load.JS"));
				filter=new FileNameExtensionFilter(Language.tr("FileType.JS")+" (*.js)","js");
				defaultExt="js";
				break;
			}
			fc.addChoosableFileFilter(filter);
			fc.setFileFilter(filter);
			if (fc.showOpenDialog(this)!=JFileChooser.APPROVE_OPTION) return false;
			CommonVariables.initialDirectoryFromJFileChooser(fc);
			file=fc.getSelectedFile();
			if (file.getName().indexOf('.')<0 && fc.getFileFilter()==filter) file=new File(file.getAbsoluteFile()+"."+defaultExt);
		}

		final String text=JSRunDataFilterTools.loadText(file);
		if (text==null) return false;
		setCurrentScript(text);
		return true;
	}

	private boolean commandSave() {
		final JFileChooser fc=new JFileChooser();
		CommonVariables.initialDirectoryToJFileChooser(fc);
		final FileFilter filter;
		final String defaultExt;
		switch (languageCombo.getSelectedIndex()) {
		case 0:
			fc.setDialogTitle(Language.tr("FileType.Save.JS"));
			filter=new FileNameExtensionFilter(Language.tr("FileType.JS")+" (*.js)","js");
			defaultExt="js";
			break;
		case 1:
			fc.setDialogTitle(Language.tr("FileType.Save.Java"));
			filter=new FileNameExtensionFilter(Language.tr("FileType.Java")+" (*.java)","java");
			defaultExt="java";
			break;
		default:
			fc.setDialogTitle(Language.tr("FileType.Save.JS"));
			filter=new FileNameExtensionFilter(Language.tr("FileType.JS")+" (*.js)","js");
			defaultExt="js";
			break;
		}
		fc.addChoosableFileFilter(filter);
		fc.setFileFilter(filter);
		fc.setAcceptAllFileFilterUsed(false);
		if (fc.showSaveDialog(this)!=JFileChooser.APPROVE_OPTION) return false;
		CommonVariables.initialDirectoryFromJFileChooser(fc);
		File file=fc.getSelectedFile();
		if (file.getName().indexOf('.')<0) {
			if (fc.getFileFilter()==filter) file=new File(file.getAbsoluteFile()+"."+defaultExt);
		}
		if (file.exists()) {
			if (!MsgBox.confirmOverwrite(this,file)) return false;
		}

		final String script=getScript();
		if (!JSRunDataFilterTools.saveText(script,file,false)) return false;
		lastScript=script;
		return true;
	}

	private void commandPopup() {
		ScriptPopup.ScriptMode mode=ScriptPopup.ScriptMode.Javascript;
		switch (languageCombo.getSelectedIndex()) {
		case 0: mode=ScriptPopup.ScriptMode.Javascript; break;
		case 1: mode=ScriptPopup.ScriptMode.Java; break;
		}

		final ScriptPopup popup=new ScriptPopup(buttonTools,model,statistics,mode,helpRunnalbe);
		popup.addFeatures(scriptFeatures);
		popup.build();
		switch (languageCombo.getSelectedIndex()) {
		case 0: popup.show(scriptEditJavascript); break;
		case 1: popup.show(scriptEditJava); break;
		}
	}

	private class ButtonListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			if (readOnly) return;

			if (e.getSource() instanceof FileDropperData) {
				final FileDropperData data=(FileDropperData)e.getSource();
				final File file=data.getFile();
				if (file.isFile()) {
					if (allowDiscard()) commandLoad(file);
					data.dragDropConsumed();
				}
				return;
			}

			if (e.getSource()==buttonNew) {
				if (allowDiscard()) {
					setCurrentScript(null);
					scriptEditJavascript.setText("");
					scriptEditJava.setText("");
					lastScript="";
					fireKeyAction();
				}
				return;
			}

			if (e.getSource()==buttonLoad) {
				if (allowDiscard()) {
					if (commandLoad(null)) fireKeyAction();
				}
				return;
			}

			if (e.getSource()==buttonSave) {
				if (commandSave()) fireKeyAction();
				return;
			}

			if (e.getSource()==buttonTools) {
				commandPopup();
				return;
			}

			if (e.getSource()==buttonHelp) {
				switch (languageCombo.getSelectedIndex()) {
				case 0:
					Help.topicModal(ScriptEditorPanel.this,"JS");
					break;
				case 1:
					Help.topicModal(ScriptEditorPanel.this,"Java");
					break;
				}
				return;
			}
		}
	}

	/**
	 * Pr�ft ob die eingegebenen Daten in Ordnung sind.
	 * @return	Gibt <code>true</code> zur�ck, wenn die Daten in Ordnung sind.
	 */
	public boolean checkData() {
		switch (languageCombo.getSelectedIndex()) {
		case 0: /* Javascript */
			return true;
		case 1: /* Java */
			if (!DynamicFactory.isWindows() && !DynamicFactory.isInMemoryProcessing()) return true;
			final String error=DynamicFactory.getFactory().test(scriptEditJava.getText(),false);
			if (error==null) return true;
			MsgBox.error(this,Language.tr("Surface.HoldJS.Dialog.Language.JavaError"),error);
			return false;
		default:
			return true;
		}
	}

	/**
	 * Liefert das eingegebene Skript zur�ck.
	 * @return	Neues Skript
	 */
	public String getScript() {
		switch (languageCombo.getSelectedIndex()) {
		case 0: return scriptEditJavascript.getText();
		case 1: return scriptEditJava.getText();
		}
		return "";
	}

	/**
	 * Gibt den gew�hlten Skriptmodus an.
	 * @return	Aktueller Skriptmodus
	 */
	public ScriptMode getMode() {
		switch (languageCombo.getSelectedIndex()) {
		case 0: return ScriptMode.Javascript;
		case 1: return ScriptMode.Java;
		default: return ScriptMode.Javascript;
		}
	}

	/**
	 * Stellt den Skriptmodus und das aktuelle Skript f�r den Editor ein
	 * @param mode	Neuer Skriptmodus
	 * @param script	Neues Skript
	 */
	public void setScript(final ScriptMode mode, final String script) {
		switch (mode) {
		case Javascript: languageCombo.setSelectedIndex(0); break;
		case Java: languageCombo.setSelectedIndex(1); break;
		}
		setCurrentScript(script);
	}

	/**
	 * F�gt einen Listener hinzu, der benachrichtigt wird, wenn der Nutzer Eingaben in das Skriptfeld vornimmt.
	 * @param listener	Neuer zu benachrichtigender Listener
	 * @return	Gibt <code>true</code> zur�ck, wenn der Listener in die Liste der zu benachrichtigenden Listener aufgenommen werden konnte.
	 */
	public boolean addKeyActionListener(final Runnable listener) {
		if (keyActionListeners.contains(listener)) return false;
		keyActionListeners.add(listener);
		return true;
	}

	/**
	 * Entfernt einen Listener aus der Liste der Listener, die benachrichtigt werden sollen, wenn der Nutzer Eingaben in das Skriptfeld vornimmt.
	 * @param listener	Nicht mehr zu benachrichtigender Listener
	 * @return	Gibt <code>true</code> zur�ck, wenn der Listener aus der Liste der zu benachrichtigenden Listener entfernt werden konnte.
	 */
	public boolean removeKeyActionListener(final Runnable listener) {
		return keyActionListeners.remove(listener);
	}

	private void fireKeyAction() {
		for (Runnable runnable: keyActionListeners) runnable.run();
	}

	/**
	 * Stellt ein, ob Editor und Toolbar aktiv sein sollen
	 * (nur verf�gbar, wenn im Konstruktor nicht ReadOnly gew�hlt wurde).
	 * @param editable	Editor und Toolbar aktivieren
	 */
	public void setEditable(final boolean editable) {
		if (readOnly) return;
		for (int i=0;i<toolbar.getComponentCount();i++) if (toolbar.getComponent(i) instanceof JButton) ((JButton)toolbar.getComponent(i)).setEnabled(editable);
		scriptEditJavascript.setEditable(editable);
		scriptEditJava.setEditable(editable);
		languageCombo.setEnabled(editable);
	}
}
