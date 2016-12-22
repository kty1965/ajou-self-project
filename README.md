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
