package complete_ocr.complete_ocr;

import java.awt.Color;
import java.awt.image.BufferedImage;

import java.io.File;
import java.util.List;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;

import java.io.IOException;
import javax.imageio.ImageIO;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgcodecs.Imgcodecs;

import net.sourceforge.tess4j.*;

@SuppressWarnings("unchecked")
public class App {
	public static String filetype(String path) {
		int index = path.lastIndexOf('.');
		return path.substring(index);
	}

	public static int pdf_to_image(String sourceDir, String destinationDir) throws IOException {
		File sourceFile = new File(sourceDir);

		File destinationFile = new File(destinationDir);
		int page_count = 0;
		if (!destinationFile.exists()) {
			destinationFile.mkdir();
			// System.out.println("Folder Created -> "+ destinationFile.getAbsolutePath());
		}

		// System.out.println(sourceFile.exists());
		if (sourceFile.exists()) {
			// System.out.println("Images copied to Folder: "+ destinationFile.getName());
			PDDocument document = PDDocument.load(sourceDir);

			List<PDPage> list = document.getDocumentCatalog().getAllPages();
			page_count = list.size();
			System.out.println("Total files to be converted -> " + page_count);
			// System.out.println(destinationDir);

			String fileName = sourceFile.getName().replace(".pdf", "");
			int pageNumber = 1;
			for (PDPage page : list) {
				BufferedImage image = page.convertToImage();
				File outputfile = new File(destinationDir + "/" + fileName + "_" + pageNumber + ".jpg");
				//System.out.println("Image Created -> " + outputfile.getName());
				ImageIO.write(image, "jpg", outputfile);
				pageNumber++;
			}
			document.close();
			System.out.println("Converted Images are saved at -> " + destinationFile.getAbsolutePath());
			return page_count;
		} else {
			System.err.println(sourceFile.getName() + " File not exists");
			return 1;
		}
	}

	// define of the convert_png_to_jpg method
	public static void convert_png_to_jpg(String sourceDir, String destinationDir) throws IOException {
		BufferedImage bufferedImage;
		File sourceFile = new File(sourceDir);

		try {

			// read image file
			bufferedImage = ImageIO.read(new File(sourceDir));

			// create a blank, RGB, same width and height, and a white background
			BufferedImage newBufferedImage = new BufferedImage(bufferedImage.getWidth(), bufferedImage.getHeight(),
					BufferedImage.TYPE_INT_RGB);
			newBufferedImage.createGraphics().drawImage(bufferedImage, 0, 0, Color.WHITE, null);
			String fileName = sourceFile.getName().replace(".png", "");
			File outputfile = new File(destinationDir + "/" + fileName + ".jpg");

			// write to jpeg file
			ImageIO.write(newBufferedImage, "jpg", outputfile);

			// System.out.println("Done");

		} catch (IOException e) {

			e.printStackTrace();

		}
	}

	// Load the native OpenCV library
	static {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
	}

	// define of the opencv_line_remova method
	public static void opencv_line_removal(String destinationDir) {

		String dirPath = destinationDir;
		// System.out.println(destinationDir);
		File dir = new File(dirPath);
		String filename, filepath;
		// System.out.println("directory read");

		File[] files = dir.listFiles();
		for (File aFile : files) {

			filename = aFile.getName();
			filepath = destinationDir + '/' + filename;

			// System.out.println("file "+filename);
			// System.out.println(filepath);

			// Load the image
			Mat source = Imgcodecs.imread(filepath);

			// Mat Template class for specifying the size of an image
			Mat image_h = Mat.zeros(source.size(), CvType.CV_8UC1);
			Mat image_v = Mat.zeros(source.size(), CvType.CV_8UC1);
			Mat output = new Mat();

			// Inverse vertical image
			Core.bitwise_not(source, output);
			Mat output_result = new Mat();

			// Create structure element for extracting horizontal lines through morphology
			// operations
			Mat kernel_h = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(20, 1));

			// Apply morphology operations
			Imgproc.morphologyEx(output, image_h, Imgproc.MORPH_OPEN, kernel_h);

			Core.subtract(output, image_h, output_result);

			// Create structure element for extracting vertical lines through morphology
			// operations
			Mat kernel_v = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(1, 20));

