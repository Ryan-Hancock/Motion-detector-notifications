<?php
require_once 'DBconfig.php';
$name= isset($_POST['name']) ? $_POST['name'] : '';
$response;
$conn = new mysqli(DB_HOST, DB_USER, DB_PASSWORD, DB_DATABASE);
$sql = "SELECT status FROM users WHERE unique_id = '$name'";
$result = $conn->query($sql);
if ($result->num_rows > 0) {
    // output data of each row
    while($row = $result->fetch_assoc()) {
        $response = $row["status"];
    }
} else {
    $response['error']= "fail";
}
$conn->close();
echo $response;