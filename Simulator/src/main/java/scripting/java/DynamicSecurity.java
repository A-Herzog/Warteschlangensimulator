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

import java.io.FilePermission;
import java.lang.reflect.ReflectPermission;
import java.security.AllPermission;
import java.security.PermissionCollection;
import java.security.Permissions;
import java.security.Policy;
import java.security.ProtectionDomain;
import java.security.SecurityPermission;
import java.util.PropertyPermission;
import java.util.concurrent.Semaphore;

/**
 * Diese Klasse aktiviert einen Security-Manager und eine Policy, um dafür zu sorgen,
 * dass Nutzer-Java-Code möglichst wenig weitere Aktionen über die vorgegebenen
 * Schnittstellen hinaus ausführen können.
 * Die Klasse ist ein Singleton und kann nicht über einen Konstruktur instanziert werden.
 * Stattdessen muss die statische {@link DynamicSecurity#getInstance()} verwendet werden.
 * Die eigentliche Verarbeitung findet im Konstruktor statt, d.h. mit der Instanz kann
 * später auch nichts weiter gemacht werden.
 * @author Alexander Herzog
 * @see <a href="https://blog.jayway.com/2014/06/13/sandboxing-plugins-in-java/">https://blog.jayway.com/2014/06/13/sandboxing-plugins-in-java/</a>
 */
public class DynamicSecurity {

	private static volatile DynamicSecurity instance;
	private static final Semaphore mutex=new Semaphore(1);

	/**
	 * Konstruktor der Klasse.<br>
	 * Kann nicht direkt aufgerufen werden. Stattdessen muss {@link DynamicSecurity#getInstance()} verwendet werden.
	 */
	private DynamicSecurity() {
		setPolicy();
		if (!prepareSecurity()) return;
	}

	private boolean prepareSecurity() {
		if (System.getSecurityManager()==null) {
			System.setSecurityManager(new SecurityManager());
		}

		try {
			System.getSecurityManager().checkPermission(new SecurityPermission("setPolicy"));
			return true;
		} catch (SecurityException e) {
			return false;
		}
	}

	private void setPolicy() {
		Policy.setPolicy(new DynamicSecurityPolicy());
	}

	/**
	 * Liefert die einzige Instanz dieser Klasse.
	 * @return	Instanz der Klasse {@link DynamicSecurity}
	 */
	public static DynamicSecurity getInstance() {
		mutex.acquireUninterruptibly();
		try {
			if (instance==null) instance=new DynamicSecurity();
			return instance;
		} finally {
			mutex.release();
		}
	}

	private class DynamicSecurityPolicy extends Policy {
		private final Permissions restrictedPermissions;
		private final Permissions allPermissions;

		public DynamicSecurityPolicy() {
			restrictedPermissions=new Permissions();
			restrictedPermissions.add(new FilePermission("<<ALL FILES>>","read,readlink,write"));
			restrictedPermissions.add(new PropertyPermission("*","read"));
			restrictedPermissions.add(new RuntimePermission("createClassLoader"));
			restrictedPermissions.add(new RuntimePermission("modifyThreadGroup"));
			restrictedPermissions.add(new RuntimePermission("getStackTrace"));
			restrictedPermissions.add(new RuntimePermission("getenv.JDK_JAVAC_OPTIONS"));
			restrictedPermissions.add(new RuntimePermission("closeClassLoader"));
			restrictedPermissions.add(new RuntimePermission("getProtectionDomain"));
			restrictedPermissions.add(new ReflectPermission("suppressAccessChecks")); /* Den brauchen wir für GraalVM */
			allPermissions=new Permissions();
			allPermissions.add(new AllPermission());
		}

		@Override
		public PermissionCollection getPermissions(ProtectionDomain domain) {
			final String loader=domain.getClassLoader().getClass().getSimpleName();
			if (loader.equals("PlatformClassLoader")) return allPermissions;
			if (loader.equals("AppClassLoader")) {
				if (domain.getCodeSource().getLocation().getProtocol().equals("jrt")) return restrictedPermissions;
				return allPermissions;
			}
			return restrictedPermissions;
		}
	}
}
