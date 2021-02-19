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
package simulator.editmodel;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.imageio.ImageIO;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import language.Language;
import mathtools.NumberTools;
import mathtools.TimeTools;
import simulator.runmodel.RunModel;
import simulator.statistics.Statistics;
import simulator.statistics.Statistics.CorrelationMode;
import tools.SetupData;
import ui.MainPanel;
import ui.modeleditor.ModelAnimationImages;
import ui.modeleditor.ModelClientData;
import ui.modeleditor.ModelLoadData;
import ui.modeleditor.ModelLongRunStatistics;
import ui.modeleditor.ModelPaths;
import ui.modeleditor.ModelResource;
import ui.modeleditor.ModelResources;
import ui.modeleditor.ModelSchedules;
import ui.modeleditor.ModelSequences;
import ui.modeleditor.ModelSurface;
import ui.modeleditor.ModelTransporters;
import ui.modeleditor.SavedViews;
import ui.modeleditor.ScaledImageCache;
import ui.modeleditor.coreelements.DataCheckResult;
import ui.modeleditor.coreelements.ModelElement;
import ui.modeleditor.coreelements.ModelElementBox;
import ui.modeleditor.elements.ComplexLine;
import ui.modeleditor.elements.ElementNoRemoteSimulation;
import ui.modeleditor.elements.ElementWithScript;
import ui.modeleditor.elements.ModelElementAnimationConnect;
import ui.modeleditor.elements.ModelElementDisposeWithTable;
import ui.modeleditor.elements.ModelElementEdge;
import ui.modeleditor.elements.ModelElementInput;
import ui.modeleditor.elements.ModelElementInputDB;
import ui.modeleditor.elements.ModelElementInputDDE;
import ui.modeleditor.elements.ModelElementOutput;
import ui.modeleditor.elements.ModelElementOutputDB;
import ui.modeleditor.elements.ModelElementOutputDDE;
import ui.modeleditor.elements.ModelElementOutputJS;
import ui.modeleditor.elements.ModelElementOutputLog;
import ui.modeleditor.elements.ModelElementRecord;
import ui.modeleditor.elements.ModelElementSource;
import ui.modeleditor.elements.ModelElementSourceDB;
import ui.modeleditor.elements.ModelElementSourceDDE;
import ui.modeleditor.elements.ModelElementSourceMulti;
import ui.modeleditor.elements.ModelElementSourceRecord;
import ui.modeleditor.elements.ModelElementSourceTable;
import ui.modeleditor.elements.ModelElementSub;
import ui.modeleditor.templates.UserTemplates;

/**
 * Editor-Modell<br>
 * Dieses Modell kann inkonsistent sein, da Verknüpfungen zwischen Bedienstationen usw. nur per Freitext erfolgen.
 * Für diese Simulation wird aus diesem Modell ein Laufzeit-Modell in Form der Klasse <code>RunModel</code> abgeleitet.
 * Das Editor-Modell selbst wird in der Simulation nicht verwendet.
 * @author Alexander Herzog
 * @see RunModel
 * @see EditModelBase
 */
public final class EditModel extends EditModelBase implements Cloneable  {
	/**
	 * Version des Simulators diese wird in die Modell- und die Statistik-Dateien geschrieben, damit der Simulator
	 * warnen kann, wenn eine Datei, die mit einer späteren Version erstellt wurde, mit einer früheren Version, die
	 * evtl. nicht alle gespeicherten Eigenschaften darstellen kann, geöffnet wird.
	 */
	public static final String systemVersion=MainPanel.VERSION;

	/**
	 * Version des Simulators, mit der dieses Editor-Modell erstellt bzw. zu letzt gespeichert wurde.
	 */
	public String version;

	/**
	 * Name des Modells (hat für die Simulation keine Bedeutung, nur zur Bezeichnung des Modells)
	 */
	public String name;

	/**
	 * Beschreibung des Modells (hat für die Simulation keine Bedeutung, nur zur Bezeichnung des Modells)
	 */
	public String description;

	/**
	 * Autor des Models
	 */
	public String author;

	/**
	 * E-Mail-Adresse des Autors des Modells
	 */
	public String authorEMail;

	/**
	 * Gibt an, ob die Kundenankünfteanzahl als Abbruchkriterium verwendet werden soll
	 * @see #clientCount
	 */
	public boolean useClientCount;

	/**
	 * Anzahl der zu simulierenden Kundenankünfte
	 */
	public long clientCount;

	/**
	 * Länge der Einschwingphase (als Anteil der Kundenankünfte), bevor die Statistikzählung beginnt.<br>
	 * Die Einschwingphase wird nicht von der Kundenanzahl abgezogen, sondern besteht aus zusätzlichen Ankünften.
	 */
	public double warmUpTime;

	/**
	 * Gibt an, wie oft der Simulationslauf als Ganzes wiederholt werden soll.
	 */
	public int repeatCount;

	/**
	 * Gibt an, ob die Abbruchbedingung verwendet werden soll.
	 * @see #terminationCondition
	 */
	public boolean useTerminationCondition;

	/**
	 * Bedingung, die, wenn Sie erfüllt ist, das Simulationsende auslöst.
	 */
	public String terminationCondition;

	/**
	 * Gibt an, ob die Simulations-Ende-Zeit als Abbruchbedingung verwendet werden soll.
	 * @see #finishTime
	 */
	public boolean useFinishTime;

	/**
	 * Zeitpunkt, zu dem die Simulation beendet werden soll.
	 */
	public long finishTime;

	/**
	 * Gibt an, ob der Batch-Means-Konfidenzradius für die Wartezeiten als Abbruchbedingung verwendet werden soll.
	 * @see #finishConfidenceHalfWidth
	 * @see #finishConfidenceLevel
	 */
	public boolean useFinishConfidence;

	/**
	 * Abbruch bei Erreichen des Batch-Means-Konfidenzradius für die Wartezeiten (-1, wenn nicht zu verwenden)
	 */
	public double finishConfidenceHalfWidth;

	/**
	 * Abbruch bei Erreichen des Batch-Means-Konfidenzradius für die Wartezeiten für das angegebene Niveau (-1, wenn nicht zu verwenden)
	 */
	public double finishConfidenceLevel;

	/**
	 * Zusammenstellung der Modell-Elemente
	 */
	public ModelSurface surface;

	/**
	 * Ressourcen (Bediener)
	 */
	public ModelResources resources;

	/**
	 * Farben der Kundentypen in der Statistik
	 */
	public ModelClientData clientData;

	/**
	 * Zeitpläne
	 */
	public ModelSchedules schedules;

	/**
	 * Festen Seed für den Zufallszahlengenerator verwenden?
	 * @see #fixedSeed
	 */
	public boolean useFixedSeed;

	/**
	 * Seed für den Zufallszahlengenerator.<br>
	 * Ist nur aktiv, wenn <code>useFixedSeed=true</code> ist.
	 * @see #useFixedSeed
	 */
	public long fixedSeed;

	/**
	 * Zusätzliche Laufzeitstatistik.<br>
	 * Die Laufzeitstatistik ist optional; dieses Feld ist dennoch stets <code>!=null</code>.
	 */
	public ModelLongRunStatistics longRunStatistics;

	/**
	 * Maximaler Autokorrelationswert der bei der Erfassung der Daten vorgesehen werden soll.
	 * @see RunModel#correlationMode
	 */
	public int correlationRange;

	/**
	 * Art der Erfassung der Autokorrelation
	 * @see CorrelationMode#CORRELATION_MODE_OFF
	 * @see CorrelationMode#CORRELATION_MODE_FAST
	 * @see CorrelationMode#CORRELATION_MODE_FULL
	 */
	public Statistics.CorrelationMode correlationMode;

	/**
	 * Gibt an ob (bei &gt;1) und wenn ja von welcher Größe die Batches
	 * sein sollen, auf deren Basis Batch-Means berechnet werden sollen.
	 */
	public int batchMeansSize;

	/**
	 * Sollen die individuellen Wartezeiten gespeichert werden?
	 * @see Statistics#clientsAllWaitingTimesCollector
	 */
	public boolean collectWaitingTimes;

	/**
	 * Liste der Namen der globalen Variablen
	 */
	public final List<String> globalVariablesNames;

