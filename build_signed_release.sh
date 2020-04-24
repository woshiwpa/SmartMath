#/bin/bash

if [ "$#" -ne 4 ]; then
    echo "Need four parameters, first is the address of key file for this Calculator app."
    echo "Note that in cygwin it should be like d:/Development/publish/SMC/smartmathcalc."
    echo "The address cannot be /cygdrive/d/Development/publish/SMC/smartmathcalc."
    echo "The second parameters is store password. The third and fourth are key name and"
    echo "password respectively."
    exit
fi



./gradlew assembleRelease -Pandroid.injected.signing.store.file=$1 -Pandroid.injected.signing.store.password=$2 -Pandroid.injected.signing.key.alias=$3 -Pandroid.injected.signing.key.password=$4
