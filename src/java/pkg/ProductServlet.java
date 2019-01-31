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
import java.io.PrintWriter;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author Len Payne <len.payne@lambtoncollege.ca>
 */
@WebServlet("/products")
public class ProductServlet extends HttpServlet {

    @PersistenceContext(unitName = "CSD4464-Servlet-Sample-2019WPU")
    EntityManager em;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) {

        List<Product> products;
        Query q;
        res.setHeader("Content-Type", "application/xml");
        try (PrintWriter out = res.getWriter()) {
            String productId = req.getParameter("product_id");
            
            if (productId != null) {
                q = em.createQuery("SELECT p FROM Product p WHERE p.product_id = :product_id");
                q.setParameter("product_id", Integer.parseInt(productId));
            } else {
                q = em.createQuery("SELECT p FROM Product p");
            }
            products = q.getResultList();
            out.println("<products>");
            for (Product p : products) {
                out.println("<product id=\"" + p.getProduct_id() + "\" />");
            }
            out.println("</products>");
        } catch (IOException ex) {
            Logger.getLogger(HelloServlet.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
