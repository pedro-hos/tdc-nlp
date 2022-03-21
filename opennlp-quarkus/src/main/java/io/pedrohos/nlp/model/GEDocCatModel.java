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
