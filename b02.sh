#!/bin/bash
# 02:FC:25:D7:6F:E3 B02
DEVNAME=B02
WRITE_CHR="6E400002-B5A3-F393-E0A9-E50E24DCCA9F";

if [ -z "$1" ]; then
    echo "Scanning..."
    MAC=`sudo timeout -s INT 3s hcitool lescan | grep ${DEVNAME} | head -n1 | awk '{print $1}'`
    [ -z "$MAC" ] && echo "Failed to find $DEVNAME" && exit 1
    echo "MAC=${MAC}"
else
    MAC=$1
fi

echo "Battery level:"
BAT=`gatttool --device=$MAC --char-read --uuid=0x2a19 |sed -e 's/^.*://'`
echo "`echo \"ibase=16; $BAT\"|bc` %"


# 0034 - handle for write characteristic
# msg: type+sender+:+string
# type 1 - sms
echo "End call:"
gatttool --device=$MAC --char-write-req --handle=0x0034 --value=DF0005F80201110000


#echo "Primary:"
#gatttool --device=$MAC --primary

#echo "Chardesc:"
#gatttool --device=$MAC --char-desc

#echo "Firmware Revision:"
#echo `gatttool --device=$MAC --char-read --uuid=0x2a26 |sed -e 's/^.*://' |xxd -r -p`

#echo "Software Revision:"
#echo `gatttool --device=$MAC --char-read --uuid=0x2a28 |sed -e 's/^.*://' |xxd -r -p`


#echo "Vibrate:"
#gatttool --device=$MAC --char-write-req --handle=0x0033 --value=CD000612010B000101
#HANDLE=`gatttool --device=$MAC --char-desc|grep 6e400002-b5a3-f393-e0a9-e50e24dcca9d|awk '{print $3}'`
#if [ -n "$HANDLE" ]; then
#    echo "Handle: $HANDLE"
#    echo $HANDLE |hexdump
#    gatttool --device=$MAC --char-write-req --handle=$HANDLE --value=CD000612010B000101
#else
#    echo "FAIL"
#fi

#echo "Notify:"
#gatttool --device=$MAC --char-write-req --handle=0x0033 --value=CD0011120107000C010101010101010101010101
#gatttool --device=$MAC --char-write-req --handle=0x0033 --value=CD001F120112001A08000043657265616C3A4861
#gatttool --device=$MAC --char-write-req --handle=0x0033 --value=636B2074686520506C616E6574


