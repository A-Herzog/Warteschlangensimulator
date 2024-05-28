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
package language;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;

import javax.swing.UIManager;

/**
 * Korrigiert unter Oracle Java 11 fehlende Übersetzungen.
 * @author Alexander Herzog
 */
public class Messages_Java11 {

	/**
	 * Konstruktor der Klasse<br>
	 * Der Konstruktor kann nicht aufgerufen werden. Diese Klasse kann nicht
	 * instanziert werden. Sie stellt nur statische Methoden zur Verfügung.
	 */
	private Messages_Java11() {
	}

	/**
	 * Prüft, ob eine Korrektur überhaupt notwendig ist.
	 * @return	Gibt <code>true</code> zurück, wenn Übersetzungen für GUI-Elemente fehlen (d.h. wenn Oracle Java in Version 11 oder höher verwendet wird).
	 */
	public static boolean isFixNeeded() {
		if (!System.getProperty("java.vm.vendor").toLowerCase().contains("oracle")) return false;
		final String[] parts=System.getProperty("java.version").split("\\.");
		if (parts.length<1) return false;
		try {
			final int majorVersion=Integer.parseInt(parts[0]);
			if (majorVersion>=11) return true;
		} catch (NumberFormatException e) {
			return false;
		}
		return false;
	}

