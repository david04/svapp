
BIN="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

source settings.sh
source "$BIN/svapp_defs.sh"

echo "Packaging..."
RUN_PACKAGE

du -sh "$WAR"

RUN "mkdir -p '$TARGET/$NAME'"

RSYNC "$WAR" "$SSHUSER@$SSHHOST:$TARGET/$NAME/$NAME.war_"

RUN "cp '$TARGET/$NAME/$NAME.war_' '$TARGET/webapps/root.war'"

RSYNC ./*.sh "$SSHUSER@$SSHHOST:$TARGET/$NAME/"
RSYNC "resources" "$SSHUSER@$SSHHOST:$TARGET/$NAME/"
RSYNC "config" "$SSHUSER@$SSHHOST:$TARGET/config/default"

RUN "ln -s /opt/$JETTY_VER/config/default/config/$NAME.config /opt/$JETTY_VER/config/"

RUN "cd /opt/$JETTY_VER; export JETTY_PORT=80 ; bin/jetty.sh restart"


