

GIT=~/git/github.com
AG=$GIT/agile-iot
JNR=$GIT/jnr

cd $JNR/jffi
mvn clean install -DskipTests=true

cd $JNR/jnr-ffi
mvn clean install -DskipTests=true

cd $JNR/jnr-unixsocket
mvn clean install -DskipTests=true

cd $GIT/dbus-java-mvn
mvn clean install -DskipTests=true

cd $AG/agile-api-spec/agile-dbus-java-interface
mvn clean install -DskipTests=true

cd $AG/agile-core
mvn clean install -DskipTests=true
