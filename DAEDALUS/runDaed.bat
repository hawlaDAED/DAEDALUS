rem JAVA_HOME must be set to the JDK or JRE installation directory 
rem (for example, C:\jdk1.4 or C:\jre1.4)
rem set JAVA_HOME=C:\j2sdk1.4.2
if not exist %JAVA_HOME% goto noJAVA_HOME

:noJAVA_HOME
echo The JAVA_HOME environment variable is not set!
goto end

%JAVA_HOME%\bin\java -Djava.library.path=Resources -Dcharva.color=1 -jar  dist/DAEDALUS.jar > %HOMEPATH%\DAED.log 2>&1 
goto end

:end
