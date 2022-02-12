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
package Main;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.StringReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;

/**
 * This class allows to start and stop a Warteschlangensimulator instance in the background
 * and submit simulation tasks to this instance and also to submit tasks to an already
 * running instance of Warteschlangensimulator (with socket server enabled) or via direct
 * jar file loading into the current Java VM.
 * <br><br>
 * <u>Mode 1 (start&amp;stop Warteschlangensimulator instance):</u>
 * <pre>
 * try (SimulatorConnect connect=new SimulatorConnect(pathToSimulator)) {
 * 	xmlStatistic=connect.task(xmlModel);
 * }</pre>
 * <u>Mode 2 (use already running socket server):</u>
 * <pre>
 * xmlStatistic=SimulatorConnect.task(xmlModel,"localhost",port);</pre>
 * <u>Mode 3 (run simulator in this Java VM):</u>
 * <pre>
 * xmlStatistic=SimulatorConnect.taskJar(pathToSimulator,xmlModel);</pre>
 * <u>Hints:</u>
 * <ul>
 * <li>You can use <code>SimulatorConnect.exampleModel()</code> to get a simple example model which can be used for <code>xmlModel</code>.</li>
 * <li>If your Warteschlangensimulator installation can be found in one of the default locations, you can also use <code>new SimulatorConnect(null)</code>
 * for using path auto-detection.</li>
 * </ul>
 * @author Alexander Herzog
 */
public class SimulatorConnect implements AutoCloseable {
	/**
	 * Port to be used for socket server if a socket server is to be started by this class<br>
	 * (if not specified in constructor).
	 */
	public static int DEFAULT_SOCKET_PORT = 10000;

	private final int port;
	private final Process process;

	/**
	 * Constructor
	 * @param path	Path to "Simulator.jar" (use <code>null</code> for auto detection)
	 * @param port	Port for the socket server (use a number &le;0 for default port)
	 * @param timeout	Timeout in seconds (&le;0 for no timeout)
	 * @throws RuntimeException	If Simulator.jar or Java runtime is not found, a RuntimeException is thrown
	 * @throws IOException	If starting the subprocess fails, a IOException is thrown
	 */
	public SimulatorConnect(final File path, final int port, final int timeout) throws RuntimeException, IOException {
		final File simulatorJar=getSimulatorJar(path);
		final File java=getJava();

		if (simulatorJar==null) throw new RuntimeException("Simulator.jar not found");
		if (java==null) throw new RuntimeException("Java not found");

		this.port=(port>0)?port:DEFAULT_SOCKET_PORT;
		process=startSimulatorProcess(java,simulatorJar,port,timeout);

		try(BufferedReader input=new BufferedReader(new InputStreamReader(process.getInputStream()))) {
			int count=0;
			while (count<2 && input.readLine()!=null) count++;
		}
	}

	/**
	 * Constructor
	 * @param path	Path to "Simulator.jar" (use <code>null</code> for auto detection)
	 * @param port	Port for the socket server (use a number &le;0 for default port)
	 * @throws RuntimeException	If Simulator.jar or Java runtime is not found, a RuntimeException is thrown
	 * @throws IOException	If starting the subprocess fails, a IOException is thrown
	 */
	public SimulatorConnect(final File path, final int port) throws RuntimeException, IOException {
		this(path,port,-1);
	}

	/**
	 * Constructor
	 * @param path	Path to "Simulator.jar" (use <code>null</code> for auto detection)
	 * @throws RuntimeException	If Simulator.jar or Java runtime is not found, a RuntimeException is thrown
	 * @throws IOException	If starting the subprocess fails, a IOException is thrown
	 * @see #DEFAULT_SOCKET_PORT
	 */
	public SimulatorConnect(final File path) throws RuntimeException, IOException {
		this(path,DEFAULT_SOCKET_PORT,-1);
	}

	private static File getSimulatorJar(final File path) {
		if (path!=null && path.toString().endsWith("Simulator.jar") && path.isFile()) return path;

		final List<File> pathList=new ArrayList<>();

		/* User suggested path */
		if (path!=null) pathList.add(path);

		/* Windows default installation locations */
		String userHome=System.getProperty("user.home");
		if (!userHome.endsWith(File.separator)) userHome+=File.separator;
		pathList.add(new File(userHome+"AppData"+File.separator+"Roaming"+File.separator+"Warteschlangensimulator"));
		pathList.add(new File ("C:\\Program Files (x86)\\Warteschlangensimulator"));
		pathList.add(new File("C:\\Program Files\\Warteschlangensimulator"));

		/* Search in home folder */
		pathList.add(new File(userHome+"Warteschlangensimulator"));
		pathList.add(new File(userHome+"Desktop"+File.separator+"Warteschlangensimulator"));
		pathList.add(new File(userHome+"Documents"+File.separator+"Warteschlangensimulator"));
		pathList.add(new File(userHome+"QS"));
		pathList.add(new File(userHome+"Desktop"+File.separator+"QS"));
		pathList.add(new File(userHome+"Documents"+File.separator+"QS"));

		for (File test: pathList) {
			final File file=new File(test,"Simulator.jar");
			if (file.isFile()) return file;
		}

		return null;
	}

