package com.amazonaws.lambda.demo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;

public class GetMakeHandler implements RequestStreamHandler {
    JSONParser parser = new JSONParser();

    @Override
    public void handleRequest(InputStream input, OutputStream output, Context context) throws IOException {
        LambdaLogger logger = context.getLogger();
        logger.log("Loading Java Lambda handler of ProxyWithStream");

        BufferedReader reader = new BufferedReader(new InputStreamReader(input));
        JSONObject responseJson = new JSONObject();
        String responseCode = "200";

        try {
            JSONObject event = (JSONObject) parser.parse(reader);
            logger.log(event.toString());

            JSONObject makeList = listMakes(context);

            JSONObject responseBody = new JSONObject();
            responseBody.put("input", event.toString());
            responseBody.put("makeList", makeList.toString());

            responseJson.put("isBase64Encoded", false);
            responseJson.put("statusCode", responseCode);
            responseJson.put("body", responseBody.toString());

        } catch (Exception pex) {
            logger.log(pex.toString());
            logger.log("" + pex.getStackTrace()[0].getLineNumber());
        }

        logger.log(responseJson.toString());
        OutputStreamWriter writer = new OutputStreamWriter(output, "UTF-8");
        writer.write(responseJson.toString());
        writer.close();
    }

    private JSONObject listMakes(Context context) {
        LambdaLogger logger = context.getLogger();
        JSONObject rs = new JSONObject();
        try {
            String url = "jdbc:mysql://cardb.clnm8zsvchg3.us-east-2.rds.amazonaws.com:3306";
            String username = "calcAdmin";
            String dbpassword = "rootmasterpassword";

            Connection conn = DriverManager.getConnection(url, username, dbpassword);
            Statement stmt = conn.createStatement();

            String query = "select * from innodb.Make as make;";
            ResultSet resultSet = stmt.executeQuery(query);
            logger.log(resultSet.toString());

            JSONArray makeList = new JSONArray();
            while (resultSet.next()) {
                JSONObject make = new JSONObject();
                make.put("makeId", resultSet.getString("make.id"));
                make.put("makeName", resultSet.getString("make.name"));
                makeList.add(make);
            }

            rs.put("makes", makeList);

            resultSet.close();

            stmt.close();
            conn.close();

        } catch (Exception e) {
            e.printStackTrace();
            logger.log("Caught exception: " + e.getMessage());
            logger.log("" + e.getStackTrace()[0].getLineNumber());
        }
        return rs;
    }

}
