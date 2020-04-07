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

/**
 * Ruft eine dynmiasche Methode mit einem bestimmten Parameter auf.
 * @author Alexander Herzog
 * @see DynamicMethod
 */
public class DynamicRunner {
	private String script;
	private DynamicStatus status;
	private String error;
	private final DynamicMethod method;

	/**
	 * Schnittstelle zum Simulator f�r den Nutzercode.<br>
	 * Wird der nutzererstellten Methode als Parameter �bergeben.
	 */
	public final SimulationImpl parameter;

	/**
	 * Konstruktor f�r den Fall, dass der Runner nur als Fehlerobjekt verwendet werden soll.
	 * @param script	Text, der als java-Methode interpretiert werden soll. Der Text muss mit dem R�ckgabewert beginnen, darf also keinen Access-Modifier enthalten.
	 * @param status	Status der Kompilierung
	 * @param error	Optionale zus�tzliche Fehlermeldung	(kann <code>null</code> sein)
	 */
	public DynamicRunner(final String script, final DynamicStatus status, final String error) {
		this.script=script;
		this.status=status;
		this.error=error;
		method=null;
		parameter=new SimulationImpl();
	}

	/**
	 * Konstruktor der Klasse
	 * @param method	Dynamische Methode
	 */
	public DynamicRunner(final DynamicMethod method) {
		script=method.getScript();
		status=DynamicStatus.OK;
		error=null;
		this.method=method;
		parameter=new SimulationImpl();
	}

	/**
	 * F�hrt die Methode mit {@link DynamicRunner#parameter} als Parameter aus
	 * @return	R�ckgabewert der Methode
	 */
	public Object run() {
		if (method==null) return DynamicStatus.RUN_ERROR;
		final Object result=method.invokeDynamicMethod(parameter);
		if (result instanceof DynamicStatus) {
			status=(DynamicStatus)result;
			error=method.getError();
			return null;
		} else {
			status=DynamicStatus.OK;
			error=null;
			return result;
		}
	}

	/**
	 * Liefert den Status der Kompilierung bzw. des Aufrufs.
	 * @return	Status der Kompilierung bzw. des Aufrufs
	 */
	public DynamicStatus getStatus() {
		return status;
	}

	/**
	 * Liefert einen optionalen zus�tzlichen Fehlertext zur Kompilierung oder zum Aufruf.
	 * @return	Optionaler zus�tzlicher Fehlertext zur Kompilierung oder zum Aufruf (kann auch im Falle eines Fehlers <code>null</code> sein)
	 */
	public String getError() {
		return error;
	}

	/**
	 * Liefert das Skript, das der dynamischen Methode zugrunde liegt, als Text.
	 * @return	Skript, das der dynamischen Methode zugrunde liegt, als Text
	 */
	public String getScript() {
		return script;
	}
}
