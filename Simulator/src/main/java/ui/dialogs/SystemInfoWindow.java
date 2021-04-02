/**
 * Copyright 2021 Alexander Hbrerzog
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
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Window;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.Serializable;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import language.Language;
import mathtools.NumberTools;
import scripting.java.DynamicFactory;
import scripting.js.JSEngineNames;
import systemtools.BaseDialog;
import tools.SetupData;
import ui.images.Images;
import ui.tools.WindowSizeStorage;

/**
 * Zeigt ein Fenster mit Systeminformationen zur Java-Laufzeitumgebung an.
 * @author Alexander Herzog
 */
public class SystemInfoWindow extends JFrame {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID=6617836217219495056L;

	/**
	 * Aktualisierungsgeschwindigkeit für die Daten (in Millisekunden)
	 */
	private static final int UPDATE_SPEED=250;

	/**
	 * System zu Ermittlung der Arbeitsspeicher-Daten
	 */
	private final MemoryMXBean memory=ManagementFactory.getMemoryMXBean();

	/**
	 * System zu Ermittlung der Thread-Daten
	 */
	private final ThreadMXBean threads=ManagementFactory.getThreadMXBean();

	/**
	 * Anzahl der logischen CPU-Kerne in diesem System
	 */
	private final int cpuCount=Runtime.getRuntime().availableProcessors();

	/**
	 * Name der Java-Variante
	 */
	private final String infoVersion;

	/**
	 * Pfad zur der Java-Installation
	 */
	private final String infoPath;

	/**
	 * 32- oder 64-bittiges Java?
	 */
	private final String infoBit;

	/**
	 * Kompiler verfügbar?
	 */
	private final String infoCompiler;

	/**
	 * Vorhandenen Javascript-Umgebungen
	 */
	private final String infoJS;

	/**
	 * Timer zu aktualisierung der Daten
	 * @see #UPDATE_SPEED
	 */
	private Timer timer;

	/**
	 * {@link StringBuilder} in dem die Daten zur Anzeige in {@link #infoLabel}
	 * zusammengesetzt werden
	 * @see #updataData()
	 * @see #infoLabel
	 */
	private StringBuilder infoText;

	/**
	 * Label zur Anzeige der Systeminformationen
	 * @see #updataData()
	 */
	private JLabel infoLabel;

	/**
	 * Anzeige eines Prozentbalken zur Speicherauslastung
	 * @see #updataData()
	 */
	private JProgressBar statusMemory;

	/**
	 * Anzeige eines Prozentbalken zur CPU-Auslastung
	 * @see #updataData()
	 */
	private JProgressBar statusCPU;

	/**
	 * Globales Setup-Objekt
	 * @see #toolsPopup(Component)
	 */
	private SetupData setup;

	/**
	 * Instanz des Fensters
	 * @see #show(Component)
	 * @see #closeWindow()
	 */
	private static SystemInfoWindow instance;

