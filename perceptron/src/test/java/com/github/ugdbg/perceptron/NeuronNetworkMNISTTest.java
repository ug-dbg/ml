package com.github.ugdbg.perceptron;

import com.github.ugdbg.data.MNIST;
import com.github.ugdbg.function.scalar.Sigmoid;
import com.github.ugdbg.function.vector.SoftMax;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Testing the Multi-Layer Perceptron {@link NeuronNetwork} with {@link MNIST}.
 * <br>
 * This test can use a local MNIST dataset if present in {@link #tempDir}/mldata.
 * Else, it can download the MNIST dataset.
 */
public class NeuronNetworkMNISTTest {
	
	private static final Logger logger = LoggerFactory.getLogger(NeuronNetworkMNISTTest.class);
	
	private MNIST mnist;
	
	private Path tempDir = Paths.get(System.getProperty("java.io.tmpdir"));
	
	@Before
	public void init() {
		Path imagesPath = Paths.get(this.tempDir.toString(), "mldata/train-images-idx3-ubyte.gz");
		Path labelsPath = Paths.get(this.tempDir.toString(), "mldata/train-labels-idx1-ubyte.gz");
		
		if (! imagesPath.toFile().exists()) {
			logger.info("Downloading MNIST...");
			this.mnist = MNIST.load();
			logger.info("MNIST downloaded !");
		} else {
			this.mnist = MNIST.load(imagesPath, labelsPath);
		}
		
		Assert.assertTrue("The MNIST dataset is not coherent !", this.mnist.isCoherent());
		
		IntStream.range(0, 13)
			.mapToObj(this.mnist::image)
			.collect(Collectors.toCollection(MNIST.Images::new))
			.label(" ", 4)
			.forEach(logger::debug);
	}
	
	@Test
	public void testIncoherentNetwork() {
		NeuronNetwork neuronNetwork = new NeuronNetwork(784);
		neuronNetwork.addLayer(2000, new Sigmoid(1));
		neuronNetwork.addLayer(200, new Sigmoid(1));
		neuronNetwork.addLayer(10, new Sigmoid(1));

		try {
			Field layers = neuronNetwork.getClass().getDeclaredField("layers");
			layers.setAccessible(true);
			((List) layers.get(neuronNetwork)).remove(1);
		} catch (NoSuchFieldException | IllegalAccessException e) {
			logger.error("Error removing layer for coherence test");
		}

		logger.info("Configured network :");
		neuronNetwork.shortLabel().forEach(logger::info);
		
		if (neuronNetwork.isCoherent()) {
			logger.error("Network [" + neuronNetwork + "] is coherent !");
			Assert.fail("Network was supposed to be incoherent after I removed a layer !");
		}
	}
	
	@Test
	public void testNeuronNetworkTrainImages_SoftMaxOutput() throws IOException, ClassNotFoundException {
		NeuronNetwork neuronNetwork = new NeuronNetwork(784);
		neuronNetwork.addLayer(200, new Sigmoid(1));
		neuronNetwork.addLayer(10, new SoftMax());
		this.testNeuronNetworkTrainImages(neuronNetwork);
	}
	
	@Test
	public void testNeuronNetworkTrainImages_SigmoidOutput() throws IOException, ClassNotFoundException {
		NeuronNetwork neuronNetwork = new NeuronNetwork(784);
		neuronNetwork.addLayer(200, new Sigmoid(1));
		neuronNetwork.addLayer(10, new Sigmoid(1));
		this.testNeuronNetworkTrainImages(neuronNetwork);
	}
	
	private void testNeuronNetworkTrainImages(NeuronNetwork neuronNetwork) throws IOException, ClassNotFoundException {
		int batchSize = 30;
		
		logger.info("Configured network :");
		neuronNetwork.shortLabel().forEach(logger::info);
		
		if (! neuronNetwork.isCoherent()) {
			Assert.fail("Network [" + neuronNetwork + "] is incoherent !");
		}
		
		List<NeuronNetwork.Input> inputs = new ArrayList<>();
		List<Integer> labels = this.mnist.getLabels().asInts();
		for (int i = 0; i < this.mnist.size(); i++) {
			inputs.add(new NeuronNetwork.Input(
				this.mnist.getImages().get(i).singleVector().normalize(0, 255), 
				labels.get(i)
			));
		}
		float accuracy = this.samplingAccuracy(neuronNetwork, inputs);
		logger.info("Initial accuracy on 100 random samples [{}]%", accuracy * 100);
		

		float learningRate = 3f;
		logger.info("Training network using batch size [{}] and learning rate [{}]", batchSize, learningRate);
		neuronNetwork.train(inputs, 2, learningRate, batchSize, NeuronNetwork.Executor.parallel(batchSize));
		accuracy = this.samplingAccuracy(neuronNetwork, inputs);
		logger.info("Accuracy on 100 random samples [{}]%", accuracy * 100);
		Assert.assertTrue(accuracy > 0.8f);
		
		logger.info("Total accuracy [{}]%", this.totalAccuracy(neuronNetwork, inputs) * 100);

		File temp = Files.createTempFile("network", "").toFile();
		temp.deleteOnExit();
		
		try (ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream(temp))) {
			outputStream.writeObject(neuronNetwork);
		}
		try (ObjectInputStream inputStream =  new ObjectInputStream(new FileInputStream(temp))){
			NeuronNetwork deserialized = (NeuronNetwork) inputStream.readObject();
			accuracy = this.totalAccuracy(deserialized, inputs);
			logger.info("Accuracy after serialization [{}]%", accuracy * 100);
			Assert.assertTrue(accuracy > 0.8f);
		}
	}
	
	private float samplingAccuracy(NeuronNetwork neuronNetwork, List<NeuronNetwork.Input> inputs) {
		Random random = new Random();
		int ok = 0;
		for (int i = 0; i < 100; i++) {
			int index = random.nextInt(60000);
			ok += this.testPrediction(neuronNetwork, inputs.get(index));
		}
		return ok / 100f;
	}
	
	private float totalAccuracy(NeuronNetwork neuronNetwork, List<NeuronNetwork.Input> inputs) {
		AtomicInteger ok = new AtomicInteger(0);
		
		List<NeuronNetwork.Task> tasks = new ArrayList<>(this.mnist.size());
		for (int i = 0; i < this.mnist.size(); i++) {
			int index = i;
			tasks.add(NeuronNetwork.Task.of(
				() -> ok.addAndGet(NeuronNetworkMNISTTest.this.testPrediction(neuronNetwork, inputs.get(index))))
			);
		}
		try {
			NeuronNetwork.Executor.parallel(32).invokeAll(tasks);
		} catch (InterruptedException e) {
			throw new RuntimeException("Interrupted exception evaluating total accuracy !", e);
		}
		return ok.floatValue() / this.mnist.size();
	}
	
	private int testPrediction(NeuronNetwork neuronNetwork, NeuronNetwork.Input input) {
		return input.expected == neuronNetwork.predict(input.input) ? 1 : 0;
	}
}
