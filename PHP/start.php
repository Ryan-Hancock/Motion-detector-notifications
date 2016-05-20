<?php
require_once 'DBconfig.php';
$name= isset($_POST['name']) ? $_POST['name'] : '';
$set= isset($_POST['set']) ? $_POST['set'] : '';
$response = array();
$conn = new mysqli(DB_HOST, DB_USER, DB_PASSWORD, DB_DATABASE);
  $upsql = "UPDATE users SET remote = '$set' WHERE unique_id='$name'";
    if ($conn->query($upsql) === TRUE) {
       $response['setError']="success";
    } else {
        $response['setError']= $conn->error;
    }
$conn->close();
echo json_encode($response);