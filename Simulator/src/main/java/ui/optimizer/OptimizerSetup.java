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
package ui.optimizer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFileChooser;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import language.Language;
import mathtools.NumberTools;
import mathtools.distribution.swing.CommonVariables;
import simulator.editmodel.EditModel;
import ui.ModelChanger;
import ui.modeleditor.ModelResource;
import xml.XMLData;

/**
 * Diese Klasse hält die Einstellungen für einen Optimierungslauf vor.
 * @author Alexander Herzog
 */
public class OptimizerSetup extends XMLData implements Cloneable {
	/**
	 * Art des Wertes, der im <code>target</code>-Feld steht.
	 * @see OptimizerSetup#targetType
	 */
	public enum TargetType {
		/** Ist der Wert in dem <code>target</code>-Feld ein xml-Tag? */
		TARGET_TYPE_XML_TAG,

		/** Ist der Wert in dem <code>target</code>-Feld ein Skript, dessen Ausgabe als Zielgröße verwendet werden soll? */
		TARGET_TYPE_SCRIPT
	}

	/**
	 * Alle Statistikdaten (Zwischenergebnisse) speichern oder nur die Daten für den finalen Optimierungslauf
	 * @see OptimizerSetup#outputMode
	 */
	public enum OutputMode{
		/** Alle Statistikdaten (inkl. Ergebnissen zu Zwischenschritten bei der Optimierung) speichern */
		OUTPUT_ALL,
		/** Nur die Statistikdaten für den finalen Optimierungslauf speichern */
		OUTPUT_LAST
	}

	/**
	 * Liste der Variablen, die während der Optimierung verändert werden können.
	 */
	public List<ControlVariable> controlVariables;

	/**
	 * Zusätzliche optionale Nebenbedingungen die erfüllt werden müssen.
	 */
	public List<String> controlVariableConstrains;

	/**
	 * Die Auswertung dieses Felds (entweder xml-Tag oder Skript) ergibt die Zielgröße.
	 */
	public String target;

	/**
	 * Art des Wertes, der im <code>target</code>-Feld steht.
	 * @see TargetType
	 */
	public TargetType targetType;

	/**
	 * Gibt an, was für die Zielgröße wünschenswert ist:
	 * -1 = Zielgröße soll so klein wie möglich sein
	 * 0 = Zielgröße soll sich in dem Bereich <code>targetRangeMin</code> bis <code>targetRangeMax</code> bewegen
	 * 1 = Zielgröße soll so groß wie möglich sein
	 * @see #targetRangeMin
	 * @see #targetRangeMax
	 */
	public int targetDirection;

	/**
	 * Ist <code>targetDirection=0</code> gewählt, so gibt dieses Feld die untere
	 * Grenze für den Zielbereich an.
	 * @see #targetDirection
	 */
	public double targetRangeMin;

	/**
	 * Ist <code>targetDirection=0</code> gewählt, so gibt dieses Feld die obere
	 * Grenze für den Zielbereich an.
	 * @see #targetDirection
	 */
	public double targetRangeMax;

	/**
	 * Ausgabeverzeichnis für die Statistikdateien
	 */
	public String outputFolder;

	/**
	 * Alle Statistikdaten (Zwischenergebnisse) speichern oder nur die Daten für den finalen Optimierungslauf
	 */
	public OutputMode outputMode;

	/**
	 * Zu verwendende Optimiererstrategie
	 */
	public String optimizerName;

	/**
	 * (Für serielle Algorithmen)
	 * Maximale Änderungsrate im ersten Schritt
	 */
	public double serialChangeSpeed1;

	/**
	 * (Für serielle Algorithmen)<br>
	 * Maximale Änderungsrate im zweitenSchritt
	 */
	public double serialChangeSpeed2;

	/**
	 * (Für serielle Algorithmen)<br>
	 * Maximale Änderungsrate im dritten Schritt
	 */
	public double serialChangeSpeed3;

	/**
	 * (Für serielle Algorithmen)<br>
	 * Maximale Änderungsrate ab dem vierten Schritt
	 */
	public double serialChangeSpeed4;

	/**
	 * (Für serielle Algorithmen)<br>
	 * In den ersten Schritten (mit abnehmender Wahrscheinlichkeit) auch Verschlechterungen zulassen?
	 */
	public boolean serialSimulatedAnnealing;

	/**
	 * (Für genetischen Algorithmus)<br>
	 * Anzahl an Modellen, die parallel simuliert werden sollen
	 */
	public int geneticPopulationSize;

	/**
	 * (Für genetischen Algorithmus)<br>
	 * Gibt an, welcher Anteil an Modellen pro Runde verworfen werden soll (Werte nahe 1: viele Modelle verwerfen, Werte nahe 0: fast alle Modelle in nächste Runde übernehmen)
	 */
	public double geneticEvolutionPressure;

	/**
	 * (Für genetischen Algorithmus)<br>
	 * Maximale Änderungsrate im ersten und zweiten Schritt
	 */
	public double geneticChangeSpeed1;

	/**
	 * (Für genetischen Algorithmus)<br>
	 * Maximale Änderungsrate im dritten und vierten Schritt
	 */
	public double geneticChangeSpeed2;

