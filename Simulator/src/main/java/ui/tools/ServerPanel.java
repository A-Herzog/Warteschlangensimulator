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
package ui.tools;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.FlowLayout;
import java.awt.SystemColor;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;

import language.Language;
import net.calc.SimulationServerGUIConnect;
import net.dde.SimulationDDEServer;
import net.web.SimulatorWebServer;
import net.webcalc.CalcWebServer;
import systemtools.MsgBox;
import tools.SetupData;
import ui.MainPanel;
import ui.ReloadManager;
import ui.help.Help;
import ui.images.Images;

/**
 * Zeigt den aktuellen Status und die letzten Ausgaben des GUI-Hintergrund-Netzwerkservers an.
 * Außerdem kann der GUI-Hintergrund-Netzwerkserver über dieses Panel gestartet und beendet werden.
 * @author Alexander Herzog
 * @see SimulationServerGUIConnect
 */
public final class ServerPanel extends SpecialPanel {
	private static final long serialVersionUID = 6115906891815733405L;

	private final SimulationServerGUIConnect serverCalc;
	private final CalcWebServer serverCalcWeb;
	private final SimulatorWebServer serverWeb;
	private final SimulationDDEServer serverDDE;

	private final JButton startStopCalcButton;
	private final JButton startStopCalcWebButton;
	private final JButton startStopWebButton;
	private final JButton startStopDDEButton;
	private final JButton helpButton;

	private final JSpinner calcPortEditSpinner;
	private final SpinnerModel calcPortEdit;
	private final JTextField calcPasswordEdit;
	private final JCheckBox calcLimitThreadsCheckBox;
	private final JCheckBox calcAutoStartCheckBox;

	private final JSpinner calcWebPortEditSpinner;
	private final SpinnerModel calcWebPortEdit;
	private final JCheckBox calcWebAutoStartCheckBox;
	private final JLabel calcWebOpenBrowserButton;

	private final JSpinner webPortEditSpinner;
	private final SpinnerModel webPortEdit;
	private final JCheckBox webAutoStartCheckBox;
	private final JLabel webOpenBrowserButton;

	private final JCheckBox ddeAutoStartCheckBox;

	private final JTextArea calcOutput;

	private final JLabel calcStatusBar;
	private final JLabel calcWebStatusBar;
	private final JLabel webStatusBar;
	private final JLabel ddeStatusBar;

	private final NotifyRunner notifyRunner;