	/**
	 * Liste der Ausdrücke zu den globalen Variablen
	 */
	public final List<String> globalVariablesExpressions;

	/**
	 * Liste der Fertigungspläne
	 */
	public final ModelSequences sequences;

	/**
	 * Liste der Transporter
	 */
	public final ModelTransporters transporters;

	/**
	 * Vorgabewerte für die Farben der Zeichenfläche
	 * @see #surfaceColors
	 */
	private static final Color[] DEFAULT_COLORS=new Color[]{
			ModelSurface.DEFAULT_BACKGROUND_COLOR,
			ModelSurface.DEFAULT_RASTER_COLOR,
			ModelSurface.DEFAULT_BACKGROUND_GRADIENT_COLOR
	};

	/**
	 * 3-elementiges Array aus Hintergrund-, Raster- und Hintergrund-Gradient-Farbe der Zeichenfläche
	 */
	public final Color[] surfaceColors=Arrays.copyOf(DEFAULT_COLORS,DEFAULT_COLORS.length);

	/**
	 * Optionales Hintergrundbild
	 */
	public BufferedImage surfaceBackgroundImage;

	/**
	 * Skalierungsfaktor für das optionale Hintergrundbild
	 */
	public double surfaceBackgroundImageScale;

	/**
	 * Soll das Hintergrundbild auch in Untermodellen dargestellt werden?
	 */
	public boolean surfaceBackgroundImageInSubModels;

	/**
	 * Soll das Hintergrundbild vor oder hinter dem Raster gezeichnet werden?
	 */
	public ModelSurface.BackgroundImageMode surfaceBackgroundImageMode;

	/**
	 * Liste der Pfadsegmente für den Wegstrecken-Editor
	 */
	public final ModelPaths pathSegments;

	/**
	 * Liste der benutzerdefinierten Animationsbilder
	 */
	public final ModelAnimationImages animationImages;

	/**
	 * Welcher Sekundenwert soll in der Verteilungsstatistik maximal erfasst werden (Angabe in Stunden)?
	 */
	public int distributionRecordHours;

	/**
	 * Simulation abbrechen, wenn ein Rechenausdruck nicht ausgerechnet werden kann.
	 */
	public boolean stoppOnCalcError;

	/**
	 * Zeichenfunktion für normale Verbindungskanten.
	 */
	public final ComplexLine edgePainterNormal;

	/**
	 * Zeichenfunktion für ausgewählte Verbindungskanten.
	 */
	public final ComplexLine edgePainterSelected;

	/**
	 * Art der Verknüpfungslinien
	 */
	public ModelElementEdge.LineMode edgeLineMode;

	/**
	 * Zeitabstand in dem für Bedingung- und ähnliche Stationen zusätzliche zeitabhängige Checks durchgeführt werden sollen.
	 * Werte &le;0 bedeuten, dass keine Checks stattfinden. Sonst ist der Wert die Millisekundenanzahl zwischen zwei Checks.
	 */
	public int timedChecksDelta;

	/**
	 * Zählung wie häufig welche Stationsübergänge stattgefunden haben
	 */
	public boolean recordStationTransitions;

	/**
	 * Erfassung aller Kundenpfade
	 */
	public boolean recordClientPaths;

	/**
	 * Sollen auch Kunden, die das System am Ende noch nicht verlassen haben, in der Statistik erfasst werden?
	 */
	public boolean recordIncompleteClients;

	/**
	 * Vorlagen direkt im Modell
	 */
	private UserTemplates templates=null; /* Nicht automatisch mit Objekt belegen, sonst Endlosschleife. Da das Templates-Objekt ein Modell instanziert. */

	/**
	 * Liste mit den dynamisch zu Simulationsbeginn in das Modell zu ladenden Daten
	 */
	public final ModelLoadData modelLoadData;

	/**
	 * Verzeichnis für optionale externe Java-Klassendateien
	 */
	public String pluginsFolder;

	/**
	 * Gespeicherte Ansichten
	 */
	public SavedViews savedViews;

	/**
	 * Konstruktor der Klasse {@link EditModel}
	 */
	public EditModel() {
		resources=new ModelResources();
		clientData=new ModelClientData();
		schedules=new ModelSchedules();
		longRunStatistics=new ModelLongRunStatistics();
		globalVariablesNames=new ArrayList<>();
		globalVariablesExpressions=new ArrayList<>();
		sequences=new ModelSequences();
		transporters=new ModelTransporters();
		pathSegments=new ModelPaths();
		animationImages=new ModelAnimationImages();
		distributionRecordHours=1;
		stoppOnCalcError=false;
		edgePainterNormal=new ComplexLine(1,Color.BLACK,0);
		edgePainterSelected=new ComplexLine(1,Color.GREEN,0);
		edgeLineMode=ModelElementEdge.LineMode.MULTI_LINE_ROUNDED;
		timedChecksDelta=-1;
		recordStationTransitions=false;
		recordClientPaths=false;
		recordIncompleteClients=false;
		modelLoadData=new ModelLoadData();
		pluginsFolder="";
		savedViews=new SavedViews();
		resetData();
	}

	/**
	 * Wurzel-Element für Modell-xml-Dateien
	 */
	@Override
	public String[] getRootNodeNames() {
		return Language.trAll("Surface.XML.Model.Root");
	}

	/**
	 * Liefert den Standardwert für das "Autor"-Feld
	 * @return	Name des aktuellen Nutzers
	 */
	public static String getDefaultAuthor() {
		final SetupData setup=SetupData.getSetup();
		if (setup.defaultUserName!=null && !setup.defaultUserName.trim().isEmpty()) return setup.defaultUserName;
		return System.getProperty("user.name");
	}

	/**
	 * Liefert den Standardwert für das "Autor E-Mail-Adresse"-Feld
	 * @return	E-Mail-Adresse des aktuellen Nutzers
	 */
	public static String getDefaultAuthorEMail() {
		final SetupData setup=SetupData.getSetup();
		if (setup.defaultUserEMail!=null && !setup.defaultUserEMail.trim().isEmpty()) return setup.defaultUserEMail;
		return "";
	}

	@Override
	protected void resetData() {
		version=systemVersion;
		name="";
		description="";
		author=getDefaultAuthor();
		authorEMail=getDefaultAuthorEMail();
		useClientCount=true;
		clientCount=10_000_000;
		warmUpTime=0.01;
		repeatCount=1;
		useTerminationCondition=false;
		terminationCondition="";
		useFinishTime=false;
		finishTime=10*86400;
		useFinishConfidence=false;
		finishConfidenceHalfWidth=3;
		finishConfidenceLevel=0.95;
		resources.clear();
		clientData.clear();
		schedules.clear();
		surface=new ModelSurface(this,resources,schedules,null);
		useFixedSeed=false;
		fixedSeed=0;
		longRunStatistics.clear();
		correlationRange=-1;
		correlationMode=Statistics.CorrelationMode.CORRELATION_MODE_OFF;
		batchMeansSize=1;
		collectWaitingTimes=false;
		globalVariablesNames.clear();
		globalVariablesExpressions.clear();
		for (int i=0;i<surfaceColors.length;i++) surfaceColors[i]=DEFAULT_COLORS[i];
		surfaceBackgroundImage=null;
		surfaceBackgroundImageScale=1.0;
		surfaceBackgroundImageInSubModels=false;
		surfaceBackgroundImageMode=ModelSurface.BackgroundImageMode.BEHIND_RASTER;
		sequences.clear();
		transporters.clear();
		pathSegments.clear();
		animationImages.clear();
		distributionRecordHours=1;
		stoppOnCalcError=false;
		edgePainterNormal.set(1,Color.BLACK,0);
		edgePainterSelected.set(1,Color.GREEN,0);
		edgeLineMode=ModelElementEdge.LineMode.MULTI_LINE_ROUNDED;
		timedChecksDelta=-1;
		recordStationTransitions=false;
		recordClientPaths=false;
		recordIncompleteClients=false;
		templates=null;
		modelLoadData.clear();
		pluginsFolder="";
		savedViews.clear();
	}

