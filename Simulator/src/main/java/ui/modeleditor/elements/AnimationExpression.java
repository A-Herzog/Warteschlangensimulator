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
package ui.modeleditor.elements;

import org.w3c.dom.Element;

import language.Language;
import mathtools.NumberTools;
import parser.MathCalcError;
import scripting.java.ClientImpl;
import scripting.java.DynamicFactory;
import scripting.java.DynamicRunner;
import scripting.java.DynamicStatus;
import scripting.java.OutputImpl;
import scripting.java.SystemImpl;
import scripting.js.JSRunSimulationData;
import simulator.runmodel.SimulationData;
import simulator.simparser.ExpressionCalc;
import ui.modeleditor.coreelements.ModelElement;

/**
 * Diese Klasse hält einen Ausdruck oder ein Skript für die Verwendung als
 * Datenquelle innerhalb eines Animationselements vor.
 * @author Alexander Herzog
 * @see AnimationExpressionPanel
 */
public class AnimationExpression implements Cloneable {
	/**
	 * Zu verwendender Modus
	 * @see AnimationExpression#mode
	 */
	public enum ExpressionMode {
		/** Rechenausdruck */
		Expression,
		/** Javascript als Sprache verwenden */
		Javascript,
		/** Java als Sprache verwenden */
		Java
	}

	/**
	 * Zu verwendender Modus
	 */
	private ExpressionMode mode;

	/**
	 * Rechenausdruck im Modus {@link ExpressionMode#Expression}
	 * @see #getExpression()
	 * @see #setExpression(String)
	 */
	private String expression;

	/**
	 * Skript im Modus {@link ExpressionMode#Javascript} oder {@link ExpressionMode#Java}
	 * @see #getScript()
	 * @see #setJavascript(String)
	 * @see #setJava(String)
	 */
	private String script;

	/**
	 * Konstruktor der Klasse
	 */
	public AnimationExpression() {
		mode=ExpressionMode.Expression;
		expression="";
		script="";
	}

	/**
	 * Copy-Konstruktor der Klasse
	 * @param copySource	Ausgangselement das kopiert werden soll
	 */
	public AnimationExpression(final AnimationExpression copySource) {
		if (copySource==null) {
			mode=ExpressionMode.Expression;
			expression="";
			script="";
		} else {
			mode=copySource.mode;
			expression=copySource.expression;
			script=copySource.script;
		}
	}

	/**
	 * Konstruktor der Klasse
	 * @param expression	Initial einzustellender Rechenausdruck
	 * @see #setExpression(String)
	 */
	public AnimationExpression(final String expression) {
		this();
		setExpression(expression);
	}

	/**
	 * Vergleicht die Daten in diesem Objekt mit den Daten in einem anderen {@link AnimationExpression}-Objekt.
	 * @param otherAnimationExpression	Zweites {@link AnimationExpression}-Objekt für den Vergleich
	 * @return	Liefert <code>true</code>, wenn die beiden Objekte inhaltlich übereinstimmen
	 */
	public boolean equalsAnimationExpression(final AnimationExpression otherAnimationExpression) {
		if (otherAnimationExpression==null) return false;

		if (mode!=otherAnimationExpression.mode) return false;
		switch (mode) {
		case Expression:
			if (!expression.equals(otherAnimationExpression.expression)) return false;
			break;
		case Javascript:
			if (!script.equals(otherAnimationExpression.script)) return false;
			break;
		case Java:
			if (!script.equals(otherAnimationExpression.script)) return false;
			break;
		}

		return true;
	}

	/**
	 * Kopiert die Daten aus einem anderen {@link AnimationExpression}-Objekt in diesen.
	 * @param copySource	Ausgangsobjekt aus dem die Daten kopiert werden sollen
	 */
	public void copyFrom(final AnimationExpression copySource) {
		if (copySource==null) return;
		mode=copySource.mode;
		expression=copySource.expression;
		script=copySource.script;
	}

