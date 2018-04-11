import classData
import re
import LatexGenerator
class Parser():

	propertyRegex = r'(public |private |protected )(static )?([a-zA-Z<>\[\]]* )([a-zA-Z][a-zA-Z0-9]*)([\s]*?=|;)'
	classRegex = r'(public |private |protected )?(class )([a-zA-Z]*)'
	methodRegex = r'(public |private |protected )(static )?([a-zA-Z<>\[\]]* |void )?([a-zA-Z]*\(([a-zA-Z\[\]<>]{1,} [a-zA-z]{1,}( ?, ?)?)*\))'
	classes = list()
	currentClass = None
	def __init__(self, classPath):
		self.classPath = classPath

	def printFile(self):
		with open(self.classPath) as file:
			for line in file:
				print line

	def parserFile(self):
		with open(self.classPath) as file:
			for line in file:
				match = re.search(self.classRegex, line)
				if match is not None:
					print match.group(), " CLASS"
					self.newClass(match.group())
					continue
				match = re.search(self.methodRegex, line)
				if match is not None:
					print match.group(), " METHOD"
					self.newMethod(match.group())
					continue
				match = re.search(Self.propertyRegex, line)
				if match is not None:
					print match.group(), " PROPERTY"
					self.newProperty(match.group())
					continue
		if self.currentClass is not None:
			self.classes.append(self.currentClass)

	def newClass(self, declaration):
		if self.currentClass is not None:
			self.classes.append(self.currentClass)
		self.currentClass = classData.Class()
		self.currentClass.setDeclaration(declaration)
		self.currentClass.setDescription("TODO make class description")
		print declaration.split(" ")[-1]
		self.currentClass.setName(declaration.split(" ")[-1])

	def newProperty(self, declaration):
		if self.currentClass is None:
			print "ERROR found property without a class"
			return
		dec = declaration.split(" ")
		if dec[1] == "static":
			currentClass.addProperty(classData.Property().newProperty(dec[0], dec[2], dec[3], declaration))
		else:
			currentClass.addProperty(classData.Property().newProperty(dec[0], dec[1], dec[2], declaration))


	def newMethod(self, declaration):
		if self.currentClass is None:
			print "ERROR found method without a class"
			return
		name = declaration.split("(")[0].split(" ")[-1]
		parameters = declaration.split("(")[1].replace(")", "").split(",")
		method = classData.Method()
		method.newMethod(name, "TODO make method description", declaration)
		for param in parameters:
			param = param.strip()
			print param + "  PARAM"
			if not param:
				continue
			parameter = classData.Parameter()
			parameter.newParameter(*param.split(" "))
			method.addParameter(parameter)

		if self.currentClass.name == method.name:
			self.currentClass.addConstructor(method)
		else:
			self.currentClass.addMethod(method)

	def generateLatex(self):
		g = LatexGenerator.Generator("test/test.tex")
		g.generateForClasses(self.classes)



p = Parser("/Users/dunmi001/python/autocument/ns.java")
p.parserFile()
p.generateLatex()
