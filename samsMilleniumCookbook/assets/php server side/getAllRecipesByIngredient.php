<?php

/*
 * Following code will list all the recipes that contain the specified ingredients
 */

 //http://localhost/recipeApp/getAllRecipes.php?recipeType=Fish&cookTime=Any&author=Van%20Keizer&keyWord=&foodName=Any
 
// array for JSON response
$response = array();


// include db connect class
require_once __DIR__ . '/db_connect.php';

// connecting to db
$db = new DB_CONNECT();
$conn=$db->connect();

// check for post data
if (isset($_GET["list0"])) {
    $array[0] = "'".$_GET["list0"]."'";
	$i=1;
	while(isset($_GET["list".$i])){
		$array[$i] = "'".$_GET["list".$i]."'";
		$i++;
	}
	
	$ingredientName = implode(', ' , $array);
	
	
	$query="SELECT recipe.recipeName, summery, userName, prepTime, cookTime FROM recipe left join recipeingredients on recipe.recipeName= recipeingredients.recipeName "
			."Where ingredientName IN ($ingredientName)";// and important=1";
	
	//SELECT recipe.recipeName from recipe left join recipeingredients on recipe.recipeName= recipeingredients.recipeName WHERE ingredientName='Almond' and important=1
	
	
	
	
	// get all products from products table
	$result = mysqli_query($conn, $query);

	//echo "</br>$query </br></br>";
	
	
	if(!$result){
	
		// no recipes found
		$response["success"] = 0;
		$response["message"] = "No recipes found";

		// echo no users JSON
		echo json_encode($response);
	
	}
	
	
	// check for empty result
	if (mysqli_num_rows($result)) {

		$response["products"] = array();
		
		while ($row = mysqli_fetch_array($result)) {

			$rating=0;
			$numRatings=0;
			
			
			$product = array();
			$product["recipeName"] = $row[0];
			$product["summery"] = $row[1];
			$product["author"] = $row[2];
			$product["prepTime"] = $row[3];
			$product["cookTime"] = $row[4];
			$product["imageUrl"] = "http://3.bp.blogspot.com/-Hzcfxomkius/TgQ4Do1I5YI/AAAAAAAABkQ/IBIdX39Lq-4/s1600/Golden-Gun-29593.jpg";
			
			//echo "recipe name= $row[0]</br>";
			
			$countResult = mysqli_query($conn, "SELECT count(*) FROM recipecomments where recipename = '$row[0]'");
			if(mysqli_num_rows($countResult) > 0){
				$CountRow = mysqli_fetch_array($countResult);
				$numRatings=$CountRow[0];
			}
			
			$product["numRatings"] = $numRatings;
			$response["message"] = "Found $numRatings ratings";
			
			$ratingResult = mysqli_query($conn, "SELECT sum(rating) FROM recipecomments where recipename = '$row[0]'");
			if(mysqli_num_rows($ratingResult) > 0){
				$ratingRow = mysqli_fetch_array($ratingResult);
				if($CountRow[0]>0)
					$rating=$ratingRow[0]/$CountRow[0];
			}	
			$product["rating"] = $rating;
			$response["message"] .= ", found total rating of $rating";

			// push single recipe into final response array
			array_push($response["products"], $product);
		}
		// success
		//$response["message"] .= ", Query= $temp";
		$response["success"] = 1;

		// echoing JSON response
		echo json_encode($response);
	} else {
		// no products found
		$response["success"] = 0;
		$response["message"] = "No recipes found";

		// echo no users JSON
		echo json_encode($response);
	}
}else{

	// did not receive data
	$response["success"] = 0;
	$response["message"] = "Missing field(s)";

	// echo no users JSON
	echo json_encode($response);

}
?>
