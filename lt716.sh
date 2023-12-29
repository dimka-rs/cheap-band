#!/bin/bash
# see https://xor.co.za/post/2022-11-30-hacking-smartwatch/
# FF:FF:FF:21:8B:63 LT716

if [ -z "$1" ]; then
    echo "Scanning..."
    MAC=`sudo hcitool lescan | grep -m 1 LT716 | awk '{print $1}'`
    [ -z "$MAC" ] && echo "Failed to find LT716" && exit 1
else
    MAC=$1
fi

#echo "Primary:"
#gatttool --device=$MAC --primary

#echo "Chardesc:"
#gatttool --device=$MAC --char-desc

#echo "Firmware Revision:"
#echo `gatttool --device=$MAC --char-read --uuid=0x2a26 |sed -e 's/^.*://' |xxd -r -p`

#echo "Software Revision:"
#echo `gatttool --device=$MAC --char-read --uuid=0x2a28 |sed -e 's/^.*://' |xxd -r -p`

#echo "Battery level:"
#BAT=`gatttool --device=$MAC --char-read --uuid=0x2a19 |sed -e 's/^.*://'`
#echo $((16#${BAT}))

echo "Vibrate:"
gatttool --device=$MAC --char-write-req --handle=0x0033 --value=CD000612010B000101
#HANDLE=`gatttool --device=$MAC --char-desc|grep 6e400002-b5a3-f393-e0a9-e50e24dcca9d|awk '{print $3}'`
#if [ -n "$HANDLE" ]; then
#    echo "Handle: $HANDLE"
#    echo $HANDLE |hexdump
#    gatttool --device=$MAC --char-write-req --handle=$HANDLE --value=CD000612010B000101
#else
#    echo "FAIL"
#fi

echo "Notify:"
gatttool --device=$MAC --char-write-req --handle=0x0033 --value=CD0011120107000C010101010101010101010101
gatttool --device=$MAC --char-write-req --handle=0x0033 --value=CD001F120112001A08000043657265616C3A4861
gatttool --device=$MAC --char-write-req --handle=0x0033 --value=636B2074686520506C616E6574