	/**
	 * Zeigt das Fenster an.
	 * (Erstellt entweder ein neues Fenster oder holt das aktuelle in den Vordergrund.)
	 * @param owner	Übergeordnetes Element
	 */
	public static void show(final Component owner) {
		if (instance==null) {
			instance=new SystemInfoWindow(owner);
			instance.setVisible(true);
		} else {
			instance.toFront();
		}
	}

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Element
	 * @see #show(Component)
	 */
	private SystemInfoWindow(final Component owner) {
		super(Language.tr("SystemInfo.Title"));
		setIconImage(Images.EXTRAS_SYSTEM_INFO.getImage());
		setup=SetupData.getSetup();

		/* Übergeordnetes Fenster */
		Component o=owner;
		while (o!=null && !(o instanceof Window)) o=o.getParent();
		final Window ownerWindow=(Window)o;

		/* Aktionen bei Schließen des Fensters */
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		addWindowListener(new WindowAdapter(){
			@Override public void windowClosing(WindowEvent e){closeWindow();}
		});

		/* Statische Daten erheben */
		infoVersion=Language.tr("InfoDialog.JavaVersion")+": "+System.getProperty("java.version")+" ("+System.getProperty("java.vm.name")+")";
		infoPath=Language.tr("InfoDialog.JavaPath")+": "+System.getProperty("java.home");
		infoBit=Language.tr("InfoDialog.Is64Bit")+": "+(System.getProperty("os.arch").contains("64")?Language.tr("InfoDialog.Is64Bit.Yes"):Language.tr("InfoDialog.Is64Bit.No"));
		infoCompiler=Language.tr("InfoDialog.JavaCompiler")+": "+(DynamicFactory.hasCompiler()?Language.tr("InfoDialog.JavaCompiler.Yes"):Language.tr("InfoDialog.JavaCompiler.No"));
		infoJS=Language.tr("InfoDialog.JSEngine")+": "+String.join(", ",JSEngineNames.available().stream().map(e->e.name).toArray(String[]::new));

		/* Gesamter Inhaltsbereich */
		final Container all=getContentPane();
		all.setLayout(new BorderLayout());
		final JPanel content=new JPanel(new BorderLayout());
		final JToolBar toolbar=new JToolBar(SwingConstants.HORIZONTAL);
		toolbar.setFloatable(false);
		all.add(toolbar,BorderLayout.NORTH);
		all.add(content,BorderLayout.CENTER);
		final JPanel south=new JPanel();
		south.setLayout(new BoxLayout(south,BoxLayout.PAGE_AXIS));
		all.add(south,BorderLayout.SOUTH);

		/* Toolbar */
		JButton button;
		toolbar.add(button=new JButton(Language.tr("SystemInfo.Close"),Images.GENERAL_EXIT.getIcon()));
		button.setToolTipText(Language.tr("SystemInfo.Close.Hint"));
		button.addActionListener(e->{closeWindow(); setVisible(false);});
		toolbar.addSeparator();
		toolbar.add(button=new JButton(Language.tr("SystemInfo.GC"),Images.GENERAL_TRASH.getIcon()));
		button.setToolTipText(Language.tr("SystemInfo.GC.Hint"));
		button.addActionListener(e->System.gc());
		toolbar.add(button=new JButton(Language.tr("SystemInfo.Tools"),Images.GENERAL_SETUP.getIcon()));
		button.setToolTipText(Language.tr("SystemInfo.Tools.Hint"));
		button.addActionListener(e->toolsPopup((Component)e.getSource()));

		/* Info-Feld */
		content.add(new JScrollPane(infoLabel=new JLabel()));
		infoLabel.setVerticalAlignment(SwingConstants.TOP);
		infoLabel.setBorder(BorderFactory.createEmptyBorder(5,10,5,10));

		/* Statusbalken */
		final MemoryUsage memoryHeap=memory.getHeapMemoryUsage();
		final MemoryUsage memoryNonHeap=memory.getNonHeapMemoryUsage();
		final long memoryAvailableMB=(memoryHeap.getMax()+memoryNonHeap.getMax())/1024/1024;
		south.add(statusMemory=new JProgressBar(SwingConstants.HORIZONTAL));
		statusMemory.setMinimum(0);
		statusMemory.setMaximum((int)memoryAvailableMB);
		statusMemory.setForeground(Color.RED);
		statusMemory.setString(Language.tr("SystemInfo.Memory"));
		statusMemory.setStringPainted(true);
		south.add(statusCPU=new JProgressBar(SwingConstants.HORIZONTAL));
		statusCPU.setMinimum(0);
		statusCPU.setMaximum(cpuCount*1000);
		statusCPU.setForeground(Color.BLUE);
		statusCPU.setString(Language.tr("SystemInfo.Load"));
		statusCPU.setStringPainted(true);

		/* Timer aktivieren */
		infoText=new StringBuilder();
		timer=new Timer("SystemInfoUpdate",true);
		timer.schedule(new TimerTask() {
			@Override public void run() {updataData();}
		},UPDATE_SPEED,UPDATE_SPEED);

		/* Fenster vorbereiten */
		setSize((int)Math.round(480*BaseDialog.windowScaling),(int)Math.round(680*BaseDialog.windowScaling));
		setMinimumSize(new Dimension((int)Math.round(480*BaseDialog.windowScaling),(int)Math.round(680*BaseDialog.windowScaling)));
		pack();
		setResizable(true);
		setLocationRelativeTo(ownerWindow);
		WindowSizeStorage.window(this,"systeminfo");
	}