	@Override
	public AnimationExpression clone() {
		return new AnimationExpression(this);
	}

	/**
	 * Liefert den aktuellen Modus.
	 * @return	Modus
	 * @see ExpressionMode
	 */
	public ExpressionMode getMode() {
		if (mode==null) return ExpressionMode.Expression;
		return mode;
	}

	/**
	 * Liefert den aktuell gewählten Ausdruck.
	 * @return	Aktueller Ausdruck
	 * @see #getMode()
	 * @see #setExpression(String)
	 */
	public String getExpression() {
		if (mode!=ExpressionMode.Expression || expression==null) return "";
		return expression;
	}

	/**
	 * Liefert das aktuell eingestellte Skript.
	 * @return	Aktuelles Skript
	 * @see #getMode()
	 * @see #setJavascript(String)
	 * @see #setJava(String)
	 */
	public String getScript() {
		if ((mode!=ExpressionMode.Javascript && mode!=ExpressionMode.Java) || script==null) return "";
		return script;
	}

	/**
	 * Stellt einen neuen Ausdruck ein (und setzt damit auch den Modus).
	 * @param expression	Neuer Ausdruck
	 * @see #getMode()
	 * @see #getExpression()
	 */
	public void setExpression(final String expression) {
		if (expression==null) return;
		mode=ExpressionMode.Expression;
		this.expression=expression;
	}

	/**
	 * Stellt ein neues Javascript-basiertes Skript ein.
	 * @param script	Neues Skript (Javascript)
	 * @see #getMode()
	 * @see #getScript()
	 */
	public void setJavascript(final String script) {
		if (script==null) return;
		mode=ExpressionMode.Javascript;
		this.script=script;
	}

	/**
	 * Stellt ein neues Java-basiertes Skript ein.
	 * @param script	Neues Skript (Java)
	 * @see #getMode()
	 * @see #getScript()
	 */
	public void setJava(final String script) {
		if (script==null) return;
		mode=ExpressionMode.Java;
		this.script=script;
	}

	/**
	 * Speichert die Daten aus diesem Objekt in einem XML-Knoten.
	 * @param node	XML-Knoten in dem die Daten gespeichert werden sollen
	 * @see #loadFromXML(Element)
	 */
	public void storeToXML(final Element node) {
		switch (mode) {
		case Expression:
			node.setTextContent(expression);
			break;
		case Javascript:
			node.setTextContent(script);
			node.setAttribute(Language.tr("AnimationExpression.XML.Mode"),Language.tr("AnimationExpression.XML.Mode.Javascript"));
			break;
		case Java:
			node.setTextContent(script);
			node.setAttribute(Language.tr("AnimationExpression.XML.Mode"),Language.tr("AnimationExpression.XML.Mode.Java"));
			break;
		}
	}

	/**
	 * Lädt die Daten aus einem XML-Knoten.
	 * @param node	XML-Knoten aus dem die Daten geladen werden sollen
	 * @see #storeToXML(Element)
	 */
	public void loadFromXML(final Element node) {
		final String modeString=Language.trAllAttribute("AnimationExpression.XML.Mode",node);
		final String content=node.getTextContent();

		if (Language.trAll("AnimationExpression.XML.Mode.Javascript",modeString)) {
			setJavascript(content);
			return;
		}

		if (Language.trAll("AnimationExpression.XML.Mode.Java",modeString)) {
			setJava(content);
			return;
		}

		setExpression(content);
	}

	/**
	 * Auszuwertender geparster Ausdruck.
	 * @see #initAnimation(ModelElement, SimulationData)
	 * @see #getAnimationValue(ModelElement, SimulationData)
	 */
	private ExpressionCalc runExpression;

	/**
	 * Initialisierter Javascript-Code
	 * @see #initAnimation(ModelElement, SimulationData)
	 * @see #getAnimationValue(ModelElement, SimulationData)
	 */
	private JSRunSimulationData jsRunner;

