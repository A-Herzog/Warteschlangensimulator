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
package net.web;

/**
 * Diese Klasse stellt statische Methoden zur Verarbeitung von Webserver-Anfragen bereit.
 * @author Alexander Herzog
 */
public class WebServerTools {
	/**
	 * Konstruktor der Klasse<br>
	 * Diese Klasse kann nicht instanziert werden. Sie stellt nur statische Hilfsmethoden zur Verfügung.
	 */
	private WebServerTools() {}

	/**
	 * Erstellt eine html-Seite.
	 * @param title	Titel der Seite
	 * @param content	Inhalt der Seite (wird innerhalb des body-Tags ausgegeben)
	 * @return	Vollständige html-Seite inkl. Kopfbereich usw.
	 */
	public static String buildPage(final String title, final String content) {
		final StringBuilder sb=new StringBuilder();

		sb.append("<!DOCTYPE html>\n");
		sb.append("<html><head>\n");
		sb.append("  <meta charset=\"utf-8\">\n");
		sb.append("  <title>"+title+"</title>\n");
		sb.append("  <link rel=\"stylesheet\" type=\"text/css\" href=\"css.css\">\n");
		sb.append("</head><body>\n");
		sb.append("<div class=\"title noprint\" id=\"title\">\n");
		/* if (info!=null && !info.isEmpty()) sb.append("  <a style=\"float: right;\" class=\"toolbaricon\" href=\"javascript:alert('"+info+"\\n(c) Alexander Herzog');\" title=\""+Language.tr("Server.WebMenu.ProgramInfo")+"\"><div class=\"toolbaricon-any toolbaricon-info\">&nbsp;</div></a>\n"); */
		sb.append("  "+title+"\n");
		sb.append("</div>\n");
		sb.append(content);
		sb.append("</body></html>\n");
		return sb.toString();
	}
}
