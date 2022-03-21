package io.pedrohos.nlp.doccat;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;

import io.pedrohos.nlp.model.GEDocCatModel;
import opennlp.tools.doccat.DoccatModel;
import opennlp.tools.doccat.DocumentCategorizer;
import opennlp.tools.doccat.DocumentCategorizerME;
import opennlp.tools.tokenize.SimpleTokenizer;
import opennlp.tools.tokenize.Tokenizer;

/**
 * @author Pedro Silva <pesilva@redhat.com>
 *
 */
@ApplicationScoped
public class MyDocCat {
	
	private static final String ROOT_PATH = "/home/pesilva/workspace/code/pessoal/tdc-nlp/";
	
	public static void main(String[] args) {
		
		GEDocCatModel label = classifyText("Abel Ferreira têm negociação avançada para renovar contrato até o fim de 2024");
		System.out.println(label.getBestCat());
		
	}
	
	public static GEDocCatModel classifyText(final String text) {
		
		try (InputStream modelIn = new FileInputStream(ROOT_PATH + "model/ge/ge-doccat-model.bin")) {
			
			Tokenizer tokenizer = SimpleTokenizer.INSTANCE;
			String[] docWords = tokenizer.tokenize(text);
			
			DoccatModel model = new DoccatModel(modelIn);
			DocumentCategorizer doccat = new DocumentCategorizerME(model);
			double[] aProbs = doccat.categorize(docWords);
			
			GEDocCatModel gedoccatModel = new GEDocCatModel();
			gedoccatModel.setBestCat(doccat.getBestCategory(aProbs));
			gedoccatModel.setProbs(getCatProbs(doccat, aProbs));
			
			return gedoccatModel;

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
		
	}

	public static Map<String, Double> getCatProbs(DocumentCategorizer doccat, double[] aProbs) {
		
		Map<String, Double> probs = new HashMap<String, Double>();
		
		for (int i = 0; i < doccat.getNumberOfCategories(); i++) {
			probs.put(doccat.getCategory(i), aProbs[i]);
		}
		
		return probs;
		
	}

}