	/**
	 * Initialisierter Java-Code
	 * @see #initAnimation(ModelElement, SimulationData)
	 * @see #getAnimationValue(ModelElement, SimulationData)
	 */
	private DynamicRunner javaRunner;

	/**
	 * Ausgabeobjekt für den Java-Code
	 * @see #initAnimation(ModelElement, SimulationData)
	 * @see #getAnimationValue(ModelElement, SimulationData)
	 */
	private StringBuilder animationOutput;

	/**
	 * Initialisiert die Auswertung des Ausdrucks oder des Skriptes in diesem Objekt.
	 * @param element	Station zu der der Ausdruck bzw. das Skript gehört
	 * @param simData	Simulationsdatenobjekt
	 * @return	Liefert <code>true</code>, wenn die Daten für die Auswertung vorbereitet werden konnten
	 */
	public boolean initAnimation(final ModelElement element, final SimulationData simData) {
		if (mode==ExpressionMode.Expression) {
			runExpression=new ExpressionCalc(simData.runModel.variableNames,simData.runModel.modelUserFunctions);
			if (runExpression.parse(expression)>=0) {runExpression=null; return false;}
			return true;
		}

		if (mode==ExpressionMode.Javascript) {
			jsRunner=new JSRunSimulationData(true,false);
			if (!jsRunner.compile(script)) {jsRunner=null; return false;}
			return true;
		}

		if (mode==ExpressionMode.Java) {
			javaRunner=DynamicFactory.getFactory().load(script,simData.runModel.javaImports);
			if (javaRunner.getStatus()!=DynamicStatus.OK) {javaRunner=null; return false;}
			javaRunner.parameter.system=new SystemImpl(simData,element.getId());
			javaRunner.parameter.client=new ClientImpl(simData);
			animationOutput=new StringBuilder();
			javaRunner.parameter.output=new OutputImpl(s->animationOutput.append(s),false);
			return true;
		}

		return false;
	}

	/**
	 * Wertet den Ausdruck bzw. das Skript im Simulationskontext aus.
	 * @param element	Station zu der der Ausdruck bzw. das Skript gehört
	 * @param simData	Simulationsdatenobjekt
	 * @return	Rückgabewert des Ausdrucks bzw. des Skriptes
	 */
	public double getAnimationValue(final ModelElement element, final SimulationData simData) {
		if (mode==ExpressionMode.Expression) {
			if (runExpression==null) return 0.0;
			simData.runData.setClientVariableValues(null);
			try {
				return runExpression.calc(simData.runData.variableValues,simData,null);
			} catch (MathCalcError e) {
				simData.calculationErrorStation(runExpression,element.getName()+" id="+element.getId());
				return 0.0;
			}
		}

		if (mode==ExpressionMode.Javascript) {
			if (jsRunner==null) return 0.0;
			jsRunner.setSimulationDataNoClient(simData,element.getId());
			final String result=jsRunner.runCompiled();
			if (!jsRunner.getLastSuccess() && simData.runModel.cancelSimulationOnScriptError) {
				simData.doEmergencyShutDown(result,element);
			}
			simData.runData.updateMapValuesForStatistics(simData);
			if (jsRunner.isOutputDouble()) return jsRunner.getOutputDouble();
			return 0.0;
		}

		if (mode==ExpressionMode.Java) {
			if (javaRunner==null) return 0.0;
			animationOutput.setLength(0);
			javaRunner.run();
			if (javaRunner.getStatus()!=DynamicStatus.OK && simData.runModel.cancelSimulationOnScriptError) {
				simData.doEmergencyShutDown(DynamicFactory.getLongStatusText(javaRunner),element);
			}
			final String result=animationOutput.toString();
			simData.runData.updateMapValuesForStatistics(simData);
			final Double D=NumberTools.getDouble(result.trim());
			if (D!=null) return D.doubleValue();
			return 0.0;
		}

		return 0.0;
	}
}
