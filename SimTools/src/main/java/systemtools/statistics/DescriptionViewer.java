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
package systemtools.statistics;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.IOException;
import java.io.Serializable;
import java.net.URL;
import java.util.function.Consumer;

import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextPane;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.HyperlinkEvent;
import javax.swing.text.Document;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.StyleSheet;

import mathtools.distribution.swing.JOpenURL;
import systemtools.images.SimToolsImages;

/**
 * Dieses Panel enthält eine ein- und ausklappbare
 * Beschreibungsseite für ein {@link StatisticViewerText}-Element.
 * @author Alexander Herzog
 * @see StatisticViewerText
 * @version 1.2
 */
public class DescriptionViewer extends JPanel {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = 8594822323998433424L;

	/** Callback an das Links (zur dialogbasierten Hilfe) übermittelt werden (kann <code>null</code> sein) */
	private final Consumer<String> linkCallback;

	/** URL der anzuzeigenden Hilfeseite */
	private final URL pageURL;

	/** Optionale zusätzliche Styles (kann <code>null</code> oder leer sein) */
	private final String customStyles;

	/** Toolbar auf dem sich die Schaltfläche zum Ein- und Ausblenden des Info-Panels befindet. */
	private final JToolBar toolbar;

	/** Schaltfläche zum Ein- und Ausblenden des Info-Panels. */
	private final JButton button;

	/**
	 * Text-Panel zur Anzeige des Info-Texts
	 */
	private final JTextPane textPane;

	/**
	 * Wird die Hilfe momentan angezeigt?
	 * @see #toggleDescription()
	 */
	private boolean descriptionVisible;

	/**
	 * Konstruktor der Klasse
	 * @param pageURL	Anzuzeigende Hilfeseite
	 * @param linkCallback	Callback an das Links (zur dialogbasierten Hilfe) übermittelt werden (kann <code>null</code> sein)
	 * @param customStyles	Optionale zusätzliche Styles (kann <code>null</code> oder leer sein)
	 */
	public DescriptionViewer(final URL pageURL, final Consumer<String> linkCallback, final String customStyles) {
		this.pageURL=pageURL;
		this.linkCallback=linkCallback;
		this.customStyles=customStyles;
		setLayout(new BorderLayout());

		toolbar=new JToolBar(SwingConstants.HORIZONTAL);
		toolbar.setFloatable(false);
		toolbar.add(button=new JButton());
		button.setText(StatisticsBasePanel.descriptionShow); /* Damit die gewünschte Toolbar-Höhe initial richtig berechnet werden kann. */
		button.setIcon(SimToolsImages.ARROW_UP.getIcon()); /* Damit die gewünschte Toolbar-Höhe initial richtig berechnet werden kann. */
		button.addActionListener(e->toggleDescription());
		add(toolbar,BorderLayout.NORTH);

		textPane=new JTextPane();
		textPane.setEditable(false);
		textPane.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES,Boolean.TRUE);

		textPane.addHyperlinkListener(e->linkClicked(e));
		add(new JScrollPane(textPane),BorderLayout.CENTER);

