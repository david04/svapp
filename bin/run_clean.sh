
echo "Removing temp files"
find | grep -E "~$" | while read i; do rm "$i"; done

echo "Removing compiled files"
rm -rf target/ project/target/ project/project/target/ src/main/webapp/VAADIN/widgetsets/

