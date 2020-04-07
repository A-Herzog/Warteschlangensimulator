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

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import language.Language;

/**
 * Basisklasse für die Ausführung von JS-Code
 * @author Alexander Herzog
 * @see JSBuilder
 */
public abstract class JSEngine {
	/**
	 * Name über den im JS-Code der Name der Engine abrufbar sein soll
	 */
	protected static final String ENGINE_NAME_BINDING="JS_ENGINE_NAME";

	private final int maxExecutionTimeMS;
	private final ThreadPoolExecutor executorPool;
	private String lastResult;
	private long lastExecutionTimeMS;
	private long executionCount;

	/**
	 * Ausgabeobjekt (wird vom Konstruktor gesetzt)
	 */
	protected final JSOutputWriter output;

	/**
	 * Konstruktor der Klasse
	 * @param maxExecutionTimeMS	Maximale Skriptlaufzeit
	 * @param output	Ausgabeobjekt
	 */
	public JSEngine(final int maxExecutionTimeMS, final JSOutputWriter output) {
		this.maxExecutionTimeMS=maxExecutionTimeMS;
		executorPool=new ThreadPoolExecutor(0,1,2,TimeUnit.SECONDS,new ArrayBlockingQueue<Runnable>(1));
		lastResult="";
		executionCount=0;
		lastExecutionTimeMS=maxExecutionTimeMS+1;
		this.output=output;
	}

	/**
	 * Liefert den Namen der Skript-Engine.
	 * @return	Name der Skript-Engine
	 */
	protected abstract String getEngineName();

	/**
	 * Lädt ein Skript in das Objekt (und kompiliert es ggf. usw.)
	 * @param script	Später auszuführendes Skript
	 * @return	Liefert <code>true</code>, wenn das Skript erfolgreich geladen werden konnte
	 */
	public abstract boolean initScript(final String script);

	/**
	 * Wird intern aufgerufen, wenn das eigentliche Skript ausgeführt werden soll.
	 * @see JSEngine#run()
	 * @throws Exception	Kann beliebige Exceptions innerhalb der JS-Verarbeitung liefern.
	 */
	protected abstract void execute() throws Exception;

	private final Callable<Boolean> scriptCallable=()->{execute(); return true;};

	/**
	 * Ausführung des Skriptes.
	 * @return	Liefert <code>true</code>, wenn das Skript erfolgreich ausgeführt werden konnte.
	 */
	public boolean run() {
		if (scriptCallable==null) {
			lastResult=Language.tr("Statistics.Filter.NoScript");
			return false;
		}

		output.reset();

		final long executionStartTime=System.currentTimeMillis();

		boolean result=false;
		if (executionCount>=100 && lastExecutionTimeMS<50) {
			/* Im Hauptthread wenn unproblematisch. */
			try {
				execute();
				return true;
			} catch (Exception e) {
				output.addExceptionMessage(e);
				lastResult=output.getResults();
				result=false;
			}
		} else {
			/* Sonst erstmal in eigenem Thread testen. */
			try {
				result=executeCallable(scriptCallable);
			} catch (Exception e) {
				output.addExceptionMessage(e);
				lastResult=output.getResults();
				result=false;
			}
		}

		lastExecutionTimeMS=System.currentTimeMillis()-executionStartTime;
		executionCount++;

		return result;
	}

	/**
	 * Liefert die Ausgabe des Skriptes.
	 * @return	Ausgabe des Skriptes
	 */
	public String getResult() {
		return lastResult;
	}

	private boolean executeCallable(final Callable<Boolean> callable) {
		final Future<Boolean> result=executorPool.submit(callable);
		try {
			result.get(maxExecutionTimeMS,TimeUnit.MILLISECONDS);
			lastResult=output.getResults();
			return true;
		} catch (InterruptedException | ExecutionException | TimeoutException e) {
			if (e instanceof TimeoutException) {
				lastResult=Language.tr("Statistics.Filter.MaximumScriptRunTimeExceeded");
			} else {
				/* Exceptions dieser Art werden nur von GraalJSNative verwendet. */
				output.addOutput(Language.tr("Statistics.Filter.GeneralError"));
				output.addExceptionMessage(e);
				lastResult=output.getResults();
			}
			return false;
		}
	}
}
