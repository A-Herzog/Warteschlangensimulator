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

import java.util.Locale;

import javax.swing.JComponent;

import mathtools.NumberTools;
import mathtools.Table;
import mathtools.distribution.swing.JDataDistributionEditPanel;
import mathtools.distribution.swing.JDataDistributionPanel;
import mathtools.distribution.swing.JDataLoader;
import mathtools.distribution.swing.JDistributionEditorPanel;
import mathtools.distribution.swing.JDistributionPanel;
import mathtools.distribution.tools.DistributionFitter;
import mathtools.distribution.tools.DistributionFitterBase;
import mathtools.distribution.tools.DistributionFitterMultiModal;
import mathtools.distribution.tools.DistributionTools;
import net.calc.NetServer;
import net.calc.SimulationClient;
import net.calc.SimulationServer;
import net.dde.DDEConnect;
import scripting.java.StatisticsImpl;
import scripting.js.JSCommandXML;
import simulator.db.DBSettings;
import simulator.simparser.ExpressionCalcModelUserFunctions;
import statistics.StatisticsCountPerformanceIndicator;
import statistics.StatisticsDataCollector;
import statistics.StatisticsDataPerformanceIndicator;
import statistics.StatisticsDataPerformanceIndicatorWithNegativeValues;
import statistics.StatisticsLongRunPerformanceIndicator;
import statistics.StatisticsMultiPerformanceIndicator;
import statistics.StatisticsQuotientPerformanceIndicator;
import statistics.StatisticsSimpleCountPerformanceIndicator;
import statistics.StatisticsSimpleValuePerformanceIndicator;
import statistics.StatisticsSimulationBaseData;
import statistics.StatisticsStateTimePerformanceIndicator;
import statistics.StatisticsTimeAnalogPerformanceIndicator;
import statistics.StatisticsTimePerformanceIndicator;
import statistics.StatisticsValuePerformanceIndicator;
import systemtools.BaseDialog;
import systemtools.GUITools;
import systemtools.JRegExWikipediaLinkLabel;
import systemtools.MsgBox;
import systemtools.SetupBase;
import systemtools.SmallColorChooser;
import systemtools.commandline.BaseCommandLineSystem;
import systemtools.commandline.CommandLineDialog;
import systemtools.help.HelpBase;
import systemtools.statistics.StatisticsBasePanel;
import tools.DateTools;
import tools.SetupData;
import ui.EditorPanelBase;
import ui.MainFrame;
import ui.MainPanel;
import ui.ModelChanger;
import ui.modeleditor.AnimationImageSource;
import ui.modeleditor.ModelAnimationImages;
import ui.modeleditor.ModelClientData;
import ui.modeleditor.ModelElementCatalog;
import ui.modeleditor.ModelLoadData;
import ui.modeleditor.ModelLongRunStatistics;
import ui.modeleditor.ModelLongRunStatisticsElement;
import ui.modeleditor.ModelPaths;
import ui.modeleditor.ModelResource;
import ui.modeleditor.ModelResourceFailure;
import ui.modeleditor.ModelResources;
import ui.modeleditor.ModelSchedule;
import ui.modeleditor.ModelSchedules;
import ui.modeleditor.ModelSequence;
import ui.modeleditor.ModelSequenceStep;
import ui.modeleditor.ModelSequences;
import ui.modeleditor.ModelSurface;
import ui.modeleditor.ModelTransporter;
import ui.modeleditor.ModelTransporterFailure;
import ui.modeleditor.ModelTransporters;
import ui.modeleditor.SavedViews;
import ui.modeleditor.elements.ComplexLine;
import ui.tools.SpecialPanel;
import xml.XMLData;
import xml.XMLTools;

/**
 * Setzt die statischen Spracheinstellungen
 * @author Alexander Herzog
 */
public class LanguageStaticLoader {
	/**
	 * Konstruktor der Klasse<br>
	 * Diese Klasse kann nicht instanziert werden. Sie stellt lediglich die statische Methode {@link #setLanguage()} zur Verf�gung.
	 */
	private LanguageStaticLoader() {}

	/**
	 * Gebietsvorgabe laut Betriebssystem<br>
	 * (Vorgabe wird beim Laden einer Sprache �berschrieben, daher wird die Systemvorgabe hier gespeichert)
	 */
	public static Locale OS_DEFAULT_LOCALE;

	static {
		OS_DEFAULT_LOCALE=Locale.getDefault();
	}

