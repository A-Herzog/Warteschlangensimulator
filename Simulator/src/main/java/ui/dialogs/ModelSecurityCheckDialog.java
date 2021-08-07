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
package ui.dialogs;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.io.Serializable;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import language.Language;
import simulator.editmodel.EditModel;
import simulator.editmodel.EditModelCertificate;
import simulator.editmodel.EditModelCertificateStore;
import systemtools.BaseDialog;
import tools.JTableExt;
import tools.JTableExtAbstractTableModel;
import tools.SetupData;
import ui.help.Help;
import ui.images.Images;
import ui.modeleditor.ModelSurface;
import ui.modeleditor.coreelements.ModelElement;
import ui.modeleditor.coreelements.ModelElementBox;
import ui.modeleditor.coreelements.ModelElementPosition;
import ui.modeleditor.elements.AnimationExpression;
import ui.modeleditor.elements.ElementWithAnimationScripts;
import ui.modeleditor.elements.ElementWithScript;
import ui.modeleditor.elements.ModelElementDisposeWithTable;
import ui.modeleditor.elements.ModelElementOutput;
import ui.modeleditor.elements.ModelElementOutputDB;
import ui.modeleditor.elements.ModelElementOutputDDE;
import ui.modeleditor.elements.ModelElementOutputJS;
import ui.modeleditor.elements.ModelElementSub;

/**
 * Prüft ein in den Editor zu ladendes Modell auf Sicherheitsrisiken
 * @author Alexander Herzog
 * @see ModelSecurityCheckDialog#doSecurityCheck(EditModel, Component)
 */
