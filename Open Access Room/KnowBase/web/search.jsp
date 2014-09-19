<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">        
        <title>Knowledge Base Search Page</title>
    </head>
    <body>
        <p>Knowledge Base Search Page
        <form id="knowbase_search_form" action="./associations">
            <table id="knowbase_search_table">
                <tr>
                    <td>
                        Input rule to retrieve potential associations:
                    </td>
                    <td>
                        <input type="text" name="righthand"></textarea>        
                    </td>

                    <td><input type="submit" value="Submit" /></td>
                </tr>                 
            </table>
        </form>
        
        <form id="drugcluster_search_form" action="./drugcluster">
            <table id="drugcluster_search_table">
                <tr>
                    <td>
                        Input cluster id:
                    </td>
                    <td>
                        <input type="text" name="clusterid"></textarea>        
                    </td>
                <td><input type="submit" value="Submit" /></td>
                </tr>                 
            </table>
        </form>
        
        
    </body>
</html>