	/**
	 * Erstellt eine Kopie des <code>EditModel</code>-Objektes
	 */
	@Override
	public EditModel clone() {
		final EditModel clone=new EditModel();

		clone.version=version;
		clone.name=name;
		clone.description=description;
		clone.author=author;
		clone.authorEMail=authorEMail;
		clone.useClientCount=useClientCount;
		clone.clientCount=clientCount;
		clone.warmUpTime=warmUpTime;
		clone.repeatCount=repeatCount;
		clone.useTerminationCondition=useTerminationCondition;
		clone.terminationCondition=terminationCondition;
		clone.useFinishTime=useFinishTime;
		clone.finishTime=finishTime;
		clone.useFinishConfidence=useFinishConfidence;
		clone.finishConfidenceHalfWidth=finishConfidenceHalfWidth;
		clone.finishConfidenceLevel=finishConfidenceLevel;
		clone.useFixedSeed=useFixedSeed;
		clone.fixedSeed=fixedSeed;
		clone.surface=surface.clone(false,clone.resources,clone.schedules,surface.getParentSurface(),clone); /* surface.getParentSurface() ist normalerweise null, es sei den, es wird ein SubSurface in ein EditModel eingehängt, um dieses SubModel bearbeiten zu können */
		clone.resources.setDataFrom(resources);
		clone.clientData.setDataFrom(clientData);
		clone.schedules.setDataFrom(schedules);
		clone.longRunStatistics.setDataFrom(longRunStatistics);
		clone.correlationRange=correlationRange;
		clone.correlationMode=correlationMode;
		clone.batchMeansSize=batchMeansSize;
		clone.collectWaitingTimes=collectWaitingTimes;
		clone.globalVariablesNames.addAll(globalVariablesNames);
		clone.globalVariablesExpressions.addAll(globalVariablesExpressions);
		for (int i=0;i<surfaceColors.length;i++) clone.surfaceColors[i]=surfaceColors[i];
		clone.surfaceBackgroundImage=surfaceBackgroundImage; /* Bild wird wenn immer neu zugewiesen, ist quasi immutable, daher reicht eine Referenz statt ScaledImageCache.copyImage(surfaceBackgroundImage); */
		clone.surfaceBackgroundImageScale=surfaceBackgroundImageScale;
		clone.surfaceBackgroundImageInSubModels=surfaceBackgroundImageInSubModels;
		clone.surfaceBackgroundImageMode=surfaceBackgroundImageMode;
		clone.sequences.setDataFrom(sequences);
		clone.transporters.setDataFrom(transporters);
		clone.pathSegments.setDataFrom(pathSegments);
		clone.animationImages.setDataFrom(animationImages);
		clone.distributionRecordHours=distributionRecordHours;
		clone.stoppOnCalcError=stoppOnCalcError;
		clone.edgePainterNormal.set(edgePainterNormal);
		clone.edgePainterSelected.set(edgePainterSelected);
		clone.edgeLineMode=edgeLineMode;
		clone.timedChecksDelta=timedChecksDelta;
		clone.recordStationTransitions=recordStationTransitions;
		clone.recordClientPaths=recordClientPaths;
		clone.recordIncompleteClients=recordIncompleteClients;
		if (templates!=null) clone.templates=templates.clone();
		clone.modelLoadData.copyDataFrom(modelLoadData);
		clone.pluginsFolder=pluginsFolder;
		clone.savedViews.copyFrom(savedViews);

		return clone;
	}

	/**
	 * Vergleicht das Editor-Modell mit einem anderen Editor-Modell
	 * (z.B. um zu prüfen, ob das Modell vor dem Verlassen des Programms gespeichert werden muss)
	 * @param otherModel	Editor-Modell, mit dem dieses Modell verglichen werden soll
	 * @return	Liefert <code>true</code> zurück, wenn die beiden Modelle identisch sind
	 */
	public boolean equalsEditModel(final EditModel otherModel) {
		return equalsEditModel(otherModel,false);
	}

	/**
	 * Vergleicht das Editor-Modell mit einem anderen Editor-Modell
	 * (z.B. um zu prüfen, ob das Modell vor dem Verlassen des Programms gespeichert werden muss)
	 * @param otherModel	Editor-Modell, mit dem dieses Modell verglichen werden soll
	 * @param ignoreAnimationData	Warm-Up-Zeit und {@link ModelElementAnimationConnect}-Elemente ignorieren?
	 * @return	Liefert <code>true</code> zurück, wenn die beiden Modelle identisch sind
	 */
	public boolean equalsEditModel(final EditModel otherModel, final boolean ignoreAnimationData) {
		/* if (!version.equalsIgnoreCase(otherModel.version)) return false; - Laden eines Beispiel (=alt Versionskennung), store to stream, load from stream würde sonst zu not equal führen */
		if (!name.equals(otherModel.name)) return false;
		if (!description.equals(otherModel.description)) return false;
		if (!author.equals(otherModel.author)) return false;
		if (!authorEMail.equals(otherModel.authorEMail)) return false;
		if (useClientCount!=otherModel.useClientCount) return false;
		if (clientCount!=otherModel.clientCount) return false;
		if (!ignoreAnimationData) {
			if (warmUpTime!=otherModel.warmUpTime) return false;
		}
		if (repeatCount!=otherModel.repeatCount) return false;
		if (useTerminationCondition!=otherModel.useTerminationCondition) return false;
		if (!terminationCondition.equalsIgnoreCase(otherModel.terminationCondition)) return false;
		if (useFinishTime!=otherModel.useFinishTime) return false;
		if (finishTime!=otherModel.finishTime) return false;
		if (useFinishConfidence!=otherModel.useFinishConfidence) return false;
		if (finishConfidenceHalfWidth!=otherModel.finishConfidenceHalfWidth) return false;
		if (finishConfidenceLevel!=otherModel.finishConfidenceLevel) return false;
		if (useFixedSeed!=otherModel.useFixedSeed) return false;
		if (fixedSeed!=otherModel.fixedSeed) return false;
		if (!surface.equalsModelSurface(otherModel.surface,ignoreAnimationData)) return false;
		if (!resources.equalsResources(otherModel.resources)) return false;
		if (!clientData.equalsModelClientData(otherModel.clientData)) return false;
		if (!schedules.equalsSchedules(otherModel.schedules)) return false;
		if (!longRunStatistics.equalsAdditionalStatistics(otherModel.longRunStatistics)) return false;
		if (correlationRange!=otherModel.correlationRange) return false;
		if (correlationMode!=otherModel.correlationMode) return false;
		if (batchMeansSize!=otherModel.batchMeansSize) return false;
		if (collectWaitingTimes!=otherModel.collectWaitingTimes) return false;
		if (globalVariablesNames.size()!=otherModel.globalVariablesNames.size()) return false;
		for (int i=0;i<globalVariablesNames.size();i++) if (!globalVariablesNames.get(i).equals(otherModel.globalVariablesNames.get(i))) return false;
		if (globalVariablesExpressions.size()!=otherModel.globalVariablesExpressions.size()) return false;
		for (int i=0;i<globalVariablesExpressions.size();i++) if (!globalVariablesExpressions.get(i).equals(otherModel.globalVariablesExpressions.get(i))) return false;
		for (int i=0;i<surfaceColors.length;i++) if (!Objects.equals(surfaceColors[i],otherModel.surfaceColors[i])) return false;
		if (!ScaledImageCache.compare(surfaceBackgroundImage,otherModel.surfaceBackgroundImage)) return false;
		if (surfaceBackgroundImageScale!=otherModel.surfaceBackgroundImageScale) return false;
		if (surfaceBackgroundImageInSubModels!=otherModel.surfaceBackgroundImageInSubModels) return false;
		if (surfaceBackgroundImageMode!=otherModel.surfaceBackgroundImageMode) return false;
		if (!sequences.equalsModelSequences(otherModel.sequences)) return false;
		if (!transporters.equalsModelTransporters(otherModel.transporters)) return false;
		if (!pathSegments.equalsModelPaths(otherModel.pathSegments)) return false;
		if (!animationImages.equalsModelAnimationImages(otherModel.animationImages)) return false;
		if (distributionRecordHours!=otherModel.distributionRecordHours) return false;
		if (stoppOnCalcError!=otherModel.stoppOnCalcError) return false;
		if (!edgePainterNormal.equalsLine(otherModel.edgePainterNormal)) return false;
		if (!edgePainterSelected.equalsLine(otherModel.edgePainterSelected)) return false;
		if (edgeLineMode!=otherModel.edgeLineMode) return false;
		if (timedChecksDelta!=otherModel.timedChecksDelta) return false;
		if (recordStationTransitions!=otherModel.recordStationTransitions) return false;
		if (recordClientPaths!=otherModel.recordClientPaths) return false;
		if (recordIncompleteClients!=otherModel.recordIncompleteClients) return false;
		if (!modelLoadData.equalsModelLoadData(otherModel.modelLoadData)) return false;
		if (!pluginsFolder.equalsIgnoreCase(otherModel.pluginsFolder)) return false;
		if (!savedViews.equalsSavedViews(savedViews)) return false;

		if (templates==null) {
			if (otherModel.templates!=null) return false;
		} else {
			if (otherModel.templates==null) return false;
			if (!templates.equalsTemplates(otherModel.templates)) return false;
		}

		return true;
	}

