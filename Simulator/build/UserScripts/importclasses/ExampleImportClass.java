package importclasses;

/**
 * <p>
 * This class can be loaded by selecting the <b>parent</b> folder of the
 * folder this file is in as the plugins folder in Warteschlangensimulator.
 * </p>
 * 
 * <p>
 * Before loading the class, it must be compiled via the
 * compile scripts in this folder or via the compile button
 * in the plugins folder dialog in Warteschlangensimulator.
 * </p>
 * 
 * <p>
 * To access this class directly you will need to set the provisioning mode
 * in the plugins configuration dialog to allow direct imports.
 * Then you can use this class directly at script stations:<br>
 * <code>ExampleImportClass example=new ExampleImportClass(); exmaple.method();</code><br>
 * There is no need to define any import statements manually at script stations.
 * Warteschlangensimulator is taking care of all imports.
 * </p>
 * 
 * <p>
 * Note that the <code>package</code> specification in line 1 of this file needs to match
 * the folder name this file is in.
 * </p>
 */
public class ExampleImportClass {	
	public int method() {
		return 123;
	}
}