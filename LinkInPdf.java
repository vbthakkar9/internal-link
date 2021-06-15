import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.interactive.action.PDActionGoTo;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotation;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationLink;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.destination.PDPageDestination;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.destination.PDPageFitWidthDestination;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;

public class LinkInPdf extends PDFTextStripper {

	private static float LOWER_LEFT_X = 0F;
	private static float LOWER_LEFT_Y = 0F;
	private static float UPPER_RIGHT_X = 0F;
	private static float UPPER_RIGHT_Y = 0F;

	private static int PAGE_NO = 0;
	private static int REDIRECT_PAGE_TO = 0;

	private static String FOUND_TEXT = "";
	private static Boolean IS_FOUND_TEXT_FIRST_TIME = Boolean.TRUE;

	private static Map<String, LinkInfo> mapLink = null;

	public LinkInPdf() throws IOException {
	}

	public static void main(String[] args) {

		File mainIndexPDF = new File("C:\\Users\\cpshah\\Desktop\\index.pdf");
		File subDocPDF = new File("C:\\Users\\cpshah\\Desktop\\sub_doc.pdf");

		try (PDDocument pdDocument = PDDocument.load(mainIndexPDF);) {

			int totalPages = pdDocument.getNumberOfPages();
			System.out.println(totalPages);

			REDIRECT_PAGE_TO = totalPages;
			UPPER_RIGHT_Y = pdDocument.getPage(0).getMediaBox().getUpperRightY();

			PDFTextStripper stripper = new LinkInPdf();
			stripper.setSortByPosition(true);

			FOUND_TEXT = "Go to Sub Document";
			IS_FOUND_TEXT_FIRST_TIME = Boolean.TRUE;

			/*
			 * First Store Link Locations and Redirect page location in map, Please note
			 * that to merge 2nd pdf, REDIRECT_PAGE_TO value will be total pages of
			 * mainIndexPdf + total pages of 1st merged pdf
			 */

			for (int i = 0; i < totalPages; i++) {

				stripper.setStartPage(i + 1);
				stripper.setEndPage(i + 1);

				PAGE_NO = i;

				System.out.println("Before Write With Page : " + (i + 1));
				Writer dummy = new OutputStreamWriter(new ByteArrayOutputStream());
				stripper.writeText(pdDocument, dummy);
				System.out.println("After Write With Page : " + (i + 1));
			}

			System.out.println("Total Map Size : " + mapLink.size());

			// Now merge all the pdfs

			// Instantiating PDFMergerUtility class
			PDFMergerUtility PDFmerger = new PDFMergerUtility();

			// Setting the destination file
			PDFmerger.setDestinationFileName("C:\\Users\\cpshah\\Desktop\\merge_result.pdf");

			// adding the source files
			PDFmerger.addSource(mainIndexPDF);
			PDFmerger.addSource(subDocPDF);

			// Merging the two documents
			PDFmerger.mergeDocuments(null);

			// Here we set totalPage as index to redirect on new pdf
			System.out.println("Documents merged");

		} catch (Exception e) {
			System.out.println("Error while stroing values in map and merging PDF  is : " + e);
		}

		// Now execute code for set the redirect links on merged pdf

		File mergePdf = new File("C:\\Users\\cpshah\\Desktop\\merge_result.pdf");

		try (PDDocument pdDocument = PDDocument.load(mergePdf);) {

			for (Map.Entry<String, LinkInfo> entry : mapLink.entrySet()) {

				System.out.println("PdPage no : " + entry.getValue().getPageNo());
				PDPage pdPage = pdDocument.getPage(entry.getValue().getPageNo());
				List<PDAnnotation> annotations = pdPage.getAnnotations();

				// Now add the link annotation
				PDAnnotationLink annotationLink = new PDAnnotationLink();

				// Set the rectangle containing the link
				PDRectangle position = new PDRectangle();
				position.setLowerLeftX(entry.getValue().getLowerLeftX());
				position.setLowerLeftY(entry.getValue().getLowerLeftY());
				position.setUpperRightX(entry.getValue().getUpperRightX());
				position.setUpperRightY(entry.getValue().getUpperRightY());
				annotationLink.setRectangle(position);

				System.out.println("Text = " + entry.getKey() + "--" + " [(LowerLeftX="
						+ entry.getValue().getLowerLeftX() + ",LowerLeftY=" + entry.getValue().getLowerLeftY()
						+ " UpperRightX=" + entry.getValue().getUpperRightX() + " UpperRightY="
						+ entry.getValue().getUpperRightY() + " Redirect Pate To ="
						+ entry.getValue().getRedirectPageTo() + " Link Set On Page No =" + entry.getValue().getPageNo()
						+ "]");

				// add the GoTo action
				PDActionGoTo actionGoto = new PDActionGoTo();
				PDPageDestination dest = new PDPageFitWidthDestination();
				dest.setPage((PDPage) pdDocument.getPage(entry.getValue().getRedirectPageTo()));
				actionGoto.setDestination(dest);
				annotationLink.setAction(actionGoto);
				annotations.add(annotationLink);

			}

			pdDocument.save(mergePdf);

			System.out.println("Set redirect link successfully");

		} catch (Exception e) {
			System.out.println("Error while creating redirect link in pdf is : " + e);
		}

	}

	@Override
	protected void writeString(String string, List<TextPosition> textPositions) throws IOException {

		for (TextPosition text : textPositions) {

			if (null != string && "" != string && FOUND_TEXT.toLowerCase().trim().equals(string.trim().toLowerCase())) {

				if (IS_FOUND_TEXT_FIRST_TIME == Boolean.TRUE) {
					IS_FOUND_TEXT_FIRST_TIME = Boolean.FALSE;

					LOWER_LEFT_X = text.getXDirAdj();
					LOWER_LEFT_Y = UPPER_RIGHT_Y - text.getYDirAdj();
					UPPER_RIGHT_Y = UPPER_RIGHT_Y - text.getYDirAdj() + text.getHeightDir();
				}

				UPPER_RIGHT_X = text.getXDirAdj();

			} else if (IS_FOUND_TEXT_FIRST_TIME == Boolean.FALSE) {
				IS_FOUND_TEXT_FIRST_TIME = Boolean.TRUE;

				if (null == mapLink)
					mapLink = new HashMap<String, LinkInfo>();

				mapLink.put(FOUND_TEXT, new LinkInfo(PAGE_NO, REDIRECT_PAGE_TO, LOWER_LEFT_X, LOWER_LEFT_Y,
						UPPER_RIGHT_X, UPPER_RIGHT_Y));

				LOWER_LEFT_X = 0F;
				LOWER_LEFT_Y = 0F;
				UPPER_RIGHT_X = 0F;
				UPPER_RIGHT_Y = 0F;

			}

			System.out.println("Text = " + string + "--" + " [(X=" + text.getXDirAdj() + ",Y=" + text.getYDirAdj()
					+ ") height=" + text.getHeightDir() + " width=" + text.getWidthDirAdj() + "]");
		}
	}

}
