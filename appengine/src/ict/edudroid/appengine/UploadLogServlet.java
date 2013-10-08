package ict.edudroid.appengine;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;

public class UploadLogServlet extends HttpServlet {
	private BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();
	
    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp)
        throws ServletException, IOException {
    	Map<String, BlobKey> blobs = blobstoreService.getUploadedBlobs(req);
    	resp.setStatus(HttpServletResponse.SC_OK);
		resp.setContentType("text/plain");

		PrintWriter out = resp.getWriter();
		out.print(blobs.get("logFile").getKeyString());
		out.flush();
		out.close();
    }
    
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
    		throws ServletException, IOException {
    	Map<String, BlobKey> blobs = blobstoreService.getUploadedBlobs(req);
    	
        Entity log = new Entity("Logs");
        log.setProperty("imei", req.getParameter("imei"));
        log.setProperty("logFileBlobKey", req.getParameter("blobkey"));
        log.setProperty("date", new Date());
        
        DatastoreService datastore =
                DatastoreServiceFactory.getDatastoreService();
        datastore.put(log);
    }
}
