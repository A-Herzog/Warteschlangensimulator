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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import language.Language;
import mathtools.NumberTools;
import simulator.simparser.ExpressionCalc;
import tools.NetHelper;

/**
 * Implementierungsklasse für das Interface {@link RuntimeInterface}
 * @author Alexander Herzog
 * @see SimulationInterface
 * @see SimulationImpl
 */
public class RuntimeImpl implements RuntimeInterface {
	private Map<String,ExpressionCalc> expressionCache;

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

		final Double D=calc.calc();
		if (D==null) return Language.tr("Statistics.Filter.CoundNotProcessExpression.Title");

		return D;
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
}
