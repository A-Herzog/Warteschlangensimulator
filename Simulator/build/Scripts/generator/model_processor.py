"""
Generates a model from a template or allows to read data from statistics.
The template for the generator itself is a valid model.
There is no need for placeholders in the template.
The locations to be changed are addressed in this script.
On replacing the template file will not be changed.
"""

from typing import Optional
from xml.etree.ElementTree import Element, ElementTree, register_namespace as xml_register_namespace, parse as xml_parse, tostring as xml_tostring, fromstring as xml_fromstring


class Model:
    """Model file changer

    Usage example:\n
    model = Model("Template.xml")\n
    model.set_attr(1, "ModelElementBatch", "Size", 123)\n
    model.set_initial_variable_value("VariableName",10)\n
    model.save("newModel.xml") or model_as_string=model.get()
    """

    def __init__(self, model: str | ElementTree) -> None:
        """Loads a template file

        Args:
            model (str | ElementTree): Model file or model xml object
        """
        self.__namespace = {'QS': 'https://a-herzog.github.io'}
        xml_register_namespace('', 'https://a-herzog.github.io')

        if isinstance(model, str):
            self.__tree = xml_parse(model)

        if isinstance(model, ElementTree):
            self.__tree = model

        self.__root = self.__tree.getroot()

    def copy(self):
        """Generates an independend copy of the model

        Returns:
            Model: New model object with its own internal xml representation
        """
        text = self.get()
        tree = xml_fromstring(text)
        return Model(ElementTree(element=tree))

    def __ns_path(self, path: str) -> str:
        return "/".join(["QS:"+segment for segment in path.split("/")])

    def get_elements(self, path: str) -> list[Element]:
        """Returns a list of xml elements from a given path

        Args:
            path (str): XPath to the elements

        Returns:
            list[Element]: List of the elements (can be empty)
        """
        return self.__root.findall('.//' + self.__ns_path(path), self.__namespace)

    def get_element(self, path: str) -> Element | None:
        """Returns the first xml element for a given path

        Args:
            path (str): XPath to the element

        Returns:
           Element | None: element or None if not element was found for the path
        """
        list = self.get_elements(path)
        if len(list) == 0: return None
        return list[0]

    def set_attr(self, station_id: int, tag_name: str, attr_name: str, attr_value) -> bool:
        """Changes the value of an attribute at a sub element at a station

        Args:
            station_id (int): Id of the station
            tag_name (str): Tag name of the sub element at the station
            attr_name (str): Attribute name
            attr_value: New value for the attribute

        Returns:
            bool: Returns True if changing the value was successful
        """
        list = self.__root.findall('.//QS:*[@id = "' + str(station_id) + '"]', self.__namespace)
        if len(list) == 0: return False
        currentElement = list[0]
        if not isinstance(currentElement, Element): return False

        list = currentElement.findall('.//QS:' + tag_name, self.__namespace)
        if len(list) == 0: return False
        tag = list[0]
        tag.attrib[attr_name] = str(attr_value)
        return True

    def set_content(self, station_id: int, tag_name: str, content) -> bool:
        """Changes the text content of a sub element at a station

        Args:
            station_id (int): Id of the station
            tag_name (str): Tag name of the sub element at the station
            content: New text value

        Returns:
            bool: Returns True if changing the value was successful
        """
        list = self.__root.findall('.//QS:*[@id = "' + str(station_id) + '"]', self.__namespace)
        if len(list) == 0: return False
        currentElement = list[0]
        if not isinstance(currentElement, Element): return False

        list = currentElement.findall('.//QS:' + tag_name, self.__namespace)
        if len(list) == 0: return False
        tag = list[0]
        tag.text = content
        return True

    def set_operator_count(self, name: str, value) -> bool:
        """Changes the number of operators in a group

        Args:
            name (str): Name of the operator group
            value (_type_): Number of operators

        Returns:
            bool: Returns True if changing the value was successful
        """
        list = self.__root.findall('.//QS:Ressource[@Name = "' + name + '"]', self.__namespace)
        if len(list) > 0:
            tag = list[0]
            tag.attrib["Wert"] = str(value)
            return True

        list = self.__root.findall('.//QS:Resource[@Name = "' + name + '"]', self.__namespace)
        if len(list) > 0:
            tag = list[0]
            tag.attrib["Value"] = str(value)
            return True

        return False

    def set_initial_variable_value(self, name: str, value) -> bool:
        """Changes the initial value of a variable

        Args:
            name (str): Name of the variable
            value ([type]): Initial value

        Returns:
            bool: Returns True if changing the value was successful
        """
        list = self.__root.findall('.//QS:InitialeVariable[@Name = "' + name + '"]', self.__namespace)
        if len(list) == 0: list = self.__root.findall('.//QS:InitialVariable[@Name = "' + name + '"]', self.__namespace)
        if len(list) == 0: return False
        tag = list[0]
        tag.text = str(value)
        return True

    def save(self, outputFile: str) -> None:
        """Saves the changed model

        Args:
            outputFile (str): Model output file
        """
        with open(outputFile, 'wb') as f:
            self.__tree.write(f, encoding='UTF-8', method='xml', xml_declaration=True)

    def get(self) -> str:
        """Returns the changed model as a string

        Returns:
            str: Changed model as string
        """
        return '<?xml version="1.0" encoding="UTF-8"?>\n' + xml_tostring(self.__root, encoding='unicode')

    def xml(self) -> Element:
        """Returns the xml root element of the model

        Returns:
            Element: Root element of the model
        """
        return self.__root


