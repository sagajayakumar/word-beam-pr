
package org.apache.beam.examples;

// beam-playground:
//   name: MinimalWordCount
//   description: An example that counts words in Shakespeare's works.
//   multifile: false
//   pipeline_options:
//   categories:
//     - Combiners
//     - Filtering
//     - IO
//     - Core Transforms

import java.util.Arrays;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.io.TextIO;
import org.apache.beam.sdk.options.PipelineOptions;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.transforms.Count;
import org.apache.beam.sdk.transforms.Filter;
import org.apache.beam.sdk.transforms.FlatMapElements;
import org.apache.beam.sdk.transforms.Flatten;
import org.apache.beam.sdk.transforms.GroupByKey;
import org.apache.beam.sdk.transforms.MapElements;
import org.apache.beam.sdk.values.KV;
import org.apache.beam.sdk.values.PCollection;
import org.apache.beam.sdk.values.PCollectionList;
import org.apache.beam.sdk.values.TypeDescriptor;
import org.apache.beam.sdk.values.TypeDescriptors;

public class MinimalPageRankJaya {

  public static void main(String[] args) {
    PipelineOptions options = PipelineOptionsFactory.create();
    Pipeline p = Pipeline.create(options);
    String dataFolder = "web04";

    PCollection<KV<String, String>> PCollectionskvLinksKV1 = jayaMapJob(p, "go.md", dataFolder);
    PCollection<KV<String, String>> PCollectionskvLinksKV2 = jayaMapJob(p, "java.md", dataFolder);
    PCollection<KV<String, String>> PCollectionskvLinksKV3 = jayaMapJob(p, "python.md", dataFolder);
    PCollection<KV<String, String>> PCollectionskvLinksKV4 = jayaMapJob(p, "ReadMe.md", dataFolder);


    PCollectionList<KV<String, String>> result = PCollectionList.of(PCollectionskvLinksKV1).and(PCollectionskvLinksKV2).and(PCollectionskvLinksKV3).and(PCollectionskvLinksKV4);

    

    PCollection<KV<String, String>> mergedList = result.apply(Flatten.<KV<String,String>>pCollections());

    PCollection<KV<String,  Iterable<String>>> group = mergedList.apply(GroupByKey.create());
    PCollection<String> pckvStrings =  group.apply(
      MapElements.into(  
        TypeDescriptors.strings())
          .via((resultMergeLstout) -> resultMergeLstout.toString()));
             pckvStrings.apply(TextIO.write().to("jayacounts"));
        p.run().waitUntilFinish();
  }

 private static PCollection<KV<String, String>> jayaMapJob(Pipeline p, String dataFile, String Path) {

    String dp = Path + "/" +  dataFile;
    PCollection<String> PCILines =  p.apply(TextIO.read().from(dp));
    PCollection<String> PCLines  = PCILines.apply(Filter.by((String line) -> !line.isEmpty()));
    PCollection<String> PCIEmptyLines = PCLines.apply(Filter.by((String line) -> !line.equals(" ")));
    PCollection<String> PCILLines = PCIEmptyLines.apply(Filter.by((String line) -> line.startsWith("[")));
   
    PCollection<String> PCLinks = PCILLines.apply(
            MapElements.into(TypeDescriptors.strings())
                .via((String linelink) -> linelink.substring(linelink.indexOf("(")+1,linelink.indexOf(")")) ));

                PCollection<KV<String, String>> kv = PCLinks.apply(
                  MapElements.into(  
                    TypeDescriptors.kvs(TypeDescriptors.strings(), TypeDescriptors.strings()))
                      .via (linelink ->  KV.of(dataFile , linelink) ));              
    return kv;
  }
}