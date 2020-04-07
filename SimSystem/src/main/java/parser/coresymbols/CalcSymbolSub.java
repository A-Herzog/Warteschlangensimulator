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
package parser.coresymbols;

import parser.CalcSystem;

/**
 * Diese Klasse wird intern verwendet, um mehrere Parameter einer Funktion zusammenzufassen.
 * @author Alexander Herzog
 * @see CalcSymbolPreOperator
 */
public final class CalcSymbolSub extends CalcSymbol {
	/**
	 * Liste der einzelnen Parameter
	 */
	private CalcSymbol[] sub;

	@Override
	public String[] getNames() {
		return new String[]{};
	}

	@Override
	public SymbolType getType() {
		return CalcSymbol.SymbolType.TYPE_SUB;
	}

	/**
	 * Stellt die Liste der Parameter ein
	 * @param sub	Liste der Parameter ein
	 */
	public void setData(final CalcSymbol[] sub) {
		this.sub=sub;
	}

	/**
	 * Liefert die Liste der Parameter ein
	 * @return	Liste der Parameter ein
	 */
	public CalcSymbol[] getData() {
		return sub;
	}

	@Override
	public Double getValue(final CalcSystem calc) {
		if (sub==null) return null;
		if (sub.length!=1) return null;
		return sub[0].getValue(calc);
	}

	@Override
	public CalcSymbol cloneSymbol() {
		CalcSymbolSub clone=(CalcSymbolSub)super.cloneSymbol();
		if (sub!=null) {
			clone.sub=new CalcSymbol[sub.length];
			for (int i=0;i<sub.length;i++) clone.sub[i]=sub[i].cloneSymbol();
		}
		return clone;
	}

	@Override
	public Object getSimplify() {
		if (sub!=null) {
			if (sub.length==1) return sub[0].getSimplify();
			CalcSymbolSub clone;
			try {clone=(CalcSymbolSub)clone();} catch (CloneNotSupportedException e) {return null;}
			clone.sub=new CalcSymbol[sub.length];
			for (int i=0;i<sub.length;i++) {
				Object o=sub[i].getSimplify();
				if (o instanceof Double) {
					CalcSymbolNumber num=new CalcSymbolNumber();
					num.setValue((Double)o);
					clone.sub[i]=num;
				}
				if (o instanceof CalcSymbol) {
					clone.sub[i]=(CalcSymbol)o;
				}
			}
			return clone;
		}
		return this;
	}
}
