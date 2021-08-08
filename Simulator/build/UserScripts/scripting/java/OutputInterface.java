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
 * Interface for invoking output functions.
 * @author Alexander Herzog
 */
public interface OutputInterface {

	/**
	 * This command allows to setup the format that is used in <tt>print</tt> and <tt>println</tt>
	 * for outputing numbers as strings. You can specify whether to use a floating point notation or percent notation or interpreting
	 * the value as a time. As default floating point notation is used.
	 * @param format	Format, can be "Fraction", "Percent", "Number" or "Time"
	 */
	void setFormat(final String format);

	/**
	 * This command allows to select the separator to be used when printing out arrays.
	 * @param separator	Separator, can be "Semicolon", "Line" or "Tabs"
	 */
	void setSeparator(final String separator);

	/**
	 * This command allows to define the number of digits to be displayed when
	 * printing a number in local notation. A negative value means that all
	 * available digits are being printed.
	 * (If the system notation is used, always all available digits are being printed.)
	 * @param digits	Number of digits to be output when using local notation
	 */
	void setDigits(final int digits);

	/**
	 * Outputs the passed expression.
	 * Strings will be written directly. Numbers are formated according to the format
	 * defined via <tt>setFormat</tt>.
	 * @param obj	Object to output
	 */
	void print(final Object obj);

	/**
	 * Outputs the passed expression and adds a line break after the expression.
	 * Strings will be written directly. Numbers are formated according to the format
	 * defined via <tt>setFormat</tt>.
	 * @param obj	Object to output
	 */
	void println(final Object obj);

	/**
	 * Outputs a line break. This functions is equivalent to calling <tt>println("")</tt>.  
	 */
	void newLine();

	/**
	 * Outputs a tabulator.
	 */
	void tab();

	/**
	 * Sets the cancel status. (When output is canceled to further file output will be performed.)
	 */
	void cancel();

	/**
	 * Sets an output file. This method is only used at file output stations.
	 * In all other cases this method has does nothing.
	 * @param file	Output file
	 */
	void setFile(final Object file);

	/**
	 * This command is only available if DDE is available, i.e. under Windows.
	 * It outputs the passed expression via DDE in the specified table in Excel.
	 * Numbers are formated according to the format defined via <tt>setFormat</tt>.
	 * @param workbook	Target workbook
	 * @param table	Target table in workbook
	 * @param cell	Target cell in table
	 * @param obj	Object to output
	 * @return	Returns <code>true</code> if the DDE operation could be performed
	 */
	boolean printlnDDE(final String workbook, final String table, final String cell, final Object obj);
}
