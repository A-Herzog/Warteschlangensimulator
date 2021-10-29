"""
Generates a model from a template or allows to read data from statistics.
The template for the generator itself is a valid model.
There is no need for placeholders in the template.
The locations to be changed are addressed in this script.
On replacing the template file will not be changed.
"""

from typing import Optional
import xml.etree.ElementTree as ElementTree


class Model:
    """Model file changer

    Usage example:\n
    model = Model("Template.xml")\n
    model.set_attr(1, "ModelElementBatch", "Size", 123)\n
    model.set_initial_variable_value("VariableName",10)\n
    model.save("newModel.xml") or model_as_string=model.get()
    """

    def __init__(self, templateFile: str) -> None:
        """Loads a template file

        Args:
            templateFile (str): Template file
        """
        self.__namespace = {'QS': 'https://a-herzog.github.io'}
        ElementTree.register_namespace('', 'https://a-herzog.github.io')

        self.__tree = ElementTree.parse(templateFile)
        self.__root = self.__tree.getroot()

    def set_attr(self, station_id: int, tagName: str, attrName: str, attrValue) -> None:
        """Changes the value of an attribute at a sub element at a station

        Args:
            station_id (int): Id of the station
            tagName (str): Tag name of the sub element at the station
            attrName (str): Attribute name
            attrValue ([type]): New value for the attribute
        """
        list = self.__root.findall('.//QS:*[@id = "' + str(station_id) + '"]', self.__namespace)
        if len(list) == 0: return
        currentElement = list[0]
        if not isinstance(currentElement, ElementTree.Element): return

        list = currentElement.findall('.//QS:' + tagName, self.__namespace)
        if len(list) == 0: return
        tag = list[0]
        tag.attrib[attrName] = str(attrValue)

    def set_content(self, station_id: int, tagName: str, content) -> None:
        list = self.__root.findall('.//QS:*[@id = "' + str(station_id) + '"]', self.__namespace)
        if len(list) == 0: return
        currentElement = list[0]
        if not isinstance(currentElement, ElementTree.Element): return

        list = currentElement.findall('.//QS:' + tagName, self.__namespace)
        if len(list) == 0: return
        tag = list[0]
        tag.text = content

    def set_initial_variable_value(self, name: str, value) -> None:
        """Changes the initial value of a variable

        Args:
            name (str): Name of the variable
            value ([type]): Initial value
        """
        list = self.__root.findall('.//QS:InitialeVariable[@Name = "' + name + '"]', self.__namespace)
        if len(list) == 0: list = self.__root.findall('.//QS:InitialVariable[@Name = "' + name + '"]', self.__namespace)
        if len(list) == 0: return
        tag = list[0]
        tag.text = str(value)

    def save(self, outputFile: str) -> None:
        """Saves the changed model

        Args:
            outputFile (str): Model output file
        """
        with open(outputFile, 'wb') as f:
            self.__tree.write(f, encoding='utf8', method='xml')

    def get(self) -> str:
        """Returns the changed model as a string

        Returns:
            str: Changed model as string
        """
        return '<?xml version="1.0" encoding="UTF-8"?>\n' + ElementTree.tostring(self.__root, encoding='unicode')


class Statistics:
    """Allows to access individual values from a statistics file"""

    def __init__(self, statistics: bytes) -> None:
        """Allows to access individual values from a statistics file

        Args:
            statistics (bytes): Statistic xml data
        """
        self.__namespace = {'QS': 'https://a-herzog.github.io'}
        ElementTree.register_namespace('', 'https://a-herzog.github.io')
        self.__root = ElementTree.fromstring(statistics)

    def get_attr(self, path: str, attr: str) -> Optional[str]:
        """Gets an attribute value

        Args:
            path (str): XPath to the tag from which the attribute is to be read
            attr (str): Name of the attribute

        Returns:
            Optional[str]: Attribute value or None if the path does not exist
        """
        list = self.__root.findall('.//QS:' + path, self.__namespace)
        if len(list) == 0: return None
        return list[0].attrib[attr]

    def get_content(self, path: str) -> Optional[str]:
        """Gets the text content from a tag

        Args:
            path (str): XPath to the tag

        Returns:
            Optional[str]: Text content or None if the path does not exist
        """
        list = self.__root.findall('.//QS:' + path, self.__namespace)
        if len(list) == 0: return None
        return list[0].text
