# Building Warteschlangensimulator

#### Required tools for building the project

* Java Development Kit, version 8 or higher ([adoptopenjdk.net](https://adoptopenjdk.net/) or [www.graalvm.org](https://www.graalvm.org/))
* NSIS ([nsis.sourceforge.io](https://nsis.sourceforge.io/Main_Page))
* LaTeX for building the documentation pdfs
* Maven (no need for manual installation if using Eclipse)

All other tools and dependencies will be downloaded during build process by Maven or via Ant target (see below).

#### Recommended tools for editing/building the project

* Eclipse ([www.eclipse.org](https://www.eclipse.org/))
* Eclipse Web Developer Tools (via Eclipse Marketplace; to get syntax highlighting in the html help files)
* SpotBugs Eclipse plugin (via Eclipse Marketplace; to scan for potential bugs)
* Better PO editor ([github.com/mlocati/betterpoeditor](https://github.com/mlocati/betterpoeditor); for easier editing the language files)
* TeXnicCenter ([www.texniccenter.org](https://www.texniccenter.org/); for easier editing/building the LaTeX documentation)

#### Steps to build using Eclipse

1. Import the projects "SimSystem", "SimTools", "Simulator" and "Simulator-build" into a new workspace.
2. Set for better compatibility in workspace Preferences>Java>Compiler>JDK Compliance>Compiler compliance level to "1.8" (all higher versions are also supported).
3. To avoid "Info" level problem messages, set in Preferences>Java>Compiler>Javadoc all dropdowns from "Ignore" to "Info" and in Preferences>Java>Compiler>Error/Warnings in the section "Potential programming problems" the entry "Potential resource leak" from "Ignore" to "Info".
4. Run target "downloadTools" in Ant file Simulator/tools/ant-downloadTools.xml to get libraries not available in Maven Central.
5. Run target "downloadLanguageTools" in Ant file Simulator/language/ant-language.xml to get tools for updating languages and installers.
6. Run target "latex" in Ant file Simulator/build/ant-build.xml to compile the LaTeX files to pdfs.
7. Run Maven goals "clean" and "install" on Simulator-build/pom.xml.
8. Run target "build" in Ant file Simulator/build/ant-build.xml to build simulator installer and binary zip archive in "Release" folder.