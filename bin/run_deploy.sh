
source settings.sh

echo "Packaging..."
echo package | sbt

du -sh "$WAR"

RUN "mkdir -p '$TARGET/$NAME'"

RSYNC "$WAR" "$SSHUSER@$SSHHOST:$TARGET/$NAME/$NAME.war_"

RUN "cp '$TARGET/$NAME/$NAME.war_' '$TARGET/webapps/root.war'"

RSYNC ./*.sh "$SSHUSER@$SSHHOST:$TARGET/$NAME/"
RSYNC "resources" "$SSHUSER@$SSHHOST:$TARGET/$NAME/"
RSYNC "config" "$SSHUSER@$SSHHOST:$TARGET/"

$RUN "cd /opt/$JETTY_VER; export JETTY_PORT=80 ; bin/jetty.sh restart"