	private static File getJava() {
		final File folder=new File(System.getProperty("java.home"),"bin");
		File file;
		file=new File(folder,"java.exe");
		if (file.isFile()) return file;
		file=new File(folder,"java");
		if (file.isFile()) return file;
		return null;
	}

	private static Process startSimulatorProcess(final File java, final File simulatorJar, final int port, final int timeout) throws IOException {
		final List<String> cmd=new ArrayList<>();
		cmd.add(java.toString());
		cmd.add("-jar");
		cmd.add(simulatorJar.toString());
		cmd.add("serverSocket");
		cmd.add(""+port);
		if (timeout>0) cmd.add(""+timeout);

		return Runtime.getRuntime().exec(cmd.toArray(new String[0]));
	}

	@Override
	public void close() throws Exception {
		try(BufferedWriter output=new BufferedWriter(new OutputStreamWriter(process.getOutputStream()))) {
			output.newLine();
		}
	}

	/**
	 * Submits a simulation task to the Warteschlangensimulator socket server started by this class.
	 * @param xml	String containing the model xml data to be simulated
	 * @return	Returns a string containing the resulting statistic xml data in case of success; otherwise <code>null</code>
	 */
	public String task(final String xml) {
		return task(xml,"localhost",port);
	}

	private static final String COMMAND="Task_Now";

	private static void sendString(final DataOutputStream output, final String text) throws IOException {
		final byte[] bytes=text.getBytes(StandardCharsets.UTF_8);
		output.writeInt(bytes.length);
		output.write(bytes);
		output.flush();
	}

	private static String readString(final DataInputStream input) throws IOException {
		final int responseSize=input.readInt();
		final byte[] response=new byte[responseSize];
		int count=0;
		while (count<responseSize) {
			final int i=input.read(response,count,responseSize-count);
			if (i<0) return null;
			count+=i;
		}
		return new String(response,StandardCharsets.UTF_8);
	}

	/**
	 * Submits a simulation task to an already running Warteschlangensimulator instance.
	 * @param xml	String containing the model xml data to be simulated
	 * @param host	Host where Warteschlangensimulator runs
	 * @param port	Port number where the Warteschlangensimulator socket server is listening
	 * @return	Returns a string containing the resulting statistic xml data in case of success; otherwise <code>null</code>
	 */
	public static String task(final String xml, final String host, final int port) {
		try (Socket socket=new Socket("localhost",port)) {
			try (DataOutputStream output=new DataOutputStream(socket.getOutputStream())) {
				sendString(output,COMMAND);
				sendString(output,xml);
				try (DataInputStream input=new DataInputStream(socket.getInputStream())) {
					final String response=readString(input);
					if (!response.equalsIgnoreCase("Result")) return null;
					return readString(input);
				}
			}
		} catch (IOException e) {
			return null;
		}
	}

	/**
	 * Loads the Simulator.jar into the current Java VM and executes the
	 * simulation directly in this Java VM.
	 * @param path	Path to "Simulator.jar" (use <code>null</code> for auto detection)
	 * @param xml	Model xml data to be simulated
	 * @return	Returns in case of success a {@link Document} object containing the statistics data, otherwise an error message ({@link String})
	 */
	public static Object taskJar(final File path, final Document xml) {
		final File simulatorJar=getSimulatorJar(path);
		if (simulatorJar==null) return "Simulator.jar not found";

		/* Load Simulator.jar */
		final URLClassLoader classLoader;
		try {
			classLoader=new URLClassLoader(new URL[] {simulatorJar.toURI().toURL()},SimulatorConnect.class.getClassLoader());
		} catch (MalformedURLException e) {
			return "Cannot load Simulator.jar";
		}

		/* Load start.Simulator class */
		final Class<?> simulatorClass;
		try {
			simulatorClass=Class.forName("start.Simulator",true,classLoader);
		} catch (ClassNotFoundException e) {
			return "Simulator class not found in Simulator.jar";
		}

		/* Get start.Simulator instance */
		final Object simulator;
		try {
			Constructor<?> cnst=simulatorClass.getDeclaredConstructor();

			simulator=cnst.newInstance();
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
			if (e instanceof InvocationTargetException) {
				final InvocationTargetException e2=(InvocationTargetException)e;
				return e2.getTargetException()+" - If you are using modules, please add the missing \"requires\" line to your module-info.java";
			} else {
				return "Cannot generate instance of start.Simulator";
			}
		}

		/* Get needed methods */
		final Method loadModelMethod;
		final Method runSimulationMethod;
		final Method getStatisticsMethod;
		try {
			loadModelMethod=simulatorClass.getDeclaredMethod("loadModel",Document.class);
			runSimulationMethod=simulatorClass.getDeclaredMethod("runSimulation",PrintStream.class);
			getStatisticsMethod=simulatorClass.getDeclaredMethod("getStatistics");
		} catch (NoSuchMethodException | SecurityException e) {
			return "Cannot find methods to be executed in start.Simulator";
		}

		/* Load model */
		try {
			final Object result=loadModelMethod.invoke(simulator,xml);
			if (result!=null) {
				if (!(result instanceof String)) return "Simulator.loadModel returned unexpected data type";
				return result;
			}
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			return "Cannot invoke Simulator.loadModel";
		}

		/* Run simulation */
		try {
			final PrintStream printStream=null;
			final Object result=runSimulationMethod.invoke(simulator,printStream);
			if (result!=null) {
				if (!(result instanceof String)) return "Simulator.runSimulation returned unexpected data type";
				return result;
			}
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			return "Cannot invoke Simulator.runSimulation";
		}

		/* Get statistics */
		try {
			final Object result=getStatisticsMethod.invoke(simulator);
			if (!(result instanceof Document)) return "Simulation did not return statistic data";
			return result;
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			return "Cannot invoke Simulator.getStatistics";
		}
	}

