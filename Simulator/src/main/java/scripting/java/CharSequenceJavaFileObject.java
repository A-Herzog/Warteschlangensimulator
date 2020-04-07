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

import java.net.URI;

import javax.tools.SimpleJavaFileObject;

/**
 * Diese Klasse hält eine java-Datei Dateisystem-frei im Speicher vor.
 * @author Alexander Herzog
 * @see DynamicClassInternalCompilerHalfMemory
 * @see DynamicClassInternalCompilerFullMemory
 */
class CharSequenceJavaFileObject extends SimpleJavaFileObject {
	/**
	 * String representing the source code to be compiled
	 */
	private final String code;

	/**
	 * This constructor will store the source code in the
	 * internal "code" variable and register it as a
	 * source code, using a URI containing the class full name
	 *
	 * @param name
	 *            name of the public class in the source code
	 * @param code
	 *            source code to compile
	 */
	public CharSequenceJavaFileObject(final String name, final String code) {
		super(URI.create("string:///" + name.replace('.','/') + Kind.SOURCE.extension),Kind.SOURCE);
		this.code=code;
	}

	/**
	 * Answers the CharSequence to be compiled. It will give
	 * the source code stored in variable "code"
	 */
	@Override
	public CharSequence getCharContent(boolean ignoreEncodingErrors) {
		return code;
	}
}