	/**
	 * (Für genetischen Algorithmus)<br>
	 * Maximale Änderungsrate im fünften und sechsten Schritt
	 */
	public double geneticChangeSpeed3;

	/**
	 * (Für genetischen Algorithmus)<br>
	 * Maximale Änderungsrate im siebten und achtenSchritt
	 */
	public double geneticChangeSpeed4;

	/**
	 * (Für genetischen Algorithmus)<br>
	 * Maximale Änderungsrate ab dem neunten Schritt
	 */
	public double geneticChangeSpeed5;

	/**
	 * Abbruch der einzelnen Simulationen nach einer bestimmten Anzahl an Sekunden<br>
	 * (oder Werte &le;0 für kein Timeout)
	 */
	public int timeoutSeconds;

	/**
	 * Konstruktor der Klasse <code>OptimizerSetup</code>
	 */
	public OptimizerSetup() {
		controlVariables=new ArrayList<>();
		controlVariableConstrains=new ArrayList<>();
		target="";
		targetType=TargetType.TARGET_TYPE_XML_TAG;
		targetDirection=0;
		targetRangeMin=0;
		targetRangeMax=10;

		File folder=CommonVariables.getCommonVariables().lastFileChooserDirectory;
		if (folder==null) folder=new JFileChooser().getFileSystemView().getDefaultDirectory();
		outputFolder=folder.toString();
		outputMode=OutputMode.OUTPUT_ALL;

		optimizerName=new OptimizerCatalog(null).getDetaultOptimizerName();

		serialChangeSpeed1=0.25;
		serialChangeSpeed2=0.1;
		serialChangeSpeed3=0.05;
		serialChangeSpeed4=0.025;
		serialSimulatedAnnealing=false;

		geneticPopulationSize=10;
		geneticEvolutionPressure=0.5;
		geneticChangeSpeed1=1;
		geneticChangeSpeed2=0.66;
		geneticChangeSpeed3=0.5;
		geneticChangeSpeed4=0.33;
		geneticChangeSpeed5=0.25;

		timeoutSeconds=-1;
	}

	/**
	 * Prüft, ob das aktuelle <code>OptimizerSetup</code>-Objekt inhaltlich mit einem anderen identisch ist.
	 * @param otherOptimizerSetup	Objekt vom Typ <code>OptimizerSetup</code>, welches mit diesem Objekt verglichen werden soll.
	 * @return	Gibt <code>true</code> zurück, wenn die beiden Objekte inhaltlich identisch sind.
	 */
	public boolean equalsOptimizerSetup(final OptimizerSetup otherOptimizerSetup) {
		if (otherOptimizerSetup==null) return false;

		if (!otherOptimizerSetup.target.equals(target)) return false;
		if (otherOptimizerSetup.targetType!=targetType) return false;
		if (otherOptimizerSetup.targetDirection!=targetDirection) return false;
		if (targetDirection==0) {
			if (otherOptimizerSetup.targetRangeMin!=targetRangeMin) return false;
			if (otherOptimizerSetup.targetRangeMax!=targetRangeMax) return false;
		}
		if (!otherOptimizerSetup.outputFolder.equals(outputFolder)) return false;
		if (otherOptimizerSetup.outputMode!=outputMode) return false;

		if (controlVariables==null || otherOptimizerSetup.controlVariables==null) return false;
		if (controlVariables.size()!=otherOptimizerSetup.controlVariables.size()) return false;
		for (int i=0;i<controlVariables.size();i++) {
			if (!otherOptimizerSetup.controlVariables.get(i).equalsControlVariable(controlVariables.get(i))) return false;
		}

		if (controlVariableConstrains==null || otherOptimizerSetup.controlVariableConstrains==null) return false;
		if (controlVariableConstrains.size()!=otherOptimizerSetup.controlVariableConstrains.size()) return false;
		for (int i=0;i<controlVariableConstrains.size();i++) {
			if (!otherOptimizerSetup.controlVariableConstrains.get(i).equalsIgnoreCase(controlVariableConstrains.get(i))) return false;
		}

		if (!optimizerName.equals(otherOptimizerSetup.optimizerName)) return false;

		if (serialChangeSpeed1!=otherOptimizerSetup.serialChangeSpeed1) return false;
		if (serialChangeSpeed2!=otherOptimizerSetup.serialChangeSpeed2) return false;
		if (serialChangeSpeed3!=otherOptimizerSetup.serialChangeSpeed3) return false;
		if (serialChangeSpeed4!=otherOptimizerSetup.serialChangeSpeed4) return false;
		if (serialSimulatedAnnealing!=otherOptimizerSetup.serialSimulatedAnnealing) return false;

		if (geneticPopulationSize!=otherOptimizerSetup.geneticPopulationSize) return false;
		if (geneticEvolutionPressure!=otherOptimizerSetup.geneticEvolutionPressure) return false;
		if (geneticChangeSpeed1!=otherOptimizerSetup.geneticChangeSpeed1) return false;
		if (geneticChangeSpeed2!=otherOptimizerSetup.geneticChangeSpeed2) return false;
		if (geneticChangeSpeed3!=otherOptimizerSetup.geneticChangeSpeed3) return false;
		if (geneticChangeSpeed4!=otherOptimizerSetup.geneticChangeSpeed4) return false;
		if (geneticChangeSpeed5!=otherOptimizerSetup.geneticChangeSpeed5) return false;

		if (timeoutSeconds!=otherOptimizerSetup.timeoutSeconds) return false;

		return true;
	}