class Statistics:
    """Allows to access individual values from a statistics file"""

    def __init__(self, statistics: bytes) -> None:
        """Allows to access individual values from a statistics file

        Args:
            statistics (bytes): Statistic xml data
        """
        self.__namespace = {'QS': 'https://a-herzog.github.io'}
        xml_register_namespace('', 'https://a-herzog.github.io')
        self.__root = xml_fromstring(statistics)

    def __ns_path(self, path: str) -> str:
        return "/".join(["QS:"+segment for segment in path.split("/")])

    def get_elements(self, path: str) -> list[Element]:
        """Returns a list of xml elements from a given path

        Args:
            path (str): XPath to the elements

        Returns:
            list[Element]: List of the elements (can be empty)
        """
        return self.__root.findall('.//' + self.__ns_path(path), self.__namespace)

    def get_element(self, path: str) -> Element | None:
        """Returns the first xml element for a given path

        Args:
            path (str): XPath to the element

        Returns:
           Element | None: element or None if not element was found for the path
        """
        list = self.get_elements(path)
        if len(list) == 0: return None
        return list[0]

    def get_attr(self, path: str, attr: str) -> Optional[str]:
        """Returns an attribute value

        Args:
            path (str): XPath to the tag from which the attribute is to be read
            attr (str): Name of the attribute

        Returns:
            Optional[str]: Attribute value or None if the path does not exist
        """
        element = self.get_element(path)
        if element is None: return None
        return element.attrib[attr]

    def get_content(self, path) -> Optional[str]:
        """Returns the text content from a tag

        Args:
            path (str): XPath segments to the tag

        Returns:
            Optional[str]: Text content or None if the path does not exist
        """
        element = self.get_element(path)
        if element is None: return None
        return element.text

    def xml(self) -> Element:
        """Returns the xml root element of the statistics data

        Returns:
            Element: Root element of the statistics data
        """
        return self.__root


def get_example_model() -> str:
    return """<?xml version="1.0" encoding="UTF-8"?>
<Model>
  <ModelClients Active="1">1000000</ModelClients>
  <ModelWarmUpPhase>0.01</ModelWarmUpPhase>
  <ModelTerminationTime Active="0">10:00:00:00</ModelTerminationTime>
  <ModelElements>
    <ModelElementSource id="1">
      <ModelElementName>Clients</ModelElementName>
      <ModelElementSize h="50" w="100" x="50" y="50"/>
      <ModelElementConnection Element="4" Type="Out"/>
      <ModelElementDistribution TimeBase="Seconds">Exponential distribution (60)</ModelElementDistribution>
      <ModelElementBatchData Size="1"/>
    </ModelElementSource>
    <ModelElementProcessStation id="2">
      <ModelElementSize h="50" w="100" x="250" y="50"/>
      <ModelElementConnection Element="4" Type="In"/>
      <ModelElementConnection Element="5" Type="Out"/>
      <ModelElementDistribution Status="ProcessTime" TimeBase="Seconds" Type="ProcessingTime">Exponential distribution (50)</ModelElementDistribution>
      <ModelElementPriority ClientType="Clients">w</ModelElementPriority>
      <ModelElementOperators Alternative="1" Count="1" Group="Operators"/>
      <ModelElementOperatorsPriority>1</ModelElementOperatorsPriority>
    </ModelElementProcessStation>
    <ModelElementDispose id="3">
      <ModelElementSize h="50" w="100" x="450" y="50"/>
      <ModelElementConnection Element="5" Type="In"/>
    </ModelElementDispose>
    <ModelElementEdge id="4">
      <ModelElementName>Arrivals (Clients)</ModelElementName>
      <ModelElementConnection Element1="1" Element2="2" Type="Edge"/>
    </ModelElementEdge>
    <ModelElementEdge id="5">
      <ModelElementConnection Element1="2" Element2="3" Type="Edge"/>
    </ModelElementEdge>
  </ModelElements>
  <Resources SecondaryPriority="Random">
    <Resource Name="Operators" Type="Number" Value="1"/>
  </Resources>
  <ClientTypeData/>
  <ModelEdgesNormal Color="0,0,0" Type="0" Width="1"/>
  <ModelEdgesSelected Type="0" Width="1"/>
  <ModelElementConnectionDrawStyle>Angled around</ModelElementConnectionDrawStyle>
</Model>"""