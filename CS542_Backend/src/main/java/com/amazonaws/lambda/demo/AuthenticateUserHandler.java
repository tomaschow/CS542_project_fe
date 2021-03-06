package com.amazonaws.lambda.demo;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class AuthenticateUserHandler implements RequestStreamHandler {
    JSONParser parser = new JSONParser();

    @Override
    public void handleRequest(InputStream input, OutputStream output, Context context) throws IOException {
        LambdaLogger logger = context.getLogger();
        logger.log("Loading Java Lambda handler of ProxyWithStream");

        BufferedReader reader = new BufferedReader(new InputStreamReader(input));
        JSONObject responseJson = new JSONObject();
        String responseCode = "200";

        String user_name = "";
        String password = "";

        try {
            JSONObject event = (JSONObject) parser.parse(reader);
            logger.log(event.toString());
            if (event.get("user_name") != null) {
                user_name = (String) event.get("user_name");
            }
            if (event.get("password") != null) {
                password = (String) event.get("password");
            }

            JSONObject result = checkUser(user_name, password, context);
            JSONObject responseBody = new JSONObject();
            if (result != null) responseBody=result;
            else {
                responseBody.put("userId", "Fail login");
                responseCode = "401";
            }

            responseBody.put("input", event.toString());

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

    private JSONObject checkUser(String user_name, String password, Context context) {
        LambdaLogger logger = context.getLogger();
        JSONObject rs = new JSONObject();

        try {
            String url = "jdbc:mysql://cardb.clnm8zsvchg3.us-east-2.rds.amazonaws.com:3306";
            String username = "calcAdmin";
            String dbpassword = "rootmasterpassword";

            Connection conn = DriverManager.getConnection(url, username, dbpassword);
            Statement stmt = conn.createStatement();

            String checkUser = String.format("select id, user_name, email from innodb.User where user_name = '%s' and password = '%s'",
                    user_name, password);
            logger.log(checkUser);
            ResultSet resultSet = stmt.executeQuery(checkUser);

            while (resultSet.next()) {
                rs.put("userId", resultSet.getInt("id"));
                rs.put("user_name", resultSet.getString("user_name"));
                rs.put("email", resultSet.getString("email"));
            }

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