	/**
	 * Erstellt eine Kopie des aktuellen <code>OptimizerSetup</code> Objekts.
	 * @return	Kopiertes Objekt
	 */
	@Override
	public OptimizerSetup clone() {
		OptimizerSetup clone;
		try {clone=(OptimizerSetup)super.clone();} catch (CloneNotSupportedException e) {return null;}

		clone.controlVariables=new ArrayList<>();
		for (ControlVariable controlVariable: controlVariables) clone.controlVariables.add(controlVariable.clone());

		clone.controlVariableConstrains=new ArrayList<>();
		for (String condition: controlVariableConstrains) clone.controlVariableConstrains.add(condition);

		clone.target=target;
		clone.targetType=targetType;
		clone.targetDirection=targetDirection;
		clone.targetRangeMin=targetRangeMin;
		clone.targetRangeMax=targetRangeMax;
		clone.outputFolder=outputFolder;
		clone.outputMode=outputMode;

		clone.optimizerName=optimizerName;

		clone.serialChangeSpeed1=serialChangeSpeed1;
		clone.serialChangeSpeed2=serialChangeSpeed2;
		clone.serialChangeSpeed3=serialChangeSpeed3;
		clone.serialChangeSpeed4=serialChangeSpeed4;
		clone.serialSimulatedAnnealing=serialSimulatedAnnealing;

		clone.geneticPopulationSize=geneticPopulationSize;
		clone.geneticEvolutionPressure=geneticEvolutionPressure;
		clone.geneticChangeSpeed1=geneticChangeSpeed1;
		clone.geneticChangeSpeed2=geneticChangeSpeed2;
		clone.geneticChangeSpeed3=geneticChangeSpeed3;
		clone.geneticChangeSpeed4=geneticChangeSpeed4;
		clone.geneticChangeSpeed5=geneticChangeSpeed5;

		clone.timeoutSeconds=timeoutSeconds;

		return clone;
	}

	@Override
	public String[] getRootNodeNames() {
		return Language.trAll("Optimizer.XML.Root");
	}

	@Override
	protected void addDataToXML(Document doc, Element node, boolean isPartOfOtherFile, final File file) {
		for (ControlVariable controlVariable: controlVariables) controlVariable.addDataToXML(doc,node);

		Element sub;

		for (String condition: controlVariableConstrains) {
			node.appendChild(sub=doc.createElement(Language.trPrimary("Optimizer.XML.ControlVariablesCondition")));
			sub.setTextContent(condition);
		}

		node.appendChild(sub=doc.createElement(Language.trPrimary("Optimizer.XML.Target")));
		sub.setTextContent(target);
		int targetTypeValue=0;
		switch (targetType) {
		case TARGET_TYPE_XML_TAG: targetTypeValue=0; break;
		case TARGET_TYPE_SCRIPT: targetTypeValue=1; break;
		}
		sub.setAttribute(Language.trPrimary("Optimizer.XML.Mode"),""+targetTypeValue);
		switch (targetDirection) {
		case -1:
			sub.setAttribute(Language.trPrimary("Optimizer.XML.Target.Direction"),Language.trPrimary("Optimizer.XML.Target.Direction.Minimize"));
			break;
		case 0:
			sub.setAttribute(Language.trPrimary("Optimizer.XML.Target.Direction"),Language.trPrimary("Optimizer.XML.Target.Direction.Range"));
			sub.setAttribute(Language.trPrimary("Optimizer.XML.RangeMinimum"),""+targetRangeMin);
			sub.setAttribute(Language.trPrimary("Optimizer.XML.RangeMaximum"),""+targetRangeMax);
			break;
		case 1:
			sub.setAttribute(Language.trPrimary("Optimizer.XML.Target.Direction"),Language.trPrimary("Optimizer.XML.Target.Direction.Maximize"));
			break;
		}

		node.appendChild(sub=doc.createElement(Language.trPrimary("Optimizer.XML.OutputFolder")));
		sub.setTextContent(outputFolder);
		switch (outputMode) {
		case OUTPUT_ALL: sub.setAttribute(Language.trPrimary("Optimizer.XML.OutputFolder.Mode"),Language.trPrimary("Optimizer.XML.OutputFolder.Mode.All")); break;
		case OUTPUT_LAST: sub.setAttribute(Language.trPrimary("Optimizer.XML.OutputFolder.Mode"),Language.trPrimary("Optimizer.XML.OutputFolder.Mode.Last")); break;
		}

		node.appendChild(sub=doc.createElement(Language.trPrimary("Optimizer.XML.Kernel")));
		sub.setTextContent(optimizerName);

		node.appendChild(sub=doc.createElement(Language.trPrimary("Optimizer.XML.Kernel.SerialSetup")));
		sub.setAttribute(Language.trPrimary("Optimizer.XML.Kernel.SerialSetup.ChangeSpeed1"),NumberTools.formatSystemNumber(serialChangeSpeed1));
		sub.setAttribute(Language.trPrimary("Optimizer.XML.Kernel.SerialSetup.ChangeSpeed2"),NumberTools.formatSystemNumber(serialChangeSpeed2));
		sub.setAttribute(Language.trPrimary("Optimizer.XML.Kernel.SerialSetup.ChangeSpeed3"),NumberTools.formatSystemNumber(serialChangeSpeed3));
		sub.setAttribute(Language.trPrimary("Optimizer.XML.Kernel.SerialSetup.ChangeSpeed4"),NumberTools.formatSystemNumber(serialChangeSpeed4));
		if (serialSimulatedAnnealing) sub.setAttribute(Language.trPrimary("Optimizer.XML.Kernel.SerialSetup.Annealing"),"1");

		node.appendChild(sub=doc.createElement(Language.trPrimary("Optimizer.XML.Kernel.GeneticSetup")));
		sub.setAttribute(Language.trPrimary("Optimizer.XML.Kernel.GeneticSetup.PopulationSize"),""+geneticPopulationSize);
		sub.setAttribute(Language.trPrimary("Optimizer.XML.Kernel.GeneticSetup.EvolutionPressure"),NumberTools.formatSystemNumber(geneticEvolutionPressure));
		sub.setAttribute(Language.trPrimary("Optimizer.XML.Kernel.GeneticSetup.ChangeSpeed1"),NumberTools.formatSystemNumber(geneticChangeSpeed1));
		sub.setAttribute(Language.trPrimary("Optimizer.XML.Kernel.GeneticSetup.ChangeSpeed2"),NumberTools.formatSystemNumber(geneticChangeSpeed2));
		sub.setAttribute(Language.trPrimary("Optimizer.XML.Kernel.GeneticSetup.ChangeSpeed3"),NumberTools.formatSystemNumber(geneticChangeSpeed3));
		sub.setAttribute(Language.trPrimary("Optimizer.XML.Kernel.GeneticSetup.ChangeSpeed4"),NumberTools.formatSystemNumber(geneticChangeSpeed4));
		sub.setAttribute(Language.trPrimary("Optimizer.XML.Kernel.GeneticSetup.ChangeSpeed5"),NumberTools.formatSystemNumber(geneticChangeSpeed5));

		if (timeoutSeconds>0) {
			node.appendChild(sub=doc.createElement(Language.trPrimary("Optimizer.XML.TimeoutSeconds")));
			sub.setTextContent(""+timeoutSeconds);
		}
	}

