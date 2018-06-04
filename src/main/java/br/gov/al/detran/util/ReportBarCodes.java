package br.gov.al.detran.util;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;

import javax.imageio.ImageIO;
import javax.xml.bind.DatatypeConverter;

import com.itextpdf.text.pdf.Barcode39;
import com.itextpdf.text.pdf.BarcodeQRCode;
import com.lowagie.text.pdf.BarcodeInter25;

final public class ReportBarCodes implements Serializable {


	public static byte[] generateCode39BarCode(String conteudo) {
		byte[] barcode = new byte[] {0,0,0};
		ByteArrayOutputStream baos;
		Barcode39 code39;
		Image image;
		BufferedImage bffImg;
		Graphics2D offg;
		try {
			baos = new ByteArrayOutputStream();
			code39 = new Barcode39();
			code39.setCode(conteudo);
			image = code39.createAwtImage(Color.black, Color.white);
			bffImg = new BufferedImage(image.getWidth(null), image.getHeight(null), BufferedImage.TYPE_3BYTE_BGR);
			offg = bffImg.createGraphics();
			offg.drawImage(image, 0, 0, null);

			ImageIO.write(bffImg, "jpeg", baos);

			return baos.toByteArray();

		} catch (IOException e) {

		} finally {
			baos = null;
			code39 = null;
			image = null;
			offg = null;

		}

		return barcode;
	}

	public static byte[] generateInter25BarCode(String conteudo) {

		byte[] barcode = new byte[] {0,0,0};

		ByteArrayOutputStream baos;
		BarcodeInter25 code25;
		Image image;
		BufferedImage bffImg;
		Graphics2D offg;
		try {
			baos = new ByteArrayOutputStream();
			code25 = new BarcodeInter25();
			code25.setCode(conteudo);
			image = code25.createAwtImage(Color.black, Color.white);
			bffImg = new BufferedImage(image.getWidth(null), image.getHeight(null), BufferedImage.TYPE_3BYTE_BGR);
			offg = bffImg.createGraphics();
			offg.drawImage(image, 0, 0, null);

			ImageIO.write(bffImg, "jpeg", baos);

			return baos.toByteArray();

		} catch (IOException e) {

		} finally {
			baos = null;
			code25 = null;
			image = null;
			offg = null;

		}

		return barcode;
	}

	public static  byte[] generateQRCodeBarCode(String conteudo) {

		byte[] barcode = new byte[] {0,0,0};
		try {

			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			BarcodeQRCode qrcode = new BarcodeQRCode(conteudo, 1, 1, null);

			Image image = qrcode.createAwtImage(Color.black, Color.white);

			BufferedImage bffImg = new BufferedImage(image.getWidth(null), image.getHeight(null), BufferedImage.TYPE_3BYTE_BGR);
			Graphics2D offg = bffImg.createGraphics();
			offg.drawImage(image, 0, 0, null);

			ImageIO.write(bffImg, "jpeg", baos);

			return baos.toByteArray();

		} catch (IOException e) {

		} finally {

		}
		return barcode;

	}

	public static String generateQRCodeAsB64(String conteudo) {

		try {

			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			BarcodeQRCode qrcode = new BarcodeQRCode(conteudo, 1, 1, null);

			Image image = qrcode.createAwtImage(Color.black, Color.white);

			BufferedImage bffImg = new BufferedImage(image.getWidth(null), image.getHeight(null), BufferedImage.TYPE_3BYTE_BGR);
			Graphics2D offg = bffImg.createGraphics();
			offg.drawImage(image, 0, 0, null);

			ImageIO.write(bffImg, "jpeg", baos);

			return DatatypeConverter.printBase64Binary(baos.toByteArray());

		} catch (IOException e) {

		} finally {

		}


       return "";
	}

}
