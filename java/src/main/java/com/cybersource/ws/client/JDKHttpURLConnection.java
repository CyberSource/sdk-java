/*
 * Copyright 2003-2014 CyberSource Corporation
 *
 * THE SOFTWARE AND THE DOCUMENTATION ARE PROVIDED ON AN "AS IS" AND "AS
 * AVAILABLE" BASIS WITH NO WARRANTY.  YOU AGREE THAT YOUR USE OF THE SOFTWARE AND THE
 * DOCUMENTATION IS AT YOUR SOLE RISK AND YOU ARE SOLELY RESPONSIBLE FOR ANY DAMAGE TO YOUR
 * COMPUTER SYSTEM OR OTHER DEVICE OR LOSS OF DATA THAT RESULTS FROM SUCH USE. TO THE FULLEST
 * EXTENT PERMISSIBLE UNDER APPLICABLE LAW, CYBERSOURCE AND ITS AFFILIATES EXPRESSLY DISCLAIM ALL
 * WARRANTIES OF ANY KIND, EXPRESS OR IMPLIED, WITH RESPECT TO THE SOFTWARE AND THE
 * DOCUMENTATION, INCLUDING ALL WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE,
 * SATISFACTORY QUALITY, ACCURACY, TITLE AND NON-INFRINGEMENT, AND ANY WARRANTIES THAT MAY ARISE
 * OUT OF COURSE OF PERFORMANCE, COURSE OF DEALING OR USAGE OF TRADE.  NEITHER CYBERSOURCE NOR
 * ITS AFFILIATES WARRANT THAT THE FUNCTIONS OR INFORMATION CONTAINED IN THE SOFTWARE OR THE
 * DOCUMENTATION WILL MEET ANY REQUIREMENTS OR NEEDS YOU MAY HAVE, OR THAT THE SOFTWARE OR
 * DOCUMENTATION WILL OPERATE ERROR FREE, OR THAT THE SOFTWARE OR DOCUMENTATION IS COMPATIBLE
 * WITH ANY PARTICULAR OPERATING SYSTEM.
 */

package com.cybersource.ws.client;

import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import static com.cybersource.ws.client.Utility.*;


/**
 * Class helps in posting the Request document for the Transaction using URLConnection.
 * Converts the document to String format and also helps in setting up the Proxy connections.
 *
 * @author sunagara
 */
public class JDKHttpURLConnection extends Connection {
    private boolean _isRequestSent = false;
    private HttpURLConnection con = null;


    /**
     * Constructor.
     * @param mc
     * @param builder
     * @param logger
     */
    JDKHttpURLConnection(
            MerchantConfig mc, DocumentBuilder builder, LoggerWrapper logger) {
        super(mc, builder, logger);
        logger.log(Logger.LT_INFO, "Using JDKHttpURLConnection for connections.");
    }

    /**
     * Post request by jdkHttpURL connection
     * @param request
     * @param requestSentTime
     * @throws IOException
     * @throws TransformerException
     */
    void postDocument(Document request, long requestSentTime)
            throws IOException,
            TransformerException {
        //long startTime = System.nanoTime();
        String serverURL = mc.getEffectiveServerURL();
        URL url = new URL(serverURL);
        con = ConnectionHelper.openConnection(url, mc);
        con.setRequestProperty(Utility.ORIGIN_TIMESTAMP, String.valueOf(System.currentTimeMillis()));
        con.setRequestProperty(Utility.SDK_ELAPSED_TIMESTAMP, String.valueOf(System.currentTimeMillis() - requestSentTime));
        con.setRequestMethod("POST");
        con.setDoOutput(true);
        ConnectionHelper.setTimeout(con, mc.getTimeout());
        logRequestHeaders();
        OutputStream out = con.getOutputStream();
        byte[] requestBytes = documentToByteArray(request);
        logger.log(Logger.LT_INFO,
                "Sending " + requestBytes.length + " bytes to " + serverURL);
        out.write(requestBytes);
        out.close();
        //System.out.println("JDKHttpURLConnection.postDocument time taken is " + TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime) + " ms");
        _isRequestSent = true;
    }

    /**
     * Method to check is request sent or not
     * @return boolean
     */
    /* (non-Javadoc)
     * @see com.cybersource.ws.client.Connection#isRequestSent()
     */
    public boolean isRequestSent() {
        return _isRequestSent;
    }

    /**
     * Method to release the connections
     */
    /* (non-Javadoc)
     * @see com.cybersource.ws.client.Connection#release()
     */
    public void release() {
        con = null;
    }

    /**
     * Method to get http response code
     * @return int
     * @throws IOException
     */
    /* (non-Javadoc)
     * @see com.cybersource.ws.client.Connection#getHttpResponseCode()
     */
    int getHttpResponseCode()
            throws IOException {
        return con != null ? con.getResponseCode() : -1;
    }

    /**
     * Method to get response error stream
     * @return InputStram
     * @throws IOException
     */
    /* (non-Javadoc)
     * @see com.cybersource.ws.client.Connection#getResponseStream()
     */
    InputStream getResponseStream()
            throws IOException {
        return con != null ? con.getInputStream() : null;
    }

    /**
     * Method to get response error stream
     * @return InputStream
     */
    /* (non-Javadoc)
     * @see com.cybersource.ws.client.Connection#getResponseErrorStream()
     */
    InputStream getResponseErrorStream() {
        return con != null ? con.getErrorStream() : null;
    }

    /**
     * Converts Document to byte array stream
     *
     * @param doc - Document document
     * @return - byte array stream.
     * @throws TransformerConfigurationException
     * @throws TransformerException
     * @throws IOException
     */
    private static byte[] documentToByteArray(Document doc)
            throws TransformerConfigurationException, TransformerException,
            IOException {
        ByteArrayOutputStream baos = null;
        try {
            baos = makeStream(doc);
            return baos.toByteArray();
        } finally {
            if (baos != null) {
                baos.close();
            }
        }
    }

    /**
     * Log Response Headers
     */
    @Override
    public void logResponseHeaders() {
        if (mc.getEnableLog() && con != null) {
            String responseTime = con.getHeaderField(RESPONSE_TIME_REPLY);
            if (StringUtils.isNotBlank(responseTime)) {
                long resIAT = getResponseIssuedAtTimeInSecs(responseTime);
                if (resIAT > 0) {
                    logger.log(Logger.LT_INFO, "responseTransitTimeSec : " + getResponseTransitTime(resIAT));
                }
            }
            logger.log(Logger.LT_INFO, "Response headers : " + con.getHeaderFields());
        }
    }

    /**
     * Log Request Headers
     */
    @Override
    public void logRequestHeaders() {
        if (mc.getEnableLog() && con != null) {
            logger.log(Logger.LT_INFO, "Request Headers : " + con.getRequestProperties());
        }
    }
}

/* Copyright 2006 CyberSource Corporation */

