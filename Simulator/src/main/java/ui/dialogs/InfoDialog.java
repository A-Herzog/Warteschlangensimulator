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
package ui.dialogs;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Desktop;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.lang.management.ManagementFactory;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.FutureTask;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.jar.Manifest;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import language.Language;
import mathtools.NumberTools;
import mathtools.distribution.swing.JOpenURL;
import scripting.java.DynamicFactory;
import systemtools.GUITools;
import systemtools.MsgBox;
import tools.IconListCellRenderer;
import tools.SetupData;
import ui.MainFrame;
import ui.MainPanel;
import ui.images.Images;

/**
 * Versionsinfo-Dialog
 * @author Alexander Herzog
 * @version 1.0
 */
public class InfoDialog extends JDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = -4544783238672067726L;

	/**
	 * Auswahlfeld zum Ändern der Programmsprache
	 */
	private JComboBox<String> languages;

	/**
	 * Dieses Feld wird auf <code>true</code> gesetzt, wenn der Dialog nicht über "Ok"
	 * oder das Schließen-Feld geschlossen wurde, sondern der Nutzer angeklickt hat,
	 * dass die Versionsgeschichte aufgerufen werden soll. Der Aufrufer der Dialogs
	 * muss auf dieses Feld achten.
	 */
	public boolean showVersionHistory;

	/**
	 * Dieses Feld wird auf <code>true</code> gesetzt, wenn der Dialog nicht über "Ok"
	 * oder das Schließen-Feld geschlossen wurde, sondern der Nutzer angeklickt hat,
	 * dass die Lizenzinformationen aufgerufen werden soll. Der Aufrufer der Dialogs
	 * muss auf dieses Feld achten.
	 */
	public boolean showLicenses;

	/**
	 * Hintergrund-Bild-Loader
	 */
	private ExecutorService executor;

	/**
	 * Liefert eine Zusammenstellung der System-Informationen.
	 * @param showRAM	Auch Informationen zur Speicherbelegung ausgeben?
	 * @param alwaysShowBits	Immer Informationen zum 32/64-Bit-Modus der Java-Umgebung ausgeben?
	 * @return	Liste mit System-Informationen
	 */
	public static List<String> getSystemInfo(final boolean showRAM, final boolean alwaysShowBits) {
		final List<String> list=new ArrayList<>();

		/* Erstellungsdatum */
		final Date buildDate=getBuildDate();
		if (buildDate!=null) {
			DateFormat formatter=DateFormat.getDateInstance();
			list.add(Language.tr("InfoDialog.BuildDate")+": "+formatter.format(buildDate));
		}

		/* Installationsverzeichnis */
		list.add(Language.tr("InfoDialog.InstallFolder")+": "+SetupData.getProgramFolder());
		list.add(Language.tr("InfoDialog.ConfigFile")+": "+SetupData.getSetupFolder()+SetupData.SETUP_FILE_NAME);
		final String mode;
		switch (SetupData.getOperationMode()) {
		case PROGRAM_FOLDER_MODE: mode=Language.tr("InfoDialog.InstallMode.ProgramFolder"); break;
		case USER_FOLDER_MODE: mode=Language.tr("InfoDialog.InstallMode.UserFolder"); break;
		case PORTABLE_MODE: mode=Language.tr("InfoDialog.InstallMode.Portable"); break;
		default: mode=Language.tr("InfoDialog.InstallMode.Unknown"); break;
		}
		list.add(Language.tr("InfoDialog.InstallMode")+": "+mode);

		list.add("");

		/* Java-Version */
		list.add(Language.tr("InfoDialog.JavaVersion")+": "+System.getProperty("java.vendor")+" "+System.getProperty("java.version")+" ("+System.getProperty("java.vm.name")+", "+System.getProperty("os.arch")+")");
		list.add(Language.tr("InfoDialog.JavaPath")+": "+System.getProperty("java.home"));
		if (alwaysShowBits || !System.getProperty("os.arch").contains("64")) {
			list.add(Language.tr("InfoDialog.Is64Bit")+": "+(System.getProperty("os.arch").contains("64")?Language.tr("InfoDialog.Is64Bit.Yes"):Language.tr("InfoDialog.Is64Bit.No")));
		}

		/* Java-Kompiler verfügbar? */
		list.add(Language.tr("InfoDialog.JavaCompiler")+": "+(DynamicFactory.hasCompiler()?Language.tr("InfoDialog.JavaCompiler.Yes"):Language.tr("InfoDialog.JavaCompiler.No")));

		list.add("");

		/* Speicherverbrauch */
		if (showRAM) {
			final long l1=ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getUsed();
			final long l2=ManagementFactory.getMemoryMXBean().getNonHeapMemoryUsage().getUsed();
			list.add(Language.tr("InfoDialog.MemoryUsage")+": "+NumberTools.formatLong((l1+l2)/1024/1024)+" MB");
			list.add(Language.tr("InfoDialog.MemoryAvailable")+": "+NumberTools.formatLong(Runtime.getRuntime().maxMemory()/1024/1024)+" MB");
			final int maxThreadsByMemory=(int)Math.max(1,Runtime.getRuntime().maxMemory()/1024/1024/100);
			final int maxThreadByCPU=Runtime.getRuntime().availableProcessors();
			if (maxThreadsByMemory<maxThreadByCPU) list.add(String.format(Language.tr("InfoDialog.ThreadMemoryLimitation"),maxThreadsByMemory,maxThreadByCPU));
		}

		return list;
	}

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Fenster
	 * @param version	Anzuzeigende Versionsnummer
	 * @param plainMode	Wird <code>true</code> übergeben, so werden die Schaltflächen für Versionsgeschichte und der Sprachschalter <b>nicht</b> angezeigt.
	 */
	public InfoDialog(final Window owner, final String version, final boolean plainMode) {
		super(owner,Language.tr("InfoDialog.Title"),Dialog.ModalityType.DOCUMENT_MODAL);
		setLayout(new BorderLayout());

		JPanel mainarea,p,p2,p3;
		JLabel image;

		add(mainarea=new JPanel(),BorderLayout.CENTER);
		mainarea.setLayout(new BoxLayout(mainarea,BoxLayout.Y_AXIS));

		/* Bild anzeigen */
		mainarea.add(p=new JPanel(new FlowLayout(FlowLayout.CENTER))); p.add(image=new JLabel());
		image.setBorder(BorderFactory.createLineBorder(Color.BLACK));

		final URL imageURL=MainFrame.class.getResource("res/Warteschlangennetz.png");
		executor=new ThreadPoolExecutor(0,1,1,TimeUnit.SECONDS,new LinkedBlockingQueue<>(),(ThreadFactory)r->new Thread(r,"Image loader"));
		executor.execute(new FutureTask<>(()->{
			if (imageURL!=null) image.setIcon(new ImageIcon(imageURL)); else image.setVisible(false);
			return null;
		}));

		/* Text anzeigen */
		mainarea.add(p=new JPanel(new FlowLayout(FlowLayout.LEFT))); p.add(p2=new JPanel());
		p2.setLayout(new BoxLayout(p2,BoxLayout.Y_AXIS));

		/* Programm, Autor und Links */
		final String htmlName=MainFrame.PROGRAM_NAME+" "+version;
		final String htmlAuthor="&copy; "+MainPanel.AUTHOR+" (<a href=\"mailto:"+MainPanel.AUTHOR_EMAIL+"\" style=\"text-decoration: none;\">"+MainPanel.AUTHOR_EMAIL+"</a>)";
		String htmlLinks=null;
		if (!plainMode) {
			htmlLinks="<a href=\"special:homepage\" style=\"text-decoration: none;\">"+Language.tr("InfoDialog.Homepage")+"</a>"+
					"&nbsp;&nbsp;"+
					"<a href=\"special:changelog\" style=\"text-decoration: none;\">"+Language.tr("InfoDialog.ShowVersionHistory")+"</a>"+
					"&nbsp;&nbsp;"+
					"<a href=\"special:license\" style=\"text-decoration: none;\">"+Language.tr("InfoDialog.ShowLicenses")+"</a>";
		}

		/* System-Informationen */
		final List<String> text=getSystemInfo(true,false);

		/* Ausgabe */
		final StringBuilder infoText=new StringBuilder();
		infoText.append("<html><body style=\"margin: 0px; padding: 0px; font-family: sans; background-color: transparent; font-size: "+Math.round(100*GUITools.getScaleFactor())+"%;\">");
		infoText.append("<p style=\"margin-top: 0px; font-weight: bold; font-size: 120%;\">"+htmlName+"</p>");
		if (!MainPanel.RELEASE_BUILD) infoText.append("<p style=\"margin-top: 0px; font-weight: bold; font-size: 110%; color: red;\">"+Language.tr("Editor.SurfaceTooltip.NonReleaseBuild")+"</p>");
		if (imageURL==null) infoText.append("<p style=\"margin-top: 0px; font-weight: bold; font-size: 110%; color: orange;\">"+Language.tr("Editor.SurfaceTooltip.CustomBuild")+"</p>");
		infoText.append("<p style=\"margin-top: 5px; font-size: 110%;\">"+htmlAuthor+"</p>");
		if (htmlLinks!=null) infoText.append("<p style=\"margin-top: 5px; font-size: 110%;\">"+htmlLinks+"</p>");
		infoText.append("<p style=\"margin-top: 5px; font-size: 85%;\">"+String.join("<br>",text)+"</p>");
		infoText.append("</body></html>");
		final String htmlInfoText=infoText.toString();

		p2.add(p3=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		final JTextPane label=new JTextPane();

		label.setContentType("text/html");
		label.setText(htmlInfoText);
		label.setEditable(false);
		label.setOpaque(false);
		label.setBackground(new Color(0,0,0,0));
		p3.add(label);
		label.addHyperlinkListener(new LinkListener());
		label.getCaret().setVisible(false);
		label.setCaretColor(label.getBackground());
		label.setHighlighter(null);

		mainarea.add(Box.createVerticalGlue());

		add(p=new JPanel(new FlowLayout(FlowLayout.LEFT)),BorderLayout.SOUTH);
		/* Ok-Button */
		final JButton okButton;
		p.add(okButton=new JButton(Language.tr("Dialog.Button.Ok")));
		okButton.addActionListener(e-> {
			if (languages!=null) {
				String lang=(languages.getSelectedIndex()==1)?"de":"en";
				SetupData setup=SetupData.getSetup();
				if (!setup.language.equals(lang)) {
					setup.language=lang;
					setup.saveSetupWithWarning(InfoDialog.this);
				}
			}
			setVisible(false);
			dispose();
		});
		okButton.setIcon(Images.MSGBOX_OK.getIcon());
		getRootPane().setDefaultButton(okButton);

		if (!plainMode) {
			/* Sprachschalter */
			p.add(Box.createHorizontalStrut(5));
			p.add(new JLabel(Language.tr("SettingsDialog.Languages")+":"));
			p.add(languages=new JComboBox<>(new String[]{Language.tr("SettingsDialog.Languages.English"),Language.tr("SettingsDialog.Languages.German")}));
			languages.setRenderer(new IconListCellRenderer(new Images[]{Images.LANGUAGE_EN,Images.LANGUAGE_DE}));
			languages.setToolTipText(Language.tr("SettingsDialog.Languages.Info"));
			SetupData setup=SetupData.getSetup();
			if (setup.language==null || setup.language.isEmpty() || setup.language.equalsIgnoreCase("de")) languages.setSelectedIndex(1); else languages.setSelectedIndex(0);
		} else {
			languages=null;
		}

		addWindowListener(new WindowAdapter() {@Override public void windowClosing(WindowEvent event) {setVisible(false); dispose();}});
		setResizable(false);
		setMinimumSize(new Dimension(550,50));
		pack();
		setLocationRelativeTo(owner);
		SwingUtilities.invokeLater(()->{pack(); setLocationRelativeTo(owner);});
	}

	/**
	 * Konstruktor der Klasse<br>
	 * Es werden die Schaltflächen für Versionsgeschichte und der Sprachschalter angezeigt.
	 * @param owner	Übergeordnetes Fenster
	 * @param version	Anzuzeigende Versionsnummer
	 */
	public InfoDialog(final Window owner, final String version) {
		this(owner,version,false);
	}

	@Override
	protected JRootPane createRootPane() {
		final JRootPane rootPane=new JRootPane();
		final KeyStroke stroke=KeyStroke.getKeyStroke("ESCAPE");
		final InputMap inputMap=rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
		inputMap.put(stroke,"ESCAPE");
		rootPane.getActionMap().put("ESCAPE",new CloseListener());
		return rootPane;
	}

	/**
	 * Liefert - wenn vorhanden - das Build-Datum aus der Manifest-Datei (aus der jar-Datei).
	 * @return	Erstellungsdatum, wenn der Simulator aus einer jar heraus gestartet wurde, sonst <code>null</code>
	 */
	private static Date getBuildDate() {
		final Enumeration<URL> resources;
		try {
			resources=InfoDialog.class.getClassLoader().getResources("META-INF/MANIFEST.MF");
		} catch (IOException e1) {
			return null;
		}
		while (resources.hasMoreElements()) {
			try {
				final URL url=resources.nextElement();
				if (!url.toString().endsWith("/Simulator.jar!/META-INF/MANIFEST.MF")) continue;
				try (InputStream stream=url.openStream()) {
					final Manifest manifest=new Manifest(stream);
					final String dateTimeString=manifest.getMainAttributes().getValue("Build-Time");
					if (dateTimeString==null) return null;

					final SimpleDateFormat dateTime=new SimpleDateFormat("yyyy-MM-dd HH:mm");
					return dateTime.parse(dateTimeString);
				}
			} catch (IOException | ParseException e) {
				return null;
			}
		}

		return null;
	}

	/**
	 * Wird ausgelöst, wenn der Dialog geschlossen werden soll.
	 * @see InfoDialog#createRootPane()
	 */
	private class CloseListener extends AbstractAction {
		/**
		 * Serialisierungs-ID der Klasse
		 * @see Serializable
		 */
		private static final long serialVersionUID = -485008309903554823L;

		/**
		 * Konstruktor der Klasse
		 */
		public CloseListener() {
			/*
			 * Wird nur benötigt, um einen JavaDoc-Kommentar für diesen (impliziten) Konstruktor
			 * setzen zu können, damit der JavaDoc-Compiler keine Warnung mehr ausgibt.
			 */
		}

		@Override
		public void actionPerformed(ActionEvent actionEvent) {
			setVisible(false);
			dispose();
		}
	}

	/**
	 * Wird ausgelöst, wenn in dem Infotext auf die E-Mail-Adresse geklickt wird.
	 */
	private class LinkListener implements HyperlinkListener {
		/**
		 * Konstruktor der Klasse
		 */
		public LinkListener() {
			/*
			 * Wird nur benötigt, um einen JavaDoc-Kommentar für diesen (impliziten) Konstruktor
			 * setzen zu können, damit der JavaDoc-Compiler keine Warnung mehr ausgibt.
			 */
		}

		@Override
		public void hyperlinkUpdate(HyperlinkEvent e) {
			if (e.getEventType()!=HyperlinkEvent.EventType.ACTIVATED) return;
			final String link=e.getDescription();

			if (link.toLowerCase().startsWith("mailto:")) {
				final URL url=e.getURL();
				try {
					Desktop.getDesktop().mail(url.toURI());
				} catch (IOException | URISyntaxException e1) {
					MsgBox.error(InfoDialog.this,Language.tr("Window.Info.NoEMailProgram.Title"),String.format(Language.tr("Window.Info.NoEMailProgram.Info"),url.toString()));
				}
			}

			if (link.toLowerCase().startsWith("special:")) {
				if (link.equalsIgnoreCase("special:changelog")) {showVersionHistory=true; setVisible(false); dispose();}
				if (link.equalsIgnoreCase("special:license")) {showLicenses=true; setVisible(false); dispose();}
				if (link.equalsIgnoreCase("special:homepage")) {JOpenURL.open(InfoDialog.this,"https://"+MainPanel.WEB_URL);}
			}
		}
	}
}
