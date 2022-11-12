package com.dsproj.dictionaryweb;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;

@WebServlet(name = "dictionaryServlet", value = "/dictionary")
public class DictionaryServlet extends HttpServlet {

    public void init() {

    }

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

        String input = request.getParameter("input");
        System.out.println("Get input:" + input);

        // response
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        DictionaryAPICaller dictionaryAPICaller = new DictionaryAPICaller();
        String jsonResult = dictionaryAPICaller.sendGETSync(input);
        out.println(jsonResult);
    }

    public void destroy() {
    }
}