package com.github.ugdbg.data;

import com.github.ugdbg.vector.primitive.FloatVector;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ArrayUtils;

import java.io.*;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.MessageFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.zip.GZIPInputStream;

/**
 * Helper class to [down]load MNIST database.
 * <br>
 * This is based on <a href="https://github.com/jeffgriffith/mnist-reader">Jeff Griffith's MNIST reader</a>.
 */
public class MNIST {
	
	private String imagesURL = "http://yann.lecun.com/exdb/mnist/train-images-idx3-ubyte.gz";
	private String labelsURL = "http://yann.lecun.com/exdb/mnist/train-labels-idx1-ubyte.gz";
	private File imagesTarget;
	private File labelsTarget;
	private List<Image> images = new ArrayList<>();
	private Labels labels;

	/**
	 * Private constructor. Please use factory methods (e.g. {@link #load(Path, Path)}).
	 */
	private MNIST() {
		try {
			this.imagesTarget = Files.createTempFile("MNIST_img", "").toFile();
			this.labelsTarget = Files.createTempFile("MNIST_lbl", "").toFile();
			this.imagesTarget.deleteOnExit();
			this.labelsTarget.deleteOnExit();
		} catch (IOException e) {
			throw new RuntimeException("Could not create MNIST target file", e);
		}
	}

	/**
	 * What is the size of the MNIST dataset ? 
	 * @return {@link #images} size.
	 */
	public int size() {
		return this.images.size();
	}

	/**
	 * Get the image at the given index of this MNIST dataset.
	 * @param at the image index
	 * @return the image at the given index
	 */
	public Image image(int at) {
		return this.getImages().get(at);
	}
	
	/**
	 * Get the label at the given index of this MNIST dataset.
	 * @param at the label index
	 * @return the label at the given index
	 */
	public Integer label(int at) {
		return this.getLabels().asInts().get(at);
	}
	
	/**
	 * Get the images from the MNIST dataset.
	 * @return {@link #images}.
	 */
	public List<Image> getImages() {
		return this.images;
	}

	/**
	 * Get the labels from the MNIST dataset.
	 * @return {@link #labels}.
	 */
	public Labels getLabels() {
		return this.labels;
	}
	
	public boolean isCoherent() {
		return this.images.size() == this.labels.asInts().size();
	}

	/**
	 * Load a new MNIST instance from the default URLs.
	 * @return a new MNIST instance whose datasets are loaded from the given URLs.
	 */
	public static MNIST load() {
		MNIST mnist = new MNIST();
		mnist.downloadImages();
		mnist.downloadLabels();
		mnist.doLoad();
		return mnist;
	}
	
	/**
	 * Load a new MNIST instance from the given URLs.
	 * @return a new MNIST instance whose datasets are loaded from the given URLs.
	 */
	public static MNIST load(String imagesURL, String labelsURL) {
		MNIST mnist = new MNIST();
		mnist.imagesURL = imagesURL;
		mnist.labelsURL = labelsURL;
		mnist.downloadImages();
		mnist.downloadLabels();
		mnist.doLoad();
		return mnist;
	}
	
	/**
	 * Load a new MNIST instance from the given paths.
	 * <b>This does not download anything from {@link #imagesURL} or {@link #labelsURL} !</b> 
	 * @return a new MNIST instance whose datasets are loaded from the given file paths.
	 */
	public static MNIST load(Path images, Path labels) {
		MNIST mnist = new MNIST();
		mnist.imagesTarget = images.toFile();
		mnist.labelsTarget = labels.toFile();
		mnist.doLoad();
		return mnist;
	}

	/**
	 * Download the MNIST images from {@link #imagesURL} to {@link #imagesTarget}.
	 */
	private void downloadImages() {
		download(this.imagesURL, this.imagesTarget.toPath());
	}
	
	/**
	 * Download the MNIST images from {@link #labelsURL} to {@link #labelsTarget}.
	 */
	private void downloadLabels() {
		download(this.labelsURL, this.labelsTarget.toPath());
	}

	/**
	 * Download from a given URL into a given file path.
	 * @param url the source URL
	 * @param to  the target file path
	 */
	private static void download(String url, Path to) {
		try {
			FileUtils.copyURLToFile(new URL(url), to.toFile(), 10000, 10000);
		} catch (IOException e) {
			throw new RuntimeException("Could not download [" + url + "] to [" + to + "]", e);
		}
	}

	/**
	 * Load MNIST datasets into the current instance from the given paths.
	 * <b>This does not download anything from {@link #imagesURL} or {@link #labelsURL} !</b> 
	 */
	private void doLoad() {
		this.imagesTarget = extract(this.imagesTarget.toPath()).toFile();
		this.labelsTarget = extract(this.labelsTarget.toPath()).toFile();
		this.images = Image.images(this.imagesTarget.toPath());
		this.labels = Labels.labels(this.labelsTarget.toPath());
		IntStream.range(0, this.size()).forEach(i -> this.images.get(i).label = this.labels.row[i]);
	}

