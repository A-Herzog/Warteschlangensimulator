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
import parser.MathCalcError;
import simulator.runmodel.RunDataClient;
import simulator.simparser.coresymbols.CalcSymbolSimData;

/**
 * Liefert das an Stelle <code>index</code> (1. Parameter) im aktuellen Kundenobjekt hinterlegte Datenfeld.<br>
 * In den "Variable"-Elementen kann schreibend auf diese Felder zugegriffen werden.
 * @author Alexander Herzog
 *
 */
public class CalcSymbolClientUserData extends CalcSymbolSimData  {
	/**
	 * Name des Befehls zum Abfragen eines Kunden-Datenfeldes
	 * @see #getNames()
	 */
	public static final String[] CLIENT_DATA_COMMANDS=new String[]{"ClientData","KundenDaten"};

	@Override
	public String[] getNames() {
		return CLIENT_DATA_COMMANDS;
	}

	@Override
	protected double calc(double[] parameters) throws MathCalcError {
		if (parameters.length!=1) throw error();
		return getClientData((int)FastMath.round(parameters[0]));
	}

	@Override
	protected double calcOrDefault(final double[] parameters, final double fallbackValue) {
		if (parameters.length!=1) return fallbackValue;
		return getClientData((int)FastMath.round(parameters[0]));
	}

	/**
	 * Prüft, ob eine Zeichenkette mit einer bestimmten anderen Zeichenkette beginnt (ohne Berücksichtigung der Groß- und Kleinschreibung)
	 * @param longText	Lange Zeichenkette
	 * @param testText	Kurze Zeichenkette, die am Anfang der langen Zeichenkette gesucht werden soll
	 * @return	Liefert <code>true</code>, wenn die lange Zeichenkette mit der zu suchenden Zeichenkette ohne Berücksichtigung der Groß- und Kleinschreibung beginnt
	 */
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

	/**
	 * Handelt es sich um einen Ausdruck der Form "ClientData(Parameter)".
	 * @param test	Zu prüfender Ausdruck
	 * @return	Liefert im Erfolgsfall den inneren Parameter (ggf. noch mit Anführungszeichen) zurück, sonst <code>null</code>.
	 */
	private static String testClientDataBase(String test) {
		if (test==null) return null;
		test=test.trim();
		if (test.isEmpty()) return null;
		final int len=test.length();

		/* Name ok? */
		int startPos=-1;
		for (String name: CLIENT_DATA_COMMANDS) if (startsWithIgnoreCase(test,name)) {
			startPos=name.length();
			if (startPos>=test.length()) return null;
			while (test.charAt(startPos)==' ' && startPos<len) startPos++;
			break;
		}
		if (startPos<0) return null;

		/* Klammern */
		if (len<startPos+3) return null;
		final char c1=test.charAt(startPos);
		final char c2=test.charAt(len-1);

		if ((c1=='(' && c2==')') || (c1=='[' && c2==']') || (c1=='{' && c2=='}')) {
			return test.substring(startPos+1,len-1).trim();
		} else {
			return null;
		}
	}

	/**
	 * Prüft, ob es sich bei einer Zeichenkette um eine gültige mögliche Zuweisung an eine Kunden-Nutzervariable handelt,<br>
	 * also um einen Ausdruck der Form "ClientData(123)".
	 * @param test	Zu prüfender Ausdruck
	 * @return	Gibt den Wert des Index, der geschrieben werden soll, zurück oder -1, wenn der Ausdruck nicht interpretiert werden konnte.
	 */
	public static int testClientData(final String test) {
		final String parameter=testClientDataBase(test);
		if (parameter==null || parameter.isEmpty()) return -1;

		final char c=parameter.charAt(0);
		if (c=='"' || c=='\'') return -1;

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
		final int len=parameter.length();
		if (len<3) return null;
		final char c1=parameter.charAt(0);
		final char c2=parameter.charAt(len-1);
		if ((c1=='"' && c2=='"') || (c1=='\'' && c2=='\'')) {
			final String result=parameter.substring(1,len-1).trim();
			if (result.isEmpty()) return null;
			return result;
		} else {
			return null;
		}
	}
}
