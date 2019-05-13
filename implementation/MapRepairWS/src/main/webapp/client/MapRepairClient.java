package client;

import java.io.*;
import java.util.Base64;

public class MapRepairClient {

    public static void main(String[] args) {
        // Data to send
        String filePathPV = "/home/ucomigna/Repositories/MapRepair/implementation/example/PolicyViews.xml";
        String filePathMap = "/home/ucomigna/Repositories/MapRepair/implementation/example/MappingToRewrite.xml";

        File filePV = new File(filePathPV);
        BufferedInputStream inputStream = getBufferedInputStream(filePV);
        byte[] inputMappingBytes = getBytes(filePV, inputStream);

        File fileMap = new File(filePathMap);
        BufferedInputStream inputStreamMap = getBufferedInputStream(fileMap);
        byte[] inputMappingBytesMap = getBytes(fileMap, inputStreamMap);

        // Data to receive
        ByteArrayOutputStream resultStream = new ByteArrayOutputStream();


        // connects to the web service
        MapRepairWSService client = new MapRepairWSService();
        MapRepairWS service = client.getMapRepairWSPort();


        // WS interactions
        try {
            // resultStream.write(service.getResultVisChase(inputMappingBytes));
            resultStream.write(service.getResultRepair(inputMappingBytes, inputMappingBytesMap));
        } catch (IOException e) {
            e.printStackTrace();
        }

        String result = new String(resultStream.toByteArray());

        closeStream(inputStream);
        System.out.println("Result: " + result);
    }

    private static void closeStream(BufferedInputStream inputStream) {
        try {
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static byte[] getBytes(File file, BufferedInputStream inputStream) {
        byte[] inputMappingBytes = new byte[(int) file.length()];
        try {
            inputStream.read(inputMappingBytes);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return inputMappingBytes;
    }

    private static BufferedInputStream getBufferedInputStream(File file) {
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return new BufferedInputStream(fis);
    }
}