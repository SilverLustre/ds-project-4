<%@ page import="org.bson.Document" %>
<%@ page import="java.util.List" %>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%
    List<Document> aggregatedLog = (List<Document>) request.getAttribute("aggregatedLog");
    List<Document> topModel = (List<Document>) request.getAttribute("topModel");
    List<Document> topInput = (List<Document>) request.getAttribute("topInput");
    double durationAvg = (Double) request.getAttribute("durationAvg");
%>
<!DOCTYPE html>
<html>
<head>
    <title>Dashboard</title>
    <link rel="stylesheet" href="dashboard.css">
    <script>
        function check() {
            window.setTimeout(function () {
                window.location.reload();
            }, 5000);
        }

        function uncheck() {
            // Reference https://stackoverflow.com/questions/8860188/javascript-clear-all-timeouts
            var id = window.setTimeout(function () {}, 0);
            while (id--) {
                window.clearTimeout(id);
            }
        }

        function checkChange(checkBox) {
            if (checkBox.checked) {
                check()
            } else {
                uncheck()
            }
        }

        window.onload = function () {
            var now = new Date();
            document.getElementById("updateTime").innerText = "Updated time: " + now.toLocaleDateString() + " " + now.toLocaleTimeString();
            document.getElementById("autoUpdateCheckBox")
        }
        window.setTimeout(function () {
            window.location.reload();
        }, 5000);
    </script>
</head>
<body>
<h1><%= "Dashboard" %>
</h1>
<div>
    <input type="checkbox" id="autoUpdateCheckBox" name="autoUpdateCheckBox" onchange="checkChange(this)" checked>
    <label for="autoUpdateCheckBox"> Auto refresh every 5 seconds</label><br>
</div>
<p id="updateTime"></p>
<br/>
<a href="dashboard">Refresh</a>

<h2>Average Processing Time for Each Request</h2>
<%= durationAvg + " second(s)" %>

<h2>Operation Logs</h2>
<table class="table-style">
    <thead>
    <tr>
        <th></th>
        <th></th>
        <th></th>
        <th></th>
        <th colspan="2">Request from User</th>
        <th>Request to API</th>
        <th>Response from API</th>
        <th colspan="2">Response to User</th>
    </tr>
    <tr>
        <th>Request Id</th>
        <th>Start Time</th>
        <th>End Time</th>
        <th>Duration</th>
        <th>Input</th>
        <th>Model</th>
        <th>GET URL</th>
        <th>Original API Response</th>
        <th>Definition</th>
        <th>Image URL</th>
    </tr>
    </thead>
    <tbody>
    <% for (Document doc : aggregatedLog) {%>
    <tr>
        <td><%= doc.get("_id") %>
        </td>
        <td><%= doc.getOrDefault("start_time", "") %>
        </td>
        <td><%= doc.getOrDefault("end_time", "") %>
        </td>
        <td><%= doc.getOrDefault("duration", "") %>
        </td>
        <td><%= doc.get("request_from_user", Document.class).getOrDefault("input", "") %>
        </td>
        <td><%= doc.get("request_from_user", Document.class).getOrDefault("model", "") %>
        </td>
        <td><%= doc.get("request_to_api", Document.class).getOrDefault("get_url", "") %>
        </td>
        <td><%= doc.get("response_from_api", Document.class).toJson() %>
        </td>
        <td><%= doc.get("response_to_user", Document.class).getOrDefault("definition", "") %>
        </td>
        <td><%= doc.get("response_to_user", Document.class).getOrDefault("image_url", "") %>
        </td>
    </tr>
    <%}%>
    </tbody>
</table>

<h2>Top 5 Android Models</h2>
<table class="table-style">
    <thead>
    <tr>
        <th>Model</th>
        <th>Count</th>
    </tr>
    </thead>
    <tbody>
    <% for (Document doc : topModel) {%>
    <tr>
        <td><%= doc.get("_id") %>
        </td>
        <td><%= doc.get("model_count") %>
        </td>
    </tr>
    <%}%>
    </tbody>
</table>

<h2>Top 10 Input</h2>
<table class="table-style">
    <thead>
    <tr>
        <th>Input</th>
        <th>Count</th>
    </tr>
    </thead>
    <tbody>
    <% for (Document doc : topInput) {%>
    <tr>
        <td><%= doc.get("_id") %>
        </td>
        <td><%= doc.get("input_count") %>
        </td>
    </tr>
    <%}%>
    </tbody>
</table>

</body>
</html>