2016-2학기 자기주도 프로젝트 김태영(TaeYoung Kim)

# HFile / Hbase를 이용한 Hadoop MapReduce와 Spark의 성능 분석

## 분석 데이터 요건
분석에 이용한 데이터는 아래와 같습니다.

### HFile data schema
2635개의 HFile</br>
`Timestamp(millisecond)`/`Random id`/`Random integer`

```
1480138679886823,3AB9F060105E1668308B2D8F80E67EE8,-73
1480138679933930,5D9980748E5B02185CF73797BCB25BCE,-73
1480138679924839,AD15077A9CCC60D5F0294916B671F149,-80
...
```

### Hbase data schema
2억건 이상의 `Random id`</br>
rowKey: `Random id` </br>
column: `device:date` / `string date`
```
 00000008a9f02e73939c923f2af24f72 column=device:date, timestamp=1482336327279, value=2015-12-26
 000000346dcb8dd0822193e3852d01a9 column=device:date, timestamp=1482335733386, value=2015-01-15
 0000003b19399118f5262d0ce7e4ca9d column=device:date, timestamp=1482335784366, value=2016-03-05
 0000003b19399118f5262d0ce7e4ca9e column=device:date, timestamp=1482335831435, value=2016-04-08
 0000005c86e447c7e7e4e7ebb1f42e01 column=device:date, timestamp=1482335367490, value=2014-01-26
 ...
 ```
 
## 분석 방법
- Hfile 데이터를 이용 해서는 Timestamp로 시간을 알아낸 뒤 시간대별 unique id를 counting하여 Hbase,Hfile로 저장하여 진행 하였다.
- Hbase 데이터를 이용 해서는 count(uniq id)를 하여 진행 하였다.

## 분석 결과

## 분석 환경설정 및 주요 환경설정 설명
### Spark(1.6.0)
- spark.dynamicAllocation.initialExecutors: 32
- spark.dynamicAllocation.minExecutors: 32
- spark.dynamicAllocation.maxExecutors: 256
- spark.executor.memory: 8G
- spark.executor.cores: 2
- spark.driver.memory: 8G
- spark.driver.cores: 2
- spark.task.cpus: 2

property | Description
--- | ---
spark.dynamicAllocation.initialExecutors | dynamic하게 executor갯수를 조절 할 수 있는데 처음 초기 갯수를 뜻한다.주로 minExecutors와 똑같다.
spark.dynamicAllocation.minExecutors | default는 0, 어느정도 제한을 두는게 좋다.
spark.dynamicAllocation.maxExecutors | default는 infinity, 어느정도 제한을 두는게 좋다.
spark.executor.memory | 각 Executor마다 할당되는 메모리를 뜻한다.
spark.executor.cores | 각 Executor마다 할당되는 코어갯수를 뜻한다.
spark.driver.memory | Executors들을 관리하는 Driver의 메모리를 뜻한다.
spark.driver.cores | Executors들을 관리하는 Driver의 코어갯수를 뜻한다.
spark.task.cpus | executor가 할당 받은 코어를 task당 몇개의 코어를 쓸지 뜻한다. 위와 같이 설정될 경우 8G/2core executor는 8G/2core의 task 1개만 수행하게 된다.

### Hbase(1.2.0)
- hbase.hregion.memstore.flush.size: 256MB
- hbase.hregion.memstore.block.multiplier: 8
- hbase.hregion.max.filesize: 10G
- Java Heap Size of HBase RegionServer in Bytes: 32G

property | Description
--- | ---
hbase.hregion.memstore.flush.size | hbase에 writing이 될때 중요한 변수이다. 메모리 스토어에 저장하고 있는 크기이다. flush가 일어날 경우 해당 메모리 스토어에 있는 데이터를 Hfile로 변환하여 hdfs에 저장하게 된다.
hbase.hregion.memstore.block.multiplier | 한 memstore에서 동시에 관리하는 Column family 갯수라 보면 된다. 관리하는 곳 n개의 메모리 합이 일정 이상 일 경우 flush가 일어나게 된다.
hbase.hregion.max.filesize | hbase에 제일 중요한 property이다 hbase도 수많은 hfile로 저장 되지만 그 hfile의 집합으로 hregion이 하나씩 만들어진다. 이 hregion의 최대 파일 크기를 말한다. 더 나아가 Input split을 결정할때 hregion이 사용되므로, 너무 작을 경우에는 input split이 많아져 ini타임이 길어 질 수있다. 너무 클 경우에는(예를 들면 100G일 경우) Mapper에서 input split 읽을때 최대 100G의 파일을 읽어야 할 수도 있다.
Java Heap Size of HBase RegionServer in Bytes | hbase region server는 여러개의 hregion들로 이루어져 있는데 이 hregion들이 가지고 있는 hfile들의 정보를 InMemory로 가지고 있어야 한다. Hfile의 갯수가 많아질 수록 hbase region server의 Java Heap 용량은 커져야 한다.

### HDFS(2.6.0)
- dfs.block.size, dfs.blocksize: 128MB
- dfs.client.socket-timeout: 60000 (msec)
- dfs.datanode.socket.write.timeout: 240000(msec)
- ipc.client.connect.max.retries: 5
- dfs.datanode.handler.count: 64

property | Description
--- | ---
dfs.block.size, dfs.blocksize | Hfile또한 여러개의 block들로 이루어질 수 있는데 한 블럭당의 크기를 의미하며, 너무 작을 경우 Hfile수가 증가하여 Hbase Region server에 java heap 메모리에 영향을 끼칠 수 있다.
dfs.client.socket-timeout | HDFS server를 직접 연결하여 사용하는 client(ex: hbase)들의 timeout 정보이다.
dfs.datanode.socket.write.timeout | HDFS에 write가 많이 몰릴 경우 write block이 일어나 write하는데 시간이 오래 걸릴 수 있는데, 이때 기다리는 타임아웃 시간이다.
ipc.client.connect.max.retries | client와 연결되는 맥스 리트라이수다. 위 write.timeout이나 socket-timeout으로 인해 이 property이상 fail할 경우 client는 exit된다.
dfs.datanode.handler.count | write나 read가 hdfs (hfile)에 요청이 오게 될 경우 하나의 thread가 담당하여 처리하게 되는데, 이 thread의 수를 의미한다.

### Yarn(2.6.0)
- mapreduce.map.memory.mb: 4G
- mapreduce.map.cpu.vcores: 1
- mapreduce.reduce.memory.mb: 4G
- mapreduce.reduce.cpu.vcores: 1

property | Description
--- | ---
mapreduce.map.memory.mb | 맵리듀스의 Mapper당 할당되는 메모리 (Mapper는 Input Split 갯수만큼 생성 된다)
mapreduce.map.cpu.vcores | 맵리듀스의 Mapper당 할당되는 코어수
mapreduce.reduce.memory.mb | 맵리듀스의 Reducer당 할당되는 메모리 (Reducer는 자동으로 수가 결정되지 않으며 default 값이나, 임의로 설정해줘야 한다)
mapreduce.reduce.cpu.vcores | 맵리듀스의 Reduce당 할당되는 코어 수
