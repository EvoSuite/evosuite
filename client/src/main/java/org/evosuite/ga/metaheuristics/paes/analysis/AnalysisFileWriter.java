package org.evosuite.ga.metaheuristics.paes.analysis;

import org.evosuite.Properties;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by Sebastian on 13.07.2018.
 */
public class AnalysisFileWriter {
    private final String fileName;
    private List<String> jsonRepresentations;

    public AnalysisFileWriter(String fileName){
        this.fileName = fileName;
        this.jsonRepresentations = new ArrayList<>();
    }

    private void add(GenerationAnalysis analysis){
        this.jsonRepresentations.add(analysis.toJSON());
    }

    public void addAll(Collection<GenerationAnalysis> analyses){
        for(GenerationAnalysis analysis : analyses)
            this.add(analysis);
    }

    private String jsonRepresentationsToJsonArray(){
        StringBuilder stringBuilder = new StringBuilder("[\n");
        String lastJsonRepresentation = this.jsonRepresentations.remove(this.jsonRepresentations.size()-1);
        for(String jsonRepresentation: this.jsonRepresentations)
            stringBuilder.append(jsonRepresentation+",\n");
        stringBuilder.append(lastJsonRepresentation+"\n");
        stringBuilder.append("]");
        return stringBuilder.toString();
    }

    public void writeToDisc() throws IOException {
        String jsonArray = this.jsonRepresentationsToJsonArray();
        if(this.fileName == null || this.fileName == "")
            throw new IllegalStateException("fileName not defined");
        int run = 1;
        while(new File(Properties.REPORT_DIR, this.fileName+"-"+run+".json").exists())
            ++run;
        File file = new File(Properties.REPORT_DIR+File.separator+this.fileName+"-"+run+".json");
        file.getParentFile().mkdirs();
        file.createNewFile();
        FileWriter fileWriter = new FileWriter(file,false);
        fileWriter.write(jsonArray);
        fileWriter.close();
    }
}