	/**
	 * Extract a GZIP file into an '_unzipped' suffixed file.
	 * <br>
	 * If the source file is not GZIP, return the input path.
	 * @param what the path of the file to unzip.
	 * @return the unzipped file path. 
	 */
	private static Path extract(Path what) {
		if (isZip(what)) {
			try {
				InputStream in = new BufferedInputStream(new GZIPInputStream(new FileInputStream(what.toFile())));
				Path out = Paths.get(what + "_unzipped");
				Files.copy(in, out, StandardCopyOption.REPLACE_EXISTING);
				return out;
			} catch (IOException e) {
				throw new RuntimeException("Unable to extract MNIST from zip [" + what + "]", e);
			}
		}
		return what;
	}

	/**
	 * Check if the given path is a GZIP file.
	 * @param pathToFile the path to the GZIP file.
	 * @return true if the path is for a GZIP file.
	 */
	private static boolean isZip(Path pathToFile) {
		try {
			new GZIPInputStream(new FileInputStream(pathToFile.toFile()));
			return true;
		} catch (IOException e) {
			return false;
		}
	}

	/**
	 * Check a magic number against an expected one.
	 * @param expectedMagicNumber the expected magic number
	 * @param magicNumber         the actual magic number
	 * @throws RuntimeException if the actual magic number does not match the expected magic number
	 */
	private static void assertMagicNumber(int expectedMagicNumber, int magicNumber) {
		if (expectedMagicNumber != magicNumber) {
			switch (expectedMagicNumber) {
				case Labels.LABEL_FILE_MAGIC_NUMBER: throw new RuntimeException("This is not a label file.");
				case Image.IMAGE_FILE_MAGIC_NUMBER:  throw new RuntimeException("This is not an image file.");
				default:
					throw new RuntimeException(MessageFormat.format(
						"Expected magic number %d, found %d", 
						expectedMagicNumber, 
						magicNumber)
					);
				}
		}
	}

	/**
	 * Load the content of a file into a {@link ByteBuffer}.
	 * @param infile the file path
	 * @return a new ByteBuffer for the content of the file
	 */
	private static ByteBuffer loadFileToByteBuffer(Path infile) {
		return ByteBuffer.wrap(loadFile(infile));
	}

