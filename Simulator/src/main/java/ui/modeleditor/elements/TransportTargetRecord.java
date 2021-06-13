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
package ui.modeleditor.elements;

import language.Language;
import simulator.editmodel.FullTextSearch;
import ui.modeleditor.coreelements.ModelElementBox;

/**
 * Speichert die Daten zu einem möglichen Routing-Ziel und der
 * zugehörigen Bedingung (Ausdruck oder Kundentyp) unter der
 * dieses Ziel angesteuert wird.
 * @author Alexander Herzog
 * @see TransportTargetSystem
 */
public final class TransportTargetRecord implements Cloneable {
	/**
	 * Wonach soll das Routing gemäß dieses Datensatzes erfolgen?
	 * @author Alexander Herzog
	 * @see TransportTargetRecord#routingMode
	 */
	public enum RoutingMode {
		/** Routing-Datensatz wählen, wenn Kundentyp passt */
		ROUTING_MODE_CLIENT_TYPE,
		/** Routing-Datensatz wählen, wenn Rechanausdruck-Bedingung erfüllt ist */
		ROUTING_MODE_EXPRESSION
	}

	/**
	 * Wonach soll das Routing gemäß dieses Datensatzes erfolgen?
	 * @see RoutingMode
	 */
	public RoutingMode routingMode;

	/**
	 * Zu erfüllende Bedingung (Kundentyp oder Rechenausdruck)
	 */
	public String routingCondition;

	/**
	 * Zielstation, die angesteuert werden soll, wenn dieser Datensatz verwendet wird.
	 */
	public String station;

	/**
	 * Konstruktor der Klasse
	 */
	public TransportTargetRecord() {
		routingMode=RoutingMode.ROUTING_MODE_CLIENT_TYPE;
		routingCondition="";
		station="";
	}

	/**
	 * Vergleicht dieses Objekt mit einem anderen {@link TransportTargetRecord}-Objekt.
	 * @param otherTransportRouting	Zweites {@link TransportTargetRecord}-Objekt das mit diesem verglichen werden soll.
	 * @return	Liefert <code>true</code>, wenn die beiden Objekte inhaltlich identisch sind.
	 */
	public boolean equalsTransportRouting(final TransportTargetRecord otherTransportRouting) {
		if (otherTransportRouting==null) return false;
		if (routingMode!=otherTransportRouting.routingMode) return false;
		if (!routingCondition.equals(otherTransportRouting.routingCondition)) return false;
		if (!station.equals(otherTransportRouting.station)) return false;
		return true;
	}

	/**
	 * Liefert einen Infotext zu der Bedingung zur Aktivierung dieses Routing-Ziels.
	 * @return	Infotext zu der Bedingung zur Aktivierung dieses Routing-Ziels
	 */
	public String getConditionText() {
		switch (routingMode) {
		case ROUTING_MODE_CLIENT_TYPE: return Language.tr("Surface.TransportSource.XML.Route.ClientType.Info")+" \""+routingCondition+"\"";
		case ROUTING_MODE_EXPRESSION: return Language.tr("Surface.TransportSource.XML.Route.Expression.Info")+" \""+routingCondition+"\"";
		default: return "";
		}
	}

	@Override
	public TransportTargetRecord clone() {
		final TransportTargetRecord clone=new TransportTargetRecord();
		clone.routingMode=routingMode;
		clone.routingCondition=routingCondition;
		clone.station=station;
		return clone;
	}

	/**
	 * Liefert ein neues {@link TransportTargetRecord}-Objekt in der Betriebsart "Routing nach Kundentyp"
	 * @param clientType	Kundentyp, der die Zielstation festlegt
	 * @param station	Zielstation
	 * @return	Neues {@link TransportTargetRecord}-Objekt
	 */
	public static TransportTargetRecord getByClientType(final String clientType, final String station) {
		final TransportTargetRecord route=new TransportTargetRecord();
		route.routingMode=TransportTargetRecord.RoutingMode.ROUTING_MODE_CLIENT_TYPE;
		route.routingCondition=clientType;
		route.station=station;
		return route;
	}

	/**
	 * Liefert ein neues {@link TransportTargetRecord}-Objekt in der Betriebsart "Routing nach Ausdruck"
	 * @param expression	Ausdruck, der, wenn er erfüllt ist, diesen Datensatz aktiviert
	 * @param station	Zielstation
	 * @return	Neues {@link TransportTargetRecord}-Objekt
	 */
	public static TransportTargetRecord getByExpression(final String expression, final String station) {
		final TransportTargetRecord route=new TransportTargetRecord();
		route.routingMode=TransportTargetRecord.RoutingMode.ROUTING_MODE_EXPRESSION;
		route.routingCondition=expression;
		route.station=station;
		return route;
	}

	/**
	 * Sucht einen Text in den Daten dieses Datensatzes.
	 * @param searcher	Such-System
	 * @param station	Station an der dieser Datensatz verwendet wird
	 * @see FullTextSearch
	 */
	public void search(final FullTextSearch searcher, final ModelElementBox station) {
		switch (routingMode) {
		case ROUTING_MODE_CLIENT_TYPE:
			searcher.testString(station,Language.tr("Editor.DialogBase.Search.TransportDestination.ForClientType"),routingCondition,newClientType->{routingCondition=newClientType;});
			break;
		case ROUTING_MODE_EXPRESSION:
			searcher.testString(station,Language.tr("Editor.DialogBase.Search.TransportDestination.ForCondition"),routingCondition,newCondition->{routingCondition=newCondition;});
			break;
		}

		searcher.testString(station,Language.tr("Editor.DialogBase.Search.TransportDestination"),this.station,newDestinationStation->{this.station=newDestinationStation;});
	}
}