			// Apply morphology operations vertical
			Imgproc.morphologyEx(output_result, image_v, Imgproc.MORPH_OPEN, kernel_v);
			Mat output_result2 = new Mat();

			Core.subtract(output_result, image_v, output_result2);

			// write an image after removed horizontal and vertical line from image
			Imgcodecs.imwrite(filepath, output_result2);

			System.out.println(filename + " written ");
			BufferedImage img = null;
			File f = null;

			try {
				f = new File(filepath);
				img = ImageIO.read(f);
			} catch (IOException e) {
				System.out.println(e);
			}
			// get image width and height
			int width = img.getWidth();
			int height = img.getHeight();
			// convert to negative
			for (int y = 0; y < height; y++) {
				for (int x = 0; x < width; x++) {
					int p = img.getRGB(x, y);
					int a = (p >> 24) & 0xff;
					int r = (p >> 16) & 0xff;
					int g = (p >> 8) & 0xff;
					int b = p & 0xff;
					// subtract RGB from 255
					r = 255 - r;
					g = 255 - g;
					b = 255 - b;
					// set new RGB value
					p = (a << 24) | (r << 16) | (g << 8) | b;
					img.setRGB(x, y, p);
				}
			}
			// write image
			try {
				f = new File(filepath);

				ImageIO.write(img, "jpg", f);
			} catch (IOException e) {
				System.out.println(e);
			}
		}

		//System.out.println(" Convert all doccument type into Image ");
	}

	// Main method
	public static void main(String[] args) throws IOException {
		try {

			// Pdf files are read from this folder
			String sourceDir = "C:/Users/Mukesh Yadav/image/test_case-c.jpg";
			// System.out.println(sourceDir);

			// converted images from pdf document are saved here
			String destinationDir = "C:/Users/Mukesh Yadav/image/newFolder";
			int page_count = 1;

			// find the extension of file
			String type = filetype(sourceDir);

			// System.out.println(type+" "+type.equals(".pdf"));

			if (type.equals(".pdf")) {
				File file = new File(destinationDir); // move to file a new folder
				File[] files = file.listFiles();
				for (File f : files) {
					f.delete();
				}

				// If file type pdf then call pdf_to_ image method to convert pdf to jpg image
				// file
				page_count = pdf_to_image(sourceDir, destinationDir);
			}

			else if (type.equals(".png") || type.equals(".tif") || type.contentEquals(".jpg")) {

				File file = new File(destinationDir); // move to file a new folder
				File[] files = file.listFiles();
				for (File f : files) {
					f.delete();
				}

				// If file type png or tif then call png_to_ jpg method to convert png or tif to
				// jpg image file
				convert_png_to_jpg(sourceDir, destinationDir);
			}

			/*
			 * else { File file = new File(destinationDir); File[] files = file.listFiles();
			 * // move to file a new folder for (File f:files) { f.delete(); } File afile
			 * =new File(sourceDir); if(afile.renameTo(new File(destinationDir+"/" +
			 * afile.getName()))){ System.out.println("File is moved successful!"); }
			 * 
			 * }
			 */

			// System.out.println("level 1");

			// opencv_line removal method call to remove the horizantal and vartical line
			// from jpg image
			opencv_line_removal(destinationDir);

			// System.out.println("level 2 crossed");

			// Instantiating the Tesseract class
			// which is used to perform OCR
			Tesseract tesseract = new Tesseract();

			tesseract.setDatapath("C:\\Program Files\\Tesseract-OCR\\tessdata");
			String dirPath = destinationDir;
			String filename, filepath;
			File dir = new File(dirPath);
			File[] files = dir.listFiles(); // All page store in Array

			int Total_Page = (int) files.length; // Total page number
			try {
				int i=2;
				for (File aFile : files) {
					filename = aFile.getName();
					filepath = destinationDir + '/' + filename;

					// System.out.println(filepath);

					// doing OCR on the image
					// and storing result in string str
					String text = tesseract.doOCR(new File(filepath));

					// print the fetch data from image-
					if(i==2) {
					System.out.println(
							"\n-------------------------------------  Page-1 ----------------------------------------\n");
					}
					System.out.print(text);
                     
					if (Total_Page-- > 1)
						System.out.println(
								"\n-------------------------------------  Page-"+i+" ----------------------------------------\n");
					i++;

				}
			} catch (TesseractException e) {
				e.printStackTrace();
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}