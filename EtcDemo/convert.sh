
cd $(dirname $0)
cd app/src/main/res/drawable
rm -rf png
mkdir png
for file in $(ls *.webp);do
    convert $file png/${file%.*}.png;
done
rm -rf myImages
for file in $(ls png/*.png);do
    etcpack $file myImages  -c etc1 -aa -ext PNG
done
cd myImages
rm yd.zip;
zip -0 -r yd.zip *.pkm;
cp yd.zip ../../../assets/etczip/lt.zip

