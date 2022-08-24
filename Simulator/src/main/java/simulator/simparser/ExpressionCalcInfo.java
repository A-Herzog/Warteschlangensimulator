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

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import parser.CalcSymbolList;
import parser.coresymbols.CalcSymbol;
import parser.symbols.distributions.CalcSymbolDistribution;
import parser.symbols.distributions.CalcSymbolTruncatedDistribution;

/**
 * Diese Klasse liefert Informationen über die Anzahl und Namen der Symbole,
 * die das Rechensystem aktuell erkennt.
 * @author Alexander Herzog
 * @see ExpressionCalc
 */
public class ExpressionCalcInfo {
	/**
	 * Informationen zu allen erkannten Symbolen
	 */
	private final Set<SymbolInfo> symbols;

	/**
	 * Menge der Namen aller Symbole
	 */
	private Set<String> names;

	/**
	 * Cache für die Listen der Symbole nach Typen
	 * @see #getTypeSymbols(parser.coresymbols.CalcSymbol.SymbolType)
	 */
	private final Map<CalcSymbol.SymbolType,Set<SymbolInfo>> cacheType;

	/**
	 * Cache für die Listen der Symbolnamen nach Typen
	 * @see #getTypeNames(parser.coresymbols.CalcSymbol.SymbolType)
	 */
	private final Map<CalcSymbol.SymbolType,Set<String>> cacheNames;

	/**
	 * Cache für die Anzahlen der Symbolnamen nach Typen
	 * @see #getTypeNameCount(parser.coresymbols.CalcSymbol.SymbolType)
	 */
	private final Map<CalcSymbol.SymbolType,Integer> cacheSum;

	/**
	 * Konstruktor der Klasse
	 */
	public ExpressionCalcInfo() {
		this(false);
	}

	/**
	 * Konstruktor der Klasse
	 * @param disableUserFunctions	Sollen mögliche nutzerdefinierte Funktionen bei der Zählung übersprungen werden?
	 */
	public ExpressionCalcInfo(final boolean disableUserFunctions) {
		symbols=processSymbolList(getSymbolList(disableUserFunctions));
		cacheType=new HashMap<>();
		cacheNames=new HashMap<>();
		cacheSum=new HashMap<>();
	}

	/**
	 * Ermittelt eine Liste von unterstützten Symbolen aus {@link ExpressionCalc}
	 * @param disableUserFunctions	Sollen mögliche nutzerdefinierte Funktionen bei der Zählung übersprungen werden?
	 * @return	Liste von unterstützten Symbolen
	 * @see AnalyzeableExpressionCalc
	 */
	private CalcSymbolList getSymbolList(final boolean disableUserFunctions) {
		final AnalyzeableExpressionCalc calc=new AnalyzeableExpressionCalc();
		if (disableUserFunctions) calc.disableUserFunctions();
		calc.parse("1+1");
		return calc.list;
	}

	/**
	 * Stellt basierend auf einem {@link CalcSymbolList}-Objekt eine Menge von Symboldaten zusammen.
	 * @param list	Symbolliste (vom Typ {@link CalcSymbolList})
	 * @return	Menge mit Symboldaten
	 * @see SymbolInfo
	 */
	private Set<SymbolInfo> processSymbolList(final CalcSymbolList list) {
		final Set<Class<? extends CalcSymbol>> symbolClasses=new HashSet<>();
		final Set<Class<? extends CalcSymbolDistribution>> truncatedDistributionClasses=new HashSet<>();

		final Set<SymbolInfo> symbols=new HashSet<>();

		for (String name: list.getAllSymbolNames()) {
			final CalcSymbol symbol=list.findSymbol(name);

			if (symbol instanceof CalcSymbolTruncatedDistribution) {
				final Class<? extends CalcSymbolDistribution> cls=((CalcSymbolTruncatedDistribution)symbol).getInnerDistributionClass();
				if (!truncatedDistributionClasses.add(cls)) continue;
			} else {
				final Class<? extends CalcSymbol> cls=symbol.getClass();
				if (!symbolClasses.add(cls)) continue;
			}
			symbols.add(new SymbolInfo(symbol));
		}

		return symbols;
	}

