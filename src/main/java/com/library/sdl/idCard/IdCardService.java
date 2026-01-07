
package com.library.sdl.idCard;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.client.j2se.MatrixToImageWriter;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Image;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfWriter;

import com.library.sdl.User;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;

@Service
public class IdCardService {

    public byte[] generateIdCard(User user, LocalDate validTill) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();

            Rectangle cardSize = new Rectangle(242, 153);
            Document doc = new Document(cardSize, 10, 10, 10, 10); // ✅ FIX margins
            PdfWriter writer = PdfWriter.getInstance(doc, out);
            doc.open();

            // ✅ BACKGROUND GOES UNDER TEXT
            PdfContentByte canvas = writer.getDirectContentUnder();
            canvas.setColorFill(new BaseColor(33, 150, 243));
            canvas.rectangle(0, 0, cardSize.getWidth(), cardSize.getHeight());
            canvas.fill();

            // Fonts
            Font title = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD, BaseColor.WHITE);
            Font text = new Font(Font.FontFamily.HELVETICA, 9, Font.NORMAL, BaseColor.WHITE);

            Paragraph content = new Paragraph();
            content.setLeading(12);

            content.add(new Paragraph("SHASTRA DIGITAL LIBRARY", title));
            content.add(new Paragraph(" "));
            content.add(new Paragraph("Name : " + user.getName(), text));
            content.add(new Paragraph("Mobile : " + user.getMobile(), text));
            content.add(new Paragraph("Seat : " + user.getSeat(), text));
            content.add(new Paragraph("Shift : " + user.getShift(), text));
            content.add(new Paragraph("Valid Till : " + validTill, text));

            doc.add(content);

            // QR Code
            Image qr = Image.getInstance(generateQRCode(user.getId()));
            qr.scaleToFit(40, 40);
            qr.setAbsolutePosition(190, 10);
            doc.add(qr);

            doc.close();
            return out.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("ID card generation failed", e);
        }
    }

    private byte[] generateQRCode(Long userId) throws Exception {
        QRCodeWriter writer = new QRCodeWriter();
        BitMatrix matrix = writer.encode(
                "SDL-VERIFY-USER:" + userId,
                BarcodeFormat.QR_CODE,
                150,
                150
        );

        ByteArrayOutputStream png = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(matrix, "PNG", png);
        return png.toByteArray();
    }
}
