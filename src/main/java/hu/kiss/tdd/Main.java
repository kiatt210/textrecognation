package hu.kiss.tdd;

import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Main {
    // Compulsory
    static{ System.loadLibrary(Core.NATIVE_LIBRARY_NAME); }

    private static int MAX_WIDTH  = 900;
    private static int MAX_HEIGHT = 1200;

    public static void main(String[] args) throws TesseractException {

        //System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        //System.load("C:\\MI\\Tools\\opencv\\build\\java\\x64");
        Mat img = Imgcodecs.imread("C:\\MI\\TextDetectionDemo\\images\\szamla.tiff");
        Mat imgGray = new Mat();

        Imgproc.resize(img,img,getOptimizedSize(img.size()));
        Imgproc.cvtColor(img, imgGray, Imgproc.COLOR_BGR2GRAY);
        Imgcodecs.imwrite("C:\\MI\\TextDetectionDemo\\images\\changes\\2_imgGray.png", imgGray);

        imgGray = setContrast(imgGray);
        Imgcodecs.imwrite("C:\\MI\\TextDetectionDemo\\images\\changes\\2_imgGrayMod.png", imgGray);
        List<double[]> aranyok = getSotetArany(imgGray);

        //printArany(aranyok);
        //showAranyok(imgGray,aranyok);
        imgGray = setContrast(imgGray,aranyok);
        Imgcodecs.imwrite("C:\\MI\\TextDetectionDemo\\images\\changes\\grayNew.png", imgGray);


        ITesseract instance = new Tesseract();
        instance.setLanguage("hun");
        //instance.setOcrEngineMode(3);
//        instance.setTessVariable("preserve_interword_spaces","1");

        File image = new File("C:\\MI\\TextDetectionDemo\\images\\changes\\grayNew.png");
        String result = instance.doOCR(image);
        System.out.println(result);
    }

    //Átalakítani, ha az egyik oldala hosszabb mint az optimális, fontps, hogy az arány megmaradjon
    private static Size getOptimizedSize(Size originalSize){
        Size optimalSize = new Size();
        double difference = 0;
        if(originalSize.width>MAX_WIDTH){
            difference = originalSize.width-MAX_WIDTH;
        }
        else if(originalSize.height > MAX_HEIGHT){
            difference = originalSize.width-MAX_WIDTH;
        }

        optimalSize.width = originalSize.width-difference;
        optimalSize.height = originalSize.height-difference;

        return optimalSize;
    };

    private void parseLines(String ocrResult){
        List<String> lines = new ArrayList<String>();


    }

    private static Mat setContrast(Mat img,List<double[]> aranyok){
        for(double[] e : aranyok){
            img = setContrast(img,e);
        }
        return img;
    }

    private static Mat setContrast(Mat img, double[] arany){
        System.out.println("Current rect: ["+arany[0]+","+arany[1]+"] ["+arany[2]+","+arany[3]+"] arany: "+arany[4]);
        System.out.println(img.width()+"X"+img.height());
        Mat aux = img.colRange((int)arany[2],(int)arany[3]).rowRange((int)arany[0],(int)arany[1]);
        Mat sub = img.submat((int)arany[0],(int)arany[1],(int)arany[2],(int)arany[3]);
        sub = setContrast(sub,arany[4]);
        sub.copyTo(aux);
        return img;
    }

    private static Mat setContrast(Mat img, double arany){
        System.out.println("Image channels: "+img.channels());


        double min = 1.15*arany;
        double max = 0.85*arany;

        for(int i = 0;i <img.rows();i++){
            for(int j=0;j<img.cols();j++){
                double[] values = img.get(i,j);
                double val = values[0];
                if(val >= min) {
                    img.put(i, j, new double[]{256});
                }
                else if(val < max){
                    img.put(i,j,new double[]{50});
                }
            }
        }

        return img;
    }

    private static Mat setContrast(Mat img){
        System.out.println("Image channels: "+img.channels());

        for(int i = 0;i <img.rows();i++){
            for(int j=0;j<img.cols();j++){
                double[] values = img.get(i,j);
                double val = values[0];
                if(val >= 110){
                    img.put(i,j,new double[]{256});
                }
                else if(val < 85){
                    img.put(i,j,new double[]{0});
                }
            }
        }

        return img;
    }

    private static double sotetebbekAranya(Mat img, double val){

        int pos = 0;

        for(int i=0;i<img.rows();i++){
            for(int j=0; j<img.cols();j++){
                if(img.get(i,j)[0] >= val){
                    pos++;
                }
            }
        }

        int x = (img.height()*img.width())/100;

        return pos > 0 ? pos/x : 0;
    }

    private static List<double[]> getSotetArany(Mat img){

        List<double[]> result = new ArrayList<double[]>();
        for(int j=100;j<img.height();j+=100){
            for(int i=100;i<img.width();i+=100){
                if(i>img.width()) continue;
                Mat rec = img.submat(j-100,j,i-100,i);

                double arany = sotetebbekAranya(rec,85);
                result.add(new double[]{j-100,j,i-100,i,arany});
            }

        }

        return result;
    }

    private static void printArany(List<double[]> aranyok){
        for(double[] e : aranyok){
            System.out.println("Current rect: ["+e[0]+","+e[1]+"] ["+e[2]+","+e[3]+"] arany: "+e[4]);
        }

    }

    private static void showAranyok(Mat img,List<double[]> aranyok){
        for(double[] e : aranyok){
            Point p = new Point();
            p.x = e[0];
            p.y = e[1];
            Point p2 = new Point();
            p2.x = e[2];
            p2.y = e[3];
            Imgproc.rectangle(img,p,p2,new Scalar(0,0,255));

        }

        Imgcodecs.imwrite("C:\\MI\\TextDetectionDemo\\images\\changes\\imgArany.png", img);
    }

}
