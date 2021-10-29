"""
Generates multiple models from a template.
The template itself is not a valid model.
It contains one or more placeholders of the type $[name].
The placeholders can be replaced by strings defined here.
On replacing the template file will not be changed.
"""

class ModelGenerator:
    def __init__(self, templateFile: str) -> None:
        """Loads a template file

        Args:
            templateFile (str): Template file
        """
        with open(templateFile, "r") as file:
            self.__template = file.read()

    def process(self, values: dict, outputFile: str) -> None:
        """Replaces the placeholders with specific values and stores the model

        Args:
            values (dict): Dict with placeholder names and specific values
            outputFile (str): Model output file
        """
        model: str = self.__template
        for key in values:
            model = model.replace("$["+key+"]", str(values[key]))

        with open(outputFile, "w") as file:
            file.write(model)

"""
Example:
generator = ModelGenerator("Template.xml")
generator.process({"ServiceTime": 60}, "model1.xml") # Replaces "$[ServiceTime]" by "60"
generator.process({"ServiceTime": 90}, "model9.xml") # Replaces "$[ServiceTime]" by "90"
"""