	@Override
	protected String loadProperty(String name, String text, Element node) {
		if (Language.trAll("Optimizer.XML.ControlVariables",name)) {
			ControlVariable controlVariable=new ControlVariable();
			final String error=controlVariable.loadFromXML(node);
			if (error!=null) return error;
			controlVariables.add(controlVariable);
			return null;
		}

		if (Language.trAll("Optimizer.XML.ControlVariablesCondition",name)) {
			controlVariableConstrains.add(text);
			return null;
		}

		Integer I;
		Double D;
		String s;

		if (Language.trAll("Optimizer.XML.Target",name)) {
			target=text;
			I=NumberTools.getInteger(Language.trAllAttribute("Optimizer.XML.Mode",node));
			if (I==null || I.intValue()<0 || I.intValue()>1) return String.format(Language.tr("Surface.XML.AttributeError"),Language.trPrimary("Optimizer.XML.Mode"),name)+" "+Language.tr("Optimizer.XMLError.ValueHasToBe0Or1");
			if (I.intValue()==0) targetType=TargetType.TARGET_TYPE_XML_TAG;
			if (I.intValue()==1) targetType=TargetType.TARGET_TYPE_SCRIPT;

			boolean directionOk=false;
			if (Language.trAll("Optimizer.XML.Target.Direction.Minimize",Language.trAllAttribute("Optimizer.XML.Target.Direction",node))) {
				targetDirection=-1;
				directionOk=true;
			}
			if (Language.trAll("Optimizer.XML.Target.Direction.Range",Language.trAllAttribute("Optimizer.XML.Target.Direction",node))) {
				targetDirection=0;
				D=NumberTools.getDouble(NumberTools.systemNumberToLocalNumber(Language.trAllAttribute("Optimizer.XML.RangeMinimum",node)));
				if (D==null) return String.format(Language.tr("Surface.XML.AttributeError"),Language.trPrimary("Optimizer.XML.RangeMinimum"),name)+" "+Language.tr("Optimizer.XMLError.ValueHasToBeNumber");
				targetRangeMin=D;
				D=NumberTools.getDouble(NumberTools.systemNumberToLocalNumber(Language.trAllAttribute("Optimizer.XML.RangeMaximum",node)));
				if (D==null || D<targetRangeMin) return String.format(Language.tr("Surface.XML.AttributeError"),Language.trPrimary("Optimizer.XML.RangeMaximum"),name)+" "+" "+Language.tr("Optimizer.XMLError.ValueHasToBeNumberAndLargerThanMinimum");
				targetRangeMax=D;
				directionOk=true;
			}
			if (Language.trAll("Optimizer.XML.Target.Direction.Maximize",Language.trAllAttribute("Optimizer.XML.Target.Direction",node))) {
				targetDirection=1;
				directionOk=true;
			}
			if (!directionOk) return String.format(Language.tr("Surface.XML.AttributeError"),Language.trPrimary("Optimizer.XML.Target.Direction"),name);
			return null;
		}

		if (Language.trAll("Optimizer.XML.OutputFolder",name)) {
			outputFolder=text;
			final String modeString=Language.trAllAttribute("Optimizer.XML.OutputFolder.Mode",node);
			if (Language.trAll("Optimizer.XML.OutputFolder.Mode.All",modeString)) outputMode=OutputMode.OUTPUT_ALL;
			if (Language.trAll("Optimizer.XML.OutputFolder.Mode.Last",modeString)) outputMode=OutputMode.OUTPUT_LAST;
			return null;
		}

		if (Language.trAll("Optimizer.XML.Kernel",name)) {
			optimizerName=new OptimizerCatalog(null).getCanonicalOptimizerName(text);
			return null;
		}

		if (Language.trAll("Optimizer.XML.Kernel.SerialSetup",name)) {
			s=Language.trAllAttribute("Optimizer.XML.Kernel.SerialSetup.ChangeSpeed1",node);
			if (!s.isBlank()) {
				D=NumberTools.getPositiveDouble(NumberTools.systemNumberToLocalNumber(s));
				if (D==0 || D.doubleValue()>1) return String.format(Language.tr("Surface.XML.AttributeError"),Language.trPrimary("Optimizer.XML.Kernel.SerialSetup.ChangeSpeed1"),name)+" "+" "+Language.tr("Optimizer.XMLError.ValueHasToBeNumberBetween0And1");
				serialChangeSpeed1=D.doubleValue();
			}
			s=Language.trAllAttribute("Optimizer.XML.Kernel.SerialSetup.ChangeSpeed2",node);
			if (!s.isBlank()) {
				D=NumberTools.getPositiveDouble(NumberTools.systemNumberToLocalNumber(s));
				if (D==0 || D.doubleValue()>1) return String.format(Language.tr("Surface.XML.AttributeError"),Language.trPrimary("Optimizer.XML.Kernel.SerialSetup.ChangeSpeed2"),name)+" "+" "+Language.tr("Optimizer.XMLError.ValueHasToBeNumberBetween0And1");
				serialChangeSpeed2=D.doubleValue();
			}
			s=Language.trAllAttribute("Optimizer.XML.Kernel.SerialSetup.ChangeSpeed3",node);
			if (!s.isBlank()) {
				D=NumberTools.getPositiveDouble(NumberTools.systemNumberToLocalNumber(s));
				if (D==0 || D.doubleValue()>1) return String.format(Language.tr("Surface.XML.AttributeError"),Language.trPrimary("Optimizer.XML.Kernel.SerialSetup.ChangeSpeed3"),name)+" "+" "+Language.tr("Optimizer.XMLError.ValueHasToBeNumberBetween0And1");
				serialChangeSpeed3=D.doubleValue();
			}
			s=Language.trAllAttribute("Optimizer.XML.Kernel.SerialSetup.ChangeSpeed4",node);
			if (!s.isBlank()) {
				D=NumberTools.getPositiveDouble(NumberTools.systemNumberToLocalNumber(s));
				if (D==0 || D.doubleValue()>1) return String.format(Language.tr("Surface.XML.AttributeError"),Language.trPrimary("Optimizer.XML.Kernel.SerialSetup.ChangeSpeed4"),name)+" "+" "+Language.tr("Optimizer.XMLError.ValueHasToBeNumberBetween0And1");
				serialChangeSpeed4=D.doubleValue();
			}
			s=Language.trAllAttribute("Optimizer.XML.Kernel.SerialSetup.Annealing",node);
			if (!s.isBlank() && !s.equals("0")) serialSimulatedAnnealing=true;
			return null;
		}

		if (Language.trAll("Optimizer.XML.Kernel.SerialSetup",name)) {
			s=Language.trAllAttribute("Optimizer.XML.Kernel.GeneticSetup.PopulationSize",node);
			if (!s.isBlank()) {
				final Long L=NumberTools.getPositiveLong(s);
				if (L==0) return String.format(Language.tr("Surface.XML.AttributeError"),Language.trPrimary("Optimizer.XML.Kernel.GeneticSetup.PopulationSize"),name)+" "+" "+Language.tr("Optimizer.XMLError.ValueHasToBePositiveIntegerNumber");
				geneticPopulationSize=L.intValue();
			}
			s=Language.trAllAttribute("Optimizer.XML.Kernel.GeneticSetup.EvolutionPressure",node);
			if (!s.isBlank()) {
				D=NumberTools.getPositiveDouble(NumberTools.systemNumberToLocalNumber(s));
				if (D==0 || D.doubleValue()>1) return String.format(Language.tr("Surface.XML.AttributeError"),Language.trPrimary("Optimizer.XML.Kernel.GeneticSetup.EvolutionPressure"),name)+" "+" "+Language.tr("Optimizer.XMLError.ValueHasToBeNumberBetween0And1");
				geneticEvolutionPressure=D.doubleValue();
			}
			s=Language.trAllAttribute("Optimizer.XML.Kernel.GeneticSetup.ChangeSpeed1",node);
			if (!s.isBlank()) {
				D=NumberTools.getPositiveDouble(NumberTools.systemNumberToLocalNumber(s));
				if (D==0 || D.doubleValue()>1) return String.format(Language.tr("Surface.XML.AttributeError"),Language.trPrimary("Optimizer.XML.Kernel.GeneticSetup.ChangeSpeed1"),name)+" "+" "+Language.tr("Optimizer.XMLError.ValueHasToBeNumberBetween0And1");
				geneticChangeSpeed1=D.doubleValue();
			}
			s=Language.trAllAttribute("Optimizer.XML.Kernel.GeneticSetup.ChangeSpeed2",node);
			if (!s.isBlank()) {
				D=NumberTools.getPositiveDouble(NumberTools.systemNumberToLocalNumber(s));
				if (D==0 || D.doubleValue()>1) return String.format(Language.tr("Surface.XML.AttributeError"),Language.trPrimary("Optimizer.XML.Kernel.GeneticSetup.ChangeSpeed2"),name)+" "+" "+Language.tr("Optimizer.XMLError.ValueHasToBeNumberBetween0And1");
				geneticChangeSpeed2=D.doubleValue();
			}
			s=Language.trAllAttribute("Optimizer.XML.Kernel.GeneticSetup.ChangeSpeed3",node);
			if (!s.isBlank()) {
				D=NumberTools.getPositiveDouble(NumberTools.systemNumberToLocalNumber(s));
				if (D==0 || D.doubleValue()>1) return String.format(Language.tr("Surface.XML.AttributeError"),Language.trPrimary("Optimizer.XML.Kernel.GeneticSetup.ChangeSpeed3"),name)+" "+" "+Language.tr("Optimizer.XMLError.ValueHasToBeNumberBetween0And1");
				geneticChangeSpeed3=D.doubleValue();
			}
			s=Language.trAllAttribute("Optimizer.XML.Kernel.GeneticSetup.ChangeSpeed4",node);
			if (!s.isBlank()) {
				D=NumberTools.getPositiveDouble(NumberTools.systemNumberToLocalNumber(s));
				if (D==0 || D.doubleValue()>1) return String.format(Language.tr("Surface.XML.AttributeError"),Language.trPrimary("Optimizer.XML.Kernel.GeneticSetup.ChangeSpeed4"),name)+" "+" "+Language.tr("Optimizer.XMLError.ValueHasToBeNumberBetween0And1");
				geneticChangeSpeed4=D.doubleValue();
			}
			s=Language.trAllAttribute("Optimizer.XML.Kernel.GeneticSetup.ChangeSpeed5",node);
			if (!s.isBlank()) {
				D=NumberTools.getPositiveDouble(NumberTools.systemNumberToLocalNumber(s));
				if (D==0 || D.doubleValue()>1) return String.format(Language.tr("Surface.XML.AttributeError"),Language.trPrimary("Optimizer.XML.Kernel.GeneticSetup.ChangeSpeed5"),name)+" "+" "+Language.tr("Optimizer.XMLError.ValueHasToBeNumberBetween0And1");
				geneticChangeSpeed5=D.doubleValue();
			}
			return null;
		}

		if (Language.trAll("Optimizer.XML.TimeoutSeconds",name)) {
			final Long L=NumberTools.getPositiveLong(text);
			if (L==null) return String.format(Language.tr("Optimizer.XML.TimeoutSeconds.LoadError"),text);
			timeoutSeconds=L.intValue();
			return null;
		}

		return null;
	}

