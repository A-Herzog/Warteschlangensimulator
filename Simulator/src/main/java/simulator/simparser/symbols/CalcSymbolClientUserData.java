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
package simulator.simparser.symbols;

import org.apache.commons.math3.util.FastMath;

import mathtools.NumberTools;
import parser.CalcSystem;
import parser.coresymbols.CalcSymbolDirectValue;
import simulator.runmodel.RunDataClient;
import simulator.simparser.coresymbols.CalcSymbolSimData;

/**
 * Liefert das an Stelle <code>index</code> (1. Parameter) im aktuellen Kundenobjekt hinterlegte Datenfeld.<br>
 * In den "Variable"-Elementen kann schreibend auf diese Felder zugegriffen werden.
 * @author Alexander Herzog
 *
 */
public class CalcSymbolClientUserData extends CalcSymbolSimData implements CalcSymbolDirectValue {
	/**
	 * Name des Befehls zum Abfragen eines Kunden-Datenfeldes
	 */
	public static final String[] CLIENT_DATA_COMMANDS=new String[]{"ClientData","KundenDaten"};

	@Override
	public String[] getNames() {
		return CLIENT_DATA_COMMANDS;
	}

	@Override
	protected Double calc(double[] parameters) {
		if (parameters.length!=1) return null;
		return fastBoxedValue(getClientData((int)FastMath.round(parameters[0])));
	}

	@Override
	protected double calcOrDefault(final double[] parameters, final double fallbackValue) {
		if (parameters.length!=1) return fallbackValue;
		return getClientData((int)FastMath.round(parameters[0]));
	}

	private static boolean startsWithIgnoreCase(final String longText, final String testText) {
		final int testLength=testText.length();
		if (longText.length()<testLength) return false;
		for (int i=0;i<testLength;i++) {
			char c1=longText.charAt(i);
			char c2=testText.charAt(i);
			if (c1==c2) continue;
			if (c1>='a' && c1<='z') c1=(char)(c1-32);
			if (c2>='a' && c2<='z') c2=(char)(c2-32);
			if (c1!=c2) return false;
		}
		return true;
	}

	private static String testClientDataBase(String test) {
		if (test==null) return null;
		test=test.trim();
		if (test.isEmpty()) return null;

		/* Name ok? */
		boolean ok=false;
		for (String name: CLIENT_DATA_COMMANDS) if (startsWithIgnoreCase(test,name)) {
			ok=true;
			test=test.substring(name.length()).trim();
			break;
		}
		if (!ok) return null;

		/* Klammern */
		if (test.length()<3) return null;
		if ((!test.startsWith("(") || !test.endsWith(")")) && (!test.startsWith("[") || !test.endsWith("]")) && (!test.startsWith("{") || !test.endsWith("}"))) return null;
		test=test.substring(1,test.length()-1).trim();
		if (test.isEmpty()) return null;

		return test;
	}

	/**
	 * Prüft, ob es sich bei einer Zeichenkette um eine gültige mögliche Zuweisung an eine Kunden-Nutzervariable handelt,<br>
	 * also um einen Ausdruck der Form "ClientData(123)".
	 * @param test	Zu prüfender Ausdruck
	 * @return	Gibt den Wert des Index, der geschrieben werden soll, zurück oder -1, wenn der Ausdruck nicht interpretiert werden konnte.
	 */
	public static int testClientData(final String test) {
		final String parameter=testClientDataBase(test);
		if (parameter==null) return -1;

		if (parameter.startsWith("\"") || parameter.startsWith("'")) return -1;

		/* Umwandeln in Zahl */
		final Integer I=NumberTools.getNotNegativeInteger(parameter);
		if (I==null || I>RunDataClient.MAX_USER_DATA_INDEX) return -1;

		return I;
	}

	/**
	 * Prüft, ob es sich bei einer Zeichenkette um eine gültige mögliche Schlüsselzuweisung handelt,<br>
	 * also um einen Ausdruck der Form "ClientData('Schlüssel')".
	 * @param test	Zu prüfender Ausdruck
	 * @return	Gibt den Namen des Schlüssels oder <code>null</code> zurück, wenn der Ausdruck nicht interpretiert werden konnte.
	 */
	public static String testClientDataString(final String test) {
		final String parameter=testClientDataBase(test);
		if (parameter==null) return null;

		/* Zeichenkette in Anführungszeichen */
		if (parameter.length()<3) return null;
		if ((!parameter.startsWith("\"") || !parameter.endsWith("\"")) && (!parameter.startsWith("'") || !parameter.endsWith("'"))) return null;
		final String result=parameter.substring(1,parameter.length()-1).trim();
		if (result.isEmpty()) return null;
		return result;
	}

	@Override
	public boolean getValueDirectOk(CalcSystem calc) {
		return allValuesConst;
	}

	@Override
	public double getValueDirect(CalcSystem calc) {
		calcSystem=calc;
		final double[] values=getParameterValues(calc);
		return calcOrDefault(values,0.0);
	}
}
