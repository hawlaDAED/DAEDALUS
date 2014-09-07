#!/bin/bash
# script for convenience purposes during development of the DAEDALUS software
if [ -z $1 ]
then
java -Djava.library.path=Resources -Dcharva.color=1 -jar  dist/DAEDALUS.jar 2>&1 $HOME/DAED_startup.log 
else
java -Djava.library.path=Resources -Dcharva.color=1 -jar  dist/DAEDALUS.jar $1 2>&1 $HOME/DAED_startup.log 
fi