	/**
	 * Fügt deutsche Texte zu möglicherweise fehlenden Swing-Meldungen hinzu.
	 * @see #setupMissingSwingMessages()
	 */
	private static void setupMissingSwingMessagesDE() {
		UIManager.put("AbstractButton.clickText","Klicken");
		UIManager.put("AbstractDocument.additionText","Hinzufügen");
		UIManager.put("AbstractDocument.deletionText","Löschen");
		UIManager.put("AbstractDocument.redoText","Wiederherstellen");
		UIManager.put("AbstractDocument.styleChangeText","Formatvorlagenänderung");
		UIManager.put("AbstractDocument.undoText","Rückgängig");
		UIManager.put("AbstractUndoableEdit.redoText","Wiederherstellen");
		UIManager.put("AbstractUndoableEdit.undoText","Rückgängig");
		/* UIManager.put("ButtonUI","com.sun.java.swing.plaf.windows.WindowsButtonUI"); */
		UIManager.put("CheckBoxMenuItem.commandSound","win.sound.menuCommand");
		/* UIManager.put("CheckBoxMenuItemUI","com.sun.java.swing.plaf.windows.WindowsCheckBoxMenuItemUI"); */
		/* UIManager.put("CheckBoxUI","com.sun.java.swing.plaf.windows.WindowsCheckBoxUI"); */
		UIManager.put("ColorChooser.cancelText","Abbrechen");
		UIManager.put("ColorChooser.okText","OK");
		UIManager.put("ColorChooser.previewText","Vorschau");
		UIManager.put("ColorChooser.resetMnemonic","90");
		UIManager.put("ColorChooser.resetText","Zurücksetzen");
		UIManager.put("ColorChooser.rgbBlueMnemonic","66");
		UIManager.put("ColorChooser.rgbBlueText","Blau");
		UIManager.put("ColorChooser.rgbDisplayedMnemonicIndex","1");
		UIManager.put("ColorChooser.rgbGreenMnemonic","78");
		UIManager.put("ColorChooser.rgbGreenText","Grün");
		UIManager.put("ColorChooser.rgbMnemonic","71");
		UIManager.put("ColorChooser.rgbNameText","RGB");
		UIManager.put("ColorChooser.rgbRedMnemonic","84");
		UIManager.put("ColorChooser.rgbRedText","Rot");
		UIManager.put("ColorChooser.sampleText","Beispieltext  Beispieltext");
		UIManager.put("ColorChooser.swatchesDisplayedMnemonicIndex","0");
		UIManager.put("ColorChooser.swatchesMnemonic","83");
		UIManager.put("ColorChooser.swatchesNameText","Swatches");
		UIManager.put("ColorChooser.swatchesRecentText","Aktuell:");
		/* UIManager.put("ColorChooserUI","javax.swing.plaf.basic.BasicColorChooserUI"); */
		UIManager.put("ComboBox.togglePopupText","togglePopup");
		/* UIManager.put("ComboBoxUI","com.sun.java.swing.plaf.windows.WindowsComboBoxUI"); */
		/* UIManager.put("DesktopIconUI","com.sun.java.swing.plaf.windows.WindowsDesktopIconUI"); */
		/* UIManager.put("DesktopPaneUI","com.sun.java.swing.plaf.windows.WindowsDesktopPaneUI"); */
		/* UIManager.put("EditorPaneUI","com.sun.java.swing.plaf.windows.WindowsEditorPaneUI"); */
		UIManager.put("FileChooser.acceptAllFileFilterText","Alle Dateien");
		UIManager.put("FileChooser.cancelButtonText","Abbrechen");
		UIManager.put("FileChooser.directoryDescriptionText","Verzeichnis");
		UIManager.put("FileChooser.directoryOpenButtonText","Öffnen");
		UIManager.put("FileChooser.fileDescriptionText","Allgemeine Datei");
		UIManager.put("FileChooser.fileNameLabelMnemonic","68");
		UIManager.put("FileChooser.fileNameLabelText","Dateiname:");
		UIManager.put("FileChooser.fileSizeGigaBytes","{0} GB");
		UIManager.put("FileChooser.fileSizeKiloBytes","{0} KB");
		UIManager.put("FileChooser.fileSizeMegaBytes","{0} MB");
		UIManager.put("FileChooser.filesOfTypeLabelMnemonic","84");
		UIManager.put("FileChooser.filesOfTypeLabelText","Dateityp:");
		UIManager.put("FileChooser.helpButtonMnemonic","72");
		UIManager.put("FileChooser.helpButtonText","Hilfe");
		UIManager.put("FileChooser.lookInLabelMnemonic","73");
		UIManager.put("FileChooser.newFolderAccessibleName","Neuer Ordner");
		UIManager.put("FileChooser.newFolderErrorSeparator",":");
		UIManager.put("FileChooser.newFolderErrorText","Fehler beim Erstellen eines neuen Ordners");
		UIManager.put("FileChooser.openButtonText","Öffnen");
		UIManager.put("FileChooser.openDialogTitleText","Öffnen");
		UIManager.put("FileChooser.saveButtonText","Speichern");
		UIManager.put("FileChooser.saveDialogTitleText","Speichern");
		UIManager.put("FileChooser.updateButtonMnemonic","75");
		UIManager.put("FileChooser.updateButtonText","Aktualisieren");
		/* UIManager.put("FileChooserUI","com.sun.java.swing.plaf.windows.WindowsFileChooserUI"); */
		/* UIManager.put("FormattedTextFieldUI","javax.swing.plaf.basic.BasicFormattedTextFieldUI"); */
		UIManager.put("FormView.browseFileButtonText","Durchsuchen...");
		UIManager.put("FormView.resetButtonText","Zurücksetzen");
		UIManager.put("FormView.submitButtonText","Abfrage weiterleiten");
		UIManager.put("InternalFrame.closeButtonToolTip","Schließen");
		UIManager.put("InternalFrame.closeSound","win.sound.close");
		UIManager.put("InternalFrame.iconButtonToolTip","Minimieren");
		UIManager.put("InternalFrame.maxButtonToolTip","Maximieren");
		UIManager.put("InternalFrame.maximizeSound","win.sound.maximize");
		UIManager.put("InternalFrame.minimizeSound","win.sound.minimize");
		UIManager.put("InternalFrame.restoreButtonToolTip","Wiederherstellen");
		UIManager.put("InternalFrame.restoreDownSound","win.sound.restoreDown");
		UIManager.put("InternalFrame.restoreUpSound","win.sound.restoreUp");
		UIManager.put("InternalFrameTitlePane.closeButtonAccessibleName","Schließen");
		UIManager.put("InternalFrameTitlePane.closeButtonText","Schließen");
		UIManager.put("InternalFrameTitlePane.iconifyButtonAccessibleName","Als Symbol darstellen");
		UIManager.put("InternalFrameTitlePane.maximizeButtonAccessibleName","Maximieren");
		UIManager.put("InternalFrameTitlePane.maximizeButtonText","Maximieren");
		UIManager.put("InternalFrameTitlePane.minimizeButtonText","Minimieren");
		UIManager.put("InternalFrameTitlePane.moveButtonText","Verschieben");
		UIManager.put("InternalFrameTitlePane.restoreButtonText","Wiederherstellen");
		UIManager.put("InternalFrameTitlePane.sizeButtonText","Größe");
		/* UIManager.put("InternalFrameUI","com.sun.java.swing.plaf.windows.WindowsInternalFrameUI"); */
		UIManager.put("IsindexView.prompt","Dieser Index kann durchsucht werden. Geben Sie Schlüsselwörter für die Suche ein:");
		/* UIManager.put("LabelUI","com.sun.java.swing.plaf.windows.WindowsLabelUI"); */
		/* UIManager.put("ListUI","javax.swing.plaf.basic.BasicListUI"); */
		UIManager.put("Menu.cancelMode","hideLastSubmenu");
		/* UIManager.put("MenuBarUI","com.sun.java.swing.plaf.windows.WindowsMenuBarUI"); */
		UIManager.put("MenuItem.acceleratorDelimiter","+");
		UIManager.put("MenuItem.commandSound","win.sound.menuCommand");
		/* UIManager.put("MenuItemUI","com.sun.java.swing.plaf.windows.WindowsMenuItemUI"); */
		/* UIManager.put("MenuUI","com.sun.java.swing.plaf.windows.WindowsMenuUI"); */
		UIManager.put("OptionPane.cancelButtonText","Abbrechen");
		UIManager.put("OptionPane.errorSound","win.sound.hand");
		UIManager.put("OptionPane.informationSound","win.sound.asterisk");
		UIManager.put("OptionPane.inputDialogTitle","Eingabe");
		UIManager.put("OptionPane.messageDialogTitle","Meldung");
		UIManager.put("OptionPane.noButtonMnemonic","78");
		UIManager.put("OptionPane.noButtonText","Nein");
		UIManager.put("OptionPane.okButtonText","OK");
		UIManager.put("OptionPane.questionSound","win.sound.question");
		UIManager.put("OptionPane.titleText","Option auswählen");
		UIManager.put("OptionPane.warningSound","win.sound.exclamation");
		UIManager.put("OptionPane.yesButtonMnemonic","74");
		UIManager.put("OptionPane.yesButtonText","Ja");
		/* UIManager.put("OptionPaneUI","javax.swing.plaf.basic.BasicOptionPaneUI"); */
		/* UIManager.put("PanelUI","javax.swing.plaf.basic.BasicPanelUI"); */
		/* UIManager.put("PasswordFieldUI","com.sun.java.swing.plaf.windows.WindowsPasswordFieldUI"); */
		UIManager.put("PopupMenu.popupSound","win.sound.menuPopup");
		/* UIManager.put("PopupMenuSeparatorUI","com.sun.java.swing.plaf.windows.WindowsPopupMenuSeparatorUI"); */
		/* UIManager.put("PopupMenuUI","com.sun.java.swing.plaf.windows.WindowsPopupMenuUI"); */
		UIManager.put("PrintingDialog.abortButtonDisplayedMnemonicIndex","0");
		UIManager.put("PrintingDialog.abortButtonMnemonic","65");
		UIManager.put("PrintingDialog.abortButtonText","Abbruch");
		UIManager.put("PrintingDialog.abortButtonToolTipText","Druckvorgang abbrechen");
		UIManager.put("PrintingDialog.contentAbortingText","Druckvorgang wird abgebrochen...");
		UIManager.put("PrintingDialog.contentInitialText","Druckvorgang läuft...");
		UIManager.put("PrintingDialog.contentProgressText","Seite {0} wurde gedruckt...");
		UIManager.put("PrintingDialog.titleAbortingText","Drucken (Abbruch)");
		UIManager.put("PrintingDialog.titleProgressText","Drucken");
		/* UIManager.put("ProgressBarUI","com.sun.java.swing.plaf.windows.WindowsProgressBarUI"); */
		UIManager.put("ProgressMonitor.progressText","Fortschritt...");
		UIManager.put("RadioButtonMenuItem.commandSound","win.sound.menuCommand");
		/* UIManager.put("RadioButtonMenuItemUI","com.sun.java.swing.plaf.windows.WindowsRadioButtonMenuItemUI"); */
		/* UIManager.put("RadioButtonUI","com.sun.java.swing.plaf.windows.WindowsRadioButtonUI"); */
		/* UIManager.put("RootPaneUI","com.sun.java.swing.plaf.windows.WindowsRootPaneUI"); */
		/* UIManager.put("ScrollBarUI","com.sun.java.swing.plaf.windows.WindowsScrollBarUI"); */
		/* UIManager.put("ScrollPaneUI","javax.swing.plaf.basic.BasicScrollPaneUI"); */
		/* UIManager.put("SeparatorUI","com.sun.java.swing.plaf.windows.WindowsSeparatorUI"); */
		/* UIManager.put("SliderUI","com.sun.java.swing.plaf.windows.WindowsSliderUI"); */
		/* UIManager.put("SpinnerUI","com.sun.java.swing.plaf.windows.WindowsSpinnerUI"); */
		UIManager.put("SplitPane.leftButtonText","linke Schaltfläche");
		UIManager.put("SplitPane.rightButtonText","rechte Schaltfläche");
		/* UIManager.put("SplitPaneUI","com.sun.java.swing.plaf.windows.WindowsSplitPaneUI"); */
		/* UIManager.put("TabbedPaneUI","com.sun.java.swing.plaf.windows.WindowsTabbedPaneUI"); */
		/* UIManager.put("TableHeaderUI","com.sun.java.swing.plaf.windows.WindowsTableHeaderUI"); */
		/* UIManager.put("TableUI","javax.swing.plaf.basic.BasicTableUI"); */
		/* UIManager.put("TextAreaUI","com.sun.java.swing.plaf.windows.WindowsTextAreaUI"); */
		/* UIManager.put("TextFieldUI","com.sun.java.swing.plaf.windows.WindowsTextFieldUI"); */
		/* UIManager.put("TextPaneUI","com.sun.java.swing.plaf.windows.WindowsTextPaneUI"); */
		/* UIManager.put("ToggleButtonUI","com.sun.java.swing.plaf.windows.WindowsToggleButtonUI"); */
		/* UIManager.put("ToolBarSeparatorUI","com.sun.java.swing.plaf.windows.WindowsToolBarSeparatorUI"); */
		/* UIManager.put("ToolBarUI","com.sun.java.swing.plaf.windows.WindowsToolBarUI"); */
		UIManager.put("ToolTipManager.enableToolTipMode","activeApplication");
		/* UIManager.put("ToolTipUI","javax.swing.plaf.basic.BasicToolTipUI"); */
		/* UIManager.put("TreeUI","com.sun.java.swing.plaf.windows.WindowsTreeUI"); */
		/* UIManager.put("ViewportUI","javax.swing.plaf.basic.BasicViewportUI"); */
	}

