
source settings.sh

while true;
do

echo "FROM:"
read OLDNAME

[[ "$OLDNAME" = "" ]] && break;

echo "TO:"
read NEWNAME

echo "$OLDNAME => $NEWNAME ok?"
read i

run_clean.sh

echo "Rename in files"
find -type f | while read i; do sed -i "s/$OLDNAME/$NEWNAME/g" "$i"; done

echo "Rename dirs"
NEXT="$(find -type d | grep -i $OLDNAME | head -n 1)"
while [ ! "$NEXT" = "" ];
do 
  mv -vi "$NEXT" "$(echo "$NEXT" | sed "s/$OLDNAME/$NEWNAME/g")";
  NEXT="$(find -type d | grep -i $OLDNAME | head -n 1)"
done

echo "Rename files"
NEXT="$(find -type f | grep -i $OLDNAME | head -n 1)"
while [ ! "$NEXT" = "" ];
do 
  mv -vi "$NEXT" "$(echo "$NEXT" | sed "s/$OLDNAME/$NEWNAME/g")";
  NEXT="$(find -type d | grep -i $OLDNAME | head -n 1)"
done

done;

echo "Compile project"
echo "compile" | sbt