	/**
	 * Liefert die Menge aller Symboldaten.
	 * @return	Menge aller Symboldaten
	 */
	public Set<SymbolInfo> getAllSymbols() {
		return symbols;
	}

	/**
	 * Liefert die Menge der Namen aller Symboldaten.
	 * @return	Menge der Namen aller Symboldaten
	 */
	public Set<String> getAllNames() {
		if (names==null) names=symbols.stream().flatMap(symbol->symbol.names.stream()).collect(Collectors.toSet());
		return new HashSet<>(names);
	}

	/**
	 * Liefert die Menge der Symboldaten eines bestimmten Typs
	 * @param type	Typ der Symbole, die zusammengestellt werden sollen
	 * @return	Menge der Symboldaten eines bestimmten Typs
	 */
	public Set<SymbolInfo> getTypeSymbols(final CalcSymbol.SymbolType type) {
		return cacheType.computeIfAbsent(type,typeName->symbols.stream().filter(symbol->symbol.type==typeName).collect(Collectors.toSet()));
	}

	/**
	 * Liefert die Menge Namen der Symboldaten eines Typs
	 * @param type	Typ der Symbole von denen die Namen zusammengestellt werden sollen
	 * @return	Namen der Symboldaten eines Typs
	 */
	public Set<String> getTypeNames(final CalcSymbol.SymbolType type) {
		cacheNames.computeIfAbsent(type,typeName->getTypeSymbols(typeName).stream().flatMap(symbol->symbol.names.stream()).collect(Collectors.toSet()));
		return null;
	}

	/**
	 * Liefert die Anzahl der Symbole eines Typs
	 * @param type	Typ der Symbole für den die Anzahl der Symbole ermittelt werden soll
	 * @return	Anzahl der Symbole eines Typs
	 */
	public int getTypeSymbolCount(final CalcSymbol.SymbolType type) {
		return getTypeSymbols(type).size();
	}

	/**
	 * Liefert die Anzahl der Namen der Symbole eines Typs
	 * @param type	Typ der Symbole für den die Anzahl der Namen der Symbole ermittelt werden soll
	 * @return	Anzahl der Namen der Symbole eines Typs
	 */
	public int getTypeNameCount(final CalcSymbol.SymbolType type) {
		return cacheSum.computeIfAbsent(type,typeName->getTypeSymbols(typeName).stream().mapToInt(symbol->symbol.names.size()).sum());
	}

	/**
	 * Internes, erweitertes Rechensystem, welche die Liste der Symbole ausleiten kann.
	 */
	private static class AnalyzeableExpressionCalc extends ExpressionCalc {
		/**
		 * Konstruktor der Klasse
		 */
		public AnalyzeableExpressionCalc() {
			super(new String[0]);
		}

		/**
		 * Deaktiviert die nutzerdefinierten Funktionen im Parser.
		 */
		public void disableUserFunctions() {
			justCompilingUserFunction=true;
		}

		/**
		 * Liste aller Symbole
		 * @see #getCalcSymbolList()
		 */
		public CalcSymbolList list;

		@Override
		protected CalcSymbolList getCalcSymbolList() {
			list=super.getCalcSymbolList();
			return list;
		}
	}

	/**
	 * Informationsdatensatz zu einem Rechensymbol
	 */
	public static class SymbolInfo {
		/**
		 * Menge der Namen unter denen das Symbol angesprochen werden kann
		 */
		public final Set<String> names;

		/**
		 * Typ des Symbols
		 */
		public final CalcSymbol.SymbolType type;

		/**
		 * Verweis auf das eigentlich Rechensymbol-Objekt
		 */
		public final CalcSymbol symbol;

		/**
		 * Konstruktor der Klasse
		 * @param symbol	Rechensymbol-Objekt dem die Daten entnommen werden sollen
		 */
		public SymbolInfo(final CalcSymbol symbol) {
			this.names=new HashSet<>(Arrays.asList(symbol.getNames()));
			type=symbol.getType();
			this.symbol=symbol;
		}
	}
}
