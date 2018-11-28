package angers.univ.ctalarmain.qrludo.utils;

import android.os.AsyncTask;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;


/**
 * Created by etudiant on 26/01/18.
 */

public class FileDowloader extends AsyncTask {

    public static String DOWNLOAD_PATH = "/sdcard/qrludo/";

    String m_url;
    FileDownloaderObserverInterface m_user;
    String m_id;
    String m_path;

    public FileDowloader(String id, FileDownloaderObserverInterface user){
        m_url = id;
        m_user = user;
        if(id.contains("id=")) {
            m_id = id.split("id=")[1];
            System.out.println("here");
        }
        else
            m_id=id;
        m_path = FileDowloader.DOWNLOAD_PATH+m_id+".mp3";


        // Creating qrludo dir if doesn't exist
        File targetDir = new File(FileDowloader.DOWNLOAD_PATH);
        if (!targetDir.exists()) {
            targetDir.mkdirs();
        }

    }

    @Override
    protected Object doInBackground(Object[] objects) {

        try {
            InputStream input = null;
            OutputStream output = null;
            HttpURLConnection connection = null;
            URL url = new URL(m_url);
            connection = (HttpURLConnection) url.openConnection();
            connection.connect();
            System.out.println(url.toString());

            // expect HTTP 200 OK, so we don't mistakenly save error report
            // instead of the file
            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                Log.v("test", "Mauvaise réponse http");
                return "Server returned HTTP " + connection.getResponseCode()
                        + " " + connection.getResponseMessage();
            }


            // download the file
            input = connection.getInputStream();
            output = new FileOutputStream(m_path);

            byte data[] = new byte[4096];
            int count;
            while ((count = input.read(data)) != -1) {
                // allow canceling with back button
                if (isCancelled()) {
                    input.close();
                    return null;
                }

                output.write(data, 0, count);
            }

            output.close();

            m_user.onDownloadComplete();


        }
        catch (Exception e) {
            Log.e("test", e.getMessage());
        }

        return null;

    }


    /**
     * Interface needed by the clients of FileDownloader
     */
    public interface FileDownloaderObserverInterface {
        void onDownloadComplete();
    }

    public static boolean viderMemoire(){
        File targetDir = new File(FileDowloader.DOWNLOAD_PATH);
        if (targetDir.isDirectory()){
            File[] files=targetDir.listFiles();
            for(File f : files){
                f.delete();
            }
            return true;
        }
        return false;
    }

}