	/**
	 * Liefert eine Liste der Ressourcen-Namen bei denen die Anzahl direkt eingestellt werden kann
	 * @param model	Editor-Modell aus dem die Ressourcen-Namen ausgelesen werden sollen
	 * @return	Liste der Ressourcen-Namen bei denen die Anzahl direkt eingestellt werden kann
	 */
	public static String[] getResourceNames(final EditModel model) {
		final List<String> resources=new ArrayList<>();
		for (String name: model.resources.list()) {
			final ModelResource resource=model.resources.get(name);
			if (resource.getMode()==ModelResource.Mode.MODE_NUMBER && resource.getCount()>=0) resources.add(name);
		}
		return resources.toArray(String[]::new);
	}

	/**
	 * Liefert die Anzahl an Bedienern in einer Ressource
	 * @param model	Editor-Modell aus dem die Ressourcen-Namen ausgelesen werden sollen
	 * @param resourceName	Name der Ressource
	 * @return	Anzahl der Bediener in der Ressource oder -1, wenn keine Anzahl ermittelt werden konnte
	 */
	public static int getResourceCount(final EditModel model, final String resourceName) {
		final ModelResource resource=model.resources.getNoAutoAdd(resourceName);
		if (resource==null) return -1;
		if (resource.getMode()==ModelResource.Mode.MODE_NUMBER && resource.getCount()>=0) return resource.getCount();
		return -1;
	}

