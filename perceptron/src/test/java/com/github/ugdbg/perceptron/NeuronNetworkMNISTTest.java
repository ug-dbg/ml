package com.github.ugdbg.perceptron;

import com.github.ugdbg.data.MNIST;
import com.github.ugdbg.function.scalar.Sigmoid;
import com.github.ugdbg.function.scalar.Tanh;
import com.github.ugdbg.function.domain.DomainCheckException;
import com.github.ugdbg.function.scalar.domain.Domains;
import com.github.ugdbg.function.vector.SoftMax;
import com.google.common.collect.Lists;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
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
		this.testNetwork(neuronNetwork, 0.8F);
	}
	
	@Test
	public void testNeuronNetworkTrainImages_SigmoidOutput() throws IOException, ClassNotFoundException {
		NeuronNetwork neuronNetwork = new NeuronNetwork(784);
		neuronNetwork.addLayer(200, new Sigmoid(1));
		neuronNetwork.addLayer(10, new Sigmoid(1));
		this.testNetwork(neuronNetwork, 0.8F);
	}
	
	@Test
	public void testNeuronNetworkTrainImages_SigmoidOutput_DomainCheck() throws IOException, ClassNotFoundException {
		NeuronNetwork neuronNetwork = new NeuronNetwork(784);
		neuronNetwork.addLayer(200, new Sigmoid(1).domainCheck(true));
		neuronNetwork.addLayer(10, new Sigmoid(1).domainCheck(true));
		this.testNetwork(neuronNetwork, 0.8F);
	}
	
	@Test(expected = DomainCheckException.class)
	public void testNeuronNetworkTrainImages_SigmoidOutput_BadDomain() throws IOException, ClassNotFoundException {
		NeuronNetwork neuronNetwork = new NeuronNetwork(784);
		neuronNetwork.addLayer(200, new Sigmoid(1).onDomain(Domains.R_MINUS_STAR).domainCheck(true));
		neuronNetwork.addLayer(10, new Sigmoid(1).domainCheck(true));
		this.testNetwork(neuronNetwork, 0.8F);
	}

	@Test
	public void testNeuronNetworkTrainImages_TanH() throws IOException, ClassNotFoundException {
		NeuronNetwork neuronNetwork = new NeuronNetwork(784);
		neuronNetwork.addLayer(200, new Tanh());
		neuronNetwork.addLayer(10, new Sigmoid(1));
		this.testNetwork(neuronNetwork, 0.6F);
	}
	
	private void testNetwork(NeuronNetwork network, float expectedAccuracy) throws IOException, ClassNotFoundException {
		int batchSize = 30;
		
		logger.info("[CONFIGURE] Configured network :");
		network.shortLabel().forEach(logger::info);
		
		if (! network.isCoherent()) {
			Assert.fail("Network [" + network + "] is incoherent !");
		}
		
		List<NeuronNetwork.Input> inputs = this.mnistToInputs();
		List<List<NeuronNetwork.Input>> halves = Lists.partition(inputs, this.mnist.size() / 2);
		List<NeuronNetwork.Input> testHalf  = halves.get(0);
		List<NeuronNetwork.Input> trainHalf = halves.get(1);

		float accuracy = this.samplingAccuracy(network, testHalf);
		logger.info("[ACCURACY] [SAMPLING] [INIT] [{}]%", accuracy * 100);
		

		float learningRate = 3f;
		logger.info("[TRAINING] batch size [{}], learning rate [{}]", batchSize, learningRate);
		network.train(trainHalf, 2, learningRate, batchSize, NeuronNetwork.Executor.parallel(batchSize));
		accuracy = this.samplingAccuracy(network, testHalf);
		logger.info("[ACCURACY] [SAMPLING] [{}]%", accuracy * 100);
		Assert.assertTrue(accuracy > expectedAccuracy);
		
		logger.info("[ACCURACY] [TOTAL] [{}]%", this.totalAccuracy(network, testHalf) * 100);

		File temp = Files.createTempFile("network", "").toFile();
		temp.deleteOnExit();
		
		try (ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream(temp))) {
			outputStream.writeObject(network);
		}
		try (ObjectInputStream inputStream =  new ObjectInputStream(new FileInputStream(temp))){
			NeuronNetwork deserialized = (NeuronNetwork) inputStream.readObject();
			accuracy = this.samplingAccuracy(deserialized, testHalf);
			logger.info("[CONTROL] [ACCURACY] [SAMPLING] After serialization/deserialization [{}]%", accuracy * 100);
			Assert.assertTrue(accuracy > expectedAccuracy);
		}
	}
	
	private float samplingAccuracy(NeuronNetwork network, List<NeuronNetwork.Input> inputs) {
		AtomicInteger ok = new AtomicInteger();
		new Random().ints(100, 0, inputs.size()).forEach(i -> ok.addAndGet(this.testPrediction(network, inputs.get(i))));
		return ok.get() / 100f;
	}
	
	private float totalAccuracy(NeuronNetwork neuronNetwork, List<NeuronNetwork.Input> inputs) {
		AtomicInteger ok = new AtomicInteger(0);
		
		List<NeuronNetwork.Task> tasks = new ArrayList<>(inputs.size());
		inputs.forEach(input -> tasks.add(
			NeuronNetwork.Task.of(() -> ok.addAndGet(NeuronNetworkMNISTTest.this.testPrediction(neuronNetwork, input))
		)));
		
		try {
			NeuronNetwork.Executor.parallel(Runtime.getRuntime().availableProcessors()).invokeAll(tasks);
		} catch (InterruptedException e) {
			throw new RuntimeException("Interrupted exception evaluating total accuracy !", e);
		}
		return ok.floatValue() / inputs.size();
	}
	
	private int testPrediction(NeuronNetwork neuronNetwork, NeuronNetwork.Input input) {
		return input.expected == neuronNetwork.predict(input.input) ? 1 : 0;
	}
	
	private List<NeuronNetwork.Input> mnistToInputs() {
		return this.mnist.getImages().stream().map(NeuronNetworkMNISTTest::imageToInput).collect(Collectors.toList());
	}
	
	private static NeuronNetwork.Input imageToInput(MNIST.Image image) {
		return new NeuronNetwork.Input(image.singleVector().normalize(0f, 255f), image.getLabel());
	}
}