	/**
	 * Versucht eine Zeichenkette als Farbe zu interpretieren
	 * @param content	Zeichenkette
	 * @return	Farbwert oder <code>null</code>, wenn die Zeichenkette leer ist oder nicht als Farbe interpretiert werden konnte
	 * @see #saveColor(Color)
	 */
	public static Color loadColor(final String content) {
		if (content==null || content.trim().isEmpty()) return null;
		final String[] parts=content.split(",");
		Integer C1=null, C2=null, C3=null;
		if (parts.length==3) {
			C1=NumberTools.getNotNegativeInteger(parts[0]);
			C2=NumberTools.getNotNegativeInteger(parts[1]);
			C3=NumberTools.getNotNegativeInteger(parts[2]);
		}
		if (C1==null || C1>255 || C2==null || C2>255 || C3==null || C3>255)	return null;
		return new Color(C1,C2,C3);
	}

	/**
	 * Wandelt eine Farbe in eine speicherbare Zeichenkette um
	 * @param color	Zu speichernde Farbe
	 * @return	Zeichenkette, die der Farbe entspricht
	 * @see #loadColor(String)
	 */
	public static String saveColor(final Color color) {
		return ""+color.getRed()+","+color.getGreen()+","+color.getBlue();
	}

	/**
	 * Sind beim Laden des Modells unbekannte Elemente aufgetreten?`
	 * @see #isUnknownElementsOnLoad()
	 */
	private boolean unknownElementsOnLoad=false;

	/**
	 * Gibt nach dem Laden eines Modell an, ob alle Elemente vollständig geladen werden konnten,
	 * oder ob es unbekannte Elemente gab. Im Fall von unbekannten Elementen
	 * geben die Lade-Methoden trotzdem <code>null</code> zurück,
	 * so dass diese Methode benötigt wird, um abzufragen, ob XML-Elemente beim Laden übersprungen
	 * wurden. Der "unbekannte Elemente"-Marker wird vor dem Laden zurückgesetzt und beim
	 * Kopieren dieses Elements nicht mitkopiert.
	 * @return	Gibt <code>true</code> zurück, wenn beim Laden Elemente übersprungen wurden.
	 */
	public boolean isUnknownElementsOnLoad() {
		return unknownElementsOnLoad;
	}

