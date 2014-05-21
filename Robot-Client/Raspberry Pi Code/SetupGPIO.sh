#!/bin/bash

echo " Robot Pi Bash Script Starting " ;

echo "Pin Out Set Up";
gpio mode 0 out
gpio mode 1 out
gpio mode 2 out
gpio mode 3 out
echo "Pin outs set up";

echo "Motor 1";
gpio write 0 0
sleep 1
gpio write 0 1
echo "Pin 0 Tested";

echo "Motor 2";
gpio write 1 0 
sleep 1
gpio write 1 1
echo "Pin 2 Tested";

echo "Motor 3";
gpio write 2 0 
sleep 1
gpio write 2 1
echo "Pin 3 Tested";

echo "Motor 4";
gpio write 3 0
sleep 1
gpio write 3 1
echo "Pin 4 Tested";

echo " Robot Pi Bash Script Complete";