	/**
	 * Liefert eine Liste der globalen Variablen
	 * @param model	Editor-Modell aus dem die Liste der globalen Variablen ausgelesen werden soll
	 * @return	Liste der globalen Variablen
	 */
	public static String[] getGlobalVariables(final EditModel model) {
		return model.globalVariables.stream().map(variable->variable.getName()).toArray(String[]::new);
	}

	/**
	 * Liefert den Ausdruck, der als Startwert für die Variable in dem Ausgangsmodell verwendet werden soll
	 * @param model	Editor-Modell aus dem die Liste der globalen Variablen ausgelesen werden soll
	 * @param variableName	Name der globalen Variable deren Startwert ermittelt werden soll
	 * @return	Ausdruck, der den Startwert der globalen Variable darstellt, oder <code>null</code>, wenn keine Variable oder kein Startwert ermittelt werden konnte
	 */
	public static String getGlobalVariablesStartValues(final EditModel model, final String variableName) {
		final var globalVariable=model.getGlobalVariableByName(variableName);
		if (globalVariable==null) return null;
		return globalVariable.getExpression();
	}

	/**
	 * Prüft, ob der angegebene Name zu einer Ressource, bei der die Anzahl eingestellt werden kann, passt
	 * @param model	Editor-Modell aus dem die Ressourcen-Namen ausgelesen werden sollen
	 * @param tag	Name bei dem geprüft werden soll, ob er zu einer Ressource passt
	 * @return	Gibt <code>true</code> zurück, wenn der Name zu einer Ressource passt
	 */
	public static boolean isResourceNameOk(final EditModel model, final String tag) {
		for (String resource: getResourceNames(model)) if (resource.equals(tag)) return true;
		return false;
	}

