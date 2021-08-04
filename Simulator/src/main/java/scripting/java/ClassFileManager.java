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
import java.util.HashMap;
import java.util.Map;

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
	private final Map<String,JavaClassObject> jclassObject;

	/**
	 * Zusätzlicher bzw. normaler Classloader
	 */
	private final ClassLoader additionalClassLoader;

	/**
	 * Will initialize the manager with the specified
	 * standard java file manager
	 * @param standardManager	delegate to this file manager
	 * @param additionalClassLoader	Zusätzlicher bzw. normaler Classloader
	 */
	public ClassFileManager(final M standardManager, final ClassLoader additionalClassLoader) {
		super(standardManager);
		jclassObject=new HashMap<>();
		this.additionalClassLoader=additionalClassLoader;
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
			protected Class<?> findClass(String name) throws ClassNotFoundException {
				try {
					final JavaClassObject classObject=jclassObject.get(name);
					if (classObject==null) {
						if (additionalClassLoader!=null) return additionalClassLoader.loadClass(name);
						return null;
					}
					final byte[] b=classObject.getBytes();
					return super.defineClass(name,b,0,b.length);
				} catch (NoClassDefFoundError e) {
					if (additionalClassLoader!=null) return additionalClassLoader.loadClass(name);
					throw e;
				}
			}
		};
	}

	/**
	 * Gives the compiler an instance of the JavaClassObject
	 * so that the compiler can write the byte code into it.
	 */
	@Override
	public JavaFileObject getJavaFileForOutput(Location location, String className, Kind kind, FileObject sibling) throws IOException {
		final JavaClassObject classObject=new JavaClassObject(className, kind);
		jclassObject.put(className,classObject);
		return classObject;
	}
}
