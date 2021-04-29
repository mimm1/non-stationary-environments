package moa.tasks;
/*
 *    EvaluatePrequential.java
 *    Copyright (C) 2007 University of Waikato, Hamilton, New Zealand
 *    @author Richard Kirkby (rkirkby@cs.waikato.ac.nz)
 *    @author Albert Bifet (abifet at cs dot waikato dot ac dot nz)
 *
 *    This program is free software; you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation; either version 3 of the License, or
 *    (at your option) any later version.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with this program. If not, see <http://www.gnu.org/licenses/>.
 *    
 */


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;

import moa.classifiers.Classifier;
import moa.classifiers.MultiClassClassifier;
import moa.core.Example;
import moa.core.Measurement;
import moa.core.ObjectRepository;
import moa.core.TimingUtils;
import moa.evaluation.WindowClassificationPerformanceEvaluator;
import moa.evaluation.EWMAClassificationPerformanceEvaluator;
import moa.evaluation.FadingFactorClassificationPerformanceEvaluator;
import moa.evaluation.LearningCurve;
import moa.evaluation.LearningEvaluation;
import moa.evaluation.LearningPerformanceEvaluator;
import moa.learners.Learner;
import moa.learners.SSLearner;
import moa.options.ClassOption;

import com.github.javacliparser.FileOption;
import com.github.javacliparser.FlagOption;
import com.github.javacliparser.FloatOption;
import com.github.javacliparser.IntOption;
import moa.streams.ExampleStream;
import moa.streams.InstanceStream;

import com.yahoo.labs.samoa.instances.Instance;
import moa.core.Utils;

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;


/**
 * Task for evaluating a classifier on a stream by testing then training with each example in sequence.
 *
 * @author Richard Kirkby (rkirkby@cs.waikato.ac.nz)
 * @author Albert Bifet (abifet at cs dot waikato dot ac dot nz)
 * @version $Revision: 7 $
 */
public class EvaluatePrequential extends ClassificationMainTask {

    @Override
    public String getPurposeString() {
        return "Evaluates a classifier on a stream by testing then training with each example in sequence.";
    }

    private static final long serialVersionUID = 1L;

    public ClassOption learnerOption = new ClassOption("learner", 'l',
            "Learner to train.", MultiClassClassifier.class, "moa.classifiers.bayes.NaiveBayes");

    public ClassOption streamOption = new ClassOption("stream", 's',
            "Stream to learn from.", ExampleStream.class,
            "generators.RandomTreeGenerator");

    public ClassOption evaluatorOption = new ClassOption("evaluator", 'e',
            "Classification performance evaluation method.",
            LearningPerformanceEvaluator.class,
            "WindowClassificationPerformanceEvaluator");

    public IntOption instanceLimitOption = new IntOption("instanceLimit", 'i',
            "Maximum number of instances to test/train on  (-1 = no limit).",
            100000000, -1, Integer.MAX_VALUE);

    public IntOption timeLimitOption = new IntOption("timeLimit", 't',
            "Maximum number of seconds to test/train for (-1 = no limit).", -1,
            -1, Integer.MAX_VALUE);

    public IntOption sampleFrequencyOption = new IntOption("sampleFrequency",
            'f',
            "How many instances between samples of the learning performance.",
            100000, 0, Integer.MAX_VALUE);

    public IntOption memCheckFrequencyOption = new IntOption(
            "memCheckFrequency", 'q',
            "How many instances between memory bound checks.", 100000, 0,
            Integer.MAX_VALUE);

    public FileOption dumpFileOption = new FileOption("dumpFile", 'd',
            "File to append intermediate csv results to.", null, "csv", true);

    public FileOption outputPredictionFileOption = new FileOption("outputPredictionFile", 'o',
            "File to append output predictions to.", null, "pred", true);

    //New for prequential method DEPRECATED
    public IntOption widthOption = new IntOption("width",
            'w', "Size of Window", 1000);

    public FloatOption alphaOption = new FloatOption("alpha",
            'a', "Fading factor or exponential smoothing factor", .01);
    
    public FlagOption mixedOption = new FlagOption("mixed", 'm',
            "temp.");
    
