
# ===== FUNCTIONS =====

DEBUG_RUN=false

function RUN { echo "> '$*'"; [[ "$DEBUG_RUN" = "true" ]] && read i; ssh -t -i $SSHKEY $SSHUSER@$SSHHOST "$*"; }
function RSYNC { rsync -avz -e "ssh -i $SSHKEY" $*; }

function RUN_PACKAGE { 
	P="$(pwd)"
	cd "$SBT_PROJ_PATH"
	echo package | sbt
	cd "$P"
}


