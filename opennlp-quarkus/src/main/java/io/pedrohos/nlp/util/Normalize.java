package io.pedrohos.nlp.util;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.List;

import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvException;

/**
 * @author Pedro Silva <pesilva@redhat.com>
 *
 */
public class Normalize {
	
	private static final String ROOT_PAH = "/home/pesilva/workspace/code/pessoal/tdc-nlp/";

	public static void main(String[] args) {
		
		System.out.println("starting nomalize ...");
		
		Normalize normalize = new Normalize();
		normalize.createCatDocTrainingAndTestFileByCSV(5, 2, "ge-doccat", ROOT_PAH + "dataset/ge/ge_news.csv");
		
		System.out.println("finish nomalize ...");
	}

	public void createCatDocTrainingAndTestFileByCSV(int posCat, int posTxt, String fileName, String csvFile) {

		try (CSVReader reader = new CSVReaderBuilder(new FileReader(csvFile)).withSkipLines(1).build()) {
			
			List<String[]> allLines = reader.readAll();
			createTrainFile(posCat, posTxt, fileName, allLines);
			
			//int trainLimit = (int) Math.round(allLines.size() * 0.8);
			//createTrainFile(posCat, posTxt, fileName, allLines.subList(0, trainLimit));
			//createTestFile(posCat, posTxt, fileName, allLines.subList(trainLimit, allLines.size()));

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (CsvException e) {
			e.printStackTrace();
		}
	}

	public void createTestFile(int posCat, int posTxt, String fileName, List<String[]> lines) throws FileNotFoundException, UnsupportedEncodingException {
		
		PrintWriter pw = new PrintWriter(ROOT_PAH + "dataset/ge/normalized/" + fileName + ".csv", "UTF-8");
		pw.write("club,title,predict\n");
		
		lines.forEach(x -> {

			String cat = x[posCat];
			String text = x[posTxt];

			if (!cat.isBlank() && !text.isBlank()) {
				pw.write(cat + "," + text + "" + "\n");
			}

		});
		
		pw.close();
	}

	public void createTrainFile(int posCat, int posTxt, String trainFile, List<String[]> lines) throws FileNotFoundException, UnsupportedEncodingException {
		
		PrintWriter pw = new PrintWriter(ROOT_PAH + "dataset/ge/normalized/" + trainFile + ".train", "UTF-8");
		lines.forEach(x -> {

			String cat = x[posCat];
			String text = x[posTxt];

			if (!cat.isBlank() && !text.isBlank()) {
				pw.write(cat + "\t" + text + "\n");
			}

		});
		 

		pw.close();
	}

}
