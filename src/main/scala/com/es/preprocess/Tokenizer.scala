package com.es.preprocess

import com.es.util.DataFrameUtil
import org.apache.spark.ml.feature.Tokenizer
import org.apache.spark.sql.SQLContext
import org.apache.spark.{SparkConf, SparkContext}
import scopt.OptionParser

/**
  * Created by mick.yi on 2017/12/19.
  * 分词器，将句子分割为词(根据标点符号,空格等)
  */
object Tokenizer {

  /** 命令行参数 */
  case class Params(input: String = "", //输入数据,parquet格式
                    output: String = "", //输出数据,parquet格式
                    inputCol: String = "", //分词列
                    outputCol: String = "", //分词结果输出列
                    resultCols: String = "", //输出结果保留的列,默认全部输出
                    appName: String = "Tokenizer"
                   )

  def main(args: Array[String]) {
    if (args.length < 5) {
      System.err.println("Usage: <file>")
      System.exit(1)
    }

    val default_params = Params()
    val parser = new OptionParser[Params]("Tokenizer") {
      head("Tokenizer:.")
      opt[String]("input")
        .required()
        .text("输入数据")
        .action((x, c) => c.copy(input = x))
      opt[String]("output")
        .required()
        .text("输出数据")
        .action((x, c) => c.copy(output = x))
      opt[String]("appName")
        .required()
        .text("appName")
        .action((x, c) => c.copy(appName = x))
      opt[String]("inputCol")
        .required()
        .text("分词列")
        .action((x, c) => c.copy(inputCol = x))
      opt[String]("outputCol")
        .required()
        .text("分词结果输出列")
        .action((x, c) => c.copy(outputCol = x))
      opt[String]("resultCols")
        .required()
        .text("输出结果保留的列")
        .action((x, c) => c.copy(resultCols = x))
    }
    parser.parse(args, default_params).map { params =>
      run(params)
    } getOrElse {
      System.exit(1)
    }

  }

  def run(p: Params): Unit = {
    val conf = new SparkConf().setAppName(p.appName)
    val sc = new SparkContext(conf)
    val sqlContext = new SQLContext(sc)

    val inputDF = sqlContext.read.parquet(p.input)
    //定义分词器
    val tokenizer = new Tokenizer().
      setInputCol(p.inputCol).
      setOutputCol(p.outputCol)

    //转换数据
    val outputDF = tokenizer.transform(inputDF)

    //保存结果
    val resultDF = DataFrameUtil.select(outputDF, p.resultCols) //只保存选择的列
    resultDF.write.parquet(p.output)

    sc.stop()
  }

}
