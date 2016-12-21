package com.zoyi.kona.job.exp.ajou;

import com.zoyi.kona.job.session.SessionsDownloadMapReduce;
import com.zoyi.kona.job.session.SessionsDownloadMapper;
import com.zoyi.util.env.Environment;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
import org.apache.hadoop.hbase.mapreduce.TableMapReduceUtil;
import org.apache.hadoop.hbase.mapreduce.TableMapper;
import org.apache.hadoop.hbase.mapreduce.TableReducer;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;

import java.io.IOException;

import static com.zoyi.kona.job.util.ConfigurationUtils.injectDefaultsInto;
import static com.zoyi.kona.job.util.ConfigurationUtils.injectInto;
import static com.zoyi.kona.lib.StringUtil.newJobTitle;

/**
 * Created by huy on 2016. 12. 22..
 */
public class FromHbase {
  class MyMapper extends TableMapper<Text, Text> {
    @Override
    protected void map(ImmutableBytesWritable key, Result value, Context context) throws IOException, InterruptedException {
      context.write(
          new Text(Bytes.toString(value.getRow())),
          new Text(Bytes.toString(value.getValue("device".getBytes(), "date".getBytes()))));
    }
  }

  public class MyReducer extends TableReducer<Text, Text, ImmutableBytesWritable> {
    @Override
    public void reduce(Text key, Iterable<Text> values, Context context) throws InterruptedException, IOException {
      int count = 0;
      for (Text value : values) {
        count++;
      }
      System.out.println(count);
    }
  }

  public static void main(String[] args) throws IOException, ClassNotFoundException, InterruptedException {
    final Configuration conf =
        injectDefaultsInto(injectInto(HBaseConfiguration.create()));
    conf.set("hbase.zookeeper.quorum", Environment.get("hbase.host"));

    final Job job = new Job(conf, "FromHbase Test MR");
    job.setJarByClass(SessionsDownloadMapReduce.class);
    Scan scan = new Scan();
    scan.setCaching(1024);
    scan.setCacheBlocks(false);

    TableMapReduceUtil.initTableMapperJob(
        "device_visit",
        scan,
        MyMapper.class,
        Text.class,
        Text.class,
        job);
    TableMapReduceUtil.initTableReducerJob(
        "device_visit",
        MyReducer.class,
        job
    );
    job.submit();
  }
}
