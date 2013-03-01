<?php

/*
 * This is for requesting the all of the comments from
 * a single recipe
 */

// array for JSON response
$response = array();


// include db connect class
require_once __DIR__ . '/db_connect.php';

// connecting to db
$db = new DB_CONNECT();
$conn=$db->connect();

// check for post data
if (isset($_GET["recipeName"])) {
    $recipeName = $_GET['recipeName'];

    // get comments from recipecomments table
	$result = mysqli_query($conn, "SELECT authorName, postTime, comment, rating FROM recipecomments WHERE recipeName ='$recipeName' ORDER BY postTime DESC");
	
	// check for empty result
    if (!empty($result)) {
        $response["products"] = array();
		
        while ($row = mysqli_fetch_array($result)) {
	
			$product = array();
			
			$product["authorName"] = $row[0];
			$product["postTime"] = $row[1];
			$product["comment"] = $row[2];
			$product["rating"] = $row[3];	
			
			array_push($response["products"], $product);
    }
    // success
    $response["success"] = 1;
	$response["message"] = "All is well";
	
	
    // echoing JSON response
    echo json_encode($response);
	
	
    } else {
        // no Comments found
        $response["success"] = 0;
        $response["message"] = "No Comments found";

        // echo no users JSON
        echo json_encode($response);
    }
} else {
    // required field is missing
    $response["success"] = 0;
    $response["message"] = "recipeName is missing";

    // echoing JSON response
	echo json_encode($response);
}
?>