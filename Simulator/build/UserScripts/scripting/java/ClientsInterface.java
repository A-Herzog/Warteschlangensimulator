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
package scripting.java;

/**
 * Interface for accessing a list of clients.
 * @author Alexander Herzog
 */
public interface ClientsInterface {
	/**
	 * Returns the number of clients in the list.
	 * @return	Number of clients in the list
	 */
	int count();

	/**
	 * Releases a client.
	 * @param index	0-based index of the client to release
	 */
	void release(final int index);

	/**
	 * Returns the client type of a client.
	 * @param index	0-based index of the client
	 * @return	Type name of the client 
	 */
	String clientTypeName(final int index);

	/**
	 * Returns the content of a client data field.
	 * @param index	0-based index of the client
	 * @param data	Index of the client data field
	 * @return	Content of the client data field
	 */
	double clientData(final int index, final int data);

	/**
	 * Sets the content of a client data field.
	 * @param index	0-based index of the client
	 * @param data	Index of the client data field
	 * @param value	New value of the client data field
	 */
	void clientData(final int index, final int data, final double value);

	/**
	 * Returns the content of a text-based client data field.
	 * @param index	0-based index of the client
	 * @param key	Key of the text-based client data field
	 * @return	Content of the text-based client data field
	 */
	String clientTextData(final int index, final String key);

	/**
	 * Sets the content of a text-based client data field.
	 * @param index	0-based index of the client
	 * @param key	Key of the text-based client data field
	 * @param value	New content of the text-based client data field
	 */
	void clientTextData(final int index, final String key, final String value);

	/**
	 * Returns the waiting time of a client as a seconds value.
	 * @param index	0-based index of the client
	 * @return Waiting time of the client
	 * @see ClientsInterface#clientWaitingTime(int)
	 */
	double clientWaitingSeconds(final int index);

	/**
	 * Returns the waiting time of a client as a formated string.
	 * @param index	0-based index of the client
	 * @return Waiting time of the client
	 * @see ClientsInterface#clientWaitingSeconds(int)
	 */
	String clientWaitingTime(final int index);
	
	/**
	 * Sets the waiting time of a client as a seconds value.
	 * @param index	0-based index of the client
	 * @param time	Waiting time of the client
	 * @see ClientsInterface#clientWaitingSeconds(int)
	 */
	void clientWaitingSecondsSet(final int index, final double time);	

	/**
	 * Returns the transfer time of a client as a seconds value.
	 * @param index	0-based index of the client
	 * @return Transfer time of the client
	 * @see ClientsInterface#clientTransferTime(int)
	 */
	double clientTransferSeconds(final int index);

	/**
	 * Returns the transfer time of a client as a formated string.
	 * @param index	0-based index of the client
	 * @return Transfer time of the client
	 * @see ClientsInterface#clientTransferSeconds(int)
	 */
	String clientTransferTime(final int index);

	/**
	 * Sets the transfer time of a client as a seconds value.
	 * @param index	0-based index of the client
	 * @param time	Transfer time of the client
	 * @see ClientsInterface#clientTransferSeconds(int)
	 */
	void clientTransferSecondsSet(final int index, final double time);
	
	/**
	 * Returns the process time of a client as a seconds value.
	 * @param index	0-based index of the client
	 * @return Process time of the client
	 * @see ClientsInterface#clientProcessTime(int)
	 */
	double clientProcessSeconds(final int index);

	/**
	 * Returns the process time of a client as a formated string.
	 * @param index	0-based index of the client
	 * @return Process time of the client
	 * @see ClientsInterface#clientProcessSeconds(int)
	 */
	String clientProcessTime(final int index);

	/**
	 * Sets the process time of a client as a seconds value.
	 * @param index	0-based index of the client
	 * @param time	Process time of the client
	 * @see ClientsInterface#clientProcessSeconds(int)
	 */
	void clientProcessSecondsSet(final int index, final double time);
	
	/**
	 * Returns the residence time of a client as a seconds value.
	 * @param index	0-based index of the client
	 * @return Residence time of the client
	 * @see ClientsInterface#clientResidenceTime(int)
	 */
	double clientResidenceSeconds(final int index);

	/**
	 * Returns the residence time of a client as a formated string.
	 * @param index	0-based index of the client
	 * @return Residence time of the client
	 * @see ClientsInterface#clientResidenceSeconds(int)
	 */
	String clientResidenceTime(final int index);
	
	/**
	 * Sets the residence time of a client as a seconds value.
	 * @param index	0-based index of the client
	 * @param time	Residence time of the client
	 * @see ClientsInterface#clientResidenceSeconds(int)
	 */
	void clientResidenceSecondsSet(final int index, final double time);	
}