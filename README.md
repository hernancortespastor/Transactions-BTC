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
2. Modificar la configuración de Kafka para poder acceder desde fuera de la instancia.

```
./confluent-5.5.0/etc/kafka/server.properties 
```
3. Modificar la siguiente linea con la IP publica de la instancia EC2.

```
 advertised.listeners=PLAINTEXT://<Public IPv4 DNS>:9092
```

4. Arrancar todos los servicios de Confluent. 

```
./confluent-5.5.0/bin/confluent local start
```
Resultado
```
Starting kafka
kafka is [UP]
Starting schema-registry
schema-registry is [UP]
Starting kafka-rest
kafka-rest is [UP]
Starting ksql-server
ksql-server is [UP]
Starting control-center
control-center is [UP]
```

5. Configuración de Kafka Connect para que transmita todos los datos que recibidos en un topic a un bucket de AWS S3. 
Se configuran las claves de acceso de AWS.
```
aws configure
```
6. Se crea un archivo JSON con la configuración correspondiente para posteriormente descargarlo desde la instancia y desplegarlo. Se ha configurado el conector para que los datos del topic de Kafka 'Transactions-raw' se vuelquen en el bucket de S3 denominado 'transactionsproject2020'.

```
wget https://github.com/hernancortespastor/Transactions-BTC/blob/main/kafka%20connect/transactions-to-s3.json
curl -s -X POST -H 'Content-Type: application/json' --data @transactions-to-s3.json http://localhost:8083/connectors
```
7. Verificamos que está en estado 'RUNNING'

```
curl localhost:8083/connectors/transactions-to-s3/status

```

8. 





