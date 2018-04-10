class Generator():

	def __init__(self, outputFile):
		self.title = ""
		self.author = ""
		self.filePath = outputFile
		self.file = open(outputFile, "w")

	def close(self):
		self.file.close()
	def setTitle(self, title):
		self.title = title
	def setAuthor(self, author):
		self.author = author

	def generatePreamble(self):
		self.file.write("\\documentclass{article}\n")
		self.file.write("\\usepackage{graphicx}\n")
		self.file.write("\\usepackage{float}\n")
		self.file.write("\\usepackage{textcomp}\n")
		self.file.write("\\usepackage[margin=1in]{geometry}\n")
		self.file.write("\\usepackage{hyperref}\n")
		self.file.write("\\hypersetup{colorlinks=true}\n")
		self.file.write("\\title{" +self.title + "}\n")
		self.file.write("\\author{" + self.author+"}\n")
		self.file.write("\\date{\\today}\n")
		self.file.write("\\begin{document}\n")
		self.file.write("\\pagenumbering{gobble}\n")
		self.file.write("\\maketitle\n")
		self.file.write("\\newpage\n")
		self.file.write("\\tableofcontents\n")
		self.file.write("\\newpage\n")
		self.file.write("\\pagenumbering{arabic}\n")

	def generateForClasses(self, classes):
		self.generatePreamble()
		self.file.write("\\section{Class Breakdown}\n")
		classes.sort(key=lambda x: x.name)
		for classDoc in classes:
			self.generateForClass(classDoc)
		self.file.write("\\end{document}\n")
		self.file.close()

	def generateForClass(self, Class):
		self.file.write("\\subsection{"+Class.name+"}\n")
		self.file.write("\\paragraph{}" + Class.description +"\n")
		self.generateForConstructor(Class.constructors)
		self.generateForMethods(Class.methods)


	def generateForConstructor(self, constructors):
		self.file.write("\\subsubsection{Constructor}\n")
		for c in constructors:
			self.file.write("\\paragraph{" + c.declaration + "}\n")
			self.file.write("\\paragraph{}" + c.description +"\n")
			print c.name
			if c.parameters is not None:
				self.generateForParameters(c.parameters)





	def generateForMethods(self, methods):
		for m in methods:
			self.file.write("\\subsubsection{" +m.name + "}\n")
			self.file.write("\\paragraph{" + m.declaration+"}\n")
			self.file.write("\\paragraph{}" + m.description+"\n")
			print m.name
			if m.parameters is not None:
				self.generateForParameters(m.parameters)



	def generateForParameters(self, parameters):
		self.file.write("\\paragraph{Parameters}\n")
		for p in parameters:
			self.file.write("\\subparagraph{" + p.name + " " + p.name + "}" + p.description+"\n")





