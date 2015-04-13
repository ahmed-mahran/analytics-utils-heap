package com.badrit.analytics.R;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import scala.Tuple2;

public class R {

    public static void execRScript(String script) {
        try {
            
            String scriptFileName = "tmp-" + System.nanoTime() + ".r";
            File scriptFile = new File(scriptFileName);
            
            {
                PrintWriter pw = new PrintWriter(scriptFile);
                pw.println(script);
                pw.flush();
                pw.close();
            }
            
            // prepare the R calling command
            String rCommand = "Rscript";
            String rOptions = "--vanilla";
            String rExecScript = scriptFile.getAbsolutePath();
            
            ProcessBuilder pb = new ProcessBuilder(rCommand, rOptions, rExecScript);
            
            // start the R process and capture the output and error streams
            long rt0 = System.currentTimeMillis();
            Process rProcess = pb.start();
            final BufferedReader br  = new BufferedReader(new InputStreamReader(rProcess.getInputStream()));
            final BufferedReader ebr = new BufferedReader(new InputStreamReader(rProcess.getErrorStream()));
            
            final ArrayList<String> res = new ArrayList<String>();
            
            // R output reader thread
            new Thread(new Runnable() {
                public void run() {
                    try {
                        String line;
                        while(null != (line = br.readLine()))
                            res.add(line);
                    } catch(Throwable t) {}
                }
            }, "R-stdout-reader-" + scriptFileName)
            .start();
            
            final StringBuffer errSb = new StringBuffer("R error:\r\n");
            
            // R error reader thread
            new Thread(new Runnable() {
                public void run() {
                    try {
                        String line = "";
                        while(null != (line = ebr.readLine()))
                            errSb.append(line + "\r\n");
                    } catch(Throwable t) {}
                }
            }, "R-stderr-reader-" + scriptFileName)
            .start();
            
            int returnResult = rProcess.waitFor();
            
            System.out.println("R Process Ended:" + "\r\n"
                    + "\treturn code: " + returnResult  + "\r\n"
                    + "\ttotal time: " + (System.currentTimeMillis() - rt0) + " Millis" + "\r\n"
                    + "R output size: " + res.size());
            
            if(0 != returnResult) {
                for(String line: res)
                    errSb.append(line + "\r\n");
                System.out.println(errSb.toString());
            }
            
            scriptFile.deleteOnExit();
            
            br.close();
            ebr.close();
            
        } catch(Throwable t) {
            t.printStackTrace();
        }
    }
    
    public static <T> void hist(List<T> data, String title, String path) {
        
        StringBuffer rScript = new StringBuffer();
        
        Iterator<T> iter = data.iterator();
        
        rScript.append("x <- c(");
        while(iter.hasNext()) rScript.append(iter.next()).append(",");
        rScript.deleteCharAt(rScript.length() - 1);
        rScript.append("); png(\"" + path + "\", width = 1024, height = 1024); hist(x, main = '" + title + "'); dev.off();");
        
        execRScript(rScript.toString());
        
    }
    
    public static <T> void density(List<T> data, String title, String path) {
        
        StringBuffer rScript = new StringBuffer();
        
        Iterator<T> iter = data.iterator();
        
        rScript.append("x <- c(");
        while(iter.hasNext()) rScript.append(iter.next()).append(",");
        rScript.deleteCharAt(rScript.length() - 1);
        rScript.append("); png(\"" + path + "\", width = 1024, height = 1024); plot(density(x), main = '" + title + "'); dev.off();");
        
        execRScript(rScript.toString());
        
    }
    
    public static <T> void boxplot(List<T> data, String title, String path) {
        
        StringBuffer rScript = new StringBuffer();
        
        Iterator<T> iter = data.iterator();
        
        rScript.append("x <- c(");
        while(iter.hasNext()) rScript.append(iter.next()).append(",");
        rScript.deleteCharAt(rScript.length() - 1);
        rScript.append("); png(\"" + path + "\", width = 1024, height = 1024); boxplot(x, main = '" + title + "'); dev.off();");
        
        execRScript(rScript.toString());
        
    }
    
    public static void timeseries(List<Tuple2<Long, Integer>> timeseries, String title, String path) {
        
        StringBuffer rScript = new StringBuffer();
        
        Iterator<Tuple2<Long, Integer>> iter = timeseries.iterator();
        
        rScript.append("x <- ts(c(");
        while(iter.hasNext()) rScript.append(iter.next()._2).append(",");
        rScript.deleteCharAt(rScript.length() - 1);
        rScript.append(")); png(\"" + path + "\", width = 1024, height = 1024); plot(x, main = '" + title + "'); dev.off();");
        
        execRScript(rScript.toString());
        
    }
    
}
