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
package gitconnect;

import java.awt.Component;
import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.SwingUtilities;

import language.Language;
import systemtools.MsgBox;
import tools.SetupData;
import ui.MainFrame;

/**
 * Diese Klasse stellt statische Hilfsroutinen zur Verfügung
 * um aus dem Simulator heraus möglichst bequem Programmstart-Pulls
 * sowie Commit/Push-Funktionen beim Speichern von Dateien
 * durchführen zu können.
 * @author Alexander Herzog
 */
public class GitTools {
	/**
	 * Konstruktor der Klasse<br>
	 * Diese Klasse stellt nur statische Hilfsroutinen
	 * zur Verfügung und kann nicht instanziert werden.
	 */
	private GitTools() {
	}

	/**
	 * Führt die Pull-Requests für die Repositories aus,
	 * die beim Programmstart abgerufen werden sollen.
	 * @param owner	Übergeordnetes Element zur Ausrichtung von Fehlermeldungen
	 */
	public static void startUpPull(final Component owner) {
		final List<GitSetup> gitSetups=SetupData.getSetup().gitSetups.stream().filter(git->git.useServer && git.pullOnStart).map(git->new GitSetup(git)).collect(Collectors.toList());
		if (gitSetups.size()>0) new Thread(()->startUpPullInner(owner,gitSetups),"StartUpGitPull").start();
	}

	/**
	 * Wandelt die Zeichen "&amp;", "&lt;" und "&gt;" in ihre entsprechenden
	 * HTML-Entitäten um.
	 * @param line	Umzuwandelnder Text
	 * @return	Umgewandelter Text
	 */
	private static String encodeHTMLentities(final String line) {
		if (line==null) return "";
		String result;
		result=line.replaceAll("&","&amp;");
		result=result.replaceAll("<","&lt;");
		result=result.replaceAll(">","&gt;");
		return result;
	}

	/**
	 * Interne Verarbeitung der Pull-Requests in einem eigenen Thread aus.
	 * @param owner	Übergeordnetes Element zur Ausrichtung von Fehlermeldungen
	 * @param gitSetups	Repositories die tatsächlich abgerufen werden sollen
	 * @see #startUpPull(Component)
	 */
	private static void startUpPullInner(final Component owner, final List<GitSetup> gitSetups) {
		final StringBuilder errors=new StringBuilder();

		for (GitSetup gitSetup: gitSetups) {
			final String error=gitSetup.doPull(null);
			if (error!=null) {
				errors.append("<ul>");
				errors.append("<li><b>"+encodeHTMLentities(gitSetup.serverURL)+" -&gt; "+encodeHTMLentities(gitSetup.localFolder)+"</b><br>"+encodeHTMLentities(error)+"</li>");
			}
		}

		if (errors.length()>0) {
			errors.append("</ul>");
			SwingUtilities.invokeLater(()->showPullErrors(owner,errors.toString()));
		}
	}

	/**
	 * Zeigt im Hauptthread eine Liste der Fehlermeldung, die beim initialen Pull aufgetreten sind, an.
	 * @param owner	Übergeordnetes Element zur Ausrichtung von Fehlermeldungen
	 * @param errorList	Liste mit Fehlermeldungen (ggf. mehrere Meldungen durch Zeilenumbrüche getrennt)
	 * @see #startUpPullInner(Component, List)
	 */
	private static void showPullErrors(final Component owner, final String errorList) {
		final StringBuilder message=new StringBuilder();

		message.append("<html><body>");
		message.append(Language.tr("Git.System.InitialPullError.Info"));
		message.append(errorList);
		message.append("</body></html>");

		MsgBox.error(owner,Language.tr("Git.System.InitialPullError.Title"),message.toString());
	}

	/**
	 * Erstellt die Commit-Nachricht für eine Datei.
	 * @param file	Neue bzw. geänderte Datei
	 * @param fileType	Art der Datei
	 * @return	Commit-Nachricht
	 */
	private static String buildCommitMessage(final File file, final GitSetup.GitSaveMode fileType) {
		final String fileTypeString;
		switch (fileType) {
		case MODELS: fileTypeString=Language.tr("Git.System.Commit.Type.Model"); break;
		case PARAMETER_SERIES: fileTypeString=Language.tr("Git.System.Commit.Type.ParameterSeries"); break;
		case OPTIMIZATION_SETUPS: fileTypeString=Language.tr("Git.System.Commit.Type.OptimizationSetup"); break;
		case STATISTICS: fileTypeString=Language.tr("Git.System.Commit.Type.StatisticsResults"); break;
		default: fileTypeString=""; break;
		}

		String message=file.getName()+" ("+MainFrame.PROGRAM_NAME+" - "+fileTypeString+")";

		if (message.length()>70) message=message.substring(0,67)+"...";

		return message;
	}

	/**
	 * Prüft, ob sich eine Datei in einem Git-Repository-Ordner befindet und führt ggf. Git-Verarbeitungen durch.
	 * @param owner	Übergeordnetes Element zur Ausrichtung von Fehlermeldungen
	 * @param authorName	Autorenname für die Commit-Nachricht
	 * @param authorEMail	Autoren-E-Mail-Adresse für die Commit-Nachricht
	 * @param file	Neue bzw. geänderte Datei
	 * @param fileType	Art der Datei
	 */
	public static void saveFile(final Component owner, final String authorName, final String authorEMail, final File file, final GitSetup.GitSaveMode fileType) {
		final List<GitSetup> gitSetups=SetupData.getSetup().gitSetups.stream().filter(git->git.saveData.contains(fileType) && git.isFileInRepositoryFolder(file)).map(git->new GitSetup(git)).collect(Collectors.toList());
		if (gitSetups.size()>0) new Thread(()->saveFileInner(owner,gitSetups,authorName,authorEMail,file,fileType),"BackgroundGitSave").start();
	}

	/**
	 * Interne Verarbeitung der Commit- und Push-Aktionen.
	 * @param owner	Übergeordnetes Element zur Ausrichtung von Fehlermeldungen
	 * @param gitSetups	Repositories die tatsächlich verarbeitet werden sollen
	 * @param authorName	Autorenname für die Commit-Nachricht
	 * @param authorEMail	Autoren-E-Mail-Adresse für die Commit-Nachricht
	 * @param file	Neue bzw. geänderte Datei
	 * @param fileType	Art der Datei
	 */
	private static void saveFileInner(final Component owner, final List<GitSetup> gitSetups, final String authorName, final String authorEMail, final File file, final GitSetup.GitSaveMode fileType) {
		for (GitSetup gitSetup: gitSetups) {
			final String error=gitSetup.doPush(new File[] {file},authorName,authorEMail,buildCommitMessage(file,fileType),null);
			if (error!=null) MsgBox.error(owner,Language.tr("Git.System.AutoPushError.Title"),String.format(Language.tr("Git.System.AutoPushError.Info"),gitSetup.localFolder,error));
		}
	}
}
