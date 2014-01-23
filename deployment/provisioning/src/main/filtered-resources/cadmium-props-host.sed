s/127.0.0.1/0.0.0.0/g
/<server name="server-two"/,/<\/server>/d
/<server name="server-three"/,/<\/server>/d
/<server name="server-one"/r cadmium-host-props.xml
