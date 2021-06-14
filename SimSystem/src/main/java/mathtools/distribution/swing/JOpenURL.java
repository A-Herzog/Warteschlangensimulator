/**
 * Copyright 2021 Alexander Herzog
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
package mathtools.distribution.swing;

import java.awt.Component;
import java.awt.Desktop;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.function.BiConsumer;

import javax.swing.JOptionPane;

/**
 * Diese Klasse stellt statische Hilfsroutinen zum Aufruf von URLs im Browser bereit.
 * @author Alexander Herzog
 */
public class JOpenURL {
	/**
	 * Standardimplementierung für {@link #openStringURL}
	 * @see #openStringURL
	 */
	public static final BiConsumer<Component,String> defaultOpenStringURL;

	/**
	 * Standardimplementierung für {@link #openURL}
	 * @see #openURL
	 */
	public static final BiConsumer<Component,URL> defaultOpenURL;

	/**
	 * Standardimplementierung für {@link #openURI}
	 * @see #openURI
	 */
	public static final BiConsumer<Component,URI> defaultOpenURI;

	/**
	 * Implementierungsfunktion, die von {@link #open(Component, String)}
	 * aufgerufen wird.
	 * @see #open(Component, String)
	 */
	public static BiConsumer<Component,String> openStringURL;

	/**
	 * Implementierungsfunktion, die von {@link #open(Component, URL)}
	 * aufgerufen wird.
	 * @see #open(Component, URL)
	 */
	public static BiConsumer<Component,URL> openURL;

	/**
	 * Implementierungsfunktion, die von {@link #open(Component, URI)}
	 * aufgerufen wird.
	 * @see #open(Component, URI)
	 */
	public static BiConsumer<Component,URI> openURI;

	static {
		defaultOpenStringURL=(parent,url)->{
			try {
				openURI.accept(parent,new URL(url).toURI());
			} catch (MalformedURLException|URISyntaxException e) {}
		};
		defaultOpenURL=(parent,url)->{
			try {
				openURI.accept(parent,url.toURI());
			} catch (URISyntaxException e) {}
		};
		defaultOpenURI=(parent,uri)->{
			if (JOptionPane.showConfirmDialog(parent,String.format(JDistributionPanel.GraphicsOpenURLWarning,uri.toString()),JDistributionPanel.GraphicsOpenURLWarningTitle,JOptionPane.YES_NO_OPTION)!=JOptionPane.YES_OPTION) return;
			try {
				Desktop.getDesktop().browse(uri);
			} catch (IOException e) {}
		};

		openStringURL=defaultOpenStringURL;
		openURL=defaultOpenURL;
		openURI=defaultOpenURI;
	}

	/**
	 * Konstruktor der Klasse<br>
	 * Diese Klasse kann nicht instanziert werden.
	 * Sie stellt nur statische Hilfsroutinen zum Aufruf von URLs im Browser bereit.
	 */
	private JOpenURL() {}

	/**
	 * Ruft eine URL im Browser auf.
	 * @param parent	Übergeordnetes Element (zur Ausrichtung von Dialogen)
	 * @param url	Aufzurufende URL
	 */
	public static void open(final Component parent, final String url) {
		if (url==null || openStringURL==null) return;
		if (!Desktop.isDesktopSupported() || !Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) return;
		openStringURL.accept(parent,url);
	}

	/**
	 * Ruft eine URL im Browser auf.
	 * @param parent	Übergeordnetes Element (zur Ausrichtung von Dialogen)
	 * @param url	Aufzurufende URL
	 */
	public static void open(final Component parent, final URL url) {
		if (url==null || openURI==null) return;
		if (!Desktop.isDesktopSupported() || !Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) return;
		openURL.accept(parent,url);
	}

	/**
	 * Ruft eine URL im Browser auf.
	 * @param parent	Übergeordnetes Element (zur Ausrichtung von Dialogen)
	 * @param uri	Aufzurufende URL
	 */
	public static void open(final Component parent, final URI uri) {
		if (uri==null || openURI==null) return;
		if (!Desktop.isDesktopSupported() || !Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) return;
		openURI.accept(parent,uri);
	}
}
