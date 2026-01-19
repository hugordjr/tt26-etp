#!/bin/sh
printf "192.168.0.241 localhost\n" > /etc/hosts.tmp
cat /etc/hosts >> /etc/hosts.tmp
mv /etc/hosts.tmp /etc/hosts
exec /usr/local/bin/garage-simulator