	/**
	 * Load the entire content of a file into a byte array.
	 * @param infile the file path
	 * @return the entire content of the file as a byte array
	 */
	private static byte[] loadFile(Path infile) {
		try {
			RandomAccessFile file = new RandomAccessFile(infile.toFile(), "r");
			FileChannel channel = file.getChannel();
			long fileSize = channel.size();
			ByteBuffer buffer = ByteBuffer.allocate((int) fileSize);
			channel.read(buffer);
			buffer.flip();
			ByteArrayOutputStream bytes = new ByteArrayOutputStream();
			for (int i = 0; i < fileSize; i++) {
				bytes.write(buffer.get());
			}
			channel.close();
			file.close();
			return bytes.toByteArray();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * A collection of images with an extra API for rendering. 
	 */
	public static class Images extends ArrayList<Image> {
		public Images() {}

		public Images(Collection<? extends Image> c) {
			super(c);
		}

		/**
		 * Create a label for the current image collection.
		 * @param delimiter     the image delimiter
		 * @param imagesPerLine the number of images per line
		 * @return a collection of lines to display : the images as rows, with X images per row. 
		 */
		public List<String> label(String delimiter, int imagesPerLine) {
			List<String> label = new ArrayList<>();

			List<List<Image>> partition = Lists.partition(this, imagesPerLine);
			partition.forEach(p -> label.addAll(new Images(p).label(delimiter)));

			return label;
		}
		
		/**
		 * Create a label for the current image collection.
		 * @param delimiter     the image delimiter
		 * @return a collection of lines to display : the images as a single row 
		 */
		public List<String> label(String delimiter) {
			List<List<String>> images = this.stream().map(Image::label).collect(Collectors.toList());
			Map<Integer, String> asRow = new TreeMap<>();
			for (List<String> image : images) {
				for (Integer i = 0; i < image.size(); i++) {
					if (! asRow.containsKey(i)) {
						asRow.put(i, image.get(i));
					} else {
						asRow.put(i, asRow.get(i) + delimiter + image.get(i));
					}
				}
			}
			return new ArrayList<>(asRow.values());
		}
	}
	
	/**
	 * An image and its label from the MNIST dataset.
	 * <br>
	 * Data is stored as a float[][] matrix.
	 */
	public static class Image {
		private static final int IMAGE_FILE_MAGIC_NUMBER = 2051;
		private int[][] image;
		private int label = -1;

		/**
		 * Private constructor. Please use {@link #images} to load image from files. 
		 * @param numRows the image height in px
		 * @param numCols the image width  in px
		 * @param bb      the source byte buffer
		 */
		private Image(int numRows, int numCols, ByteBuffer bb) {
			this.image = new int[numRows][];
			for (int row = 0; row < numRows; row++) {
				this.image[row] = new Row(numCols, bb).row;
			}
		}

		/**
		 * What is actually written in this image ?
		 * @return {@link #label}, the actual value of the image (a [0-9] integer)
		 */
		public int getLabel() {
			return this.label;
		}

		/**
		 * Get a row (line) from the image at a given index.
		 * @param at the image row index
		 * @return a new Row for the image line at the given index  
		 */
		public Row getRow(int at) {
			return new Row(this.image[at]);
		}

		/**
		 * Get the image data as a single vector.
		 * @return a new Vector with the inlined {@link #image} data matrix.
		 */
		public FloatVector singleVector() {
			List<Float> out = new ArrayList<>();
			for (int[] row : this.image) {
				for (int value : row) {
					out.add((float) value);
				}
			}
			return FloatVector.of(ArrayUtils.toPrimitive(out.toArray(new Float[0])));
		}
		
		public List<String> label() {
			List<String> label = new ArrayList<>(this.image.length);
			label.add("┌" + horizontalLine(this.image.length) + "┐");
			for (int[] rows : this.image) {
				StringBuilder line = new StringBuilder();
				line.append("│");
				for (int pixelVal : rows) {
					if (pixelVal == 0) {
						line.append(" ");
					} else if (pixelVal < 256 / 3) {
						line.append(".");
					} else if (pixelVal < 2 * (256 / 3)) {
						line.append("x");
					} else {
						line.append("X");
					}
				}
				line.append("│");
				label.add(line.toString());
			}
			label.add("└" + horizontalLine(this.image.length) + "┘");
	
			return label;
		}

		@Override
		public String toString() {
			return Joiner.on("\n").join(this.label());
		}

		static String horizontalLine(int length) {
			return new String(new char[Math.max(length, 0)]).replace('\0', '─');
		}
		
		/**
		 * Load images from a given file path
		 * @param inFile the input file path
		 * @return a list of images load from the source file
		 */
		static List<Image> images(Path inFile) {
			ByteBuffer bb = loadFileToByteBuffer(inFile);
			
			assertMagicNumber(IMAGE_FILE_MAGIC_NUMBER, bb.getInt());
	
			int numImages = bb.getInt();
			int numRows = bb.getInt();
			int numColumns = bb.getInt();
			List<Image> images = new ArrayList<>();
	
			for (int i = 0; i < numImages; i++) {
				images.add(new Image(numRows, numColumns, bb));
			}
	
			return images;
		}
	}

	/**
	 * A row is a convenience class for an {@link Image} line.
	 */
	static class Row {
		int[] row;
		
		private Row(int[] row) {
			this.row = row;
		}
		
		private Row(int numCols, ByteBuffer bb) {
			this.row = new int[numCols];
			for (int col = 0; col < numCols; ++col) {
				this.row[col] = bb.get() & 0xFF; // To unsigned
			}
		}
	}

	/**
	 * The ordered labels from the MNIST dataset.
	 */
	public static class Labels extends Row {
		private static final int LABEL_FILE_MAGIC_NUMBER = 2049;
		
		private Labels(int numCols, ByteBuffer bb) {
			super(numCols, bb);
		}

		/**
		 * Get the ordered MNIST labels as Strings (e.g. 1 → "1")
		 * @return a new List of the ordered MNIST labels as Strings
		 */
		public List<String> asStrings() {
			return Arrays.stream(this.row).mapToObj(String::valueOf).collect(Collectors.toList());
		}

		/**
		 * Get the ordered MNIST labels as Integers (e.g. 1 → 1)
		 * @return a new List of the ordered MNIST labels as Integers
		 */
		public List<Integer> asInts() {
			return Arrays.stream(this.row).boxed().collect(Collectors.toList());
		}

		/**
		 * Load the MNIST labels from the given file path
		 * @param infile the input file path
		 * @return the MNIST labels
		 */
		static Labels labels(Path infile) {
			ByteBuffer bb = loadFileToByteBuffer(infile);
	
			assertMagicNumber(LABEL_FILE_MAGIC_NUMBER, bb.getInt());
	
			int numLabels = bb.getInt();
			return new Labels(numLabels, bb);
		}
	}
}
