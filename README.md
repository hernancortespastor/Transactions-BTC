# Transactions-BTC

Proyecto de final de Master de Arquitectura Big Data de Kschool.

## Arquitectura



## Requisitos previos.
* Registro en AWS.

* Creación de bucket 'transactionsproject2020' en S3.

* Lanzamiento de instancia de EC2 Amazon Linux 2 desde donde se desplegará Kafka. Se configura la seguridad añadiendo 'Inbound rules' para el puerto TCP 9092 permitiendo el acceso desde otras instancias. 

* Lanzamiento de instancia de EC2 Amazon Linux 2 desde donde se desplegará Apache Druid. Se configura la seguridad añadiendo 'Inbound rules' para el puerto TCP 8888 permitiendo el acceso desde otras instancias. 

* Lanzamiento de instancia de EC2 Amazon Linux 2 desde donde se desplegará SuperSet. Se configura la seguridad añadiendo 'Inbound rules' para el puerto TCP 8088 permitiendo el acceso desde otras instancias. 

## Instalación y despliegue.

### 1. Kafka.
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

Configuración de Kafka Connect para que transmita todos los datos que recibidos en un topic a un bucket de AWS S3. 
Se configuran las claves de acceso de AWS.
```
aws configure
```
Se crea un archivo JSON con la configuración correspondiente para posteriormente descargarlo desde la instancia y desplegarlo. Se ha configurado el conector para que los datos del topic de Kafka 'transactions-raw' se vuelquen en el bucket de S3 denominado 'transactionsproject2020't

```
wget https://github.com/hernancortespastor/Transactions-BTC/blob/main/kafka%20connect/transactions-to-s3.json
curl -s -X POST -H 'Content-Type: application/json' --data @transactions-to-s3.json http://localhost:8083/connectors
```
Se verifique que está en estado 'RUNNING'

```
curl localhost:8083/connectors/transactions-to-s3/status

```

### 2. Web Socket.

Para comenzar a enviar datos al topic 'transactions-raw' se ejecuta la siguiente aplicación.
```
https://github.com/hernancortespastor/WebSocketTransactionsBTC
```

### 2. Spark.

Descargar repositorio:
```
git clone https://github.com/hernancortespastor/Transactions-BTC
```

Abrir el proyecto  en Intellij seleccionando el fichero 'build.sbt' del repositorio descargado.

Abrir 'Sbt Shell' de Intellij y ejecutar:
```
package
```
Se genera un .jar que se lanzará en un cluster EMR. 
Desde AWS creamos un cluster EMR con la configuración por defecto.

![alt text](https://github.com/hernancortespastor/Transactions-BTC/blob/main/img/Selection_002.png)


Desde EMR se lanzará el job del jar con la siguiente configuración.

* Job Spark Structured Streaming

```
spark-submit --deploy-mode cluster
--packages org.apache.spark:spark-sql-kafka-0-10_2.11:2.4.6 
--class SparkTransactionsMain s3://transactionsprojectjarfiles/sparktransactions_2.11-0.1.jar yarn <Public IPv4 Kafka>:9092

```
* Job Spark Batch

```
spark-submit --deploy-mode cluster
--packages org.apache.spark:spark-sql-kafka-0-10_2.11:2.4.6 
--class SparkBatchTransactionsMain s3://transactionsprojectjarfiles/sparktransactions_2.11-0.1.jar yarn <Public IPv4 Kafka>:9092

```

### 3. Druid.

Se desplegará Apache Druid desde una instancia EC2 de AWS. Se accede mediante SSH a la instacia y se ejecutan las siguientes instrucciones.

```
wget https://ftp.cixug.es/apache/druid/0.20.0/apache-druid-0.20.0-bin.tar.gz
tar -xzf apache-druid-0.20.0-bin.tar.gz
apache-druid-0.20.0/bin/start-single-server-small

```
Desde un navegador se accede a Druid en:

```
<Public IPv4 address instancia Druid>:8888
```

Desde la interfaz gráfica de Druid se generan los diferentes Datasources cargando los datos desde los diferentes topics de Kafka generados en Spark.


### 4. Superset.
Se desplegará Superset desde una instancia EC2 de AWS. Se accede mediante SSH a la instacia y se realiza la instalación siguiendo los pasos de la web oficial.

Una vez instalado se lanzará mediante la siguiente instrucción.
```
superset run -h 0.0.0.0 -p 8088 --with-threads --reload --debugger
```

Desde un navegador se accede a Superset en:

```
 <Public IPv4 address instancia Superset>:8088
 ```


Se añade Druid como nueva base de datos. Se requiere la siguiente URI.

```
druid://<Public IPv4 address instancia Druid>:8888/druid/v2/sql
```












