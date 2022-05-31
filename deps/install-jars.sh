#/usr/bin/bash

mvn install:install-file -Dfile=batfish-0.36.0.jar -DgroupId=org.batfish \
                         -DartifactId=batfish -Dversion=0.36.0 \
                         -Dpackaging=jar

mvn install:install-file -Dfile=batfish-common-protocol-0.36.0.jar -DgroupId=org.batfish \
                         -DartifactId=batfish-common-protocol -Dversion=0.36.0 \
                         -Dpackaging=jar

mvn install:install-file -Dfile=ddlogapi.jar -DgroupId=org.ddlog \
                         -DartifactId=ddlogapi -Dversion=1.2.3 \
                         -Dpackaging=jar
