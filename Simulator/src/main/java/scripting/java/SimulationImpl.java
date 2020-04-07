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
 * Implementierungsklasse für das Interface {@link SimulationInterface}
 * @author Alexander Herzog
 * @see SimulationInterface
 */
public class SimulationImpl implements SimulationInterface {
	/**
	 * Das Objekt kann nicht eingestellt werden.
	 * Es wird bei Bedarf automatisch angelegt werden.
	 */
	private RuntimeImpl runtime=null;

	/**
	 * Ein {@link SystemImpl}-Objekt muss manuell eingetragen werden,
	 * wenn {@link SimulationImpl#getSystem()} einen Wert ungleich
	 * <code>null</code> liefern soll.
	 */
	public SystemImpl system=null;

	/**
	 * Ein {@link ClientImpl}-Objekt muss manuell eingetragen werden,
	 * wenn {@link SimulationImpl#getClient()} einen Wert ungleich
	 * <code>null</code> liefern soll.
	 */
	public ClientImpl client=null;

	/**
	 * Ein {@link ClientsImpl}-Objekt muss manuell eingetragen werden,
	 * wenn {@link SimulationImpl#getClients()} einen Wert ungleich
	 * <code>null</code> liefern soll.
	 */
	public ClientsImpl clients=null;

	/**
	 * Ein {@link InputValueImpl}-Objekt muss manuell eingetragen werden,
	 * wenn {@link SimulationImpl#getInputValue()} einen Wert ungleich
	 * <code>null</code> liefern soll.
	 */
	public InputValueImpl inputValue=null;

	/**
	 * Ein {@link OutputImpl}-Objekt muss manuell eingetragen werden,
	 * wenn {@link SimulationImpl#getOutput()} einen Wert ungleich
	 * <code>null</code> liefern soll.
	 */
	public OutputImpl output=null;

	/**
	 * Ein {@link OutputImpl}-Objekt muss manuell eingetragen werden,
	 * wenn {@link SimulationImpl#getFileOutput()} einen Wert ungleich
	 * <code>null</code> liefern soll.
	 */
	public OutputImpl fileoutput=null;

	/**
	 * Ein {@link StatisticsImpl}-Objekt muss manuell eingetragen werden,
	 * wenn {@link SimulationImpl#getStatistics()} einen Wert ungleich
	 * <code>null</code> liefern soll.
	 */
	public StatisticsImpl statistics=null;

	/**
	 * Ein {@link ModelImpl}-Objekt muss manuell eingetragen werden,
	 * wenn {@link SimulationImpl#getModel()} einen Wert ungleich
	 * <code>null</code> liefern soll.
	 */
	public ModelImpl model=null;

	@Override
	public RuntimeInterface getRuntime() {
		if (runtime==null) runtime=new RuntimeImpl();
		return runtime;
	}

	@Override
	public SystemInterface getSystem() {
		return system;
	}

	@Override
	public ClientInterface getClient() {
		return client;
	}

	@Override
	public ClientsInterface getClients() {
		return clients;
	}

	@Override
	public InputValueInterface getInputValue() {
		return inputValue;
	}

	@Override
	public OutputInterface getOutput() {
		return output;
	}

	@Override
	public OutputInterface getFileOutput() {
		return fileoutput;
	}

	@Override
	public StatisticsInterface getStatistics() {
		return statistics;
	}

	@Override
	public ModelInterface getModel() {
		return model;
	}
}
