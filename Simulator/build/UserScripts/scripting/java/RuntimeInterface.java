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
 * Interface for accessing simulation independent data.
 * @author Alexander Herzog
  */
public interface RuntimeInterface {
	/**
	 * Calculates an expression.
	 * @param expression	Expression to be calculated
	 * @return	Returns a {@link Double} object if successful. In case of error an error message.
	 */
	Object calc(final String expression);

	/**
	 * Returns the computer time in milliseconds.
	 * @return	Computer time in milliseconds
	 */
	long getTime();

	/**
	 * Load and returns a value from a Internet address
	 * @param url	URL to be loaded
	 * @param errorValue	Value to be returned in case of an error
	 * @return	Loaded value or <code>errorValue</code> in case of an error
	 */
	double getInput(final String url, final double errorValue);
	
	/**
	 * Executes an external command and returns immediately.
	 * @param commandLine	Command to execute
	 * @return	Returns <code>true</code> if the command could be executed
	 */
	boolean execute(final String commandLine);

	/**
	 * Executes an external command and returns the output.
	 * @param commandLine	Command to execute
	 * @return	Returns the output if successful, otherwise <code>null</code>
	 */
	String executeAndReturnOutput(final String commandLine);

	/**
	 * Executes an external command and waits for completion.
	 * @param commandLine	Command to execute
	 * @return	Returns the return code if successful, -1 otherwise
	 */
	int executeAndWait(final String commandLine);
	
		/**
	 * Returns a station local data object for script data.<br>
	 * This method should <b>not</b> be used from this interface.
	 * This is a bug, but must be kept for compatibility with existing models.
	 * The method of the same name in the {@link SystemInterface} should be used.
	 * @return	Station local data object for script data
	 */
	Map<String,Object> getMapLocal();

	/**
	 * Returns the global data object for script data.<br>
	 * This method should <b>not</b> be used from this interface.
	 * This is a bug, but must be kept for compatibility with existing models.
	 * The method of the same name in the {@link SystemInterface} should be used.
	 * @return	Global data object for script data
	 */
	Map<String,Object> getMapGlobal();
}
