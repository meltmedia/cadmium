/jboss:domain:web:/a \
\            <connector name="ajp" protocol="AJP/1.3" scheme="http" socket-binding="ajp"/>
/<\/virtual-server>/ a\
\            <virtual-server name="cadmium.localhost" enable-welcome-root="false" />
s/127.0.0.1/0.0.0.0/g
/<\/extensions>/ r cadmium-standalone-props.xml
