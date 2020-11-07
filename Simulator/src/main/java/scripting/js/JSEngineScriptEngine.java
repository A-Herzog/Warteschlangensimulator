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

import java.util.Map;
import java.util.function.Consumer;

import javax.script.Bindings;
import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;

/**
 * Ausführung von JS-Code über das Scripting-API
 * @author Alexander Herzog
 */
public class JSEngineScriptEngine extends JSEngine {
	/**
	 * Skripting-Engine
	 * @see #initEngine(ScriptEngine, Map)
	 */
	private ScriptEngine engine;

	/**
	 * Konstruktor der Klasse
	 * @param maxExecutionTimeMS	Maximale Skriptlaufzeit
	 * @param output	Ausgabeobjekt
	 */
	public JSEngineScriptEngine(final int maxExecutionTimeMS, final JSOutputWriter output) {
		super(maxExecutionTimeMS,output);
	}

	/**
	 * Initialisiert die zu verwendende Skripting-Engine
	 * @param engine	Skripting-Engine
	 * @param javaObjects	Java-Objekte, die innerhalb des JS-Codes zur Verfügung stehen sollen
	 */
	public void initEngine(final ScriptEngine engine, final Map<String,Object> javaObjects) {
		this.engine=engine;

		try {
			final Bindings bindings=engine.getContext().getBindings(ScriptContext.ENGINE_SCOPE);
			for (Map.Entry<String,Object> entry: javaObjects.entrySet()) bindings.put(entry.getKey(),entry.getValue());
			bindings.put(JSEngine.ENGINE_NAME_BINDING,getEngineName());
			final Consumer<String> print=line->output.addOutput(line+"\n");
			bindings.put("print",print);
			disableJavaClassAccess(bindings);
		} catch (Exception e) {}
	}

	@Override
	protected String getEngineName() {
		return engine.getClass().getSimpleName();
	}

	/**
	 * Löscht bestimmte Klassen und Methoden aus der Javascript-zu-Java-Bindung
	 * um sicher zu stellen, dass von Javascript aus nicht auf die Funktionen
	 * des Hauptprogramms bzw. der Java-Umgebung zugegriffen werden kann.
	 * @param bindings	Javascript-zu-Java-Bindung
	 * @see #initEngine(ScriptEngine, Map)
	 */
	private void disableJavaClassAccess(final Map<String,Object> bindings) {
		bindings.put("com",null);
		bindings.put("java",null);
		bindings.put("javax",null);
		bindings.put("javafx",null);
		bindings.put("jdk",null);
		bindings.put("org",null);
		bindings.put("sun",null);
		bindings.put("Java",null);
		bindings.put("Packages",null);
		bindings.put("arguments",null);

		bindings.remove("exit");
		bindings.remove("quit");
		bindings.remove("JavaImporter");
		bindings.remove("load");
		bindings.remove("loadWithNewGlobal");
		bindings.remove("readFully");
	}

	/**
	 * Später auszuführendes Skript
	 * @see #initScript(String)
	 */
	private String script;

	/**
	 * In ein Objekt übersetztes Skript {@link #script}.
	 * @see #script
	 * @see #initScript(String)
	 */
	private CompiledScript compiledScript;

	@Override
	public boolean initScript(final String script) {
		this.script=script;
		compiledScript=null;

		if (engine instanceof Compilable) {
			final Compilable c=(Compilable)engine;
			try {
				compiledScript=c.compile(script);
			} catch (Exception e) {}
		}

		return true;
	}

	@Override
	protected void execute() throws Exception {
		if (compiledScript==null) {
			engine.eval(script);
		} else {
			compiledScript.eval();
		}
	}
}
