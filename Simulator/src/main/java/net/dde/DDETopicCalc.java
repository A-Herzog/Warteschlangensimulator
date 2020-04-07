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
package net.dde;

import java.util.ArrayList;
import java.util.List;

import mathtools.NumberTools;
import ui.AnimationPanel;
import ui.MainPanel;

/**
 * Topic für den DDE-Server zum Berechnen von Formelausdrücken im Kontext des Simulators
 * @author Alexander Herzog
 * @see SimulationDDEServer
 */
public class DDETopicCalc extends DDETopic {
	private static final int MAX_LAST_REQUESTS=100;

	private final MainPanel mainPanel;
	private final List<String> lastRequests;
	private final List<String> lastValues;

	/**
	 * Konstruktor der Klasse
	 * @param server	DDE-Server
	 * @param mainPanel	Haupt-Panel des Simulators (über das auf das Animations-Panel verlinkt wird welches die eigentlichen Berechnungen durchführt)
	 */
	public DDETopicCalc(final DDEServerSystem server, final MainPanel mainPanel) {
		super(server,"Calc");
		this.mainPanel=mainPanel;
		lastRequests=new ArrayList<>();
		lastValues=new ArrayList<>();
	}

	@Override
	protected boolean isNotListedItemSupported(final String itemName) {
		return true;
	}

	private void updateRecentlyUsedList(final String key, final String value) {
		final int index=lastRequests.indexOf(key);
		if (index>=0) {
			/* Schon in Liste, nur nach oben schieben */
			if (index>0) { /* Schon ganz oben? */
				lastRequests.remove(index);
				lastRequests.add(0,key);
				lastValues.remove(index);
				lastValues.add(0,value);
			}
		} else {
			lastRequests.add(0,key);
			lastValues.add(0,value);
			final int size=lastRequests.size();
			if (size>MAX_LAST_REQUESTS) {
				lastRequests.remove(size-1);
				lastValues.remove(size-1);
			}
		}
	}

	private synchronized String calcExpression(final String expression) {
		if (expression==null || expression.trim().isEmpty()) return "";
		if (!(mainPanel.currentPanel instanceof AnimationPanel)) return "";

		AnimationPanel animationPanel=(AnimationPanel)mainPanel.currentPanel;
		final Double D=animationPanel.calculateExpression(expression);
		return (D==null)?"":NumberTools.formatSystemNumber(D.doubleValue());
	}

	@Override
	protected String getNotListedItem(final String itemName) {
		final String value=calcExpression(itemName);
		updateRecentlyUsedList(itemName,value);
		return value;
	}

	@Override
	protected List<String> getItemsToUpdate() {
		List<String> list=null;

		for (int i=0;i<lastRequests.size();i++) {
			final String expression=lastRequests.get(i);
			final String value=calcExpression(expression);
			if (!value.equals(lastValues.get(i))) {
				lastValues.set(i,value);
				if (list==null) list=new ArrayList<>();
				list.add(expression);
				/* System.out.println("UpdateNotify: "+expression+"="+value); */
			}
		}

		return list;
	}
}
