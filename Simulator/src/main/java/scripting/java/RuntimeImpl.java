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
package scripting.java;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import language.Language;
import mathtools.NumberTools;
import parser.MathCalcError;
import simulator.runmodel.SimulationData;
import simulator.simparser.ExpressionCalc;
import tools.NetHelper;
import tools.SetupData;

/**
 * Implementierungsklasse für das Interface {@link RuntimeInterface}
 * @author Alexander Herzog
 * @see SimulationInterface
 * @see SimulationImpl
 */
public class RuntimeImpl implements RuntimeInterface {
	/**
	 * Zuordnung von Rechenausdruck-Zeichenketten und bereits erstellten passenden Objekten
	 * @see #getExpression(String)
	 */
	private Map<String,ExpressionCalc> expressionCache;

	/** Stationslokale Daten */
	private final RuntimeData mapLocal;

	/** Modellweite Daten */
	private final Map<String,Object> mapGlobal;

	/**
	 * Konstruktor der Klasse
	 * @param simData	Simulationsdatenobjekt, dessen Daten bereitgestellt werden sollen (wird nur für die globale Zuordnung verwendet)
	 */
	public RuntimeImpl(final SimulationData simData) {
		mapLocal=new RuntimeData();
		mapGlobal=(simData==null)?null:(simData.runData.getMapGlobal());
	}

	/**
	 * Versucht eine Zeichenkette in ein Rechenobjekt umzuwandeln.
	 * @param text	Zeichenkette, die die Formel enthält
	 * @return	Liefert im Erfolgsfall ein Rechenobjekt, sonst eine Fehlermeldung
	 */
	private Object getExpression(final String text) {
		if (expressionCache==null) expressionCache=new HashMap<>();
		ExpressionCalc expression=expressionCache.get(text);
		if (expression!=null) return expression;

		expression=new ExpressionCalc(new String[0],null);
		final int errorPos=expression.parse(text);
		if (errorPos>=0) return String.format(Language.tr("Statistics.Filter.CoundNotProcessExpression.Info"),errorPos+1);
		expressionCache.put(text,expression);
		return expression;
	}

	@Override
	public Object calc(final String expression) {
		final Object result=getExpression(expression);
		if (result instanceof String) return result;
		final ExpressionCalc calc=(ExpressionCalc)result;

		try {
			return NumberTools.fastBoxedValue(calc.calc());
		} catch (MathCalcError e) {
			return Language.tr("Statistics.Filter.CoundNotProcessExpression.Title");
		}
	}

	@Override
	public long getTime() {
		return System.currentTimeMillis();
	}

	@Override
	public double getInput(final String url, final double errorValue) {
		URI uriObj=null;
		try {
			uriObj=new URI(url);
		} catch (URISyntaxException e) {
			return errorValue;
		}

		final String text=NetHelper.loadText(uriObj,false,false);
		if (text==null) return errorValue;
		final Double D=NumberTools.getDouble(text);
		if (D==null) return errorValue;
		return D.doubleValue();
	}

	/**
	 * Regulärer Ausdruck für {@link #tokenize(String)}
	 * @see #tokenize(String)
	 */
	private final Pattern tokenizePattern=Pattern.compile("\"([^\"]*)\"|(\\S+)");

	/**
	 * Zerlegt einen Gesamt-Befehl in seine einzelnen (durch Leerzeichen getrennte)
	 * Bestandteile. Dabei werden Bereiche in Anführungszeichen zusammengehalten.
	 * @param commandLine	Gesamt-Befehl als einzelne Zeichenkette
	 * @return	Befehl aus Einzelkomponenten
	 */
	private String[] tokenize(final String commandLine) {
		final List<String> list=new ArrayList<>();
		final Matcher m=tokenizePattern.matcher(commandLine);
		while (m.find()) {
			if (m.group(1)!=null) list.add(m.group(1)); else list.add(m.group(2));
		}
		return list.toArray(String[]::new);
	}

	@Override
	public boolean execute(final String commandLine) {
		/* Sicherheitsprüfung */
		if (!SetupData.getSetup().modelSecurityAllowExecuteExternal) return false;

		/* Ausführung */
		try {
			final Process p=Runtime.getRuntime().exec(tokenize(commandLine));
			if (p==null) return false;
			return true;
		} catch (IOException e) {
			return false;
		}
	}

	@Override
	public String executeAndReturnOutput(final String commandLine) {
		/* Sicherheitsprüfung */
		if (!SetupData.getSetup().modelSecurityAllowExecuteExternal) return null;

		/* Ausführung */
		try {
			final Process p=Runtime.getRuntime().exec(tokenize(commandLine));
			if (p==null) return null;
			p.waitFor();

			final StringBuilder result=new StringBuilder();
			try (Reader reader = new BufferedReader(new InputStreamReader(p.getInputStream(),StandardCharsets.UTF_8))) {
				int c=0;
				while ((c=reader.read())!=-1) {
					result.append((char)c);
				}
			}
			return result.toString();
		} catch (IOException | InterruptedException e) {
			return null;
		}
	}

	@Override
	public int executeAndWait(final String commandLine) {
		/* Sicherheitsprüfung */
		if (!SetupData.getSetup().modelSecurityAllowExecuteExternal) return -1;

		/* Ausführung */
		try {
			final Process p=Runtime.getRuntime().exec(tokenize(commandLine));
			if (p==null) return -1;
			return p.waitFor();
		} catch (IOException | InterruptedException e) {
			return -1;
		}
	}

	@Override
	public Map<String,Object> getMapLocal() {
		return mapLocal.get();
	}

	@Override
	public Map<String,Object> getMapGlobal() {
		return mapGlobal;
	}
}