    //End New for prequential methods

	private double acc = 0.0;
	private double size = 0.0;

	private double exec_time;



    @Override
    public Class<?> getTaskResultType() {
        return LearningCurve.class;
    }

    @Override
    protected Object doMainTask(TaskMonitor monitor, ObjectRepository repository) {
        Learner learner = (Learner) getPreparedClassOption(this.learnerOption);
        ExampleStream stream = (ExampleStream) getPreparedClassOption(this.streamOption);
		
        LearningPerformanceEvaluator evaluator = (LearningPerformanceEvaluator) getPreparedClassOption(this.evaluatorOption);
        LearningCurve learningCurve = new LearningCurve(
                "learning evaluation instances");

        //New for prequential methods
        if (evaluator instanceof WindowClassificationPerformanceEvaluator) {
            //((WindowClassificationPerformanceEvaluator) evaluator).setWindowWidth(widthOption.getValue());
            if (widthOption.getValue() != 1000) {
                System.out.println("DEPRECATED! Use EvaluatePrequential -e (WindowClassificationPerformanceEvaluator -w " + widthOption.getValue() + ")");
                 return learningCurve;
            }
        }
        if (evaluator instanceof EWMAClassificationPerformanceEvaluator) {
            //((EWMAClassificationPerformanceEvaluator) evaluator).setalpha(alphaOption.getValue());
            if (alphaOption.getValue() != .01) {
                System.out.println("DEPRECATED! Use EvaluatePrequential -e (EWMAClassificationPerformanceEvaluator -a " + alphaOption.getValue() + ")");
                return learningCurve;
            }
        }
        if (evaluator instanceof FadingFactorClassificationPerformanceEvaluator) {
            //((FadingFactorClassificationPerformanceEvaluator) evaluator).setalpha(alphaOption.getValue());
            if (alphaOption.getValue() != .01) {
                System.out.println("DEPRECATED! Use EvaluatePrequential -e (FadingFactorClassificationPerformanceEvaluator -a " + alphaOption.getValue() + ")");
                return learningCurve;
            }
        }
        //End New for prequential methods

        learner.setModelContext(stream.getHeader());
        int maxInstances = this.instanceLimitOption.getValue();
        long instancesProcessed = 0;
        int maxSeconds = this.timeLimitOption.getValue();
        int secondsElapsed = 0;
        monitor.setCurrentActivity("Evaluating learner...", -1.0);

        File dumpFile = this.dumpFileOption.getFile();
        PrintStream immediateResultStream = null;
        if (dumpFile != null) {
            try {
                if (dumpFile.exists()) {
                    immediateResultStream = new PrintStream(
                            new FileOutputStream(dumpFile, true), true);
                } else {
                    immediateResultStream = new PrintStream(
                            new FileOutputStream(dumpFile), true);
                }
            } catch (Exception ex) {
                throw new RuntimeException(
                        "Unable to open immediate result file: " + dumpFile, ex);
            }
        }
        //File for output predictions
        File outputPredictionFile = this.outputPredictionFileOption.getFile();
        PrintStream outputPredictionResultStream = null;
        if (outputPredictionFile != null) {
            try {
                if (outputPredictionFile.exists()) {
                    outputPredictionResultStream = new PrintStream(
                            new FileOutputStream(outputPredictionFile, true), true);
                } else {
                    outputPredictionResultStream = new PrintStream(
                            new FileOutputStream(outputPredictionFile), true);
                }
            } catch (Exception ex) {
                throw new RuntimeException(
                        "Unable to open prediction result file: " + outputPredictionFile, ex);
            }
        }
        boolean firstDump = true;
        boolean preciseCPUTiming = TimingUtils.enablePreciseTiming();
        long evaluateStartTime = TimingUtils.getNanoCPUTimeOfCurrentThread();
        long lastEvaluateStartTime = evaluateStartTime;
        double RAMHours = 0.0;
        while (stream.hasMoreInstances()
                && ((maxInstances < 0) || (instancesProcessed < maxInstances))
                && ((maxSeconds < 0) || (secondsElapsed < maxSeconds))) {
        	
        	//if (instancesProcessed==6)
     		  // System.out.println("train original ");
        	
            Example trainInst = stream.nextInstance();
            Example testInst = (Example) trainInst; //.copy();
            //testInst.setClassMissing();
            double[] prediction = learner.getVotesForInstance(testInst);
            // Output prediction
            if (outputPredictionFile != null) {
                int trueClass = (int) ((Instance) trainInst.getData()).classValue();
                outputPredictionResultStream.println(Utils.maxIndex(prediction) + "," + (
                 ((Instance) testInst.getData()).classIsMissing() == true ? " ? " : trueClass));
            }

            //evaluator.addClassificationAttempt(trueClass, prediction, testInst.weight());
            evaluator.addResult(testInst, prediction);
            learner.trainOnInstance(trainInst);
            instancesProcessed++;
            
           // System.out.println(instancesProcessed);
           // System.out.println(evaluator);
            
            if (instancesProcessed % this.sampleFrequencyOption.getValue() == 0
                    || stream.hasMoreInstances() == false) {
            	
          		//chunk finished
              //  System.out.println("***** Chunk finished");
                long evaluateTime = TimingUtils.getNanoCPUTimeOfCurrentThread();
                double time = TimingUtils.nanoTimeToSeconds(evaluateTime - evaluateStartTime);
                double timeIncrement = TimingUtils.nanoTimeToSeconds(evaluateTime - lastEvaluateStartTime);
                double RAMHoursIncrement = learner.measureByteSize() / (1024.0 * 1024.0 * 1024.0); //GBs
                RAMHoursIncrement *= (timeIncrement / 3600.0); //Hours
                RAMHours += RAMHoursIncrement;
                lastEvaluateStartTime = evaluateTime;
                learningCurve.insertEntry(new LearningEvaluation(
                        new Measurement[]{
                            new Measurement(
                            "learning evaluation instances",
                            instancesProcessed),
                            new Measurement(
                            "evaluation time ("
                            + (preciseCPUTiming ? "cpu "
                            : "") + "seconds)",
                            time),
                            new Measurement(
                            "model cost (RAM-Hours)",
                            RAMHours)
                        },
                        evaluator, learner));
               acc  += learningCurve.getMeasurement(learningCurve.numEntries()-1, 4);
               exec_time += learningCurve.getMeasurement(learningCurve.numEntries()-1, 1);
               // size  = size+ learningCurve.getMeasurement(learningCurve.numEntries()-1, 10);
          


                if (immediateResultStream != null) {
                    if (firstDump) {
                        immediateResultStream.println(learningCurve.headerToString());
                        firstDump = false;
                    }
                    immediateResultStream.println(learningCurve.entryToString(learningCurve.numEntries() - 1));
                    immediateResultStream.flush();
                }
            }
            if (instancesProcessed % INSTANCES_BETWEEN_MONITOR_UPDATES == 0) {
                if (monitor.taskShouldAbort()) {
                    return null;
                }
                long estimatedRemainingInstances = stream.estimatedRemainingInstances();
                if (maxInstances > 0) {
                    long maxRemaining = maxInstances - instancesProcessed;
                    if ((estimatedRemainingInstances < 0)
                            || (maxRemaining < estimatedRemainingInstances)) {
                        estimatedRemainingInstances = maxRemaining;
                    }
                }
                monitor.setCurrentActivityFractionComplete(estimatedRemainingInstances < 0 ? -1.0
                        : (double) instancesProcessed
                        / (double) (instancesProcessed + estimatedRemainingInstances));
                if (monitor.resultPreviewRequested()) {
                    monitor.setLatestResultPreview(learningCurve.copy());
                }
                secondsElapsed = (int) TimingUtils.nanoTimeToSeconds(TimingUtils.getNanoCPUTimeOfCurrentThread()
                        - evaluateStartTime);
            }
        }
        if (immediateResultStream != null) {
            immediateResultStream.close();
        }
        if (outputPredictionResultStream != null) {
            outputPredictionResultStream.close();
        }
        
    /*    try {
			WritetoFile(learningCurve, learner, stream);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
       //System.out.printf("%.2f", acc/ learningCurve.numEntries());
        System.out.printf("%.2f", exec_time);

        System.out.println();
        //System.out.println("Ensemble Size: " + size/ learningCurve.numEntries());
      //  System.out.println("Drifts: " +       (int) (learningCurve.getMeasurement(learningCurve.numEntries()-1, 11)));
      //  System.out.println("Warnings: " +       (int) (learningCurve.getMeasurement(learningCurve.numEntries()-1, 12)));


        return learningCurve;
    }
    
    
	public void WritetoFile(LearningCurve learningCurve, Learner learner, ExampleStream stream) throws FileNotFoundException, IOException {
		
		String s = stream.getHeader().getRelationName();
		String filename = "stats";
	    String SheetName= null;
		
	        String classifier = learner.toString();
	        if (classifier.contains("WeightedMajorityAlgorithm"))
	           SheetName = "WMA";
	        if (classifier.contains("DWMNB"))
	           SheetName = "DWM-NB";
	        if (classifier.contains("DWMHT"))
	          SheetName = "DWM-HT";
	        if (classifier.contains("HDWM"))
	           SheetName = "Lite";
	        if( SheetName == null)
	        	   SheetName = "Sheet1";
			
			XSSFWorkbook workbook = null;
			filename="C:/Test/AutoResults/" + filename + ".xlsx";

			FileInputStream fis = new FileInputStream(filename);
			XSSFSheet sheet = null;
			 int rows = learningCurve.numEntries();
			 
		        try {
		        	
		            workbook = new XSSFWorkbook(fis);
		                    	      
		            sheet = workbook.getSheet(SheetName);
		            
		            //delete existing data
		            for (int i = sheet.getLastRowNum(); i >= 0; i--) {
		            	if(sheet.getRow(i) != null)
		            	  sheet.removeRow(sheet.getRow(i));
		            	}
		            
		            Row row = sheet.createRow(0);

		            int rowNum = 0;

		                // create new row
		                row = sheet.createRow(rowNum++);
		                row.createCell(0).setCellValue(filename);
		                row = sheet.createRow(rowNum++);

		               // create summary  Header in Excel
		                
		              	for (int i=0; i< learningCurve.getMeasurementArray().size(); i++)
		              		
		                    row.createCell(i).setCellValue(learningCurve.getMeasurementName(i)); 
		                
		                
		                row = sheet.createRow(rowNum++);
		                // insert summary measurements in Excel
		                

		            	for (int j=0; j< learningCurve.getMeasurementArray().size(); j++) {
		                	double sum = 0.0;

		            	    for (int i=0; i< learningCurve.numEntries(); i++) {
		            	    	sum += learningCurve.getMeasurement(i, j);  
		            	    }
		            	 
		                row.createCell(j).setCellValue(sum/learningCurve.numEntries()); 

		            };
		            
		            row = sheet.createRow(rowNum++);
		            row.createCell(0).setCellValue("Drifts");
		            row.createCell(1).setCellValue("Warnings");
		            row = sheet.createRow(rowNum++);
		            
			        if (classifier.contains("HDWM")) {

		            row.createCell(0).setCellValue((int) (learningCurve.getMeasurement(learningCurve.numEntries()-1, 11)));
		            row.createCell(1).setCellValue((int) (learningCurve.getMeasurement(learningCurve.numEntries()-1, 12)));
			        }
		            
		            row = sheet.createRow(rowNum++);

		                
		                // insert measurements in Excel
		                for (int i=0; i< learningCurve.numEntries(); i++) {
		                    row = sheet.createRow(rowNum++);

		                	for (int j=0; j< learningCurve.getMeasurementArray().size(); j++) 
		                        row.createCell(j).setCellValue(learningCurve.getMeasurement(i, j));       
		                	
		                }
		                	
		            
	      
	            // Writing sheet data
	            FileOutputStream outputStream = new FileOutputStream(filename);
	            workbook.write(outputStream);
	           
	            
	            if(workbook != null)
                    workbook.close();
            
        }catch (EncryptedDocumentException | IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
	        finally {
            try {
                if(workbook != null)
                    workbook.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    
    
	
	}

    

}
