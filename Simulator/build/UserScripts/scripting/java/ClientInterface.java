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

import java.util.Map;

/**
 * Interface for accessing client specific data.
 * @author Alexander Herzog
 */
public interface ClientInterface {
	/**
	 * Calculates an expression in the simulation and the client context.
	 * @param expression	Expression to be calculated
	 * @return	Returns a {@link Double} object if successful. In case of error an error message.
	 */
	Object calc(final String expression);

	/**
	 * Return true of false depending if the current client was generated during the warm-up phase and
	 * therefore will not be recorded in the statistics.
	 * @return	Return <code>true</code> if the current client was generated during the warm-up phase
	 */
	boolean isWarmUp();

	/**
	 * Returns true of false depending if the current client is to be recorded in the statistics.
	 * This value is independent of the warm-up phase. A client will only be recorded if he was
	 * generated after the warm-up phase and this value is true.
	 * @return	Recording the client in the statistics
	 */
	boolean isInStatistics();

	/**
	 * Sets if a client is to be recorded in the statistics.
	 * This value is independent of the warm-up phase. A client will only be recorded if he was
	 * generated after the warm-up phase and this value is not set to false.

	 * @param inStatistics	Erfassung des Kunden in der Statistik
	 */
	void setInStatistics(final boolean inStatistics);

	/**
	 * Returns the 1 based consecutive number of the current client.
	 * When using multiple simulation threads this number is thread local.
	 * @return	Consecutive number of the current client
	 */
	long getNumber();

	/**
	 * Returns the name of the type of the client who has triggered the processing of the script.
	 * @return	Name of the client type
	 */
	String getTypeName();
	
	/**
	 * Returns the ID of the station where the current client was created
	 * or where it was assigned its current type.
	 * @return	ID of the station
	 */
	int getSourceStationID();

	/**
	 * Returns the current waiting time of the client who has triggered the processing of the script as a seconds numerical value.
	 * @return Waiting time of the client
	 * @see ClientInterface#getWaitingTime()
	 */
	double getWaitingSeconds();

	/**
	 * Returns the current waiting time of the client who has triggered the processing of the script as a formated time value as a string.
	 * @return Waiting time of the client
	 * @see ClientInterface#getWaitingSeconds()
	 */
	String getWaitingTime();

	/**
	 * Sets the current waiting time of the client who has triggered the processing of the script.
	 * @param seconds	Waiting time of the client
	 * @see ClientInterface#getWaitingSeconds()
	 */
	void setWaitingSeconds(final double seconds);

	/**
	 * Returns the current transfer time of the client who has triggered the processing of the script as a seconds numerical value.
	 * @return Transfer time of the client
	 * @see ClientInterface#getTransferTime()
	 */
	double getTransferSeconds();

	/**
	 * Returns the current transfer time of the client who has triggered the processing of the script as a formated time value as a string.
	 * @return Transfer time of the client
	 * @see ClientInterface#getTransferSeconds()
	 */
	String getTransferTime();

	/**
	 * Sets the current transfer time of the client who has triggered the processing of the script.
	 * @param seconds	Transfer time of the client
	 * @see ClientInterface#getTransferSeconds()
	 */
	void setTransferSeconds(final double seconds);

	/**
	 * Returns the current service time of the client who has triggered the processing of the script as a seconds numerical value.
	 * @return Bisherige Service time of the client
	 * @see ClientInterface#getProcessTime()
	 */
	double getProcessSeconds();

	/**
	 * Returns the current service time of the client who has triggered the processing of the script as a formated time value as a string.
	 * @return Bisherige Service time of the client
	 * @see ClientInterface#getProcessSeconds()
	 */
	String getProcessTime();

	/**
	 * Sets the current service time of the client who has triggered the processing of the script.
	 * @param seconds	Service time of the client
	 * @see ClientInterface#getProcessSeconds()
	 */
	void setProcessSeconds(final double seconds);

	/**
	 * Returns the current residence time of the client who has triggered the processing of the script as a seconds numerical value.
	 * @return Residence time of the client
	 * @see ClientInterface#getResidenceTime()
	 */
	double getResidenceSeconds();

	/**
	 * Returns the current residence time of the client who has triggered the processing of the script as a formated time value as a string.
	 * @return Residence time of the client
	 * @see ClientInterface#getResidenceSeconds()
	 */
	String getResidenceTime();

	/**
	 * Sets the current residence time of the client who has triggered the processing of the script.
	 * @param seconds	Residence time of the client
	 * @see ClientInterface#getResidenceSeconds()
	 */
	void setResidenceSeconds(final double seconds);

	/**
	 * Return for the current client the numerical value which is stored by the index <tt>index</tt>.
	 * @param index	Index for which the numerical value should be returned
	 * @return	Numerical value for the index. (If no value is set for the index 0.0 will be returned.)
	 */
	double getValue(final int index);

	/**
	 * Sets for the current client the <tt>value</tt> for the index <tt>index</tt>.
	 * @param index	Index for which the numerical value should be set
	 * @param value	New numerical value for the index for the client
	 */
	void setValue(final int index, final int value);

	/**
	 * Sets for the current client the <tt>value</tt> for the index <tt>index</tt>.
	 * @param index	Index for which the numerical value should be set
	 * @param value	New numerical value for the index for the client
	 */
	void setValue(final int index, final double value);

