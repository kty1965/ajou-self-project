package com.zoyi.kona.job.exp.ajou;

import com.zoyi.kona.job.exp.tf.TFCountingMR;
import com.zoyi.kona.job.exp.tf.TFMapReduce;
import com.zoyi.kona.lib.StringUtil;
import com.zoyi.util.env.Environment;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.HBaseAdmin;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
import org.apache.hadoop.hbase.mapreduce.TableMapReduceUtil;
import org.apache.hadoop.hbase.mapreduce.TableReducer;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.Time;

import java.io.IOException;
import java.util.Set;
import java.util.TreeSet;

/**
 * Created by huy on 2016. 11. 29..
 */
public class FromHfileToHFile {
  public static class MyReducer extends Reducer<Text, Text, Text, Text> {

    public void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
      Set<String> devices = new TreeSet();
      for (Text value : values) {
        devices.add(value.toString());
      }
      context.write(new Text(key.toString()), new Text(String.valueOf(devices.size())));
    }
  }

  public static void runCounting(String input) throws IOException, ClassNotFoundException, InterruptedException {
    Configuration conf = new Configuration();
    conf.set("hbase.zookeeper.quorum", Environment.get("hbase.host"));
    conf.setBoolean("mapreduce.input.fileinputformat.input.dir.recursive", true);


    Job job = Job.getInstance(conf, FromHfileToHFile.class.getName());
    job.setJarByClass(FromHfileToHFile.class);
    job.setMapperClass(TFCountingMR.MyMapper.class);
    job.setOutputKeyClass(Text.class);
    job.setOutputValueClass(Text.class);
    FileInputFormat.addInputPath(job, new Path(input));

    job.setReducerClass(MyReducer.class);
    FileOutputFormat.setOutputPath(
        job,
        new Path("FromHfileToHFile")
    );
    job.waitForCompletion(true);
  }

  public static void main(String[] args) throws Exception {
    runCounting(args[0]);
  }
}
