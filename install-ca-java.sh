#!/bin/bash

mkdir /tmp/java-cas
pushd /tmp/java-cas

curl -O http://www.startssl.com/certs/ca.crt
curl -O http://www.startssl.com/certs/sub.class2.server.ca.crt

echo "Keystore password should be \"changeit\", then enter \"y\""

JAVA_VERSIONS=$(ls /Library/Java/JavaVirtualMachines/)

for VERSION in $JAVA_VERSIONS ; do
  echo "Importing into java version $VERSION"

  sudo keytool -import -trustcacerts -keystore /Library/Java/JavaVirtualMachines/$VERSION/Contents/Home/jre/lib/security/cacerts -alias startcom.ca -file ca.crt
  sudo keytool -import -trustcacerts -keystore /Library/Java/JavaVirtualMachines/$VERSION/Contents/Home/jre/lib/security/cacerts -alias class2.startcom.ca -file sub.class2.server.ca.crt

done

popd
rm -rf /tmp/java-cas