	/**
	 * Zeitpunkt des letzten Aufrufs von {@link #updataData()}
	 * @see #updataData()
	 */
	private long lastTimeStamp;

	/**
	 * CPU-Zeit der Threads beim letzten Aufruf
	 */
	private Map<Long,Long> lastLoad;

	/**
	 * Belegter Speicher (in MB) beim letzten Aufruf von {@link #updataData()}
	 * @see #updataData()
	 */
	private long lastMemoryUsedMB;

	/**
	 * Reservierter Speicher (in MB) beim letzten Aufruf von {@link #updataData()}
	 * @see #updataData()
	 */
	private long lastMemoryCommitedMB;

	/**
	 * Aktualisiert die Daten in {@link #infoLabel}
	 * @see #infoLabel
	 * @see #timer
	 */
	private void updataData() {
		/* Arbeitsspeicherdaten ermitteln */
		final MemoryUsage memoryHeap=memory.getHeapMemoryUsage();
		final MemoryUsage memoryNonHeap=memory.getNonHeapMemoryUsage();
		final long memoryUsedMB=(memoryHeap.getUsed()+memoryNonHeap.getUsed())/1024/1024;
		final long memoryCommitedMB=(memoryHeap.getCommitted()+memoryNonHeap.getCommitted())/1024/1024;
		final long memoryAvailableMB=(memoryHeap.getMax()+memoryNonHeap.getMax())/1024/1024;
		statusMemory.setValue((int)memoryUsedMB);
		statusMemory.setString(Language.tr("SystemInfo.Memory")+" ("+NumberTools.formatPercent(((double)memoryUsedMB)/memoryAvailableMB)+")");

		/* Auslastung ermitteln */
		final Set<Long> activeIDs=new HashSet<>();
		final Map<Long,Long> activeLoad=new HashMap<>();
		final long timeStamp=System.currentTimeMillis();
		long sum=0;
		final Map<Long,Long> load=new HashMap<>();
		for (long id: threads.getAllThreadIds()) {
			final long l=threads.getThreadCpuTime(id);
			load.put(id,l);
			final long threadLoad=(lastLoad==null)?l:(l-lastLoad.getOrDefault(id,0L));
			if (threadLoad>0) {
				activeIDs.add(id);
				activeLoad.put(id,threadLoad);
			}
			sum+=threadLoad;
		}
		lastLoad=load;
		final long delta=(timeStamp-lastTimeStamp)*1_000_000;
		lastTimeStamp=timeStamp;
		final double workLoad=((double)sum)/delta;
		final double workLoadSystem=workLoad/cpuCount;
		statusCPU.setValue((int)Math.round(workLoad*1000));
		statusCPU.setString(Language.tr("SystemInfo.Load")+" ("+NumberTools.formatPercent(Math.min(1,workLoadSystem))+")");

		/* Threaddaten ermitteln */
		final ThreadInfo[] threadInfo=threads.getThreadInfo(threads.getAllThreadIds());
		final Set<Thread> threadsList=Thread.getAllStackTraces().keySet();

		/* Ausgabe erstellen */
		infoText.setLength(0);

		infoText.append("<html><body>");

		/* Ausgabe: System */
		infoText.append("<h4>"+Language.tr("SystemInfo.System")+"</h4>\n");
		infoText.append("<p>\n");
		infoText.append(infoVersion+"<br>");
		infoText.append(infoPath+"<br>");
		infoText.append(infoBit+"<br>");
		infoText.append("</p>\n");
		infoText.append("<h4>"+Language.tr("SystemInfo.UserDefinedCode")+"</h4>\n");
		infoText.append("<p>\n");
		infoText.append(infoCompiler+"<br>");
		infoText.append(infoJS);
		infoText.append("</p>\n");

		/* Ausgabe: Speicher */
		infoText.append("<h4>"+Language.tr("SystemInfo.Memory")+"</h4>\n");
		infoText.append("<p>\n");
		infoText.append(Language.tr("SystemInfo.Memory.Available")+": "+NumberTools.formatLong(memoryAvailableMB)+" MB<br>\n");
		if (lastMemoryCommitedMB!=memoryCommitedMB) {
			infoText.append("<span style=\"color: blue;\">");
		} else {
			if (memoryCommitedMB>0.9*memoryAvailableMB) infoText.append("<span style=\"color: red;\">"); else infoText.append("<span>");
		}
		infoText.append(Language.tr("SystemInfo.Memory.Commited")+": "+NumberTools.formatLong(memoryCommitedMB)+" MB<br>\n");
		infoText.append("</span>");
		if (lastMemoryUsedMB!=memoryUsedMB) {
			infoText.append("<span style=\"color: blue;\">");
		} else {
			if (memoryUsedMB>0.9*memoryAvailableMB) infoText.append("<span style=\"color: red;\">"); else infoText.append("<span>");
		}
		infoText.append(Language.tr("SystemInfo.Memory.Used")+": "+NumberTools.formatLong(memoryUsedMB)+" MB<br>\n");
		infoText.append("</span>");
		lastMemoryCommitedMB=memoryCommitedMB;
		lastMemoryUsedMB=memoryUsedMB;

		try {
			infoText.append(Language.tr("SystemInfo.GC")+": "+String.join(", ",ManagementFactory.getGarbageCollectorMXBeans().stream().map(gcMxBean->gcMxBean.getName()).toArray(String[]::new)));
		} catch (RuntimeException e) {}

		infoText.append("</p>\n");

		/* Ausgabe: Arbeitslast */
		infoText.append("<h4>"+Language.tr("SystemInfo.Load")+"</h4>\n");
		infoText.append("<p>\n");
		if (workLoadSystem>0) {
			if (workLoadSystem>0.5) infoText.append("<span style=\"color: red;\">"); else infoText.append("<span style=\"color: blue;\">");
		} else {
			infoText.append("<span>");
		}
		infoText.append(Language.tr("SystemInfo.Load.Load")+": "+NumberTools.formatNumber(Math.min(cpuCount,workLoad))+"<br>\n");
		infoText.append("</span>");
		infoText.append(Language.tr("SystemInfo.Load.CPUCount")+": "+cpuCount+"<br>\n");
		if (workLoadSystem>0) {
			if (workLoadSystem>0.5) infoText.append("<span style=\"color: red;\">"); else infoText.append("<span style=\"color: blue;\">");
		} else {
			infoText.append("<span>");
		}
		infoText.append(Language.tr("SystemInfo.Load.LoadSystem")+": "+NumberTools.formatPercent(Math.min(1,workLoadSystem)));
		infoText.append("</span>");
		infoText.append("</p>\n");

		/* Ausgabe: Threads */
		infoText.append("<h4>"+Language.tr("SystemInfo.ThreadCount")+"</h4>\n");
		infoText.append("<p>\n");
		final int threadCount=threads.getThreadCount();
		final int threadDaemonCount=threads.getDaemonThreadCount();
		infoText.append(Language.tr("SystemInfo.ThreadCount.Foreground")+": "+(threadCount-threadDaemonCount)+" ("+Language.tr("SystemInfo.ThreadCount.Info")+")<br>\n");
		infoText.append(Language.tr("SystemInfo.ThreadCount.Background")+": "+threadDaemonCount+" ("+Language.tr("SystemInfo.ThreadCount.Info")+")<br>\n");
		infoText.append(Language.tr("SystemInfo.ThreadCount.Peak")+": "+threads.getPeakThreadCount()+"<br>\n");
		infoText.append("</p>\n");

		/* Ausgabe: Threads */
		infoText.append("<h4>"+Language.tr("SystemInfo.Threads")+"</h4>\n");
		infoText.append("<p>\n");
		for (ThreadInfo info: threadInfo) if (info!=null) {
			final long id=info.getThreadId();
			final Thread thread=threadsList.stream().filter(t->t.getId()==id).findFirst().orElseGet(()->null);
			if (thread==null || thread.isDaemon()) continue;
			if (!activeIDs.contains(id) && thread.getContextClassLoader()==null) continue;
			if (activeIDs.contains(id)) infoText.append("<span style=\"color: blue;\">"); else infoText.append("<span>");
			final String name=info.getThreadName();
			if (name.equals("DestroyJavaVM helper thread")) continue; /* OpenJ9 */
			infoText.append(name);
			infoText.append("</span>");
			infoText.append("<span style=\"color: gray;\">");
			final Long cpuTime=activeLoad.get(id);
			final double threadLoad=(cpuTime!=null)?Math.min(1,cpuTime.doubleValue()/delta):0.0;
			infoText.append(" ("+Language.tr("SystemInfo.Load.Load")+"="+NumberTools.formatNumber(threadLoad)+")");
			infoText.append("</span>");
			infoText.append("<br>\n");
		}
		infoText.append("</p>\n");

		/* Ausgabe: Hintergrund-Threads */
		boolean first=true;
		for (ThreadInfo info: threadInfo) if (info!=null)  {
			final long id=info.getThreadId();
			final Thread thread=threadsList.stream().filter(t->t.getId()==id).findFirst().orElseGet(()->null);
			if (thread==null || !thread.isDaemon()) continue;
			if (!activeIDs.contains(id) && threads.getThreadCpuTime(id)<500_000_000 && (thread.getContextClassLoader()==null || thread.getThreadGroup()==null || thread.getThreadGroup().getParent()==null)) continue;
			final String name=info.getThreadName();
			if (name.startsWith("SwingWorker-pool-")) continue;
			if (name.startsWith("JIT Compilation Thread-")) continue; /* OpenJ9 */
			if (name.startsWith("JVMCI-native CompilerThread")) continue; /* Graal */
			if (name.equals("SetupChangeWatcher")) continue;
			if (name.equals("SetupChangeWatcherFileSystem")) continue;
			if (name.equals("SystemInfoUpdate")) continue;
			if (name.equals("Reference Handler")) continue;
			if (name.equals("Finalizer")) continue;
			if (name.equals("Signal Dispatcher")) continue;
			if (name.equals("Attach Listener")) continue;
			if (name.equals("Attach API wait loop")) continue; /* OpenJ9 */
			if (name.equals("IProfiler")) continue; /* OpenJ9 */
			if (name.equals("Common-Cleaner")) continue;
			if (name.equals("Notification Thread")) continue;
			if (name.equals("Swing-Shell")) continue;
			if (name.equals("TimerQueue")) continue;
			if (name.equals("Java2D Disposer")) continue;
			if (first) {
				infoText.append("<h4>"+Language.tr("SystemInfo.DaemonThreads")+"</h4>\n");
				infoText.append("<p>\n");
				first=false;
			}
			if (activeIDs.contains(id)) infoText.append("<span style=\"color: blue;\">"); else infoText.append("<span>");
			infoText.append(name);
			infoText.append("</span>");
			infoText.append("<span style=\"color: gray;\">");
			final Long cpuTime=activeLoad.get(id);
			final double threadLoad=(cpuTime!=null)?Math.min(1,cpuTime.doubleValue()/delta):0.0;
			infoText.append(" ("+Language.tr("SystemInfo.Load.Load")+"="+NumberTools.formatNumber(threadLoad)+")");
			infoText.append("</span>");
			infoText.append("<br>\n");
		}
		if (!first) infoText.append("</p>\n");

		/* Ausgabeende */
		infoText.append("</body></html>");

		/* Ergebnisse anzeigen */
		final String newText=infoText.toString();
		SwingUtilities.invokeLater(()->infoLabel.setText(newText));
	}

