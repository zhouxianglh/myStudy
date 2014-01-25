package com.henglu.test;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Hashtable;

import javax.imageio.ImageIO;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.EncodeHintType;
import com.google.zxing.LuminanceSource;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.NotFoundException;
import com.google.zxing.Result;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

/** 
* 使用 com.google.zxing, 开源主页:https://github.com/zxing
* @author zhouxianglh@gmail.com
* @version 1.0  2014-1-25 上午10:42:00 
*/
public class Test {
    /**
     * 简单的生成二维码
     * @param contents 文本内容
     * @param width 宽
     * @param height 高
     * @param imgPath 路径
     */
    public static void encode(String contents, int width, int height, String imgPath) throws WriterException,
            IOException {
        Hashtable<EncodeHintType, Object> hints = new Hashtable<EncodeHintType, Object>();
        // 纠错级别越高，整体需要携带的信息越多：L级可纠正约7%错误、M级别可纠正约15%错误、Q级别可纠正约25%错误、H级别可纠正约30%错误。
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
        // 转码后写入,不选择编码则为UTF-8,这里使用ISO-8859-1 方便微信识别
        BitMatrix bitMatrix = new MultiFormatWriter().encode(new String(contents.getBytes(), "ISO-8859-1"),
                BarcodeFormat.QR_CODE, width, height, hints);
        MatrixToImageWriter.writeToFile(bitMatrix, "png", new File(imgPath));
    }

    /** 
     * 简单的二维码识别
     * @param imgPath 
     */
    public static String decode(String imgPath) throws IOException, NotFoundException {
        BufferedImage image = ImageIO.read(new File(imgPath));
        LuminanceSource source = new BufferedImageLuminanceSource(image);
        BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
        Hashtable<DecodeHintType, Object> hints = new Hashtable<DecodeHintType, Object>();
        Result result = new MultiFormatReader().decode(bitmap, hints);
        return result.getText();
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        String imagePath = "D:\\temp.png";
        try {
            encode("BEGIN:VCARD\nVERSION:3.0\nFN:胖子小胖子\nURL:g.cn\nEND:VCARD", 500, 500, imagePath);
            System.out.println(decode(imagePath));
        } catch (NotFoundException | WriterException | IOException e) {
            e.printStackTrace();
        }
    }
}
