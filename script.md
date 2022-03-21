0. Executar exemplo de CLI:

```shell
$ opennlp LanguageDetector model/langdetect-183.bin < dataset/lang-detect.txt
```

1. Criando projeto com Quarkus:

```shell script
quarkus create app io.pedrohos.nlp:opennlp-quarkus
```

2. Importar a dependência do OpenNLP

```xml
    <dependency>
        <groupId>org.apache.opennlp</groupId>
        <artifactId>opennlp-tools</artifactId>
        <version>1.9.4</version>
    </dependency>
```

3. Importar a dependência do OpenCSV

```xml
    <dependency>
        <groupId>com.opencsv</groupId>
        <artifactId>opencsv</artifactId>
        <version>5.6</version>
    </dependency>
```

4. Criar a classe "Normalize" para montar o arquivo de treino:

~~~java
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
~~~

5. Criar classe "DocCatTrain" para treinar o modelo:

```java
package io.pedrohos.nlp.doccat;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import opennlp.tools.doccat.DoccatFactory;
import opennlp.tools.doccat.DoccatModel;
import opennlp.tools.doccat.DocumentCategorizerME;
import opennlp.tools.doccat.DocumentSample;
import opennlp.tools.doccat.DocumentSampleStream;
import opennlp.tools.util.MarkableFileInputStreamFactory;
import opennlp.tools.util.ObjectStream;
import opennlp.tools.util.PlainTextByLineStream;
import opennlp.tools.util.TrainingParameters;

/**
 * @author Pedro Silva <pesilva@redhat.com>
 *
 */

//Using Naive Bayes theorem
public class DocCatTrain {
	
	private static final String ROOT_PAH = "/home/pesilva/workspace/code/pessoal/tdc-nlp/";

	public static void main(String[] args) {

		try {

			MarkableFileInputStreamFactory factory = new MarkableFileInputStreamFactory(new File(ROOT_PAH + "dataset/ge/normalized/ge-doccat.train"));
			ObjectStream<DocumentSample> sampleStream = new DocumentSampleStream(new PlainTextByLineStream(factory, StandardCharsets.UTF_8));

			// setting the parameters for training
			TrainingParameters params = new TrainingParameters();
			params.put(TrainingParameters.ITERATIONS_PARAM, 100);
			params.put(TrainingParameters.CUTOFF_PARAM, 0);
			
			DoccatModel model = DocumentCategorizerME.train("pt", sampleStream, params, new DoccatFactory());
			OutputStream modelOut = new BufferedOutputStream(new FileOutputStream(ROOT_PAH + "model/ge/ge-doccat-model.bin"));
			model.serialize(modelOut);

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
```

6. Criar modelo:

```java
package io.pedrohos.nlp.model;

import java.util.Map;

/**
 * @author Pedro Silva <pesilva@redhat.com>
 *
 */
public class GEDocCatModel {
	
	private String bestCat;
	private Map<String, Double> probs;
	
	/**
	 * @return the bestCat
	 */
	public String getBestCat() {
		return bestCat;
	}
	/**
	 * @param bestCat the bestCat to set
	 */
	public void setBestCat(String bestCat) {
		this.bestCat = bestCat;
	}
	/**
	 * @return the probs
	 */
	public Map<String, Double> getProbs() {
		return probs;
	}
	/**
	 * @param probs the probs to set
	 */
	public void setProbs(Map<String, Double> probs) {
		this.probs = probs;
	}

}
```

7. Criar classe para classificação:

```java
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
```

8. Add the `quarkus-resteasy-jsonb` dep:

```xml
		<dependency>
			<groupId>io.quarkus</groupId>
			<artifactId>quarkus-resteasy-jsonb</artifactId>
		</dependency>
```

9. Criar um Resource

```java
package io.pedrohos.nlp.resource;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import io.pedrohos.nlp.doccat.MyDocCat;

@Path("/nlp/doccat")
public class DocCatResource {

	@Inject
	MyDocCat mydoccat;
	
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response hello(@QueryParam("text") String text) {
        return Response.ok().entity(MyDocCat.classifyText(text)).build();
    }
    
}
```

10. Chamar http://localhost:8080/nlp/doccat?text=""