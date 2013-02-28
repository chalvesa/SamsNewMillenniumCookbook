<?php

/*
 * Following code will create a new comment row for a recipe
 */

// array for JSON response
$response = array();

// check for required fields
if (isset($_GET["recipeName"])) {
//if(true){
    
	//var_dump($_GET);
	
	$recipeName = $_GET['recipeName'];		
    $comment = $_GET["comment"];
    $rating = $_GET["rating"];
    $authorName = $_GET["authorName"];

    //var_dump( $error);

    // include db connect class
    require_once __DIR__ . '/db_connect.php';

    // connecting to db
    $db = new DB_CONNECT();

    // mysql inserting a new row
    $result = mysql_query("INSERT INTO recipecomments(recipeName, comment, rating, authorName)"
					."VALUES('$recipeName', '$comment', '$rating', '$authorName')");

	
    // check if row inserted or not
    if ($result) {
        // successfully inserted into database
        $response["success"] = 1;
        $response["message"] = "Comment successfully created.";

        // echoing JSON response
        echo json_encode($response);
    } else {
        // failed to insert row
		$error = mysql_error();
        $response["success"] = 0;
        $response["message"] = $error;
        
        // echoing JSON response
        echo json_encode($response);
    }
} else {
    // required field is missing
    $response["success"] = 0;
    $response["message"] = "Required field(s) is missing";

    // echoing JSON response
    echo json_encode($response);
}
?>