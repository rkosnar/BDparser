#!/bin/bash
#
#    This program is free software: you can redistribute it and/or modify
#    it under the terms of the GNU General Public License as published by
#    the Free Software Foundation, either version 3 of the License, or
#    (at your option) any later version.
#
#    This program is distributed in the hope that it will be useful,
#    but WITHOUT ANY WARRANTY; without even the implied warranty of
#    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
#    GNU General Public License for more details.
#
#    You should have received a copy of the GNU General Public License
#    along with this program.  If not, see <http://www.gnu.org/licenses/>.
#

if [[ "$1" == "do" ]]; then
	apt-get -y install handbrake-cli zenity
	cp BDparser.jar /usr/share/BDparser.jar
	unzip -p BDparser.jar icon.png > /usr/share/pixmaps/BDparser.png
#	touch /usr/share/applications/BDparser.desktop
	echo "[Desktop Entry]" > /usr/share/applications/BDparser.desktop
	echo "Encoding=UTF-8" >> /usr/share/applications/BDparser.desktop
	echo "Name=DB convert" >> /usr/share/applications/BDparser.desktop
	echo "Name[cs]=DB konvertor" >> /usr/share/applications/BDparser.desktop
	echo "Exec=java -jar /usr/share/BDparser.jar" >> /usr/share/applications/BDparser.desktop
	echo "Icon=/usr/share/pixmaps/BDparser.png" >> /usr/share/applications/BDparser.desktop
	echo "Type=Application" >> /usr/share/applications/BDparser.desktop
	chmod 777 /usr/share/applications/BDparser.desktop /usr/share/BDparser.jar /usr/share/pixmaps/BDparser.png
else
	gksudo ./install.sh do
fi