	@Override
	protected String loadProperty(final String name, final String text, final Element node) {
		if (Language.trAll("Surface.XML.ModelVersion",name)) {
			if (text.isEmpty()) version=systemVersion; else version=text;
			return null;
		}
		if (Language.trAll("Surface.XML.ModelName",name)) {this.name=text; return null;}
		if (Language.trAll("Surface.XML.ModelDescription",name)) {description=text; return null;}
		if (Language.trAll("Surface.XML.ModelAuthor",name)) {author=text; return null;}
		if (Language.trAll("Surface.XML.ModelAuthorEMail",name)) {authorEMail=text; return null;}

		if (Language.trAll("Surface.XML.ModelClients",name)) {
			final Long L=NumberTools.getNotNegativeLong(text);
			if (L==null || L==0) return String.format(Language.tr("Surface.Model.ErrorClients"),text);
			clientCount=L;
			final String s=Language.trAllAttribute("Surface.XML.Active",node);
			useClientCount=!s.trim().isEmpty() && !s.equals("0");
			return null;
		}
		if (Language.trAll("Surface.XML.ModelWarmUpPhase",name)) {
			final Double D=NumberTools.getExtProbability(text);
			if (D==null || D<0) return String.format(Language.tr("Surface.Model.ErrorWarmUp"),text);
			warmUpTime=D;
			return null;
		}
		if (Language.trAll("Surface.XML.ModelRepeatCount",name)) {
			final Long L=NumberTools.getPositiveLong(text);
			if (L==null) return String.format(Language.tr("Surface.Model.ErrorRepeatCount"),text);
			repeatCount=(int)L.longValue();
			return null;
		}
		if (Language.trAll("Surface.XML.ModelTerminationCondition",name)) {
			terminationCondition=text;
			final String s=Language.trAllAttribute("Surface.XML.Active",node);
			useTerminationCondition=!s.trim().isEmpty() && !s.equals("0");
			return null;
		}
		if (Language.trAll("Surface.XML.ModelTerminationTime",name)) {
			finishTime=TimeTools.getTime(text);
			final String s=Language.trAllAttribute("Surface.XML.Active",node);
			useFinishTime=!s.trim().isEmpty() && !s.equals("0");
			return null;
		}
		if (Language.trAll("Surface.XML.ModelTerminationConfidence",name)) {
			final Double halfWidth=NumberTools.getPositiveDouble(text);
			final Double level=NumberTools.getProbability(Language.trAllAttribute("Surface.XML.Level",node));

			if (halfWidth==null) return String.format(Language.tr("Surface.Model.ErrorTerminationConfidenceHalfWidth"),text);
			if (level==null || level>=1) return String.format(Language.tr("Surface.Model.ErrorTerminationConfidenceLevel"),Language.trAllAttribute("Surface.XML.Level",node));

			final String s=Language.trAllAttribute("Surface.XML.Active",node);
			useFinishConfidence=!s.trim().isEmpty() && !s.equals("0");
			if (useFinishConfidence) {
				finishConfidenceHalfWidth=halfWidth;
				finishConfidenceLevel=level;
			}
			return null;
		}
		if (Language.trAll("Surface.XML.ModelFixedSeed",name)) {
			final Long L=NumberTools.getLong(text);
			if (L==null) return String.format(Language.tr("Surface.Model.ErrorSeed"),text);
			fixedSeed=L;
			final String s=Language.trAllAttribute("Surface.XML.Active",node);
			useFixedSeed=!s.trim().isEmpty() && !s.equals("0");
			return null;
		}

		if (Language.trAll("Surface.XML.ModelCorrelation",name)) {
			final Integer I=NumberTools.getNotNegativeInteger(text);
			if (I==null) return String.format(Language.tr("Surface.Model.ErrorCorrelation"),text);
			final String s=Language.trAllAttribute("Surface.XML.ModelCorrelation.Full",node);
			if (s.equals("1")) {
				correlationMode=Statistics.CorrelationMode.CORRELATION_MODE_FULL;
			} else {
				correlationMode=Statistics.CorrelationMode.CORRELATION_MODE_FAST;
			}
			correlationRange=I;
			return null;
		}

		if (Language.trAll("Surface.XML.ModelBatchMeans",name)) {
			final Integer I=NumberTools.getNotNegativeInteger(text);
			if (I==null) return String.format(Language.tr("Surface.Model.ErrorBatchMeans"),text);
			batchMeansSize=I.intValue();
			return null;
		}

		if (Language.trAll("Surface.XML.ModelCollectWaitingTimes",name)) {
			final String s=Language.trAllAttribute("Surface.XML.Active",node);
			collectWaitingTimes=s.equals("1");
			return null;
		}

		for (String test: ModelSurface.XML_NODE_NAME) if (name.equalsIgnoreCase(test)) {
			unknownElementsOnLoad=false;
			final String error=surface.loadFromXML(node);
			unknownElementsOnLoad=surface.isUnknownElementsOnLoad();
			if (error!=null) return error;
			return null;
		}

		for (String test: ModelResources.XML_NODE_NAME) if (name.equalsIgnoreCase(test)) {
			final String error=resources.loadFromXML(node);
			if (error!=null) return error;
			return null;
		}

		for (String test: ModelClientData.XML_NODE_NAME) if (name.equalsIgnoreCase(test)) {
			final String error=clientData.loadFromXML(node);
			if (error!=null) return error;
			return null;
		}

		for (String test: ModelSchedules.XML_NODE_NAME) if (name.equalsIgnoreCase(test)) {
			final String error=schedules.loadFromXML(node);
			if (error!=null) return error;
			return null;
		}

		for (String test: ModelLongRunStatistics.XML_NODE_NAME) if (name.equalsIgnoreCase(test)) {
			final String error=longRunStatistics.loadFromXML(node);
			if (error!=null) return error;
			return null;
		}

		if (Language.trAll("Surface.XML.GlobalVariable",name)) {
			final String varName=Language.trAllAttribute("Surface.XML.GlobalVariable.Name",node);
			if (varName!=null && !varName.trim().isEmpty()) {
				globalVariablesNames.add(varName);
				globalVariablesExpressions.add(text);
			}
			return null;
		}

		if (Language.trAll("Surface.XML.SurfaceColor",name)) {
			final Color c1=loadColor(Language.trAllAttribute("Surface.XML.SurfaceColor.Background",node));
			final Color c2=loadColor(Language.trAllAttribute("Surface.XML.SurfaceColor.Raster",node));
			final Color c3=loadColor(Language.trAllAttribute("Surface.XML.SurfaceColor.Background2",node));
			if (c1!=null) surfaceColors[0]=c1;
			if (c2!=null) surfaceColors[1]=c2;
			if (c3!=null) surfaceColors[2]=c3;
			return null;
		}

		if (Language.trAll("Surface.XML.SurfaceBackgroundImage",name)) {
			final Double D=NumberTools.getPositiveDouble(Language.trAllAttribute("Surface.XML.SurfaceBackgroundImage.Scale",node));
			if (D!=null) surfaceBackgroundImageScale=D.doubleValue();
			final String inSubModels=Language.trAllAttribute("Surface.XML.SurfaceBackgroundImage.InSubModels",node);
			if (!inSubModels.isEmpty() && !inSubModels.equals("0")) surfaceBackgroundImageInSubModels=true;
			final String inFrontOfRaster=Language.trAllAttribute("Surface.XML.SurfaceBackgroundImage.InFrontOfRaster",node);
			if (!inFrontOfRaster.isEmpty() && !inFrontOfRaster.equals("0")) surfaceBackgroundImageMode=ModelSurface.BackgroundImageMode.IN_FRONT_OF_RASTER;
			if (!text.isEmpty()) {
				try {
					final ByteArrayInputStream stream=new ByteArrayInputStream(Base64.getDecoder().decode(text));

					final boolean useCache=ImageIO.getUseCache();
					try {
						ImageIO.setUseCache(false); /* Wird benötigt, wenn im Stream nicht gesprungen werden kann, was bei einem ByteArrayInputStream nun definitiv möglich ist.  */
						final BufferedImage image=ImageIO.read(stream);
						if (image!=null) surfaceBackgroundImage=image;
					} finally {
						ImageIO.setUseCache(useCache);
					}
				} catch (IOException | IllegalArgumentException e) {}
			}
			return null;
		}

		for (String test: ModelSequences.XML_NODE_NAME) if (name.equalsIgnoreCase(test)) {
			final String error=sequences.loadFromXML(node);
			if (error!=null) return error;
			return null;
		}

		for (String test: ModelTransporters.XML_NODE_NAME) if (name.equalsIgnoreCase(test)) {
			final String error=transporters.loadFromXML(node);
			if (error!=null) return error;
			return null;
		}

		for (String test: ModelPaths.XML_NODE_NAME) if (name.equalsIgnoreCase(test)) {
			final String error=pathSegments.loadFromXML(node);
			if (error!=null) return error;
			return null;
		}

		for (String test: ModelAnimationImages.XML_NODE_NAME) if (name.equalsIgnoreCase(test)) {
			final String error=animationImages.loadFromXML(node);
			if (error!=null) return error;
			return null;
		}

		if (Language.trAll("Surface.XML.DistributionRecordHours",name)) {
			final Long L=NumberTools.getNotNegativeLong(text);
			if (L==null) return String.format(Language.tr("Surface.Model.DistributionRecordHours"),text);
			distributionRecordHours=L.intValue();
			return null;
		}

		if (Language.trAll("Surface.XML.StoppOnCalcError",name)) {
			stoppOnCalcError=text.equals("1");
			return null;
		}

		if (Language.trAll("Surface.XML.EdgePainter.Normal",name)) {
			final String widthString=Language.trAllAttribute("Surface.XML.EdgePainter.Width",node);
			if (!widthString.isEmpty()) {
				final Long width=NumberTools.getPositiveLong(widthString);
				if (width==null) return String.format(Language.tr("Surface.XML.EdgePainter.Width.Error"),name,widthString);
				edgePainterNormal.setWidth(width.intValue());
			}

			final Color c=loadColor(Language.trAllAttribute("Surface.XML.EdgePainter.Color",node));
			if (c!=null) edgePainterNormal.setColor(c);

			final String lineTypeString=Language.trAllAttribute("Surface.XML.EdgePainter.Type",node);
			if (!lineTypeString.isEmpty()) {
				final Long lineType=NumberTools.getNotNegativeLong(lineTypeString);
				if (lineType==null) return String.format(Language.tr("Surface.XML.EdgePainter.Type.Error"),name,lineTypeString);
				edgePainterNormal.setType(lineType.intValue());
			}

			return null;
		}

		if (Language.trAll("Surface.XML.EdgePainter.Selected",name)) {
			final String widthString=Language.trAllAttribute("Surface.XML.EdgePainter.Width",node);
			if (!widthString.isEmpty()) {
				final Long width=NumberTools.getPositiveLong(widthString);
				if (width==null) return String.format(Language.tr("Surface.XML.EdgePainter.Width.Error"),name,widthString);
				edgePainterSelected.setWidth(width.intValue());
			}

			/*
			keine Farbe für selected
			final Color c=loadColor(Language.trAllAttribute("Surface.XML.EdgePainter.Color",node));
			if (c!=null) edgePainterSelected.setColor(c);
			 */

			final String lineTypeString=Language.trAllAttribute("Surface.XML.EdgePainter.Type",node);
			if (!lineTypeString.isEmpty()) {
				final Long lineType=NumberTools.getNotNegativeLong(lineTypeString);
				if (lineType==null) return String.format(Language.tr("Surface.XML.EdgePainter.Type.Error"),name,lineTypeString);
				edgePainterSelected.setType(lineType.intValue());
			}

			return null;
		}

		if (Language.trAll("Surface.XML.LineMode",name)) {
			if (Language.trAll("Surface.XML.LineMode.Direct",text)) edgeLineMode=ModelElementEdge.LineMode.DIRECT;
			if (Language.trAll("Surface.XML.LineMode.MultiLine",text)) edgeLineMode=ModelElementEdge.LineMode.MULTI_LINE;
			if (Language.trAll("Surface.XML.LineMode.MultiLineRounded",text)) edgeLineMode=ModelElementEdge.LineMode.MULTI_LINE_ROUNDED;
			if (Language.trAll("Surface.XML.LineMode.CubicCurve",text)) edgeLineMode=ModelElementEdge.LineMode.CUBIC_CURVE;
			return null;
		}

		if (Language.trAll("Surface.XML.TimedChecks",name)) {
			final Long ms=NumberTools.getPositiveLong(text);
			if (ms==null) return String.format(Language.tr("Surface.XML.TimedChecks.Error"),name,text);
			timedChecksDelta=ms.intValue();
			return null;
		}

		if (Language.trAll("Surface.XML.PathRecording",name)) {
			String s;
			s=Language.trAllAttribute("Surface.XML.PathRecording.StationTransitions",node);
			recordStationTransitions=!s.trim().isEmpty() && !s.equals("0");
			s=Language.trAllAttribute("Surface.XML.PathRecording.ClientPaths",node);
			recordClientPaths=!s.trim().isEmpty() && !s.equals("0");
			return null;
		}

		if (Language.trAll("Surface.XML.RecordIncompleteClients",name)) {
			recordIncompleteClients=!text.trim().isEmpty() && !text.equals("0");
			return null;
		}

		if (UserTemplates.isTemplatesNode(name)) {
			templates=new UserTemplates();
			templates.load(node);
			return null;
		}

		for (String test: ModelLoadData.XML_NODE_NAME) if (name.equalsIgnoreCase(test)) {
			final String error=modelLoadData.loadFromXML(node);
			if (error!=null) return error;
			return null;
		}

		if (Language.trAll("Surface.XML.PluginsFolder",name)) {
			pluginsFolder=text;
			return null;
		}

		for (String test: SavedViews.XML_NODE_NAME) if (name.equalsIgnoreCase(test)) {
			final String error=savedViews.loadFromXML(node);
			if (error!=null) return error;
			return null;
		}

		return null;
	}

