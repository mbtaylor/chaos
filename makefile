
JAVAC = javac
JAVA = java
JAR = jar
JAVADOC = javadoc
STIL_JAR = stil.jar
RESULT_DIR = results

JAVADOC_FLAGS = -Xdoclint:all,-missing

JARFILE = chaos.jar

JSRC = \
       Attractor.java \
       AttractorStarTable.java \
       ChaosWriter.java \

build: jar

jar: $(JARFILE)

run: build
	mkdir -p $(RESULT_DIR); \
	cd $(RESULT_DIR); \
	java -classpath $(JARFILE):$(STIL_JAR) ChaosWriter

$(STIL_JAR):
	curl -O 'http://andromeda.star.bris.ac.uk/~mbt/stil/stil.jar'

javadocs: $(JSRC)
	rm -rf javadocs
	mkdir javadocs
	$(JAVADOC) $(JAVADOC_FLAGS) -quiet \
                   -d javadocs $(JSRC) package-info.java

clean:
	rm -rf $(JARFILE) $(TEST_JARFILE) tmp \
               index.html javadocs cdflist.html cdfdump.html \
               $(ARTIFACTS) artifacts.zip \

veryclean: clean
	rm -f $(STIL_JAR) $(RESULT_DIR)

$(JARFILE): $(JSRC) $(STIL_JAR)
	rm -rf tmp
	mkdir -p tmp
	$(JAVAC) -classpath $(STIL_JAR) -d tmp $(JSRC) \
            && $(JAR) cf $@ -C tmp .
	rm -rf tmp


