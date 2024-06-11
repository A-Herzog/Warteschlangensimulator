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
package simulator.simparser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import mathtools.NumberTools;
import simulator.simparser.symbols.CalcSymbolUserFunction;
import simulator.simparser.symbols.CalcSymbolUserFunctionJS;
import tools.SetupData;

/**
 * Diese Singleton-Klasse verwaltet die in {@link ExpressionCalc}
 * bereitgestellten zusätzlichen nutzerdefinierten Funktionen.
 * @author Alexander Herzog
 * @see ExpressionCalc
 */
public class ExpressionCalcUserFunctionsManager {
	/**
	 * Instanz des Setup-Objektes
	 */
	private SetupData setup=SetupData.getSetup();

	/**
	 * Instanz dieses Singletons
	 * @see #getInstance()
	 */
	private static ExpressionCalcUserFunctionsManager instance;

	/**
	 * Liste der nutzerdefinierten Rechenfunktionen
	 */
	private final List<UserFunction> userFunctions;

	/**
	 * Liefert die Instanz dieser Singleton-Klasse
	 * @return	Instanz dieser Klasse
	 */
	public static synchronized ExpressionCalcUserFunctionsManager getInstance() {
		if (instance==null) instance=new ExpressionCalcUserFunctionsManager();
		return instance;
	}

	/**
	 * Konstruktor der Klasse<br>
	 * Diese Klasse ist ein Singleton und kann nicht direkt instanziert werden.
	 * @see #getInstance()
	 */
	private ExpressionCalcUserFunctionsManager() {
		userFunctions=new ArrayList<>();
		loadData();
	}

	/**
	 * Lädt die aktuelle Liste der nutzerdefinierten Funktionen in die
	 * {@link ExpressionCalc}-Klasse.
	 * @return	Array mit den Parser-Ergebnissen für die einzelnen Funktionen
	 */
	public synchronized int[] load() {
		return load(true);
	}

	/**
	 * Lädt die aktuelle Liste der nutzerdefinierten Funktionen in die
	 * {@link ExpressionCalc}-Klasse.
	 * @param saveData	Soll die aktuelle Liste der nutzerdefinierten Funktionen auch im Setup gespeichert werden?
	 * @return	Array mit den Parser-Ergebnissen für die einzelnen Funktionen
	 */
	public synchronized int[] load(final boolean saveData) {
		if (saveData) saveData();

		final int[] results=new int[userFunctions.size()];
		Arrays.fill(results,-1);

		ExpressionCalc.userFunctions=new ArrayList<>();
		ExpressionCalc.userFunctionsJS=new ArrayList<>();
		for (int i=0;i<userFunctions.size();i++) {
			final UserFunction userFunction=userFunctions.get(i);
			final Object obj=userFunction.compile();
			if (obj instanceof Integer) {
				results[i]=(Integer)obj;
			} else {
				switch (userFunction.mode) {
				case EXPRESSION:
					ExpressionCalc.userFunctions.add((CalcSymbolUserFunction)obj);
					break;
				case JAVASCRIPT:
					ExpressionCalc.userFunctionsJS.add((CalcSymbolUserFunctionJS)obj);
					break;
				}
			}
		}
		return results;
	}

	/**
	 * Prüft ob eine Zeichenkette als Funktion interpretiert werden kann
	 * @param parameterCount	Anzahl der Parameter für die innere Funktion
	 * @param content	Zu interpretierende Zeichenkette
	 * @return	Liefert im Erfolgsfall -1, sonst die nullbasierende Position des Parser-Fehlers
	 */
	public static int test(final int parameterCount, final String content) {
		return CalcSymbolUserFunction.test(parameterCount,content);
	}

	/**
	 * Liefert die Liste der nutzerdefinierten Funktionen
	 * @return	Liste der nutzerdefinierten Funktionen
	 */
	public List<UserFunction> getUserFunctions() {
		return userFunctions;
	}

	/**
	 * Lädt die Daten aus dem Setup nach {@link #userFunctions}.
	 */
	private void loadData() {
		userFunctions.clear();

		List<String> userFunctionRecords;

		userFunctionRecords=setup.userDefinedCalculationFunctions;
		if (userFunctionRecords!=null) for (String record: userFunctionRecords) {
			final UserFunction userFunction=UserFunction.loadFromString(record,UserFunctionMode.EXPRESSION);
			if (userFunction!=null) userFunctions.add(userFunction);
		}

		userFunctionRecords=setup.userDefinedJSFunctions;
		if (userFunctionRecords!=null) for (String record: userFunctionRecords) {
			final UserFunction userFunction=UserFunction.loadFromString(record,UserFunctionMode.JAVASCRIPT);
			if (userFunction!=null) userFunctions.add(userFunction);
		}

		load(false);
	}

