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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.Serializable;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;

import language.Language;
import mathtools.NumberTools;
import systemtools.BaseDialog;
import systemtools.ColorChooserButton;
import systemtools.MsgBox;
import ui.images.Images;
import ui.modeleditor.AnimationImageSource;
import ui.modeleditor.ModelAnimationImages;
import ui.modeleditor.ModelElementBaseDialog;

/**
 * Dialog, der die Auswahl einer Farbe f�r einen Kundentyp in der Statistik<br>
 * Dieser Dialog wird von <code>EditorPanelDialog</code> verwendet.
 * @author Alexander Herzog
 * @see ModelPropertiesDialog
 */
public class ClientDataDialog extends BaseDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = 7425813595312162449L;

	/**
	 * Objekt das die verf�gbaren Animations-Icons vorh�lt
	 */
	private final AnimationImageSource imageSource;

	/** Auswahloption: Farbe automatisch festlegen */
	private final JRadioButton optionAutomaticColor;
	/** Auswahloption: Benutzerdefinierte Farbefestlegen */
	private final JRadioButton optionUserColor;
	/** Auswahlfeld f�r benutzerdefinierte Farbe */
	private final ColorChooserButton colorChooser;
	/** Datenmodell f�r Animations-Icon Auswahlfeld {@link #iconChooser} */
	private final DefaultComboBoxModel<JLabel> iconChooserList;
	/** Animations-Icon Auswahlfeld */
	private final JComboBox<JLabel> iconChooser;

	/** Eingabefeld f�r die Wartezeitkosten */
	private final JTextField costsWaiting;
	/** Eingabefeld f�r die Transferzeitkosten */
	private final JTextField costsTransfer;
	/** Eingabefeld f�r die Bedienzeitkosten */
	private final JTextField costsProcess;

	/**
	 * Konstruktor der Klasse <code>EditorPanelClientDataDialog</code>
	 * @param owner	�bergeordnetes Element
	 * @param help	Runnable, das aufgerufen wird, wenn der Nutzer auf die Hilfe-Schaltfl�che klickt
	 * @param userColor	Bisher gew�hlte benutzerdefinierte Farbe oder <code>null</code>, wenn die Farbe automatisch festgelegt werden soll
	 * @param icon	Animationsicon f�r diesen Kundentyp
	 * @param costs	3-elementiges Array aus Wartezeit-, Transferzeit- und Bedienzeitkosten
	 * @param modelImages	Benutzerdefinierte Animationsicons
	 * @param readOnly	Gibt an, ob die Einstellungen ver�ndert werden d�rfen
	 */
	public ClientDataDialog(final Component owner, final Runnable help, final Color userColor, final String icon, final double[] costs, final ModelAnimationImages modelImages, final boolean readOnly) {
		super(owner,Language.tr("Editor.ClientDialog.Title"),readOnly);
		imageSource=new AnimationImageSource();
		final JPanel main=createGUI(help);
		main.setLayout(new BorderLayout());

		JPanel content, sub, line;
		JLabel label;
		Object[] data;

		JTabbedPane tabs=new JTabbedPane();
		main.add(tabs,BorderLayout.CENTER);

		/* Tab: "Icon" */
		tabs.addTab(Language.tr("Editor.ClientDialog.Tab.Icon"),content=new JPanel(new BorderLayout()));

		/* Icon-Combobox */
		content.add(sub=new JPanel(),BorderLayout.NORTH);
		sub.setLayout(new BoxLayout(sub,BoxLayout.PAGE_AXIS));

		sub.add(new JSeparator());

		sub.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(new JLabel("<html><body>"+Language.tr("Editor.ClientDialog.Tab.Icon.Hint")+"</body></html>"));

		sub.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(label=new JLabel(Language.tr("Editor.ClientDialog.Tab.ColorAndIcon.IconForClientType")+":"));
		line.add(iconChooser=new JComboBox<>());
		iconChooserList=imageSource.getIconsComboBox(modelImages);
		iconChooser.setModel(iconChooserList);
		iconChooser.setRenderer(new AnimationImageSource.IconComboBoxCellRenderer());
		iconChooser.setEnabled(!readOnly);
		label.setLabelFor(iconChooser);

		/* Icon-Combobox mit Vorgabe belegen */
		int index=0;
		if (icon!=null) for (int i=0;i<iconChooserList.getSize();i++) {
			String name=iconChooserList.getElementAt(i).getText();
			String value=AnimationImageSource.ICONS.getOrDefault(name,name);
			if (icon.equalsIgnoreCase(value)) {index=i; break;}
		}
		iconChooser.setSelectedIndex(index);

		/* Tab: "Farbe" */
		tabs.addTab(Language.tr("Editor.ClientDialog.Tab.Color"),content=new JPanel(new BorderLayout()));

		/* Radiobuttons */
		content.add(sub=new JPanel(),BorderLayout.NORTH);
		sub.setLayout(new BoxLayout(sub,BoxLayout.PAGE_AXIS));

		sub.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(label=new JLabel("<html><body>"+Language.tr("Editor.ClientDialog.Tab.Color.Hint")+"</body></html>"));

		sub.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(optionAutomaticColor=new JRadioButton(Language.tr("Editor.ClientDialog.Tab.ColorAndIcon.Color.Automatic")));
		optionAutomaticColor.setEnabled(!readOnly);
		sub.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(optionUserColor=new JRadioButton(Language.tr("Editor.ClientDialog.Tab.ColorAndIcon.Color.UserDefined")+":"));
		optionUserColor.setEnabled(!readOnly);
		line.add(Box.createHorizontalStrut(5));
		line.add(colorChooser=new ColorChooserButton(userColor));
		colorChooser.setEnabled(!readOnly);
		colorChooser.addClickListener(e->optionUserColor.setSelected(true));

		final ButtonGroup buttonGroup=new ButtonGroup();
		buttonGroup.add(optionAutomaticColor);
		buttonGroup.add(optionUserColor);
		optionAutomaticColor.setSelected(userColor==null);
		optionUserColor.setSelected(userColor!=null);

		/* Tab: "Kosten" */
		tabs.addTab(Language.tr("Editor.ClientDialog.Tab.Costs"),content=new JPanel(new BorderLayout()));
		content.add(sub=new JPanel(),BorderLayout.NORTH);
		sub.setLayout(new BoxLayout(sub,BoxLayout.PAGE_AXIS));

		data=ModelElementBaseDialog.getInputPanel(Language.tr("Editor.ClientDialog.Tab.Costs.PerWaitingSecond")+":",NumberTools.formatNumber(costs[0]),10);
		sub.add((JPanel)data[0]);
		costsWaiting=(JTextField)data[1];
		costsWaiting.setEnabled(!readOnly);
		costsWaiting.addKeyListener(new KeyListener(){
			@Override public void keyTyped(KeyEvent e) {checkData(false);}
			@Override public void keyPressed(KeyEvent e) {checkData(false);}
			@Override public void keyReleased(KeyEvent e) {checkData(false);}
		});

		data=ModelElementBaseDialog.getInputPanel(Language.tr("Editor.ClientDialog.Tab.Costs.PerTransferSecond")+":",NumberTools.formatNumber(costs[1]),10);
		sub.add((JPanel)data[0]);
		costsTransfer=(JTextField)data[1];
		costsTransfer.setEnabled(!readOnly);
		costsTransfer.addKeyListener(new KeyListener(){
			@Override public void keyTyped(KeyEvent e) {checkData(false);}
			@Override public void keyPressed(KeyEvent e) {checkData(false);}
			@Override public void keyReleased(KeyEvent e) {checkData(false);}
		});

		data=ModelElementBaseDialog.getInputPanel(Language.tr("Editor.ClientDialog.Tab.Costs.PerProcessSecond")+":",NumberTools.formatNumber(costs[2]),10);
		sub.add((JPanel)data[0]);
		costsProcess=(JTextField)data[1];
		costsProcess.setEnabled(!readOnly);
		costsProcess.addKeyListener(new KeyListener(){
			@Override public void keyTyped(KeyEvent e) {checkData(false);}
			@Override public void keyPressed(KeyEvent e) {checkData(false);}
			@Override public void keyReleased(KeyEvent e) {checkData(false);}
		});

		/* Icons auf Tabs */
		tabs.setIconAt(0,Images.MODELPROPERTIES_CLIENTS_ICON.getIcon());
		tabs.setIconAt(1,Images.MODELPROPERTIES_CLIENTS_COLOR.getIcon());
		tabs.setIconAt(2,Images.MODELPROPERTIES_CLIENTS_COSTS.getIcon());

		pack();
		setLocationRelativeTo(this.owner);
	}

	/**
	 * Pr�ft, ob die eingegebenen Daten in Ordnung sind.
	 * @param showErrorMessages	Wird hier <code>true</code> �bergeben, so wird eine Fehlermeldung ausgegeben, wenn die Daten nicht in Ordnung sind.
	 * @return	Gibt <code>true</code> zur�ck, wenn die Daten in Ordnung sind.
	 */
	private boolean checkData(final boolean showErrorMessages) {
		boolean ok=true;
		Double D;

		D=NumberTools.getDouble(costsWaiting,true);
		if (D==null) {
			ok=false;
			if (showErrorMessages){
				MsgBox.error(this,Language.tr("Editor.ClientDialog.Tab.Costs.Error"),Language.tr("Editor.ClientDialog.Tab.Costs.PerWaitingSecond.Error"));
				return false;
			}
		}

		D=NumberTools.getDouble(costsTransfer,true);
		if (D==null) {
			ok=false;
			if (showErrorMessages){
				MsgBox.error(this,Language.tr("Editor.ClientDialog.Tab.Costs.Error"),Language.tr("Editor.ClientDialog.Tab.Costs.PerTransferSecond.Error"));
				return false;
			}
		}

		D=NumberTools.getDouble(costsProcess,true);
		if (D==null) {
			ok=false;
			if (showErrorMessages){
				MsgBox.error(this,Language.tr("Editor.ClientDialog.Tab.Costs.Error"),Language.tr("Editor.ClientDialog.Tab.Costs.PerProcessSecond.Error"));
				return false;
			}
		}

		return ok;
	}

	/**
	 * Wird beim Klicken auf "Ok" aufgerufen, um zu pr�fen, ob die Daten in der aktuellen Form
	 * in Ordnung sind und gespeichert werden k�nnen.
	 * @return	Gibt <code>true</code> zur�ck, wenn die Daten in Ordnung sind.
	 */
	@Override
	protected boolean checkData() {
		return checkData(true);
	}

	/**
	 * Liefert die eingestellte Farbe f�r den Kundentyp
	 * @return	Eingestellte benutzerdefinierte Farbe oder <code>null</code>, wenn automatisch eine Farbe gew�hlt werden soll.
	 */
	public Color getUserColor() {
		if (optionAutomaticColor.isSelected()) return null;
		return colorChooser.getColor();
	}

	/**
	 * Liefert das eingestellte Icon f�r den Kundentyp
	 * @return	Eingestelltes Icon f�r den Kundentyp
	 */
	public String getIcon() {
		String name=iconChooserList.getElementAt(iconChooser.getSelectedIndex()).getText();
		return AnimationImageSource.ICONS.getOrDefault(name,name);
	}

	/**
	 * Liefert die eingestellten Kosten f�r die Wartezeit-, Transferzeit- und Bedienzeiten
	 * @return	3-elementiges Array aus Wartezeit-, Transferzeit- und Bedienzeitkosten oder <code>null</code>, wenn keine Kosten hinterlegt sind
	 */
	public double[] getCosts() {
		Double D;

		D=NumberTools.getDouble(costsWaiting,true);
		final double d1=(D==null)?0:D;
		D=NumberTools.getDouble(costsTransfer,true);
		final double d2=(D==null)?0:D;
		D=NumberTools.getDouble(costsProcess,true);
		final double d3=(D==null)?0:D;

		return new double[]{d1,d2,d3};
	}
}