/**
 * This class can be loaded by selecting the folder this
 * file is in as the plugins folder in Warteschlangensimulator.
 *
 * Before loading the class, it must be compiled via the
 * compile scripts in this folder.
 * 
 * The example method can be accessed then at script stations via
 * <code>sim.getSystem().runPlugin("ExampleClass","exampleFunction",null);</code>.
 * It will return "123" as an <code>Integer</code> object.
 */
public class ExampleClass {
	public Object exampleFunction(final scripting.java.RuntimeInterface runtime, final scripting.java.SystemInterface system, final Object data) {	
		return 123;
	}
}