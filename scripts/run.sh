#!/bin/bash

function usage() {
	echo "Usage: $0 [type]"
	echo ":: type ::"
	echo "0 : use nlp"
	echo "1 : simple"
	exit 1
}

TYPE=$1

if [[ $# -ne 1 ]]; then
	usage
fi

if [[ ${TYPE} -ne "1" ]] && [[ ${TYPE} -ne "0" ]]; then
	usage
fi

# env.
CONFIG="/Users/sindongboy/Dropbox/Documents/workspace/keyword-extraction-core/config"
NLP_CONFIG="/Users/sindongboy/Dropbox/Documents/workspace/nlp_indexterms/config"
NLP_DICT="/Users/sindongboy/Dropbox/Documents/workspace/nlp_indexterms/resource"
RESOURCE="/Users/sindongboy/Dropbox/Documents/workspace/keyword-extraction-core/resource"

# dependency
DEP=`find ../lib -type f -name "*" | awk '{printf("%s:", $0);}' | sed 's/:$//g'`
TARGET="../target/keyword-extraction-core-1.0.0-SNAPSHOT.jar"

CP="${DEP}:${CONFIG}:${NLP_DICT}:${NLP_CONFIG}:${RESOURCE}:${TARGET}"

if [[ ${TYPE} == "0" ]]; then
	# omp
	java -Xmx4G -Dfile.encoding=UTF-8 -cp $CP com.skplanet.nlp.driver.KeywordExtractorTester
else
	# simple
	java -Xmx4G -Dfile.encoding=UTF-8 -cp $CP com.skplanet.nlp.driver.SimpleKeywordExtractionTester
fi


