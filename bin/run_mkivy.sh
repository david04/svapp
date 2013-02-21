
mkdir -p ivy

rm ivy/*

find  ~/.ivy2/ -type f |grep "jar$" | while read i; do ln -fs "$i" ivy/; done