	/**
	 * Loads the Simulator.jar into the current Java VM and executes the
	 * simulation directly in this Java VM.
	 * @param path	Path to "Simulator.jar" (use <code>null</code> for auto detection)
	 * @param xml	String containing the model xml data to be simulated
	 * @return	Returns in case of success a {@link Document} object containing the statistics data, otherwise an error message ({@link String})
	 */
	public static Object taskJar(final File path, final String model) {
		try {
			final DocumentBuilder builder=DocumentBuilderFactory.newInstance().newDocumentBuilder();
			final Document doc=builder.parse(new InputSource(new StringReader(model)));
			return taskJar(path,doc);
		} catch (Exception e) {
			return "Cannot convert model text to xml document";
		}
	}

	/**
	 * Returns the xml code as a string of a simple M/M/1 model.
	 * @return	Xml code as a string of a simple M/M/1 model
	 */
	public static String exampleModel() {
		final List<String> lines=new ArrayList<>();

		lines.add("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		lines.add("<Modell>");
		lines.add("  <ModellKunden Aktiv=\"1\">5000000</ModellKunden>");
		lines.add("  <ModellAufwaermphase>0.01</ModellAufwaermphase>");
		lines.add("  <ModellElemente>");
		lines.add("    <ModellElementBedienstation id=\"1\">");
		lines.add("      <ModellElementGroesse h=\"50\" w=\"100\" x=\"300\" y=\"100\"/>");
		lines.add("      <ModellElementVerbindung Element=\"4\" Typ=\"Einlaufend\"/>");
		lines.add("      <ModellElementVerbindung Element=\"5\" Typ=\"Auslaufend\"/>");
		lines.add("      <ModellElementVerteilung Status=\"Bedienzeit\" Typ=\"Bedienzeiten\" Zeitbasis=\"Sekunden\">Exponentialverteilung (80)</ModellElementVerteilung>");
		lines.add("      <ModellElementPrioritaet Kundentyp=\"Kunden\">w</ModellElementPrioritaet>");
		lines.add("      <ModellElementBediener Alternative=\"1\" Anzahl=\"1\" Gruppe=\"Bedienergruppe\"/>");
		lines.add("      <ModellElementBedienerPrioritaet>1</ModellElementBedienerPrioritaet>");
		lines.add("    </ModellElementBedienstation>");
		lines.add("    <ModellElementQuelle id=\"2\">");
		lines.add("      <ModellElementName>Kunden</ModellElementName>");
		lines.add("      <ModellElementGroesse h=\"50\" w=\"100\" x=\"50\" y=\"100\"/>");
		lines.add("      <ModellElementVerbindung Element=\"4\" Typ=\"Auslaufend\"/>");
		lines.add("      <ModellElementVerteilung Zeitbasis=\"Sekunden\">Exponentialverteilung (100)</ModellElementVerteilung>");
		lines.add("    </ModellElementQuelle>");
		lines.add("    <ModellElementEnde id=\"3\">");
		lines.add("      <ModellElementGroesse h=\"50\" w=\"100\" x=\"550\" y=\"100\"/>");
		lines.add("      <ModellElementVerbindung Element=\"5\" Typ=\"Einlaufend\"/>");
		lines.add("    </ModellElementEnde>");
		lines.add("    <ModellElementKante id=\"4\">");
		lines.add("      <ModellElementName>Ankünfte (Kunden)</ModellElementName>");
		lines.add("      <ModellElementVerbindung Element1=\"2\" Element2=\"1\" Typ=\"Kante\"/>");
		lines.add("    </ModellElementKante>");
		lines.add("      <ModellElementKante id=\"5\">");
		lines.add("      <ModellElementVerbindung Element1=\"1\" Element2=\"3\" Typ=\"Kante\"/>");
		lines.add("    </ModellElementKante>");
		lines.add("  </ModellElemente>");
		lines.add("  <Ressourcen UntergeordnetePrioritaet=\"Zufaellig\">");
		lines.add("    <Ressource Name=\"Bedienergruppe\" Typ=\"Anzahl\" Wert=\"1\"/>");
		lines.add("  </Ressourcen>");
		lines.add("</Modell>");

		return String.join("\n",lines);
	}
}
