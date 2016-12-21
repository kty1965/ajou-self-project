package com.zoyi.kona.job.exp.ajou;

import com.zoyi.kona.job.exp.tf.TFCountingMR;
import com.zoyi.kona.job.exp.tf.TFMapReduce;
import com.zoyi.util.env.Environment;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.HBaseAdmin;
import org.apache.hadoop.hbase.mapreduce.TableMapReduceUtil;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.util.Time;

import java.io.IOException;

/**
 * Created by huy on 2016. 11. 29..
 */
public class FromHfileToHBase {

  private static void createHbaseTable(Configuration hbaseConfig, String table) throws IOException {
    final TableName tableName = TableName.valueOf(table);

    final HTableDescriptor tableDescriptor = new HTableDescriptor(tableName);
    tableDescriptor.addFamily(new HColumnDescriptor("value"));

    new HBaseAdmin(hbaseConfig).createTable(tableDescriptor);
  }

  public static void runCounting(String input) throws IOException, ClassNotFoundException, InterruptedException {
    String countingStore = String.format("from_hfile_to_hbase_%d", Time.now());
    Configuration conf = new Configuration();
    conf.set("hbase.zookeeper.quorum", Environment.get("hbase.host"));
    conf.setBoolean("mapreduce.input.fileinputformat.input.dir.recursive", true);
    createHbaseTable(conf, countingStore);
    Job job = Job.getInstance(conf, FromHfileToHBase.class.getName());
    job.setJarByClass(FromHfileToHBase.class);
    job.setMapperClass(TFCountingMR.MyMapper.class);
    TableMapReduceUtil.initTableReducerJob(
        countingStore,
        TFCountingMR.MyReducer.class,
        job
    );
    job.setOutputKeyClass(Text.class);
    job.setOutputValueClass(Text.class);
    FileInputFormat.addInputPath(job, new Path(input));
    job.waitForCompletion(true);
  }

  public static void main(String[] args) throws Exception {
    runCounting(args[0]);
  }
}