	/**
	 * Speichert die Daten aus {@link #userFunctions} ins Setup.
	 */
	private void saveData() {
		if (setup.userDefinedCalculationFunctions==null) setup.userDefinedCalculationFunctions=new ArrayList<>();
		setup.userDefinedCalculationFunctions.clear();
		userFunctions.stream().filter(userFunction->userFunction.mode==UserFunctionMode.EXPRESSION).map(userFunction->userFunction.toString()).forEach(setup.userDefinedCalculationFunctions::add);

		if (setup.userDefinedJSFunctions==null) setup.userDefinedJSFunctions=new ArrayList<>();
		setup.userDefinedJSFunctions.clear();
		userFunctions.stream().filter(userFunction->userFunction.mode==UserFunctionMode.JAVASCRIPT).map(userFunction->userFunction.toString()).forEach(setup.userDefinedJSFunctions::add);

		setup.saveSetup();
	}

	/**
	 * Art der nutzerdefinierten Funktion
	 */
	public enum UserFunctionMode {
		/** gewöhnlicher Rechenausdruck */
		EXPRESSION,
		/** Javascript-basierte Funktion */
		JAVASCRIPT
	}

	/**
	 * Datensatz für eine nutzerdefinierte Funktion
	 * @see ExpressionCalcUserFunctionsManager#getUserFunctions()
	 */
	public static class UserFunction {
		/**
		 * Name der Funktion
		 */
		public String name;

		/**
		 * Anzahl der Parameter für die Funktion
		 */
		public int parameterCount;

		/**
		 * Zu interpretierende Zeichenkette
		 */
		public String content;

		/**
		 * Art der Funktion
		 */
		public UserFunctionMode mode;

		/**
		 * Konstruktor der Klasse
		 * @param name	Name der Funktion
		 * @param parameterCount	Anzahl der Parameter für die Funktion
		 * @param content	Zu interpretierende Zeichenkette
		 * @param mode	Art der Funktion
		 */
		public UserFunction(final String name, final int parameterCount, final String content, final UserFunctionMode mode) {
			this.name=name;
			this.parameterCount=parameterCount;
			this.content=content;
			this.mode=mode;
		}

		/**
		 * Copy-Konstruktor der Klasse
		 * @param source	Zu kopierendes Ausgangsobjekt
		 */
		public UserFunction(final UserFunction source) {
			this.name=source.name;
			this.parameterCount=source.parameterCount;
			this.content=source.content;
			this.mode=source.mode;
		}

		/**
		 * Übersetzt den Datensatz in ein {@link CalcSymbolUserFunction}-Objekt
		 * @return	Liefert im Erfolgsfall ein {@link CalcSymbolUserFunction}-Objekt, sonst eine nullbasierende Zahl ({@link Integer}), die die Position des Fehlers beim Parsen angibt
		 */
		private Object compile() {
			switch (mode) {
			case EXPRESSION:
				return CalcSymbolUserFunction.compile(name,parameterCount,content);
			case JAVASCRIPT:
				return new CalcSymbolUserFunctionJS(name,content);
			default:
				return 0; /* Fehler an Index 0 */
			}
		}

		@Override
		public String toString() {
			return name+"\t"+parameterCount+"\t"+content;
		}

		/**
		 * Versucht eine nutzerdefinierte Funktion aus einem Text, in
		 * dem die Komponenten per Tabulator getrennt sind, zu laden.
		 * @param text	Nutzerdefinierte Funktion als Text
		 * @param mode	Art der Funktion
		 * @return	Liefert im Erfolgsfall die neue nutzerdefinierte Funktion, sonst <code>null</code>
		 */
		private static UserFunction loadFromString(final String text, final UserFunctionMode mode) {
			final String[] parts=text.split("\\t",3);
			if (parts.length!=3) return null;
			final Integer I=NumberTools.getNotNegativeInteger(parts[1]);
			if (I==null) return null;
			return new UserFunction(parts[0],I,parts[2],mode);
		}
	}
}
