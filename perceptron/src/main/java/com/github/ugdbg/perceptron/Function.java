package com.github.ugdbg.perceptron;

import java.io.Serializable;

/**
 * A real and derivable R → R function. <br>
 */
public interface Function extends Serializable {
	float apply(float input);
	float derive(float input);
}