	/**
	 * Fügt englische Texte zu möglicherweise fehlenden Swing-Meldungen hinzu.
	 * @see #setupMissingSwingMessages()
	 */
	private static void setupMissingSwingMessagesEN() {
		UIManager.put("AbstractButton.clickText","click");
		UIManager.put("AbstractDocument.additionText","addition");
		UIManager.put("AbstractDocument.deletionText","deletion");
		UIManager.put("AbstractDocument.redoText","Redo");
		UIManager.put("AbstractDocument.styleChangeText","style change");
		UIManager.put("AbstractDocument.undoText","Undo");
		UIManager.put("AbstractUndoableEdit.redoText","Redo");
		UIManager.put("AbstractUndoableEdit.undoText","Undo");
		/* UIManager.put("ButtonUI","com.sun.java.swing.plaf.windows.WindowsButtonUI"); */
		/* UIManager.put("CheckBoxMenuItemUI","com.sun.java.swing.plaf.windows.WindowsCheckBoxMenuItemUI"); */
		/* UIManager.put("CheckBoxUI","com.sun.java.swing.plaf.windows.WindowsCheckBoxUI"); */
		UIManager.put("ColorChooser.cancelText","Cancel");
		UIManager.put("ColorChooser.okText","OK");
		UIManager.put("ColorChooser.previewText","Preview");
		UIManager.put("ColorChooser.resetMnemonic","82");
		UIManager.put("ColorChooser.resetText","Reset");
		UIManager.put("ColorChooser.rgbBlueMnemonic","66");
		UIManager.put("ColorChooser.rgbBlueText","Blue");
		UIManager.put("ColorChooser.rgbDisplayedMnemonicIndex","1");
		UIManager.put("ColorChooser.rgbGreenMnemonic","78");
		UIManager.put("ColorChooser.rgbGreenText","Green");
		UIManager.put("ColorChooser.rgbMnemonic","71");
		UIManager.put("ColorChooser.rgbNameText","RGB");
		UIManager.put("ColorChooser.rgbRedMnemonic","68");
		UIManager.put("ColorChooser.rgbRedText","Red");
		UIManager.put("ColorChooser.sampleText","Sample Text  Sample Text");
		UIManager.put("ColorChooser.swatchesDisplayedMnemonicIndex","0");
		UIManager.put("ColorChooser.swatchesMnemonic","83");
		UIManager.put("ColorChooser.swatchesNameText","Swatches");
		UIManager.put("ColorChooser.swatchesRecentText","Recent:");
		/* UIManager.put("ColorChooserUI","javax.swing.plaf.basic.BasicColorChooserUI"); */
		UIManager.put("ComboBox.togglePopupText","togglePopup");
		/* UIManager.put("ComboBoxUI","com.sun.java.swing.plaf.windows.WindowsComboBoxUI"); */
		/* UIManager.put("DesktopIconUI","com.sun.java.swing.plaf.windows.WindowsDesktopIconUI"); */
		/* UIManager.put("DesktopPaneUI","com.sun.java.swing.plaf.windows.WindowsDesktopPaneUI"); */
		/* UIManager.put("EditorPaneUI","com.sun.java.swing.plaf.windows.WindowsEditorPaneUI"); */
		UIManager.put("FileChooser.acceptAllFileFilterText","All Files");
		UIManager.put("FileChooser.cancelButtonText","Cancel");
		UIManager.put("FileChooser.directoryDescriptionText","Directory");
		UIManager.put("FileChooser.directoryOpenButtonText","Open");
		UIManager.put("FileChooser.fileDescriptionText","Generic File");
		UIManager.put("FileChooser.fileNameLabelMnemonic","78");
		UIManager.put("FileChooser.fileNameLabelText","File name:");
		UIManager.put("FileChooser.fileSizeGigaBytes","{0} GB");
		UIManager.put("FileChooser.fileSizeKiloBytes","{0} KB");
		UIManager.put("FileChooser.fileSizeMegaBytes","{0} MB");
		UIManager.put("FileChooser.filesOfTypeLabelMnemonic","84");
		UIManager.put("FileChooser.filesOfTypeLabelText","Files of type:");
		UIManager.put("FileChooser.helpButtonMnemonic","72");
		UIManager.put("FileChooser.helpButtonText","Help");
		UIManager.put("FileChooser.lookInLabelMnemonic","73");
		UIManager.put("FileChooser.newFolderAccessibleName","New Folder");
		UIManager.put("FileChooser.newFolderErrorSeparator",":");
		UIManager.put("FileChooser.newFolderErrorText","Error creating new folder");
		UIManager.put("FileChooser.openButtonText","Open");
		UIManager.put("FileChooser.openDialogTitleText","Open");
		UIManager.put("FileChooser.saveButtonText","Save");
		UIManager.put("FileChooser.saveDialogTitleText","Save");
		UIManager.put("FileChooser.updateButtonMnemonic","85");
		UIManager.put("FileChooser.updateButtonText","Update");
		/* UIManager.put("FileChooserUI","com.sun.java.swing.plaf.windows.WindowsFileChooserUI"); */
		UIManager.put("FormView.browseFileButtonText","Browse...");
		UIManager.put("FormView.resetButtonText","Reset");
		UIManager.put("FormView.submitButtonText","Submit Query");
		/* UIManager.put("FormattedTextFieldUI","javax.swing.plaf.basic.BasicFormattedTextFieldUI"); */
		UIManager.put("InternalFrame.closeButtonToolTip","Close");
		UIManager.put("InternalFrame.iconButtonToolTip","Minimize");
		UIManager.put("InternalFrame.maxButtonToolTip","Maximize");
		UIManager.put("InternalFrame.restoreButtonToolTip","Restore");
		UIManager.put("InternalFrameTitlePane.closeButtonAccessibleName","Close");
		UIManager.put("InternalFrameTitlePane.closeButtonText","Close");
		UIManager.put("InternalFrameTitlePane.iconifyButtonAccessibleName","Iconify");
		UIManager.put("InternalFrameTitlePane.maximizeButtonAccessibleName","Maximize");
		UIManager.put("InternalFrameTitlePane.maximizeButtonText","Maximize");
		UIManager.put("InternalFrameTitlePane.minimizeButtonText","Minimize");
		UIManager.put("InternalFrameTitlePane.moveButtonText","Move");
		UIManager.put("InternalFrameTitlePane.restoreButtonText","Restore");
		UIManager.put("InternalFrameTitlePane.sizeButtonText","Size");
		/* UIManager.put("InternalFrameUI","com.sun.java.swing.plaf.windows.WindowsInternalFrameUI"); */
		UIManager.put("IsindexView.prompt","This is a searchable index.  Enter search keywords:");
		/* UIManager.put("LabelUI","com.sun.java.swing.plaf.windows.WindowsLabelUI"); */
		/* UIManager.put("ListUI","javax.swing.plaf.basic.BasicListUI"); */
		/* UIManager.put("MenuBarUI","com.sun.java.swing.plaf.windows.WindowsMenuBarUI"); */
		UIManager.put("MenuItem.acceleratorDelimiter","+");
		/* UIManager.put("MenuItemUI","com.sun.java.swing.plaf.windows.WindowsMenuItemUI"); */
		/* UIManager.put("MenuUI","com.sun.java.swing.plaf.windows.WindowsMenuUI"); */
		UIManager.put("OptionPane.cancelButtonText","Cancel");
		UIManager.put("OptionPane.inputDialogTitle","Input");
		UIManager.put("OptionPane.messageDialogTitle","Message");
		UIManager.put("OptionPane.noButtonMnemonic","78");
		UIManager.put("OptionPane.noButtonText","No");
		UIManager.put("OptionPane.okButtonText","OK");
		UIManager.put("OptionPane.titleText","Select an Option");
		UIManager.put("OptionPane.yesButtonMnemonic","89");
		UIManager.put("OptionPane.yesButtonText","Yes");
		/* UIManager.put("OptionPaneUI","javax.swing.plaf.basic.BasicOptionPaneUI"); */
		/* UIManager.put("PanelUI","javax.swing.plaf.basic.BasicPanelUI"); */
		/* UIManager.put("PasswordFieldUI","com.sun.java.swing.plaf.windows.WindowsPasswordFieldUI"); */
		/* UIManager.put("PopupMenuSeparatorUI","com.sun.java.swing.plaf.windows.WindowsPopupMenuSeparatorUI"); */
		/* UIManager.put("PopupMenuUI","com.sun.java.swing.plaf.windows.WindowsPopupMenuUI"); */
		UIManager.put("PrintingDialog.abortButtonDisplayedMnemonicIndex","0");
		UIManager.put("PrintingDialog.abortButtonMnemonic","65");
		UIManager.put("PrintingDialog.abortButtonText","Abort");
		UIManager.put("PrintingDialog.abortButtonToolTipText","Abort Printing");
		UIManager.put("PrintingDialog.contentAbortingText","Printing aborting...");
		UIManager.put("PrintingDialog.contentInitialText","Printing in progress...");
		UIManager.put("PrintingDialog.contentProgressText","Printed page {0}...");
		UIManager.put("PrintingDialog.titleAbortingText","Printing (Aborting)");
		UIManager.put("PrintingDialog.titleProgressText","Printing");
		/* UIManager.put("ProgressBarUI","com.sun.java.swing.plaf.windows.WindowsProgressBarUI"); */
		UIManager.put("ProgressMonitor.progressText","Progress...");
		/* UIManager.put("RadioButtonMenuItemUI","com.sun.java.swing.plaf.windows.WindowsRadioButtonMenuItemUI"); */
		/* UIManager.put("RadioButtonUI","com.sun.java.swing.plaf.windows.WindowsRadioButtonUI"); */
		/* UIManager.put("RootPaneUI","com.sun.java.swing.plaf.windows.WindowsRootPaneUI"); */
		/* UIManager.put("ScrollBarUI","com.sun.java.swing.plaf.windows.WindowsScrollBarUI"); */
		/* UIManager.put("ScrollPaneUI","javax.swing.plaf.basic.BasicScrollPaneUI"); */
		/* UIManager.put("SeparatorUI","com.sun.java.swing.plaf.windows.WindowsSeparatorUI"); */
		/* UIManager.put("SliderUI","com.sun.java.swing.plaf.windows.WindowsSliderUI"); */
		/* UIManager.put("SpinnerUI","com.sun.java.swing.plaf.windows.WindowsSpinnerUI"); */
		UIManager.put("SplitPane.leftButtonText","left button");
		UIManager.put("SplitPane.rightButtonText","right button");
		/* UIManager.put("SplitPaneUI","com.sun.java.swing.plaf.windows.WindowsSplitPaneUI"); */
		/* UIManager.put("TabbedPaneUI","com.sun.java.swing.plaf.windows.WindowsTabbedPaneUI"); */
		/* UIManager.put("TableHeaderUI","com.sun.java.swing.plaf.windows.WindowsTableHeaderUI"); */
		/* UIManager.put("TableUI","javax.swing.plaf.basic.BasicTableUI"); */
		/* UIManager.put("TextAreaUI","com.sun.java.swing.plaf.windows.WindowsTextAreaUI"); */
		/* UIManager.put("TextFieldUI","com.sun.java.swing.plaf.windows.WindowsTextFieldUI"); */
		/* UIManager.put("TextPaneUI","com.sun.java.swing.plaf.windows.WindowsTextPaneUI"); */
		/* UIManager.put("ToggleButtonUI","com.sun.java.swing.plaf.windows.WindowsToggleButtonUI"); */
		/* UIManager.put("ToolBarSeparatorUI","com.sun.java.swing.plaf.windows.WindowsToolBarSeparatorUI"); */
		/* UIManager.put("ToolBarUI","com.sun.java.swing.plaf.windows.WindowsToolBarUI"); */
		UIManager.put("ToolTipManager.enableToolTipMode","activeApplication");
		/* UIManager.put("ToolTipUI","javax.swing.plaf.basic.BasicToolTipUI"); */
		/* UIManager.put("TreeUI","com.sun.java.swing.plaf.windows.WindowsTreeUI"); */
		/* UIManager.put("ViewportUI","javax.swing.plaf.basic.BasicViewportUI"); */
	}