	@Override
	protected void addDataToXML(final Document doc, final Element node, final boolean isPartOfOtherFile, final File file) {
		Element sub;

		if (!isPartOfOtherFile) version=systemVersion; /* Versionskennung aktualisieren, sofern das Modell direkt gespeichert wird und nicht nur Teil einer Statistikdatei ist. */

		addTextToXML(doc,node,Language.trPrimary("Surface.XML.ModelVersion"),version);
		if (!name.isEmpty()) addTextToXML(doc,node,Language.trPrimary("Surface.XML.ModelName"),name);
		if (!description.isEmpty()) addTextToXML(doc,node,Language.trPrimary("Surface.XML.ModelDescription"),description);
		addTextToXML(doc,node,Language.trPrimary("Surface.XML.ModelAuthor"),author);
		if (!authorEMail.isEmpty()) addTextToXML(doc,node,Language.trPrimary("Surface.XML.ModelAuthorEMail"),authorEMail);
		sub=addTextToXML(doc,node,Language.trPrimary("Surface.XML.ModelClients"),clientCount);
		sub.setAttribute(Language.trPrimary("Surface.XML.Active"),useClientCount?"1":"0");
		addTextToXML(doc,node,Language.trPrimary("Surface.XML.ModelWarmUpPhase"),NumberTools.formatSystemNumber(warmUpTime));
		if (repeatCount>1) addTextToXML(doc,node,Language.trPrimary("Surface.XML.ModelRepeatCount"),""+repeatCount);
		if (useTerminationCondition || !terminationCondition.isEmpty()) {
			sub=addTextToXML(doc,node,Language.trPrimary("Surface.XML.ModelTerminationCondition"),terminationCondition);
			sub.setAttribute(Language.trPrimary("Surface.XML.Active"),useTerminationCondition?"1":"0");
		}
		if (useFinishTime || finishTime>0) {
			sub=addTextToXML(doc,node,Language.trPrimary("Surface.XML.ModelTerminationTime"),TimeTools.formatLongTime(finishTime));
			sub.setAttribute(Language.trPrimary("Surface.XML.Active"),useFinishTime?"1":"0");
		}
		if (useFinishConfidence) {
			sub=addTextToXML(doc,node,Language.trPrimary("Surface.XML.ModelTerminationConfidence"),NumberTools.formatSystemNumber(finishConfidenceHalfWidth));
			sub.setAttribute(Language.trPrimary("Surface.XML.Active"),"1");
			sub.setAttribute(Language.trPrimary("Surface.XML.Level"),NumberTools.formatSystemNumber(finishConfidenceLevel));
		}
		if (useFixedSeed || fixedSeed!=0) {
			sub=addTextToXML(doc,node,Language.trPrimary("Surface.XML.ModelFixedSeed"),fixedSeed);
			sub.setAttribute(Language.trPrimary("Surface.XML.Active"),useFixedSeed?"1":"0");
		}
		if (correlationMode!=Statistics.CorrelationMode.CORRELATION_MODE_OFF && correlationRange>0) {
			sub=addTextToXML(doc,node,Language.trPrimary("Surface.XML.ModelCorrelation"),correlationRange);
			if (correlationMode==Statistics.CorrelationMode.CORRELATION_MODE_FULL) sub.setAttribute(Language.trPrimary("Surface.XML.ModelCorrelation.Full"),"1");
		}
		if (batchMeansSize>1) addTextToXML(doc,node,Language.trPrimary("Surface.XML.ModelBatchMeans"),batchMeansSize);

		if (collectWaitingTimes) {
			node.appendChild(sub=doc.createElement(Language.trPrimary("Surface.XML.ModelCollectWaitingTimes"))); /* nicht in dtd/xsd, da das im normalen Programm nie vor kommt. */
			sub.setAttribute(Language.trPrimary("Surface.XML.Active"),"1");
		}

		surface.addDataToXML(doc,node);
		resources.addDataToXML(doc,node);
		clientData.addDataToXML(doc,node);
		schedules.addToXML(doc,node);
		longRunStatistics.addToXML(doc,node);

		for (int i=0;i<Math.min(globalVariablesNames.size(),globalVariablesExpressions.size());i++) {
			node.appendChild(sub=doc.createElement(Language.trPrimary("Surface.XML.GlobalVariable")));
			sub.setAttribute(Language.trPrimary("Surface.XML.GlobalVariable.Name"),globalVariablesNames.get(i));
			sub.setTextContent(globalVariablesExpressions.get(i));
		}

		if (surfaceColors!=null && surfaceColors.length>=2 && surfaceColors[0]!=null && surfaceColors[1]!=null && !Objects.deepEquals(surfaceColors,DEFAULT_COLORS)) {
			node.appendChild(sub=doc.createElement(Language.trPrimary("Surface.XML.SurfaceColor")));
			sub.setAttribute(Language.trPrimary("Surface.XML.SurfaceColor.Background"),saveColor(surfaceColors[0]));
			sub.setAttribute(Language.trPrimary("Surface.XML.SurfaceColor.Raster"),saveColor(surfaceColors[1]));
			if (surfaceColors[2]!=null) sub.setAttribute(Language.trPrimary("Surface.XML.SurfaceColor.Background2"),saveColor(surfaceColors[2]));
		}

		if (surfaceBackgroundImage!=null || surfaceBackgroundImageScale!=1.0 || surfaceBackgroundImageInSubModels || surfaceBackgroundImageMode==ModelSurface.BackgroundImageMode.IN_FRONT_OF_RASTER) {
			node.appendChild(sub=doc.createElement(Language.trPrimary("Surface.XML.SurfaceBackgroundImage")));
			if (surfaceBackgroundImageScale!=1.0) sub.setAttribute(Language.trPrimary("Surface.XML.SurfaceBackgroundImage.Scale"),NumberTools.formatSystemNumber(surfaceBackgroundImageScale));
			if (surfaceBackgroundImageInSubModels) sub.setAttribute(Language.trPrimary("Surface.XML.SurfaceBackgroundImage.InSubModels"),"1");
			if (surfaceBackgroundImageMode==ModelSurface.BackgroundImageMode.IN_FRONT_OF_RASTER) sub.setAttribute(Language.trPrimary("Surface.XML.SurfaceBackgroundImage.InFrontOfRaster"),"1");
			if (surfaceBackgroundImage!=null) {
				try {
					final ByteArrayOutputStream stream=new ByteArrayOutputStream();
					ImageIO.write(surfaceBackgroundImage,"png",stream);
					sub.setTextContent(new String(Base64.getEncoder().encode(stream.toByteArray())));
				} catch (IOException e) {}
			}
		}

		sequences.addDataToXML(doc,node);
		transporters.addDataToXML(doc,node);
		pathSegments.addDataToXML(doc,node);
		animationImages.addDataToXML(doc,node);

		if (distributionRecordHours!=1) {
			node.appendChild(sub=doc.createElement(Language.trPrimary("Surface.XML.DistributionRecordHours")));
			sub.setTextContent(""+distributionRecordHours);
		}

		if (stoppOnCalcError) {
			node.appendChild(sub=doc.createElement(Language.trPrimary("Surface.XML.StoppOnCalcError")));
			sub.setTextContent("1");
		}

		node.appendChild(sub=doc.createElement(Language.trPrimary("Surface.XML.EdgePainter.Normal")));
		sub.setAttribute(Language.trPrimary("Surface.XML.EdgePainter.Width"),""+edgePainterNormal.getWidth());
		sub.setAttribute(Language.trPrimary("Surface.XML.EdgePainter.Color"),saveColor(edgePainterNormal.getColor()));
		sub.setAttribute(Language.trPrimary("Surface.XML.EdgePainter.Type"),""+edgePainterNormal.getType());

		node.appendChild(sub=doc.createElement(Language.trPrimary("Surface.XML.EdgePainter.Selected")));
		sub.setAttribute(Language.trPrimary("Surface.XML.EdgePainter.Width"),""+edgePainterSelected.getWidth());
		sub.setAttribute(Language.trPrimary("Surface.XML.EdgePainter.Type"),""+edgePainterSelected.getType());

		node.appendChild(sub=doc.createElement(Language.trPrimary("Surface.XML.LineMode")));
		switch (edgeLineMode) {
		case DIRECT: sub.setTextContent(Language.trPrimary("Surface.XML.LineMode.Direct")); break;
		case MULTI_LINE: sub.setTextContent(Language.trPrimary("Surface.XML.LineMode.MultiLine")); break;
		case MULTI_LINE_ROUNDED: sub.setTextContent(Language.trPrimary("Surface.XML.LineMode.MultiLineRounded")); break;
		case CUBIC_CURVE: sub.setTextContent(Language.trPrimary("Surface.XML.LineMode.CubicCurve")); break;
		}

		if (timedChecksDelta>0) {
			node.appendChild(sub=doc.createElement(Language.trPrimary("Surface.XML.TimedChecks")));
			sub.setTextContent(""+timedChecksDelta);
		}

		if (recordStationTransitions || recordClientPaths) {
			node.appendChild(sub=doc.createElement(Language.trPrimary("Surface.XML.PathRecording")));
			if (recordStationTransitions) sub.setAttribute(Language.trPrimary("Surface.XML.PathRecording.StationTransitions"),"1");
			if (recordClientPaths) sub.setAttribute(Language.trPrimary("Surface.XML.PathRecording.ClientPaths"),"1");
		}

		if (recordIncompleteClients) {
			node.appendChild(sub=doc.createElement(Language.trPrimary("Surface.XML.RecordIncompleteClients")));
			sub.setTextContent("1");
		}

		if (templates!=null) templates.save(doc,node);

		modelLoadData.addDataToXML(doc,node);

		if (pluginsFolder!=null && !pluginsFolder.trim().isEmpty()) {
			node.appendChild(sub=doc.createElement(Language.trPrimary("Surface.XML.PluginsFolder")));
			sub.setTextContent(pluginsFolder);
		}

		savedViews.addDataToXML(doc,node);
	}

