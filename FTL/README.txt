FTL SUITE README
beta release - initial release february 2013

--THE FTL SUITE--
The FTL Suite is a platform for reasoning and learning upon the Feature Term (FTerm) formalism. 
FTL Suite supports the creation of ontologies, and the basic operations for reasoning are subsumption, anti-unification and unification. 
Moreover, the novel operation of amalgamating (or blending) terms is included, 
as well a novel similarity measures for complex situations represented as FTerms. 

The core of the FTL Suite is a collection of refinement operators which can be used as the basis of many  algorithms for reasoning and learning. 
The FTL Suite includes a number of implemented methods for learning using inductive concept learning approaches and CBR (lazy learning) approaches. 
These methods include classical techniques, classical techniques re-implemented upon refinement operators, and new techniques developed by the authors. 
The FTL Suite implements several classical and new similarity measures (implemented upon refinement operators) for complex objets represented as FTerms. 

Implemented techniques include similarity measures, FOIL (HIDRA), LID, INDIE, ABUI, AMAIL, and there is also a package to perform argumentation using FTerms. 
Translators from terms to other formats such as Horn Clauses, or even LaTeX figures are also available.

Finally, a collection of datasets represented as feature terms is included (most from the UCI machine learning repository), 
the user can select a specific dataset and use a learning method on it (either inductive concept learning methods or case-based lazy learning techniques). 
However, be aware the not all methods are applicable to all datasets when their requirements do not match.

--AUTHORSHIP AND OWNERSHIP
The core libraries and the implemented methods have been authored by Santigo Ontañón, 
in collaboration with Enric Plaza; the interface has been authored by Carlos López de Toro, in collaboration with Enric Plaza. 
The FTL Suite has been developed by the authors at Barcelona’s Artificial Intelligence Research Institute IIIA-CSIC.
The FTL Suite is distributed as open source under the Code license New BSD License.

--ACKNOWLEDGEMENTS
The FTL Suite development has been partially funded by two projects:
MID-CBR. An Integrative Framework for Developing Case-based Systems (TIN2006-15140-C03-01), and
Next-CBR. Evolving CBR for multi-source experience and knowledge-rich applications (MICIN TIN2009-13692-C03-01)
The FTL Suite is an (re)evolution of the NOOS representation language designed by Josep-Lluís Arcos and Enric Plaza.

--STRUCTURE--

The toolkit distribution uses the following organization:

+ FTL
|-- Resources   Various example data files used by the demo applications
|------- config   log4java configuration file
|------- DATA     Dataset packs
|------- Demos    Preloaded demos for Operations
|-- cache  Preloaded files to load faster	
|-- doc    Documentation. The Javadoc API files reside here once generated
|-- libs   Third-party libraries useful with prefuse and their licenses
|-- logs   A log4java file for the GUI
|-- src    The source code for the prefuse toolkit

--REQUIREMENTS--

FTL is written in Java 1.6 using a Java2D graphics library. To compile
the FTL code, and to build and run FTL applications, you'll need a
copy of the Java Development Kit (JDK) for version 1.6 or greater. You can
download the most recent version of the JDK from
http://java.sun.com/j2se/1.5.0/download.html.

We also recommended (though by no means is it required) that you use an
Integrated Development Environment such as Eclipse (http://eclipse.org).
Especially if you are a Java novice, it will likely make your life much easier.

--BUILDING--
You can use the Eclipse integrated development environment
(available for free at http://ww.eclipse.org) to load the source files, then
Eclipse will compile the software for you. Within Eclipse, right-click the
background of the "Package Explorer" panel and choose "Import". Then select
"Existing Projects into Workspace". In resulting dialog, click the radio
button for "Select archive file" and browse for the FTL distribution
zip file. The "FTL" project should then appear in the area below.
Now just click the "Finish" button to import the project and build it.
Once FTL has been loaded as a project within Eclipse, you
can then run the GUI placed in src/csic/iiia/ftl/demo/FTLGUI.java (main method)
to run demos or use the libraries on your own.

--LICENSE--
The file license.txt contains this project's license