	/**
	 * Korrigiert die Übersetzungen für die GUI.
	 */
	public static void setupMissingSwingMessages() {
		if (Locale.getDefault().getLanguage().equals(Locale.GERMANY.getLanguage())) {
			setupMissingSwingMessagesDE();
			return;
		}

		if (Locale.getDefault().getLanguage().equals(Locale.US.getLanguage())) {
			setupMissingSwingMessagesEN();
			return;
		}
	}

	/**
	 * Liest (wenn unter einer intakten Java-Version gestartet) die Spracheinstellungen
	 * für eine bestimmte Sprache aus und gibt diese in der Konsole aus.
	 * @param locale	Sprache für die die GUI-Daten ausgegeben werden sollen
	 * @see #getLanguages()
	 */
	private static void getLanguage(final Locale locale) {
		UIManager.getDefaults().setDefaultLocale(locale);
		Locale.setDefault(locale);

		final List<String> keys=new ArrayList<>();
		final Enumeration<Object> keysEnum=UIManager.getDefaults().keys();

		while (keysEnum.hasMoreElements()) {
			final Object key=keysEnum.nextElement();
			if (key instanceof String) keys.add((String)key);
		}
		keys.sort(String.CASE_INSENSITIVE_ORDER);

		for (String key: keys) {
			final String value=UIManager.getString(key,locale);
			if (value==null) continue;
			System.out.println(String.format("UIManager.put(\"%s\",\"%s\");",key,value));
		}
	}

	/**
	 * Liest (wenn unter einer intakten Java-Version gestartet) die Spracheinstellungen aus
	 * und gibt diese in der Konsole aus.
	 */
	public static void getLanguages() {
		final Locale locale=Locale.getDefault();
		final Locale localeUI=UIManager.getDefaults().getDefaultLocale();

		System.out.println("en");
		getLanguage(Locale.US);

		System.out.println("de");
		getLanguage(Locale.GERMANY);

		Locale.setDefault(locale);
		UIManager.getDefaults().setDefaultLocale(localeUI);
	}
}
