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
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import language.Language;
import mathtools.NumberTools;
import parser.MathCalcError;
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

	/**
	 * Versucht eine Zeichenkette in ein Rechenobjekt umzuwandeln.
	 * @param text	Zeichenkette, die die Formel enthält
	 * @return	Liefert im Erfolgsfall ein Rechenobjekt, sonst eine Fehlermeldung
	 */
	private Object getExpression(final String text) {
		if (expressionCache==null) expressionCache=new HashMap<>();
		ExpressionCalc expression=expressionCache.get(text);
		if (expression!=null) return expression;

		expression=new ExpressionCalc(new String[0]);
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
		URL urlObj=null;
		try {
			urlObj=new URL(url);
		} catch (MalformedURLException e) {
			return errorValue;
		}

		final String text=NetHelper.loadText(urlObj,false,false);
		if (text==null) return errorValue;
		final Double D=NumberTools.getDouble(text);
		if (D==null) return errorValue;
		return D.doubleValue();
	}

	@Override
	public boolean execute(final String commandLine) {
		/* Sicherheitsprüfung */
		if (!SetupData.getSetup().modelSecurityAllowExecuteExternal) return false;

		/* Ausführung */
		try {
			final Process p=Runtime.getRuntime().exec(commandLine);
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
			final Process p=Runtime.getRuntime().exec(commandLine);
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
			final Process p=Runtime.getRuntime().exec(commandLine);
			if (p==null) return -1;
			return p.waitFor();
		} catch (IOException | InterruptedException e) {
			return -1;
		}
	}
}
