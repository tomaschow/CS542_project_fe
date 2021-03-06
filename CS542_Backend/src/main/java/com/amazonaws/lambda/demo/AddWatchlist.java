package com.amazonaws.lambda.demo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;

public class AddWatchlist implements RequestStreamHandler {
	JSONParser parser = new JSONParser();
    @Override
    public void handleRequest(InputStream input, OutputStream output, Context context) throws IOException {
    	LambdaLogger logger = context.getLogger();
        logger.log("Loading Java Lambda handler of ProxyWithStream");
        
        BufferedReader reader = new BufferedReader(new InputStreamReader(input));
        JSONObject responseJson = new JSONObject();
        String responseCode = "200";
        
        int userId = -1;
        int carId = -1;
        
        try {
        	JSONObject event = (JSONObject)parser.parse(reader);
        	logger.log(event.toString());
        	if ( event.get("userId") != null) {
            	userId = Integer.parseInt((String)event.get("userId"));
            }
        	if ( event.get("carId") != null) {
            	carId = Integer.parseInt((String)event.get("carId"));
            }
            
            addWatchlist(userId, carId, context);
            
            JSONObject responseBody = new JSONObject();
            responseBody.put("input", event.toString());

            responseJson.put("isBase64Encoded", false);
            responseJson.put("statusCode", responseCode);
            responseJson.put("body", responseBody.toString());  

        } catch(Exception pex) {
            logger.log(pex.toString());
            logger.log("" + pex.getStackTrace()[0].getLineNumber());
        }
        
        logger.log(responseJson.toString());
        OutputStreamWriter writer = new OutputStreamWriter(output, "UTF-8");
        writer.write(responseJson.toString());  
        writer.close();
    }
	private void addWatchlist(int userId, int carId, Context context) {
		LambdaLogger logger = context.getLogger();
		try {
    		String url = "jdbc:mysql://cardb.clnm8zsvchg3.us-east-2.rds.amazonaws.com:3306";
    	    String username = "calcAdmin";
    	    String dbpassword = "rootmasterpassword";

    	    Connection conn = DriverManager.getConnection(url, username, dbpassword);
    	    Statement stmt = conn.createStatement();
    	    
    	    //	Add new car
    	    String newWatchlist = String.format("INSERT INTO innodb.Watchlist (userId, carId)"
    	    		+ " VALUES (%d, %d)",
    	    		userId, carId);
    	    stmt.executeUpdate(newWatchlist);
    	    
    	    stmt.close();
    	    conn.close();

    	} catch (Exception e) {
    	    e.printStackTrace();
    	    logger.log("Caught exception: " + e.getMessage());
    	}
	}

}