	/**
	 * Gibt an, ob das Modell so gestaltet ist, dass die Simulation auf alle CPU-Kerne verteilt werden kann.
	 * @return	Gibt <code>true</code> zurück, wenn das Modell parallel simuliert werden kann.
	 */
	public boolean allowMultiCore() {
		return getSingleCoreReason().isEmpty();
	}

	/**
	 * Prüft, ob ein bestimmtes Element die Single-Core-only Simulation notwendig macht.
	 * @param element	Element das betrachtet werdne soll
	 * @param reasons	Fügt mögliche Single-Core-Gründe zu dieser Liste hinzu
	 * @see #getSingleCoreReason()
	 */
	private void testElementSingleCoreReason(final ModelElement element, final List<String> reasons) {
		/* Bei begrenzter Anzahl an Kundenankünften in einem Element nicht in den Multi-Core-Modus schalten. */

		if (element instanceof ModelElementSource) {
			ModelElementSourceRecord record=((ModelElementSource)element).getRecord();
			if (record.getMaxArrivalCount()>0) reasons.add(String.format(Language.tr("Surface.SingleCoreReason.FixedArrivalCountAtStation"),element.getId()));
			if (record.getMaxArrivalClientCount()>0) reasons.add(String.format(Language.tr("Surface.SingleCoreReason.FixedArrivalClientCountAtStation"),element.getId()));
		}
		if (element instanceof ModelElementSourceMulti) {
			boolean usesMaxArrival=false;
			boolean usesMaxArrivalClient=false;
			for (ModelElementSourceRecord record : ((ModelElementSourceMulti)element).getRecords()) {
				if (record.getMaxArrivalCount()>0) usesMaxArrival=true;
				if (record.getMaxArrivalClientCount()>0) usesMaxArrivalClient=true;
			}
			if (usesMaxArrival) reasons.add(String.format(Language.tr("Surface.SingleCoreReason.FixedArrivalCountAtStation"),element.getId()));
			if (usesMaxArrivalClient) reasons.add(String.format(Language.tr("Surface.SingleCoreReason.FixedArrivalClientCountAtStation"),element.getId()));
		}
		if (element instanceof ModelElementSourceTable || element instanceof ModelElementSourceDB || element instanceof ModelElementSourceDDE) {
			reasons.add(String.format(Language.tr("Surface.SingleCoreReason.FixedArrivalCountAtStation"),element.getId()));
		}

		/* Animation */

		if (element instanceof ModelElementAnimationConnect) {
			reasons.add(Language.tr("Surface.SingleCoreReason.AnimationMode"));
		}

		/* Ein- und Ausgabe */

		if ((element instanceof ElementNoRemoteSimulation) && ((ElementNoRemoteSimulation)element).inputConnected())  {
			reasons.add(String.format(Language.tr("Surface.SingleCoreReason.OutputElementUsed"),element.getId()));
		}
		if (element instanceof ModelElementRecord) reasons.add(String.format(Language.tr("Surface.SingleCoreReason.OutputElementUsed"),element.getId()));

		/* Eingabe über Anweisung in Script-Element */

		if (element instanceof ElementWithScript) {
			if (((ElementWithScript)element).scriptRequiresSingleCoreMode()) {
				reasons.add(String.format(Language.tr("Surface.SingleCoreReason.ScriptContent"),element.getId()));
			}
		}

		/* Zeitpläne werden verwendet für ein Kundenquellen */

		if (element instanceof ModelElementSource) {
			ModelElementSourceRecord record=((ModelElementSource)element).getRecord();
			if (record.getNextMode()==ModelElementSourceRecord.NextMode.NEXT_SCHEDULE) reasons.add(String.format(Language.tr("Surface.SingleCoreReason.SourceUsesSchedule"),element.getId(),record.getInterarrivalTimeSchedule()));
		}
		if (element instanceof ModelElementSourceMulti) {
			String schedule=null;
			for (ModelElementSourceRecord record: ((ModelElementSourceMulti)element).getRecords()) {
				if (record.getNextMode()==ModelElementSourceRecord.NextMode.NEXT_SCHEDULE) {schedule=record.getInterarrivalTimeSchedule(); break;}
			}
			if (schedule!=null) reasons.add(String.format(Language.tr("Surface.SingleCoreReason.SourceUsesSchedule"),element.getId(),schedule));
		}
	}

	/**
	 * Liefert eine Liste mit Gründen, warum die Multi-Core-Simulation nicht verwendet werden kann.
	 * @return	Liste mit Gründen, die gegen eine parallele Simulation sprechen (oder eine leere Liste, wenn parallel simuliert werden kann).
	 */
	public List<String> getSingleCoreReason() {
		final List<String> reasons=new ArrayList<>();

		/* Eigenschaften der Elemente auf Multi-Core-Verträglichkeit prüfen */
		for (ModelElement element1: surface.getElements()) {
			testElementSingleCoreReason(element1,reasons);
			if (element1 instanceof ModelElementSub) for (ModelElement element2: ((ModelElementSub)element1).getSubSurface().getElements()) {
				testElementSingleCoreReason(element2,reasons);
			}
		}

		/* Abbruchbedingung eingestellt */
		if (useTerminationCondition && !terminationCondition.trim().isEmpty()) {
			reasons.add(Language.tr("Surface.SingleCoreReason.TerminalConditionUsed"));
		}

		/* Abbruchzeitpunkt eingestellt */
		if (useFinishTime) {
			reasons.add(Language.tr("Surface.SingleCoreReason.TerminalTimeUsed"));
		}

		/* Abbruch über Batch-Means-Konfidenzradius */
		if (useFinishConfidence) {
			reasons.add(Language.tr("Surface.SingleCoreReason.TerminalConfidenceUsed"));
		}

		/* Zeitpläne werden verwendet für eine Ressource */
		for (String name: surface.getResources().list()) {
			if (surface.getResources().get(name).getMode()==ModelResource.Mode.MODE_SCHEDULE) reasons.add(String.format(Language.tr("Surface.SingleCoreReason.ResourceUsesSchedule"),name,surface.getResources().get(name).getSchedule()));
		}

		/* Fester Seed */
		if (useFixedSeed) {
			reasons.add(Language.tr("Surface.SingleCoreReason.FixedSeed"));
		}

		/* Laufzeitstatisik */
		if (longRunStatistics.isActive()) {
			reasons.add(Language.tr("Surface.SingleCoreReason.AdditionalStatistics"));
		}

		/* Aufzeichnung individueller Wartezeiten */
		if (collectWaitingTimes) {
			reasons.add(Language.tr("Surface.SingleCoreReason.CollectingWaitingTimes"));
		}

		return reasons;
	}

