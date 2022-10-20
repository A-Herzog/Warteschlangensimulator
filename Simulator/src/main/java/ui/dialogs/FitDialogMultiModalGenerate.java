/**
 * Copyright 2022 Alexander Herzog
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
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.Serializable;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.apache.commons.math3.distribution.AbstractRealDistribution;

import language.Language;
import mathtools.NumberTools;
import mathtools.distribution.DataDistributionImpl;
import mathtools.distribution.LogNormalDistributionImpl;
import mathtools.distribution.swing.JDistributionPanel;
import mathtools.distribution.tools.DistributionFitterBase;
import mathtools.distribution.tools.DistributionFitterMultiModal;
import mathtools.distribution.tools.DistributionRandomNumber;
import mathtools.distribution.tools.WrapperGammaDistribution;
import systemtools.BaseDialog;
import systemtools.MsgBox;
import ui.modeleditor.ModelElementBaseDialog;

/**
 * Zeigt einen Dialog zur Generierung von Testmesswerten
 * gemäß zweier überlagerter Verteilungen an.
 * @author Alexander Herzog
 * @see FitDialogMultiModal
 */
public class FitDialogMultiModalGenerate extends BaseDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID=7104851139769120097L;

	/**
	 * Welche Verteilung soll für die Teildichten verwendet werden?
	 */
	private final DistributionFitterMultiModal.FitDistribution distributionType;

	/**
	 * Einstellungen zu den Teildichten
	 * @see #checkData(boolean)
	 */
	private final GenerateSetup generateSetup;

	/**
	 * Panel zur Anzeige der Verteilung gemäß der eingestellten Werte
	 */
	private final JDistributionPanel distributionPanel;

	/**
	 * Eingabefeld für den Erwartungswert der ersten Verteilung
	 */
	private final JTextField mean1Edit;

	/**
	 * Eingabefeld für die Standardabweichung der ersten Verteilung
	 */
	private final JTextField sd1Edit;

	/**
	 * Eingabefeld für die Anzahl an Messwerten gemäß der ersten Verteilung
	 */
	private final JTextField n1Edit;

	/**
	 * Eingabefeld für den Erwartungswert der zweiten Verteilung
	 */
	private final JTextField mean2Edit;

	/**
	 * Eingabefeld für die Standardabweichung der zweiten Verteilung
	 */
	private final JTextField sd2Edit;

	/**
	 * Eingabefeld für die Anzahl an Messwerten gemäß der zweiten Verteilung
	 */
	private final JTextField n2Edit;

	/**
	 * Kostruktor der Klasse
	 * @param owner	Übergeordnetes Element
	 * @param distributionType	Welche Verteilung soll für die Teildichten verwendet werden?
	 * @param generateSetup	Bisherige Einstellungen zu den Teildichten
	 */
	public FitDialogMultiModalGenerate(final Component owner, final DistributionFitterMultiModal.FitDistribution distributionType, final GenerateSetup generateSetup) {
		super(owner,Language.tr("FitDialogMultiModalGenerator.Title"));

		this.distributionType=distributionType;
		this.generateSetup=new GenerateSetup(generateSetup);
		final JPanel content=createGUI(null);
		content.setLayout(new BorderLayout());

		/* Anzeige der Verteilung */
		content.add(distributionPanel=new JDistributionPanel(null,1000,false,JDistributionPanel.DENSITY),BorderLayout.CENTER);

		/* Eingabefelder */
		final JPanel setup=new JPanel();
		setup.setLayout(new BoxLayout(setup,BoxLayout.PAGE_AXIS));
		content.add(setup,BorderLayout.SOUTH);

		JPanel line;
		Object[] data;

		setup.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(new JLabel("<html><body><b>"+String.format(Language.tr("FitDialogMultiModalGenerator.Distribution"),1)+"</b></body></html>"));

		data=ModelElementBaseDialog.getInputPanel(Language.tr("FitDialogMultiModalGenerator.ExpectedValue")+":",NumberTools.formatNumberMax(generateSetup.mean1),10);
		setup.add((JPanel)data[0]);
		mean1Edit=(JTextField)data[1];
		setListener(mean1Edit);

		data=ModelElementBaseDialog.getInputPanel(Language.tr("FitDialogMultiModalGenerator.StandardDeviation")+":",NumberTools.formatNumberMax(generateSetup.sd1),10);
		setup.add((JPanel)data[0]);
		sd1Edit=(JTextField)data[1];
		setListener(sd1Edit);

		data=ModelElementBaseDialog.getInputPanel(Language.tr("FitDialogMultiModalGenerator.NumberOfValues")+":",""+generateSetup.n1,10);
		setup.add((JPanel)data[0]);
		n1Edit=(JTextField)data[1];
		setListener(n1Edit);

		setup.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(new JLabel("<html><body><b>"+String.format(Language.tr("FitDialogMultiModalGenerator.Distribution"),2)+"</b></body></html>"));

		data=ModelElementBaseDialog.getInputPanel(Language.tr("FitDialogMultiModalGenerator.ExpectedValue")+":",NumberTools.formatNumberMax(generateSetup.mean2),10);
		setup.add((JPanel)data[0]);
		mean2Edit=(JTextField)data[1];
		setListener(mean2Edit);

		data=ModelElementBaseDialog.getInputPanel(Language.tr("FitDialogMultiModalGenerator.StandardDeviation")+":",NumberTools.formatNumberMax(generateSetup.sd2),10);
		setup.add((JPanel)data[0]);
		sd2Edit=(JTextField)data[1];
		setListener(sd2Edit);

		data=ModelElementBaseDialog.getInputPanel(Language.tr("FitDialogMultiModalGenerator.NumberOfValues")+":",""+generateSetup.n2,10);
		setup.add((JPanel)data[0]);
		n2Edit=(JTextField)data[1];
		setListener(n2Edit);

		/* Daten initial aufbauen */
		checkData(false);

		/* Dialog starten */
		setSizeRespectingScreensize(800,600);
		setMinSizeRespectingScreensize(800,600);
		setLocationRelativeTo(getOwner());
		setVisible(true);
	}

	/**
	 * Stellt ein, dass die Daten neu ausgewertet werden, wenn in einem bestimmten Textfeld eine Taste gedrückt wird.
	 * @param textField	Textfeld bei dem auf Tastendrücke reagiert werden soll
	 */
	private void setListener(final JTextField textField) {
		textField.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				checkData(false);
			}
		});
	}

	/**
	 * Prüft, ob die eingegebenen Daten in Ordnung sind.
	 * @param showErrorMessages	Wird hier <code>true</code> übergeben, so wird eine Fehlermeldung ausgegeben, wenn die Daten nicht in Ordnung sind.
	 * @return	Gibt <code>true</code> zurück, wenn die Daten in Ordnung sind.
	 */
	private boolean checkData(final boolean showErrorMessages) {
		boolean ok=true;

		Double D;
		Long L;

		/* Verteilung 1 */

		D=NumberTools.getNotNegativeDouble(mean1Edit,true);
		if (D==null) {
			ok=false;
			if (showErrorMessages) {
				MsgBox.error(this,Language.tr("FitDialogMultiModalGenerator.ExpectedValue.ErrorTitle"),Language.tr("FitDialogMultiModalGenerator.ExpectedValue.ErrorInfo1"));
				return false;
			}
		} else {
			generateSetup.mean1=D;
		}

		D=NumberTools.getNotNegativeDouble(sd1Edit,true);
		if (D==null) {
			ok=false;
			if (showErrorMessages) {
				MsgBox.error(this,Language.tr("FitDialogMultiModalGenerator.StandardDeviation.ErrorTitle"),Language.tr("FitDialogMultiModalGenerator.StandardDeviation.ErrorInfo1"));
				return false;
			}
		} else {
			generateSetup.sd1=D;
		}

		L=NumberTools.getPositiveLong(n1Edit,true);
		if (D==null) {
			ok=false;
			if (showErrorMessages) {
				MsgBox.error(this,Language.tr("FitDialogMultiModalGenerator.NumberOfValues.ErrorTitle"),Language.tr("FitDialogMultiModalGenerator.NumberOfValues.ErrorInfo1"));
				return false;
			}
		} else {
			generateSetup.n1=L.intValue();
		}

		/* Verteilung 2 */

		D=NumberTools.getNotNegativeDouble(mean2Edit,true);
		if (D==null) {
			ok=false;
			if (showErrorMessages) {
				MsgBox.error(this,Language.tr("FitDialogMultiModalGenerator.ExpectedValue.ErrorTitle"),Language.tr("FitDialogMultiModalGenerator.ExpectedValue.ErrorInfo2"));
				return false;
			}
		} else {
			generateSetup.mean2=D;
		}

		D=NumberTools.getNotNegativeDouble(sd2Edit,true);
		if (D==null) {
			ok=false;
			if (showErrorMessages) {
				MsgBox.error(this,Language.tr("FitDialogMultiModalGenerator.StandardDeviation.ErrorTitle"),Language.tr("FitDialogMultiModalGenerator.StandardDeviation.ErrorInfo2"));
				return false;
			}
		} else {
			generateSetup.sd2=D;
		}

		L=NumberTools.getPositiveLong(n2Edit,true);
		if (D==null) {
			ok=false;
			if (showErrorMessages) {
				MsgBox.error(this,Language.tr("FitDialogMultiModalGenerator.NumberOfValues.ErrorTitle"),Language.tr("FitDialogMultiModalGenerator.NumberOfValues.ErrorInfo2"));
				return false;
			}
		} else {
			generateSetup.n2=L.intValue();
		}

		/* Kombinierte Verteilung anzeigen */

		if (ok) {
			distributionPanel.setDistribution(generateSetup.generateValuesDistribution(distributionType));
		}

		return ok;
	}

	@Override
	protected boolean checkData() {
		return checkData(true);
	}

	/**
	 * Liefert die Einstellungen zu den Teildichten.
	 * @return	Einstellungen zu den Teildichten
	 */
	public GenerateSetup getNewGenerateSetup() {
		return generateSetup;
	}

	/**
	 * Einstellungen zu den Teildichten
	 */
	public static class GenerateSetup {
		/**
		 * Erwartungswert der ersten Verteilung
		 */
		public double mean1;

		/**
		 * Standardabweichung der ersten Verteilung
		 */
		public double sd1;

		/**
		 * Anzahl an Messwerten gemäß der ersten Verteilung
		 */
		public int n1;

		/**
		 * Erwartungswert der zweiten Verteilung
		 */
		public double mean2;

		/**
		 * Standardabweichung der zweiten Verteilung
		 */
		public double sd2;

		/**
		 * Anzahl an Messwerten gemäß der zweiten Verteilung
		 */
		public int n2;

		/**
		 * Konstruktor der Klasse
		 */
		public GenerateSetup() {
			mean1=50;
			sd1=100;
			n1=1_000_000;

			mean2=150;
			sd2=30;
			n2=Math.round(n1/3);
		}

		/**
		 * Copy-Konstruktor der Klasse
		 * @param copySource	Zu kopierende Ausgangseinstellungen
		 */
		public GenerateSetup(final GenerateSetup copySource) {
			this();
			if (copySource!=null) {
				mean1=copySource.mean1;
				sd1=copySource.sd1;
				n1=copySource.n1;
				mean2=copySource.mean2;
				sd2=copySource.sd2;
				n2=copySource.n2;
			}
		}

		/**
		 * Generiert Testmesswerte gemäß den eingestellten Teilverteilungen.
		 * @param distributionType	Welche Verteilung soll für die Teildichten verwendet werden?
		 * @return	Testmesswerte
		 */
		public double[] generateValues(final DistributionFitterMultiModal.FitDistribution distributionType) {
			final double[] result=new double[n1+n2];
			AbstractRealDistribution dist;

			switch (distributionType) {
			case LOG_NORMAL: dist=new LogNormalDistributionImpl(mean1,sd1); break;
			case GAMMA: dist=new WrapperGammaDistribution().getDistribution(mean1,sd1); break;
			default: dist=new LogNormalDistributionImpl(mean1,sd1); break;
			}
			for (int i=0;i<n1;i++) result[i]=Math.min(1000,Math.max(0,DistributionRandomNumber.random(dist)));

			switch (distributionType) {
			case LOG_NORMAL: dist=new LogNormalDistributionImpl(mean2,sd2); break;
			case GAMMA: dist=new WrapperGammaDistribution().getDistribution(mean2,sd2); break;
			default: dist=new LogNormalDistributionImpl(mean2,sd2); break;
			}
			for (int i=0;i<n2;i++) result[n1+i]=Math.min(1000,Math.max(0,DistributionRandomNumber.random(dist)));

			return result;
		}

		/**
		 * Generiert Testmesswerte gemäß den eingestellten Teilverteilungen und liefert eine Häufigkeitsverteilung zurück.
		 * @param distributionType	Welche Verteilung soll für die Teildichten verwendet werden?
		 * @return	Häufigkeitsverteilung über die Testmesswerte
		 */

		public DataDistributionImpl generateValuesDistribution(final DistributionFitterMultiModal.FitDistribution distributionType) {
			final double[][] values=new double[][] {generateValues(distributionType)};
			final Object[] obj=DistributionFitterBase.dataDistributionFromValues(values);
			return (DataDistributionImpl)obj[0];
		}
	}
}