	/**
	 * Prüft, ob der angegebene Name zu einer globalen Variable passt
	 * @param model	Editor-Modell aus dem die Liste der globalen Variablen ausgelesen werden soll
	 * @param tag	Name bei dem geprüft werden soll, ob er zu einer globalen Variable passt
	 * @return	Gibt <code>true</code> zurück, wenn der Name zu einer globalen Variable passt
	 */
	public static boolean isGlobalVariableOk(final EditModel model, final String tag) {
		for (String variable: getGlobalVariables(model)) if (variable.equals(tag)) return true;
		return false;
	}

	/**
	 * Diese Klasse bildet eine einzelne Steuerungsvariable ab
	 * @author Alexander Herzog
	 * @see OptimizerSetup#controlVariables
	 */
	public static final class ControlVariable implements Cloneable {
		/**
		 * Ressource, Variable oder xml-Tag, welches den zu variierenden Wert beinhaltet
		 */
		public String tag;

		/**
		 * Modus
		 */
		public ModelChanger.Mode mode;

		/**
		 * Typs des Wertes (Zahl, Verteilungsparameter, ...)
		 * @see ModelChanger#XML_ELEMENT_MODES
		 */
		public int xmlMode;

		/**
		 * Minimaler Wert der Variable
		 */
		public double rangeFrom;

		/**
		 * Maximaler Wert der Variable
		 */
		public double rangeTo;

		/**
		 * Wert der Variable im ersten Simulationslauf
		 */
		public double start;

		/**
		 * Gibt an, ob die Variable gebrochene Zahlen (<code>false</code>) oder nur Ganzzahlen (<code>true</code>) annehmen darf.
		 */
		public boolean integerValue;

		/**
		 * Konstruktor der Klasse <code>ControlVariable</code>
		 */
		public ControlVariable() {
			tag="";
			mode=ModelChanger.Mode.MODE_RESOURCE;
			xmlMode=0;
			rangeFrom=1;
			rangeTo=10;
			start=1;
			integerValue=false;
		}

		/**
		 * Prüft, ob das aktuelle <code>ControlVariable</code>-Objekt inhaltlich mit einem anderen identisch ist.
		 * @param otherControlVariable	Objekt vom Typ <code>ControlVariable</code>, welches mit diesem Objekt verglichen werden soll.
		 * @return	Gibt <code>true</code> zurück, wenn die beiden Objekte inhaltlich identisch sind.
		 */
		public boolean equalsControlVariable(final ControlVariable otherControlVariable) {
			if (otherControlVariable==null) return false;

			if (!otherControlVariable.tag.equals(tag)) return false;
			if (otherControlVariable.mode!=mode) return false;
			if (otherControlVariable.xmlMode!=xmlMode) return false;
			if (otherControlVariable.rangeFrom!=rangeFrom) return false;
			if (otherControlVariable.rangeTo!=rangeTo) return false;
			if (otherControlVariable.start!=start) return false;
			if (otherControlVariable.integerValue!=integerValue) return false;

			return true;
		}

		/**
		 * Erstellt eine Kopie des aktuellen <code>ControlVariable</code> Objekts.
		 * @return	Kopiertes Objekt
		 */
		@Override
		public ControlVariable clone() {
			ControlVariable clone=new ControlVariable();

			clone.tag=tag;
			clone.mode=mode;
			clone.xmlMode=xmlMode;
			clone.rangeFrom=rangeFrom;
			clone.rangeTo=rangeTo;
			clone.start=start;
			clone.integerValue=integerValue;

			return clone;
		}

