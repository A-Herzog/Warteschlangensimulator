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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import xml.XMLTools;

/**
 * Prüft die Funktionsweise von {@link XMLTools}
 * @author Alexander Herzog
 * @see XMLTools
 */
public class XMLToolsTest {
	/**
	 * Test: Basistests
	 */
	@Test
	void baseTest() {
		XMLTools xml;

		xml=new XMLTools();
		assertNull(xml.getError());

		xml=new XMLTools((File)null);
		assertNull(xml.getError());

		xml=new XMLTools((InputStream)null);
		assertNull(xml.getError());

		xml=new XMLTools((OutputStream)null);
		assertNull(xml.getError());

		xml=new XMLTools((String)null);
		assertNull(xml.getError());

		xml=new XMLTools((File)null,null);
		assertNull(xml.getError());

		xml=new XMLTools((OutputStream)null,null);
		assertNull(xml.getError());

		xml=new XMLTools((File)null,XMLTools.FileType.AUTO);
		assertNull(xml.getError());
	}

	/**
	 * Test: xml erzeugen und wieder laden
	 */
	@Test
	void loadTest() {
		XMLTools xml;

		xml=new XMLTools();
		assertNull(xml.generateRoot(null));
		assertNull(xml.generateRoot(""));
		assertFalse(xml.save((Element)null));
		assertFalse(xml.save((Document)null));

		try (ByteArrayOutputStream out=new ByteArrayOutputStream()) {
			xml=new XMLTools(out);
			Element root=xml.generateRoot("xmlroot");
			assertNotNull(root);
			assertTrue(xml.save(root));
			assertNull(xml.getError());

			try (ByteArrayInputStream in=new ByteArrayInputStream(out.toByteArray())) {
				xml=new XMLTools(in);
				root=xml.load();
				assertNull(xml.getError());
				assertNotNull(root);
				assertEquals("xmlroot",root.getNodeName());
			}
		} catch (IOException e) {
			assertTrue(false);
		}

		try (ByteArrayOutputStream out=new ByteArrayOutputStream()) {
			xml=new XMLTools(out);
			Element root=xml.generateRoot("xmlroot",true);
			assertNotNull(root);
			assertTrue(xml.save(root));
			assertNull(xml.getError());

			try (ByteArrayInputStream in=new ByteArrayInputStream(out.toByteArray())) {
				xml=new XMLTools(in);
				root=xml.load();
				assertNull(xml.getError());
				assertNotNull(root);
				assertEquals("xmlroot",root.getNodeName());
			}
		} catch (IOException e) {
			assertTrue(false);
		}

		try (ByteArrayOutputStream out=new ByteArrayOutputStream()) {
			xml=new XMLTools(out);
			Element root=xml.generateRoot("xmlroot");
			assertNotNull(root);
			assertTrue(xml.save(root.getOwnerDocument()));
			assertNull(xml.getError());

			try (ByteArrayInputStream in=new ByteArrayInputStream(out.toByteArray())) {
				xml=new XMLTools(in);
				root=xml.load();
				assertNull(xml.getError());
				assertNotNull(root);
				assertEquals("xmlroot",root.getNodeName());
			}
		} catch (IOException e) {
			assertTrue(false);
		}
	}

	/**
	 * Test: Speicherformate
	 */
	@Test
	void saveFormatTest() {
		for (XMLTools.DefaultSaveFormat format: XMLTools.DefaultSaveFormat.values()) {
			assertEquals(format,XMLTools.DefaultSaveFormat.getFormat(format.identifier));
		}

		assertNull(XMLTools.DefaultSaveFormat.getFormat(null));
		assertNull(XMLTools.DefaultSaveFormat.getFormat(""));
		assertNull(XMLTools.DefaultSaveFormat.getFormat("unknownformat"));
	}

	/**
	 * Test: xml &lt;-&gt; json
	 */
	@Test
	void jsonTest() {
		XMLTools xml;
		Element root;
		String json;

		xml=new XMLTools();

		assertEquals("",XMLTools.xmlToJson((Element)null,true,true));
		assertEquals("",XMLTools.xmlToJson((Document)null,true,true));
		assertNull(XMLTools.jsonToXml(null,true));

		root=xml.generateRoot("xmlroot");
		assertNotNull(root);
		assertNull(xml.getError());
		json=XMLTools.xmlToJson(root,false,true);
		assertNotNull(json);
		root=XMLTools.jsonToXml(json,true);
		assertNotNull(root);
		assertEquals("xmlroot",root.getNodeName());

		root=xml.generateRoot("xmlroot");
		assertNotNull(root);
		assertNull(xml.getError());
		json=XMLTools.xmlToJson(root,false,false);
		assertNotNull(json);
		root=XMLTools.jsonToXml(json,false);
		assertNotNull(root);
		assertEquals("xmlroot",root.getNodeName());

		root=xml.generateRoot("xmlroot");
		assertNotNull(root);
		assertNull(xml.getError());
		json=XMLTools.xmlToJson(root.getOwnerDocument(),false,true);
		assertNotNull(json);
		root=XMLTools.jsonToXml(json,true);
		assertNotNull(root);
		assertEquals("xmlroot",root.getNodeName());

		root=xml.generateRoot("xmlroot");
		assertNotNull(root);
		assertNull(xml.getError());
		json=XMLTools.xmlToJson(root.getOwnerDocument(),false,false);
		assertNotNull(json);
		root=XMLTools.jsonToXml(json,false);
		assertNotNull(root);
		assertEquals("xmlroot",root.getNodeName());
	}

	/**
	 * Test: Base64-Codierung
	 */
	@Test
	void base64Test() {
		final XMLTools xml=new XMLTools();
		final Element root=xml.generateRoot("xmlroot");
		final String result=xml.getBase64xml(root);
		assertNotNull(result);
		assertTrue(!result.isEmpty());
	}
}