/profile name="default"/,/<\/profile>/ {
    /urn:jboss:domain:web:/ a\
        \        <connector name="ajp" protocol="AJP/1.3" scheme="http" socket-binding="ajp"/>
}
/<\/virtual-server>/ a\
\                <virtual-server name="cadmium.localhost" enable-welcome-root="false" />
/<server-group name="other-server-group"/,/<\/server-group>/d
/<server-group name="main-server-group"/,/<\/server-group>/ {
  s/profile="full"/profile="default"/g
  s_<socket-binding-group ref="full-sockets"/>_<socket-binding-group ref="standard-sockets"/>_g
  s#<heap size="1303m" max-size="1303m"/>#<heap size="MIN_HEAP_SIZE" max-size="MAX_HEAP"/>#g
  s#<permgen max-size="256m"/>#<permgen max-size="PERM_SIZE"/>#g
  /socket-binding-group/r cadmium-domain-group-props.xml
}
