.PHONY: help clean victims-java-service image

default: help

help:
	@echo "Targets:"
	@echo "	victims-java-service: Builds a victims-java-service jar"
	@echo "	clean: cleans up and removes built files"
	@echo "	image: builds a container image"

victims-java-service:
	mvn clean package

clean:
	mvn clean

image: clean
	s2i build . redhat-openjdk-18/openjdk18-openshift victims-java

test:
	mvn clean verify