	/**
	 * Sets for the current client the <tt>value</tt> for the index <tt>index</tt>.
	 * @param index	Index for which the numerical value should be set
	 * @param value	New numerical value for the index for the client (the string will be parsed by <tt>calc</tt>)
	 */
	void setValue(final int index, final String value);

	/**
	 * Returns for the current client the string which is stored by the key <tt>key</tt>.
	 * @param key	Key for which the value is requested
	 * @return	Value for the key for the client (if no value is set for the key an empty string will be returned)
	 */
	String getText(final String key);

	/**
	 * Sets for the current client string <tt>value</tt> for the key <tt>key</tt>.
	 * @param key	Key for which the value is to be set
	 * @param value	New text value for the key for the current client
	 */
	void setText(final String key, final String value);

	/**
	 * Returns the number of clients that are in the temporary batch.  
	 * If the current client is not a temporary batch, the function returns 0.
	 * @return	Number of client in the batch or 0 file the current client is not a temporary batch
	 */
	int batchSize();

	/**
	 * Return all numerical values stored for the current client.
	 * @return	All numerical values stored for the current client
	 */
	double[] getAllValues();

	/**
	 * Return all text values stored for the current client.
	 * @return	All text values stored for the current client
	 */
	Map<String,String> getAllTexts();

	/**
	 * Returns the name of one of the clients in the current batch.
	 * @param	batchIndex 0-based Index of the client in the batch (valid values are in the range from 0 to {@link #batchSize()}-1)
	 * @return	Name of the client or <code>null</code>, if the index is invalid
	 */
	String getBatchTypeName(final int batchIndex);

	/**
	 * Returns the previous waiting time of one of the clients in the current batch in seconds as a numerical value.
	 * @param	batchIndex 0-based Index of the client in the batch (valid values are in the range from 0 to {@link #batchSize()}-1)
	 * @return Waiting time of the client
	 * @see ClientInterface#getBatchWaitingTime(int)
	 */
	double getBatchWaitingSeconds(final int batchIndex);

	/**
	 * Returns the previous waiting time of one of the clients in the current batch in formatted form as a string.
	 * @param	batchIndex 0-based Index of the client in the batch (valid values are in the range from 0 to {@link #batchSize()}-1)
	 * @return Waiting time of the client
	 * @see ClientInterface#getBatchWaitingSeconds(int)
	 */
	String getBatchWaitingTime(final int batchIndex);

	/**
	 * Returns the previous transfer time of one of the clients in the current batch in seconds as a numerical value.
	 * @param	batchIndex 0-based Index of the client in the batch (valid values are in the range from 0 to {@link #batchSize()}-1)
	 * @return Transfer time of the client
	 * @see ClientInterface#getBatchTransferTime(int)
	 */
	double getBatchTransferSeconds(final int batchIndex);

	/**
	 * Returns the previous transfer time of one of the clients in the current batch in formatted form as a string.
	 * @param	batchIndex 0-based Index of the client in the batch (valid values are in the range from 0 to {@link #batchSize()}-1)
	 * @return Transfer time of the client
	 * @see ClientInterface#getBatchTransferSeconds(int)
	 */
	String getBatchTransferTime(final int batchIndex);

	/**
	 * Returns the previous service time of one of the clients in the current batch in seconds as a numerical value.
	 * @param	batchIndex 0-based Index of the client in the batch (valid values are in the range from 0 to {@link #batchSize()}-1)
	 * @return Service time of the client
	 * @see ClientInterface#getBatchProcessTime(int)
	 */
	double getBatchProcessSeconds(final int batchIndex);

	/**
	 * Returns the previous service time of one of the clients in the current batch in formatted form as a string.
	 * @param	batchIndex 0-based Index of the client in the batch (valid values are in the range from 0 to {@link #batchSize()}-1)
	 * @return Service time of the client
	 * @see ClientInterface#getBatchProcessSeconds(int)
	 */
	String getBatchProcessTime(final int batchIndex);

	/**
	 * Returns the previous residence time of one of the clients in the current batch in seconds as a numerical value.
	 * @param	batchIndex 0-based Index of the client in the batch (valid values are in the range from 0 to {@link #batchSize()}-1)
	 * @return Residence time of the client
	 * @see ClientInterface#getBatchResidenceTime(int)
	 */
	double getBatchResidenceSeconds(final int batchIndex);

	/**
	 * Returns the previous residence time of one of the clients in the current batch in formatted form as a string.
	 * @param	batchIndex 0-based Index of the client in the batch (valid values are in the range from 0 to {@link #batchSize()}-1)
	 * @return Residence time of the client
	 * @see ClientInterface#getBatchResidenceSeconds(int)
	 */
	String getBatchResidenceTime(final int batchIndex);

	/**
	 * Returns a stored numerical value for one of the clients in the current batch.
	 * @param	batchIndex 0-based Index of the client in the batch (valid values are in the range from 0 to {@link #batchSize()}-1)
	 * @param index	Index for which the numerical value is to be returned
	 * @return	Numerical value for the index and the client. (If no value is set for the index, 0.0 will be returned.)
	 */
	double getBatchValue(final int batchIndex, final int index);

	/**
	 * Returns a stored text value for one of the clients in the current batch.
	 * @param	batchIndex 0-based Index of the client in the batch (valid values are in the range from 0 to {@link #batchSize()}-1)
	 * @param key	Key for which the value is to be returned
	 * @return	Value for the key for the client. (If no value is set for the key, an empty string will be returned.)
	 */
	String getBatchText(final int batchIndex, final String key);
}
