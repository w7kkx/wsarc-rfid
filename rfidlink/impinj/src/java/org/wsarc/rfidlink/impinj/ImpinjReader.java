package org.wsarc.rfidlink.impinj;
/**
 * Created by root on 6/14/17.
 */

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.UnknownHostException;

public class ImpinjReader {

    /*
    IMPINJ SPEEDWAY READER - RAMROD RFID TAG ACQUISITION PROGRAM
    */
    // The IP address or hostname of your reader
    // final String READER_HOSTNAME = "SpeedwayR-10-27-52";
    final static String READER_HOSTNAME = "169.254.126.010";
    // The TCP port specified in Speedway Connect
    final static int READER_PORT = 14150;
    final static String RFID_PREFIX = "300833B2DDD";
    final static int RFID_SUFFIX_OFFSET = 18;

    static volatile boolean shouldRunThread = true;

    public static void main(String[] args) {

        try {
            // Create a TCP socket connection to the reader
            Socket s = new Socket(READER_HOSTNAME, READER_PORT);
            // Create a BufferedReader object from the socket connection

            final BufferedReader br = new BufferedReader(new InputStreamReader(s.getInputStream()));

            ImpinjParser parser = new ImpinjParser();

            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    String line = null;
                    try {
                        while (shouldRunThread && (line = br.readLine()) != null) {
                            parser.parse(line);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
            t.start();
            // do other things here
            // .
            // .
            // .

            // if we are done doing other things but still want out thread to run we should wait for it
            t.wait();

        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    // call this to stop request nicely that the thread should stop processing
    public static void shutdownThread(){
        shouldRunThread = false;
    }

    static class ImpinjParser {

        int recordLength = 0; // Used to detect a change in reader
        // output data
        int tagIndex = -1; //Relative pointer to start of record's
        //  RFID tag
        int tagLast5Index = 0;
        boolean validTag = true;


        public void parse(String impinjRcd) {
// Adjust pointer to tag# only if needed
            if (recordLength != impinjRcd.length()) {
                tagIndex = impinjRcd.indexOf(RFID_PREFIX);
                if (tagIndex >= 0) { // VALID TAG
                    tagLast5Index = impinjRcd.indexOf(RFID_PREFIX)
                            + RFID_SUFFIX_OFFSET;
                    recordLength = impinjRcd.length();
                }
            }

// Verify only the first 5 characters of the RFID_PREFIX
            if (tagIndex >= 0 && impinjRcd.regionMatches(tagIndex,
                    RFID_PREFIX, 0, 4)) {
                String rfidString = impinjRcd.substring(tagLast5Index, 5);
                System.out.println("RFID " + rfidString + " within ImpinjRcd" + impinjRcd);
                int rfidNum = Integer.parseInt(rfidString);
            } else {
                System.out.println("Invalid Impinj Record: " + impinjRcd);
            }
        }
    }
}



// private int ConvertRFIDtoBIBNUM(int rfid)
// Prepend time
// Append to the running sequency file
// Display to the Rider Display textarea