	/**
	 * Konstruktor der Klasse
	 * @param doneNotify	Runnable, das aufgerufen wird, wenn das Panel geschlossen werden soll
	 * @param mainPanel	Hauptpanel des Simulators (für den Zugriff durch den Animations-Web-Server)
	 */
	public ServerPanel(final Runnable doneNotify, final MainPanel mainPanel) {
		super(doneNotify);
		serverCalc=SimulationServerGUIConnect.getInstance();
		serverCalcWeb=CalcWebServer.getInstance();
		serverWeb=SimulatorWebServer.getInstance(mainPanel);
		serverDDE=SimulationDDEServer.getInstance(mainPanel);
		notifyRunner=new NotifyRunner();
		ReloadManager.addBroadcastReceiver(NotifyRunner.id,notifyRunner);

		JPanel line;
		JLabel label;
		JSpinner.NumberEditor editor;

		/* Symbolleiste */

		startStopCalcButton=addUserButton(Language.tr("SimulationServer.Toolbar.Start"),Language.tr("SimulationServer.Toolbar.Start.Hint"),null);
		startStopCalcWebButton=addUserButton(Language.tr("SimulationServer.Toolbar.CalcWebStart"),Language.tr("SimulationServer.Toolbar.CalcWebStart.Hint"),null);
		startStopWebButton=addUserButton(Language.tr("SimulationServer.Toolbar.WebStart"),Language.tr("SimulationServer.Toolbar.WebStart.Hint"),null);
		startStopDDEButton=addUserButton(Language.tr("SimulationServer.Toolbar.DDEStart"),Language.tr("SimulationServer.Toolbar.DDEStart.Hint"),null);
		addCloseButton();
		helpButton=addUserButton(Language.tr("Main.Toolbar.Help"),Language.tr("Main.Toolbar.Help.Hint"),Images.HELP.getURL());

		/* Mainpanel */

		final JPanel content=new JPanel(new BorderLayout());
		add(content,BorderLayout.CENTER);

		/* Eingabezeile für Einstellungen */

		final JPanel setup=new JPanel();
		content.add(setup,BorderLayout.NORTH);
		setup.setLayout(new BoxLayout(setup,BoxLayout.PAGE_AXIS));

		/* Simulationsserver */

		setup.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(new JLabel("<html><body><b>"+Language.tr("SimulationServer.Setup.CalcServer")+":</b></body></html>"));
		line.add(Box.createHorizontalStrut(5));

		line.add(label=new JLabel(Language.tr("SimulationServer.Setup.Port")+":"));
		calcPortEditSpinner=new JSpinner(calcPortEdit=new SpinnerNumberModel(1,1,65535,1));
		editor=new JSpinner.NumberEditor(calcPortEditSpinner);
		editor.getFormat().setGroupingUsed(false);
		calcPortEdit.setValue(serverCalc.getLastPort());
		calcPortEditSpinner.setEditor(editor);
		line.add(calcPortEditSpinner);
		label.setLabelFor(calcPortEditSpinner);

		line.add(label=new JLabel(Language.tr("SimulationServer.Setup.Password")+":"));
		line.add(calcPasswordEdit=new JTextField(serverCalc.getLastPasswort(),20));
		label.setLabelFor(calcPasswordEdit);

		line.add(calcLimitThreadsCheckBox=new JCheckBox(Language.tr("SimulationServer.Setup.LimitThreadCount"),serverCalc.getLastLimitThreadCount()));
		calcLimitThreadsCheckBox.setToolTipText(Language.tr("SimulationServer.Setup.LimitThreadCount.Hint"));

		line.add(calcAutoStartCheckBox=new JCheckBox(Language.tr("SimulationServer.Setup.CalcAutoStart"),SetupData.getSetup().simulationServerAutoStart));
		calcAutoStartCheckBox.setToolTipText(Language.tr("SimulationServer.Setup.CalcAutoStart.Hint"));

		/* Webserver */

		setup.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(new JLabel("<html><body><b>"+Language.tr("SimulationServer.Setup.CalcWebServer")+":</b></body></html>"));
		line.add(Box.createHorizontalStrut(5));

		line.add(label=new JLabel(Language.tr("SimulationServer.Setup.Port")+":"));
		calcWebPortEditSpinner=new JSpinner(calcWebPortEdit=new SpinnerNumberModel(1,1,65535,1));
		editor=new JSpinner.NumberEditor(calcWebPortEditSpinner);
		editor.getFormat().setGroupingUsed(false);
		calcWebPortEdit.setValue(serverWeb.getLastPort());
		calcWebPortEditSpinner.setEditor(editor);
		line.add(calcWebPortEditSpinner);
		label.setLabelFor(calcWebPortEditSpinner);

		line.add(calcWebAutoStartCheckBox=new JCheckBox(Language.tr("SimulationServer.Setup.CalcWebAutoStart"),SetupData.getSetup().calcWebServerAutoStart));
		calcWebAutoStartCheckBox.setToolTipText(Language.tr("SimulationServer.Setup.CalcWebAutoStart.Hint"));

		line.add(calcWebOpenBrowserButton=new JLabel("<html><body><span style=\"color: blue; text-decoration: underline;\">"+Language.tr("SimulationServer.Setup.OpenBrowser")+"</span></body></html>"));
		calcWebOpenBrowserButton.setToolTipText(Language.tr("SimulationServer.Setup.OpenBrowser.Hint"));
		calcWebOpenBrowserButton.setBorder(BorderFactory.createEmptyBorder(0,10,0,0));
		calcWebOpenBrowserButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		calcWebOpenBrowserButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (SwingUtilities.isLeftMouseButton(e)) commandOpenBrowser(serverCalcWeb.getRunningPort());
			}
		});

		/* Fernsteuerungs-Server */

		setup.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(new JLabel("<html><body><b>"+Language.tr("SimulationServer.Setup.WebServer")+":</b></body></html>"));
		line.add(Box.createHorizontalStrut(5));

		line.add(label=new JLabel(Language.tr("SimulationServer.Setup.Port")+":"));
		webPortEditSpinner=new JSpinner(webPortEdit=new SpinnerNumberModel(1,1,65535,1));
		editor=new JSpinner.NumberEditor(webPortEditSpinner);
		editor.getFormat().setGroupingUsed(false);
		webPortEdit.setValue(serverWeb.getLastPort());
		webPortEditSpinner.setEditor(editor);
		line.add(webPortEditSpinner);
		label.setLabelFor(webPortEditSpinner);

		line.add(webAutoStartCheckBox=new JCheckBox(Language.tr("SimulationServer.Setup.WebAutoStart"),SetupData.getSetup().webServerAutoStart));
		webAutoStartCheckBox.setToolTipText(Language.tr("SimulationServer.Setup.WebAutoStart.Hint"));

		line.add(webOpenBrowserButton=new JLabel("<html><body><span style=\"color: blue; text-decoration: underline;\">"+Language.tr("SimulationServer.Setup.OpenBrowser")+"</span></body></html>"));
		webOpenBrowserButton.setToolTipText(Language.tr("SimulationServer.Setup.OpenBrowser.Hint"));
		webOpenBrowserButton.setBorder(BorderFactory.createEmptyBorder(0,10,0,0));
		webOpenBrowserButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		webOpenBrowserButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (SwingUtilities.isLeftMouseButton(e)) commandOpenBrowser(serverWeb.getLastPort());
			}
		});

		/* DDE-Server */

		setup.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(new JLabel("<html><body><b>"+Language.tr("SimulationServer.Setup.DDEServer")+":</b></body></html>"));
		line.add(Box.createHorizontalStrut(5));

		line.add(ddeAutoStartCheckBox=new JCheckBox(Language.tr("SimulationServer.Setup.DDEAutoStart"),SetupData.getSetup().ddeServerAutoStart));
		ddeAutoStartCheckBox.setToolTipText(Language.tr("SimulationServer.Setup.DDEAutoStart.Hint"));

		/* Infotext über Ausgabebereich */

		setup.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(new JLabel("<html><body><b>"+Language.tr("SimulationServer.Setup.CalcServerOutput")+":</b></body></html>"));

		/* Ausgabebereich */

		content.add(new JScrollPane(calcOutput=new JTextArea()),BorderLayout.CENTER);
		calcOutput.setEditable(false);
		serverCalc.addOutputListener(list->{
			final String text=String.join("\n",list.toArray(new String[0]));
			calcOutput.setText(text);
			calcOutput.setCaretPosition(text.length());
		});

		/* Statuszeile */

		final JPanel statusPanel=new JPanel(new FlowLayout(FlowLayout.LEFT));
		content.add(statusPanel,BorderLayout.SOUTH);
		statusPanel.add(calcStatusBar=new JLabel(""));
		calcStatusBar.setBorder(BorderFactory.createEmptyBorder(0,5,0,10));
		calcStatusBar.setIcon(Images.SERVER_CALC.getIcon());
		statusPanel.add(calcWebStatusBar=new JLabel(""));
		calcWebStatusBar.setBorder(BorderFactory.createEmptyBorder(0,5,0,10));
		calcWebStatusBar.setIcon(Images.SERVER_CALC_WEB.getIcon());
		statusPanel.add(webStatusBar=new JLabel(""));
		webStatusBar.setBorder(BorderFactory.createEmptyBorder(0,5,0,10));
		webStatusBar.setIcon(Images.SERVER_WEB.getIcon());
		statusPanel.add(ddeStatusBar=new JLabel(""));
		ddeStatusBar.setBorder(BorderFactory.createEmptyBorder(0,5,0,0));
		ddeStatusBar.setIcon(Images.SERVER_DDE.getIcon());

		/* F1-Hotkey */

		getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("F1"),"actionHelp");
		getActionMap().put("actionHelp",new AbstractAction("actionHelp") {
			private static final long serialVersionUID = 1738622101739292954L;
			@Override public void actionPerformed(ActionEvent event) {commandHelp();}
		});

		/* Start der Verarbeitung */

		setupButtons();
	}

	private String getServerStatus(final boolean running, final String name) {
		final StringBuilder sb=new StringBuilder();
		sb.append("<html><body>");
		sb.append(name);
		sb.append(":&nbsp;");
		if (running) {
			sb.append("<span style=\"color: green;\">");
			sb.append(Language.tr("SimulationServer.Status.On"));
			sb.append("</span>");
		} else {
			sb.append("<span style=\"color: red;\">");
			sb.append(Language.tr("SimulationServer.Status.Off"));
			sb.append("</span>");
		}
		sb.append("</body></html>");
		return sb.toString();
	}

	private void setupButtons() {
		setupButtons(true);
	}

	private void setupButtons(final boolean triggerNotifiy) {
		URL imgURL;

		/* Simulationssserver */

		if (serverCalc.isServerRunning()) {
			startStopCalcButton.setText(Language.tr("SimulationServer.Toolbar.Stop"));
			startStopCalcButton.setToolTipText(Language.tr("SimulationServer.Toolbar.Stop.Hint"));
			imgURL=Images.SERVER_CALC_STOP.getURL();
		} else {
			startStopCalcButton.setText(Language.tr("SimulationServer.Toolbar.Start"));
			startStopCalcButton.setToolTipText(Language.tr("SimulationServer.Toolbar.Start.Hint"));
			imgURL=Images.SERVER_CALC_START.getURL();
		}
		if (imgURL!=null) startStopCalcButton.setIcon(new ImageIcon(imgURL));

		calcPortEditSpinner.setEnabled(!serverCalc.isServerRunning());
		calcPasswordEdit.setEnabled(!serverCalc.isServerRunning());
		calcLimitThreadsCheckBox.setEnabled(!serverCalc.isServerRunning());

		/* Webserver */

		if (serverCalcWeb.isRunning()) {
			startStopCalcWebButton.setText(Language.tr("SimulationServer.Toolbar.CalcWebStop"));
			startStopCalcWebButton.setToolTipText(Language.tr("SimulationServer.Toolbar.CalcWebStop.Hint"));
			imgURL=Images.SERVER_CALC_WEB_STOP.getURL();
		} else {
			startStopCalcWebButton.setText(Language.tr("SimulationServer.Toolbar.CalcWebStart"));
			startStopCalcWebButton.setToolTipText(Language.tr("SimulationServer.Toolbar.CalcWebStart.Hint"));
			imgURL=Images.SERVER_CALC_WEB_START.getURL();
		}
		if (imgURL!=null) startStopCalcWebButton.setIcon(new ImageIcon(imgURL));

		calcWebPortEditSpinner.setEnabled(!serverCalcWeb.isRunning());

		calcWebOpenBrowserButton.setVisible(serverCalcWeb.isRunning());

		/* Fernsteuerungs-Server */

		if (serverWeb.isRunning()) {
			startStopWebButton.setText(Language.tr("SimulationServer.Toolbar.WebStop"));
			startStopWebButton.setToolTipText(Language.tr("SimulationServer.Toolbar.WebStop.Hint"));
			imgURL=Images.SERVER_WEB_STOP.getURL();
		} else {
			startStopWebButton.setText(Language.tr("SimulationServer.Toolbar.WebStart"));
			startStopWebButton.setToolTipText(Language.tr("SimulationServer.Toolbar.WebStart.Hint"));
			imgURL=Images.SERVER_WEB_START.getURL();
		}
		if (imgURL!=null) startStopWebButton.setIcon(new ImageIcon(imgURL));

		webPortEditSpinner.setEnabled(!serverWeb.isRunning());

		webOpenBrowserButton.setVisible(serverWeb.isRunning());

		/* DDE-Server */

		if (serverDDE.isRunning()) {
			startStopDDEButton.setText(Language.tr("SimulationServer.Toolbar.DDEStop"));
			startStopDDEButton.setToolTipText(Language.tr("SimulationServer.Toolbar.DDEStop.Hint"));
			imgURL=Images.SERVER_DDE_STOP.getURL();
		} else {
			startStopDDEButton.setText(Language.tr("SimulationServer.Toolbar.DDEStart"));
			startStopDDEButton.setToolTipText(Language.tr("SimulationServer.Toolbar.DDEStart.Hint"));
			imgURL=Images.SERVER_DDE_START.getURL();
		}
		if (imgURL!=null) startStopDDEButton.setIcon(new ImageIcon(imgURL));

		/* Statusleiste */

		calcStatusBar.setText(getServerStatus(serverCalc.isServerRunning(),Language.tr("SimulationServer.Status.Server")));
		calcWebStatusBar.setText(getServerStatus(serverCalcWeb.isRunning(),Language.tr("SimulationServer.Status.CalcWeb")));
		webStatusBar.setText(getServerStatus(serverWeb.isRunning(),Language.tr("SimulationServer.Status.Web")));
		ddeStatusBar.setText(getServerStatus(serverDDE.isRunning(),Language.tr("SimulationServer.Status.DDE")));

		/* Andere Fenster benachrichtigen */
		if (triggerNotifiy) {
			ReloadManager.notify(notifyRunner);
		}
	}

	private int checkCalcPort(final boolean showErrorMessage) {
		final Integer I=(Integer)calcPortEdit.getValue();
		if (I==null) {
			if (showErrorMessage) MsgBox.error(this,Language.tr("SimulationServer.Setup.Port.ErrorTitle"),String.format(Language.tr("SimulationServer.Setup.Port.ErrorInfoInvalidNumber"),((JSpinner.DefaultEditor)calcPortEditSpinner.getEditor()).getTextField().getText()));
			return -1;
		}

		final int port=I.intValue();
		if (port<1 || port>65535) {
			calcPortEditSpinner.setBackground(Color.RED);
			if (showErrorMessage) MsgBox.error(this,Language.tr("SimulationServer.Setup.Port.ErrorTitle"),String.format(Language.tr("SimulationServer.Setup.Port.ErrorInfoInvalidPort"),port));
			return -1;
		} else {
			calcPortEditSpinner.setBackground(SystemColor.text);
		}

		return port;
	}

	private void commandStartStopCalc() {
		if (serverCalc.isServerRunning()) {
			serverCalc.stopServer();
		} else {
			final int port=checkCalcPort(true);
			if (port>0) serverCalc.startServer(port,calcPasswordEdit.getText(),calcLimitThreadsCheckBox.isSelected());
		}
		setupButtons();
	}

	private int checkCalcWebPort(final boolean showErrorMessage) {
		final Integer I=(Integer)calcWebPortEdit.getValue();
		if (I==null) {
			if (showErrorMessage) MsgBox.error(this,Language.tr("SimulationServer.Setup.Port.ErrorTitle"),String.format(Language.tr("SimulationServer.Setup.Port.ErrorInfoInvalidNumber"),((JSpinner.DefaultEditor)calcWebPortEditSpinner.getEditor()).getTextField().getText()));
			return -1;
		}

		final int port=I.intValue();
		if (port<1 || port>65535) {
			calcWebPortEditSpinner.setBackground(Color.RED);
			if (showErrorMessage) MsgBox.error(this,Language.tr("SimulationServer.Setup.Port.ErrorTitle"),String.format(Language.tr("SimulationServer.Setup.Port.ErrorInfoInvalidPort"),port));
			return -1;
		} else {
			calcWebPortEditSpinner.setBackground(SystemColor.text);
		}

		return port;
	}

	private void commandStartStopCalcWeb() {
		if (serverCalcWeb.isRunning()) {
			serverCalcWeb.stop();
		} else {
			final int port=checkCalcWebPort(true);
			if (port>0) {
				if (serverCalcWeb.start(port)) {
				} else {
					MsgBox.error(this,Language.tr("SimulationServer.Setup.CalcWebServer"),Language.tr("SimulationServer.Setup.CalcWebServer.MessageStartError"));
				}
			}
		}
		setupButtons();
	}

	private int checkWebPort(final boolean showErrorMessage) {
		final Integer I=(Integer)webPortEdit.getValue();
		if (I==null) {
			if (showErrorMessage) MsgBox.error(this,Language.tr("SimulationServer.Setup.Port.ErrorTitle"),String.format(Language.tr("SimulationServer.Setup.Port.ErrorInfoInvalidNumber"),((JSpinner.DefaultEditor)webPortEditSpinner.getEditor()).getTextField().getText()));
			return -1;
		}

		final int port=I.intValue();
		if (port<1 || port>65535) {
			webPortEditSpinner.setBackground(Color.RED);
			if (showErrorMessage) MsgBox.error(this,Language.tr("SimulationServer.Setup.Port.ErrorTitle"),String.format(Language.tr("SimulationServer.Setup.Port.ErrorInfoInvalidPort"),port));
			return -1;
		} else {
			webPortEditSpinner.setBackground(SystemColor.text);
		}

		return port;
	}

	private void commandStartStopWeb() {
		if (serverWeb.isRunning()) {
			serverWeb.stop();
			/* MsgBox.error(this,Language.tr("SimulationServer.Setup.WebServer"),Language.tr("SimulationServer.Setup.WebServer.MessageStopped")); */
		} else {
			final int port=checkWebPort(true);
			if (port>0) {
				if (serverWeb.start(port)) {
					/* MsgBox.error(this,Language.tr("SimulationServer.Setup.WebServer"),String.format(Language.tr("SimulationServer.Setup.WebServer.MessageStarted"),port)); */
				} else {
					MsgBox.error(this,Language.tr("SimulationServer.Setup.WebServer"),Language.tr("SimulationServer.Setup.WebServer.MessageStartError"));
				}
			}
		}
		setupButtons();
	}

	private void commandStartStopDDE() {
		if (serverDDE.isRunning()) {
			serverDDE.stop();
		} else {
			if (!serverDDE.start()) {
				MsgBox.error(this,Language.tr("SimulationServer.Setup.DDEServer"),Language.tr("SimulationServer.Setup.DDEServer.MessageStartError"));
			}
		}
		setupButtons();
	}

	private void commandHelp() {
		Help.topicModal(ServerPanel.this,"SimulationServer");
	}

	private void commandOpenBrowser(int port) {
		final StringBuilder sb=new StringBuilder();
		sb.append("http://localhost");
		if (port!=80) {
			sb.append(":");
			sb.append(port);
		}
		sb.append("/");

		try {
			Desktop.getDesktop().browse(new URI(sb.toString()));
		} catch (IOException | URISyntaxException e1) {
			MsgBox.error(this,Language.tr("Window.Info.NoInternetConnection"),String.format(Language.tr("Window.Info.NoInternetConnection.Address"),sb.toString()));
		}
		return;

	}

	@Override
	public void requestClose() {
		serverCalc.removeAllOutputListeners();

		int i;
		final SetupData setup=SetupData.getSetup();

		i=checkCalcPort(false); if (i>0) setup.simulationServerPort=i;
		setup.simulationServerPasswort=calcPasswordEdit.getText();
		setup.simulationServerLimitThreadCount=calcLimitThreadsCheckBox.isSelected();
		setup.simulationServerAutoStart=calcAutoStartCheckBox.isSelected();

		i=checkCalcWebPort(false); if (i>0) setup.calcWebServerPort=i;
		setup.calcWebServerAutoStart=calcWebAutoStartCheckBox.isSelected();

		i=checkWebPort(false); if (i>0) setup.webServerPort=i;
		setup.webServerAutoStart=webAutoStartCheckBox.isSelected();

		setup.ddeServerAutoStart=ddeAutoStartCheckBox.isSelected();

		setup.saveSetup();

		ReloadManager.removeBroadcastReceiver(notifyRunner);

		close();
	}

	@Override
	protected void userButtonClick(int index, JButton button) {
		if (button==startStopCalcButton) {commandStartStopCalc(); return;}
		if (button==startStopCalcWebButton) {commandStartStopCalcWeb(); return;}
		if (button==startStopWebButton) {commandStartStopWeb(); return;}
		if (button==startStopDDEButton) {commandStartStopDDE(); return;}
		if (button==helpButton) {commandHelp(); return;}
	}

	/**
	 * Startet die Serverdienste gemäß den Autostart-Einstellungen
	 * @param mainPanel	Hauptpanel des Simulators (für den Zugriff durch den Animations-Web-Server)
	 */
	public static void autoStartServers(final MainPanel mainPanel) {
		final SetupData setup=SetupData.getSetup();

		if (setup.simulationServerAutoStart) {
			final SimulationServerGUIConnect serverCalc=SimulationServerGUIConnect.getInstance();
			if (setup.simulationServerPort>0) serverCalc.startServer(setup.simulationServerPort,setup.simulationServerPasswort,setup.simulationServerLimitThreadCount);
		}

		if (setup.calcWebServerAutoStart) {
			final CalcWebServer serverCalcWeb=CalcWebServer.getInstance();
			if (setup.calcWebServerPort>0) serverCalcWeb.start(setup.calcWebServerPort);
		}

		if (setup.webServerAutoStart) {
			final SimulatorWebServer serverWeb=SimulatorWebServer.getInstance(mainPanel);
			if (setup.webServerPort>0) serverWeb.start(setup.webServerPort);
		}

		if (setup.ddeServerAutoStart) {
			final SimulationDDEServer serverDDE=SimulationDDEServer.getInstance(mainPanel);
			serverDDE.start();
		}
	}

	/**
	 * Trägt ein neues Hauptpanel in die laufenden Server ein.
	 * @param mainPanel	Hauptpanel des Simulators (für den Zugriff durch den Animations-Web-Server)
	 */
	public static void updateRunningServers(final MainPanel mainPanel) {
		SimulatorWebServer.updatePanel(mainPanel);
		SimulationDDEServer.updatePanel(mainPanel);
	}

	private class NotifyRunner implements Runnable {
		public final static String id="server";
		@Override
		public void run() {
			setupButtons(false);
		}
	}
}