		SwingUtilities.invokeLater(new InitRunnable());
	}

	/**
	 * Führt Verarbeitungen durch, die erst nach dem Abschluss des Konstruktors möglich sind.
	 */
	private class InitRunnable implements Runnable {
		/**
		 * Konstruktor der Klasse
		 */
		public InitRunnable() {
			/*
			 * Wird nur benötigt, um einen JavaDoc-Kommentar für diesen (impliziten) Konstruktor
			 * setzen zu können, damit der JavaDoc-Compiler keine Warnung mehr ausgibt.
			 */
		}

		/**
		 * Wird beim Laden des Dokuments aufgerufen und erlaubt das
		 * Hinzufügen von nutzerdefinierten Style-Regeln.
		 * @param styleSheet	Stylesheet-Objekt zu dem die Regeln hinzugefügt werden
		 */
		protected void addCustomStyles(final StyleSheet styleSheet) {
			if (customStyles!=null && !customStyles.isBlank())	styleSheet.addRule(customStyles);
		}

		@Override
		public void run() {
			try {
				textPane.setPage(pageURL); /* Muss in SwingUtilities.invokeLater aufgerufen werden, sonst kann's bei der Report-Generierung Blockierungen geben. */
				final Document document=textPane.getDocument();
				if (document instanceof HTMLDocument) {
					final HTMLDocument htmlDocument=(HTMLDocument)document;
					addCustomStyles(htmlDocument.getStyleSheet());
				}
			} catch (IOException e1) {
				textPane.setText("Page "+pageURL.toString()+" not found.");
			}

			if (toolbar.getHeight()<20) {
				SwingUtilities.invokeLater(new InitRunnable());
				return;
			}
			descriptionVisible=true;
			toggleDescription();
		}
	}

	/**
	 * Benachrichtigt das Objekt, dass sich der Trenner im übergeordneten
	 * {@link JSplitPane} verschoben wurde.
	 */
	public void splitterMovedInfo() {
		if (toolbar.getHeight()==0) return;
		if (!(getParent() instanceof JSplitPane)) return;

		final JSplitPane split=(JSplitPane)getParent();
		final int minSize=toolbar.getHeight()+split.getDividerSize();

		if (split.getDividerLocation()>split.getSize().height-minSize) {
			split.setDividerLocation(split.getSize().height-minSize);
		}

		descriptionVisible=(split.getDividerLocation()<split.getSize().height-minSize-1);
		updateButton();
	}

	/**
	 * Aktualisiert die Schaltfläche je nach dem, ob das Beschreibungs-Panel
	 * sichtbar ist oder nicht.
	 * @see #splitterMovedInfo()
	 * @see #toggleDescription()
	 */
	private void updateButton() {
		if (descriptionVisible) {
			button.setText(StatisticsBasePanel.descriptionHide);
			button.setToolTipText(StatisticsBasePanel.descriptionHideHint);
			button.setIcon(SimToolsImages.ARROW_DOWN.getIcon());
		} else {
			button.setText(StatisticsBasePanel.descriptionShow);
			button.setToolTipText(StatisticsBasePanel.descriptionShowHint);
			button.setIcon(SimToolsImages.ARROW_UP.getIcon());
		}
	}

	/**
	 * Blendet das Beschreibungs-Panel ein oder aus.
	 * @see #button
	 */
	private void toggleDescription() {
		descriptionVisible=!descriptionVisible;
		updateButton();

		if (getParent() instanceof JSplitPane) {
			final JSplitPane split=(JSplitPane)getParent();
			if (descriptionVisible) {
				split.setDividerLocation(2*split.getSize().height/3);
			} else {
				final int minSize=toolbar.getHeight()+split.getDividerSize();
				split.setDividerLocation(split.getSize().height-minSize);
			}
		}
	}

	/**
	 * Reagiert auf Bewegungen der Maus über Links und Klicks auf Links.
	 * @param e	Auslösendes Ereignis
	 * @see #linkCallback
	 */
	private void linkClicked(final HyperlinkEvent e) {
		if (e.getEventType()==HyperlinkEvent.EventType.ENTERED) {
			setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
			return;
		}

		if (e.getEventType()==HyperlinkEvent.EventType.EXITED) {
			setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
			return;
		}

		if (e.getEventType()==HyperlinkEvent.EventType.ACTIVATED) {
			final String description=e.getDescription();
			if (description!=null && !description.isBlank()) {
				final String linkLower=description.toLowerCase();

				if (linkLower.startsWith("http://") || linkLower.startsWith("https://")) {
					JOpenURL.open(this,description);
					return;
				}

				if (linkCallback!=null) linkCallback.accept(description);
			}
		}
	}

	@Override
	public Dimension getMaximumSize() {
		final Dimension size=super.getMaximumSize();
		if (descriptionVisible && getParent()!=null) {
			size.height=getParent().getHeight()/2;
		} else {
			size.height=toolbar.getPreferredSize().height;
		}
		return size;
	}

	@Override
	public Dimension getMinimumSize() {
		final Dimension size=super.getMinimumSize();
		size.height=toolbar.getPreferredSize().height;
		return size;
	}

	@Override
	public Dimension getPreferredSize() {
		final Dimension size=super.getPreferredSize();
		if (descriptionVisible && getParent()!=null) {
			size.height=getParent().getHeight()/3;
		} else {
			size.height=toolbar.getPreferredSize().height;
		}
		return size;
	}

	@Override
	public Dimension getSize() {
		final Dimension size=super.getSize();
		if (!descriptionVisible) {
			size.height=toolbar.getPreferredSize().height;
		}
		return size;
	}

	/**
	 * Liefert ein {@link JSplitPane}, welches oben den eigentlichen Inhalt
	 * und unten diesen {@link DescriptionViewer} enthält. Die Verknüpfung
	 * zwischen Splitter und {@link DescriptionViewer} wird dabei auch bereits
	 * hergestellt.
	 * @param mainComponent	Komponenten für den oberen Bereich des Fensters
	 * @return	Splitter, der die Komponente und die Beschreibung enthält
	 */
	public JSplitPane getSplitPanel(final Component mainComponent) {
		final JSplitPane splitPane=new JSplitPane(JSplitPane.VERTICAL_SPLIT,mainComponent,this);
		splitPane.setContinuousLayout(true);
		splitPane.setResizeWeight(1);
		splitPane.addPropertyChangeListener(JSplitPane.DIVIDER_LOCATION_PROPERTY,e->splitterMovedInfo());
		splitPane.addComponentListener(new ComponentAdapter() {
			@Override public void componentResized(ComponentEvent e) {splitterMovedInfo();}
		});
		return splitPane;
	}
}