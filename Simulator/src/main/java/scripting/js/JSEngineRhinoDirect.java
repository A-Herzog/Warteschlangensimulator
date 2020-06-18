package scripting.js;

import java.util.Map;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.NativeJavaObject;
import org.mozilla.javascript.Script;
import org.mozilla.javascript.ScriptableObject;

/**
 * Ausführung von JS-Code über das Rhino-API
 * @author Alexander Herzog
 */
public class JSEngineRhinoDirect extends JSEngine {
	private ScriptableObject scope;
	private Script script;
	private String compileError;

	/**
	 * Konstruktor der Klasse
	 * @param maxExecutionTimeMS	Maximale Skriptlaufzeit
	 * @param output	Ausgabeobjekt
	 */
	public JSEngineRhinoDirect(final int maxExecutionTimeMS, final JSOutputWriter output) {
		super(maxExecutionTimeMS,output);

		final Context cx=Context.enter();
		try {
			scope=cx.initSafeStandardObjects(null,true);
		} finally {
			Context.exit();
		}
	}

	private final static String print="function print(str) {Output.println(str);}";

	/**
	 * Initialisiert die zu verwendende Skripting-Engine
	 * @param javaObjects	Java-Objekte, die innerhalb des JS-Codes zur Verfügung stehen sollen
	 * @return	Gibt an, ob die Skripting-Engine erfolgreich initialisiert werden konnte (bzw. also vorhanden ist)
	 */
	public boolean initEngine(final Map<String,Object> javaObjects) {
		final Context cx=Context.enter();
		try {

			for (Map.Entry<String,Object> entry: javaObjects.entrySet()) {
				final Object obj=entry.getValue();
				if ((obj instanceof Integer) || (obj instanceof Double) || (obj instanceof String)) {
					scope.put(entry.getKey(),scope,obj);
				} else {
					scope.put(entry.getKey(),scope,new NativeJavaObject(scope,obj,obj.getClass()));
				}
			}

			scope.put(JSEngine.ENGINE_NAME_BINDING,scope,getEngineName());

			cx.evaluateString(scope,print,"print",1,null);

		} catch (Exception e) {
			return false;
		} finally {
			Context.exit();
		}

		return true;
	}

	@Override
	protected String getEngineName() {
		return "RhinoDirect";
	}

	@Override
	public boolean initScript(final String script) {
		final Context cx=Context.enter();
		try {
			cx.setOptimizationLevel(9);
			this.script=cx.compileString(script,"script",1,null);
		} catch (Exception e) {
			compileError=e.getMessage();
		} finally {
			Context.exit();
		}

		return true;
	}

	@Override
	protected void execute() throws Exception {
		if (compileError!=null) throw new Exception(compileError);
		final Context cx=Context.enter();
		try {
			cx.getWrapFactory().setJavaPrimitiveWrap(false);
			cx.setOptimizationLevel(9);
			script.exec(cx,scope);
		} finally {
			Context.exit();
		}
	}
}
