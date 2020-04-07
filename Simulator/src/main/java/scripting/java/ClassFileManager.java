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

import java.io.IOException;
import java.security.SecureClassLoader;

import javax.tools.FileObject;
import javax.tools.ForwardingJavaFileManager;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.JavaFileObject.Kind;

/**
 * Diese Klasse ermöglicht den Zugriff auf zur Laufzeit selbst kompilierte Klassen
 * @param <M>	Erweiterung von {@link JavaFileManager}
 */
public class ClassFileManager<M extends JavaFileManager> extends ForwardingJavaFileManager<M> {
	/**
	 * Instance of JavaClassObject that will store the
	 * compiled bytecode of our class
	 */
	private JavaClassObject jclassObject;

	/**
	 * Will initialize the manager with the specified
	 * standard java file manager
	 * @param standardManager	delegate to this file manager
	 */
	public ClassFileManager(final M standardManager) {
		super(standardManager);
	}

	/**
	 * Will be used by us to get the class loader for our
	 * compiled class. It creates an anonymous class
	 * extending the SecureClassLoader which uses the
	 * byte code created by the compiler and stored in
	 * the JavaClassObject, and returns the Class for it
	 */
	@Override
	public ClassLoader getClassLoader(Location location) {
		return new SecureClassLoader() {
			@Override
			protected Class<?> findClass(String name)
					throws ClassNotFoundException {
				byte[] b = jclassObject.getBytes();
				return super.defineClass(name, jclassObject
						.getBytes(), 0, b.length);
			}
		};
	}

	/**
	 * Gives the compiler an instance of the JavaClassObject
	 * so that the compiler can write the byte code into it.
	 */
	@Override
	public JavaFileObject getJavaFileForOutput(Location location,
			String className, Kind kind, FileObject sibling)
					throws IOException {
		jclassObject = new JavaClassObject(className, kind);
		return jclassObject;
	}
}
