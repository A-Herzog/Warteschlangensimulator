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

import org.w3c.dom.Element;

import language.Language;
import mathtools.NumberTools;
import simulator.simparser.symbols.CalcSymbolUserFunction;
import simulator.simparser.symbols.CalcSymbolUserFunctionJS;
import simulator.simparser.symbols.CalcSymbolUserFunctionJava;
import tools.SetupData;

/**
 * Diese Singleton-Klasse verwaltet die in {@link ExpressionCalc}
 * bereitgestellten zus�tzlichen nutzerdefinierten Funktionen.
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
	 * L�dt die aktuelle Liste der nutzerdefinierten Funktionen in die
	 * {@link ExpressionCalc}-Klasse.
	 * @return	Array mit den Parser-Ergebnissen f�r die einzelnen Funktionen
	 */
	public synchronized int[] load() {
		return load(true);
	}

	/**
	 * L�dt die aktuelle Liste der nutzerdefinierten Funktionen in die
	 * {@link ExpressionCalc}-Klasse.
	 * @param saveData	Soll die aktuelle Liste der nutzerdefinierten Funktionen auch im Setup gespeichert werden?
	 * @return	Array mit den Parser-Ergebnissen f�r die einzelnen Funktionen
	 */
	public synchronized int[] load(final boolean saveData) {
		if (saveData) saveData();

		final int[] results=new int[userFunctions.size()];
		Arrays.fill(results,-1);

		ExpressionCalc.userFunctions=new ArrayList<>();
		ExpressionCalc.userFunctionsJS=new ArrayList<>();
		ExpressionCalc.userFunctionsJava=new ArrayList<>();
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
				case JAVA:
					ExpressionCalc.userFunctionsJava.add((CalcSymbolUserFunctionJava)obj);
					break;
				}
			}
		}
		return results;
	}

	/**
	 * Pr�ft ob eine Zeichenkette als Funktion interpretiert werden kann
	 * @param parameterCount	Anzahl der Parameter f�r die innere Funktion
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
	 * L�dt die Daten aus dem Setup nach {@link #userFunctions}.
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

		userFunctionRecords=setup.userDefinedJavaFunctions;
		if (userFunctionRecords!=null) for (String record: userFunctionRecords) {
			final UserFunction userFunction=UserFunction.loadFromString(record,UserFunctionMode.JAVA);
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

		if (setup.userDefinedJavaFunctions==null) setup.userDefinedJavaFunctions=new ArrayList<>();
		setup.userDefinedJavaFunctions.clear();
		userFunctions.stream().filter(userFunction->userFunction.mode==UserFunctionMode.JAVA).map(userFunction->userFunction.toString()).forEach(setup.userDefinedJavaFunctions::add);

		setup.saveSetup();
	}

	/**
	 * Art der nutzerdefinierten Funktion
	 */
	public enum UserFunctionMode {
		/** gew�hnlicher Rechenausdruck */
		EXPRESSION,
		/** Javascript-basierte Funktion */
		JAVASCRIPT,
		/** Java-basierte Funktion */
		JAVA
	}

	/**
	 * Datensatz f�r eine nutzerdefinierte Funktion
	 * @see ExpressionCalcUserFunctionsManager#getUserFunctions()
	 */
	public static class UserFunction {
		/**
		 * Name der Funktion
		 */
		public String name;

		/**
		 * Anzahl der Parameter f�r die Funktion
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
		 * @param parameterCount	Anzahl der Parameter f�r die Funktion
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
		 * Vergleich diese nutzerdefinierte Funktion mit einer anderen.
		 * @param otherUserFunction	Weitere nutzerdefinierte Funktion, die inhaltlich mit dieser verglichen werden soll
		 * @return	Liefert <code>true</code>, wenn die beiden Funktionen inhaltlich �bereinstimmen
		 */
		public boolean equalsUserFunction(final UserFunction otherUserFunction) {
			if (otherUserFunction==null) return false;
			if (!name.equals(otherUserFunction.name)) return false;
			if (parameterCount!=otherUserFunction.parameterCount) return false;
			if (!content.equals(otherUserFunction.content)) return false;
			if (mode!=otherUserFunction.mode) return false;
			return true;
		}

		/**
		 * �bersetzt den Datensatz in ein {@link CalcSymbolUserFunction}-Objekt
		 * @return	Liefert im Erfolgsfall ein {@link CalcSymbolUserFunction}-Objekt, sonst eine nullbasierende Zahl ({@link Integer}), die die Position des Fehlers beim Parsen angibt
		 */
		public Object compile() {
			switch (mode) {
			case EXPRESSION:
				return CalcSymbolUserFunction.compile(name,parameterCount,content);
			case JAVASCRIPT:
				return new CalcSymbolUserFunctionJS(name,content);
			case JAVA:
				return new CalcSymbolUserFunctionJava(name,content);
			default:
				return 0; /* Fehler an Index 0 */
			}
		}

		/**
		 * Speichert die Daten zu der nutzerdefinierten Funktion in einem XML-Knoten
		 * @param node	XML-Knoten in dem die Daten gespeichert werden sollen
		 */
		public void saveToXML(final Element node) {
			node.setAttribute(Language.trPrimary("Surface.XML.RootName.UserFunctions.Name"),name);
			node.setAttribute(Language.trPrimary("Surface.XML.RootName.UserFunctions.ParameterCount"),""+parameterCount);
			switch (mode) {
			case EXPRESSION: node.setAttribute(Language.trPrimary("Surface.XML.RootName.UserFunctions.Mode"),Language.trPrimary("Surface.XML.RootName.UserFunctions.Mode.Expression")); break;
			case JAVASCRIPT: node.setAttribute(Language.trPrimary("Surface.XML.RootName.UserFunctions.Mode"),Language.trPrimary("Surface.XML.RootName.UserFunctions.Mode.Javascript")); break;
			case JAVA: node.setAttribute(Language.trPrimary("Surface.XML.RootName.UserFunctions.Mode"),Language.trPrimary("Surface.XML.RootName.UserFunctions.Mode.Java")); break;
			}
			node.setTextContent(content);
		}

		/**
		 * Versucht die Daten zu einer nutzerdefinierten Funktion aus einem XML-Knoten zu laden
		 * @param node	XML-Knoten, aus dem die Daten geladen werden soll
		 * @return	Liefert im Erfolgsfall die neue nutzerdefinierte Funktion, sonst eine Fehlermeldung
		 */
		public static Object loadFromXML(final Element node) {
			final UserFunction function=new UserFunction("",0,"",UserFunctionMode.EXPRESSION);

			function.name=Language.trAllAttribute("Surface.XML.RootName.UserFunctions.Name",node).trim();
			if (function.name.isBlank()) return Language.tr("Surface.XML.RootName.UserFunctions.Name.Error");

			final Integer I=NumberTools.getNotNegativeInteger(Language.trAllAttribute("Surface.XML.RootName.UserFunctions.ParameterCount",node));
			if (I==null) return String.format(Language.tr("Surface.XML.RootName.UserFunctions.ParameterCount.Error"),function.name);

			function.mode=UserFunctionMode.EXPRESSION;
			final String modeString=Language.trAllAttribute("Surface.XML.RootName.UserFunctions.Mode",node).trim();
			if (modeString.equalsIgnoreCase(Language.trPrimary("Surface.XML.RootName.UserFunctions.Mode.Javascript"))) function.mode=UserFunctionMode.JAVASCRIPT;
			if (modeString.equalsIgnoreCase(Language.trPrimary("Surface.XML.RootName.UserFunctions.Mode.Java"))) function.mode=UserFunctionMode.JAVA;

			function.content=node.getTextContent();

			return function;
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
		public static UserFunction loadFromString(final String text, final UserFunctionMode mode) {
			final String[] parts=text.split("\\t",3);
			if (parts.length!=3) return null;
			final Integer I=NumberTools.getNotNegativeInteger(parts[1]);
			if (I==null) return null;
			return new UserFunction(parts[0],I,parts[2],mode);
		}
	}
}
