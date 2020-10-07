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
package xmltests;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import xml.XMLData;
import xml.XMLTools;

/**
 * Prüft die Funktionsweise von {@link XMLData}
 * @author Alexander Herzog
 * @see XMLData
 */
class XMLDataTest {
	/**
	 * Erzeugt ein xml-Daten-Objekt an dem im Folgenden die Tests durchgeführt werden können
	 * @return	xml-Daten-Objekt
	 */
	private XMLData getTestData() {
		return new XMLData() {
			@Override
			protected String loadProperty(String name, String text, Element node) {return null;}
			@Override public String[] getRootNodeNames() {return new String[]{"xmlroot"};}
			@Override protected void addDataToXML(Document doc, Element node, boolean isPartOfOtherFile, File file) {}
		};
	}

	/**
	 * Test: Basistests
	 */
	@Test
	void baseTest() {
		try (ByteArrayOutputStream out=new ByteArrayOutputStream()) {
			XMLData data=getTestData();
			assertTrue(data.saveToStream(out));
			try (ByteArrayInputStream in=new ByteArrayInputStream(out.toByteArray())) {
				data=getTestData();
				assertNull(data.loadFromStream(in));
			}
		} catch (IOException e) {
			assertTrue(false);
		}

		XMLData data=getTestData();
		assertFalse(data.saveToFile(null));
		assertFalse(data.saveToFile(null,XMLTools.FileType.AUTO));
		assertFalse(data.saveToStream(null));
		assertNotNull(data.loadFromFile(null));
		assertNotNull(data.loadFromFile(null,XMLTools.FileType.AUTO));
		assertNotNull(data.loadFromStream(null));
		assertNotNull(data.loadFromString(null));
	}
}