	/**
	 * Stellt die Sprache f�r Dialog, Verteilungs-Editoren usw. ein.
	 */
	public static void setLanguage() {
		Locale locale=Locale.US;
		if (Language.tr("Numbers.Language").equalsIgnoreCase("de")) locale=Locale.GERMANY;

		/* Zahlenformate */
		switch (SetupData.getSetup().numberFormat) {
		case BY_LANGUAGE:
			NumberTools.setLocale(locale);
			DateTools.setLocale(locale);
			break;
		case BY_SYSTEM:
			NumberTools.setLocale(OS_DEFAULT_LOCALE);
			DateTools.setLocale(OS_DEFAULT_LOCALE);
			break;
		case COMMA:
			NumberTools.setLocale(Locale.GERMAN);
			DateTools.setLocale(Locale.GERMAN);
			break;
		case POINT:
			NumberTools.setLocale(Locale.US);
			DateTools.setLocale(Locale.US);
			break;
		}

		/* Ja/Nein-Dialoge */
		JComponent.setDefaultLocale(locale);
		Locale.setDefault(locale);

		/* Message-Dialoge */
		MsgBox.TitleInformation=Language.tr("Dialog.Title.Information");
		MsgBox.TitleWarning=Language.tr("Dialog.Title.Warning");
		MsgBox.TitleError=Language.tr("Dialog.Title.Error");
		MsgBox.TitleConfirmation=Language.tr("Dialog.Title.Confirmation");
		MsgBox.TitleAlternatives=Language.tr("Dialog.Title.Alternatives");
		MsgBox.OverwriteTitle=Language.tr("Dialog.Overwrite.Title");
		MsgBox.OverwriteInfo=Language.tr("Dialog.Overwrite.Info");
		MsgBox.OverwriteYes=Language.tr("Dialog.Overwrite.Yes");
		MsgBox.OverwriteYesInfo=Language.tr("Dialog.Overwrite.Yes.Info");
		MsgBox.OverwriteNo=Language.tr("Dialog.Overwrite.No");
		MsgBox.OverwriteNoInfo=Language.tr("Dialog.Overwrite.No.Info");
		MsgBox.OptionYes=Language.tr("Dialog.Button.Yes");
		MsgBox.OptionNo=Language.tr("Dialog.Button.No");
		MsgBox.OptionCancel=Language.tr("Dialog.Button.Cancel");
		MsgBox.OptionSaveYes=Language.tr("Dialog.SaveNow.Yes");
		MsgBox.OptionSaveYesInfo=Language.tr("Dialog.SaveNow.Yes.Info");
		MsgBox.OptionSaveNo=Language.tr("Dialog.SaveNow.No");
		MsgBox.OptionSaveNoInfo=Language.tr("Dialog.SaveNow.No.Info");
		MsgBox.OptionSaveCancelInfo=Language.tr("Dialog.SaveNow.Cancel.Info");
		MsgBox.OpenURLInfo=Language.tr("Dialog.OpenURL.Info");
		MsgBox.OpenURLInfoYes=Language.tr("Dialog.OpenURL.InfoYes");
		MsgBox.OpenURLInfoNo=Language.tr("Dialog.OpenURL.InfoNo");
		MsgBox.OptionCopyURL=Language.tr("Dialog.OpenURL.CopyURL");
		MsgBox.OptionInfoCopyURL=Language.tr("Dialog.OpenURL.CopyURLInfo");
		MsgBox.OpenURLErrorTitle=Language.tr("Window.Info.NoInternetConnection");
		MsgBox.OpenURLErrorMessage=Language.tr("Window.Info.NoInternetConnection.Address");
		MsgBox.ActiveLocale=locale;

		/* Sucheinstellungen */
		JRegExWikipediaLinkLabel.title=Language.tr("RegExWikipdiaLink.Title");
		JRegExWikipediaLinkLabel.tooltip=Language.tr("RegExWikipdiaLink.Tooltip");
		JRegExWikipediaLinkLabel.url=Language.tr("RegExWikipdiaLink.URL");

		/* Verteilungen */
		DistributionTools.DistData=Language.trAll("Distribution.Data");
		DistributionTools.DistDataWikipedia=Language.tr("Distribution.DataWikipedia");
		DistributionTools.DistNever=Language.trAll("Distribution.Never");
		DistributionTools.DistInfinite=Language.trAll("Distribution.Infinite");
		DistributionTools.DistPoint=Language.trAll("Distribution.Point");
		DistributionTools.DistPointInfo=Language.tr("Distribution.PointInfo");
		DistributionTools.DistUniform=Language.trAll("Distribution.Uniform");
		DistributionTools.DistUniformWikipedia=Language.tr("Distribution.UniformWikipedia");
		DistributionTools.DistUniformInfo=Language.tr("Distribution.UniformInfo");
		DistributionTools.DistExp=Language.trAll("Distribution.Exp");
		DistributionTools.DistExpWikipedia=Language.tr("Distribution.ExpWikipedia");
		DistributionTools.DistExpInfo=Language.tr("Distribution.ExpInfo");
		DistributionTools.DistNormal=Language.trAll("Distribution.Normal");
		DistributionTools.DistNormalWikipedia=Language.tr("Distribution.NormalWikipedia");
		DistributionTools.DistNormalInfo=Language.tr("Distribution.NormalInfo");
		DistributionTools.DistLogNormal=Language.trAll("Distribution.LogNormal");
		DistributionTools.DistLogNormalWikipedia=Language.tr("Distribution.LogNormalWikipedia");
		DistributionTools.DistLogNormalInfo=Language.tr("Distribution.LogNormalInfo");
		DistributionTools.DistErlang=Language.trAll("Distribution.Erlang");
		DistributionTools.DistErlangWikipedia=Language.tr("Distribution.ErlangWikipedia");
		DistributionTools.DistErlangInfo=Language.tr("Distribution.ErlangInfo");
		DistributionTools.DistGamma=Language.trAll("Distribution.Gamma");
		DistributionTools.DistGammaWikipedia=Language.tr("Distribution.GammaWikipedia");
		DistributionTools.DistGammaInfo=Language.tr("Distribution.GammaInfo");
		DistributionTools.DistInverseGamma=Language.trAll("Distribution.InverseGamma");
		DistributionTools.DistInverseGammaWikipedia=Language.tr("Distribution.InverseGammaWikipedia");
		DistributionTools.DistBeta=Language.trAll("Distribution.Beta");
		DistributionTools.DistBetaWikipedia=Language.tr("Distribution.BetaWikipedia");
		DistributionTools.DistCauchy=Language.trAll("Distribution.Cauchy");
		DistributionTools.DistCauchyWikipedia=Language.tr("Distribution.CauchyWikipedia");
		DistributionTools.DistHalfCauchy=Language.trAll("Distribution.HalfCauchy");
		DistributionTools.DistLogCauchy=Language.trAll("Distribution.LogCauchy");
		DistributionTools.DistLogCauchyWikipedia=Language.tr("Distribution.LogCauchyWikipedia");
		DistributionTools.DistWeibull=Language.trAll("Distribution.Weibull");
		DistributionTools.DistWeibullWikipedia=Language.tr("Distribution.WeibullWikipedia");
		DistributionTools.DistChi=Language.trAll("Distribution.Chi");
		DistributionTools.DistChiWikipedia=Language.tr("Distribution.ChiWikipedia");
		DistributionTools.DistChiSquare=Language.trAll("Distribution.ChiSquare");
		DistributionTools.DistChiSquareWikipedia=Language.tr("Distribution.ChiSquareWikipedia");
		DistributionTools.DistF=Language.trAll("Distribution.F");
		DistributionTools.DistFWikipedia=Language.tr("Distribution.FWikipedia");
		DistributionTools.DistJohnson=Language.trAll("Distribution.DistJohnsonSU");
		DistributionTools.DistJohnsonWikipedia=Language.tr("Distribution.DistJohnsonSUWikipedia");
		DistributionTools.DistTriangular=Language.trAll("Distribution.Triangular");
		DistributionTools.DistTriangularWikipedia=Language.tr("Distribution.TriangularWikipedia");
		DistributionTools.DistTriangularInfo=Language.tr("Distribution.TriangularInfo");
		DistributionTools.DistTrapezoid=Language.trAll("Distribution.Trapezoid");
		DistributionTools.DistTrapezoidWikipedia=Language.tr("Distribution.TrapezoidWikipedia");
		DistributionTools.DistPert=Language.trAll("Distribution.Pert");
		DistributionTools.DistPertWikipedia=Language.tr("Distribution.PertWikipedia");
		DistributionTools.DistLaplace=Language.trAll("Distribution.Laplace");
		DistributionTools.DistLaplaceWikipedia=Language.tr("Distribution.LaplaceWikipedia");
		DistributionTools.DistPareto=Language.trAll("Distribution.Pareto");
		DistributionTools.DistParetoWikipedia=Language.tr("Distribution.ParetoWikipedia");
		DistributionTools.DistLogistic=Language.trAll("Distribution.Logistic");
		DistributionTools.DistLogisticWikipedia=Language.tr("Distribution.LogisticWikipedia");
		DistributionTools.DistInverseGaussian=Language.trAll("Distribution.InverseGaussian");
		DistributionTools.DistInverseGaussianWikipedia=Language.tr("Distribution.InverseGaussianWikipedia");
		DistributionTools.DistRayleigh=Language.trAll("Distribution.Rayleigh");
		DistributionTools.DistRayleighWikipedia=Language.tr("Distribution.RayleighWikipedia");
		DistributionTools.DistLogLogistic=Language.trAll("Distribution.LogLogistic");
		DistributionTools.DistLogLogisticWikipedia=Language.tr("Distribution.LogLogisticWikipedia");
		DistributionTools.DistPower=Language.trAll("Distribution.Power");
		DistributionTools.DistPowerWikipedia=Language.tr("Distribution.PowerWikipedia");
		DistributionTools.DistGumbel=Language.trAll("Distribution.Gumbel");
		DistributionTools.DistGumbelWikipedia=Language.tr("Distribution.GumbelWikipedia");
		DistributionTools.DistFatigueLife=Language.trAll("Distribution.FatigueLife");
		DistributionTools.DistFatigueLifeWikipedia=Language.tr("Distribution.FatigueLifeWikipedia");
		DistributionTools.DistFrechet=Language.trAll("Distribution.Frechet");
		DistributionTools.DistFrechetWikipedia=Language.tr("Distribution.FrechetWikipedia");
		DistributionTools.DistHyperbolicSecant=Language.trAll("Distribution.HyperbolicSecant");
		DistributionTools.DistHyperbolicSecantWikipedia=Language.tr("Distribution.HyperbolicSecantWikipedia");
		DistributionTools.DistSawtoothLeft=Language.trAll("Distribution.SawtoothLeft");
		DistributionTools.DistSawtoothLeftWikipedia=Language.tr("Distribution.SawtoothLeftWikipedia");
		DistributionTools.DistSawtoothRight=Language.trAll("Distribution.SawtoothRight");
		DistributionTools.DistSawtoothRightWikipedia=Language.tr("Distribution.SawtoothRightWikipedia");
		DistributionTools.DistLevy=Language.trAll("Distribution.Levy");
		DistributionTools.DistLevyWikipedia=Language.tr("Distribution.LevyWikipedia");
		DistributionTools.DistMaxwellBoltzmann=Language.trAll("Distribution.MaxwellBoltzmann");
		DistributionTools.DistMaxwellBoltzmannWikipedia=Language.tr("Distribution.MaxwellBoltzmannWikipedia");
		DistributionTools.DistStudentT=Language.trAll("Distribution.StudentT");
		DistributionTools.DistStudentTWikipedia=Language.tr("Distribution.StudentTWikipedia");
		DistributionTools.DistHyperGeom=Language.trAll("Distribution.HyperGeom");
		DistributionTools.DistHyperGeomWikipedia=Language.tr("Distribution.HyperGeomWikipedia");
		DistributionTools.DistBinomial=Language.trAll("Distribution.Binomial");
		DistributionTools.DistBinomialWikipedia=Language.tr("Distribution.BinomialWikipedia");
		DistributionTools.DistPoisson=Language.trAll("Distribution.Poisson");
		DistributionTools.DistPoissonWikipedia=Language.tr("Distribution.PoissonWikipedia");
		DistributionTools.DistPlanck=Language.trAll("Distribution.Planck");
		DistributionTools.DistNegativeBinomial=Language.trAll("Distribution.NegativeBinomial");
		DistributionTools.DistNegativeBinomialWikipedia=Language.tr("Distribution.NegativeBinomialWikipedia");
		DistributionTools.DistNegativeHyperGeom=Language.trAll("Distribution.NegativeHyperGeom");
		DistributionTools.DistNegativeHyperGeomWikipedia=Language.tr("Distribution.NegativeHyperGeomWikipedia");
		DistributionTools.DistZeta=Language.trAll("Distribution.Zeta");
		DistributionTools.DistZetaWikipedia=Language.tr("Distribution.ZetaWikipedia");
		DistributionTools.DistDiscreteUniform=Language.trAll("Distribution.DiscreteUniform");
		DistributionTools.DistDiscreteUniformWikipedia=Language.tr("Distribution.DiscreteUniformWikipedia");
		DistributionTools.DistGeometric=Language.trAll("Distribution.Geometric");
		DistributionTools.DistGeometricWikipedia=Language.tr("Distribution.GeometricWikipedia");
		DistributionTools.DistLogarithmic=Language.trAll("Distribution.Logarithmic");
		DistributionTools.DistLogarithmicWikipedia=Language.tr("Distribution.LogarithmicWikipedia");
		DistributionTools.DistBorel=Language.trAll("Distribution.Borel");
		DistributionTools.DistBorelWikipedia=Language.tr("Distribution.BorelWikipedia");
		DistributionTools.DistBoltzmann=Language.trAll("Distribution.Boltzmann");
		DistributionTools.DistHalfNormal=Language.trAll("Distribution.HalfNormal");
		DistributionTools.DistHalfNormalWikipedia=Language.tr("Distribution.HalfNormalWikipedia");
		DistributionTools.DistUQuadratic=Language.trAll("Distribution.UQuadratic");
		DistributionTools.DistUQuadraticWikipedia=Language.tr("Distribution.UQuadraticWikipedia");
		DistributionTools.DistReciprocal=Language.trAll("Distribution.Reciprocal");
		DistributionTools.DistReciprocalWikipedia=Language.tr("Distribution.ReciprocalWikipedia");
		DistributionTools.DistKumaraswamy=Language.trAll("Distribution.Kumaraswamy");
		DistributionTools.DistKumaraswamyWikipedia=Language.tr("Distribution.KumaraswamyWikipedia");
		DistributionTools.DistIrwinHall=Language.trAll("Distribution.IrwinHall");
		DistributionTools.DistIrwinHallWikipedia=Language.tr("Distribution.IrwinHallWikipedia");
		DistributionTools.DistSine=Language.trAll("Distribution.Sine");
		DistributionTools.DistCosine=Language.trAll("Distribution.Cosine");
		DistributionTools.DistCosineWikipedia=Language.tr("Distribution.CosineWikipedia");
		DistributionTools.DistArcsine=Language.trAll("Distribution.Arcsine");
		DistributionTools.DistWignerHalfCircle=Language.trAll("Distribution.WignerHalfCircle");
		DistributionTools.DistWignerHalfCircleWikipedia=Language.tr("Distribution.WignerHalfCircleWikipedia");
		DistributionTools.DistLogGamma=Language.trAll("Distribution.LogGamma");
		DistributionTools.DistLogGammaWikipedia=Language.tr("Distribution.LogGammaWikipedia");
		DistributionTools.DistLogLaplace=Language.trAll("Distribution.LogLaplace");
		DistributionTools.DistLogLaplaceWikipedia=Language.tr("Distribution.LogLaplaceWikipedia");
		DistributionTools.DistContinuousBernoulli=Language.trAll("Distribution.ContinuousBernoulli");
		DistributionTools.DistContinuousBernoulliWikipedia=Language.tr("Distribution.ContinuousBernoulliWikipedia");
		DistributionTools.DistGeneralizedRademacher=Language.trAll("Distribution.GeneralizedRademacher");
		DistributionTools.DistGeneralizedRademacherWikipedia=Language.tr("Distribution.GeneralizedRademacherWikipedia");
		DistributionTools.DistUnknown=Language.tr("Distribution.Unknown");
		DistributionTools.DistDataPoint=Language.tr("Distribution.DataPoint");
		DistributionTools.DistDataPoints=Language.tr("Distribution.DataPoints");
		DistributionTools.DistRange=Language.tr("Distribution.Range");
		DistributionTools.DistLocation=Language.tr("Distribution.Location");
		DistributionTools.DistScale=Language.tr("Distribution.Scale");
		DistributionTools.DistInverseScale=Language.tr("Distribution.InverseScale");
		DistributionTools.DistMostLikely=Language.tr("Distribution.MostLikely");
		DistributionTools.DistDegreesOfFreedom=Language.tr("Distribution.DegreesOfFreedom");
		DistributionTools.DistMean=Language.tr("Distribution.Mean");
		DistributionTools.DistMeanWikipedia=Language.tr("Distribution.Mean.Wikipedia");
		DistributionTools.DistStdDev=Language.tr("Distribution.StdDev");
		DistributionTools.DistStdDevWikipedia=Language.tr("Distribution.StdDev.Wikipedia");
		DistributionTools.DistCV=Language.tr("Distribution.CV");
		DistributionTools.DistCVWikipedia=Language.tr("Distribution.CV.Wikipedia");
		DistributionTools.DistSkewness=Language.tr("Distribution.Skewness");
		DistributionTools.DistSkewnessWikipedia=Language.tr("Distribution.Skewness.Wikipedia");
		DistributionTools.DistMode=Language.tr("Distribution.Mode");
		DistributionTools.DistModeWikipedia=Language.tr("Distribution.Mode.Wikipedia");
		DistributionTools.DistParameter=Language.tr("Distribution.Parameter");
		JDataDistributionPanel.errorString=Language.tr("Distribution.NoDistribution");

		/* DistributionFitterBase */
		DistributionFitterBase.ErrorInvalidFormat=Language.tr("DistributionFitter.ErrorInvalidFormat");
		DistributionFitterBase.ValueCount=Language.tr("DistributionFitter.ValueCount");
		DistributionFitterBase.ValueRange=Language.tr("DistributionFitter.ValueRange");
		DistributionFitterBase.Mean=Language.tr("DistributionFitter.Mean");
		DistributionFitterBase.StdDev=Language.tr("DistributionFitter.StdDev");

		/* DistributionFitter */
		DistributionFitter.ComparedDistributions=Language.tr("DistributionFitter.ComparedDistributions");
		DistributionFitter.MeanSquares=Language.tr("DistributionFitter.MeanSquares");
		DistributionFitter.PValue=Language.tr("DistributionFitter.PValue");
		DistributionFitter.PValueChiSqr=Language.tr("DistributionFitter.PValueChiSqr");
		DistributionFitter.PValueAndersonDarling=Language.tr("DistributionFitter.PValueAndersonDarling");
		DistributionFitter.BestFitFor=Language.tr("DistributionFitter.BestFitFor");
		DistributionFitter.FitError=Language.tr("DistributionFitter.FitError");
		DistributionFitter.NotFit=Language.tr("DistributionFitter.NotFit");

		/* DistributionFitterMultiModal */
		DistributionFitterMultiModal.usedDistribution=Language.tr("DistributionFitterMultiModal.UsedDistribution");
		DistributionFitterMultiModal.step=Language.tr("DistributionFitterMultiModal.Step");
		DistributionFitterMultiModal.mode=Language.tr("DistributionFitterMultiModal.Mode");
		DistributionFitterMultiModal.noMode=Language.tr("DistributionFitterMultiModal.NoMode");
		DistributionFitterMultiModal.approximation=Language.tr("DistributionFitterMultiModal.Approximation");
		DistributionFitterMultiModal.fraction=Language.tr("DistributionFitterMultiModal.Fraction");
		DistributionFitterMultiModal.fractionsPostOptimization=Language.tr("DistributionFitterMultiModal.FractionPostOptimization");
		DistributionFitterMultiModal.fractionsPostOptimizationCurrent=Language.tr("DistributionFitterMultiModal.FractionPostOptimizationCurrent");
		DistributionFitterMultiModal.fractionsPostOptimizationNew=Language.tr("DistributionFitterMultiModal.FractionPostOptimizationNew");
		DistributionFitterMultiModal.calculationCommand=Language.tr("DistributionFitterMultiModal.CalculationCommand");

		/* Table */
		Table.BoolTrue=Language.tr("Table.BoolTrue");
		Table.BoolFalse=Language.tr("Table.BoolFalse");
		Table.TableFileTableName=Language.tr("Table.TableFileTableName");
		Table.FileTypeAll=Language.tr("FileType.AllTables");
		Table.FileTypeText=Language.tr("FileType.Text");
		Table.FileTypeCSV=Language.tr("FileType.CSV");
		Table.FileTypeCSVR=Language.tr("FileType.CSVR");
		Table.FileTypeDIF=Language.tr("FileType.DIF");
		Table.FileTypeSYLK=Language.tr("FileType.SYLK");
		Table.FileTypeDBF=Language.tr("FileType.DBF");
		Table.FileTypeExcelOld=Language.tr("FileType.ExcelOld");
		Table.FileTypeExcel=Language.tr("FileType.Excel");
		Table.FileTypeODS=Language.tr("FileType.FileTypeODS");
		Table.FileTypeSQLite=Language.tr("FileType.SQLite");
		Table.FileTypeWord=Language.tr("FileType.WordTable");
		Table.FileTypeHTML=Language.tr("FileType.HTMLTable");
		Table.FileTypeTex=Language.tr("FileType.LaTeXTable");
		Table.FileTypeTypst=Language.tr("FileType.TypstTable");

		Table.LoadErrorFirstCellInvalid=Language.tr("Table.LoadErrorFirstCellInvalid");
		Table.LoadErrorLastCellInvalid=Language.tr("Table.LoadErrorLastCellInvalid");
		Table.LoadErrorCellRangeInvalid=Language.tr("Table.LoadErrorCellRangeInvalid");
		Table.LoadErrorCellNotInTable=Language.tr("Table.LoadErrorCellNotInTable");
		Table.LoadErrorCellValueNaN=Language.tr("Table.LoadErrorCellValueNaN");

		/* JDataLoader */
		JDataLoader.Title=Language.tr("JDataLoader.Title");
		JDataLoader.Sheet=Language.tr("JDataLoader.Sheet");
		JDataLoader.SelectArea=Language.tr("JDataLoader.SelectArea");
		JDataLoader.ButtonOk=Language.tr("Dialog.Button.Ok");
		JDataLoader.ButtonCancel=Language.tr("Dialog.Button.Cancel");
		JDataLoader.ImportErrorTitle=Language.tr("Dialog.Title.Error");
		JDataLoader.ImportErrorNoArea=Language.tr("JDataLoader.ImportErrorNoArea");
		JDataLoader.ImportErrorTooFewCells=Language.tr("JDataLoader.ImportErrorTooFewCells");
		JDataLoader.ImportErrorTooManyCells=Language.tr("JDataLoader.ImportErrorTooManyCells");
		JDataLoader.ImportErrorInvalidValue=Language.tr("JDataLoader.ImportErrorInvalidValue");
		JDataLoader.ImportErrorInvalidData=Language.tr("JDataLoader.ImportErrorInvalidData");
		JDataLoader.ImportErrorFileError=Language.tr("JDataLoader.ImportErrorFileError");

		/* JDistributionEditorPanel */
		JDistributionEditorPanel.DialogTitle=Language.tr("JDistributionEditor.Title");
		JDistributionEditorPanel.ButtonOk=Language.tr("Dialog.Button.Ok");
		JDistributionEditorPanel.ButtonCancel=Language.tr("Dialog.Button.Cancel");
		JDistributionEditorPanel.ButtonCopyData=Language.tr("Dialog.Button.Copy");
		JDistributionEditorPanel.ButtonPasteData=Language.tr("Dialog.Button.Paste");
		JDistributionEditorPanel.ButtonPasteAndFillData=Language.tr("Dialog.Button.PasteDoNotScale");
		JDistributionEditorPanel.ButtonPasteAndFillDataTooltip=Language.tr("Dialog.Button.PasteDoNotScale.Tooltip");
		JDistributionEditorPanel.ButtonLoadData=Language.tr("JDistributionEditor.Load");
		JDistributionEditorPanel.ButtonLoadDataDialogTitle=Language.tr("JDistributionEditor.Load.Title");
		JDistributionEditorPanel.ButtonSaveData=Language.tr("JDistributionEditor.Save");
		JDistributionEditorPanel.ButtonSaveDataDialogTitle=Language.tr("JDistributionEditor.Save.Title");
		JDistributionEditorPanel.DistData=Language.tr("JDistributionEditor.DataVector");
		JDistributionEditorPanel.DistMean=Language.tr("Distribution.E");
		JDistributionEditorPanel.DistStdDev=Language.tr("Distribution.StdDev");
		JDistributionEditorPanel.DistUniformStart=Language.tr("Distribution.Uniform.Start");
		JDistributionEditorPanel.DistUniformEnd=Language.tr("Distribution.Uniform.End");
		JDistributionEditorPanel.DistDegreesOfFreedom=Language.tr("Distribution.DegreesOfFreedom");
		JDistributionEditorPanel.DistDegreesOfFreedomNumerator=Language.tr("Distribution.DegreesOfFreedom.Numerator");
		JDistributionEditorPanel.DistDegreesOfFreedomDenominator=Language.tr("Distribution.DegreesOfFreedom.Denominator");
		JDistributionEditorPanel.DistRadius=Language.tr("Distribution.Radius");
		JDistributionEditorPanel.ChangeValueDown=Language.tr("JDistributionEditor.ValueDown");
		JDistributionEditorPanel.ChangeValueUp=Language.tr("JDistributionEditor.ValueUp");
		JDistributionEditorPanel.DistMostLikely=Language.tr("Distribution.MostLikely");
		JDistributionEditorPanel.SetupListTitle=Language.tr("Distribution.SetupList.Title");
		JDistributionEditorPanel.SetupListInfo=Language.tr("Distribution.SetupList.Info");
		JDistributionEditorPanel.SetupListDivier=Language.tr("Distribution.SetupList.Divider");
		JDistributionEditorPanel.SetupListInfoSingular=Language.tr("Distribution.SetupList.Info.Singular");
		JDistributionEditorPanel.SetupListInfoPlural=Language.tr("Distribution.SetupList.Info.Plural");

		/* JDataDistributionEditPanel */
		JDataDistributionEditPanel.ButtonCopy=Language.tr("Dialog.Button.Copy");
		JDataDistributionEditPanel.ButtonCopyTooltip=Language.tr("JDistributionEditor.Copy.Info");
		JDataDistributionEditPanel.ButtonCopyTable=Language.tr("JDistributionEditor.Copy.Table");
		JDataDistributionEditPanel.ButtonCopyGraphics=Language.tr("JDistributionEditor.Copy.Graphics");
		JDataDistributionEditPanel.ButtonPaste=Language.tr("Dialog.Button.Paste");
		JDataDistributionEditPanel.ButtonPasteTooltip=Language.tr("JDistributionEditor.Paste.Info");
		JDataDistributionEditPanel.ButtonLoad=Language.tr("JDistributionEditor.Load");
		JDataDistributionEditPanel.ButtonLoadTooltip=Language.tr("JDistributionEditor.Load.Info");
		JDataDistributionEditPanel.ButtonLoadDialogTitle=Language.tr("JDistributionEditor.Load.Title");
		JDataDistributionEditPanel.ButtonSave=Language.tr("JDistributionEditor.Save");
		JDataDistributionEditPanel.ButtonSaveTooltip=Language.tr("JDistributionEditor.Save.Info");
		JDataDistributionEditPanel.ButtonSaveDialogTitle=Language.tr("JDistributionEditor.Save.Title");
		JDataDistributionEditPanel.LoadError=Language.tr("JDistributionEditor.Load.Error");
		JDataDistributionEditPanel.LoadErrorTitle=Language.tr("Dialog.Title.Error");
		JDataDistributionEditPanel.SaveOverwriteWarning=Language.tr("Dialog.Overwrite.Info");
		JDataDistributionEditPanel.SaveOverwriteWarningTitle=Language.tr("Dialog.Title.Warning");
		JDataDistributionEditPanel.CountDensityLabel=Language.tr("JDistributionEditor.Density.Label");
		JDataDistributionEditPanel.CumulativeProbabilityLabel=Language.tr("JDistributionEditor.CumulativeProbability.Label");

		/* JDistributionPanel */
		JDistributionPanel.ErrorString=Language.tr("JDistributionEditor.NoDistribution");
		JDistributionPanel.EditButtonLabel=Language.tr("Dialog.Button.Edit");
		JDistributionPanel.EditButtonTooltip=Language.tr("JDistributionEditor.Edit.Info");
		JDistributionPanel.EditButtonLabelDisabled=Language.tr("JDistributionEditor.Edit.Disabled");
		JDistributionPanel.CopyButtonLabel=Language.tr("Dialog.Button.Copy");
		JDistributionPanel.CopyButtonTable=Language.tr("Dialog.Button.Copy.Table");
		JDistributionPanel.CopyButtonRandomNumbers=Language.tr("Dialog.Button.Copy.RandomNumbers");
		JDistributionPanel.CopyButtonImage=Language.tr("Dialog.Button.Copy.Image");
		JDistributionPanel.SaveButtonLabel=Language.tr("Dialog.Button.Save");
		JDistributionPanel.SaveButtonTable=Language.tr("Dialog.Button.Save.Table");
		JDistributionPanel.SaveButtonRandomNumbers=Language.tr("Dialog.Button.Save.RandomNumbers");
		JDistributionPanel.SaveButtonImage=Language.tr("Dialog.Button.Save.Image");
		JDistributionPanel.InfoButtonLabel=Language.tr("JDistributionEditor.Info");
		JDistributionPanel.InfoButtonTooltip=Language.tr("JDistributionEditor.Info.Tooltip");
		JDistributionPanel.InfoWindowParameters=Language.tr("JDistributionEditor.InfoWindow.Parameters");
		JDistributionPanel.InfoWindowMore=Language.tr("JDistributionEditor.InfoWindow.MoreInfo");
		JDistributionPanel.WebAppButtonLabel=Language.tr("JDistributionEditor.InfoWindow.WebApp");
		JDistributionPanel.WikiButtonLabel=Language.tr("JDistributionEditor.Wikipedia");
		JDistributionPanel.WikiButtonTooltip=Language.tr("JDistributionEditor.Wikipedia.Info");
		JDistributionPanel.ChangeDistributionType=Language.tr("JDistributionEditor.QuickSelect");
		JDistributionPanel.ChangeDistributionTypeHighlightList=Language.tr("JDistributionEditor.QuickSelectInfo");
		JDistributionPanel.DensityLabel=Language.tr("JDistributionEditor.Density.Label");
		JDistributionPanel.CountDensityLabel=Language.tr("JDistributionEditor.Density.Label");
		JDistributionPanel.CumulativeProbabilityLabel=Language.tr("JDistributionEditor.CumulativeProbability.Label");
		JDistributionPanel.StoreGraphicsDialogTitle=Language.tr("JDistributionEditor.Save.Graphics");
		JDistributionPanel.FileTypeJPEG=Language.tr("FileType.jpeg");
		JDistributionPanel.FileTypeGIF=Language.tr("FileType.gif");
		JDistributionPanel.FileTypePNG=Language.tr("FileType.png");
		JDistributionPanel.FileTypeBMP=Language.tr("FileType.bmp");
		JDistributionPanel.FileTypeTIFF=Language.tr("FileType.tiff");
		JDistributionPanel.GraphicsFileOverwriteWarning=Language.tr("Dialog.Overwrite.Info");
		JDistributionPanel.GraphicsFileOverwriteWarningTitle=Language.tr("Dialog.Title.Warning");
		JDistributionPanel.GraphicsOpenURLWarning=Language.tr("Dialog.OpenURL.Info");
		JDistributionPanel.GraphicsOpenURLWarningTitle=Language.tr("Dialog.Title.Warning");
		JDistributionPanel.RandomNumbersCount=Language.tr("Dialog.RandomNumbers.Count");
		JDistributionPanel.RandomNumbersError=Language.tr("Dialog.RandomNumbers.Error");
		JDistributionPanel.ToCalculationExpression=Language.tr("Dialog.ConvertDistributionToExpression");
		JDistributionPanel.Generator=Language.tr("Dialog.RandomNumbers.Generator");

		/* GUITools */
		GUITools.errorNoGraphicsOutputAvailable=Language.tr("Window.ErrorNoGraphics");

		/* SmallColorChooser */
		SmallColorChooser.ColorNameF0F8FF=Language.tr("Color.F0F8FF");
		SmallColorChooser.ColorNameFAEBD7=Language.tr("Color.FAEBD7");
		SmallColorChooser.ColorName7FFFD4=Language.tr("Color.7FFFD4");
		SmallColorChooser.ColorNameF0FFFF=Language.tr("Color.F0FFFF");
		SmallColorChooser.ColorNameF5F5DC=Language.tr("Color.F5F5DC");
		SmallColorChooser.ColorNameFFE4C4=Language.tr("Color.FFE4C4");
		SmallColorChooser.ColorName000000=Language.tr("Color.000000");
		SmallColorChooser.ColorNameFFEBCD=Language.tr("Color.FFEBCD");
		SmallColorChooser.ColorName0000FF=Language.tr("Color.0000FF");
		SmallColorChooser.ColorName8A2BE2=Language.tr("Color.8A2BE2");
		SmallColorChooser.ColorNameA52A2A=Language.tr("Color.A52A2A");
		SmallColorChooser.ColorNameDEB887=Language.tr("Color.DEB887");
		SmallColorChooser.ColorName5F9EA0=Language.tr("Color.5F9EA0");
		SmallColorChooser.ColorName7FFF00=Language.tr("Color.7FFF00");
		SmallColorChooser.ColorNameD2691E=Language.tr("Color.D2691E");
		SmallColorChooser.ColorNameFF7F50=Language.tr("Color.FF7F50");
		SmallColorChooser.ColorName6495ED=Language.tr("Color.6495ED");
		SmallColorChooser.ColorNameFFF8DC=Language.tr("Color.FFF8DC");
		SmallColorChooser.ColorNameDC143C=Language.tr("Color.DC143C");
		SmallColorChooser.ColorName00FFFF=Language.tr("Color.00FFFF");
		SmallColorChooser.ColorName00008B=Language.tr("Color.00008B");
		SmallColorChooser.ColorName008B8B=Language.tr("Color.008B8B");
		SmallColorChooser.ColorNameB8860B=Language.tr("Color.B8860B");
		SmallColorChooser.ColorNameA9A9A9=Language.tr("Color.A9A9A9");
		SmallColorChooser.ColorName006400=Language.tr("Color.006400");
		SmallColorChooser.ColorNameBDB76B=Language.tr("Color.BDB76B");
		SmallColorChooser.ColorName8B008B=Language.tr("Color.8B008B");
		SmallColorChooser.ColorName556B2F=Language.tr("Color.8B008B");
		SmallColorChooser.ColorNameFF8C00=Language.tr("Color.FF8C00");
		SmallColorChooser.ColorName9932CC=Language.tr("Color.9932CC");
		SmallColorChooser.ColorName8B0000=Language.tr("Color.8B0000");
		SmallColorChooser.ColorNameE9967A=Language.tr("Color.E9967A");
		SmallColorChooser.ColorName8FBC8F=Language.tr("Color.8FBC8F");
		SmallColorChooser.ColorName483D8B=Language.tr("Color.483D8B");
		SmallColorChooser.ColorName2F4F4F=Language.tr("Color.2F4F4F");
		SmallColorChooser.ColorName00CED1=Language.tr("Color.00CED1");
		SmallColorChooser.ColorName9400D3=Language.tr("Color.9400D3");
		SmallColorChooser.ColorNameFF1493=Language.tr("Color.FF1493");
		SmallColorChooser.ColorName00BFFF=Language.tr("Color.00BFFF");
		SmallColorChooser.ColorName696969=Language.tr("Color.696969");
		SmallColorChooser.ColorName1E90FF=Language.tr("Color.1E90FF");
		SmallColorChooser.ColorNameB22222=Language.tr("Color.B22222");
		SmallColorChooser.ColorNameFFFAF0=Language.tr("Color.FFFAF0");
		SmallColorChooser.ColorName228B22=Language.tr("Color.228B22");
		SmallColorChooser.ColorNameDCDCDC=Language.tr("Color.DCDCDC");
		SmallColorChooser.ColorNameF8F8FF=Language.tr("Color.F8F8FF");
		SmallColorChooser.ColorNameFFD700=Language.tr("Color.FFD700");
		SmallColorChooser.ColorNameDAA520=Language.tr("Color.DAA520");
		SmallColorChooser.ColorName808080=Language.tr("Color.808080");
		SmallColorChooser.ColorName008000=Language.tr("Color.008000");
		SmallColorChooser.ColorNameADFF2F=Language.tr("Color.ADFF2F");
		SmallColorChooser.ColorNameF0FFF0=Language.tr("Color.F0FFF0");
		SmallColorChooser.ColorNameFF69B4=Language.tr("Color.FF69B4");
		SmallColorChooser.ColorNameCD5C5C=Language.tr("Color.CD5C5C");
		SmallColorChooser.ColorName4B0082=Language.tr("Color.4B0082");
		SmallColorChooser.ColorNameFFFFF0=Language.tr("Color.FFFFF0");
		SmallColorChooser.ColorNameF0E68C=Language.tr("Color.F0E68C");
		SmallColorChooser.ColorNameE6E6FA=Language.tr("Color.E6E6FA");
		SmallColorChooser.ColorNameFFF0F5=Language.tr("Color.FFF0F5");
		SmallColorChooser.ColorName7CFC00=Language.tr("Color.7CFC00");
		SmallColorChooser.ColorNameFFFACD=Language.tr("Color.FFFACD");
		SmallColorChooser.ColorNameADD8E6=Language.tr("Color.ADD8E6");
		SmallColorChooser.ColorNameF08080=Language.tr("Color.F08080");
		SmallColorChooser.ColorNameE0FFFF=Language.tr("Color.E0FFFF");
		SmallColorChooser.ColorNameFAFAD2=Language.tr("Color.FAFAD2");
		SmallColorChooser.ColorNameD3D3D3=Language.tr("Color.D3D3D3");
		SmallColorChooser.ColorName90EE90=Language.tr("Color.90EE90");
		SmallColorChooser.ColorNameFFB6C1=Language.tr("Color.FFB6C1");
		SmallColorChooser.ColorNameFFA07A=Language.tr("Color.FFA07A");
		SmallColorChooser.ColorName20B2AA=Language.tr("Color.20B2AA");
		SmallColorChooser.ColorName778899=Language.tr("Color.778899");
		SmallColorChooser.ColorNameB0C4DE=Language.tr("Color.B0C4DE");
		SmallColorChooser.ColorNameFFFFE0=Language.tr("Color.FFFFE0");
		SmallColorChooser.ColorName00FF00=Language.tr("Color.00FF00");
		SmallColorChooser.ColorName32CD32=Language.tr("Color.32CD32");
		SmallColorChooser.ColorNameFAF0E6=Language.tr("Color.FAF0E6");
		SmallColorChooser.ColorNameFF00FF=Language.tr("Color.FF00FF");
		SmallColorChooser.ColorName800000=Language.tr("Color.FF00FF");
		SmallColorChooser.ColorName66CDAA=Language.tr("Color.66CDAA");
		SmallColorChooser.ColorName0000CD=Language.tr("Color.0000CD");
		SmallColorChooser.ColorNameBA55D3=Language.tr("Color.BA55D3");
		SmallColorChooser.ColorName9370DB=Language.tr("Color.9370DB");
		SmallColorChooser.ColorName3CB371=Language.tr("Color.3CB371");
		SmallColorChooser.ColorName7B68EE=Language.tr("Color.7B68EE");
		SmallColorChooser.ColorName00FA9A=Language.tr("Color.00FA9A");
		SmallColorChooser.ColorName48D1CC=Language.tr("Color.48D1CC");
		SmallColorChooser.ColorNameC71585=Language.tr("Color.C71585");
		SmallColorChooser.ColorName191970=Language.tr("Color.191970");
		SmallColorChooser.ColorNameF5FFFA=Language.tr("Color.F5FFFA");
		SmallColorChooser.ColorNameFFE4E1=Language.tr("Color.FFE4E1");
		SmallColorChooser.ColorNameFFE4B5=Language.tr("Color.FFE4B5");
		SmallColorChooser.ColorNameFFDEAD=Language.tr("Color.FFDEAD");
		SmallColorChooser.ColorName000080=Language.tr("Color.000080");
		SmallColorChooser.ColorNameFDF5E6=Language.tr("Color.FDF5E6");
		SmallColorChooser.ColorName6B8E23=Language.tr("Color.6B8E23");
		SmallColorChooser.ColorNameFFA500=Language.tr("Color.FFA500");
		SmallColorChooser.ColorNameFF4500=Language.tr("Color.FF4500");
		SmallColorChooser.ColorNameDA70D6=Language.tr("Color.DA70D6");
		SmallColorChooser.ColorNameEEE8AA=Language.tr("Color.EEE8AA");
		SmallColorChooser.ColorName98FB98=Language.tr("Color.98FB98");
		SmallColorChooser.ColorNameAFEEEE=Language.tr("Color.AFEEEE");
		SmallColorChooser.ColorNameDB7093=Language.tr("Color.DB7093");
		SmallColorChooser.ColorNameFFEFD5=Language.tr("Color.FFEFD5");
		SmallColorChooser.ColorNameFFDAB9=Language.tr("Color.FFDAB9");
		SmallColorChooser.ColorNameCD853F=Language.tr("Color.CD853F");
		SmallColorChooser.ColorNameFFC0CB=Language.tr("Color.FFC0CB");
		SmallColorChooser.ColorNameDDA0DD=Language.tr("Color.DDA0DD");
		SmallColorChooser.ColorNameB0E0E6=Language.tr("Color.B0E0E6");
		SmallColorChooser.ColorName800080=Language.tr("Color.800080");
		SmallColorChooser.ColorName663399=Language.tr("Color.663399");
		SmallColorChooser.ColorNameFF0000=Language.tr("Color.FF0000");
		SmallColorChooser.ColorNameBC8F8F=Language.tr("Color.BC8F8F");
		SmallColorChooser.ColorName4169E1=Language.tr("Color.4169E1");
		SmallColorChooser.ColorName8B4513=Language.tr("Color.8B4513");
		SmallColorChooser.ColorNameFA8072=Language.tr("Color.FA8072");
		SmallColorChooser.ColorNameF4A460=Language.tr("Color.F4A460");
		SmallColorChooser.ColorName2E8B57=Language.tr("Color.2E8B57");
		SmallColorChooser.ColorNameFFF5EE=Language.tr("Color.FFF5EE");
		SmallColorChooser.ColorNameA0522D=Language.tr("Color.A0522D");
		SmallColorChooser.ColorNameC0C0C0=Language.tr("Color.C0C0C0");
		SmallColorChooser.ColorName87CEEB=Language.tr("Color.87CEEB");
		SmallColorChooser.ColorName6A5ACD=Language.tr("Color.6A5ACD");
		SmallColorChooser.ColorName708090=Language.tr("Color.708090");
		SmallColorChooser.ColorNameFFFAFA=Language.tr("Color.FFFAFA");
		SmallColorChooser.ColorName00FF7F=Language.tr("Color.00FF7F");
		SmallColorChooser.ColorName4682B4=Language.tr("Color.4682B4");
		SmallColorChooser.ColorNameD2B48C=Language.tr("Color.D2B48C");
		SmallColorChooser.ColorName008080=Language.tr("Color.008080");
		SmallColorChooser.ColorNameD8BFD8=Language.tr("Color.D8BFD8");
		SmallColorChooser.ColorNameFF6347=Language.tr("Color.FF6347");
		SmallColorChooser.ColorName40E0D0=Language.tr("Color.40E0D0");
		SmallColorChooser.ColorNameEE82EE=Language.tr("Color.EE82EE");
		SmallColorChooser.ColorNameF5DEB3=Language.tr("Color.F5DEB3");
		SmallColorChooser.ColorNameFFFFFF=Language.tr("Color.FFFFFF");
		SmallColorChooser.ColorNameF5F5F5=Language.tr("Color.F5F5F5");
		SmallColorChooser.ColorNameFFFF00=Language.tr("Color.FFFF00");
		SmallColorChooser.ColorName9ACD32=Language.tr("Color.9ACD32");
		SmallColorChooser.ColorNameFFFFFA=Language.tr("Color.FFFFFA");

		/* Setup */
		SetupBase.errorSaveTitle=Language.tr("Setup.SaveError.Title");
		SetupBase.errorSaveMessage=Language.tr("Setup.SaveError.Info");

		/* Allgemeines */
		MainPanel.UNSAVED_MODEL=Language.tr("Window.UnsavedFile");

		/* Dialoge */
		BaseDialog.buttonTitleClose=Language.tr("Dialog.Button.Close");
		BaseDialog.buttonTitleOk=Language.tr("Dialog.Button.Ok");
		BaseDialog.buttonTitleCancel=Language.tr("Dialog.Button.Cancel");
		BaseDialog.buttonTitleHelp=Language.tr("Dialog.Button.Help");

		/* Hilfe */
		HelpBase.title=Language.tr("Window.Help");
		HelpBase.buttonClose=Language.tr("Dialog.Button.Close");
		HelpBase.buttonCloseInfo=Language.tr("Help.Close.Info");
		HelpBase.buttonStartPage=Language.tr("Help.StartPage");
		HelpBase.buttonStartPageInfo=Language.tr("Help.StartPage.Info");
		HelpBase.buttonBack=Language.tr("Dialog.Button.Back");
		HelpBase.buttonBackInfo=Language.tr("Help.Back.Info");
		HelpBase.buttonNext=Language.tr("Dialog.Button.Forward");
		HelpBase.buttonNextInfo=Language.tr("Help.Forward.Info");
		HelpBase.buttonContent=Language.tr("Help.Content");
		HelpBase.buttonContentInfo=Language.tr("Help.Content.Info");
		HelpBase.buttonSearch=Language.tr("Help.Search");
		HelpBase.buttonSearchInfo=Language.tr("Help.Search.Info");
		HelpBase.buttonSearchString=Language.tr("Help.Search.SearchString");
		HelpBase.buttonSearchNoHitSelected=Language.tr("Help.Search.NoHitSelected");
		HelpBase.buttonSearchResultTypePage=Language.tr("Help.Search.Type.Page");
		HelpBase.buttonSearchResultTypeIndex=Language.tr("Help.Search.Type.Index");
		HelpBase.buttonSearchResultOnPage=Language.tr("Help.Search.ResultOnPage");
		HelpBase.buttonSearchResultCountSingular=Language.tr("Help.Search.ResultCountSingular");
		HelpBase.buttonSearchResultCountPlural=Language.tr("Help.Search.ResultCountPlural");
		HelpBase.buttonSearchResultSelect=Language.tr("Help.Search.ResultSelect");
		HelpBase.buttonSearchTabSearch=Language.tr("Help.Search.Search");
		HelpBase.buttonSearchTabAllPages=Language.tr("Help.Search.AllPages");
		HelpBase.buttonPrint=Language.tr("Help.Print");
		HelpBase.buttonPrintInfo=Language.tr("Help.Print.Info");
		HelpBase.errorNoEMailTitle=Language.tr("Window.Info.NoEMailProgram.Title");
		HelpBase.errorNoEMailInfo=Language.tr("Window.Info.NoHTMLPrint.Info");
		HelpBase.errorNoInternetTitle=Language.tr("Window.Info.NoInternetConnection");
		HelpBase.errorNoInternetInfo=Language.tr("Window.Info.NoInternetConnection.Address");
		HelpBase.errorPrintTitle=Language.tr("Window.Info.PrintError");
		HelpBase.errorPrintInfoNoHandler=Language.tr("Window.Info.PrintError.NoHandler");
		HelpBase.errorPrintInfo=Language.tr("Window.Info.PrintError.Info");

		/* XML-Daten */
		XMLData.errorRootElementName=Language.tr("XML.RootElementNameError");
		XMLData.errorOutOfMemory=Language.tr("XML.OutOfMemoryError");
		XMLTools.errorInitXMLInterpreter=Language.tr("XML.InterpreterError");
		XMLTools.errorXMLProcess=Language.tr("XML.InterpreterCouldNotProcessData");
		XMLTools.errorXMLProcessFile=Language.tr("XML.ErrorProcessingFile");
		XMLTools.errorInternalErrorNoInputObject=Language.tr("XML.NoInputObjectSelected");
		XMLTools.errorInternalErrorNoOutputObject=Language.tr("XML.NoOutputObjectSelected");
		XMLTools.errorZipCreating=Language.tr("XML.ErrorCreatingZipStream");
		XMLTools.errorZipCreatingFile=Language.tr("XML.ErrorCreatingZipFile");
		XMLTools.errorZipClosing=Language.tr("XML.ErrorClosingStream");
		XMLTools.errorOpeningFile=Language.tr("XML.ErrorOpeningFile");
		XMLTools.errorClosingFile=Language.tr("XML.ErrorClosingFile");
		XMLTools.errorCanceledByUser=Language.tr("XML.ErrorCanceledByUser");
		XMLTools.errorEncryptingFile=Language.tr("XML.ErrorEncryptingFile");
		XMLTools.errorDecryptingFile=Language.tr("XML.ErrorDecryptingFile");
		XMLTools.errorFileDoesNotExists=Language.tr("XML.FileNotFound");
		XMLTools.errorNoEmbeddedXMLData=Language.tr("XML.ErrorNoEmbeddedData");
		XMLTools.errorStreamProcessing=Language.tr("XML.ErrorProcessingStream");
		XMLTools.errorStreamClosing=Language.tr("XML.ErrorClosingStream");
		XMLTools.enterPassword=Language.tr("XML.EnterPassword");
		XMLTools.fileTypeXML=Language.tr("FileType.xml");
		XMLTools.fileTypeCompressedXML=Language.tr("FileType.xmz");
		XMLTools.fileTypeTARCompressedXML=Language.tr("FileType.targz");
		XMLTools.fileTypeJSON=Language.tr("FileType.json");
		XMLTools.fileTypeEncryptedXML=Language.tr("FileType.cs");
		XMLTools.fileTypeAll=Language.tr("FileType.AllSupportedFiles");
		XMLTools.xmlComment=String.format(Language.tr("XML.Comment"),MainFrame.PROGRAM_NAME,"https://"+MainPanel.REPOSITORY_URL);

		/* Elementenkatalog */
		ModelElementCatalog.GROUP_INPUTOUTPUT=Language.tr("Elements.Catalog.InputOutput");
		ModelElementCatalog.GROUP_PROCESSING=Language.tr("Elements.Catalog.Processing");
		ModelElementCatalog.GROUP_BATCH=Language.tr("Elements.Catalog.Batch");
		ModelElementCatalog.GROUP_ASSIGN=Language.tr("Elements.Catalog.Assignments");
		ModelElementCatalog.GROUP_BRANCH=Language.tr("Elements.Catalog.Branching");
		ModelElementCatalog.GROUP_BARRIER=Language.tr("Elements.Catalog.Barriers");
		ModelElementCatalog.GROUP_TRANSPORT=Language.tr("Elements.Catalog.Transport");
		ModelElementCatalog.GROUP_DATAINPUTOUTPUT=Language.tr("Elements.Catalog.DataInputOutput");
		ModelElementCatalog.GROUP_LOGIC=Language.tr("Elements.Catalog.Logic");
		ModelElementCatalog.GROUP_ANALOG=Language.tr("Elements.Catalog.Analog");
		ModelElementCatalog.GROUP_ANIMATION=Language.tr("Elements.Catalog.Animation");
		ModelElementCatalog.GROUP_INTERACTIVE=Language.tr("Elements.Catalog.AnimationInteractive");
		ModelElementCatalog.GROUP_DECORATION=Language.tr("Elements.Catalog.Decoration");
		ModelElementCatalog.GROUP_OTHERS=Language.tr("Elements.Catalog.Others");
		ModelElementCatalog.GROUP_ORDER=new String[]{
				ModelElementCatalog.GROUP_INPUTOUTPUT,
				ModelElementCatalog.GROUP_PROCESSING,
				ModelElementCatalog.GROUP_ASSIGN,
				ModelElementCatalog.GROUP_BRANCH,
				ModelElementCatalog.GROUP_BARRIER,
				ModelElementCatalog.GROUP_BATCH,
				ModelElementCatalog.GROUP_TRANSPORT,
				ModelElementCatalog.GROUP_DATAINPUTOUTPUT,
				ModelElementCatalog.GROUP_LOGIC,
				ModelElementCatalog.GROUP_ANALOG,
				ModelElementCatalog.GROUP_ANIMATION,
				ModelElementCatalog.GROUP_INTERACTIVE,
				ModelElementCatalog.GROUP_DECORATION,
				ModelElementCatalog.GROUP_OTHERS
		};
		ModelElementCatalog.forceReinit();

		/* Editor-Basis-Panel */
		EditorPanelBase.SAVE_MODEL=Language.tr("Main.Toolbar.SaveModel");
		EditorPanelBase.SAVE_MODEL_ERROR=Language.tr("Editor.SaveModel.Error");
		EditorPanelBase.LOAD_MODEL=Language.tr("Main.Toolbar.LoadModel");
		EditorPanelBase.NEWER_VERSION_INFO=Language.tr("Editor.NewerVersion.Info");
		EditorPanelBase.NEWER_VERSION_TITLE=Language.tr("Editor.NewerVersion.Title");
		EditorPanelBase.UNKNOWN_ELEMENTS_TITLE=Language.tr("Editor.UnknownElements.Title");
		EditorPanelBase.UNKNOWN_ELEMENTS_INFO=Language.tr("Editor.UnknownElements.Info");

		/* Statistik-Basis-Panel */
		StatisticsBasePanel.typeText=Language.tr("Statistic.Type.Text");
		StatisticsBasePanel.typeTable=Language.tr("Statistic.Type.Table");
		StatisticsBasePanel.typeImage=Language.tr("Statistic.Type.Image");
		StatisticsBasePanel.typeNoData=Language.tr("Statistic.Type.NoData");
		StatisticsBasePanel.overwriteTitle=Language.tr("Dialog.Overwrite.Title");
		StatisticsBasePanel.overwriteInfo=Language.tr("Dialog.Overwrite.Info");
		StatisticsBasePanel.writeErrorTitle=Language.tr("Statistic.WriteError.Title");
		StatisticsBasePanel.writeErrorInfo=Language.tr("Statistic.WriteError.Info");
		StatisticsBasePanel.treeCopyParameter=Language.tr("Statistic.Tree.Parameter");
		StatisticsBasePanel.treeCopyParameterHint=Language.tr("Statistic.Tree.Parameter.Hint");
		StatisticsBasePanel.treeBookmarkSetOn=Language.tr("Statistic.Tree.Parameter.BookmarkOn");
		StatisticsBasePanel.treeBookmarkSetOnHint=Language.tr("Statistic.Tree.Parameter.BookmarkOn.Hint");
		StatisticsBasePanel.treeBookmarkSetOff=Language.tr("Statistic.Tree.Parameter.BookmarkOff");
		StatisticsBasePanel.treeBookmarkSetOffHint=Language.tr("Statistic.Tree.Parameter.BookmarkOff.Hint");
		StatisticsBasePanel.treeBookmarkJump=Language.tr("Statistic.Tree.Parameter.BookmarkJump");
		StatisticsBasePanel.treeBookmarkJumpHint=Language.tr("Statistic.Tree.Parameter.BookmarkJump.Hint");
		StatisticsBasePanel.viewersInformation=Language.tr("Statistic.Viewer.Information");
		StatisticsBasePanel.viewersNoHTMLApplicationInfo=Language.tr("Statistic.Viewer.NoHTMLApplication.Info");
		StatisticsBasePanel.viewersNoHTMLApplicationTitle=Language.tr("Statistic.Viewer.NoHTMLApplication.Title");
		StatisticsBasePanel.viewersSaveText=Language.tr("Statistic.Viewer.SaveText");
		StatisticsBasePanel.viewersSaveTable=Language.tr("Statistic.Viewer.SaveTable");
		StatisticsBasePanel.viewersSaveImage=Language.tr("Statistic.Viewer.SaveImage");
		StatisticsBasePanel.viewersSaveImageSizeTitle=Language.tr("Statistic.Viewer.SaveImage.Size.Title");
		StatisticsBasePanel.viewersSaveImageSizePrompt=Language.tr("Statistic.Viewer.SaveImage.Size.Prompt");
		StatisticsBasePanel.viewersSaveImageSizeErrorTitle=Language.tr("Statistic.Viewer.SaveImage.Size.Error.Title");
		StatisticsBasePanel.viewersSaveImageSizeErrorInfo=Language.tr("Statistic.Viewer.SaveImage.Size.Error.Info");
		StatisticsBasePanel.viewersSaveImageErrorTitle=Language.tr("Statistic.Viewer.SaveImage.Error.Title");
		StatisticsBasePanel.viewersSaveImageErrorInfo=Language.tr("Statistic.Viewer.SaveImage.Error.Info");
		StatisticsBasePanel.viewersLoadImage=Language.tr("Statistic.Viewer.LoadImage");
		StatisticsBasePanel.viewersSaveTableErrorTitle=Language.tr("Statistic.Viewer.SaveTable.Error.Title");
		StatisticsBasePanel.viewersSaveTableErrorInfo=Language.tr("Statistic.Viewer.SaveTable.Error.Info");
		StatisticsBasePanel.viewersChartSetupTitle=Language.tr("Statistic.Viewer.DiagramSettings.Title");
		StatisticsBasePanel.viewersChartSetupDefaults=Language.tr("Statistic.Viewer.DiagramSettings.Defaults");
		StatisticsBasePanel.viewersChartSetupDefaultsHint=Language.tr("Statistic.Viewer.DiagramSettings.Defaults.Hint");
		StatisticsBasePanel.viewersChartSetupDefaultsThis=Language.tr("Statistic.Viewer.DiagramSettings.Defaults.ThisPage");
		StatisticsBasePanel.viewersChartSetupDefaultsAll=Language.tr("Statistic.Viewer.DiagramSettings.Defaults.AllPages");
		StatisticsBasePanel.viewersChartSetupFontSize=Language.tr("Statistic.Viewer.DiagramSettings.Font.Size");
		StatisticsBasePanel.viewersChartSetupFontBold=Language.tr("Statistic.Viewer.DiagramSettings.Font.Bold");
		StatisticsBasePanel.viewersChartSetupFontItalic=Language.tr("Statistic.Viewer.DiagramSettings.Font.Italic");
		StatisticsBasePanel.viewersChartSetupTitleFont=Language.tr("Statistic.Viewer.DiagramSettings.DiagramTitle");
		StatisticsBasePanel.viewersChartSetupAxisFont=Language.tr("Statistic.Viewer.DiagramSettings.Axis");
		StatisticsBasePanel.viewersChartSetupAxisLabelsFont=Language.tr("Statistic.Viewer.DiagramSettings.Axis.LabelsFont");
		StatisticsBasePanel.viewersChartSetupAxisValuesFont=Language.tr("Statistic.Viewer.DiagramSettings.Axis.ValuesFont");
		StatisticsBasePanel.viewersChartSetupLegendFont=Language.tr("Statistic.Viewer.DiagramSettings.LegendFont");
		StatisticsBasePanel.viewersChartSetupSurface=Language.tr("Statistic.Viewer.DiagramSettings.Surface");
		StatisticsBasePanel.viewersChartSetupSurfaceBackgroundColor=Language.tr("Statistic.Viewer.DiagramSettings.Background.Color");
		StatisticsBasePanel.viewersChartSetupSurfaceBackgroundGradient=Language.tr("Statistic.Viewer.DiagramSettings.Background.Gradient");
		StatisticsBasePanel.viewersChartSetupSurfaceBackgroundGradientActive=Language.tr("Statistic.Viewer.DiagramSettings.Background.Gradient.Active");
		StatisticsBasePanel.viewersChartSetupSurfaceOutlineColor=Language.tr("Statistic.Viewer.DiagramSettings.Outline.Color");
		StatisticsBasePanel.viewersChartSetupSurfaceOutlineWidth=Language.tr("Statistic.Viewer.DiagramSettings.Outline.Width");
		StatisticsBasePanel.viewersReport=Language.tr("Statistic.Viewer.Report");
		StatisticsBasePanel.viewersReportHint=Language.tr("Statistic.Viewer.Report.Hint");
		StatisticsBasePanel.viewersToolsHint=Language.tr("Statistic.Viewer.Tools.Hint");
		StatisticsBasePanel.viewersToolsShowAll=Language.tr("Statistic.Viewer.Tools.ShowAll");
		StatisticsBasePanel.viewersToolsHideAll=Language.tr("Statistic.Viewer.Tools.HideAll");
		StatisticsBasePanel.viewersReportNoTablesSelectedTitle=Language.tr("Statistic.Viewer.Report.NoTablesSelected.Title");
		StatisticsBasePanel.viewersReportNoTablesSelectedInfo=Language.tr("Statistic.Viewer.Report.NoTablesSelected.Info");
		StatisticsBasePanel.viewersReportSaveWorkbook=Language.tr("Statistic.Viewer.Report.Workbook");
		StatisticsBasePanel.viewersReportSaveWorkbookErrorTitle=Language.tr("Statistic.Viewer.Report.Workbook.Error.Title");
		StatisticsBasePanel.viewersReportSaveWorkbookErrorInfo=Language.tr("Statistic.Viewer.Report.Workbook.Error.Info");
		StatisticsBasePanel.viewersReportSaveHTMLImages=Language.tr("Statistic.Viewer.Report.SaveHTMLImages");
		StatisticsBasePanel.viewersReportSaveHTMLImagesInline=Language.tr("Statistic.Viewer.Report.SaveHTMLImages.Inline");
		StatisticsBasePanel.viewersReportSaveHTMLImagesFile=Language.tr("Statistic.Viewer.Report.SaveHTMLImages.Files");
		StatisticsBasePanel.viewersReportSaveHTMLAppTitle=Language.tr("Statistic.Viewer.Report.HTMLAppTitle");
		StatisticsBasePanel.viewersReportSaveHTMLAppInfo=Language.tr("Statistic.Viewer.Report.HTMLApp.Info");
		StatisticsBasePanel.viewersReportSaveHTMLAppJSError=Language.tr("Statistic.Viewer.Report.HTMLApp.JSError");
		StatisticsBasePanel.viewersReportCustomize=Language.tr("Statistic.Viewer.Report.Settings.MenuItem");
		StatisticsBasePanel.viewersReportCustomizeTitle=Language.tr("Statistic.Viewer.Report.Settings.Title");
		StatisticsBasePanel.viewersReportCustomizeReset=Language.tr("Statistic.Viewer.Report.Settings.Reset");
		StatisticsBasePanel.viewersReportCustomizeResetThisPage=Language.tr("Statistic.Viewer.Report.Settings.Reset.ThisPage");
		StatisticsBasePanel.viewersReportCustomizeResetAllPages=Language.tr("Statistic.Viewer.Report.Settings.Reset.AllPages");
		StatisticsBasePanel.viewersReportCustomizeTabPageMargins=Language.tr("Statistic.Viewer.Report.Settings.Margins");
		StatisticsBasePanel.viewersReportCustomizeTabPageMarginsTop=Language.tr("Statistic.Viewer.Report.Settings.Margins.Top");
		StatisticsBasePanel.viewersReportCustomizeTabPageMarginsRight=Language.tr("Statistic.Viewer.Report.Settings.Margins.Right");
		StatisticsBasePanel.viewersReportCustomizeTabPageMarginsBottom=Language.tr("Statistic.Viewer.Report.Settings.Margins.Bottom");
		StatisticsBasePanel.viewersReportCustomizeTabPageMarginsLeft=Language.tr("Statistic.Viewer.Report.Settings.Margins.Left");
		StatisticsBasePanel.viewersReportCustomizeTabHeader=Language.tr("Statistic.Viewer.Report.Settings.Header");
		StatisticsBasePanel.viewersReportCustomizeTabHeaderLogo=Language.tr("Statistic.Viewer.Report.Settings.Header.Logo");
		StatisticsBasePanel.viewersReportCustomizeTabHeaderLogoLoad=Language.tr("Statistic.Viewer.Report.Settings.Header.Logo.Load");
		StatisticsBasePanel.viewersReportCustomizeTabHeaderLogoLoadHint=Language.tr("Statistic.Viewer.Report.Settings.Header.Logo.Load.Hint");
		StatisticsBasePanel.viewersReportCustomizeTabHeaderLogoPaste=Language.tr("Statistic.Viewer.Report.Settings.Header.Logo.Paste");
		StatisticsBasePanel.viewersReportCustomizeTabHeaderLogoPasteHint=Language.tr("Statistic.Viewer.Report.Settings.Header.Logo.Paste.Hint");
		StatisticsBasePanel.viewersReportCustomizeTabHeaderLogoRemove=Language.tr("Statistic.Viewer.Report.Settings.Header.Logo.Remove");
		StatisticsBasePanel.viewersReportCustomizeTabHeaderLogoRemoveHint=Language.tr("Statistic.Viewer.Report.Settings.Header.Logo.Remove.Hint");
		StatisticsBasePanel.viewersReportCustomizeTabHeaderLogoAlignment=Language.tr("Statistic.Viewer.Report.Settings.Header.Logo.Alignment");
		StatisticsBasePanel.viewersReportCustomizeTabHeaderLogoAlignmentLeft=Language.tr("Statistic.Viewer.Report.Settings.Header.Logo.Alignment.Left");
		StatisticsBasePanel.viewersReportCustomizeTabHeaderLogoAlignmentCenter=Language.tr("Statistic.Viewer.Report.Settings.Header.Logo.Alignment.Center");
		StatisticsBasePanel.viewersReportCustomizeTabHeaderLogoAlignmentRight=Language.tr("Statistic.Viewer.Report.Settings.Header.Logo.Alignment.Right");
		StatisticsBasePanel.viewersReportCustomizeTabHeaderLogoMaxWidth=Language.tr("Statistic.Viewer.Report.Settings.Header.Logo.MaxWidth");
		StatisticsBasePanel.viewersReportCustomizeTabHeaderLogoMaxHeight=Language.tr("Statistic.Viewer.Report.Settings.Header.Logo.MaxHeight");
		StatisticsBasePanel.viewersReportCustomizeTabHeaderLogoRepeat=Language.tr("Statistic.Viewer.Report.Settings.Header.Logo.Repeat");
		StatisticsBasePanel.viewersReportCustomizeTabHeaderLogoRepeatHint=Language.tr("Statistic.Viewer.Report.Settings.Header.Logo.Repeat.Hint");
		StatisticsBasePanel.viewersReportCustomizeTabFooter=Language.tr("Statistic.Viewer.Report.Settings.Footer");
		StatisticsBasePanel.viewersReportCustomizeTabFooterPageNumber=Language.tr("Statistic.Viewer.Report.Settings.Footer.PageNumber");
		StatisticsBasePanel.viewersReportCustomizeTabFooterDate=Language.tr("Statistic.Viewer.Report.Settings.Footer.Date");
		StatisticsBasePanel.viewersReportCustomizeTabFonts=Language.tr("Statistic.Viewer.Report.Settings.Fonts");
		StatisticsBasePanel.viewersReportCustomizeTabFontsHeader=Language.tr("Statistic.Viewer.Report.Settings.Fonts.Header");
		StatisticsBasePanel.viewersReportCustomizeTabFontsText=Language.tr("Statistic.Viewer.Report.Settings.Fonts.Text");
		StatisticsBasePanel.viewersReportCustomizeTabFontsTableHeader=Language.tr("Statistic.Viewer.Report.Settings.Fonts.TableHeader");
		StatisticsBasePanel.viewersReportCustomizeTabFontsTableText=Language.tr("Statistic.Viewer.Report.Settings.Fonts.TableText");
		StatisticsBasePanel.viewersReportCustomizeTabFontsFooter=Language.tr("Statistic.Viewer.Report.Settings.Fonts.Footer");
		StatisticsBasePanel.viewersReportCustomizeTabFontsParSkip=Language.tr("Statistic.Viewer.Report.Settings.Fonts.ParSkip");
		StatisticsBasePanel.viewersReportCustomizeTabFontsSize=Language.tr("Statistic.Viewer.Report.Settings.Fonts.Size");
		StatisticsBasePanel.viewersReportCustomizeTabFontsBold=Language.tr("Statistic.Viewer.Report.Settings.Fonts.Bold");
		StatisticsBasePanel.viewersReportCustomizePDFandDOCX=Language.tr("Statistic.Viewer.Report.Settings.Type.PDFandDOCX");
		StatisticsBasePanel.viewersReportCustomizePDFonly=Language.tr("Statistic.Viewer.Report.Settings.Type.PDFonly");

		StatisticsBasePanel.viewersToolbarZoom=Language.tr("Statistic.Viewer.Toolbar.Zoom");
		StatisticsBasePanel.viewersToolbarZoomHint=Language.tr("Statistic.Viewer.Toolbar.Zoom.Hint");
		StatisticsBasePanel.viewersToolbarZoomHintPanel=Language.tr("Statistic.Viewer.Toolbar.Zoom.HintPanel");
		StatisticsBasePanel.viewersToolbarCopy=Language.tr("Statistic.Viewer.Toolbar.Copy");
		StatisticsBasePanel.viewersToolbarCopyHint=Language.tr("Statistic.Viewer.Toolbar.Copy.Hint");
		StatisticsBasePanel.viewersToolbarCopyHintPlain=Language.tr("Statistic.Viewer.Toolbar.Copy.HintPlain");
		StatisticsBasePanel.viewersToolbarCopyDefaultSize=Language.tr("Statistic.Viewer.Toolbar.Copy.DefaultSize");
		StatisticsBasePanel.viewersToolbarCopyWindowSize=Language.tr("Statistic.Viewer.Toolbar.Copy.WindowSize");
		StatisticsBasePanel.viewersToolbarPrint=Language.tr("Statistic.Viewer.Toolbar.Print");
		StatisticsBasePanel.viewersToolbarPrintHint=Language.tr("Statistic.Viewer.Toolbar.Print.Hint");
		StatisticsBasePanel.viewersToolbarSave=Language.tr("Statistic.Viewer.Toolbar.Save");
		StatisticsBasePanel.viewersToolbarSaveHint=Language.tr("Statistic.Viewer.Toolbar.Save.Hint");
		StatisticsBasePanel.viewersToolbarSaveDefaultSize=Language.tr("Statistic.Viewer.Toolbar.Save.DefaultSize");
		StatisticsBasePanel.viewersToolbarSaveWindowSize=Language.tr("Statistic.Viewer.Toolbar.Save.WindowSize");
		StatisticsBasePanel.viewersToolbarNavigation=Language.tr("Statistic.Viewer.Toolbar.Navigation");
		StatisticsBasePanel.viewersToolbarNavigationHint=Language.tr("Statistic.Viewer.Toolbar.Navigation.Hint");
		StatisticsBasePanel.viewersToolbarSearch=Language.tr("Statistic.Viewer.Toolbar.Search");
		StatisticsBasePanel.viewersToolbarSearchHint=Language.tr("Statistic.Viewer.Toolbar.Search.Hint");
		StatisticsBasePanel.viewersToolbarSearchTitle=Language.tr("Statistic.Viewer.Toolbar.Search.DialogTitle");
		StatisticsBasePanel.viewersToolbarSearchString=Language.tr("Statistic.Viewer.Toolbar.Search.DialogSearchString");
		StatisticsBasePanel.viewersToolbarSearchCaseSensitive=Language.tr("Statistic.Viewer.Toolbar.Search.DialogCaseSensitive");
		StatisticsBasePanel.viewersToolbarSearchRegEx=Language.tr("Statistic.Viewer.Toolbar.Search.DialogRegEx");
		StatisticsBasePanel.viewersToolbarSearchNotFound=Language.tr("Statistic.Viewer.Toolbar.Search.NotFound");
		StatisticsBasePanel.viewersToolbarSettings=Language.tr("Statistic.Viewer.Toolbar.Settings");
		StatisticsBasePanel.viewersToolbarSettingsHint=Language.tr("Statistic.Viewer.Toolbar.Settings.Hint");
		StatisticsBasePanel.viewersToolbarOpenText=Language.tr("Statistic.Viewer.Toolbar.OpenText");
		StatisticsBasePanel.viewersToolbarOpenTextHint=Language.tr("Statistic.Viewer.Toolbar.OpenText.Hint");
		StatisticsBasePanel.viewersToolbarOpenTable=Language.tr("Statistic.Viewer.Toolbar.OpenTable");
		StatisticsBasePanel.viewersToolbarOpenTableHint=Language.tr("Statistic.Viewer.Toolbar.OpenTable.Hint");
		StatisticsBasePanel.viewersToolbarWord=Language.tr("Statistic.Viewer.Toolbar.OpenWord");
		StatisticsBasePanel.viewersToolbarWordHint=Language.tr("Statistic.Viewer.Toolbar.OpenWordHint");
		StatisticsBasePanel.viewersToolbarODT=Language.tr("Statistic.Viewer.Toolbar.OpenODT");
		StatisticsBasePanel.viewersToolbarODTHint=Language.tr("Statistic.Viewer.Toolbar.OpenODT.Hint");
		StatisticsBasePanel.viewersToolbarExcel=Language.tr("Statistic.Viewer.Toolbar.Excel");
		StatisticsBasePanel.viewersToolbarExcelHint=Language.tr("Statistic.Viewer.Toolbar.Excel.Hint");
		StatisticsBasePanel.viewersToolbarExcelPrefix=Language.tr("Statistic.Viewer.Toolbar.Excel.Prefix");
		StatisticsBasePanel.viewersToolbarExcelSaveErrorTitle=Language.tr("Statistic.Viewer.Toolbar.Excel.Error.Title");
		StatisticsBasePanel.viewersToolbarExcelSaveErrorInfo=Language.tr("Statistic.Viewer.Toolbar.Excel.Error.Info");
		StatisticsBasePanel.viewersToolbarODS=Language.tr("Statistic.Viewer.Toolbar.OpenODS");
		StatisticsBasePanel.viewersToolbarODSHint=Language.tr("Statistic.Viewer.Toolbar.OpenODS.Hint");
		StatisticsBasePanel.viewersToolbarPDF=Language.tr("Statistic.Viewer.Toolbar.OpenPDF");
		StatisticsBasePanel.viewersToolbarPDFHint=Language.tr("Statistic.Viewer.Toolbar.OpenPDF.Hint");
		StatisticsBasePanel.viewersToolbarNewWindow=Language.tr("Statistic.Viewer.Toolbar.NewWindow");
		StatisticsBasePanel.viewersToolbarNewWindowHint=Language.tr("Statistic.Viewer.Toolbar.NewWindow.Hint");
		StatisticsBasePanel.viewersToolbarNewWindowTitle=Language.tr("Statistic.Viewer.Toolbar.NewWindow.Title");
		StatisticsBasePanel.viewersToolbarWindowSize=Language.tr("Statistic.Viewer.Toolbar.NewWindow.Size");
		StatisticsBasePanel.viewersToolbarWindowSizeHint=Language.tr("Statistic.Viewer.Toolbar.NewWindow.Size.Hint");
		StatisticsBasePanel.viewersToolbarFullscreen=Language.tr("Statistic.Viewer.Toolbar.NewWindow.Fullscreen");
		StatisticsBasePanel.viewersToolbarFullscreenHint=Language.tr("Statistic.Viewer.Toolbar.NewWindow.Fullscreen.Hint");
		StatisticsBasePanel.viewersToolbarSelectAll=Language.tr("Statistic.Viewer.Toolbar.SelectAll");
		StatisticsBasePanel.viewersToolbarSelectAllHint=Language.tr("Statistic.Viewer.Toolbar.SelectAll.Hint");
		StatisticsBasePanel.viewersToolbarSelectNone=Language.tr("Statistic.Viewer.Toolbar.SelectNone");
		StatisticsBasePanel.viewersToolbarSelectNoneHint=Language.tr("Statistic.Viewer.Toolbar.SelectNone.Hint");
		StatisticsBasePanel.viewersToolbarSaveTables=Language.tr("Statistic.Viewer.Toolbar.SaveTables");
		StatisticsBasePanel.viewersToolbarSaveTablesHint=Language.tr("Statistic.Viewer.Toolbar.SaveTables.Hint");
		StatisticsBasePanel.contextCopy=Language.tr("Statistic.Viewer.Context.Copy");
		StatisticsBasePanel.contextCopyTable=Language.tr("Statistic.Viewer.Context.Copy.Table");
		StatisticsBasePanel.contextCopyColumn=Language.tr("Statistic.Viewer.Context.Copy.Column");
		StatisticsBasePanel.contextSort=Language.tr("Statistic.Viewer.Context.Sort");
		StatisticsBasePanel.contextSortAscending=Language.tr("Statistic.Viewer.Context.Sort.Ascending");
		StatisticsBasePanel.contextSortDescending=Language.tr("Statistic.Viewer.Context.Sort.Descending");
		StatisticsBasePanel.contextSortOriginal=Language.tr("Statistic.Viewer.Context.Sort.Reset");
		StatisticsBasePanel.contextSelectColumn=Language.tr("Statistic.Viewer.Context.SelectColumn");
		StatisticsBasePanel.contextFilter=Language.tr("Statistic.Viewer.Context.Filter");
		StatisticsBasePanel.contextFilterReset=Language.tr("Statistic.Viewer.Context.Filter.All");
		StatisticsBasePanel.contextFilterSelect=Language.tr("Statistic.Viewer.Context.Filter.Select");
		StatisticsBasePanel.contextFilterSelectTitle=Language.tr("Statistic.Viewer.Context.Filter.SelectTitle");
		StatisticsBasePanel.contextFilterSelectAll=Language.tr("Statistic.Viewer.Context.Filter.SelectAll");
		StatisticsBasePanel.contextFilterSelectNone=Language.tr("Statistic.Viewer.Context.Filter.SelectNone");
		StatisticsBasePanel.contextColWidthThis=Language.tr("Statistic.Viewer.Context.Width.This");
		StatisticsBasePanel.contextColWidthAll=Language.tr("Statistic.Viewer.Context.Width.All");
		StatisticsBasePanel.contextColWidthDefault=Language.tr("Statistic.Viewer.Context.Width.Default");
		StatisticsBasePanel.contextColWidthByContent=Language.tr("Statistic.Viewer.Context.Width.ByContent");
		StatisticsBasePanel.contextColWidthByContentAndHeader=Language.tr("Statistic.Viewer.Context.Width.ByContentAndHeader");
		StatisticsBasePanel.contextColWidthByWindowWidth=Language.tr("Statistic.Viewer.Context.Width.ByWindowWidth");
		StatisticsBasePanel.viewersSpecialTextCategory=Language.tr("Statistic.Viewer.SpecialText.Category");
		StatisticsBasePanel.viewersSpecialTextSubCategory=Language.tr("Statistic.Viewer.SpecialText.SubCategory");
		StatisticsBasePanel.viewersSpecialTextNoData=Language.tr("Statistic.Viewer.SpecialText.NoData");
		StatisticsBasePanel.viewersSpecialTextStartSimulation=Language.tr("Statistic.Viewer.SpecialText.StartSimulation");
		StatisticsBasePanel.viewersSpecialTextLoadData=Language.tr("Statistic.Viewer.SpecialText.LoadData");
		StatisticsBasePanel.viewersChartNumber=Language.tr("Statistic.Viewer.Chart.Number");
		StatisticsBasePanel.viewersChartPart=Language.tr("Statistic.Viewer.Chart.Part");
		StatisticsBasePanel.viewersChartTime=Language.tr("Statistic.Viewer.Chart.Time");
		StatisticsBasePanel.viewersChartInSeconds=Language.tr("Statistic.Viewer.Chart.InSeconds");
		StatisticsBasePanel.viewersChartInMinutes=Language.tr("Statistic.Viewer.Chart.InMinutes");
		StatisticsBasePanel.viewersChartInHours=Language.tr("Statistic.Viewer.Chart.InHours");
		StatisticsBasePanel.viewersTextSeconds=Language.tr("Statistic.Seconds");
		StatisticsBasePanel.descriptionShow=Language.tr("Statistic.Description.Show");
		StatisticsBasePanel.descriptionShowHint=Language.tr("Statistic.Description.Show.Hint");
		StatisticsBasePanel.descriptionHide=Language.tr("Statistic.Description.Hide");
		StatisticsBasePanel.descriptionHideHint=Language.tr("Statistic.Description.Hide.Hint");
		StatisticsBasePanel.previousAdd=Language.tr("Statistic.Previous");
		StatisticsBasePanel.previousAddHint=Language.tr("Statistic.Previous.Hint");
		StatisticsBasePanel.previousRemove=Language.tr("Statistic.PreviousRemove");
		StatisticsBasePanel.previousRemoveHint=Language.tr("Statistic.PreviousRemove.Hint");
		StatisticsBasePanel.internetErrorTitle=Language.tr("Statistic.Viewer.NoInternet.Title");
		StatisticsBasePanel.internetErrorInfo=Language.tr("Statistic.Viewer.NoInternet.Info");
		StatisticsBasePanel.mailErrorTitle=Language.tr("Statistic.Viewer.MailError.Title");
		StatisticsBasePanel.mailErrorInfo=Language.tr("Statistic.Viewer.MailError.Info");
		StatisticsBasePanel.fileTypeTXT=Language.tr("FileType.Text");
		StatisticsBasePanel.fileTypeRTF=Language.tr("FileType.RTF");
		StatisticsBasePanel.fileTypeHTML=Language.tr("FileType.HTML");
		StatisticsBasePanel.fileTypeHTMLJS=Language.tr("FileType.HTMLApp");
		StatisticsBasePanel.fileTypeDOCX=Language.tr("FileType.Word");
		StatisticsBasePanel.fileTypeODT=Language.tr("FileType.FileTypeODT");
		StatisticsBasePanel.fileTypePDF=Language.tr("FileType.PDF");
		StatisticsBasePanel.fileTypeMD=Language.tr("FileType.md");
		StatisticsBasePanel.fileTypeJPG=Language.tr("FileType.jpeg");
		StatisticsBasePanel.fileTypeGIF=Language.tr("FileType.gif");
		StatisticsBasePanel.fileTypePNG=Language.tr("FileType.png");
		StatisticsBasePanel.fileTypeBMP=Language.tr("FileType.bmp");
		StatisticsBasePanel.fileTypeTIFF=Language.tr("FileType.tiff");
		StatisticsBasePanel.fileTypeWordWithImage=Language.tr("FileType.WordImage");
		StatisticsBasePanel.fileTypeSCE=Language.tr("FileType.SciLabScript");
		StatisticsBasePanel.fileTypeTEX=Language.tr("FileType.LaTeX");
		StatisticsBasePanel.fileTypeTYP=Language.tr("FileType.Typst");

		/* Kommandozeilen-System */
		BaseCommandLineSystem.errorBig=Language.tr("Dialog.Title.Error").toUpperCase();
		BaseCommandLineSystem.unknownCommand=Language.tr("CommandLine.UnknownCommand");
		BaseCommandLineSystem.commandCountIf=Language.tr("CommandLine.Count.If");
		BaseCommandLineSystem.commandCountThen0=Language.tr("CommandLine.Count.Then0");
		BaseCommandLineSystem.commandCountThen1=Language.tr("CommandLine.Count.Then1");
		BaseCommandLineSystem.commandCountThenN=Language.tr("CommandLine.Count.ThenN");
		BaseCommandLineSystem.commandCountThenAtLeast1=Language.tr("CommandLine.Count.ThenAtLeast1");
		BaseCommandLineSystem.commandCountThenAtLeastN=Language.tr("CommandLine.Count.ThenAtLeastN");
		BaseCommandLineSystem.commandCountThenMaximum1=Language.tr("CommandLine.Count.ThenMaximum1");
		BaseCommandLineSystem.commandCountThenMaximumN=Language.tr("CommandLine.Count.ThenMaximumN");
		BaseCommandLineSystem.commandCountThenBut0=Language.tr("CommandLine.Count.But0");
		BaseCommandLineSystem.commandCountThenBut1=Language.tr("CommandLine.Count.But1");
		BaseCommandLineSystem.commandCountThenButN=Language.tr("CommandLine.Count.ButN");
		BaseCommandLineSystem.commandReportHelp=Language.tr("CommandLine.ReportBase.Help");
		BaseCommandLineSystem.commandReportError=Language.tr("CommandLine.ReportBase.Error");
		BaseCommandLineSystem.commandReportErrorInputDoesNotExists=Language.tr("CommandLine.ReportBase.Error.Input");
		BaseCommandLineSystem.commandReportErrorOutputExists=Language.tr("CommandLine.ReportBase.Error.Output");
		BaseCommandLineSystem.commandReportDone=Language.tr("CommandLine.ReportBase.Done");
		BaseCommandLineSystem.commandHelpName=Language.tr("CommandLine.Help.Name");
		BaseCommandLineSystem.commandHelpNamesOtherLanguages=Language.trOther("CommandLine.Help.Name").toArray(String[]::new);
		BaseCommandLineSystem.commandHelpHelpShort=Language.tr("CommandLine.Help.Help.Short");
		BaseCommandLineSystem.commandHelpHelpLong=Language.tr("CommandLine.Help.Help.Long");
		BaseCommandLineSystem.commandHelpInfo1=Language.tr("CommandLine.Help.Info1");
		BaseCommandLineSystem.commandHelpInfo2=Language.tr("CommandLine.Help.Info2");
		BaseCommandLineSystem.commandHelpError=Language.tr("CommandLine.Help.Error");
		BaseCommandLineSystem.commandInteractiveName=Language.trAll("CommandLine.Interactive.Name");
		BaseCommandLineSystem.commandInteractiveNamesOtherLanguages=Language.trOther("CommandLine.Interactive.Name").toArray(String[]::new);
		BaseCommandLineSystem.commandHelpInteractiveShort=Language.tr("CommandLine.Interactive.Description.Short");
		BaseCommandLineSystem.commandHelpInteractiveLong=Language.tr("CommandLine.Interactive.Description.Long");
		BaseCommandLineSystem.commandHelpInteractiveStart=Language.tr("CommandLine.Interactive.Start");
		BaseCommandLineSystem.commandHelpInteractiveStop=Language.tr("CommandLine.Interactive.Stop");
		BaseCommandLineSystem.commandHelpInteractiveReady=Language.tr("CommandLine.Interactive.Ready");

		CommandLineDialog.title=Language.tr("CommandLine.Dialog.Title");
		CommandLineDialog.stop=Language.tr("CommandLine.Dialog.StopCommand");
		CommandLineDialog.stopHint=Language.tr("CommandLine.Dialog.StopCommand.Hint");
		CommandLineDialog.labelCommand=Language.tr("CommandLine.Dialog.Command");
		CommandLineDialog.tabDescription=Language.tr("CommandLine.Dialog.Tab.Description");
		CommandLineDialog.tabParametersAndResults=Language.tr("CommandLine.Dialog.Tab.ParametersAndResults");
		CommandLineDialog.labelParameters=Language.tr("CommandLine.Dialog.ParametersForThisCommand");
		CommandLineDialog.labelResults=Language.tr("CommandLine.Dialog.Results");

		/* Model changer */
		ModelChanger.XML_ELEMENT_MODES=new String[]{
				Language.tr("Batch.Parameter.ChangeType.Number"),
				Language.tr("Batch.Parameter.ChangeType.Mean"),
				Language.tr("Batch.Parameter.ChangeType.StdDev"),
				Language.tr("Batch.Parameter.ChangeType.DistributionParameter1"),
				Language.tr("Batch.Parameter.ChangeType.DistributionParameter2"),
				Language.tr("Batch.Parameter.ChangeType.DistributionParameter3"),
				Language.tr("Batch.Parameter.ChangeType.DistributionParameter4")
		};

		/* Special-Panel (Basis) */
		SpecialPanel.buttonClose=Language.tr("Dialog.Button.Close");
		SpecialPanel.buttonCloseHint=Language.tr("Dialog.Button.Close.Hint");

		/* Animations-Icons */
		AnimationImageSource.iconNameAttach=Language.tr("Animation.Icon.Attach");
		AnimationImageSource.iconNameBook=Language.tr("Animation.Icon.Book");
		AnimationImageSource.iconNameBug=Language.tr("Animation.Icon.Bug");
		AnimationImageSource.iconNameCD=Language.tr("Animation.Icon.CD");
		AnimationImageSource.iconNameCake=Language.tr("Animation.Icon.Cake");
		AnimationImageSource.iconNameCar=Language.tr("Animation.Icon.Car");
		AnimationImageSource.iconNameCart=Language.tr("Animation.Icon.Cart");
		AnimationImageSource.iconNameClock=Language.tr("Animation.Icon.Clock");
		AnimationImageSource.iconNameComputer=Language.tr("Animation.Icon.Computer");
		AnimationImageSource.iconNameDatabase=Language.tr("Animation.Icon.Database");
		AnimationImageSource.iconNameDisk=Language.tr("Animation.Icon.Disk");
		AnimationImageSource.iconNameLetter=Language.tr("Animation.Icon.Letter");
		AnimationImageSource.iconNameSmiley=Language.tr("Animation.Icon.Smiley");
		AnimationImageSource.iconNameSymbolFemale=Language.tr("Animation.Icon.SymbolFemale");
		AnimationImageSource.iconNameSymbolMale=Language.tr("Animation.Icon.SymbolMale");
		AnimationImageSource.iconNameFlagBlue=Language.tr("Animation.Icon.FlagBlue");
		AnimationImageSource.iconNameFlagGreen=Language.tr("Animation.Icon.FlagGreen");
		AnimationImageSource.iconNameFlagOrange=Language.tr("Animation.Icon.FlagOrange");
		AnimationImageSource.iconNameFlagPink=Language.tr("Animation.Icon.FlagPink");
		AnimationImageSource.iconNameFlagPurple=Language.tr("Animation.Icon.FlagPurple");
		AnimationImageSource.iconNameFlagRed=Language.tr("Animation.Icon.FlagRed");
		AnimationImageSource.iconNameFlagYellow=Language.tr("Animation.Icon.FlagYellow");
		AnimationImageSource.iconNameHeart=Language.tr("Animation.Icon.Heart");
		AnimationImageSource.iconNameKey=Language.tr("Animation.Icon.Key");
		AnimationImageSource.iconNameLorry=Language.tr("Animation.Icon.Lorry");
		AnimationImageSource.iconNameLorryLeft=Language.tr("Animation.Icon.LorryLeft");
		AnimationImageSource.iconNameLorryEmpty=Language.tr("Animation.Icon.LorryEmpty");
		AnimationImageSource.iconNameLorryLeftEmpty=Language.tr("Animation.Icon.LorryLeftEmpty");
		AnimationImageSource.iconNameMoneyDollar=Language.tr("Animation.Icon.MoneyDollar");
		AnimationImageSource.iconNameMoneyEuro=Language.tr("Animation.Icon.MoneyEuro");
		AnimationImageSource.iconNameMoneyPound=Language.tr("Animation.Icon.MoneyPound");
		AnimationImageSource.iconNameMoneyYen=Language.tr("Animation.Icon.MoneyYen");
		AnimationImageSource.iconNameMusic=Language.tr("Animation.Icon.Music");
		AnimationImageSource.iconNameDocument=Language.tr("Animation.Icon.Document");
		AnimationImageSource.iconNameStar=Language.tr("Animation.Icon.Star");
		AnimationImageSource.iconNamePersonGray=Language.tr("Animation.Icon.PersonGray");
		AnimationImageSource.iconNamePersonGreen=Language.tr("Animation.Icon.PersonGreen");
		AnimationImageSource.iconNamePersonOrange=Language.tr("Animation.Icon.PersonOrange");
		AnimationImageSource.iconNamePersonRed=Language.tr("Animation.Icon.PersonRed");
		AnimationImageSource.iconNamePersonSuit=Language.tr("Animation.Icon.PersonSuit");
		AnimationImageSource.iconNamePersonBlue=Language.tr("Animation.Icon.PersonBlue");
		AnimationImageSource.iconNameNote=Language.tr("Animation.Icon.Note");
		AnimationImageSource.iconNamePackage=Language.tr("Animation.Icon.Package");
		AnimationImageSource.iconNameClouds=Language.tr("Animation.Icon.Clouds");
		AnimationImageSource.iconNameLightning=Language.tr("Animation.Icon.Lightning");
		AnimationImageSource.iconNameSun=Language.tr("Animation.Icon.Sun");
		AnimationImageSource.iconNameWorld=Language.tr("Animation.Icon.World");
		AnimationImageSource.iconNameBallRed=Language.tr("Animation.Icon.BallRed");
		AnimationImageSource.iconNameBallBlue=Language.tr("Animation.Icon.BallBlue");
		AnimationImageSource.iconNameBallYellow=Language.tr("Animation.Icon.BallYellow");
		AnimationImageSource.iconNameBallGreen=Language.tr("Animation.Icon.BallGreen");
		AnimationImageSource.iconNameBallBlack=Language.tr("Animation.Icon.BallBlack");
		AnimationImageSource.iconNameBallWhite=Language.tr("Animation.Icon.BallWhite");
		AnimationImageSource.iconNameBallOrange=Language.tr("Animation.Icon.BallOrange");
		AnimationImageSource.iconNameBallGray=Language.tr("Animation.Icon.BallGray");
		AnimationImageSource.iconNameCog=Language.tr("Animation.Icon.Cog");
		AnimationImageSource.iconNameEye=Language.tr("Animation.Icon.Eye");
		AnimationImageSource.iconNameHouse=Language.tr("Animation.Icon.House");
		AnimationImageSource.iconNameBricks=Language.tr("Animation.Icon.Bricks");
		AnimationImageSource.iconNameOperator=Language.tr("Animation.Icon.Operator");
		AnimationImageSource.iconNameColorRed=Language.tr("Animation.Icon.ColorRed");
		AnimationImageSource.iconNameColorBlue=Language.tr("Animation.Icon.ColorBlue");
		AnimationImageSource.iconNameColorYellow=Language.tr("Animation.Icon.ColorYelow");
		AnimationImageSource.iconNameColorGreen=Language.tr("Animation.Icon.ColorGreen");
		AnimationImageSource.iconNameColorBlack=Language.tr("Animation.Icon.ColorBlack");
		AnimationImageSource.iconNameColorWhite=Language.tr("Animation.Icon.ColorWhite");
		AnimationImageSource.iconNameColorOrange=Language.tr("Animation.Icon.ColorOrange");
		AnimationImageSource.iconNameColorGray=Language.tr("Animation.Icon.ColorGray");
		AnimationImageSource.iconNameArrowDown=Language.tr("Animation.Icon.ArrowDown");
		AnimationImageSource.iconNameArrowLeft=Language.tr("Animation.Icon.ArrowLeft");
		AnimationImageSource.iconNameArrowRight=Language.tr("Animation.Icon.ArrowRight");
		AnimationImageSource.iconNameArrowUp=Language.tr("Animation.Icon.ArrowUp");
		AnimationImageSource.iconNameQuestionmark=Language.tr("Animation.Icon.Questionmark");
		AnimationImageSource.iconNameExclamationmark=Language.tr("Animation.Icon.Exclamationmark");
		AnimationImageSource.iconNameFolder=Language.tr("Animation.Icon.Folder");
		AnimationImageSource.iconNameFolderBlue=Language.tr("Animation.Icon.FolderBlue");
		AnimationImageSource.iconNameFolderGreen=Language.tr("Animation.Icon.FolderGreen");
		AnimationImageSource.iconNameFolderOrange=Language.tr("Animation.Icon.FolderOrange");
		AnimationImageSource.iconNameFolderPink=Language.tr("Animation.Icon.FolderPink");
		AnimationImageSource.iconNameFolderPurple=Language.tr("Animation.Icon.FolderPurple");
		AnimationImageSource.iconNameFolderRed=Language.tr("Animation.Icon.FolderRed");
		AnimationImageSource.iconNameCharacter=Language.tr("Animation.Icon.Character");
		AnimationImageSource.iconNameDigit=Language.tr("Animation.Icon.Digit");
		AnimationImageSource.iconNameAnimalDog=Language.tr("Animation.Icon.Dog");
		AnimationImageSource.iconNameAnimalMonkey=Language.tr("Animation.Icon.Monkey");
		AnimationImageSource.iconNameAnimalPenguin=Language.tr("Animation.Icon.Penguin");
		AnimationImageSource.iconNameAnimalCat=Language.tr("Animation.Icon.Cat");
		AnimationImageSource.iconNameBattery=Language.tr("Animation.Icon.Battery");
		AnimationImageSource.iconNameBell=Language.tr("Animation.Icon.Bell");
		AnimationImageSource.iconNameBriefcase=Language.tr("Animation.Icon.Briefcase");
		AnimationImageSource.iconNameCandle=Language.tr("Animation.Icon.Candle");
		AnimationImageSource.iconNameTaxi=Language.tr("Animation.Icon.Taxi");
		AnimationImageSource.iconNameFlask=Language.tr("Animation.Icon.Flask");
		AnimationImageSource.iconNameFlower=Language.tr("Animation.Icon.Flower");
		AnimationImageSource.iconNameHamburger=Language.tr("Animation.Icon.Hamburger");
		AnimationImageSource.iconNameHand=Language.tr("Animation.Icon.Hand");
		AnimationImageSource.iconNameJar=Language.tr("Animation.Icon.Jar");
		AnimationImageSource.iconNameLeaf=Language.tr("Animation.Icon.Leaf");
		AnimationImageSource.iconNameLifebuoy=Language.tr("Animation.Icon.Lifebuoy");
		AnimationImageSource.iconNameLightbulbOff=Language.tr("Animation.Icon.LightbulbOff");
		AnimationImageSource.iconNameLightbuldOn=Language.tr("Animation.Icon.LightbulbOn");
		AnimationImageSource.iconNameLollipop=Language.tr("Animation.Icon.Lollipop");
		AnimationImageSource.iconNamePalette=Language.tr("Animation.Icon.Palette");
		AnimationImageSource.iconNamePin=Language.tr("Animation.Icon.Pin");
		AnimationImageSource.iconNameSoccer=Language.tr("Animation.Icon.Soccer");
		AnimationImageSource.iconNameToolbox=Language.tr("Animation.Icon.Toolbox");
		AnimationImageSource.iconNameTrafficCone=Language.tr("Animation.Icon.TrafficCone");
		AnimationImageSource.iconNameUmbrella=Language.tr("Animation.Icon.Umbrella");
		AnimationImageSource.iconNameWaterDrop=Language.tr("Animation.Icon.WaterDrop");
		ModelAnimationImages.XML_NODE_NAME=Language.trAll("Animation.XML.RootName");

		AnimationImageSource.initIconsMap();

		/* Linientypen */
		ComplexLine.LINE_TYPE_NAMES=Language.tr("Surface.LineTypes").split("\\n");

		/* Surface */
		ModelSurface.XML_NODE_NAME=Language.trAll("Surface.XML.RootName.Model");
		ModelClientData.XML_NODE_NAME=Language.trAll("Surface.XML.RootName.ClientData");
		ModelResources.XML_NODE_NAME=Language.trAll("Surface.XML.RootName.Resources");
		ModelResource.XML_NODE_NAME=Language.trAll("Surface.XML.RootName.Resource");
		ModelResourceFailure.XML_NODE_NAME=Language.trAll("Surface.XML.RootName.ResourceFailure");
		ModelSchedules.XML_NODE_NAME=Language.trAll("Surface.XML.RootName.Schedules");
		ModelSchedule.XML_NODE_NAME=Language.trAll("Surface.XML.RootName.Schedule");
		ModelLongRunStatistics.XML_NODE_NAME=Language.trAll("Surface.XML.RootName.AdditionalStatistics");
		ModelLongRunStatistics.XML_NODE_STEPWIDE_ATTR=Language.trAll("Surface.XML.RootName.AdditionalStatistics.StepWide");
		ModelLongRunStatistics.XML_NODE_CLOSELASTINTERVAL_ATTR=Language.trAll("Surface.XML.RootName.AdditionalStatistics.LastInterval");
		ModelLongRunStatisticsElement.XML_NODE_NAME=Language.trAll("Surface.XML.RootName.AdditionalStatistics.Element");
		ModelSequenceStep.XML_NODE_NAME=Language.trAll("Surface.XML.RootName.SequenceStep");
		ModelSequence.XML_NODE_NAME=Language.trAll("Surface.XML.RootName.Sequence");
		ModelSequences.XML_NODE_NAME=Language.trAll("Surface.XML.RootName.Sequences");
		ModelTransporters.XML_NODE_NAME=Language.trAll("Surface.XML.RootName.Transporters");
		ModelTransporter.XML_NODE_NAME=Language.trAll("Surface.XML.RootName.Transporter");
		ModelTransporterFailure.XML_NODE_NAME=Language.trAll("Surface.XML.RootName.TransporterFailure");
		ModelPaths.XML_NODE_NAME=Language.trAll("Surface.XML.RootName.PathSegments");
		ModelLoadData.XML_NODE_NAME=Language.trAll("Surface.XML.RootName.LoadData");
		SavedViews.XML_NODE_NAME=Language.trAll("Surface.XML.RootName.SavedView");
		ExpressionCalcModelUserFunctions.XML_NODE_NAME=Language.trAll("Surface.XML.RootName.UserFunctions");

		/* Statistik */
		StatisticsSimpleCountPerformanceIndicator.xmlNameCount=Language.trAll("Statistics.XML.Count");
		StatisticsSimpleCountPerformanceIndicator.xmlNameCountError=Language.tr("Statistics.XML.Count.Error");
		StatisticsSimpleCountPerformanceIndicator.xmlNamePart=Language.trAll("Statistics.XML.Part");
		StatisticsSimpleValuePerformanceIndicator.xmlNameValue=Language.trAll("Statistics.XML.Value");
		StatisticsSimpleValuePerformanceIndicator.xmlNameValueError=Language.tr("Statistics.XML.Value.Error");
		StatisticsCountPerformanceIndicator.xmlNameCount=Language.trAll("Statistics.XML.Count");
		StatisticsCountPerformanceIndicator.xmlNameCountError=Language.tr("Statistics.XML.Count.Error");
		StatisticsCountPerformanceIndicator.xmlNameSuccessCount=Language.trAll("Statistics.XML.CountSuccess");
		StatisticsCountPerformanceIndicator.xmlNameSuccessCountError=Language.tr("Statistics.XML.CountSuccess.Error");
		StatisticsCountPerformanceIndicator.xmlNameSuccessPart=Language.tr("Statistics.XML.PartSuccess");
		StatisticsDataPerformanceIndicator.xmlNameCount=Language.trAll("Statistics.XML.Count");
		StatisticsDataPerformanceIndicator.xmlNameCountError=Language.tr("Statistics.XML.Count.Error");
		StatisticsDataPerformanceIndicator.xmlNameSum=Language.trAll("Statistics.XML.Sum");
		StatisticsDataPerformanceIndicator.xmlNameSumError=Language.tr("Statistics.XML.Sum.Error");
		StatisticsDataPerformanceIndicator.xmlNameSumSquared=Language.trAll("Statistics.XML.Sum2");
		StatisticsDataPerformanceIndicator.xmlNameSumSquaredError=Language.tr("Statistics.XML.Sum2.Error");
		StatisticsDataPerformanceIndicator.xmlNameSumCubic=Language.trAll("Statistics.XML.Sum3");
		StatisticsDataPerformanceIndicator.xmlNameSumCubicError=Language.tr("Statistics.XML.Sum3.Error");
		StatisticsDataPerformanceIndicator.xmlNameSumQuartic=Language.trAll("Statistics.XML.Sum4");
		StatisticsDataPerformanceIndicator.xmlNameSumQuarticError=Language.tr("Statistics.XML.Sum4.Error");
		StatisticsDataPerformanceIndicator.xmlNameMean=Language.trAll("Statistics.XML.Mean");
		StatisticsDataPerformanceIndicator.xmlNameSD=Language.trAll("Statistics.XML.StdDev");
		StatisticsDataPerformanceIndicator.xmlNameCV=Language.trAll("Statistics.XML.CV");
		StatisticsDataPerformanceIndicator.xmlNameSk=Language.trAll("Statistics.XML.Sk");
		StatisticsDataPerformanceIndicator.xmlNameKurt=Language.trAll("Statistics.XML.Kurt");
		StatisticsDataPerformanceIndicator.xmlNameMin=Language.trAll("Statistics.XML.Minimum");
		StatisticsDataPerformanceIndicator.xmlNameMinError=Language.tr("Statistics.XML.Minimum.Error");
		StatisticsDataPerformanceIndicator.xmlNameMax=Language.trAll("Statistics.XML.Maximum");
		StatisticsDataPerformanceIndicator.xmlNameMaxError=Language.tr("Statistics.XML.Maximum.Error");
		StatisticsDataPerformanceIndicator.xmlNameDistribution=Language.trAll("Statistics.XML.Distribution");
		StatisticsDataPerformanceIndicator.xmlNameDistributionError=Language.tr("Statistics.XML.Distribution.Error");
		StatisticsDataPerformanceIndicator.xmlNameCorrelationError=Language.tr("Statistics.XML.AutocorrelationData.Error");
		StatisticsDataPerformanceIndicator.xmlNameCorrelation=Language.trAll("Statistics.XML.Autocorrelation");
		StatisticsDataPerformanceIndicator.xmlNameBatchSize=Language.trAll("Statistics.XML.BatchSize");
		StatisticsDataPerformanceIndicator.xmlNameBatchSizeError=Language.tr("Statistics.XML.BatchSize.Error");
		StatisticsDataPerformanceIndicator.xmlNameBatchCount=Language.trAll("Statistics.XML.BatchCount");
		StatisticsDataPerformanceIndicator.xmlNameBatchCountError=Language.tr("Statistics.XML.BatchCount.Error");
		StatisticsDataPerformanceIndicator.xmlNameBatchMeansVar=Language.trAll("Statistics.XML.BatchMeans");
		StatisticsDataPerformanceIndicator.xmlNameBatchMeansVarError=Language.tr("Statistics.XML.BatchMeans.Error");
		StatisticsDataPerformanceIndicator.xmlNameMeanBatchHalfWide=Language.trAll("Statistics.XML.MeanBatchHalfWide");
		StatisticsDataPerformanceIndicator.xmlNameRunCount=Language.trAll("Statistics.XML.RunCount");
		StatisticsDataPerformanceIndicator.xmlNameRunCountError=Language.tr("Statistics.XML.RunCount.Error");
		StatisticsDataPerformanceIndicator.xmlNameRunVar=Language.trAll("Statistics.XML.Run");
		StatisticsDataPerformanceIndicator.xmlNameRunVarError=Language.tr("Statistics.XML.Run.Error");
		StatisticsDataPerformanceIndicator.xmlNameRunHalfWide=Language.trAll("Statistics.XML.RunHalfWide");
		StatisticsDataPerformanceIndicator.xmlNameQuantil=Language.tr("Statistics.XML.Quantil");
		StatisticsDataPerformanceIndicator.xmlNameQuantilLimit=Language.trAll("Statistics.XML.QuantilLimit");
		StatisticsDataPerformanceIndicator.xmlNameWelfordM2=Language.trAll("Statistics.XML.WelfordM2");
		StatisticsDataPerformanceIndicator.xmlNameWelfordM2Error=Language.tr("Statistics.XML.WelfordM2.Error");
		StatisticsDataPerformanceIndicatorWithNegativeValues.xmlNameCount=Language.trAll("Statistics.XML.Count");
		StatisticsDataPerformanceIndicatorWithNegativeValues.xmlNameCountError=Language.tr("Statistics.XML.Count.Error");
		StatisticsDataPerformanceIndicatorWithNegativeValues.xmlNameSum=Language.trAll("Statistics.XML.Sum");
		StatisticsDataPerformanceIndicatorWithNegativeValues.xmlNameSumError=Language.tr("Statistics.XML.Sum.Error");
		StatisticsDataPerformanceIndicatorWithNegativeValues.xmlNameSumSquared=Language.trAll("Statistics.XML.Sum2");
		StatisticsDataPerformanceIndicatorWithNegativeValues.xmlNameSumSquaredError=Language.tr("Statistics.XML.Sum2.Error");
		StatisticsDataPerformanceIndicatorWithNegativeValues.xmlNameSumCubic=Language.trAll("Statistics.XML.Sum3");
		StatisticsDataPerformanceIndicatorWithNegativeValues.xmlNameSumCubicError=Language.tr("Statistics.XML.Sum3.Error");
		StatisticsDataPerformanceIndicatorWithNegativeValues.xmlNameSumQuartic=Language.trAll("Statistics.XML.Sum4");
		StatisticsDataPerformanceIndicatorWithNegativeValues.xmlNameSumQuarticError=Language.tr("Statistics.XML.Sum4.Error");
		StatisticsDataPerformanceIndicatorWithNegativeValues.xmlNameMean=Language.trAll("Statistics.XML.Mean");
		StatisticsDataPerformanceIndicatorWithNegativeValues.xmlNameSD=Language.trAll("Statistics.XML.StdDev");
		StatisticsDataPerformanceIndicatorWithNegativeValues.xmlNameCV=Language.trAll("Statistics.XML.CV");
		StatisticsDataPerformanceIndicatorWithNegativeValues.xmlNameSk=Language.trAll("Statistics.XML.Sk");
		StatisticsDataPerformanceIndicatorWithNegativeValues.xmlNameKurt=Language.trAll("Statistics.XML.Kurt");
		StatisticsDataPerformanceIndicatorWithNegativeValues.xmlNameMin=Language.trAll("Statistics.XML.Minimum");
		StatisticsDataPerformanceIndicatorWithNegativeValues.xmlNameMinError=Language.tr("Statistics.XML.Minimum.Error");
		StatisticsDataPerformanceIndicatorWithNegativeValues.xmlNameMax=Language.trAll("Statistics.XML.Maximum");
		StatisticsDataPerformanceIndicatorWithNegativeValues.xmlNameMaxError=Language.tr("Statistics.XML.Maximum.Error");
		StatisticsDataPerformanceIndicatorWithNegativeValues.xmlNameDistribution=Language.trAll("Statistics.XML.Distribution");
		StatisticsDataPerformanceIndicatorWithNegativeValues.xmlNameDistributionError=Language.tr("Statistics.XML.Distribution.Error");
		StatisticsDataPerformanceIndicatorWithNegativeValues.xmlNameQuantil=Language.tr("Statistics.XML.Quantil");
		StatisticsDataPerformanceIndicatorWithNegativeValues.xmlNameQuantilLimit=Language.trAll("Statistics.XML.QuantilLimit");
		StatisticsDataPerformanceIndicatorWithNegativeValues.xmlNameWelfordM2=Language.trAll("Statistics.XML.WelfordM2");
		StatisticsDataPerformanceIndicatorWithNegativeValues.xmlNameWelfordM2Error=Language.tr("Statistics.XML.WelfordM2.Error");
		StatisticsMultiPerformanceIndicator.xmlTypeName=Language.trAll("Statistics.XML.Type");
		StatisticsMultiPerformanceIndicator.xmlInternalError=Language.tr("Statistics.XML.InternalError");
		StatisticsSimulationBaseData.xmlNameRunDate=Language.trAll("Statistics.XML.RunDate");
		StatisticsSimulationBaseData.xmlNameRunTime=Language.trAll("Statistics.XML.RunTime");
		StatisticsSimulationBaseData.xmlNameRunTimeError=Language.tr("Statistics.XML.RunTime.Error");
		StatisticsSimulationBaseData.xmlNameRunOS=Language.trAll("Statistics.XML.RunOS");
		StatisticsSimulationBaseData.xmlNameRunUser=Language.trAll("Statistics.XML.RunUser");
		StatisticsSimulationBaseData.xmlNameRunThreads=Language.trAll("Statistics.XML.RunThreads");
		StatisticsSimulationBaseData.xmlNameNUMA=Language.trAll("Statistics.XML.RunThreads.NUMA");
		StatisticsSimulationBaseData.xmlNameDynamicBalance=Language.trAll("Statistics.XML.RunThreads.DynamicBalance");
		StatisticsSimulationBaseData.xmlNameDynamicBalanceData=Language.trAll("Statistics.XML.RunThreads.DynamicBalanceData");
		StatisticsSimulationBaseData.xmlNameRunThreadTimes=Language.trAll("Statistics.XML.ThreadRunTimes");
		StatisticsSimulationBaseData.xmlNameRunThreadsError=Language.tr("Statistics.XML.RunThreads.Error");
		StatisticsSimulationBaseData.xmlNameRunEvents=Language.trAll("Statistics.XML.RunEvents");
		StatisticsSimulationBaseData.xmlNameRunEventsError=Language.tr("Statistics.XML.RunEvents.Error");
		StatisticsSimulationBaseData.xmlNameRunRepeatCount=Language.trAll("Statistics.XML.RunRepeatCount");
		StatisticsSimulationBaseData.xmlNameRunRepeatCountError=Language.tr("Statistics.XML.RunRepeatCount.Error");
		StatisticsSimulationBaseData.xmlNameEmergencyShutDown=Language.trAll("Statistics.XML.EmergencyShutDown");
		StatisticsSimulationBaseData.xmlNameWarning=Language.trAll("Statistics.XML.Warning");
		StatisticsTimePerformanceIndicator.xmlLoadError=Language.tr("Statistics.XML.Disribution.ElementError");
		StatisticsTimePerformanceIndicator.xmlNameStart=Language.trAll("Statistics.XML.Start");
		StatisticsTimePerformanceIndicator.xmlNameStartError=Language.tr("Statistics.XML.StartError");
		StatisticsTimePerformanceIndicator.xmlNameSum=Language.tr("Statistics.XML.Sum");
		StatisticsTimePerformanceIndicator.xmlNameValues=Language.trAll("Statistics.XML.Values");
		StatisticsTimePerformanceIndicator.xmlNameValuesError=Language.tr("Statistics.XML.ValuesError");
		StatisticsTimePerformanceIndicator.xmlNameValuesSquared=Language.trAll("Statistics.XML.ValuesSquared");
		StatisticsTimePerformanceIndicator.xmlNameValuesSquaredError=Language.tr("Statistics.XML.ValuesSquaredError");
		StatisticsTimePerformanceIndicator.xmlNameValuesCubic=Language.trAll("Statistics.XML.Sum3");
		StatisticsTimePerformanceIndicator.xmlNameValuesCubicError=Language.tr("Statistics.XML.Sum3.Error");
		StatisticsTimePerformanceIndicator.xmlNameValuesQuartic=Language.trAll("Statistics.XML.Sum4");
		StatisticsTimePerformanceIndicator.xmlNameValuesQuarticError=Language.tr("Statistics.XML.Sum4.Error");
		StatisticsTimePerformanceIndicator.xmlNameMean=Language.tr("Statistics.XML.Mean");
		StatisticsTimePerformanceIndicator.xmlNameSD=Language.tr("Statistics.XML.StdDev");
		StatisticsTimePerformanceIndicator.xmlNameCV=Language.tr("Statistics.XML.CV");
		StatisticsTimePerformanceIndicator.xmlNameSk=Language.trAll("Statistics.XML.Sk");
		StatisticsTimePerformanceIndicator.xmlNameKurt=Language.trAll("Statistics.XML.Kurt");
		StatisticsTimePerformanceIndicator.xmlNameMin=Language.trAll("Statistics.XML.Minimum");
		StatisticsTimePerformanceIndicator.xmlNameMax=Language.trAll("Statistics.XML.Maximum");
		StatisticsTimePerformanceIndicator.xmlNameQuantil=Language.tr("Statistics.XML.Quantil");
		StatisticsValuePerformanceIndicator.xmlNameValue=Language.trAll("Statistics.XML.Value");
		StatisticsValuePerformanceIndicator.xmlNameValueError=Language.tr("Statistics.XML.Value.Error");
		StatisticsLongRunPerformanceIndicator.xmlLoadError=Language.tr("Statistics.XML.Disribution.ElementError");
		StatisticsLongRunPerformanceIndicator.xmlLoadStepWideError=Language.tr("Statistics.XML.Disribution.StepWideError");
		StatisticsLongRunPerformanceIndicator.xmlNameStep=Language.trAll("Statistics.XML.Disribution.StepWide");
		StatisticsStateTimePerformanceIndicator.xmlChild=Language.trAll("Statistics.XML.StateTime");
		StatisticsStateTimePerformanceIndicator.xmlChildName=Language.trAll("Statistics.XML.StateTime.Name");
		StatisticsDataCollector.xmlDistributionError=Language.tr("Statistics.XML.Values.Error");
		StatisticsTimeAnalogPerformanceIndicator.xmlNameSum=Language.trAll("Statistics.XML.Sum");
		StatisticsTimeAnalogPerformanceIndicator.xmlNameSumError=Language.tr("Statistics.XML.Sum.Error");
		StatisticsTimeAnalogPerformanceIndicator.xmlNameTime=Language.trAll("Statistics.XML.Time");
		StatisticsTimeAnalogPerformanceIndicator.xmlNameTimeError=Language.tr("Statistics.XML.Time.Error");
		StatisticsTimeAnalogPerformanceIndicator.xmlNameMean=Language.trPrimary("Statistics.XML.Mean");
		StatisticsTimeAnalogPerformanceIndicator.xmlNameMin=Language.trAll("Statistics.XML.Minimum");
		StatisticsTimeAnalogPerformanceIndicator.xmlNameMinError=Language.tr("Statistics.XML.Minimum.Error");
		StatisticsTimeAnalogPerformanceIndicator.xmlNameMax=Language.trAll("Statistics.XML.Maximum");
		StatisticsTimeAnalogPerformanceIndicator.xmlNameMaxError=Language.tr("Statistics.XML.Maximum.Error");
		StatisticsQuotientPerformanceIndicator.xmlNameNumerator=Language.trAll("Statistics.XML.Numerator");
		StatisticsQuotientPerformanceIndicator.xmlNameDenominator=Language.trAll("Statistics.XML.Denominator");
		StatisticsQuotientPerformanceIndicator.xmlNameQuotient=Language.trAll("Statistics.XML.Quotient");
		StatisticsQuotientPerformanceIndicator.xmlNameNumeratorError=Language.tr("Statistics.XML.Numerator.Error");
		StatisticsQuotientPerformanceIndicator.xmlNameDenominatorError=Language.tr("Statistics.XML.Denominator.Error");

		/* Filter */
		JSCommandXML.updateLanguage();
		StatisticsImpl.updateLanguage();

		/* Netzwerk */
		NetServer.ERROT_START=Language.tr("Server.ErrorStart");
		NetServer.LOG_START=Language.tr("Server.Log.Start");
		NetServer.LOG_STOP=Language.tr("Server.Log.Stop");
		NetServer.LOG_CONNECTION_START=Language.tr("Server.Log.ConnectionStart");
		NetServer.LOG_CONNECTION_STOP=Language.tr("Server.Log.ConnectionStop");
		SimulationServer.SIMULATION_STARTED=Language.tr("Server.Log.SimulationStarted");
		SimulationServer.SIMULATION_FINISHED=Language.tr("Server.Log.SimulationFinished");
		SimulationServer.SIMULATION_FINISHED_SENDING=Language.tr("Server.Log.SimulationFinishedButSending");
		SimulationServer.SIMULATION_CANCELED=Language.tr("Server.Log.SimulationCanceled");
		SimulationServer.PREPARE_REJECTED_DUE_TO_OVERLOAD=Language.tr("Server.Log.ServerOverloaded");
		SimulationServer.PREPARE_NO_MODEL=Language.tr("Server.Log.PrepareNoModel");
		SimulationServer.PREPARE_NO_REMOTE_MODEL=Language.tr("Server.Log.PrepareNoRemoteModel");
		SimulationServer.PREPARE_VERSION_MISMATCH=Language.tr("Server.Log.PrepareVersionMismatch");
		SimulationClient.NO_CONNECT=Language.tr("Server.ErrorNoConnect");
		SimulationClient.ERROR_SENDING_MODEL=Language.tr("Server.ErrorSendingModel");
		SimulationClient.ERROR_ON_REMOTE_PREPARE=Language.tr("Server.ErrorServerPrepare");

		/* Datenbankeinstellungen */
		DBSettings.XML_NODE_NAME=Language.trAll("Surface.XML.Database.Root");

		/* Excel-DDE-Verbindung */
		try {
			DDEConnect.EXCEL_LANGUAGE_DEFAULT_ROW_IDENTIFIER=Language.tr("DDE.Excel.Identifier.Row");
			DDEConnect.EXCEL_LANGUAGE_DEFAULT_COL_IDENTIFIER=Language.tr("DDE.Excel.Identifier.Col");
		} catch (NoClassDefFoundError e) {} /* F�r Player */
	}
}