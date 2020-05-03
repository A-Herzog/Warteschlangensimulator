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

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Ausführung von JS-Code über das Graal-API
 * @author Alexander Herzog
 */
public class JSEngineGraalNative extends JSEngine {
	private Class<?> contextClass=null;
	private Object contextObj=null;
	private Method contextEval=null;

	private Class<?> sourceClass=null;
	private Object sourceObj;

	/**
	 * Konstruktor der Klasse
	 * @param maxExecutionTimeMS	Maximale Skriptlaufzeit
	 * @param output	Ausgabeobjekt
	 */
	public JSEngineGraalNative(final int maxExecutionTimeMS, final JSOutputWriter output) {
		super(maxExecutionTimeMS,output);
	}

	private Class<?> getClassFromName(final String name) {
		try {
			return Class.forName(name);
		} catch (ClassNotFoundException | SecurityException | IllegalArgumentException e) {
			return null;
		}
	}

	/**
	 * Initialisiert die zu verwendende Skripting-Engine
	 * @param javaObjects	Java-Objekte, die innerhalb des JS-Codes zur Verfügung stehen sollen
	 * @return	Gibt an, ob die Skripting-Engine erfolgreich initialisiert werden konnte (bzw. also vorhanden ist)
	 */
	public boolean initEngine(final Map<String,Object> javaObjects) {
		/*
		private org.graalvm.polyglot.Source source;
		private org.graalvm.polyglot.Context context;

		final org.graalvm.polyglot.Context.Builder builder=org.graalvm.polyglot.Context.newBuilder("js");
		builder.allowHostAccess(HostAccess.ALL);
		context=builder.build();
		org.graalvm.polyglot.Value binding=context.getBindings("js");
		for (Map.Entry<String,Object> entry: javaObjects.entrySet()) binding.putMember(entry.getKey(),entry.getValue());
		binding.putMember(JSEngine.ENGINE_NAME_BINDING,getEngineName());
		final Consumer<String> print=line->output.addOutput(line+"\n");
		binding.putMember("print",print);
		return true;
		 */

		/* final org.graalvm.polyglot.Context.Builder builder=org.graalvm.polyglot.Context.newBuilder("js"); */
		Object builder=null;
		try {
			final Class<?> classContext=getClassFromName("org.graalvm.polyglot.Context");
			if (classContext==null) return false;
			final Method method=classContext.getMethod("newBuilder",String[].class);
			if (method==null) return false;
			final Object obj=new String[] {"js"};
			builder=method.invoke(null,obj);
			if (builder==null) return false;
		} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e)  {
			return false;
		}

		/* org.graalvm.polyglot.HostAccess Klasse */
		Class<?> classHostAccess=getClassFromName("org.graalvm.polyglot.HostAccess");
		if (classHostAccess==null) return false;

		/* HostAccess.ALL */
		Object hostAccessAll=null;
		try {
			final Field field=classHostAccess.getField("ALL");
			if (field==null) return false;
			hostAccessAll=field.get(null);
			if (hostAccessAll==null) return false;
		} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
			return false;
		}

		/* builder.allowHostAccess(HostAccess.ALL); */
		try {
			final Method allowHostAccess=builder.getClass().getMethod("allowHostAccess",classHostAccess);
			if (allowHostAccess==null) return false;
			allowHostAccess.invoke(builder,hostAccessAll);
		} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			return false;
		}

		/* context=builder.build(); */
		try {
			final Method build=builder.getClass().getMethod("build");
			if (build==null) return false;
			contextObj=build.invoke(builder);
		} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			return false;
		}

		/* Context Klasse */
		contextClass=contextObj.getClass();
		if (contextClass==null) return false;

		/* org.graalvm.polyglot.Value binding=context.getBindings("js"); */
		Object binding=null;
		try {
			final Method getBindings=contextClass.getMethod("getBindings",String.class);
			if (getBindings==null) return false;
			binding=getBindings.invoke(contextObj,"js");
			if (binding==null) return false;
		} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			return false;
		}

		/* binding.putMember */
		try {
			final Method putMember=binding.getClass().getMethod("putMember",String.class,Object.class);
			if (putMember==null) return false;
			for (Map.Entry<String,Object> entry: javaObjects.entrySet()) putMember.invoke(binding,entry.getKey(),entry.getValue());
			putMember.invoke(binding,JSEngine.ENGINE_NAME_BINDING,getEngineName());
			final Consumer<String> print=line->output.addOutput(line+"\n");
			putMember.invoke(binding,"print",print);
		} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			return false;
		}

		/* org.graalvm.polyglot.Source Klasse */
		sourceClass=getClassFromName("org.graalvm.polyglot.Source");
		if (sourceClass==null) return false;

		/* context.eval */
		try {
			contextEval=contextClass.getMethod("eval",sourceClass);
			if (contextEval==null) return false;
		} catch (NoSuchMethodException | SecurityException e) {
			return false;
		}

		return true;
	}

	@Override
	protected String getEngineName() {
		return "GraalJSNative";
	}

	@Override
	public boolean initScript(String script) {
		/* source=org.graalvm.polyglot.Source.create("js",script); */
		try {
			final Method sourceCreate=sourceClass.getMethod("create",String.class,CharSequence.class);
			if (sourceCreate==null) return false;
			sourceObj=sourceCreate.invoke(null,"js",script);
			if (sourceObj==null) return false;
		} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			return false;
		}

		return true;
	}

	@Override
	protected void execute() throws Exception {
		/* context.eval(source); */
		contextEval.invoke(contextObj,sourceObj);
	}
}
