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
package tools;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Interpretiert eine über api.github.com geladene Verzeichnisinhaltsliste
 * @author Alexander Herzog
 */
public class URLLoaderGitHubFolder {
	/**
	 * Zu interpretierende JSON-Daten
	 */
	private String data;

	/**
	 * Konstruktor der Klasse
	 * @param data	Zu interpretierende JSON-Daten
	 */
	public URLLoaderGitHubFolder(final byte[] data) {
		this(new String(data,StandardCharsets.UTF_8));
	}

	/**
	 * Konstruktor der Klasse
	 * @param data	Zu interpretierende JSON-Daten
	 */
	public URLLoaderGitHubFolder(final String data) {
		this.data=data;
	}

	/**
	 * Führt die Verarbeitung durch.
	 * @param user	Repository-User
	 * @param repo	Repository Name
	 * @param branch	Zweig im Repository
	 * @return	Liefert im Erfolgsfall eine Liste mit Verzeichniseinträgen
	 */
	public List<FileRecord> process(final String user, final String repo, final String branch) {
		if (data==null) return null;

		try {
			final JSONArray jsonArray=new JSONArray(data);
			final int size=jsonArray.length();
			if (size==0) return null;
			final List<FileRecord> records=new ArrayList<>();
			for (int i=0;i<size;i++) {
				final Object obj=jsonArray.get(i);
				if (obj instanceof JSONObject) {
					final JSONObject jsonObject=(JSONObject)obj;
					final Object objName=jsonObject.get("name");
					final Object objSize=jsonObject.get("size");
					final Object objURL=jsonObject.get("download_url");
					if ((objName instanceof String) && (objSize instanceof Integer) && (objURL instanceof String)) {
						records.add(new FileRecord((String)objName,(Integer)objSize,(String)objURL,user,repo,branch));
					}
				}
			}
			if (records.size()==0) return null;
			return records;
		} catch (JSONException e) {
			return null;
		}

	}

	/**
	 * Ein Objekt dieser Klasse repräsentiert einen
	 * einzelnen Verzeichniseintrag.
	 * @see URLLoaderGitHubFolder#process(String, String, String)
	 */
	public static class FileRecord {
		/**
		 * Dateiname
		 */
		public final String name;

		/**
		 * Größe (in Bytes)
		 */
		public final int size;

		/**
		 * Downloadadresse
		 */
		public final String url;

		/**
		 * Repository-User
		 */
		public final String user;

		/**
		 * Repository Name
		 */
		public final String repo;

		/**
		 * Zweig im Repository
		 */
		public final String branch;

		/**
		 * Konstruktor der Klasse
		 * @param name	Dateiname
		 * @param size	Größe (in Bytes)
		 * @param url	Downloadadresse
		 * @param user	Repository-User
		 * @param repo	Repository Name
		 * @param branch	Zweig im Repository
		 */
		public FileRecord(final String name, final int size, final String url, final String user, final String repo, final String branch) {
			this.name=name;
			this.size=size;
			this.url=url;
			this.user=user;
			this.repo=repo;
			this.branch=branch;
		}
	}
}