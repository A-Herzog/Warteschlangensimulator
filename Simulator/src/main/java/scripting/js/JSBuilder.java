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
package scripting.js;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import tools.SetupData;

/**
 * Erstellt ein JS-Engine-Objekt
 * @author Alexander Herzog
 */
public class JSBuilder {
	/**
	 * Maximale Skriptlaufzeit (wird im Konstruktor angegeben)
	 */
	public final int maxExecutionTimeMS;

	/**
	 * Name der verwendeten JS-Engine (wird auf Basis des im Konstruktor angegebenen Nutzerwunsches gewählt)
	 */
	public final JSEngineNames engineName;

	/**
	 * Ausgabeobjekt
	 */
	public final JSOutputWriter output;

	private final Map<String,Object> binding;

	/**
	 * Konstruktor der Klasse
	 * @param maxExecutionTimeMS	Maximale Skriptlaufzeit
	 * @param engineName	Nutzerwunsch bzgl. der zu verwendenden Engine
	 * @param outputCallback	Optionales Callback welches die Daten des Ausgabeobjektes erhalten soll
	 */
	public JSBuilder(final int maxExecutionTimeMS, final JSEngineNames engineName, final Consumer<String> outputCallback) {
		this.maxExecutionTimeMS=maxExecutionTimeMS;
		this.engineName=JSEngineNames.bestMatch(engineName);
		output=new JSOutputWriter(outputCallback);
		binding=new HashMap<>();
	}

	/**
	 * Konstruktor der Klasse
	 * @param maxExecutionTimeMS	Maximale Skriptlaufzeit
	 * @param engineName	Nutzerwunsch bzgl. der zu verwendenden Engine
	 */
	public JSBuilder(final int maxExecutionTimeMS, final JSEngineNames engineName) {
		this(maxExecutionTimeMS,engineName,null);
	}

	/**
	 * Konstruktor der Klasse
	 * @param maxExecutionTimeMS	Maximale Skriptlaufzeit
	 */
	public JSBuilder(final int maxExecutionTimeMS) {
		this(maxExecutionTimeMS,getEngineFromSetup(),null);
	}

	/**
	 * Konstruktor der Klasse
	 * @param maxExecutionTimeMS	Maximale Skriptlaufzeit
	 * @param outputCallback	Optionales Callback welches die Daten des Ausgabeobjektes erhalten soll
	 */
	public JSBuilder(final int maxExecutionTimeMS, final Consumer<String> outputCallback) {
		this(maxExecutionTimeMS,getEngineFromSetup(),outputCallback);
	}

	private static JSEngineNames getEngineFromSetup() {
		return JSEngineNames.fromName(SetupData.getSetup().jsEngine);
	}

	/**
	 * Fügt ein Objekt zu der Liste der in JS verfügbaren Objekte hinzu
	 * @param name	Names des Objektes in JS
	 * @param object	Java-Objekt
	 */
	public void addBinding(final String name, final Object object) {
		binding.put(name,object);
	}

	private JSEngine buildScriptEngineBased(final JSEngineNames engineName) {
		final ScriptEngineManager manager=new ScriptEngineManager();
		final ScriptEngine engine=manager.getEngineByName(engineName.name);
		final JSEngineScriptEngine runner=new JSEngineScriptEngine(maxExecutionTimeMS,output);
		runner.initEngine(engine,binding);
		return runner;
	}

	/*
	private JSEngine buildRhinoEngine() {
		try {
			phobos_fast.script.javascript.RhinoScriptEngineFactory factory=new phobos_fast.script.javascript.RhinoScriptEngineFactory();
			final Context cx=Context.enter();
			cx.setOptimizationLevel(9);
			final ScriptEngine engine=factory.getScriptEngine();
			final JSEngineScriptEngine runner=new JSEngineScriptEngine(maxExecutionTimeMS,output);
			runner.initEngine(engine,binding);
			return runner;
		} catch (Exception | UnsupportedClassVersionError e) {
			return null;
		}
	}
	 */

	private JSEngine buildRhinoEngineDirect() {
		final JSEngineRhinoDirect runner=new JSEngineRhinoDirect(maxExecutionTimeMS,output);
		if (!runner.initEngine(binding)) return null;
		return runner;
	}

	private JSEngine buildGraalNative() {
		final JSEngineGraalNative runner=new JSEngineGraalNative(maxExecutionTimeMS,output);
		if (!runner.initEngine(binding)) return null;
		return runner;
	}

	/**
	 * Erstellt einen JS-Runner auf Basis der Einstellungen
	 * @return	JS-Runner
	 */
	public JSEngine build() {
		switch (engineName) {
		case RHINO:
			/* return buildRhinoEngine(); */
			return buildRhinoEngineDirect();
		case NASHORN:
			return buildScriptEngineBased(JSEngineNames.NASHORN);
		case GRAALJS:
			System.setProperty("polyglot.js.nashorn-compat","true");
			return buildScriptEngineBased(JSEngineNames.GRAALJS);
		case GRAALJSNative:
			System.clearProperty("polyglot.js.nashorn-compat");
			return buildGraalNative();
		default:
			return null;
		}
	}
}
