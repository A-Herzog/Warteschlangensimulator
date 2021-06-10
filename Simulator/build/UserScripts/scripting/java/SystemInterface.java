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
 * Interface for accessing simulation dependent data.
 * @author Alexander Herzog
 */
public interface SystemInterface {
	/**
	 * Calculates an expression in the simulation context.
	 * @param expression	Expression to be calculated
	 * @return	Returns a {@link Double} object if successful. In case of error an error message.
	 */
	Object calc(final String expression);

	/**
	 * Returns the current time in the simulation (in seconds)
	 * @return	Current time in the simulation (in seconds)
	 */
	double getTime();

	/**
	 * Returns if the simulation is still in warm-up phasis.
	 * @return	Returns <code>true</code>, if the simulation is still in warm-up phasis
	 */
	boolean isWarmUp();

	/**
	 * Returns the number of clients at a station.
	 * @param id	ID of the station
	 * @return	Number of clients at the station
	 */
	int getWIP(final int id);

	/**
	 * Returns the number of clients in the queue at a station.
	 * @param id	ID of the station
	 * @return	Number of clients in the queue at the station
	 */
	int getNQ(final int id);

	/**
	 * Returns the number of clients at all stations together.
	 * @return	Number of clients at all stations together
	 */
	int getWIP();

	/**
	 * Returns the number of clients in the queues at all stations together.
	 * @return	Number of clients in the queues at all stations together
	 */
	int getNQ();

	/**
	 * Sets the value of a simulation variable. The variable must already exist, otherwise there will be no assignment.
	 * @param varName	Name of the variable
	 * @param varValue	new value (Integer, Double or String, which is then first interpreted)
	 */
	void set(final String varName, final Object varValue);

	/**
	 * Set the value at a "Analog value" or "Tank" station.
	 * @param elementID	ID of the station where the value is to be set
	 * @param value	New value (Number of expression to be evaluated)
	 */
	void setAnalogValue(final Object elementID, final Object value);

	/**
	 * Set the change rate at a  "Analog value" station.
	 * @param elementID	ID of the station where the change rate is to be set
	 * @param value	New value (Number of expression to be evaluated)
	 */
	void setAnalogRate(final Object elementID, final Object value);

	/**
	 * Sets the maximum flow for a valve at a tank
	 * @param elementID	ID of the tank for which the maximum flow is to be set for a valve
	 * @param valveNr	1-based number of the valve
	 * @param value	New value (Number of expression to be evaluated)
	 */
	void setAnalogValveMaxFlow(final Object elementID, final Object valveNr, final Object value);

	/**
	 * Returns the number of operators in all operator groups together.
	 * @return	Number of operators in all operator groups together
	 */
	int getAllResourceCount();

	/**
	 * Returns the number of operators in an operator group.
	 * @param resourceId	1-based ID of the operator group
	 * @return	Number of operators in the operator group
	 */
	int getResourceCount(final int resourceId);

	/**
	 * Sets the number of available operators in an operator group.
	 * @param resourceId	1-based ID of the operator group
	 * @param count	Number of available operators
	 * @return	Returns <code>true</code>, if the number could be changed.
	 */
	boolean setResourceCount(final int resourceId, final int count);

	/**
	 * Returns how many operators in an operator group are current in down time.
	 * @param resourceId	1-based ID of the operator group
	 * @return	Number of operators
	 */
	int getResourceDown(final int resourceId);

	/**
	 * Returns how many operators in all operator groups together are current in down time.
	 * @return	Number of operators
	 */
	int getAllResourceDown();

	/**
	 * Triggers a signal.
	 * @param signalName	Name of the signal
	 */
	void signal(final String signalName);

	/**
	 * Call a method in an external class file which is located in the plugins folder.
	 * @param className	Name of the class file (without file extension), this means name of the class
	 * @param functionName	Name of the methode to be called inside the class
	 * @param data	Additional data which will be passed as a parameter to the method to be called
	 * @return	Return value of the methode
	 */
	Object runPlugin(final String className, final String functionName, final Object data);
	
	/**
	 * Records a message in the logging output.
	 * @param obj	Message to be logged
	 */
	void log(final Object obj);
	
	/**
	 * Returns the list of all clients at a delay station.
	 * @param id	ID of the delay station
	 * @return	List of all clients at the station or <code>null</code>, if no clients list is available for this id
	 */
	ClientsInterface getDelayStationData(final int id);

	/**
	 * Returns a station local data object for script data.
	 * @return	Station local data object for script data
	 */
	Map<String,Object> getMapLocal();

	/**
	 * Returns the global data object for script data.
	 * @return	Global data object for script data
	 */
	Map<String,Object> getMapGlobal();

	/**
	 * Terminates the simulation.
	 * @param message	Optional error message. If <code>null</code> is passed, the simulation will be terminated without error.
	 */
	void terminateSimulation(final String message);
}