	/**
	 * Prüft, ob das Modell gefahrlos (=keine Ausgabe in eine Datei) im Hintergrund simuliert werden kann.
	 * @return	Gibt <code>true</code> zurück, wenn das Modell im Hintergrund simuliert werden kann.
	 */
	public boolean canRunInBackground() {
		if (modelLoadData.willChangeModel()) return false;

		for (ModelElement element1: surface.getElements()) {
			if (element1 instanceof ModelElementSourceTable) return false;
			if (element1 instanceof ModelElementSourceDB) return false;
			if (element1 instanceof ModelElementSourceDDE) return false;
			if (element1 instanceof ModelElementInput) return false;
			if (element1 instanceof ModelElementInputDB) return false;
			if (element1 instanceof ModelElementInputDDE) return false;
			if (element1 instanceof ModelElementOutput) return false;
			if (element1 instanceof ModelElementOutputJS) return false;
			if (element1 instanceof ModelElementOutputDB) return false;
			if (element1 instanceof ModelElementOutputDDE) return false;
			if (element1 instanceof ModelElementOutputLog) return false;
			if (element1 instanceof ModelElementDisposeWithTable) return false;
			/*
			if (element1 instanceof ModelElementDecideJS) return false;
			if (element1 instanceof ModelElementHoldJS) return false;
			if (element1 instanceof ModelElementSetJS) return false;
			 */
			if (element1 instanceof ElementWithScript) return !((ElementWithScript)element1).scriptRequiresSingleCoreMode();

			if (element1 instanceof ModelElementSub) for (ModelElement element2: ((ModelElementSub)element1).getSubSurface().getElements()) {
				if (element2 instanceof ModelElementSourceTable) return false;
				if (element2 instanceof ModelElementSourceDB) return false;
				if (element2 instanceof ModelElementSourceDDE) return false;
				if (element2 instanceof ModelElementInput) return false;
				if (element2 instanceof ModelElementInputDB) return false;
				if (element2 instanceof ModelElementInputDDE) return false;
				if (element2 instanceof ModelElementOutput) return false;
				if (element2 instanceof ModelElementOutputJS) return false;
				if (element2 instanceof ModelElementOutputDB) return false;
				if (element2 instanceof ModelElementOutputDDE) return false;
				if (element2 instanceof ModelElementOutputLog) return false;
				if (element2 instanceof ModelElementDisposeWithTable) return false;
				/*
				if (element2 instanceof ModelElementDecideJS) return false;
				if (element2 instanceof ModelElementHoldJS) return false;
				if (element2 instanceof ModelElementSetJS) return false;
				 */
				if (element1 instanceof ElementWithScript) return !((ElementWithScript)element2).scriptRequiresSingleCoreMode();
			}
		}

		return true;
	}

	/**
	 * Liefert einen Grund, warum keine Simulationswiederholung erfolgen kann.
	 * @return	Grund, warum keine Simulationswiederholung erfolgen kann, oder <code>null</code>, wenn nichts gegen eine Wiederholung spricht.
	 */
	public String getNoRepeatReason() {
		/* Animation */
		for (ModelElement element: surface.getElements()) if (element instanceof ModelElementAnimationConnect) {
			return Language.tr("Surface.SingleCoreReason.AnimationMode");
		}

		/* Dateiausgaben */
		for (ModelElement element: surface.getElements()) {
			if ((element instanceof ModelElementOutput) && ((ModelElementOutput)element).inputConnected()) return String.format(Language.tr("Surface.SingleCoreReason.OutputElementUsed"),element.getId());
			if ((element instanceof ModelElementOutputJS) && ((ModelElementOutputJS)element).inputConnected()) return String.format(Language.tr("Surface.SingleCoreReason.OutputElementUsed"),element.getId());
			if ((element instanceof ModelElementOutputDB) && ((ModelElementOutputDB)element).inputConnected()) return String.format(Language.tr("Surface.SingleCoreReason.OutputElementUsed"),element.getId());
			if ((element instanceof ModelElementOutputDDE) && ((ModelElementOutputDDE)element).inputConnected()) return String.format(Language.tr("Surface.SingleCoreReason.OutputElementUsed"),element.getId());
			if ((element instanceof ModelElementOutputLog) && ((ModelElementOutputLog)element).inputConnected()) return String.format(Language.tr("Surface.SingleCoreReason.OutputElementUsed"),element.getId());
			if ((element instanceof ModelElementRecord) && ((ModelElementRecord)element).inputConnected()) return String.format(Language.tr("Surface.SingleCoreReason.OutputElementUsed"),element.getId());
			if ((element instanceof ModelElementDisposeWithTable) && ((ModelElementDisposeWithTable)element).inputConnected()) return String.format(Language.tr("Surface.SingleCoreReason.OutputElementUsed"),element.getId());
		}

		/* Laufzeitstatisik */
		if (longRunStatistics.isActive()) {
			return Language.tr("Surface.SingleCoreReason.AdditionalStatistics");
		}

		/* Messung der Autokorrelation */
		if (correlationMode!=Statistics.CorrelationMode.CORRELATION_MODE_OFF && correlationRange>0) {
			return Language.tr("Surface.SingleCoreReason.Autocorrelation");
		}

		/* Aufzeichnung individueller Wartezeiten */
		if (collectWaitingTimes) {
			return Language.tr("Surface.SingleCoreReason.CollectingWaitingTimes");
		}

		return null;
	}

	/**
	 * Liefert ein Array mit dem im Modell selbst definierten Variablennamen
	 * @return	Array mit im Modell definierten Variablennamen
	 */
	public String[] getModelVariableNames() {
		return globalVariablesNames.toArray(new String[0]);
	}

	/**
	 * Liefert eine Liste mit allen globalen Variablen und ihren Startwerten
	 * @return	Liste mit allen globalen Variablen und ihren Startwerten
	 */
	public Map<String,String> getInitialVariablesWithValues() {
		final Map<String,String> map=new HashMap<>();
		for (int i=0;i<globalVariablesNames.size();i++) map.put(globalVariablesNames.get(i),globalVariablesExpressions.get(i));
		return map;
	}

	/**
	 * Prüft bei allen Elementen, ob die jeweils ggf. genutzten externen Datenquellen vorhanden sind
	 * @return	Ergebnis der Prüfung der externen Datenquellen
	 * @see DataCheckResult
	 */
	public List<DataCheckResult> getDataCheckResults() {
		final List<DataCheckResult> results=new ArrayList<>();

		for (ModelElement element1: surface.getElements()) {
			if (element1 instanceof ModelElementBox) results.add(((ModelElementBox)element1).checkExternalData());
			if (element1 instanceof ModelElementSub) for (ModelElement element2: ((ModelElementSub)element1).getSubSurface().getElements()) {
				if (element2 instanceof ModelElementBox) results.add(((ModelElementBox)element2).checkExternalData());
			}
		}

		if (modelLoadData.willChangeModel()) switch (modelLoadData.getMode()) {
		case DDE: results.add(DataCheckResult.checkDDEAllowEmptyTable(null,modelLoadData.getWorkbook(),modelLoadData.getTable())); break;
		case FILE: results.add(DataCheckResult.checkFile(null,modelLoadData.getWorkbook())); break;
		}

		return results;
	}

	/**
	 * Liefert das zu dem Modell gehörige Nutzervorlagen-Objekt
	 * @return	Nutzervorlagen-Objekt
	 * @see UserTemplates
	 */
	public UserTemplates getTemplates() {
		if (templates==null) templates=new UserTemplates();
		return templates;
	}
}