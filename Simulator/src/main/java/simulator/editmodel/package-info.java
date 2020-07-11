/**
 * Das in diesem Package abgebildete Editor-Modell kann aus xml-Dateien geladen
 * und in diese geschrieben werden. Stationen, Eigenschaften usw. sind hier noch
 * nicht über Referenzen verbunden und das Modell ist noch nicht auf Konsistenz
 * geprüft. Dies ist das Modell, welches auch in {@link ui.EditorPanel}
 * zum Bearbeiten angeboten wird. Mit Hilfe des {@link simulator.builder.RunModelCreator}
 * kann es geprüft und in ein simulierbares {@link simulator.runmodel.RunModel}
 * umgewandelt werden.<br><br>
 * Die wesentliche Klasse in diesem Package ist:<br>
 * {@link simulator.editmodel.EditModel}
 * @author Alexander Herzog
 * @see simulator.editmodel.EditModel
 * @see ui.EditorPanel
 * @see simulator.builder.RunModelCreator
 * @see simulator.runmodel.RunModel
 */
package simulator.editmodel;