		/**
		 * Speichert die Einstellungen in einem XML-Knoten
		 * @param doc	XML-Dokument
		 * @param node	Eltern-XML-Knoten
		 */
		private void addDataToXML(final Document doc, final Element node) {
			Element sub=doc.createElement(Language.trPrimary("Optimizer.XML.ControlVariables"));
			node.appendChild(sub);
			sub.setTextContent(tag);
			int modeInt=0;
			switch (mode) {
			case MODE_RESOURCE: modeInt=0; break;
			case MODE_VARIABLE: modeInt=1; break;
			case MODE_MAP: modeInt=3; break;
			case MODE_XML: modeInt=2; break;
			}
			sub.setAttribute(Language.trPrimary("Optimizer.XML.GlobalMode"),""+modeInt);
			sub.setAttribute(Language.trPrimary("Optimizer.XML.Mode"),""+xmlMode);
			sub.setAttribute(Language.trPrimary("Optimizer.XML.RangeMinimum"),NumberTools.formatSystemNumber(rangeFrom));
			sub.setAttribute(Language.trPrimary("Optimizer.XML.RangeMaximum"),NumberTools.formatSystemNumber(rangeTo));
			sub.setAttribute(Language.trPrimary("Optimizer.XML.StartValue"),NumberTools.formatSystemNumber(start));
			if (mode!=ModelChanger.Mode.MODE_RESOURCE) {
				if (integerValue) sub.setAttribute(Language.trPrimary("Optimizer.XML.IntegerNumber"),"1");
			}
		}

		/**
		 * Versucht die Einstellungen aus einem XML-Knoten zu laden
		 * @param node	XML-Knoten der die Daten enthält
		 * @return	Liefert im Erfolgsfall <code>null</code>, sonst eine Fehlermeldung
		 */
		private String loadFromXML(final Element node) {
			Integer I;
			Double D;
			String s;

			tag=node.getTextContent();

			s=Language.trAllAttribute("Optimizer.XML.GlobalMode",node);
			if (!s.isEmpty()) {
				I=NumberTools.getInteger(s);
				if (I==null || I<0 || I>3) return String.format(Language.tr("Surface.XML.AttributeError"),Language.trPrimary("Optimizer.XML.Mode"),node.getNodeName())+" "+String.format(Language.tr("Optimizer.XMLError.ValueHasToBeNumberInRange0ToN"),2);
				switch (I.intValue()) {
				case 0: mode=ModelChanger.Mode.MODE_RESOURCE; break;
				case 1: mode=ModelChanger.Mode.MODE_VARIABLE; break;
				case 2: mode=ModelChanger.Mode.MODE_XML; break;
				case 3: mode=ModelChanger.Mode.MODE_MAP; break;
				}
			} else {
				mode=ModelChanger.Mode.MODE_XML;
			}

			I=NumberTools.getInteger(Language.trAllAttribute("Optimizer.XML.Mode",node));
			if (I==null || I<0 || I>=ModelChanger.XML_ELEMENT_MODES.length) return String.format(Language.tr("Surface.XML.AttributeError"),Language.trPrimary("Optimizer.XML.Mode"),node.getNodeName())+" "+String.format(Language.tr("Optimizer.XMLError.ValueHasToBeNumberInRange0ToN"),ModelChanger.XML_ELEMENT_MODES.length-1);
			xmlMode=I;

			D=NumberTools.getDouble(NumberTools.systemNumberToLocalNumber(Language.trAllAttribute("Optimizer.XML.RangeMinimum",node)));
			if (D==null) return String.format(Language.tr("Surface.XML.AttributeError"),Language.trPrimary("Optimizer.XML.RangeMinimum"),node.getNodeName())+" "+Language.tr("Optimizer.XMLError.ValueHasToBeNumber");
			rangeFrom=D;

			D=NumberTools.getDouble(NumberTools.systemNumberToLocalNumber(Language.trAllAttribute("Optimizer.XML.RangeMaximum",node)));
			if (D==null || D<rangeFrom) return String.format(Language.tr("Surface.XML.AttributeError"),Language.trPrimary("Optimizer.XML.RangeMaximum"),node.getNodeName())+" "+Language.tr("Optimizer.XMLError.ValueHasToBeNumberAndLargerThanMinimum");
			rangeTo=D;

			D=NumberTools.getDouble(NumberTools.systemNumberToLocalNumber(Language.trAllAttribute("Optimizer.XML.StartValue",node)));
			if (D==null || D<rangeFrom || D>rangeTo) return String.format(Language.tr("Surface.XML.AttributeError"),Language.trPrimary("Optimizer.XML.StartValue"),node.getNodeName())+" "+Language.tr("Optimizer.XMLError.ValueHasToBeNumberInRange");
			start=D;

			if (mode!=ModelChanger.Mode.MODE_RESOURCE) {
				final String isInt=Language.trAllAttribute("Optimizer.XML.IntegerNumber",node);
				if (!isInt.isEmpty() && !isInt.equals("0")) integerValue=true;
			}

			return null;
		}
	}
}