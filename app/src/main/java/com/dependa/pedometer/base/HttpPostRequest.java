package com.dependa.pedometer.base;

import android.content.ContentValues;
import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.util.Map;

/**
 * Created by RGY on 9/22/2017.
 */

public class HttpPostRequest {

    public JSONObject POST(String _url, ContentValues _params) throws ParseException, IOException {
        HttpURLConnection urlConn = null;
        StringBuffer sbParams = new StringBuffer();

        boolean isAnd = false;

        String key;
        String value;

        for (Map.Entry<String, Object> parameter : _params.valueSet()) {
            key = parameter.getKey();
            value = parameter.getValue().toString();

            if (isAnd)
                sbParams.append("&");

            sbParams.append(key).append("=").append(value);

            if (!isAnd)
                if (_params.size() >= 2)
                    isAnd = true;
        }

        try {
            URL url = new URL(_url);
            urlConn = (HttpURLConnection) url.openConnection();

            urlConn.setRequestMethod("POST");
            urlConn.setRequestProperty("Accept-Charset", "UTF-8");
            urlConn.setRequestProperty("Context_Type", "application/x-www-form-urlencoded;cahrset=UTF-8");

            String strParams = sbParams.toString();
            OutputStream os = urlConn.getOutputStream();
            os.write(strParams.getBytes("UTF-8"));
            os.flush();
            os.close();

            if (urlConn.getResponseCode() != HttpURLConnection.HTTP_OK)
                return null;

            BufferedReader reader = new BufferedReader(new InputStreamReader(urlConn.getInputStream(), "UTF-8"));

            String line;
            String page = "";

            while ((line = reader.readLine()) != null) {
                page += line;
            }

            return new JSONObject(page);

        } catch (MalformedURLException e) { // for URL.
            e.printStackTrace();
        } catch (IOException e) { // for openConnection().
            e.printStackTrace();
        } catch (JSONException e) { // for openConnection().
            e.printStackTrace();
        } finally {
            if (urlConn != null)
                urlConn.disconnect();
        }

        return null;
    }

    public JSONObject GET(String _url, ContentValues _params) throws ParseException, IOException {
        HttpURLConnection urlConn = null;

        boolean isAnd = false;

        String key;
        String value;
        _url += "?";

        for (Map.Entry<String, Object> parameter : _params.valueSet()) {
            key = parameter.getKey();
            value = parameter.getValue().toString();

            if (isAnd)
                _url +=  "&";

            _url += key + "=" + value;

            if (!isAnd)
                if (_params.size() >= 2)
                    isAnd = true;
        }

        try {
            URL url = new URL(_url);
            urlConn = (HttpURLConnection) url.openConnection();

            BufferedReader reader = new BufferedReader(new InputStreamReader(urlConn.getInputStream(), "UTF-8"));

            String line;
            String page = "";

            while ((line = reader.readLine()) != null) {
                page += line;
            }

            return new JSONObject(page);

        } catch (MalformedURLException e) { // for URL.
            e.printStackTrace();
        } catch (IOException e) { // for openConnection().
            e.printStackTrace();
        } catch (JSONException e) { // for openConnection().
            e.printStackTrace();
        } finally {
            if (urlConn != null)
                urlConn.disconnect();
        }

        return null;
    }

    public JSONObject FileUpload(ContentValues _params, String filePath) {
        HttpURLConnection urlConn = null;
        DataOutputStream outputStream = null;
        InputStream inputStream = null;

        String twoHyphens = "--";
        String boundary = "*****" + Long.toString(System.currentTimeMillis()) + "*****";
        String lineEnd = "\r\n";

        String result = "";

        int bytesRead, bytesAvailable, bufferSize;
        byte[] buffer;
        int maxBufferSize = 1024 * 1024;

        try {
            File file = new File(filePath);
            FileInputStream fileInputStream = new FileInputStream(file);

            URL url = new URL(Constants.FILE_UPLOAD_URL);
            urlConn = (HttpURLConnection) url.openConnection();

            urlConn.setDoInput(true);
            urlConn.setDoOutput(true);
            urlConn.setUseCaches(false);

            urlConn.setRequestMethod("POST");
            urlConn.setRequestProperty("Connection", "Keep-Alive");
            urlConn.setRequestProperty("User-Agent", "Android Multipart HTTP Client 1.0");
            urlConn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

            outputStream = new DataOutputStream(urlConn.getOutputStream());
            outputStream.writeBytes(twoHyphens + boundary + lineEnd);
            outputStream.writeBytes("Content-Disposition: form-data; name=\"" + "data" + "\"; filename=\"" + _params.getAsString("email") + "\"" + lineEnd);
            outputStream.writeBytes("Content-Type: " + "text/csv" + lineEnd);
            outputStream.writeBytes("Content-Transfer-Encoding: binary" + lineEnd);

            outputStream.writeBytes(lineEnd);

            bytesAvailable = fileInputStream.available();
            bufferSize = Math.min(bytesAvailable, maxBufferSize);
            buffer = new byte[bufferSize];

            bytesRead = fileInputStream.read(buffer, 0, bufferSize);
            while (bytesRead > 0) {
                outputStream.write(buffer, 0, bufferSize);
                bytesAvailable = fileInputStream.available();
                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                bytesRead = fileInputStream.read(buffer, 0, bufferSize);
            }

            outputStream.writeBytes(lineEnd);

            for (String key : _params.keySet()) {
                String value = _params.getAsString(key);

                outputStream.writeBytes(twoHyphens + boundary + lineEnd);
                outputStream.writeBytes("Content-Disposition: form-data; name=\"" + key + "\"" + lineEnd);
                outputStream.writeBytes("Content-Type: text/plain" + lineEnd);
                outputStream.writeBytes(lineEnd);
                outputStream.writeBytes(value);
                outputStream.writeBytes(lineEnd);
            }

            outputStream.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

            outputStream.flush();
            outputStream.close();

            if (urlConn.getResponseCode() != HttpURLConnection.HTTP_OK)
                return null;

            BufferedReader reader = new BufferedReader(new InputStreamReader(urlConn.getInputStream(), "UTF-8"));

            String line;
            String page = "";

            while ((line = reader.readLine()) != null) {
                page += line;
            }

            return new JSONObject(page);

        } catch (Exception e) {
            return null;
        }
    }

    public boolean FileDownload(Context context, String email, String machineId, String filePath) {
        try {

            URL downloadFileUrl = new URL(Constants.FILE_DOWNLOAD_URL + "/" + email + "/" + machineId + "/");
            HttpURLConnection httpURLConnection = (HttpURLConnection) downloadFileUrl.openConnection();
            httpURLConnection.setRequestMethod("GET");
            httpURLConnection.setDoOutput(true);
            httpURLConnection.connect();

            File file = new File(filePath);
            file.createNewFile();
            final FileOutputStream fileOutputStream = new FileOutputStream(file);
            final byte buffer[] = new byte[1024 * 1024];

            final InputStream inputStream = httpURLConnection.getInputStream();

            int len1 = 0;
            while ((len1 = inputStream.read(buffer)) > 0) {
                fileOutputStream.write(buffer, 0, len1);
            }
            fileOutputStream.flush();
            fileOutputStream.close();


        } catch (final Exception exception) {
            exception.printStackTrace();
            return false;
        }

        return true;
    }

    private String convertStreamToString(InputStream is) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();

        String line = null;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }
}
