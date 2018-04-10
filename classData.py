class Class():

	def __init__(self):
		self.constructors = list()
		self.methods = list()
		self.declaration = ""
		self.name = ""
		self.description = ""

	def addMethod(self, method):
		self.methods.append(method)

	def addConstructor(self, constructor):
		self.constructors.append(constructor)

	def setDeclaration(self, declaration):
		self.declaration = declaration

	def setName(self, name):
		self.name = name

	def setDescription(self, description):
		self.description = description


class Method():

	def __init__(self):
		self.parameters = list()
		self.returnType = ""
		self.returnDesc = ""
		self.declaration = ""
		self.name = ""
		self.description = ""

	def newMethod(self, name, description, declaration):
		self.name = name
		self.description = description 
		self.declaration = declaration

	def addParameter(self, parameter):
		self.parameters.append(parameter)

	def setReturnType(self, type):
		self.returnType = type

	def setReturnDesc(self, description):
		self.returnDesc = description

	def setDeclaration(self, declaration):
		self.declaration = declaration

	def setName(self, name):
		self.name = name

	def setDescription(self, description):
		sefl.description = description

class Parameter():
	
	def __init__(self):
		self.type = ""
		self.name = ""
		self.description = "TODO make parameter description"

	def newParameter(self, type, name):
		self.type = type
		self.name = name

	def setType(self, type):
		self.type = type

	def setName(self, name):
		self.name = name

	def setDescription(self, description):
		self.description = description