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
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.Serializable;
import java.util.Hashtable;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButton;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;

import language.Language;
import mathtools.NumberTools;
import simulator.simparser.ExpressionCalc;
import systemtools.MsgBox;
import systemtools.SmallColorChooser;
import tools.DateTools;
import tools.IconListCellRenderer;
import ui.images.Images;
import ui.infopanel.InfoPanel;
import ui.modeleditor.ModelElementBaseDialog;
import ui.script.ScriptEditorAreaBuilder;
import ui.tools.DateTimePanel;

/**
 * Dialog, der Einstellungen für ein {@link ModelElementAnimationTextValue}-Element anbietet
 * @author Alexander Herzog
 * @see ModelElementAnimationTextValue
 */
public class ModelElementAnimationTextValueDialog extends ModelElementBaseDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = 6936633077601457873L;

	/** Option: Ausdruck */
	private JRadioButton optionExpression;
	/** Option: Aktuelle Simulationszeit */
	private JRadioButton optionTime;
	/** Option: Aktuelles Datum */
	private JRadioButton optionDate;
	/** Eingabefeld für den Rechenausdruck */
	private JTextField editExpression;
	/** Eingabefeld für die Anzahl an anzuzeigenden Nachkommastellen */
	private JSpinner digits;
	/** Option: Ausgabe als Zahl, Prozentwert oder Zeitangabe */
	private JComboBox<String> optionFormat;
	/** Auswahlfeld für den Bezugspunkt für die Ausgabe des aktuellen Datums */
	private DateTimePanel dateTime;
	/** Auswahlbox der Schriftart */
	private JComboBox<FontCache.FontFamily> fontFamilyComboBox;
	/** Eingabefeld für die Schriftgröße */
	private JTextField sizeField;
	/** Option: Schrift in Fettdruck anzeigen */
	private JCheckBox optionBold;
	/** Option: Schrift kursiv anzeigen */
	private JCheckBox optionItalic;
	/** Optionaler Text vor dem Haupttext */
	private RSyntaxTextArea preText;
	/** Optionaler Text nach dem Haupttext */
	private RSyntaxTextArea postText;
	/** Option: HTML- und LaTeX-Symbole interpretieren */
	private JCheckBox optionInterpretSymbols;
	/** Option: Markdown interpretieren */
	private JCheckBox optionInterpretMarkdown;
	/** Option: LaTeX-Formatierungen interpretieren */
	private JCheckBox optionInterpretLaTeX;

	/** Auswahl der Textfarbe */
	private SmallColorChooser colorChooser;
	/** Option: Hintergrundfarbe verwenden? */
	private JCheckBox background;
	/** Auswahl der Hintergrundfarbe */
	private SmallColorChooser colorChooserBackground;
	/** Schieberegler zur Auswahl des Deckkraft der Hintergrundfarbe */
	private JSlider alpha;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Fenster
	 * @param element	Zu bearbeitendes {@link ModelElementAnimationTextValue}
	 * @param readOnly	Nur-Lese-Modus
	 */
	public ModelElementAnimationTextValueDialog(final Component owner, final ModelElementAnimationTextValue element, final ModelElementBaseDialog.ReadOnlyMode readOnly) {
		super(owner,Language.tr("Surface.AnimationText.Dialog.Title"),element,"ModelElementAnimationText",readOnly);
	}

	/**
	 * Stellt die Größe des Dialogfensters ein.
	 */
	@Override
	protected void setDialogSize() {
		setMinSizeRespectingScreensize(800,0);
	}

	@Override
	protected String getInfoPanelID() {
		return InfoPanel.stationAnimationTextValue;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected JComponent getContentPanel() {
		JPanel subPanel, subPanel2, line;
		Object[] data;

		final JPanel content=new JPanel();

		content.setLayout(new BoxLayout(content,BoxLayout.PAGE_AXIS));

		/* Anzuzeigender Text */
		content.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(optionExpression=new JRadioButton(Language.tr("Surface.AnimationText.Dialog.Expression")+":"));
		optionExpression.setEnabled(!readOnly);
		line.add(editExpression=new JTextField());
		ModelElementBaseDialog.addUndoFeature(editExpression);
		editExpression.setPreferredSize(new Dimension(200,editExpression.getPreferredSize().height));
		editExpression.addKeyListener(new KeyListener() {
			@Override public void keyTyped(KeyEvent e) {optionExpression.setSelected(true); checkData(false);}
			@Override public void keyReleased(KeyEvent e) {optionExpression.setSelected(true); checkData(false);}
			@Override public void keyPressed(KeyEvent e) {optionExpression.setSelected(true); checkData(false);}
		});
		editExpression.setEditable(!readOnly);
		line.add(getExpressionEditButton(this,editExpression,false,false,element.getModel(),element.getSurface()));
		if (!readOnly) line.add(AnimationExpressionPanel.getTemplatesButton(element.getModel(),command->{editExpression.setText(command); checkData(false);}));

		line.add(optionFormat=new JComboBox<>(new String[] {
				Language.tr("Surface.AnimationText.Dialog.Number"),
				Language.tr("Surface.AnimationText.Dialog.PercentValue"),
				Language.tr("Surface.AnimationText.Dialog.TimeValue")
		}));
		optionFormat.setRenderer(new IconListCellRenderer(new Images[] {
				Images.GENERAL_NUMBERS,
				Images.GENERAL_PERCENT,
				Images.GENERAL_TIME,
		}));
		optionFormat.setEnabled(!readOnly);
		optionFormat.addActionListener(e->digits.setEnabled(optionFormat.isEnabled() && optionFormat.getSelectedIndex()<2));

		JLabel label;
		line.add(label=new JLabel(Language.tr("Surface.AnimationText.Dialog.Digits")+":"));
		final SpinnerModel spinnerModel=new SpinnerNumberModel(1,0,15,1);
		line.add(digits=new JSpinner(spinnerModel));
		label.setLabelFor(digits);
		digits.setEnabled(!readOnly);
		digits.addChangeListener(e->checkData(false));
		digits.addKeyListener(new KeyListener() {
			@Override public void keyTyped(KeyEvent e) {checkData(false);}
			@Override public void keyReleased(KeyEvent e) {checkData(false);}
			@Override public void keyPressed(KeyEvent e) {checkData(false);}
		});

		content.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(optionTime=new JRadioButton(Language.tr("Surface.AnimationText.Dialog.CurrentSimulationTime")));
		optionTime.setEnabled(!readOnly);
		content.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(optionDate=new JRadioButton(Language.tr("Surface.AnimationText.Dialog.CurrentSimulationDate")));
		optionDate.setEnabled(!readOnly);
		line.add(dateTime=new DateTimePanel(readOnly,DateTools.getNow(false)));

		final ButtonGroup buttonGroup=new ButtonGroup();
		buttonGroup.add(optionExpression);
		buttonGroup.add(optionTime);
		buttonGroup.add(optionDate);

		/* Schritftart */
		FontCache.FontFamily fontFamily=null;
		if (element instanceof ModelElementAnimationTextValue) fontFamily=((ModelElementAnimationTextValue)element).getFontFamily();
		data=getFontFamilyComboBoxPanel(Language.tr("Surface.AnimationText.Dialog.FontFamily")+":",fontFamily);
		fontFamilyComboBox=(JComboBox<FontCache.FontFamily>)data[1];
		fontFamilyComboBox.setEnabled(!readOnly);
		content.add((JPanel)data[0]);

		/* Schriftgröße */
		data=getInputPanel(Language.tr("Surface.AnimationText.Dialog.FontSize")+":","",5);
		sizeField=(JTextField)data[1];
		sizeField.setEditable(!readOnly);
		content.add((JPanel)data[0]);
		sizeField.addActionListener(e->NumberTools.getNotNegativeInteger(sizeField,true));

		/* Fett / Kursiv */
		content.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(optionBold=new JCheckBox("<html><b>"+Language.tr("Surface.AnimationText.Dialog.FontSize.Bold")+"</b></html>",false));
		optionBold.setEnabled(!readOnly);
		line.add(optionItalic=new JCheckBox("<html><i>"+Language.tr("Surface.AnimationText.Dialog.FontSize.Italic")+"</i></html>",false));
		optionItalic.setEnabled(!readOnly);

		/* Optionaler Text vor dem Haupttext */
		data=ScriptEditorAreaBuilder.getInputPanel(Language.tr("Surface.AnimationText.Dialog.OptionalPreText")+":","",30,ScriptEditorAreaBuilder.TextAreaMode.TEXT_ELEMENT);
		content.add(line=(JPanel)data[0]);
		preText=(RSyntaxTextArea)data[1];
		preText.setEditable(!readOnly);
		line.add(new JPreviewButton(preText));

		/* Optionaler Text nach dem Haupttext */
		data=ScriptEditorAreaBuilder.getInputPanel(Language.tr("Surface.AnimationText.Dialog.OptionalPostText")+":","",30,ScriptEditorAreaBuilder.TextAreaMode.TEXT_ELEMENT);
		content.add(line=(JPanel)data[0]);
		postText=(RSyntaxTextArea)data[1];
		postText.setEditable(!readOnly);
		line.add(new JPreviewButton(postText));

		/* Zeile für Einstellungen zu Pre- und Posttext*/
		content.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));

		/* Interpretation von Symbolen */
		line.add(optionInterpretSymbols=new JCheckBox(Language.tr("Surface.Text.Dialog.FontSize.HTMLLaTeX")));
		optionInterpretSymbols.setToolTipText(Language.tr("Surface.Text.Dialog.FontSize.HTMLLaTeX.Info"));
		optionInterpretSymbols.setEnabled(!readOnly);
		optionInterpretSymbols.addActionListener(e->{
			ScriptEditorAreaBuilder.setEntityAutoComplete(preText,optionInterpretSymbols.isSelected());
			ScriptEditorAreaBuilder.setEntityAutoComplete(postText,optionInterpretSymbols.isSelected());
		});

		/* Interpretation von Markdown */
		line.add(optionInterpretMarkdown=new JCheckBox(Language.tr("Surface.Text.Dialog.FontSize.Markdown")));
		optionInterpretMarkdown.setToolTipText(Language.tr("Surface.Text.Dialog.FontSize.Markdown.Info"));
		optionInterpretMarkdown.setEnabled(!readOnly);

		/* Interpretation von LaTeX-Formatierungen */
		line.add(optionInterpretLaTeX=new JCheckBox(Language.tr("Surface.Text.Dialog.FontSize.LaTeX")));
		optionInterpretLaTeX.setToolTipText(Language.tr("Surface.Text.Dialog.FontSize.LaTeX.Info"));
		optionInterpretLaTeX.setEnabled(!readOnly);

		/* Zeile für Farben */
		content.add(subPanel=new JPanel(new FlowLayout(FlowLayout.LEFT)));

		/* Schriftfarbe */
		subPanel.add(subPanel2=new JPanel());
		subPanel2.setLayout(new BoxLayout(subPanel2,BoxLayout.PAGE_AXIS));

		subPanel2.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(label=new JLabel(Language.tr("Surface.AnimationText.Dialog.Color")+":"));

		subPanel2.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(colorChooser=new SmallColorChooser(Color.BLACK),BorderLayout.CENTER);
		colorChooser.setEnabled(!readOnly);
		label.setLabelFor(colorChooser);

		/* Hintergrundfarbe */
		subPanel.add(subPanel2=new JPanel());
		subPanel2.setLayout(new BoxLayout(subPanel2,BoxLayout.PAGE_AXIS));

		subPanel2.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(background=new JCheckBox(Language.tr("Surface.AnimationText.Dialog.FillBackground")),BorderLayout.NORTH);
		background.setEnabled(!readOnly);

		subPanel2.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(colorChooserBackground=new SmallColorChooser(Color.BLACK),BorderLayout.CENTER);
		colorChooserBackground.setEnabled(!readOnly);
		colorChooserBackground.addClickListener(e->background.setSelected(true));

		/* Deckkraft */
		content.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)),BorderLayout.SOUTH);
		JLabel alphaLabel=new JLabel(Language.tr("Surface.AnimationText.Dialog.Alpha")+":");
		line.add(alphaLabel);
		line.add(alpha=new JSlider(0,100,100));
		alphaLabel.setLabelFor(alpha);
		alpha.setEnabled(!readOnly);
		alpha.setMinorTickSpacing(1);
		alpha.setMajorTickSpacing(10);
		Hashtable<Integer,JComponent> labels=new Hashtable<>();
		for (int i=0;i<=10;i++) labels.put(i*10,new JLabel(NumberTools.formatPercent(i/10.0)));
		alpha.setLabelTable(labels);
		alpha.setPaintTicks(true);
		alpha.setPaintLabels(true);
		alpha.setPreferredSize(new Dimension(400,alpha.getPreferredSize().height));
		alpha.addChangeListener(e->background.setSelected(true));

		/* Werte initialisieren */
		if (element instanceof ModelElementAnimationTextValue) {
			final ModelElementAnimationTextValue text=(ModelElementAnimationTextValue)element;
			switch (text.getMode()) {
			case MODE_EXPRESSION_NUMBER:
				optionExpression.setSelected(true);
				editExpression.setText(text.getExpression());
				optionFormat.setSelectedIndex(0);
				digits.setValue(text.getDigits());
				optionTime.setSelected(false);
				optionDate.setSelected(false);
				dateTime.setDate(DateTools.getNow(false));
				break;
			case MODE_EXPRESSION_PERCENT:
				optionExpression.setSelected(true);
				editExpression.setText(text.getExpression());
				optionFormat.setSelectedIndex(1);
				digits.setValue(text.getDigits());
				optionTime.setSelected(false);
				optionDate.setSelected(false);
				dateTime.setDate(DateTools.getNow(false));
				break;
			case MODE_EXPRESSION_TIME:
				optionExpression.setSelected(true);
				editExpression.setText(text.getExpression());
				optionFormat.setSelectedIndex(2);
				digits.setValue(text.getDigits());
				digits.setEnabled(false);
				optionTime.setSelected(false);
				optionDate.setSelected(false);
				dateTime.setDate(DateTools.getNow(false));
				break;
			case MODE_TIME:
				optionExpression.setSelected(false);
				editExpression.setText("123");
				optionFormat.setSelectedIndex(0);
				optionTime.setSelected(true);
				optionDate.setSelected(false);
				dateTime.setDate(DateTools.getNow(false));
				break;
			case MODE_DATE:
				optionExpression.setSelected(false);
				editExpression.setText("123");
				optionFormat.setSelectedIndex(0);
				optionTime.setSelected(false);
				optionDate.setSelected(true);
				dateTime.setDate(text.getDateZero()*1000);
				break;
			}

			sizeField.setText(""+text.getTextSize());
			optionBold.setSelected(text.getTextBold());
			optionItalic.setSelected(text.getTextItalic());

			preText.setText(text.getPreText());
			postText.setText(text.getPostText());
			optionInterpretSymbols.setSelected(text.isInterpretSymbols());
			if (text.isInterpretSymbols()) {
				ScriptEditorAreaBuilder.setEntityAutoComplete(preText,optionInterpretSymbols.isSelected());
				ScriptEditorAreaBuilder.setEntityAutoComplete(postText,optionInterpretSymbols.isSelected());
			}
			optionInterpretMarkdown.setSelected(text.isInterpretMarkdown());
			optionInterpretLaTeX.setSelected(text.isInterpretLaTeX());

			colorChooser.setColor(text.getColor());
			background.setSelected(text.getFillColor()!=null);
			colorChooserBackground.setColor(text.getFillColor());
			alpha.setValue((int)Math.round(100*text.getFillAlpha()));
		}

		checkData(false);

		return content;
	}

	/**
	 * Prüft, ob die eingegebenen Daten in Ordnung sind.
	 * @param showErrorMessages	Wird hier <code>true</code> übergeben, so wird eine Fehlermeldung ausgegeben, wenn die Daten nicht in Ordnung sind.
	 * @return	Gibt <code>true</code> zurück, wenn die Daten in Ordnung sind.
	 */
	private boolean checkData(final boolean showErrorMessages) {
		if (readOnly) return false;

		boolean ok=true;

		boolean expressionOk=true;
		int error=0;
		final String text=editExpression.getText().trim();
		if (text.isEmpty()) {
			expressionOk=false;
			editExpression.setBackground(Color.red);
		} else {
			error=ExpressionCalc.check(text,element.getSurface().getMainSurfaceVariableNames(element.getModel().getModelVariableNames(),false),element.getModel().userFunctions);
			if (error>=0) {
				expressionOk=false;
				editExpression.setBackground(Color.red);
			} else {
				editExpression.setBackground(NumberTools.getTextFieldDefaultBackground());
			}
		}

		if (optionExpression.isSelected()) {
			if (!expressionOk) {
				ok=false;
				if (showErrorMessages) {
					if (text.isEmpty()) {
						MsgBox.error(this,Language.tr("Surface.AnimationText.Dialog.Expression.Error.Title"),Language.tr("Surface.AnimationText.Dialog.Expression.Error.InfoMissing"));
					} else {
						MsgBox.error(this,Language.tr("Surface.AnimationText.Dialog.Expression.Error.Title"),String.format(Language.tr("Surface.AnimationText.Dialog.Expression.Error.InfoInvalid"),text,error+1));
					}
					return false;
				}
			}
		}

		if (optionDate.isSelected()) {
			if (!dateTime.check()) {
				ok=false;
				if (showErrorMessages) {
					MsgBox.error(this,Language.tr("Surface.AnimationText.Dialog.Date.Error.Title"),Language.tr("Surface.AnimationText.Dialog.Date.Error.InfoInvalid"));
					return false;
				}
			}
		}

		Integer I=NumberTools.getNotNegativeInteger(sizeField,true);
		if (I==null) {
			ok=false;
			if (showErrorMessages) {
				MsgBox.error(this,Language.tr("Surface.AnimationText.Dialog.FontSize.Error.Title"),Language.tr("Surface.AnimationText.Dialog.FontSize.Error.Info"));
				return false;
			}
		}

		return ok;
	}

	/**
	 * Wird beim Klicken auf "Ok" aufgerufen, um zu prüfen, ob die Daten in der aktuellen Form
	 * in Ordnung sind und gespeichert werden können.
	 * @return	Gibt <code>true</code> zurück, wenn die Daten in Ordnung sind.
	 */
	@Override
	protected boolean checkData() {
		return checkData(true);
	}

	/**
	 * Speichert die Dialog-Daten in dem zugehörigen Daten-Objekt.<br>
	 * (Diese Routine wird beim Klicken auf "Ok" nach <code>checkData</code> aufgerufen.
	 * @see #checkData()
	 */
	@Override
	protected void storeData() {
		super.storeData();

		if (!(element instanceof ModelElementAnimationTextValue)) return;
		final ModelElementAnimationTextValue text=(ModelElementAnimationTextValue)element;

		/* Anzuzeigender Text */
		if (optionExpression.isSelected()) {
			text.setDigits((Integer)digits.getValue());
			switch (optionFormat.getSelectedIndex()) {
			case 0: text.setMode(ModelElementAnimationTextValue.ModeExpression.MODE_EXPRESSION_NUMBER); break;
			case 1: text.setMode(ModelElementAnimationTextValue.ModeExpression.MODE_EXPRESSION_PERCENT); break;
			case 2: text.setMode(ModelElementAnimationTextValue.ModeExpression.MODE_EXPRESSION_TIME); break;
			}
			text.setExpression(editExpression.getText().trim());
		} else {
			if (optionTime.isSelected()) {
				text.setMode(ModelElementAnimationTextValue.ModeExpression.MODE_TIME);
			} else {
				text.setMode(ModelElementAnimationTextValue.ModeExpression.MODE_DATE);
				text.setDateZero(dateTime.getDate()/1000);
			}
		}

		/* Schriftart */
		text.setFontFamily((FontCache.FontFamily)fontFamilyComboBox.getSelectedItem());

		/* Schriftgröße */
		Integer I=NumberTools.getNotNegativeInteger(sizeField,true);
		if (I!=null) text.setTextSize(I);

		/* Fett/Kursiv */
		text.setTextBold(optionBold.isSelected());
		text.setTextItalic(optionItalic.isSelected());

		/* Pre- und Posttext */
		text.setPreText(preText.getText());
		text.setPostText(postText.getText());

		/* Interpretation von Symbolen */
		text.setInterpretSymbols(optionInterpretSymbols.isSelected());
		text.setInterpretMarkdown(optionInterpretMarkdown.isSelected());
		text.setInterpretLaTeX(optionInterpretLaTeX.isSelected());

		/* Schriftfarbe */
		text.setColor(colorChooser.getColor());

		/* Hintergrundfarbe */
		if (background.isSelected()) {
			text.setFillColor(colorChooserBackground.getColor());
		} else {
			text.setFillColor(null);
		}

		/* Deckkraft */
		text.setFillAlpha(alpha.getValue()/100.0);
	}

	/**
	 * Zeigt eine gerenderte Vorschau als Popupmenü an.
	 */
	private class JPreviewButton extends JButton {
		/**
		 * Serialisierungs-ID der Klasse
		 * @see Serializable
		 */
		private static final long serialVersionUID=6601504627066654048L;

		/**
		 * Datenquelle für den Text
		 */
		private final RSyntaxTextArea textGetter;

		/**
		 * Konstruktor
		 * @param textGetter	Datenquelle für den Text
		 */
		public JPreviewButton(final RSyntaxTextArea textGetter) {
			this.textGetter=textGetter;
			addActionListener(e->showPreview());
			setIcon(Images.ZOOM.getIcon());
			setToolTipText(Language.tr("Surface.AnimationText.Dialog.Preview"));
		}

		/**
		 * Zeigt das Popoup an.
		 */
		private void showPreview() {
			final JPopupMenu popup=new JPopupMenu();

			final ModelElementTextPreviewPanel preview=new ModelElementTextPreviewPanel() {
				/**
				 * Serialisierungs-ID der Klasse
				 * @see Serializable
				 */
				private static final long serialVersionUID=2544122055927821610L;

				@Override
				public Dimension getPreferredSize() {
					final Dimension d=super.getPreferredSize();
					d.width=Math.max(d.width,300);
					d.height=Math.max(d.height,100);
					return d;
				}
			};
			preview.set(
					optionInterpretMarkdown.isSelected(),
					optionInterpretLaTeX.isSelected(),
					optionInterpretSymbols.isSelected(),
					textGetter.getText(),
					colorChooser.getColor(),
					(background.isSelected()?colorChooserBackground.getColor():null),
					alpha.getValue()/100.0,
					14,
					optionBold.isSelected(),
					optionItalic.isSelected(),
					(FontCache.FontFamily)fontFamilyComboBox.getSelectedItem(),
					ModelElementText.TextAlign.LEFT);
			popup.add(preview);

			popup.show(this,0,getHeight());
		}
	}
}
