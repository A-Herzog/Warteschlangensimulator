package scripting.js;

import java.util.Map;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.NativeJavaObject;
import org.mozilla.javascript.Script;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.WrapFactory;

/**
 * Ausführung von JS-Code über das Rhino-API
 * @author Alexander Herzog
 */
public class JSEngineRhinoDirect extends JSEngine {
	/**
	 * Kontext zur Skriptausführung
	 * @see #initEngine(Map)
	 * @see #execute()
	 */
	private ScriptableObject scope;

	/**
	 * Hält das übersetzte Skript vor.
	 * @see #initScript(String)
	 * @see #execute()
	 */
	private Script script;

	/**
	 * Wird bei {@link #initScript(String)} mit möglichen
	 * Fehlermeldungen belegt, die dann bei {@link #execute()}
	 * eine Exception auslösen.
	 * @see #initScript(String)
	 * @see #execute()
	 */
	private String compileError;

	/**
	 * System zum Wrappen von Zahlen (schneller bzw. speichersparsamer als die Rhino-interne Implementierung)
	 * @see #execute()
	 * @see FastWrapFactory
	 */
	private final FastWrapFactory wrapFactory=new FastWrapFactory();

	/**
	 * Ergänzte Fassung der Rhino-Context-Factory die sicherstellt,
	 * dass alle Codeoptimierungen aktiv sind.
	 * @see FastContextFactory
	 */
	private final FastContextFactory contextFactory=new FastContextFactory();

	/**
	 * Kontext für die Skriptausführung.
	 * @see #execute()
	 */
	private Context context=null;

	/**
	 * Konstruktor der Klasse
	 * @param maxExecutionTimeMS	Maximale Skriptlaufzeit
	 * @param output	Ausgabeobjekt
	 */
	public JSEngineRhinoDirect(final int maxExecutionTimeMS, final JSOutputWriter output) {
		super(maxExecutionTimeMS,output);

		try (Context cx=contextFactory.enterContext()) {
			scope=cx.initSafeStandardObjects(null,true);
		}
	}

	/**
	 * Fügt in {@link #initEngine(Map)} eine direkt aufrufbare "print"-Funktion
	 * bereit, die Ausgaben an Output.println weiterleitet.
	 * @see #initEngine(Map)
	 */
	private static final String print="function print(str) {Output.println(str);}";

	/**
	 * Initialisiert die zu verwendende Skripting-Engine
	 * @param javaObjects	Java-Objekte, die innerhalb des JS-Codes zur Verfügung stehen sollen
	 * @return	Gibt an, ob die Skripting-Engine erfolgreich initialisiert werden konnte (bzw. also vorhanden ist)
	 */
	public boolean initEngine(final Map<String,Object> javaObjects) {
		try (Context cx=contextFactory.enterContext()) {

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
		}

		return true;
	}

	@Override
	protected String getEngineName() {
		return "RhinoDirect";
	}

	@Override
	public boolean initScript(final String script) {
		try (Context cx=contextFactory.enterContext()) {
			cx.setOptimizationLevel(9);
			this.script=cx.compileString(script,"script",1,null);
		} catch (Exception e) {
			compileError=e.getMessage();
		}

		return true;
	}

	@Override
	protected void execute() throws Exception {
		if (compileError!=null) throw new Exception(compileError);
		context=contextFactory.enterContext(context);
		try {
			context.setWrapFactory(wrapFactory);
			context.setOptimizationLevel(9);
			script.exec(context,scope);
		} finally {
			Context.exit();
		}
	}

	/**
	 * System zum Wrappen von Zahlen (schneller bzw. speichersparsamer als die Rhino-interne Implementierung)
	 * @see JSEngineRhinoDirect#wrapFactory
	 * @see JSEngineRhinoDirect#execute()
	 */
	private static final class FastWrapFactory extends WrapFactory {
		/**
		 * Konstruktor der Klasse
		 */
		public FastWrapFactory() {
			super();
			setJavaPrimitiveWrap(false);
		}
	}

	/**
	 * Ergänzte Fassung der Rhino-Context-Factory die sicherstellt,
	 * dass alle Codeoptimierungen aktiv sind.
	 * @see JSEngineRhinoDirect#contextFactory
	 */
	private static final class FastContextFactory extends ContextFactory {
		/**
		 * Konstruktor der Klasse
		 */
		public FastContextFactory() {
			/*
			 * Wird nur benötigt, um einen JavaDoc-Kommentar für diesen (impliziten) Konstruktor
			 * setzen zu können, damit der JavaDoc-Compiler keine Warnung mehr ausgibt.
			 */
		}

		@Override
		protected boolean hasFeature(Context cx, int featureIndex) {
			if (featureIndex==Context.FEATURE_INTEGER_WITHOUT_DECIMAL_PLACE) return true;
			return super.hasFeature(cx,featureIndex);
		}

		@Override
		protected void onContextCreated(Context cx) {
			cx.setLanguageVersion(Context.VERSION_ES6);
			cx.setOptimizationLevel(9);
			cx.setGeneratingDebug(false);
		}
	}
}