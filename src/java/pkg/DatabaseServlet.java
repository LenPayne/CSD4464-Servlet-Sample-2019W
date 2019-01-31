/*
 * The MIT License
 *
 * Copyright 2019 Len Payne <len.payne@lambtoncollege.ca>.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package pkg;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.AsyncContext;
import javax.servlet.AsyncEvent;
import javax.servlet.AsyncListener;
import javax.servlet.ServletRequest;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author Len Payne <len.payne@lambtoncollege.ca>
 */
@WebServlet(urlPatterns="/db", asyncSupported=true)
public class DatabaseServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) {
        AsyncContext ac = request.startAsync();
        ac.addListener(new AsyncListener() {

            @Override
            public void onComplete(AsyncEvent event) throws IOException {
                ServletRequest request = event.getSuppliedRequest();
                boolean isJobDone = (boolean) request.getAttribute("jobDone");
                if (isJobDone) response.getWriter().println(request.getAttribute("jobPayload"));
            }
            // Three other methods must be implemented on AsyncListeners but are unnecessary

            @Override
            public void onTimeout(AsyncEvent event) throws IOException {
            }

            @Override
            public void onError(AsyncEvent event) throws IOException {
            }

            @Override
            public void onStartAsync(AsyncEvent event) throws IOException {
            }
        });
        
        ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(10);
        executor.execute(new AsyncService(ac));
    }
    
    private class AsyncService implements Runnable {
        AsyncContext ac;
        public AsyncService(AsyncContext ac) {
            this.ac = ac;
        }
        @Override
        public void run() {
            String payload = "";
            payload = getListOfProducts();
            ServletRequest request = ac.getRequest();
            request.setAttribute("jobDone", true);
            request.setAttribute("jobPayload", payload);
            ac.complete();
        }
        
        private String getListOfProducts() {
            try {
                String result = "<table>";
                Connection conn = getConnection();
                String sql = "SELECT * FROM PRODUCT";
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql);
                while (rs.next()) {
                    result += "<tr><td>"
                            + rs.getInt("PRODUCT_ID")
                            + "</td><td>"
                            + rs.getString("DESCRIPTION")
                            + "</td><td>"
                            + rs.getDouble("PURCHASE_COST")
                            + "</td><td>"
                            + rs.getString("AVAILABLE")
                            + "</td></tr>";
                }
                result += "</table>";
                
                return result;
            } catch (SQLException ex) {
                Logger.getLogger(DatabaseServlet.class.getName()).log(Level.SEVERE, null, ex);
                return null;
            }
        }
        
        private Connection getConnection() {
            try {
                Class.forName("org.apache.derby.jdbc.ClientDriver");
                String jdbc = "jdbc:derby://localhost:1527/sample";
                Connection conn = DriverManager.getConnection(jdbc, "app", "app");
                return conn;
            } catch (ClassNotFoundException | SQLException ex) {
                Logger.getLogger(DatabaseServlet.class.getName()).log(Level.SEVERE, null, ex);
                return null;
            }
        }
    }
}
