package com.github.ugdbg.perceptron;

import java.util.ArrayList;
import java.util.List;

/**
 * A list of {@link Gradient} that can be summed.
 */
class Gradients extends ArrayList<Gradient> {
	Gradients() {}

	/**
	 * Create a list of gradients, one gradient per layer. 
	 * Every gradient is initialized from the layer input/output size.
	 * @param layers the network layers
	 * @return a new Gradients list
	 */
	static Gradients init(List<NeuronLayer> layers) {
		Gradients gradients = new Gradients();
		layers.forEach(l -> gradients.add(new Gradient(l.inputSize(), l.outputSize())));
		return gradients;
	}

	/**
	 * Sum the current gradient list with the given ones, index based.
	 * <br>
	 * Gradient lists sizes should match !
	 * @param others the other gradient list
	 */
	void sum(Gradients others) {
		for (int i = 0; i < Math.min(this.size(), others.size()); i++) {
			this.get(i).sum(others.get(i));
		}
	}
}