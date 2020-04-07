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

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.junit.jupiter.api.Test;

import xml.ChiperTools;

/**
 * Prüft die Funktionsweise von {@link ChiperTools}
 * @author Alexander Herzog
 * @see ChiperTools
 */
public class ChiperToolsTest {
	private final byte[] data="Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet. Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet.".getBytes();

	/**
	 * Test: Ver- und Entschlüsselung eines Byte-Arrays
	 */
	@Test
	void testArray() {

		final byte[] encrypted=ChiperTools.encrypt(data,"password");
		assertNotNull(encrypted);

		final byte[] decrypted1=ChiperTools.decrypt(encrypted,"password");
		assertNotNull(decrypted1);
		assertArrayEquals(data,decrypted1);

		final byte[] decrypted2=ChiperTools.decrypt(encrypted,"wrong_password");
		assertNull(decrypted2);
	}

	/**
	 * Test: Ver- und Entschlüsselung eines Streams
	 */
	@Test
	void testStream() {
		final ByteArrayOutputStream streamData=new ByteArrayOutputStream();
		try {
			streamData.write(data);
		} catch (IOException e) {
			assertTrue(false);
		}

		final ByteArrayOutputStream streamEncrypted=ChiperTools.encrypt(streamData,"password");
		assertNotNull(streamEncrypted);
		assertTrue(streamEncrypted.size()>0);

		final ByteArrayOutputStream streamDecrypted1=ChiperTools.decrypt(streamEncrypted,"password");
		assertNotNull(streamDecrypted1);
		assertTrue(streamDecrypted1.size()>0);
		assertArrayEquals(data,streamDecrypted1.toByteArray());

		final ByteArrayOutputStream streamDecrypted2=ChiperTools.decrypt(streamEncrypted,"wrong_password");
		assertNull(streamDecrypted2);
	}
}
