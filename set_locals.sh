#!/usr/bin/env bash
echo "Set local variables before start App"
#echo "don't forget this command: chmod +x set_locals.sh"



export CASSANDRA_ADDRESS='localhost'
echo "CASSANDRA_ADDRESS=$CASSANDRA_ADDRESS"

export CASSANDRA_PORT='9042'
echo "CASSANDRA_PORT=$CASSANDRA_PORT"

export CASSANDRA_KEYSPACE='file_io'
echo "CASSANDRA_KEYSPACE=$CASSANDRA_KEYSPACE"

export CASSANDRA_LOGIN=''
echo "CASSANDRA_LOGIN=$CASSANDRA_LOGIN"

export CASSANDRA_PASSWORD=''
echo "CASSANDRA_PASSWORD=$CASSANDRA_PASSWORD"



export WEBSERVER_ADDRESS='0.0.0.0'
echo "WEBSERVER_ADDRESS=$WEBSERVER_ADDRESS"

export WEBSERVER_PORT=8101
echo "WEBSERVER_PORT=$WEBSERVER_PORT"



export KAMON_STATSD_HOSTNAME='127.0.0.1'
echo "KAMON_STATSD_HOSTNAME=$KAMON_STATSD_HOSTNAME"

export KAMON_STATSD_PORT=8125
echo "KAMON_STATSD_PORT=$KAMON_STATSD_PORT"