	/**
	 * Zeigt das Tools-Popupmenü an.
	 * @param parent	Übergeordnetes Element
	 */
	private void toolsPopup(final Component parent) {
		final JPopupMenu menu=new JPopupMenu();

		JMenuItem label;
		JCheckBoxMenuItem item;

		menu.add(label=new JMenuItem("<html>"+Language.tr("SystemInfo.Tools.Info")+"</html>"));
		label.setEnabled(false);

		menu.add(label=new JMenuItem("<html><b>"+Language.tr("SystemInfo.Tools.Surface")+"</b></html>"));
		label.setEnabled(false);

		menu.add(item=new JCheckBoxMenuItem(Language.tr("SystemInfo.Tools.Surface.Antialias"),setup.antialias));
		item.addActionListener(e->{setup.antialias=!setup.antialias; setup.saveSetup();});

		menu.add(label=new JMenuItem("<html><b>"+Language.tr("SystemInfo.Tools.UserInterface")+"</b></html>"));
		label.setEnabled(false);

		menu.add(item=new JCheckBoxMenuItem(Language.tr("SystemInfo.Tools.UserInterface.QuickFilter"),setup.showQuickFilter));
		item.addActionListener(e->{setup.showQuickFilter=!setup.showQuickFilter; setup.saveSetup();});

		menu.add(item=new JCheckBoxMenuItem(Language.tr("SystemInfo.Tools.UserInterface.QuickAccess"),setup.showQuickAccess));
		item.addActionListener(e->{setup.showQuickAccess=!setup.showQuickAccess; setup.saveSetup();});

		menu.add(item=new JCheckBoxMenuItem(Language.tr("SystemInfo.Tools.UserInterface.MemoryUsage"),setup.showMemoryUsage));
		item.addActionListener(e->{setup.showMemoryUsage=!setup.showMemoryUsage; setup.saveSetup();});

		menu.add(item=new JCheckBoxMenuItem(Language.tr("SystemInfo.Tools.UserInterface.Feedback"),setup.showFeedbackButton));
		item.addActionListener(e->{setup.showFeedbackButton=!setup.showFeedbackButton; setup.saveSetup();});

		menu.add(label=new JMenuItem("<html><b>"+Language.tr("SystemInfo.Tools.Simulation")+"</b></html>"));
		label.setEnabled(false);

		menu.add(item=new JCheckBoxMenuItem(Language.tr("SystemInfo.Tools.Simulation.SecurityManager"),setup.useSecurityManagerForUserCode));
		item.addActionListener(e->{setup.useSecurityManagerForUserCode=!setup.useSecurityManagerForUserCode; setup.saveSetup();});

		menu.add(item=new JCheckBoxMenuItem(Language.tr("SystemInfo.Tools.Simulation.LoadBalancer"),setup.useDynamicThreadBalance));
		item.addActionListener(e->{setup.useDynamicThreadBalance=!setup.useDynamicThreadBalance; setup.saveSetup();});

		menu.show(parent,0,parent.getHeight());
	}

	/**
	 * Aktionen beim Schließen des Fensters.
	 */
	private void closeWindow() {
		timer.cancel();
		instance=null;
	}
}