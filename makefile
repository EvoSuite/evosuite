.PHONY: debugEvosuite runEvosuite

EVOSUITE_PROP_FILE=evosuite-files/evosuite.properties

EVOSUITE_PROP_DEBUG=\
	CP=:/Users/gorla/Documents/workspaces/workspaceDatec/SampleTestsDF/bin/ \
	PROJECT_PREFIX= \
	timeout=50000000000000000000 \
	port=1044 \
	debug=true \
	client_on_thread=false \
	log.level=DEBUG \
	write_cfg=false

EVOSUITE_PROP_EXEC=\
	CP=:/Users/gorla/Documents/workspaces/workspaceDatec/SampleTestsDF/bin/ \
	PROJECT_PREFIX= \
	log.level=DEBUG \
	client_on_thread=true \
	global_timeout=60 \
	write_cfg=true \
	criterion=defuse

EVOSUITE_PROP_EXEC_SF100=\
	PROJECT_PREFIX= \
	log.level=DEBUG \
	global_timeout=60 \
	write_cfg=false \
	criterion=defuse \
	sandbox=true

#	client_on_thread=true 

debugEvosuite : 
	( for x in $(EVOSUITE_PROP_DEBUG); \
		do echo "$$x" ; done ) > $(EVOSUITE_PROP_FILE)
	java -cp target/classes:target/evosuite-0.1-SNAPSHOT-jar-minimal.jar \
		org.evosuite.EvoSuite \
		-Dprint_to_system=true \
		-criterion defuse \
		-generateSuite \
		-target temp-1_tullibee/ \
		-cp temp-1_tullibee/
#		-cp /Users/gorla/Documents/workspaces/workspaceDatec/SampleTestsDF/bin/ 

runEvosuite : 
	( for x in $(EVOSUITE_PROP_EXEC); \
		do echo "$$x" ; done ) > $(EVOSUITE_PROP_FILE)
	java -cp target/classes:target/evosuite-0.1-SNAPSHOT-jar-minimal.jar \
		org.evosuite.EvoSuite \
		-Dprint_to_system=true \
		-criterion defuse \
		-generateSuite \
		-Dstopping_condition=MaxTime \
		-Dsearch_budget=5 \
		-target temp-1_tullibee/ \
		-cp temp-1_tullibee/ >runEvosuite.log
#		-class TestClass 
#		-cp /Users/gorla/Documents/workspaces/workspaceDatec/SampleTestsDF/bin/ \


SF100DIR="/Users/gorla/Documents/workspaces/workspaceDatec/evosuite-dataflow/SF100"
PROJECTS = 1_tullibee 2_a4j 3_jigen 4_rif 5_templateit 6_jnfe 7_sfmis 8_gfarcegestionfa 9_falselight 10_water-simulator


SF100_LOGS=$(PROJECTS:%=%.log)
sf100: $(SF100_LOGS) 

.sf100prop : 
	echo "Writing properties file for sf100 suite"
	mkdir -p evosuite-files
	if [ -e $(EVOSUITE_PROP_FILE) ]; then \
		rm $(EVOSUITE_PROP_FILE); \
	fi
	( for x in $(EVOSUITE_PROP_EXEC_SF100); \
		do echo "$$x" ; done ) >> $(EVOSUITE_PROP_FILE)



%.log : .sf100prop
	rm -rf temp-$*
	mkdir -p temp-$*
	echo "** Analyzing project: $*"
	cp $(SF100DIR)/$*/*.jar temp-$*/
	cd temp-$*; jar xvf $(SF100DIR)/$*/*.jar
	LIBS=""; \
	for l in $(SF100DIR)/$*/lib/*.jar; \
		do LIBS="$$LIBS:$$l"; done; \
	java -cp target/classes:target/evosuite-0.1-SNAPSHOT-jar-minimal.jar \
		org.evosuite.EvoSuite \
		-Dprint_to_system=true \
		-generateSuite \
		-Dstopping_condition=MaxTime \
		-Dsearch_budget=5 \
		-cp "temp-$*:$$LIBS" \
		-target temp-$*/ 2> $*-exc.log > $*.log 


clean :
	-rm *.log
	rm -rf evosuite-tests
	rm -rf evosuite-files
	rm -rf evosuite-graphs
	rm -rf evosuite-report
	rm -rf temp-*