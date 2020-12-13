# Transactions-BTC

Proyecto de final de Master de Arquitectura Big Data de Kschool.

## Esquema funcionamiento

## Instalación.

1. Kafka.
Se desplegará Kafka y Kafka Connect desde una instancia EC2 de AWS. Usaremos la version Community de Confluent. Se accede mediante SSH a la instacia y se ejecutan las siguientes instrucciones.
```
wget https://packages.confluent.io/archive/5.5/confluent-5.5.0-2.12.tar.gz
tar -xvzf confluent-5.5.0-2.12.tar.gz
```
Modificar la configuración de Kafka para poder acceder desde fuera de la instancia.

```
./confluent-5.5.0/etc/kafka/server.properties 
```
Modificar la siguiente linea con la IP publica de la instancia EC2.

```
 advertised.listeners=PLAINTEXT://<Public IPv4 DNS>:9092
```

Arrancar todos los servicios de Confluent. 

```
./confluent-5.5.0/bin/confluent local start
```



