
source settings.sh

echo "DROP DATABASE $NAME" | sudo sudo -u postgres psql
echo "CREATE DATABASE $NAME OWNER $NAME" | sudo sudo -u postgres psql

cat resources/db/schema.sql | sudo sudo -u postgres psql $NAME -U $NAME

