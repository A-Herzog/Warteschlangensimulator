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
package systemtools.commandline;

import java.io.InputStream;
import java.io.PrintStream;

/**
 * Abstrakte Basisklasse aller Kommandozeilenbefehle
 * @author Alexander Herzog
 * @see BaseCommandLineSystem
 */
public abstract class AbstractCommand {

	/**
	 * Gibt die Namen der Parameter an, die diesen Befehl ausf�hren sollen.
	 * @return	Array aus den Namen der Befehle; die Gro�- und Kleinschreibung wird beim sp�teren Vergleich nicht ber�cksichtigt
	 */
	public abstract String[] getKeys();

	/**
	 * Gibt den prim�ren Namen des Befehls an, der in der Hilfe angezeigt werden soll.
	 * Dieser Name sollte mit dem ersten Array-Eintrag von <code>getKeys</code> �bereinstimmen.
	 * @return	Name des Befehls
	 */
	public String getName() {
		return getKeys()[0];
	}

	/**
	 * Gibt an, ob der Befehl in der Befehlshilfe aufgelistet werden soll.
	 * @return	Im Falle von <code>true</code> wird der Befehl in der Hilfe nicht angezeigt.
	 */
	public boolean isHidden() {
		return false;
	}

	/**
	 * Liefert eine kurze, einzeilige Beschreibung des Befehls zur�ck
	 * @return	Kurzbeschreibung des Befehls
	 */
	public abstract String getShortDescription();

	/**
	 * Liefert eine lange, mehrzeilige Beschreibung des Befehls inkl.
	 * Erkl�rung der weiteren Parameter zur�ck
	 * @return	Langbeschreibung des Befehls
	 */
	public abstract String[] getLongDescription();

	/**
	 * Bereitet die Ausf�hrung des Befehls vor
	 * @param additionalArguments	Weitere Kommandozeilenargumente nach dem Befehlsschl�sselwort
	 * @param in	Ein {@link InputStream}-Objekt oder <code>null</code>, �ber das Zeichen von der Konsole gelesen werden k�nnen (<code>null</code>, wenn keine Konsole verf�gbar ist)
	 * @param out	Ein {@link PrintStream}-Objekt, �ber das Texte ausgegeben werden k�nnen.
	 * @return	Gibt <code>null</code> zur�ck, wenn die Vorbereitung erfolgreich war, sonst eine Fehlermeldung.
	 */
	public String prepare(String[] additionalArguments, InputStream in, PrintStream out) {
		return null;
	}

	/**
	 * F�hrt den Befehl aus.
	 * @param allCommands	Liste alle registrierten Befehle
	 * @param in	Ein {@link InputStream}-Objekt oder <code>null</code>, �ber das Zeichen von der Konsole gelesen werden k�nnen (<code>null</code>, wenn keine Konsole verf�gbar ist)
	 * @param out	Ein {@link PrintStream}-Objekt, �ber das Texte ausgegeben werden k�nnen.
	 */
	public abstract void run(AbstractCommand[] allCommands, InputStream in, PrintStream out);

	/**
	 * Pr�ft, ob die angegebene Parameteranzahl korrekt ist und generiert ggf. eine passende Fehlermeldung.
	 * @param min	Minimale Anzahl an weiteren Parmetern (nach dem Befehls-Parameter)
	 * @param max	Maximale Anzahl an weiteren Parmetern (nach dem Befehls-Parameter)
	 * @param additionalArguments	�bergebene weitere Parameter
	 * @return	Gibt im Erfolgsfall <code>null</code> zur�ck, sonst eine Fehlermeldung als String.
	 */
	protected final String parameterCountCheck(int min, int max, String[] additionalArguments) {
		String s=String.format(BaseCommandLineSystem.commandCountIf,getName())+" ";
		String t=". "+String.format(BaseCommandLineSystem.commandCountThenButN,additionalArguments.length);
		if (additionalArguments.length==0) t=". "+BaseCommandLineSystem.commandCountThenBut0;
		if (additionalArguments.length==1) t=". "+BaseCommandLineSystem.commandCountThenBut1;

		if (min==max) {
			if (additionalArguments.length!=min) {
				if (min==0) return s+BaseCommandLineSystem.commandCountThen0+t;
				if (min==1) return s+BaseCommandLineSystem.commandCountThen1+t;
				return s+String.format(BaseCommandLineSystem.commandCountThenN,min);
			}
			return null;
		}

		if (additionalArguments.length<min) {
			if (min==1) return s+BaseCommandLineSystem.commandCountThenAtLeast1+t;
			return s+String.format(BaseCommandLineSystem.commandCountThenAtLeastN,min)+t;
		}

		if (additionalArguments.length>max) {
			if (max==1) return s+BaseCommandLineSystem.commandCountThenMaximum1+t;
			return s+String.format(BaseCommandLineSystem.commandCountThenMaximumN,max)+t;
		}

		return null;
	}

	/**
	 * Pr�ft, ob die angegebene Parameteranzahl korrekt ist und generiert ggf. eine passende Fehlermeldung.
	 * @param count	Geforderte Anzahl an weiteren Parmetern (nach dem Befehls-Parameter)
	 * @param additionalArguments	�bergebene weitere Parameter
	 * @return	Gibt im Erfolgsfall <code>null</code> zur�ck, sonst eine Fehlermeldung als String.
	 */
	protected final String parameterCountCheck(int count, String[] additionalArguments) {
		return parameterCountCheck(count,count,additionalArguments);
	}

	/**
	 * Setzt den Quit-Status (der ggf. an einen Server weitergegeben werden kann).
	 * (Beim Aufruf �ber Kommandozeile nicht n�tig, aber z.B. sinnvoll, wenn Betrieb �ber GUI.)
	 */
	public void setQuit() {}
}