public class ModelSecurityCheckDialog extends BaseDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = -2678240248035761903L;

	/** Hilfe-Runnable für diesen Dialog (und Unter-Dialoge) */
	private final Runnable help;

	/**
	 * Name der Nutzers, der das Modell signiert hat (kann <code>null</code> sein)
	 */
	private final String signatureUserName;

	/**
	 * Öffentlicher Schlüssel, der zum Signieren verwendet wurde (kann <code>null</code> sein)
	 */
	private final PublicKey signaturePublicKey;

	/**
	 * Soll dem verwendeten öffentlichen Schlüssel immer vertraut werden?
	 */
	private final JCheckBox alwaysTrust;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes visuelles Element (zur Ausrichtung des Dialogs)
	 * @param list	Liste mit den Sicherheitsrisiken
	 * @param signatureUserName	Name der Nutzers, der das Modell signiert hat (kann <code>null</code> sein)
	 * @param signaturePublicKey	Öffentlicher Schlüssel, der zum Signieren verwendet wurde (kann <code>null</code> sein)
	 */
	public ModelSecurityCheckDialog(final Component owner, final List<CriticalElement> list, final String signatureUserName, final PublicKey signaturePublicKey) {
		super(owner,Language.tr("ModelSecurityCheck.Title"));
		this.signatureUserName=signatureUserName;
		this.signaturePublicKey=signaturePublicKey;

		/* GUI */
		help=()->Help.topicModal(ModelSecurityCheckDialog.this.owner,"ModelSecurityCheck");
		final JPanel content=createGUI(help);
		content.setLayout(new BorderLayout());

		/* Erklärungszeile oben */
		final JPanel top=new JPanel(new FlowLayout(FlowLayout.LEFT));
		content.add(top,BorderLayout.NORTH);
		top.add(new JLabel(String.format((list.size()==1)?Language.tr("ModelSecurityCheck.Info.Singular"):Language.tr("ModelSecurityCheck.Info.Plural"),list.size())));

		/* Tabelle in der Mitte */
		final JTableExt table=new JTableExt();
		final ModelSecurityCheckTableModel tableModel=new ModelSecurityCheckTableModel(list);
		table.setModel(tableModel);
		table.getColumnModel().getColumn(0).setMaxWidth(125);
		table.getColumnModel().getColumn(0).setMinWidth(125);
		table.getColumnModel().getColumn(1).setMaxWidth(25);
		table.getColumnModel().getColumn(1).setMinWidth(25);
		table.getColumnModel().getColumn(2).setMaxWidth(125);
		table.getColumnModel().getColumn(2).setMinWidth(125);
		table.getColumnModel().getColumn(3).setMaxWidth(125);
		table.getColumnModel().getColumn(3).setMinWidth(125);
		table.getColumnModel().getColumn(4).setMinWidth(150);
		table.getColumnModel().getColumn(5).setMaxWidth(50);
		table.getColumnModel().getColumn(5).setMinWidth(50);
		table.setIsPanelCellTable(5);
		content.add(new JScrollPane(table),BorderLayout.CENTER);

		/* Externem Nutzer / Zertifikat vertrauen */
		if (signatureUserName!=null && !signatureUserName.trim().isEmpty() && signaturePublicKey!=null) {
			JPanel line;
			final JPanel bottom=new JPanel();
			bottom.setLayout(new BoxLayout(bottom,BoxLayout.PAGE_AXIS));
			content.add(bottom,BorderLayout.SOUTH);
			bottom.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
			line.add(new JLabel(String.format("<html><body>"+Language.tr("ModelSecurityCheck.Signature.Info")+"</body></html>",encodeHTML(signatureUserName))));
			bottom.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
			line.add(alwaysTrust=new JCheckBox(Language.tr("ModelSecurityCheck.Signature.AlwaysTrust")));
		} else {
			alwaysTrust=null;
		}

		/* Dialog starten */
		setMinSizeRespectingScreensize(700,400);
		setResizable(true);
		pack();
		setLocationRelativeTo(getOwner());
		SwingUtilities.invokeLater(()->focusOkButton());
		setVisible(true);
	}

	/**
	 * Wandelt die Zeichen "&amp;", "&lt;" und "&gt;" in ihre entsprechenden
	 * HTML-Entitäten um.
	 * @param line	Umzuwandelnder Text
	 * @return	Umgewandelter Text
	 */
	private static String encodeHTML(final String line) {
		if (line==null) return "";
		String result;
		result=line.replaceAll("&","&amp;");
		result=result.replaceAll("<","&lt;");
		result=result.replaceAll(">","&gt;");
		return result;
	}

	/**
	 * Prüft eine Station auf sicherheitskritische Eigenschaften.
	 * @param element	Zu prüfende Station
	 * @return	Liefert ggf. ein Informationobjekt mit Daten zu der sicherheitskritischen Eigenschaften zurück oder <code>null</code>, wenn keine sicherheitskritischen Eigenschaften vorliegne
	 * @see #getCriticalElements(ModelSurface)
	 */
	private static CriticalElement[] testElement(final ModelElementPosition element) {
		final List<CriticalElement> list=new ArrayList<>();

		if (element instanceof ModelElementOutput) {
			final ModelElementOutput box=(ModelElementOutput)element;
			list.add(new CriticalElement(box,CriticalType.FILE_OUTPUT,box.getOutputFile()));
		}
		if (element instanceof ModelElementOutputJS) {
			final ModelElementOutputJS box=(ModelElementOutputJS)element;
			list.add(new CriticalElement(box,CriticalType.FILE_OUTPUT,box.getOutputFile()));
		}
		if (element instanceof ModelElementOutputDDE) {
			final ModelElementOutputDDE box=(ModelElementOutputDDE)element;
			list.add(new CriticalElement(box,CriticalType.DDE_OUTPUT,box.getWorkbook()));
		}
		if (element instanceof ModelElementOutputDB) {
			final ModelElementOutputDB box=(ModelElementOutputDB)element;
			list.add(new CriticalElement(box,CriticalType.DB_OUTPUT,box.getDb().getConfig()));
		}
		if (element instanceof ModelElementDisposeWithTable) {
			final ModelElementDisposeWithTable box=(ModelElementDisposeWithTable)element;
			list.add(new CriticalElement(box,CriticalType.FILE_OUTPUT,box.getOutputFile()));
		}
		if ((element instanceof ElementWithScript) && (element instanceof ModelElementBox)) {
			final ElementWithScript.ScriptMode scriptMode=((ElementWithScript)element).getMode();
			final String script=((ElementWithScript)element).getScript();
			list.add(new CriticalElement((ModelElementBox)element,scriptMode,script));
		}
		if ((element instanceof ElementWithAnimationScripts) && (element instanceof ModelElementPosition)) {
			list.addAll(Arrays.asList(((ElementWithAnimationScripts)element).getAnimationScripts()).stream().map(expression->new CriticalElement(element,expression)).collect(Collectors.toList()));
		}

		if (list.size()==0) return null;
		return list.toArray(new CriticalElement[0]);
	}

	@Override
	protected void storeData() {
		if (alwaysTrust!=null && alwaysTrust.isSelected()) {
			final EditModelCertificateStore certStore=new EditModelCertificateStore();
			certStore.registerTrustedPublicKey(signatureUserName,signaturePublicKey);
		}
	}

	/**
	 * Liefert Informationen zu den sicherheitskritischen Eigenschaften aller Stationen.
	 * @param surface	Haupt-Zeichenfläche
	 * @return	Liste mit allen sicherheitskritischen Eigenschaften (kann leer sein, ist aber nie <code>null</code>)
	 * @see #doSecurityCheck(EditModel, Component)
	 */
	public static List<CriticalElement> getCriticalElements(final ModelSurface surface) {
		final List<CriticalElement> list=new ArrayList<>();

		for (ModelElement element: surface.getElements()) if (element instanceof ModelElementPosition) {
			final CriticalElement[] criticalRecords=testElement((ModelElementPosition)element);
			if (criticalRecords!=null) for (CriticalElement criticalRecord: criticalRecords) list.add(criticalRecord);
			if (element instanceof ModelElementSub) list.addAll(getCriticalElements(((ModelElementSub)element).getSubSurface()));
		}


		return list;
	}

	/**
	 * Name der Nutzers, der das Modell signiert hat (kann <code>null</code> sein)<br>
	 * (Beim Interpretieren der Daten via {@link EditModelCertificate})
	 */

	private static String signatureExternalUserName;

	/**
	 * Öffentlicher Schlüssel, der zum Signieren verwendet wurde (kann <code>null</code> sein)<br>
	 * (Beim Interpretieren der Daten via {@link EditModelCertificate})
	 */
	private static PublicKey signatureExternalPublicKey;

	/**
	 * Führt die Sicherheitsprüfung eines Modells durch und zeigt dabei nötigenfalls einen Dialog an.
	 * @param model	Zu prüfendes Modell
	 * @param owner Übergeordnetes visuelles Element (zur Ausrichtung des Dialogs)
	 * @return	Gibt <code>true</code> zurück, wenn das Laden des Modells freigegeben wurde.
	 */
	public static boolean doSecurityCheck(final EditModel model, final Component owner) {
		final SetupData.ModelSecurity security=SetupData.getSetup().modelSecurity;

		/* Alles erlauben? */
		if (security==SetupData.ModelSecurity.ALLOWALL) return true;

		/* Erlauben auf Basis einer Signatur? */
		signatureExternalUserName=null;
		signatureExternalPublicKey=null;
		if (model.loadedModelCertificate!=null) {
			if (!model.loadedModelCertificate.isSecurityWarningNeeded(model,(userName,publicKey)->{signatureExternalUserName=userName; signatureExternalPublicKey=publicKey; return false;})) return true;
		}

		/* Kritische Elemente suchen */
		final List<CriticalElement> list=getCriticalElements(model.surface);
		if (list.size()==0) return true;

		/* Alles potentiell gefährliche sofort verbieten? */
		if (security==SetupData.ModelSecurity.STRICT) return false;

		/* Dialog anzeigen */
		final ModelSecurityCheckDialog dialog=new ModelSecurityCheckDialog(owner,list,signatureExternalUserName,signatureExternalPublicKey);
		return dialog.getClosedBy()==BaseDialog.CLOSED_BY_OK;
	}

	/**
	 * Arten von kritischen Stationen
	 * @author Alexander Herzog
	 *
	 */
	public enum CriticalType {
		/** Dateiausgabe */
		FILE_OUTPUT,
		/** DDE-Datenausgabe */
		DDE_OUTPUT,
		/** Datenbankausgabe */
		DB_OUTPUT,
		/** Scripting (Javascript) */
		SCRIPT_JAVASCRIPT,
		/** Scripting (Java) */
		SCRIPT_JAVA
	}

	/**
	 * Beschreibung eines kritischen Elements
	 * @author Alexander Herzog
	 */
	public static class CriticalElement {
		/**
		 * Stationstyp als Text
		 */
		public final String stationType;

		/**
		 * Stations-ID
		 */
		public final int stationId;

		/**
		 * Name der Station
		 */
		public final String stationName;

		/**
		 * Was ist daran kritisch?
		 * @see ModelSecurityCheckDialog.CriticalType
		 */
		public final CriticalType type;

		/**
		 * Zusätzliche Beschreibung
		 */
		public final String info;

		/**
		 * Konstruktor der Klasse
		 * @param station	Station, die als kritisch eingestuft wurde
		 * @param type	Was ist daran kritisch?
		 * @param info	Zusätzliche Beschreibung
		 */
		public CriticalElement(final ModelElementBox station, final CriticalType type, final String info) {
			stationType=station.getTypeName();
			stationId=station.getId();
			if (station.getName().trim().isEmpty())	{
				stationName=String.format(Language.tr("ModelSecurityCheck.Station.NoName"),station.getId());
			} else {
				stationName=String.format(Language.tr("ModelSecurityCheck.Station.WithName"),station.getName(),station.getId());
			}

			this.type=type;
			this.info=info;
		}

		/**
		 * Konstruktor der Klasse
		 * @param station	Station, die als kritisch eingestuft wurde
		 * @param type	Was ist daran kritisch?
		 * @param info	Zusätzliche Beschreibung
		 */
		public CriticalElement(final ModelElementBox station, final ElementWithScript.ScriptMode type, final String info) {
			stationType=station.getTypeName();
			stationId=station.getId();
			if (station.getName().trim().isEmpty())	{
				stationName=String.format(Language.tr("ModelSecurityCheck.Station.NoName"),station.getId());
			} else {
				stationName=String.format(Language.tr("ModelSecurityCheck.Station.WithName"),station.getName(),station.getId());
			}

			switch (type) {
			case Javascript: this.type=CriticalType.SCRIPT_JAVASCRIPT; break;
			case Java: this.type=CriticalType.SCRIPT_JAVA; break;
			default: this.type=CriticalType.SCRIPT_JAVASCRIPT; break;
			}
			this.info=info;
		}

		/**
		 * Konstruktor der Klasse
		 * @param station	Station, die als kritisch eingestuft wurde
		 * @param expression	Animation-Skript-Objekt
		 * @see AnimationExpression
		 */
		public CriticalElement(final ModelElementPosition station, AnimationExpression expression) {
			stationType=station.getContextMenuElementName();
			stationId=station.getId();
			if (station.getName().trim().isEmpty())	{
				stationName=String.format(Language.tr("ModelSecurityCheck.Station.NoName"),station.getId());
			} else {
				stationName=String.format(Language.tr("ModelSecurityCheck.Station.WithName"),station.getName(),station.getId());
			}

			switch (expression.getMode()) {
			case Javascript: this.type=CriticalType.SCRIPT_JAVASCRIPT; break;
			case Java: this.type=CriticalType.SCRIPT_JAVA; break;
			default: this.type=CriticalType.SCRIPT_JAVASCRIPT; break;
			}
			this.info=expression.getScript();
		}
	}

	/**
	 * Tabellenmodell das die Daten zu den gefundenen Problemen vorhält
	 */
	private class ModelSecurityCheckTableModel extends JTableExtAbstractTableModel {
		/**
		 * Serialisierungs-ID der Klasse
		 * @see Serializable
		 */
		private static final long serialVersionUID = 3042549921461322668L;
		/** Liste mit den sicherheitskritischen Eigenschaften */
		private final List<CriticalElement> list;
		/** Schaltflächen zur Anzeige von Details */
		private final JButton[] button;

		/**
		 * Konstruktor der Klasse
		 * @param list	Liste mit den sicherheitskritischen Eigenschaften
		 */
		public ModelSecurityCheckTableModel(final List<CriticalElement> list) {
			this.list=list;
			button=new JButton[list.size()];
		}

		@Override
		public int getRowCount() {
			return list.size();
		}

		@Override
		public int getColumnCount() {
			return 6;
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			if (rowIndex<0 || rowIndex>=list.size()) return null;
			final CriticalElement critical=list.get(rowIndex);

			switch (columnIndex) {
			case 0:
				return critical.stationType;
			case 1:
				return critical.stationId;
			case 2:
				return critical.stationName;
			case 3:
				switch (critical.type) {
				case DB_OUTPUT: return Language.tr("ModelSecurityCheck.CriticalType.DBOutput");
				case DDE_OUTPUT: return Language.tr("ModelSecurityCheck.CriticalType.DDEOutput");
				case FILE_OUTPUT: return Language.tr("ModelSecurityCheck.CriticalType.FileOutput");
				case SCRIPT_JAVASCRIPT: return Language.tr("ModelSecurityCheck.CriticalType.Script");
				case SCRIPT_JAVA: return Language.tr("ModelSecurityCheck.CriticalType.Script");
				default: return "";
				}
			case 4:
				switch (critical.type) {
				case DB_OUTPUT: return critical.info;
				case DDE_OUTPUT: return Language.tr("ModelSecurityCheck.CriticalType.DDEOutput.Workbook")+": "+critical.info;
				case FILE_OUTPUT: return Language.tr("ModelSecurityCheck.CriticalType.FileOutput.FileName")+": "+critical.info;
				case SCRIPT_JAVASCRIPT: return Language.tr("ModelSecurityCheck.CriticalType.Script.Info");
				case SCRIPT_JAVA: return Language.tr("ModelSecurityCheck.CriticalType.Script.Info");
				default: return "";
				}
			case 5:
				if (critical.type==CriticalType.SCRIPT_JAVASCRIPT || critical.type==CriticalType.SCRIPT_JAVA) {
					if (button[rowIndex]==null) {
						button[rowIndex]=new JButton("");
						button[rowIndex].setIcon(Images.GENERAL_SCRIPT.getIcon());
						button[rowIndex].setToolTipText(Language.tr("ModelSecurityCheck.CriticalType.Script.Tooltip"));
						button[rowIndex].addActionListener(e->{
							new ModelSecurityCheckScriptDialog(ModelSecurityCheckDialog.this,critical.stationType+" - "+critical.stationName,critical.type,critical.info,help);
						});
					}
					return button[rowIndex];
				} else {
					return "";
				}
			}
			return null;
		}

		@Override
		public final boolean isCellEditable(int rowIndex, int columnIndex) {
			return columnIndex==5;
		}

		@Override
		public String getColumnName(int column) {
			switch (column) {
			case 0: return Language.tr("ModelSecurityCheck.Column.StationType");
			case 1: return Language.tr("ModelSecurityCheck.Column.StationID");
			case 2: return Language.tr("ModelSecurityCheck.Column.StationName");
			case 3: return Language.tr("ModelSecurityCheck.Column.CriticalType");
			case 4: return Language.tr("ModelSecurityCheck.Column.CriticalInfo");
			case 5: return Language.tr("ModelSecurityCheck.Column.CriticalButton");
			}
			return "";
		}

	}
}
