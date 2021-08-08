import scripting.java.*;
import java.lang.*;
import java.math.*;
import java.util.*;
import java.util.function.*;
import java.util.stream.*;

/**
 * <p>
 * This class can be loaded by selecting the folder this
 * file is in as the plugins folder in Warteschlangensimulator.
 * </p>
 *
 * <p>
 * Before loading the class, it must be compiled via the
 * compile scripts in this folder or via the compile button
 * in the plugins folder dialog in Warteschlangensimulator.
 * </p>
 * 
 * <p>
 * The example method can be accessed then at script stations via<br>
 * <code>sim.getSystem().runPlugin("ExamplePluginClass","exampleFunction",null);</code>.<br>
 * It will return "123" as an <code>Integer</code> object.
 * </p>
 */
public class ExamplePluginClass {
	public Object exampleFunction(final scripting.java.RuntimeInterface runtime, final scripting.java.SystemInterface system, final Object data) {	
		return 123;
	}
}