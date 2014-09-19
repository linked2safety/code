echo "Installing local maven dependencies...."
mvn install:install-file -Dfile=ejbca-ws-cli.jar -DgroupId=org.ejbca.core   -DartifactId=ws-cli -Dversion=1.0.0 -Dpackaging=jar
mvn install:install-file -Dfile=lib/cert-cvc.jar  -DgroupId=org.ejbca.cvc   -DartifactId=cert-cvc -Dversion=1.0.0 -Dpackaging=jar
mvn install:install-file -Dfile=lib/cesecore-ejb-interface.jar  -DgroupId=org.cesecore   -DartifactId=cesecore -Dversion=1.0.0 -Dpackaging=jar
mvn install:install-file -Dfile=lib/ejbca-util.jar   -DgroupId=org.ejbca.util   -DartifactId=ejbca-util -Dversion=1.0.0 -Dpackaging=jar
mvn install:install-file -Dfile=lib/cesecore-common.jar    -DgroupId=org.cesecore   -DartifactId=cesecore-common -Dversion=1.0.0 -Dpackaging=jar
mvn install:install-file -Dfile=lib/cesecore-entity.jar   -DgroupId=org.cesecore   -DartifactId=cesecore-entity -Dversion=1.0.0 -Dpackaging=jar
mvn install:install-file -Dfile=lib/ejbca-ws.jar   -DgroupId=org.ejbca.core   -DartifactId=ejbca-ws -Dversion=1.0.0 -Dpackaging=jar
echo "Done!"
